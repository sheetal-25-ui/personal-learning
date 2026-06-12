# Week 7: Infrastructure as Code (IaC)

> **Goal**: Learn how to create, manage, and version your entire infrastructure using code instead of clicking around in cloud consoles.

---

## What is Infrastructure as Code (IaC)?

### The Problem — Before IaC

Imagine you join a company. They have 15 servers running in AWS. You ask your senior:

- "How was the production server set up?"
- "Umm... Rajesh did it 6 months ago. He left the company."
- "Is there any documentation?"
- "There's a Google Doc from 2022... it might be outdated."
- "Can we recreate the staging server?"
- "Last time it took Amit 3 days of clicking around in AWS Console."

This is the **manual infrastructure** problem. Everybody clicks around in the AWS Console (or Azure Portal, or GCP Console) to create servers, databases, load balancers, and networks. Nobody writes down exactly what they did. When something breaks, or when you need to recreate it, you are lost.

**Common disasters without IaC:**

1. **"Snowflake servers"** — Every server is slightly different because different people set them up at different times with different settings. Production has Java 17, staging has Java 11, dev has Java 21. Nobody knows why.
2. **"Works on staging, breaks on production"** — Because staging and production are NOT identical (different security groups, different environment variables, different instance sizes).
3. **"Disaster recovery takes 2 days"** — Your production server goes down. Recreating it means someone sitting for 2 days clicking through 47 different AWS Console screens.
4. **"Who changed the firewall rule?"** — Someone opened port 22 to the entire internet. Nobody knows who or when. There is no audit trail.

### The Solution — Infrastructure as Code

With IaC, **everything about your infrastructure is written in code files**:

- Want a server? Write it in a `.tf` file.
- Want a database? Write it in a `.tf` file.
- Want a load balancer? Write it in a `.tf` file.
- Want a complete copy of production for staging? Run one command.

**The entire infrastructure lives in Git, just like your Java code.**

### Real-Life Analogy

**Without IaC = Building a house by memory**

Imagine a contractor builds your house. No blueprint, no plan. He just remembers: "3 bedrooms, 2 bathrooms, kitchen on the left." He builds a beautiful house. Now you want the EXACT same house in another city. He says, "Hmm, I think the kitchen was 12 feet wide... or was it 14? And the plumbing — I'm not sure which direction I ran the pipes."

Result: The second house is SIMILAR but NOT the same. Different room sizes, plumbing in different places, electrical wiring slightly off.

**With IaC = Building from a detailed blueprint**

An architect draws a precise blueprint. Every wall, every pipe, every wire is documented. Want to build the same house in 10 cities? Hand the blueprint to 10 contractors. Every house will be IDENTICAL.

That blueprint is your Infrastructure as Code.

**Another Indian analogy:**

Think of your mom's recipe for biryani. If she cooks from memory, every time the biryani is slightly different — sometimes more salt, sometimes less saffron. But if she writes down the EXACT recipe with precise measurements (2 cups rice, 1 tsp saffron, 500g chicken, cook for exactly 25 minutes), ANYONE can make the same biryani. That recipe file = Infrastructure as Code.

---

## Why IaC Matters — The Six Superpowers

### 1. Reproducibility

Same infrastructure in dev, staging, and production. No more "works on staging, fails on production." Every environment is created from the same code.

**Example:** Your `main.tf` creates a VPC + EC2 + RDS. You run it three times with different variable files (`dev.tfvars`, `staging.tfvars`, `prod.tfvars`). Three identical environments with different sizes.

### 2. Version Control

Your infrastructure changes are tracked in Git, just like application code.

```
commit abc123 — "Add Redis cache cluster for session management"
commit def456 — "Increase EC2 instance size from t3.medium to t3.large"
commit ghi789 — "Open port 443 for HTTPS traffic"
```

Want to know who opened that port? `git blame`. Want to revert a bad change? `git revert`. This is revolutionary compared to "someone clicked something in the console last Tuesday."

### 3. Speed

Creating an entire environment (VPC + subnets + security groups + EC2 instances + RDS database + S3 bucket + load balancer) manually takes 2-4 hours of clicking.

With IaC: `terraform apply` — 10 to 15 minutes. Go get chai.

### 4. Documentation

The code IS the documentation. Want to know what your infrastructure looks like? Read the `.tf` files. No more outdated Google Docs or Confluence pages that nobody updates.

### 5. Disaster Recovery

Your entire AWS region goes down (it has happened — the Mumbai region had outages). With IaC, you point your code at a different region and run `terraform apply`. Your entire infrastructure is recreated in the new region in minutes.

Without IaC? Someone has to manually recreate everything from memory. Good luck.

### 6. No Snowflake Servers

Every server is created from the same code, so every server is identical. If a server has problems, you don't debug it — you destroy it and create a new one from the same code. This is called **"cattle, not pets"** — you treat servers like cattle (replaceable) not like pets (unique and precious).

---

## IaC Tools Comparison

| Tool | Type | Language | Provider Support | Best For |
|------|------|----------|-----------------|----------|
| **Terraform** | Provisioning | HCL | AWS, Azure, GCP, and 1000+ more | Creating cloud resources |
| **Ansible** | Configuration | YAML | Any server with SSH | Configuring servers, deploying apps |
| **CloudFormation** | Provisioning | YAML/JSON | AWS only | AWS-only shops |
| **Pulumi** | Provisioning | Python/JS/Go/Java | AWS, Azure, GCP | Devs who prefer real languages |
| **Chef** | Configuration | Ruby | Any server | Complex configuration (legacy) |
| **Puppet** | Configuration | DSL | Any server | Large enterprise (legacy) |

