output "backend_url" {
  value       = "https://${azurerm_linux_web_app.backend.default_hostname}"
  description = "URL do backend"
}

output "frontend_url" {
  value       = "https://${azurerm_linux_web_app.frontend.default_hostname}"
  description = "URL do frontend"
}

output "storage_account_name" {
  value       = azurerm_storage_account.storage.name
  description = "Nome da conta de armazenamento"
}

output "storage_account_primary_blob_endpoint" {
  value       = azurerm_storage_account.storage.primary_blob_endpoint
  description = "Endpoint primário do blob storage"
}

output "database_fqdn" {
  value       = azurerm_postgresql_flexible_server.database.fqdn
  description = "FQDN do servidor PostgreSQL"
  sensitive   = true
}

output "database_name" {
  value       = var.database_name
  description = "Nome do banco de dados"
}

output "application_insights_connection_string" {
  value       = azurerm_application_insights.insights.connection_string
  description = "String de conexão do Application Insights"
  sensitive   = true
}

output "resource_group_name" {
  value       = azurerm_resource_group.rg.name
  description = "Nome do resource group"
}

output "resource_group_location" {
  value       = azurerm_resource_group.rg.location
  description = "Localização do resource group"
}

output "storage_container_name" {
  value       = azurerm_storage_container.images.name
  description = "Nome do container de imagens"
}

output "backend_identity_principal_id" {
  value       = azurerm_user_assigned_identity.backend_identity.principal_id
  description = "Principal ID da Managed Identity do backend"
}

output "log_analytics_workspace_id" {
  value       = azurerm_log_analytics_workspace.workspace.id
  description = "ID do Log Analytics Workspace"
}

output "application_insights_app_id" {
  value       = azurerm_application_insights.insights.app_id
  description = "App ID do Application Insights"
}