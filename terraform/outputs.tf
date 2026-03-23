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
  value       = "http://${aws_eip.backend.public_ip}:8080"
}

output "db_endpoint" {
  description = "RDS endpoint"
  value       = aws_db_instance.olma.address
}