### Provisioning vs Configuration — They Are Different Jobs

Think of building a restaurant:

- **Terraform (Provisioning)** = "Build the restaurant." Create the building, install electricity, plumbing, gas lines, buy the refrigerators and ovens. This is CREATING the infrastructure.
- **Ansible (Configuration)** = "Set up the kitchen." Install the software on the stove (gas connections), organize the pantry, train the staff, set the menu. This is CONFIGURING the infrastructure.

You often use BOTH together:
1. Terraform creates the EC2 instance
2. Ansible SSHs into it and installs Java, deploys your JAR, configures Nginx

---

## Terraform — Deep Dive

### What is Terraform?

Terraform is a tool by HashiCorp that lets you write what infrastructure you WANT, and it figures out HOW to create it. You describe the end state, and Terraform makes it happen.

**Declarative vs Imperative — A Critical Distinction**

**Imperative (Shell scripts, AWS CLI):**
```bash
# Step 1: Create VPC
aws ec2 create-vpc --cidr-block 10.0.0.0/16
# Step 2: Create subnet
aws ec2 create-subnet --vpc-id vpc-123 --cidr-block 10.0.1.0/24
# Step 3: Create EC2
aws ec2 run-instances --image-id ami-123 --instance-type t3.micro --subnet-id subnet-456
```

Problems with imperative:
- What if Step 1 already ran but Step 2 failed? Running the script again creates a DUPLICATE VPC.
- You have to handle the ORDER yourself (subnet needs VPC ID, EC2 needs subnet ID).
- You have to handle errors, retries, and partial failures.

**Declarative (Terraform):**
```hcl
resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
}

resource "aws_subnet" "public" {
  vpc_id     = aws_vpc.main.id
  cidr_block = "10.0.1.0/24"
}

resource "aws_instance" "web" {
  ami           = "ami-123"
  instance_type = "t3.micro"
  subnet_id     = aws_subnet.public.id
}
```

You just say WHAT you want. Terraform:
- Figures out the order (VPC first, then subnet, then EC2)
- Handles dependencies (subnet references VPC ID automatically)
- Is idempotent — run it 10 times, you still get ONE VPC, ONE subnet, ONE EC2
- Knows what already exists (via state file) and only creates what's missing

**Real-life analogy:** Imperative = telling a rickshaw driver "Turn left, then right, then go straight for 500 meters, then right again." Declarative = telling an Uber driver "Take me to Koramangala 4th Block." The driver figures out the route.

### Installing Terraform

**On Mac:**
```bash
brew install terraform
```

**On Linux (Ubuntu):**
```bash
# Add HashiCorp GPG key and repository
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform
```

**Verify installation:**
```bash
terraform version
# Output: Terraform v1.x.x
```

### The Terraform Workflow — Four Commands You Will Use Every Day

```
terraform init  -->  terraform plan  -->  terraform apply  -->  terraform destroy
   (Setup)           (Preview)            (Execute)             (Cleanup)
```

**1. `terraform init` — Set up the project**

Downloads the provider plugins (AWS, Azure, etc.) and initializes the working directory. You run this ONCE when starting a new project, or when you add a new provider.

```bash
terraform init
# Downloading hashicorp/aws v5.x.x ...
# Terraform has been successfully initialized!
```

Real-life: Like installing the kitchen equipment before you start cooking. You only do it once.

**2. `terraform plan` — Preview what will happen**

Shows you EXACTLY what Terraform will create, modify, or delete — WITHOUT actually doing anything. This is your safety net.

```bash
terraform plan
# + aws_instance.web will be created
#   + ami           = "ami-0abcdef1234567890"
#   + instance_type = "t3.micro"
# Plan: 1 to add, 0 to change, 0 to destroy.
```

Real-life: Like the bill preview at a restaurant before you pay. "You ordered 2 dosas and 1 coffee. Total: Rs 250. Confirm?" You see what you are about to do before committing.

**3. `terraform apply` — Create the resources**

Actually creates (or modifies or deletes) the resources. It shows the plan again and asks for confirmation.

```bash
terraform apply
# Do you want to perform these actions? (yes/no): yes
# aws_instance.web: Creating...
# aws_instance.web: Creation complete after 45s [id=i-0abc123def456]
# Apply complete! Resources: 1 added, 0 changed, 0 destroyed.
```

Real-life: Actually paying the bill. The money leaves your account.

**4. `terraform destroy` — Delete everything**

Removes ALL resources that Terraform manages. Used for cleanup (especially in dev/staging to save costs).

```bash
terraform destroy
# - aws_instance.web will be destroyed
# Do you want to destroy? (yes/no): yes
# Destroy complete! Resources: 1 destroyed.
```

Real-life: Cancelling your Swiggy order before it's delivered.

### HCL Syntax — The Language of Terraform

HCL (HashiCorp Configuration Language) is designed to be readable by humans. If you know JSON and YAML, HCL will feel natural.

#### Providers — Which Cloud to Talk To

