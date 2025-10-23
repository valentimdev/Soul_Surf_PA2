# ------------------------------
# Configurações específicas para deploy do backend
# ------------------------------

# Source Control para Backend
resource "azurerm_app_service_source_control" "backend" {
  app_id   = azurerm_linux_web_app.backend.id
  repo_url = var.backend_repo_url
  branch   = var.backend_branch
  manual_integration = true
}

# Configurações adicionais do backend
resource "azurerm_linux_web_app_slot" "backend_staging" {
  count          = var.environment == "production" ? 1 : 0
  name           = "staging"
  app_service_id = azurerm_linux_web_app.backend.id

  site_config {
    always_on = false
    
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
    "SPRING_JPA_SHOW_SQL"             = "true"
    "JWT_SECRET"                      = var.jwt_secret
    "JWT_EXPIRATION"                  = "86400000"
    "MAIL_HOST"                       = var.mail_host
    "MAIL_PORT"                       = var.mail_port
    "MAIL_USERNAME"                   = var.mail_username
    "MAIL_PASSWORD"                   = var.mail_password
    "FRONTEND_URL"                    = "https://${azurerm_linux_web_app.frontend.default_hostname}"
  }

  tags = {
    Environment = "staging"
    Project     = var.project_name
  }
}

# Health Check para o backend
resource "azurerm_monitor_diagnostic_setting" "backend" {
  name                       = "backend-diagnostics"
  target_resource_id         = azurerm_linux_web_app.backend.id
  log_analytics_workspace_id = azurerm_log_analytics_workspace.workspace.id

  enabled_log {
    category = "AppServiceHTTPLogs"
  }

  enabled_log {
    category = "AppServiceConsoleLogs"
  }

  enabled_log {
    category = "AppServiceAppLogs"
  }

  enabled_log {
    category = "AppServiceAuditLogs"
  }

  enabled_log {
    category = "AppServiceIPSecAuditLogs"
  }

  enabled_log {
    category = "AppServicePlatformLogs"
  }

  metric {
    category = "AllMetrics"
    enabled  = true
  }
}
