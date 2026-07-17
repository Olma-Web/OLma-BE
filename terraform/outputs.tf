output "public_ip" {
  description = "Backend server IP"
  value       = aws_eip.backend.public_ip
}

output "ssh_command" {
  description = "SSH command to connect"
  value       = "ssh ubuntu@${aws_eip.backend.public_ip}"
}

output "api_url" {
  description = "Backend API URL"
  value       = "https://${var.domain}"
}

output "docs_url" {
  description = "Documentation site URL"
  value       = "https://${var.docs_domain}"
}

output "grafana_url" {
  description = "Grafana URL"
  value       = "https://${var.grafana_domain}"
}

output "db_endpoint" {
  description = "RDS endpoint"
  value       = aws_db_instance.olma.address
}