```hcl
# Tell Terraform: "I want to use AWS, in the Mumbai region"
provider "aws" {
  region = "ap-south-1"   # Mumbai region
}
```

A provider is like a translator. Terraform speaks HCL. AWS speaks its own API. The AWS provider translates between them.

#### Resources — The Things You Create

```hcl
# Create an EC2 instance
resource "aws_instance" "my_server" {
  ami           = "ami-0614680123427b75e"  # Amazon Linux 2023 in Mumbai
  instance_type = "t3.micro"               # 2 vCPU, 1 GB RAM (free tier eligible)

  tags = {
    Name        = "my-spring-boot-server"
    Environment = "dev"
  }
}
```

Breaking this down:
- `resource` — keyword that says "create something"
- `"aws_instance"` — the TYPE of resource (EC2 instance)
- `"my_server"` — YOUR name for it (used to reference it in other resources)
- Inside `{}` — the configuration for this resource

#### Variables — Make Your Config Reusable

```hcl
# variables.tf — Define the variables
variable "instance_type" {
  description = "EC2 instance size"
  type        = string
  default     = "t3.micro"
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
}

# main.tf — Use the variables
resource "aws_instance" "my_server" {
  ami           = "ami-0614680123427b75e"
  instance_type = var.instance_type

  tags = {
    Name        = "server-${var.environment}"
    Environment = var.environment
  }
}
```

Now you can create different environments:
```bash
# Dev — small instance
terraform apply -var="environment=dev" -var="instance_type=t3.micro"

# Production — large instance
terraform apply -var="environment=prod" -var="instance_type=t3.xlarge"
```

Or use `.tfvars` files:
```hcl
# dev.tfvars
environment   = "dev"
instance_type = "t3.micro"

# prod.tfvars
environment   = "prod"
instance_type = "t3.xlarge"
```

```bash
terraform apply -var-file="dev.tfvars"
terraform apply -var-file="prod.tfvars"
```

#### Outputs — Display Useful Information

```hcl
# outputs.tf
output "server_public_ip" {
  description = "The public IP of the EC2 instance"
  value       = aws_instance.my_server.public_ip
}

output "server_id" {
  description = "The instance ID"
  value       = aws_instance.my_server.id
}
```

After `terraform apply`:
```
Outputs:
  server_public_ip = "13.232.45.67"
  server_id        = "i-0abc123def456"
```

Now you know where your server is without going to the AWS Console.

#### Data Sources — Read Existing Resources

```hcl
# Find the latest Amazon Linux 2023 AMI automatically
# (instead of hardcoding the AMI ID)
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

resource "aws_instance" "my_server" {
  ami           = data.aws_ami.amazon_linux.id  # Always gets the latest AMI
  instance_type = "t3.micro"
}
```

Data sources READ information — they don't create anything. Like looking up a phone number in a directory.

#### Locals — Computed Values

```hcl
locals {
  common_tags = {
    Project     = "my-spring-boot-app"
    Environment = var.environment
    ManagedBy   = "terraform"
  }

  name_prefix = "${var.project}-${var.environment}"
}

resource "aws_instance" "my_server" {
  ami           = data.aws_ami.amazon_linux.id
  instance_type = var.instance_type
  tags          = merge(local.common_tags, { Name = "${local.name_prefix}-web" })
}

resource "aws_s3_bucket" "logs" {
  bucket = "${local.name_prefix}-logs"
  tags   = local.common_tags
}
```

Locals let you compute values ONCE and reuse them. Like defining a constant at the top of your Java class.

### Building AWS Infrastructure with Terraform — Complete Example

Let us build a real infrastructure for deploying a Spring Boot application. This includes: VPC, subnets, Internet Gateway, security groups, an EC2 instance, an RDS database, and an S3 bucket.

#### File Structure

```
my-infrastructure/
  providers.tf        # AWS provider configuration
  variables.tf        # Input variables
  vpc.tf              # VPC and networking
  security-groups.tf  # Firewall rules
  ec2.tf              # Application server
  rds.tf              # Database
  s3.tf               # File storage
  outputs.tf          # Output values
  dev.tfvars          # Dev environment values
  prod.tfvars         # Production environment values
```

#### providers.tf

```hcl
terraform {
  # Specify the required Terraform version
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"   # Use AWS provider version 5.x
    }
  }
}

provider "aws" {
  region = var.aws_region
}
```

#### variables.tf

```hcl
variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "ap-south-1"  # Mumbai
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
  # 10.0.0.0/16 gives us 65,536 IP addresses — enough for most applications
}

variable "db_username" {
  description = "Database master username"
  type        = string
  sensitive   = true  # Won't be shown in logs or plan output
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.micro"
}
```

#### vpc.tf

