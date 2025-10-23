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
