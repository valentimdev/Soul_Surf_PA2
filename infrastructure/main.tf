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
# Backend Web App
# ------------------------------
resource "azurerm_linux_web_app" "backend" {
  name                = "${var.project_name}-backend"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  service_plan_id     = azurerm_service_plan.asp.id

  site_config {
    always_on = var.environment == "production" ? true : false
    
    application_stack {
      java_version = var.backend_stack
    }

    cors {
      allowed_origins = [
        "https://${azurerm_linux_web_app.frontend.default_hostname}",
        "http://localhost:3000",
        "http://localhost:5173"
      ]
    }
  }

  app_settings = {
    "WEBSITES_PORT"                    = "8080"
    "BLOB_CONNECTION"                  = azurerm_storage_account.storage.primary_connection_string
    "SPRING_DATASOURCE_URL"           = "jdbc:postgresql://${azurerm_postgresql_flexible_server.database.fqdn}:5432/${var.database_name}?sslmode=require"
    "SPRING_DATASOURCE_USERNAME"      = var.db_admin_username
    "SPRING_DATASOURCE_PASSWORD"      = var.db_admin_password
    "SPRING_JPA_HIBERNATE_DDL_AUTO"   = "update"
    "SPRING_JPA_SHOW_SQL"             = var.environment == "development" ? "true" : "false"
    "JWT_SECRET"                      = var.jwt_secret
    "JWT_EXPIRATION"                  = "86400000"
    "MAIL_HOST"                       = var.mail_host
    "MAIL_PORT"                       = var.mail_port
    "MAIL_USERNAME"                   = var.mail_username
    "MAIL_PASSWORD"                   = var.mail_password
    "FRONTEND_URL"                    = "https://${azurerm_linux_web_app.frontend.default_hostname}"
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
  }

  app_settings = {
    "BACKEND_URL" = "https://${azurerm_linux_web_app.backend.default_hostname}"
    "NODE_ENV"     = var.environment
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