```hcl
# --- VPC ---
# A VPC is your private network in AWS. Think of it as your own
# gated colony — traffic from outside can only enter through gates you define.
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true   # Allows instances to resolve domain names
  enable_dns_hostnames = true   # Gives instances public DNS names

  tags = {
    Name = "${var.environment}-vpc"
  }
}

# --- Public Subnet (Availability Zone A) ---
# Public subnet = has a route to the internet.
# Your EC2 instance (web server) goes here because users need to reach it.
resource "aws_subnet" "public_a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"       # 256 IP addresses
  availability_zone       = "${var.aws_region}a" # ap-south-1a
  map_public_ip_on_launch = true                 # Instances get public IPs automatically

  tags = {
    Name = "${var.environment}-public-a"
  }
}

# --- Public Subnet (Availability Zone B) ---
# RDS requires subnets in at least 2 AZs for high availability
resource "aws_subnet" "public_b" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = "${var.aws_region}b"
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.environment}-public-b"
  }
}

# --- Private Subnet (for database) ---
# Private subnet = NO route to the internet.
# Your database goes here because it should NOT be reachable from the internet.
resource "aws_subnet" "private_a" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.10.0/24"
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "${var.environment}-private-a"
  }
}

resource "aws_subnet" "private_b" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.11.0/24"
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "${var.environment}-private-b"
  }
}

# --- Internet Gateway ---
# The gate of your gated colony. Without this, nothing in your VPC
# can reach the internet, and nobody on the internet can reach your VPC.
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${var.environment}-igw"
  }
}

# --- Route Table for Public Subnets ---
# Traffic going to 0.0.0.0/0 (anywhere on the internet) should go
# through the Internet Gateway. Without this route, even public
# subnets can't reach the internet.
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"              # All internet traffic
    gateway_id = aws_internet_gateway.main.id  # Goes through IGW
  }

  tags = {
    Name = "${var.environment}-public-rt"
  }
}

# Associate the route table with the public subnets
resource "aws_route_table_association" "public_a" {
  subnet_id      = aws_subnet.public_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_b" {
  subnet_id      = aws_subnet.public_b.id
  route_table_id = aws_route_table.public.id
}
```

#### security-groups.tf

```hcl
# --- Security Group for EC2 (Web Server) ---
# A security group is a firewall. It controls which traffic is allowed
# in (ingress) and out (egress) of your instance.
resource "aws_security_group" "web" {
  name        = "${var.environment}-web-sg"
  description = "Allow HTTP, HTTPS, and SSH traffic"
  vpc_id      = aws_vpc.main.id

  # Allow SSH from anywhere (for management)
  # In production, restrict this to your office IP!
  ingress {
    description = "SSH access"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # TODO: Restrict to your IP in production
  }

  # Allow HTTP traffic (your Spring Boot app on port 8080)
  ingress {
    description = "Application port"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow HTTPS traffic
  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow ALL outbound traffic (so the server can download packages, etc.)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"          # -1 means all protocols
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-web-sg"
  }
}

# --- Security Group for RDS (Database) ---
# Only allow traffic from the web server, not from the internet
resource "aws_security_group" "db" {
  name        = "${var.environment}-db-sg"
  description = "Allow PostgreSQL from web server only"
  vpc_id      = aws_vpc.main.id

  # Allow PostgreSQL traffic ONLY from the web security group
  ingress {
    description     = "PostgreSQL from web server"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.web.id]  # Only web servers can connect
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-db-sg"
  }
}
```

#### ec2.tf

```hcl
# Find the latest Amazon Linux 2023 AMI
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

# Create an SSH key pair so you can SSH into the instance
resource "aws_key_pair" "deployer" {
  key_name   = "${var.environment}-deployer-key"
  public_key = file("~/.ssh/id_rsa.pub")  # Uses your existing SSH public key
}

# --- EC2 Instance ---
resource "aws_instance" "web" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type
  key_name               = aws_key_pair.deployer.key_name
  subnet_id              = aws_subnet.public_a.id
  vpc_security_group_ids = [aws_security_group.web.id]

  # User data script runs when the instance first boots
  # This installs Java and prepares the server for your Spring Boot app
  user_data = <<-EOF
    #!/bin/bash
    # Update the system
    sudo yum update -y

    # Install Java 17 (needed for Spring Boot)
    sudo yum install -y java-17-amazon-corretto

    # Create a user for the application (don't run as root!)
    sudo useradd -r -s /bin/false springapp

    # Create directories for the application
    sudo mkdir -p /opt/springapp
    sudo chown springapp:springapp /opt/springapp

    echo "Server setup complete. Java version:"
    java -version
  EOF

  root_block_device {
    volume_size = 20     # 20 GB disk
    volume_type = "gp3"  # General purpose SSD
    encrypted   = true   # Encrypt the disk at rest
  }

  tags = {
    Name = "${var.environment}-web-server"
  }
}
```

#### rds.tf

```hcl
# RDS needs a subnet group — tells AWS which subnets the database can use
resource "aws_db_subnet_group" "main" {
  name       = "${var.environment}-db-subnet-group"
  subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_b.id]

  tags = {
    Name = "${var.environment}-db-subnet-group"
  }
}

# --- RDS PostgreSQL Instance ---
resource "aws_db_instance" "main" {
  identifier     = "${var.environment}-postgres"
  engine         = "postgres"
  engine_version = "15.4"
  instance_class = "db.t3.micro"  # Smallest size, suitable for dev/staging

  allocated_storage     = 20       # 20 GB storage
  max_allocated_storage = 100      # Auto-scale up to 100 GB if needed
  storage_type          = "gp3"
  storage_encrypted     = true     # Encrypt data at rest

  db_name  = "myapp"
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.db.id]

  # Backup configuration
  backup_retention_period = 7        # Keep backups for 7 days
  backup_window           = "03:00-04:00"  # Backup at 3 AM IST (adjust for UTC)

  # Don't create a final snapshot when destroying (for dev/staging)
  skip_final_snapshot = var.environment != "prod"
  # For production, Terraform will create a final snapshot before deletion
  final_snapshot_identifier = var.environment == "prod" ? "${var.environment}-final-snapshot" : null

  tags = {
    Name = "${var.environment}-postgres"
  }
}
```

