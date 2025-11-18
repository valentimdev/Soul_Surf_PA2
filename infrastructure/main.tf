terraform {
  required_version = ">= 1.5.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.70"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.1"
    }
  }
}

provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

# ------------------------------
# Resource Group
# ------------------------------
resource "azurerm_resource_group" "rg" {
  name     = "${var.project_name}-rg"
  location = var.location

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# ------------------------------
# Storage Account (para fotos/arquivos)
# ------------------------------
resource "random_integer" "rand" {
  min = 10000
  max = 99999
}

resource "azurerm_storage_account" "storage" {
  name                     = "${var.project_name}st${random_integer.rand.result}"
  resource_group_name      = azurerm_resource_group.rg.name
  location                 = azurerm_resource_group.rg.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"

  blob_properties {
    cors_rule {
      allowed_headers    = ["*"]
      allowed_methods    = ["DELETE", "GET", "HEAD", "MERGE", "POST", "OPTIONS", "PUT"]
      allowed_origins    = ["*"]
      exposed_headers    = ["*"]
      max_age_in_seconds = 200
    }
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# ------------------------------
# Blob Container para imagens
# ------------------------------
resource "azurerm_storage_container" "images" {
  name                  = "images"
  storage_account_name  = azurerm_storage_account.storage.name
  container_access_type = "blob"
}

# ------------------------------
# PostgreSQL Database
# ------------------------------
resource "azurerm_postgresql_flexible_server" "database" {
  name                   = "${var.project_name}-db"
  resource_group_name    = azurerm_resource_group.rg.name
  location               = azurerm_resource_group.rg.location
  version                = "15"
  administrator_login    = var.db_admin_username
  administrator_password = var.db_admin_password
  zone                   = "1"

  storage_mb = 32768
  sku_name   = "B_Standard_B1ms"

  backup_retention_days        = 7
  geo_redundant_backup_enabled = false

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "azurerm_postgresql_flexible_server_database" "app_database" {
  name      = var.database_name
  server_id = azurerm_postgresql_flexible_server.database.id
  collation = "en_US.utf8"
  charset   = "utf8"
}

# ------------------------------
# PostgreSQL Firewall Rules
# ------------------------------
# Permitir acesso do Azure Services
resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_azure_services" {
  name             = "AllowAzureServices"
  server_id        = azurerm_postgresql_flexible_server.database.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

# Permitir acesso do backend App Service (será atualizado após criação)
resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_backend" {
  name             = "AllowBackendAppService"
  server_id        = azurerm_postgresql_flexible_server.database.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "255.255.255.255"
  # Nota: Em produção, considere usar Private Endpoints para melhor segurança
}

# ------------------------------
# App Service Plan
# ------------------------------
resource "azurerm_service_plan" "asp" {
  name                = "${var.project_name}-plan"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  os_type             = "Linux"
  sku_name            = var.app_service_sku

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# ------------------------------
# Managed Identity para Backend
# ------------------------------
resource "azurerm_user_assigned_identity" "backend_identity" {
  name                = "${var.project_name}-backend-identity"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Role assignment para acesso ao Storage Account
resource "azurerm_role_assignment" "backend_storage_blob_contributor" {
  scope                = azurerm_storage_account.storage.id
  role_definition_name = "Storage Blob Data Contributor"
  principal_id         = azurerm_user_assigned_identity.backend_identity.principal_id
}

# ------------------------------
# Backend Web App
# ------------------------------
resource "azurerm_linux_web_app" "backend" {
  name                = "${var.project_name}-backend"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  service_plan_id     = azurerm_service_plan.asp.id

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.backend_identity.id]
  }

  site_config {
    always_on = var.environment == "production" ? true : false
    
    application_stack {
      java_version = var.backend_stack
    }

    # Habilitar WebSocket
    websockets_enabled = true

    # HTTPS Only
    https_only = true

    # CORS - usando depends_on para garantir que frontend seja criado primeiro
    cors {
      allowed_origins = concat(
        [
          "https://${azurerm_linux_web_app.frontend.default_hostname}",
        ],
        var.environment == "production" ? [] : [
          "http://localhost:3000",
          "http://localhost:5173"
        ]
      )
      support_credentials = true
    }
  }

  app_settings = {
    "WEBSITES_PORT"                    = "8080"
    "BLOB_CONNECTION"                  = azurerm_storage_account.storage.primary_connection_string
    "AZURE_STORAGE_ACCOUNT_NAME"        = azurerm_storage_account.storage.name
    "AZURE_STORAGE_CONTAINER_NAME"     = azurerm_storage_container.images.name
    "SPRING_DATASOURCE_URL"            = "jdbc:postgresql://${azurerm_postgresql_flexible_server.database.fqdn}:5432/${var.database_name}?sslmode=require"
    "SPRING_DATASOURCE_USERNAME"       = var.db_admin_username
    "SPRING_DATASOURCE_PASSWORD"       = var.db_admin_password
    "SPRING_JPA_HIBERNATE_DDL_AUTO"    = "update"
    "SPRING_JPA_SHOW_SQL"              = var.environment == "development" ? "true" : "false"
    "JWT_SECRET"                       = var.jwt_secret
    "JWT_EXPIRATION"                   = "86400000"
    "MAIL_HOST"                        = var.mail_host
    "MAIL_PORT"                        = tostring(var.mail_port)
    "MAIL_USERNAME"                    = var.mail_username
    "MAIL_PASSWORD"                    = var.mail_password
    "WEATHER_API_URL"                  = var.weather_api_url
    "WEATHER_API_KEY"                  = var.weather_api_key
    "FRONTEND_URL"                     = "https://${azurerm_linux_web_app.frontend.default_hostname}"
    "APPINSIGHTS_INSTRUMENTATIONKEY"   = azurerm_application_insights.insights.instrumentation_key
    "APPLICATIONINSIGHTS_CONNECTION_STRING" = azurerm_application_insights.insights.connection_string
  }

  depends_on = [
    azurerm_linux_web_app.frontend,
    azurerm_application_insights.insights
  ]

  logs {
    detailed_error_messages = true
    failed_request_tracing  = true
    http_logs {
      file_system {
        retention_in_days = 7
        retention_in_mb  = 100
      }
    }
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# ------------------------------
# Frontend Web App
# ------------------------------
resource "azurerm_linux_web_app" "frontend" {
  name                = "${var.project_name}-frontend"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  service_plan_id     = azurerm_service_plan.asp.id

  site_config {
    always_on = var.environment == "production" ? true : false
    
    application_stack {
      node_version = var.frontend_stack
    }

    # HTTPS Only
    https_only = true
  }

  app_settings = {
    "VITE_BACKEND_URL" = "https://${azurerm_linux_web_app.backend.default_hostname}"
    "VITE_WS_URL"      = "wss://${azurerm_linux_web_app.backend.default_hostname}/ws"
    "NODE_ENV"         = var.environment
  }

  logs {
    detailed_error_messages = true
    failed_request_tracing  = true
    http_logs {
      file_system {
        retention_in_days = 7
        retention_in_mb  = 100
      }
    }
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# ------------------------------
# Log Analytics Workspace
# ------------------------------
resource "azurerm_log_analytics_workspace" "workspace" {
  name                = "${var.project_name}-logs"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  sku                 = "PerGB2018"
  retention_in_days   = 30

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# ------------------------------
# Application Insights
# ------------------------------
resource "azurerm_application_insights" "insights" {
  name                = "${var.project_name}-insights"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  application_type    = "web"
  workspace_id        = azurerm_log_analytics_workspace.workspace.id

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

