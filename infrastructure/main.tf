terraform {
  required_version = ">= 1.5.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.70"
    }
  }
}

provider "azurerm" {
  features {}
}

# ------------------------------
# Variáveis (pode mover para variables.tf)
# ------------------------------
variable "project_name" {
  type    = string
  default = "soulsurf"
}

variable "location" {
  type    = string
  default = "eastus"
}

# ------------------------------
# Resource Group
# ------------------------------
resource "azurerm_resource_group" "rg" {
  name     = "${var.project_name}-rg"
  location = var.location
}

# ------------------------------
# Storage Account (para fotos/arquivos)
# ------------------------------
resource "azurerm_storage_account" "storage" {
  name                     = "${var.project_name}storage${random_integer.rand.result}"
  resource_group_name      = azurerm_resource_group.rg.name
  location                 = azurerm_resource_group.rg.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
}

resource "random_integer" "rand" {
  min = 10000
  max = 99999
}

# ------------------------------
# App Service Plan
# ------------------------------
resource "azurerm_service_plan" "asp" {
  name                = "${var.project_name}-plan"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  os_type             = "Linux"
  sku_name            = "B1" # Básico, pode mudar para S1 (Prod)
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
    application_stack {
      java_version = "17" # Se backend for Spring Boot
    }
  }

  app_settings = {
    "WEBSITES_PORT" = "8080"
    "BLOB_CONNECTION" = azurerm_storage_account.storage.primary_connection_string
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
    application_stack {
      node_version = "18-lts" # Se for Next.js/React
    }
  }

  app_settings = {
    "BACKEND_URL" = azurerm_linux_web_app.backend.default_hostname
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
}