#### s3.tf

```hcl
# --- S3 Bucket for Application Files ---
resource "aws_s3_bucket" "app_files" {
  bucket = "${var.environment}-myapp-files-${random_id.bucket_suffix.hex}"
  # Bucket names must be globally unique, so we add a random suffix

  tags = {
    Name = "${var.environment}-app-files"
  }
}

# Generate a random suffix for the bucket name
resource "random_id" "bucket_suffix" {
  byte_length = 4
}

# Block all public access — files are private by default
resource "aws_s3_bucket_public_access_block" "app_files" {
  bucket = aws_s3_bucket.app_files.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Enable versioning — accidentally deleted a file? Recover it.
resource "aws_s3_bucket_versioning" "app_files" {
  bucket = aws_s3_bucket.app_files.id

  versioning_configuration {
    status = "Enabled"
  }
}

# Enable server-side encryption
resource "aws_s3_bucket_server_side_encryption_configuration" "app_files" {
  bucket = aws_s3_bucket.app_files.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}
```

#### outputs.tf

```hcl
output "web_server_public_ip" {
  description = "Public IP of the web server"
  value       = aws_instance.web.public_ip
}

output "web_server_public_dns" {
  description = "Public DNS of the web server"
  value       = aws_instance.web.public_dns
}

output "database_endpoint" {
  description = "RDS endpoint for database connection"
  value       = aws_db_instance.main.endpoint
}

output "s3_bucket_name" {
  description = "Name of the S3 bucket"
  value       = aws_s3_bucket.app_files.id
}

output "ssh_command" {
  description = "SSH command to connect to the web server"
  value       = "ssh -i ~/.ssh/id_rsa ec2-user@${aws_instance.web.public_ip}"
}
```

#### dev.tfvars

```hcl
environment   = "dev"
instance_type = "t3.micro"
db_username   = "devadmin"
db_password   = "dev-super-secret-password"
```

### Running It

```bash
# Step 1: Initialize (download AWS provider)
terraform init

# Step 2: Preview what will be created
terraform plan -var-file="dev.tfvars"
# Output: Plan: 14 to add, 0 to change, 0 to destroy.

# Step 3: Create everything
terraform apply -var-file="dev.tfvars"
# Type "yes" when prompted
# ... waits 5-10 minutes ...
# Apply complete! Resources: 14 added.
# Outputs:
#   web_server_public_ip = "13.232.45.67"
#   database_endpoint    = "dev-postgres.abc123.ap-south-1.rds.amazonaws.com:5432"
#   ssh_command          = "ssh -i ~/.ssh/id_rsa ec2-user@13.232.45.67"

# Step 4: When done, clean up (especially for dev!)
terraform destroy -var-file="dev.tfvars"
```

### Terraform State — Terraform's Memory

When Terraform creates resources, it records what it created in a **state file** (`terraform.tfstate`). This file is critical — it tells Terraform what already exists so it knows what to create, update, or delete.

**What is the state file?**

It is a JSON file that maps your `.tf` resources to real AWS resources:

```json
{
  "resources": [
    {
      "type": "aws_instance",
      "name": "web",
      "instances": [
        {
          "attributes": {
            "id": "i-0abc123def456",
            "public_ip": "13.232.45.67"
          }
        }
      ]
    }
  ]
}
```

Without state, Terraform would not know that `aws_instance.web` already exists as `i-0abc123def456`. It would try to create a NEW instance every time you run `terraform apply`.

**NEVER put terraform.tfstate in Git!**

The state file contains:
- Resource IDs and configurations
- Sensitive data (database passwords, secret keys)
- Internal Terraform metadata

It should be treated like a credentials file.

**Remote State — For Teams**

When working in a team, everyone needs access to the same state file. Store it in S3 with locking via DynamoDB:

```hcl
# backend.tf
terraform {
  backend "s3" {
    bucket         = "my-company-terraform-state"
    key            = "dev/terraform.tfstate"
    region         = "ap-south-1"
    dynamodb_table = "terraform-state-lock"  # Prevents two people from applying at once
    encrypt        = true
  }
}
```

Real-life analogy: State locking is like the "token system" at Indian railway ticket counters. Only one person (the one holding the token) can book a ticket at a time. Others wait. This prevents two people from making conflicting changes simultaneously.

### Terraform Modules — Reusable Components

Modules are like functions in Java. Write once, use many times.

**Without modules:** You copy-paste the same VPC code for dev, staging, and prod. When you need to change the VPC, you change it in 3 places. You forget one. Bugs.

**With modules:** You write the VPC code ONCE as a module. Each environment calls the module with different parameters.

```
modules/
  vpc/
    main.tf
    variables.tf
    outputs.tf
  ec2/
    main.tf
    variables.tf
    outputs.tf

environments/
  dev/
    main.tf      # Uses modules/vpc and modules/ec2
  prod/
    main.tf      # Uses the same modules with different variables
```

