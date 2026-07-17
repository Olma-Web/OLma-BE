variable "region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "domain" {
  description = "Backend API domain for Caddy reverse proxy (HTTPS)"
  type        = string
  default     = "api.olma.kro.kr"
}

variable "docs_domain" {
  description = "Documentation site domain served by Caddy"
  type        = string
  default     = "docs.olma.kro.kr"
}

variable "grafana_domain" {
  description = "Grafana domain served by Caddy"
  type        = string
  default     = "grafana.olma.kro.kr"
}

# ---- SSH ----

variable "ssh_public_key" {
  description = "SSH public key (cat ~/.ssh/id_ed25519.pub)"
  type        = string
}

# ---- GitHub Container Registry ----

variable "ghcr_username" {
  description = "GitHub username for GHCR login"
  type        = string
}

variable "ghcr_token" {
  description = "GitHub PAT with read:packages scope"
  type        = string
  sensitive   = true
}

variable "ghcr_image" {
  description = "GHCR image name (e.g. olma-web/olma-backend)"
  type        = string
  default     = "olma-web/olma-backend"
}

# ---- Database ----

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "olma"
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}

# ---- JWT ----

variable "jwt_secret" {
  description = "BASE64-encoded HMAC-SHA256 secret for JWT signing (>= 32 bytes after decode)"
  type        = string
  sensitive   = true
}

# ---- Monitoring ----

variable "discord_webhook_url" {
  description = "Discord webhook URL for Grafana alert notifications"
  type        = string
  sensitive   = true
  default     = "https://discord.com/api/webhooks/WEBHOOK_ID/WEBHOOK_TOKEN"
}
