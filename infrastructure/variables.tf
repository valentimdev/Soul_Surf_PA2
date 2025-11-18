variable "project_name" {
  type        = string
  default     = "soulsurf"
  description = "Nome do projeto usado para nomear recursos"
}

variable "location" {
  type        = string
  default     = "East US"
  description = "Localização dos recursos Azure"
}

variable "environment" {
  type        = string
  default     = "development"
  description = "Ambiente de deploy (development, staging, production)"
  
  validation {
    condition     = contains(["development", "staging", "production"], var.environment)
    error_message = "Environment deve ser development, staging ou production."
  }
}

variable "backend_stack" {
  type        = string
  default     = "17"
  description = "Versão do Java para o backend"
}

variable "frontend_stack" {
  type        = string
  default     = "18-lts"
  description = "Versão do Node.js para o frontend"
}

variable "app_service_sku" {
  type        = string
  default     = "B1"
  description = "SKU do App Service Plan (B1, S1, P1, etc)"
}

variable "database_name" {
  type        = string
  default     = "soulsurf_db"
  description = "Nome do banco de dados PostgreSQL"
}

variable "db_admin_username" {
  type        = string
  default     = "soulsurf_admin"
  description = "Usuário administrador do banco de dados"
  sensitive   = true
}

variable "db_admin_password" {
  type        = string
  description = "Senha do administrador do banco de dados"
  sensitive   = true
}

variable "jwt_secret" {
  type        = string
  description = "Chave secreta para JWT"
  sensitive   = true
}

variable "mail_host" {
  type        = string
  default     = "smtp.gmail.com"
  description = "Host do servidor de email"
}

variable "mail_port" {
  type        = number
  default     = 587
  description = "Porta do servidor de email"
}

variable "mail_username" {
  type        = string
  description = "Usuário do servidor de email"
  sensitive   = true
}

variable "mail_password" {
  type        = string
  description = "Senha do servidor de email"
  sensitive   = true
}

variable "backend_repo_url" {
  type        = string
  description = "URL do repositório Git do backend"
  default     = ""
}

variable "backend_branch" {
  type        = string
  description = "Branch do repositório do backend para deploy"
  default     = "main"
}

variable "frontend_repo_url" {
  type        = string
  description = "URL do repositório Git do frontend"
  default     = ""
}

variable "frontend_branch" {
  type        = string
  description = "Branch do repositório do frontend para deploy"
  default     = "main"
}

variable "enable_cdn" {
  type        = bool
  default     = false
  description = "Habilitar CDN para o frontend"
}

variable "weather_api_url" {
  type        = string
  default     = "https://api.openweathermap.org/data/2.5/weather"
  description = "URL da API de clima"
}

variable "weather_api_key" {
  type        = string
  description = "Chave da API de clima (OpenWeatherMap)"
  sensitive   = true
}