terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.region
}

# ============================================================
# Network
# ============================================================

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# ============================================================
# AMI
# ============================================================

data "aws_ami" "ubuntu_arm" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-arm64-server-*"]
  }

  filter {
    name   = "state"
    values = ["available"]
  }
}

# ============================================================
# Security Groups
# ============================================================

resource "aws_security_group" "backend" {
  name        = "olma-backend-sg"
  description = "Security group for OLma backend EC2"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTP (Caddy)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS (Caddy)"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Backend API (direct)"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "olma-backend-sg" }
}

resource "aws_security_group" "db" {
  name        = "olma-db-sg"
  description = "Security group for OLma RDS"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description     = "PostgreSQL from backend"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.backend.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "olma-db-sg" }
}

# ============================================================
# RDS (PostgreSQL)
# ============================================================

resource "aws_db_instance" "olma" {
  identifier     = "olma"
  engine         = "postgres"
  engine_version = "17"
  instance_class = var.db_instance_class

  allocated_storage = 20
  storage_type      = "gp3"

  db_name  = "olma"
  username = var.db_username
  password = var.db_password

  vpc_security_group_ids = [aws_security_group.db.id]
  publicly_accessible    = false
  skip_final_snapshot    = true

  tags = { Name = "olma-db" }
}

# ============================================================
# EC2
# ============================================================

resource "aws_key_pair" "olma" {
  key_name   = "olma-backend-key"
  public_key = var.ssh_public_key
}

resource "aws_instance" "backend" {
  ami                    = data.aws_ami.ubuntu_arm.id
  instance_type          = "t4g.small"
  key_name               = aws_key_pair.olma.key_name
  vpc_security_group_ids = [aws_security_group.backend.id]
  subnet_id              = data.aws_subnets.default.ids[0]

  user_data_replace_on_change = true

  credit_specification {
    cpu_credits = "standard"
  }

  user_data = <<-SCRIPT
    #!/bin/bash
    set -e

    # Docker
    apt-get update
    apt-get install -y docker.io
    usermod -aG docker ubuntu
    systemctl enable docker
    systemctl start docker

    # GHCR login
    echo "${var.ghcr_token}" | docker login ghcr.io -u ${var.ghcr_username} --password-stdin

    # Copy docker config for ubuntu user
    mkdir -p /home/ubuntu/.docker
    cp /root/.docker/config.json /home/ubuntu/.docker/config.json
    chown -R ubuntu:ubuntu /home/ubuntu/.docker

    # Env file (avoids special character issues in shell)
    cat > /home/ubuntu/olma.env << 'ENVEOF'
    SPRING_PROFILES_ACTIVE=prod
    SPRING_DATASOURCE_URL=jdbc:postgresql://${aws_db_instance.olma.address}:5432/olma?sslmode=require
    SPRING_DATASOURCE_USERNAME=${var.db_username}
    SPRING_DATASOURCE_PASSWORD=${var.db_password}
    JWT_SECRET=${var.jwt_secret}
    ENVEOF
    sed -i 's/^    //' /home/ubuntu/olma.env
    chmod 600 /home/ubuntu/olma.env
    chown ubuntu:ubuntu /home/ubuntu/olma.env

    # Monitoring Env file
    cat > /home/ubuntu/monitoring.env << 'ENVEOF'
    DISCORD_WEBHOOK_URL=${var.discord_webhook_url}
    ENVEOF
    sed -i 's/^    //' /home/ubuntu/monitoring.env
    chmod 600 /home/ubuntu/monitoring.env
    chown ubuntu:ubuntu /home/ubuntu/monitoring.env

    # Caddy (reverse proxy + auto HTTPS)
    apt-get install -y debian-keyring debian-archive-keyring apt-transport-https curl
    curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
    curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | tee /etc/apt/sources.list.d/caddy-stable.list
    apt-get update
    apt-get install -y caddy

    cat > /etc/caddy/Caddyfile << 'CADDYEOF'
    docs.olma.kro.kr {
      root * /var/www/docs
      file_server
    }

    :80 {
      reverse_proxy localhost:8080
    }
    CADDYEOF
    sed -i 's/^    //' /etc/caddy/Caddyfile

    systemctl enable caddy
    systemctl restart caddy

    # Backend container
    docker run -d \
      --name olma-backend \
      --restart unless-stopped \
      -p 8080:8080 \
      --env-file /home/ubuntu/olma.env \
      ghcr.io/${var.ghcr_image}:latest

  SCRIPT

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  tags = { Name = "olma-backend" }

  depends_on = [aws_db_instance.olma]
}

resource "aws_eip" "backend" {
  instance = aws_instance.backend.id

  tags = { Name = "olma-backend-eip" }
}