```hcl
# environments/dev/main.tf
module "vpc" {
  source      = "../../modules/vpc"
  environment = "dev"
  vpc_cidr    = "10.0.0.0/16"
}

module "web_server" {
  source        = "../../modules/ec2"
  environment   = "dev"
  instance_type = "t3.micro"
  subnet_id     = module.vpc.public_subnet_id
  sg_id         = module.vpc.web_security_group_id
}
```

The Terraform Registry (registry.terraform.io) has thousands of community modules. Instead of writing VPC code from scratch, you can use the official AWS VPC module.

### Terraform Best Practices

1. **Always use remote state** — S3 + DynamoDB for locking
2. **Always run `terraform plan` before `terraform apply`** — review changes before executing
3. **Never modify Terraform-managed resources manually** — if you change something in the AWS Console, Terraform will undo it on the next apply (this is called "drift")
4. **Use variables for anything that differs between environments** — instance types, CIDR blocks, names
5. **Use modules for reusability** — don't copy-paste resource blocks
6. **Tag everything** — every resource should have Name, Environment, ManagedBy tags
7. **Use `.gitignore`** — exclude `terraform.tfstate`, `*.tfstate.backup`, `.terraform/`
8. **Lock provider versions** — so a new provider version doesn't break your code unexpectedly
9. **Use workspaces or separate directories for environments** — keep dev and prod state separate
10. **Store secrets in environment variables or a vault** — never hardcode passwords in `.tf` files

---

## Ansible — Deep Dive

### What is Ansible?

Ansible is a tool that lets you **configure servers automatically** by SSHing into them and running tasks. Think of Terraform as the tool that BUILDS the house, and Ansible as the tool that FURNISHES it.

**Agentless** — Ansible doesn't need any special software installed on the target servers. It just needs SSH access. This is a huge advantage over tools like Chef and Puppet, which require an "agent" running on every server.

Real-life analogy: Ansible is like a TV remote control. You don't install anything on the TV — the remote just sends commands over infrared (Ansible sends commands over SSH). Chef/Puppet are like a smart TV with an app installed — more powerful but more setup.

### Key Concepts

**Inventory — Which servers to manage**

```ini
# inventory.ini
[web_servers]
13.232.45.67   ansible_user=ec2-user  ansible_ssh_private_key_file=~/.ssh/id_rsa
13.232.45.68   ansible_user=ec2-user  ansible_ssh_private_key_file=~/.ssh/id_rsa

[db_servers]
10.0.10.5      ansible_user=ec2-user  ansible_ssh_private_key_file=~/.ssh/id_rsa
```

The inventory is your address book. It lists all the servers you want to manage, grouped by role.

**Playbook — The task list**

A playbook is a YAML file that describes what you want to do on which servers. It is the core of Ansible.

**Tasks — Individual actions**

Each task does ONE thing: install a package, copy a file, start a service.

**Roles — Organized collections of tasks**

A role bundles related tasks together. Like a Java package grouping related classes.

**Handlers — Triggered actions**

A handler runs only when notified. Example: "Restart Nginx" is a handler — it only runs if a task changes the Nginx config file. If the config didn't change, Nginx is not restarted.

### Installing Ansible

```bash
# On Mac
brew install ansible

# On Ubuntu
sudo apt update
sudo apt install ansible

# Verify
ansible --version
```

### Ansible Playbook — Deploy a Spring Boot Application

This is a complete, real-world playbook that takes a fresh EC2 instance and turns it into a running Spring Boot application server.

```yaml
# deploy-springboot.yml
---
# The three dashes mark the beginning of a YAML document.

# A "play" targets a group of hosts and runs tasks on them.
- name: Deploy Spring Boot Application
  hosts: web_servers          # Run on all servers in the [web_servers] group
  become: yes                 # Run commands as root (sudo)
  vars:
    java_version: "17"
    app_name: "my-spring-boot-app"
    app_user: "springapp"
    app_port: 8080
    jar_source: "../target/my-app-1.0.0.jar"   # Built JAR file on your machine
    app_dir: "/opt/{{ app_name }}"

  tasks:
    # ---- Step 1: Update system packages ----
    - name: Update all system packages
      yum:
        name: "*"
        state: latest
      # Like running "sudo yum update -y"
      # Ensures the server has latest security patches

    # ---- Step 2: Install Java 17 ----
    - name: Install Java {{ java_version }}
      yum:
        name: "java-{{ java_version }}-amazon-corretto"
        state: present
      # "state: present" means "make sure it's installed"
      # If it's already installed, Ansible does NOTHING (idempotent!)

    - name: Verify Java installation
      command: java -version
      register: java_check     # Store the output in a variable
      changed_when: false      # This task never "changes" anything

    - name: Print Java version
      debug:
        msg: "{{ java_check.stderr_lines }}"
      # Java prints version to stderr (not stdout), so we use stderr_lines

    # ---- Step 3: Create application user ----
    - name: Create application user
      user:
        name: "{{ app_user }}"
        system: yes              # System user (no home directory, no login)
        shell: /bin/false        # Cannot log in interactively
      # Running the app as a dedicated user instead of root = security best practice

    # ---- Step 4: Create application directory ----
    - name: Create application directory
      file:
        path: "{{ app_dir }}"
        state: directory
        owner: "{{ app_user }}"
        group: "{{ app_user }}"
        mode: "0755"

    # ---- Step 5: Copy JAR file to server ----
    - name: Copy Spring Boot JAR to server
      copy:
        src: "{{ jar_source }}"
        dest: "{{ app_dir }}/{{ app_name }}.jar"
        owner: "{{ app_user }}"
        group: "{{ app_user }}"
        mode: "0644"
      notify: Restart Spring Boot App
      # "notify" triggers the handler below ONLY IF this task changes something.
      # If the JAR file is already the same, nothing happens.

    # ---- Step 6: Create systemd service file ----
    - name: Create systemd service for Spring Boot app
      template:
        src: templates/springboot.service.j2
        dest: "/etc/systemd/system/{{ app_name }}.service"
        mode: "0644"
      notify: Restart Spring Boot App
      # Uses a Jinja2 template (see below)

    # ---- Step 7: Reload systemd to pick up the new service ----
    - name: Reload systemd daemon
      systemd:
        daemon_reload: yes

    # ---- Step 8: Start and enable the application ----
    - name: Start and enable Spring Boot app
      systemd:
        name: "{{ app_name }}"
        state: started
        enabled: yes            # Start automatically on server boot

    # ---- Step 9: Wait for the application to be ready ----
    - name: Wait for Spring Boot app to start
      uri:
        url: "http://localhost:{{ app_port }}/actuator/health"
        status_code: 200
      register: health_check
      until: health_check.status == 200
      retries: 30               # Try 30 times
      delay: 5                  # Wait 5 seconds between retries (total: 150 seconds max)
      # This is like checking if your friend's phone is reachable after they
      # said "I'll call you in 5 minutes." You keep trying every few seconds.

    - name: Print health check result
      debug:
        msg: "Application is healthy! Status: {{ health_check.json.status }}"

  # ---- Handlers ----
  # Handlers run ONCE at the end, and only if they were notified.
  handlers:
    - name: Restart Spring Boot App
      systemd:
        name: "{{ app_name }}"
        state: restarted
```

