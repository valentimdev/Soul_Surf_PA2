variable "project_name" {
  type    = string
  default = "soulsurf"
}

variable "location" {
  type    = string
  default = "eastus"
}

variable "backend_stack" {
  type    = string
  default = "java17"
  description = "Versão da stack do backend (java17, java11, etc)"
}

variable "frontend_stack" {
  type    = string
  default = "node18"
  description = "Versão da stack do frontend (node16, node18, etc)"
}
