output "backend_url" {
  value = "https://${azurerm_linux_web_app.backend.default_hostname}"
}

output "storage_account" {
  value = azurerm_storage_account.storage.primary_blob_endpoint
}
