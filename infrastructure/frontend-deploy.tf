# ------------------------------
# Configurações específicas para deploy do frontend
# ------------------------------

# Source Control para Frontend
resource "azurerm_app_service_source_control" "frontend" {
  app_id   = azurerm_linux_web_app.frontend.id
  repo_url = var.frontend_repo_url
  branch   = var.frontend_branch
  manual_integration = true
}

# Configurações adicionais do frontend
resource "azurerm_linux_web_app_slot" "frontend_staging" {
  count          = var.environment == "production" ? 1 : 0
  name           = "staging"
  app_service_id = azurerm_linux_web_app.frontend.id

  site_config {
    always_on = false
    
    application_stack {
      node_version = var.frontend_stack
    }
  }

  app_settings = {
    "BACKEND_URL" = "https://${azurerm_linux_web_app.backend.default_hostname}"
    "NODE_ENV"     = "staging"
  }

  tags = {
    Environment = "staging"
    Project     = var.project_name
  }
}

# Health Check para o frontend
resource "azurerm_monitor_diagnostic_setting" "frontend" {
  name                       = "frontend-diagnostics"
  target_resource_id         = azurerm_linux_web_app.frontend.id
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

# CDN Profile para o frontend (opcional para produção)
resource "azurerm_cdn_profile" "frontend_cdn" {
  count               = var.enable_cdn ? 1 : 0
  name                = "${var.project_name}-cdn"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  sku                 = "Standard_Microsoft"

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "azurerm_cdn_endpoint" "frontend_cdn_endpoint" {
  count               = var.enable_cdn ? 1 : 0
  name                = "${var.project_name}-cdn-endpoint"
  profile_name        = azurerm_cdn_profile.frontend_cdn[0].name
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name

  origin {
    name      = "frontend-origin"
    host_name = azurerm_linux_web_app.frontend.default_hostname
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}
