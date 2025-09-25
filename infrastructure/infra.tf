# Resource Group
resource "azurerm_resource_group" "rg" {
  name     = "${var.project_name}-rg"
  location = var.location
}

# Storage Account
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
}

# App Service Plan
resource "azurerm_service_plan" "asp" {
  name                = "${var.project_name}-plan"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  os_type             = "Linux"
  sku_name            = "B1"
}

# Backend Web App
resource "azurerm_linux_web_app" "backend" {
  name                = "${var.project_name}-backend"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  service_plan_id     = azurerm_service_plan.asp.id

  site_config {
    application_stack {
      java_version = "17"
    }
  }

  app_settings = {
    "WEBSITES_PORT"   = "8080"
    "BLOB_CONNECTION" = azurerm_storage_account.storage.primary_connection_string
  }
}

# Frontend Web App
resource "azurerm_linux_web_app" "frontend" {
  name                = "${var.project_name}-frontend"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  service_plan_id     = azurerm_service_plan.asp.id

  site_config {
    application_stack {
      node_version = "18-lts"
    }
  }

  app_settings = {
    "BACKEND_URL" = azurerm_linux_web_app.backend.default_hostname
  }
}

# Application Insights
resource "azurerm_application_insights" "insights" {
  name                = "${var.project_name}-insights"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  application_type    = "web"
}