**The systemd service template** (`templates/springboot.service.j2`):

```ini
# This file tells Linux how to run your Spring Boot app as a service.
# Like creating a Windows Service — it starts on boot, restarts on crash.

[Unit]
Description={{ app_name }} Spring Boot Application
After=network.target
# "After=network.target" means: don't start until the network is ready

[Service]
User={{ app_user }}
Group={{ app_user }}
ExecStart=/usr/bin/java -jar {{ app_dir }}/{{ app_name }}.jar --server.port={{ app_port }}
SuccessExitStatus=143
# 143 = SIGTERM (normal shutdown signal). Without this, systemd thinks
# a normal shutdown is a crash.

Restart=on-failure
RestartSec=10
# If the app crashes, wait 10 seconds and restart it automatically.

StandardOutput=journal
StandardError=journal
# Send logs to journald (view with: journalctl -u {{ app_name }})

Environment=SPRING_PROFILES_ACTIVE=prod
Environment=JAVA_OPTS=-Xmx512m -Xms256m
# Set JVM memory: min 256MB, max 512MB

[Install]
WantedBy=multi-user.target
# Start when the system reaches "multi-user" mode (normal startup)
```

**Run the playbook:**

```bash
ansible-playbook -i inventory.ini deploy-springboot.yml
```

Output looks like:
```
PLAY [Deploy Spring Boot Application] ****************************************

TASK [Update all system packages] *********************************************
changed: [13.232.45.67]

TASK [Install Java 17] ********************************************************
ok: [13.232.45.67]     # Already installed — Ansible skipped it!

TASK [Copy Spring Boot JAR to server] *****************************************
changed: [13.232.45.67]

TASK [Wait for Spring Boot app to start] **************************************
FAILED - RETRYING: Wait for Spring Boot app to start (30 retries left)
FAILED - RETRYING: Wait for Spring Boot app to start (29 retries left)
ok: [13.232.45.67]     # App started on the 3rd attempt

PLAY RECAP ********************************************************************
13.232.45.67               : ok=9    changed=4    unreachable=0    failed=0
```

### Ansible vs Shell Scripts — Why Ansible Wins

| Feature | Shell Script | Ansible |
|---------|-------------|---------|
| **Idempotent** | No — running twice may install Java twice or fail | Yes — running twice has no extra effect |
| **Readable** | Bash can be cryptic | YAML is human-friendly |
| **Error handling** | You write try/catch manually | Built-in — fails clearly with details |
| **Multiple servers** | Loop + SSH in script | Built-in parallelism |
| **Dry run** | No | `--check` flag (like terraform plan) |
| **Rolling updates** | Complex scripting | `serial: 2` (update 2 servers at a time) |

**Idempotent** is the key advantage. Run an Ansible playbook 10 times — the result is the same as running it once. A shell script that does `apt install java` might fail on the second run if the package manager is locked, or it might re-download and reinstall unnecessarily.

---

## CloudFormation — Brief Overview

CloudFormation is AWS's own IaC tool. If you are 100% on AWS and never plan to use another cloud, it is a valid choice.

