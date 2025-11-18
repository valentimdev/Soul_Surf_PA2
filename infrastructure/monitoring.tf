# ------------------------------
# Alertas e Monitoramento
# ------------------------------

# Alerta para alta utilização de CPU do Backend
resource "azurerm_monitor_metric_alert" "backend_high_cpu" {
  name                = "${var.project_name}-backend-high-cpu"
  resource_group_name = azurerm_resource_group.rg.name
  scopes              = [azurerm_linux_web_app.backend.id]
  description         = "Alerta quando a CPU do backend excede 80%"
  severity            = 2
  frequency           = "PT1M"
  window_size         = "PT5M"

  criteria {
    metric_namespace = "Microsoft.Web/sites"
    metric_name      = "CpuPercentage"
    aggregation      = "Average"
    operator         = "GreaterThan"
    threshold        = 80
  }

  action {
    action_group_id = azurerm_monitor_action_group.main.id
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Alerta para alta utilização de CPU do Frontend
resource "azurerm_monitor_metric_alert" "frontend_high_cpu" {
  name                = "${var.project_name}-frontend-high-cpu"
  resource_group_name = azurerm_resource_group.rg.name
  scopes              = [azurerm_linux_web_app.frontend.id]
  description         = "Alerta quando a CPU do frontend excede 80%"
  severity            = 2
  frequency           = "PT1M"
  window_size         = "PT5M"

  criteria {
    metric_namespace = "Microsoft.Web/sites"
    metric_name      = "CpuPercentage"
    aggregation      = "Average"
    operator         = "GreaterThan"
    threshold        = 80
  }

  action {
    action_group_id = azurerm_monitor_action_group.main.id
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Alerta para alta taxa de erro HTTP 5xx
resource "azurerm_monitor_metric_alert" "backend_http_5xx" {
  name                = "${var.project_name}-backend-http-5xx"
  resource_group_name = azurerm_resource_group.rg.name
  scopes              = [azurerm_linux_web_app.backend.id]
  description         = "Alerta quando há muitos erros HTTP 5xx no backend"
  severity            = 1
  frequency           = "PT1M"
  window_size         = "PT5M"

  criteria {
    metric_namespace = "Microsoft.Web/sites"
    metric_name      = "Http5xx"
    aggregation      = "Total"
    operator         = "GreaterThan"
    threshold        = 10
  }

  action {
    action_group_id = azurerm_monitor_action_group.main.id
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Alerta para disponibilidade do banco de dados
resource "azurerm_monitor_metric_alert" "database_high_cpu" {
  name                = "${var.project_name}-database-high-cpu"
  resource_group_name = azurerm_resource_group.rg.name
  scopes              = [azurerm_postgresql_flexible_server.database.id]
  description         = "Alerta quando a CPU do banco de dados excede 80%"
  severity            = 2
  frequency           = "PT1M"
  window_size         = "PT5M"

  criteria {
    metric_namespace = "Microsoft.DBforPostgreSQL/flexibleServers"
    metric_name      = "cpu_percent"
    aggregation      = "Average"
    operator         = "GreaterThan"
    threshold        = 80
  }

  action {
    action_group_id = azurerm_monitor_action_group.main.id
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Action Group para notificações (requer configuração de email)
resource "azurerm_monitor_action_group" "main" {
  name                = "${var.project_name}-alerts"
  resource_group_name = azurerm_resource_group.rg.name
  short_name          = "soulsurf"

  # Nota: Configure o email_receiver com um email válido
  # email_receiver {
  #   name          = "admin"
  #   email_address = "admin@example.com"
  # }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