```yaml
# cloudformation-example.yml
AWSTemplateFormatVersion: "2010-09-09"
Description: "Simple EC2 + Security Group"

Parameters:
  InstanceType:
    Type: String
    Default: t3.micro
    AllowedValues: [t3.micro, t3.small, t3.medium]

Resources:
  WebServerSecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      GroupDescription: "Allow HTTP and SSH"
      SecurityGroupIngress:
        - IpProtocol: tcp
          FromPort: 80
          ToPort: 80
          CidrIp: 0.0.0.0/0
        - IpProtocol: tcp
          FromPort: 22
          ToPort: 22
          CidrIp: 0.0.0.0/0

  WebServer:
    Type: AWS::EC2::Instance
    Properties:
      ImageId: ami-0614680123427b75e
      InstanceType: !Ref InstanceType
      SecurityGroupIds:
        - !Ref WebServerSecurityGroup
      Tags:
        - Key: Name
          Value: "my-web-server"

Outputs:
  PublicIP:
    Value: !GetAtt WebServer.PublicIp
```

**Terraform vs CloudFormation:**
- Terraform: Multi-cloud (AWS + Azure + GCP). Uses HCL. Huge community. More flexible.
- CloudFormation: AWS only. Uses YAML/JSON. Free (no extra tool). Deeper AWS integration.

Most companies choose Terraform because they want the option to go multi-cloud, or because HCL is more readable than CloudFormation's verbose YAML.

---

## Common Mistakes

### Terraform Mistakes

1. **Committing `terraform.tfstate` to Git** — This file contains secrets and should NEVER be in Git. Add it to `.gitignore`.
2. **Hardcoding values** — Don't hardcode AMI IDs, instance types, or CIDR blocks. Use variables.
3. **No remote state** — Working alone on your laptop is fine for learning, but for teams, use S3 + DynamoDB.
4. **Manual changes after Terraform** — If you change a security group in the AWS Console, Terraform will undo your change on the next `apply`. All changes must go through Terraform.
5. **Not running `terraform plan`** — Always preview before applying. `terraform apply` without review is like running `DROP TABLE` without checking which table.
6. **Forgetting to destroy dev resources** — EC2 instances cost money 24/7. Destroy dev environments when you are not using them.

### Ansible Mistakes

1. **Running as root** — Always use `become: yes` selectively, not globally. Don't SSH as root.
2. **Not testing with `--check`** — Ansible has a dry-run mode. Use it before running on production.
3. **Hardcoding server IPs** — Use dynamic inventory or variable files instead.
4. **Not using handlers** — Restarting Nginx after every task is wasteful. Use handlers so it restarts only when the config actually changes.
5. **Ignoring idempotency** — Using `command` or `shell` modules instead of proper modules (like `yum`, `copy`, `template`) breaks idempotency. Always prefer the purpose-built module.

---

## Practice Exercises

### Exercise 1: Terraform Basics — Create an EC2 Instance

Create a Terraform configuration that:
1. Uses the AWS provider (Mumbai region)
2. Creates a security group allowing SSH (port 22) and HTTP (port 8080)
3. Creates a t3.micro EC2 instance with Amazon Linux 2023
4. Outputs the public IP and SSH command
5. Use variables for instance_type and environment name

### Exercise 2: Terraform Advanced — Full VPC + EC2 + RDS

Extend Exercise 1 to include:
1. A VPC with CIDR 10.0.0.0/16
2. 2 public subnets and 2 private subnets
3. An Internet Gateway and route table
4. Security groups for web (ports 22, 8080, 443) and database (port 5432 from web only)
5. An RDS PostgreSQL instance in the private subnet
6. An S3 bucket for application files
7. Remote state stored in S3 with DynamoDB locking

### Exercise 3: Ansible Basics — Install Java and Deploy a JAR

Write an Ansible playbook that:
1. Connects to a target server via SSH
2. Updates system packages
3. Installs Java 17
4. Creates an application user (`springapp`)
5. Copies a JAR file to `/opt/myapp/`
6. Creates a systemd service file
7. Starts the application and verifies it responds on port 8080

### Exercise 4: Ansible — Nginx Reverse Proxy

Write an Ansible playbook that:
1. Installs Nginx
2. Creates a reverse proxy configuration (forwards port 80 to localhost:8080)
3. Enables HTTPS with a self-signed certificate (for learning)
4. Uses handlers to restart Nginx only when the config changes
5. Verifies Nginx is running and responding

### Exercise 5: Terraform + Ansible — The Full Pipeline

1. Use Terraform to create: VPC + EC2 + Security Groups
2. Use Terraform's `local-exec` provisioner to generate an Ansible inventory file with the new EC2 IP
3. Run the Ansible playbook from Exercise 3 to configure the server
4. Verify the Spring Boot app is accessible from your browser

This is how it works in real companies: Terraform builds the infrastructure, Ansible configures it.

---

## Key Takeaways

| Concept | One-Line Summary |
|---------|-----------------|
| IaC | Write your infrastructure in code files, version it in Git, recreate with one command |
| Terraform | Declarative tool to CREATE cloud resources (servers, databases, networks) |
| Ansible | Agentless tool to CONFIGURE servers (install software, deploy apps) |
| State file | Terraform's memory of what it created — store remotely, never in Git |
| Idempotent | Run it 10 times, same result as running once — critical property of good IaC |
| Modules | Reusable Terraform components — like functions in programming |
| Playbooks | Ansible's task lists in YAML — what to do on which servers |
| Handlers | Ansible's triggered actions — run only when something changes |

**Next week:** We will learn about Monitoring (how to know if your infrastructure and apps are healthy) and Security (how to protect everything you just built).
