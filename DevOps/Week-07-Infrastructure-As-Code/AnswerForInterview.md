# Week 7: Infrastructure as Code (IaC) — Interview Answers & Exercise Solutions

> **The Interview Golden Rule** 🏆
> Whenever you answer an IaC question, walk it through four gears — every single time:
> 1. **Definition** — what is it, in one crisp sentence?
> 2. **Problem it solves** — why does this exist? What pain came before it?
> 3. **Config example** — show a tiny Terraform/Ansible/shell snippet (interviewers love that you can *write* it, not just talk about it).
> 4. **Best practice** — how do real teams use it well, and what mistake do they avoid?
>
> If you wrap every answer in this `Definition → Problem → Example → Best practice` sandwich, you will sound senior even on topics you barely know. This file is built around that rhythm.

---

## How to use this file

- **PART A** solves all 5 practice exercises from `Plan.md`, plus 3 bonus drills (modules, state surgery, drift detection) — full Terraform HCL, Ansible YAML, and shell, with commands and explanations.
- **PART B** gives you 16 interview questions with spoken-style model answers and comparison tables.
- **Memory Hooks** gives you a vivid analogy for every concept so it sticks under pressure.
- A final interview tip closes it out.

Think of this as your *night-before-the-interview* file. Skim PART B, run the commands in PART A on a throwaway AWS account, and you will walk in calm. ☕

---

# PART A — Exercise / Practice Solutions

A note before we start: **always run these against a sandbox / free-tier AWS account, and always `terraform destroy` when done.** EC2 and RDS cost money 24/7. Treat the cloud like a taxi meter — it's running even when you're not looking. 🚕

---

## Exercise 1: Terraform Basics — Create an EC2 Instance

**Task recap:** AWS provider (Mumbai), a security group allowing SSH (22) + HTTP (8080), a `t3.micro` Amazon Linux 2023 instance, outputs for public IP + SSH command, variables for `instance_type` and `environment`.

### File structure
```
exercise1/
├── providers.tf
├── variables.tf
├── main.tf
└── outputs.tf
```

### providers.tf
```hcl
terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"   # Pin to 5.x so a new major version can't silently break us
    }
  }
}

provider "aws" {
  region = "ap-south-1"   # Mumbai
}
```

### variables.tf
```hcl
variable "instance_type" {
  description = "EC2 instance size"
  type        = string
  default     = "t3.micro"   # Free-tier-friendly default; override for prod
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"
}
```

### main.tf
```hcl
# Look up the latest Amazon Linux 2023 AMI instead of hardcoding an ID.
# Hardcoded AMIs go stale and differ per region — a data source self-heals.
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

# Firewall: allow SSH (management) + 8080 (the app). Egress fully open so the
# box can pull packages. In prod, lock SSH to your office IP — see best practice.
resource "aws_security_group" "web" {
  name        = "${var.environment}-web-sg"
  description = "Allow SSH and app traffic"

  ingress {
    description = "SSH access"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]   # TODO prod: restrict to ["<your-ip>/32"]
  }

  ingress {
    description = "Spring Boot app port"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"            # -1 = all protocols
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.environment}-web-sg"
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

resource "aws_instance" "web" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type
  vpc_security_group_ids = [aws_security_group.web.id]

  tags = {
    Name        = "${var.environment}-web-server"
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}
```

### outputs.tf
```hcl
output "public_ip" {
  description = "Public IP of the web server"
  value       = aws_instance.web.public_ip
}

output "ssh_command" {
  description = "Ready-to-paste SSH command"
  value       = "ssh ec2-user@${aws_instance.web.public_ip}"
}
```

### Commands
```bash
terraform init                                  # download the AWS provider (once)
terraform fmt                                   # auto-format your .tf files
terraform validate                              # catch syntax/type errors
terraform plan  -var="environment=dev"          # preview: "Plan: 2 to add"
terraform apply -var="environment=dev"          # type "yes" -> creates SG + EC2
# ... outputs print public_ip and ssh_command ...
terraform destroy -var="environment=dev"        # tear down when finished
```

**Why this is correct:** the data source keeps the AMI current, variables make the same code reusable across environments (`-var="instance_type=t3.large"` for prod), and outputs save you a trip to the AWS Console. The `~> 5.0` pin protects you from a surprise provider upgrade.

---

## Exercise 2: Terraform Advanced — Full VPC + EC2 + RDS + S3 + Remote State

**Task recap:** VPC `10.0.0.0/16`, 2 public + 2 private subnets, IGW + route table, web SG (22, 8080, 443) and DB SG (5432 from web only), RDS PostgreSQL in private subnets, an S3 bucket, and remote state in S3 with DynamoDB locking.

The `Plan.md` already contains the bulk of this (`vpc.tf`, `security-groups.tf`, `ec2.tf`, `rds.tf`, `s3.tf`, `outputs.tf`). The exercise's *new* requirement is **remote state with locking** — that's the part interviewers actually probe. Here is the complete remote-state setup plus the glue.

### Step 1 — Bootstrap the state backend (chicken-and-egg)

The S3 bucket and DynamoDB table that *hold* the state must exist *before* you can use them as a backend. Create them once with a tiny separate config (kept in local state) or the AWS CLI:

```bash
# Create the state bucket (globally unique name)
aws s3api create-bucket \
  --bucket my-company-terraform-state \
  --region ap-south-1 \
  --create-bucket-configuration LocationConstraint=ap-south-1

# Block all public access + enable versioning (so you can recover a bad state)
aws s3api put-public-access-block --bucket my-company-terraform-state \
  --public-access-block-configuration \
  BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
aws s3api put-bucket-versioning --bucket my-company-terraform-state \
  --versioning-configuration Status=Enabled

# Create the lock table — "LockID" is the exact attribute name Terraform expects
aws dynamodb create-table \
  --table-name terraform-state-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region ap-south-1
```

### Step 2 — backend.tf
```hcl
terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws    = { source = "hashicorp/aws", version = "~> 5.0" }
    random = { source = "hashicorp/random", version = "~> 3.0" }   # for S3 bucket suffix
  }

  backend "s3" {
    bucket         = "my-company-terraform-state"
    key            = "dev/terraform.tfstate"   # one key per environment keeps state separate
    region         = "ap-south-1"
    dynamodb_table = "terraform-state-lock"    # the LockID table — prevents concurrent applies
    encrypt        = true                      # encrypt the state at rest (it holds secrets)
  }
}
```

After adding the backend block you re-init so Terraform migrates state to S3:
```bash
terraform init -migrate-state
# "Do you want to copy existing state to the new backend? yes"
```

### Step 3 — the rest of the stack

Use the `Plan.md` files verbatim: `providers.tf`, `variables.tf`, `vpc.tf`, `security-groups.tf`, `ec2.tf`, `rds.tf`, `s3.tf`, `outputs.tf`. They already satisfy requirements 1–6 (VPC, 4 subnets, IGW + route table, the two security groups with `security_groups = [aws_security_group.web.id]` on the DB rule, the RDS instance in `private_a`/`private_b` via `aws_db_subnet_group`, and the encrypted/versioned S3 bucket).

> ⚠️ **One correctness note worth saying out loud in an interview:** the `Plan.md` RDS sits in *private* subnets (good — databases should never be internet-reachable), but those private subnets have **no NAT gateway and no route to the internet**. That's fine for RDS itself (it doesn't need outbound internet), but if you ever put an EC2 box in a private subnet, it can't reach the internet without a NAT gateway. Mentioning this nuance signals real-world VPC understanding.

### Step 4 — secrets, the right way
Never put `db_password` in a committed `.tfvars`. Use an environment variable instead:
```bash
export TF_VAR_db_username="devadmin"
export TF_VAR_db_password="$(openssl rand -base64 20)"   # generate a strong random password
terraform plan  -var-file="dev.tfvars"
terraform apply -var-file="dev.tfvars"
```
`TF_VAR_<name>` is Terraform's built-in way to feed a variable from the environment — keeps the secret out of files and shell history (use `export` carefully).

### Step 5 — .gitignore (non-negotiable)
```gitignore
# .gitignore — state holds passwords and resource IDs; it is NOT source code
*.tfstate
*.tfstate.*
.terraform/
.terraform.lock.hcl   # (optional to ignore; many teams COMMIT the lock file — see note)
crash.log
*.tfvars              # if they contain secrets
override.tf
```
*(Note: `.terraform.lock.hcl` — the dependency lock file — is usually **committed** so everyone uses identical provider versions. Only the state, the cache dir, and secret tfvars must be ignored.)*

### Verify the lock actually works
Open two terminals, run `terraform apply` in both. The second one prints:
```
Error: Error acquiring the state lock
Lock Info:
  ID:        9f8e7d6c-...
  Operation: OperationTypeApply
  Who:       you@laptop
```
That's the DynamoDB lock doing its job — exactly the "railway ticket token" from the Plan. Only one apply at a time. 🎫

---

## Exercise 3: Ansible Basics — Install Java and Deploy a JAR

**Task recap:** SSH to a target, update packages, install Java 17, create the `springapp` user, copy a JAR to `/opt/myapp/`, create a systemd service, start it, verify port 8080.

### inventory.ini
```ini
[web_servers]
13.232.45.67 ansible_user=ec2-user ansible_ssh_private_key_file=~/.ssh/id_rsa
```

### deploy-jar.yml
```yaml
---
- name: Install Java and deploy Spring Boot JAR
  hosts: web_servers
  become: yes                    # run tasks with sudo
  vars:
    app_name: "myapp"
    app_user: "springapp"
    app_port: 8080
    app_dir: "/opt/{{ app_name }}"
    jar_source: "./target/my-app-1.0.0.jar"

  tasks:
    - name: Update all system packages
      ansible.builtin.yum:
        name: "*"
        state: latest          # idempotent: only updates what's outdated

    - name: Install Java 17 (Amazon Corretto)
      ansible.builtin.yum:
        name: "java-17-amazon-corretto"
        state: present         # "present" = ensure installed; does nothing if already there

    - name: Create the application user
      ansible.builtin.user:
        name: "{{ app_user }}"
        system: yes
        shell: /bin/false      # the app user cannot log in interactively (security)

    - name: Create the application directory
      ansible.builtin.file:
        path: "{{ app_dir }}"
        state: directory
        owner: "{{ app_user }}"
        group: "{{ app_user }}"
        mode: "0755"

    - name: Copy the JAR to the server
      ansible.builtin.copy:
        src: "{{ jar_source }}"
        dest: "{{ app_dir }}/{{ app_name }}.jar"
        owner: "{{ app_user }}"
        group: "{{ app_user }}"
        mode: "0644"
      notify: Restart app        # only fires the handler if the JAR actually changed

    - name: Install the systemd service unit
      ansible.builtin.copy:
        dest: "/etc/systemd/system/{{ app_name }}.service"
        mode: "0644"
        content: |
          [Unit]
          Description={{ app_name }} Spring Boot app
          After=network.target

          [Service]
          User={{ app_user }}
          ExecStart=/usr/bin/java -jar {{ app_dir }}/{{ app_name }}.jar --server.port={{ app_port }}
          SuccessExitStatus=143   # 143 = SIGTERM; a clean stop, not a crash
          Restart=on-failure
          RestartSec=10

          [Install]
          WantedBy=multi-user.target
      notify: Restart app

    - name: Reload systemd so it sees the new unit
      ansible.builtin.systemd:
        daemon_reload: yes

    - name: Start and enable the app on boot
      ansible.builtin.systemd:
        name: "{{ app_name }}"
        state: started
        enabled: yes

    - name: Wait until the app answers on port {{ app_port }}
      ansible.builtin.uri:
        url: "http://localhost:{{ app_port }}/actuator/health"
        status_code: 200
      register: health
      until: health.status == 200
      retries: 30
      delay: 5                  # poll every 5s, up to 150s total

  handlers:
    - name: Restart app
      ansible.builtin.systemd:
        name: "{{ app_name }}"
        state: restarted
```

### Commands
```bash
ansible-playbook -i inventory.ini deploy-jar.yml --check   # dry run first (like terraform plan)
ansible-playbook -i inventory.ini deploy-jar.yml           # for real
```

**Why this is correct & idempotent:** every step uses a *purpose-built module* (`yum`, `user`, `file`, `copy`, `systemd`) rather than raw `shell`/`command`. That means running the playbook twice is safe — `state: present` skips an already-installed Java, the user isn't recreated, and the JAR is only re-copied (and the handler only fires) if the file content changed. Run it 10 times → same result as once. That's the whole point of Ansible. ♻️

---

## Exercise 4: Ansible — Nginx Reverse Proxy with HTTPS + Handlers

**Task recap:** install Nginx, reverse-proxy port 80 → localhost:8080, add HTTPS with a self-signed cert, restart Nginx *only* when config changes (handlers), verify it's running.

### nginx-proxy.yml
```yaml
---
- name: Nginx reverse proxy with self-signed HTTPS
  hosts: web_servers
  become: yes
  vars:
    app_port: 8080
    cert_dir: "/etc/nginx/ssl"
    server_name: "myapp.local"

  tasks:
    - name: Install Nginx
      ansible.builtin.yum:
        name: nginx
        state: present

    - name: Ensure the SSL directory exists
      ansible.builtin.file:
        path: "{{ cert_dir }}"
        state: directory
        mode: "0700"

    # creates: makes this idempotent — openssl only runs if the cert doesn't exist yet
    - name: Generate a self-signed certificate (learning only)
      ansible.builtin.command: >
        openssl req -x509 -nodes -days 365 -newkey rsa:2048
        -keyout {{ cert_dir }}/selfsigned.key
        -out {{ cert_dir }}/selfsigned.crt
        -subj "/CN={{ server_name }}"
      args:
        creates: "{{ cert_dir }}/selfsigned.crt"

    - name: Deploy the reverse-proxy config
      ansible.builtin.copy:
        dest: /etc/nginx/conf.d/reverse-proxy.conf
        mode: "0644"
        content: |
          server {
              listen 80;
              server_name {{ server_name }};
              # Push everyone to HTTPS
              return 301 https://$host$request_uri;
          }
          server {
              listen 443 ssl;
              server_name {{ server_name }};
              ssl_certificate     {{ cert_dir }}/selfsigned.crt;
              ssl_certificate_key {{ cert_dir }}/selfsigned.key;
              location / {
                  proxy_pass http://localhost:{{ app_port }};
                  proxy_set_header Host $host;
                  proxy_set_header X-Real-IP $remote_addr;
                  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                  proxy_set_header X-Forwarded-Proto $scheme;
              }
          }
      notify: Reload Nginx     # the magic: Nginx reloads ONLY if this file changed

    - name: Make sure Nginx is started and enabled
      ansible.builtin.systemd:
        name: nginx
        state: started
        enabled: yes

    - name: Verify Nginx answers on 443
      ansible.builtin.uri:
        url: "https://localhost/"
        validate_certs: no     # self-signed cert, so skip validation in this lab
        status_code: [200, 502]  # 502 is OK if the app behind it isn't up yet
      register: nginx_check

    - name: Show the result
      ansible.builtin.debug:
        msg: "Nginx responded with HTTP {{ nginx_check.status }}"

  handlers:
    - name: Reload Nginx
      ansible.builtin.systemd:
        name: nginx
        state: reloaded        # reload > restart: keeps connections alive, no downtime
```

**The handler is the star of this exercise.** Note how the config task `notify`s `Reload Nginx`. If you run the playbook again and the config hasn't changed, the `copy` task reports `ok` (not `changed`), the handler is **never notified**, and Nginx is **not** reloaded. That's the difference between a pro playbook and a naive one that bounces Nginx on every run. `reloaded` (graceful) is also chosen over `restarted` because it re-reads config with zero dropped connections. 🔄

---

## Exercise 5: Terraform + Ansible — The Full Pipeline

**Task recap:** Terraform creates VPC + EC2 + SGs, a `local-exec` provisioner writes an Ansible inventory with the new IP, then run the Exercise-3 playbook, then browse the app.

This is the real-world pattern: **Terraform builds, Ansible configures.** Two tools, one assembly line.

### main.tf (the provisioner glue)
```hcl
resource "aws_instance" "web" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type
  key_name               = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.web.id]
  subnet_id              = aws_subnet.public_a.id

  tags = { Name = "${var.environment}-web", ManagedBy = "terraform" }
}

# After the box exists, Terraform shells out locally to write an inventory file.
# local-exec runs on YOUR machine, not on the EC2 box.
resource "null_resource" "ansible_inventory" {
  triggers = {
    instance_id = aws_instance.web.id   # regenerate inventory if the instance changes
  }

  provisioner "local-exec" {
    command = <<-EOT
      cat > inventory.ini <<EOF
      [web_servers]
      ${aws_instance.web.public_ip} ansible_user=ec2-user ansible_ssh_private_key_file=~/.ssh/id_rsa
      EOF
    EOT
  }
}

# Wait for SSH to come up, then run the playbook — still on the local machine.
resource "null_resource" "run_ansible" {
  depends_on = [null_resource.ansible_inventory]

  provisioner "local-exec" {
    command = <<-EOT
      # Poll until SSH (port 22) is reachable, then configure the box
      until nc -z ${aws_instance.web.public_ip} 22; do echo "waiting for ssh..."; sleep 5; done
      ANSIBLE_HOST_KEY_CHECKING=False ansible-playbook -i inventory.ini deploy-jar.yml
    EOT
  }
}
```

### Run it end to end
```bash
terraform init
terraform apply -var="environment=dev" -auto-approve
#   1. EC2 + SG + VPC come up
#   2. inventory.ini is written with the live public IP
#   3. the SSH wait loop blocks until port 22 answers
#   4. ansible-playbook installs Java + deploys the JAR + starts systemd
terraform output ssh_command
# Browse:  http://<public_ip>:8080/actuator/health   ->  {"status":"UP"}
```

**The honest best-practice caveat (say this in the interview):** provisioners are officially a *last resort* in Terraform. The cleaner production pattern is to let Terraform `output` the IP and run Ansible as a **separate CI/CD step**, or use a **dynamic inventory plugin** (`aws_ec2`) that reads live instances straight from AWS by tag — no brittle inventory-file generation. Knowing *that the provisioner approach is the "tutorial" way and the dynamic inventory is the "real" way* is exactly the kind of nuance that separates a junior from a mid-level candidate. 🎯

---

## Bonus Exercise 6: Write a Reusable Terraform Module

**Task:** turn the EC2 + SG into a module and call it twice (dev + prod) with different sizes.

### modules/web-server/variables.tf
```hcl
variable "environment"   { type = string }
variable "instance_type" { type = string }
variable "subnet_id"     { type = string }
```

### modules/web-server/main.tf
```hcl
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]
  filter { name = "name", values = ["al2023-ami-*-x86_64"] }
}

resource "aws_security_group" "this" {
  name = "${var.environment}-web-sg"
  ingress { from_port = 8080, to_port = 8080, protocol = "tcp", cidr_blocks = ["0.0.0.0/0"] }
  egress  { from_port = 0,    to_port = 0,    protocol = "-1",  cidr_blocks = ["0.0.0.0/0"] }
}

resource "aws_instance" "this" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = [aws_security_group.this.id]
  tags = { Name = "${var.environment}-web", ManagedBy = "terraform" }
}
```

### modules/web-server/outputs.tf
```hcl
output "public_ip"   { value = aws_instance.this.public_ip }
output "instance_id" { value = aws_instance.this.id }
```

### environments/main.tf — call the module twice
```hcl
module "dev_web" {
  source        = "../modules/web-server"
  environment   = "dev"
  instance_type = "t3.micro"        # small + cheap for dev
  subnet_id     = var.dev_subnet_id
}

module "prod_web" {
  source        = "../modules/web-server"
  environment   = "prod"
  instance_type = "t3.large"        # beefier for prod — same code, different knob
  subnet_id     = var.prod_subnet_id
}

output "dev_ip"  { value = module.dev_web.public_ip }
output "prod_ip" { value = module.prod_web.public_ip }
```

**Why this matters:** the EC2/SG recipe is written **once**. Change the AMI logic or add a tag, and both environments inherit it. That's "DRY" (Don't Repeat Yourself) — a module is to Terraform what a function is to Java. Fix the bug in one place, not three. 🧩

---

## Bonus Exercise 7: State Management Surgery

**Task:** show the three state operations every engineer eventually needs.

```bash
# 1. INSPECT — list everything Terraform is tracking
terraform state list
#   aws_instance.web
#   aws_security_group.web

# 2. IMPORT — adopt a resource that already exists in AWS (created manually) into state.
#    You first write the matching resource block, then import the real ID:
terraform import aws_instance.web i-0abc123def456
#    Now Terraform "owns" that box — future plans will manage it.

# 3. MOVE — you renamed a resource in code; tell state without destroy/recreate
terraform state mv aws_instance.web aws_instance.app_server

# 4. REMOVE — stop managing a resource WITHOUT destroying it in AWS
terraform state rm aws_instance.web

# 5. SHOW — see the recorded attributes of one resource
terraform state show aws_instance.web
```

**When you'd use each:** `import` when migrating click-ops infrastructure into Terraform (very common first job on a new team); `mv` after a refactor so you don't needlessly destroy/recreate a live database; `rm` when handing a resource off to another state file. The golden rule: **never hand-edit `terraform.tfstate` with a text editor** — use these commands, which keep the JSON consistent. ✋

---

## Bonus Exercise 8: Detect and Fix Configuration Drift

**Task:** someone changed a security group in the AWS Console. Detect it and decide what to do.

```bash
# 1. Detect drift — refresh state against reality, then plan
terraform plan
#   ~ aws_security_group.web has changed outside of Terraform:
#     ~ ingress { + from_port = 3389 }   # someone opened RDP in the console!
#   Plan: 0 to add, 1 to change, 0 to destroy.

# Option A — REVERT: let Terraform undo the manual change (code is the source of truth)
terraform apply        # removes the rogue port 3389 rule, restoring code's intent

# Option B — ADOPT: the manual change was actually wanted, so codify it instead.
#   Edit security-groups.tf to ADD the 3389 ingress rule, then:
terraform plan         # "No changes. Your infrastructure matches the configuration."
```

**The lesson:** drift is what happens when reality diverges from your code. Terraform's `plan` is your drift *detector*; `apply` is your drift *corrector*. The healthy team rule is **"all changes go through code"** — if you must hotfix in the console during an incident, you immediately back-port that change into the `.tf` files so the next `apply` doesn't silently undo your fix. Drift left unmanaged is how snowflake servers creep back in. ❄️

---

# PART B — Interview Questions & Model Answers

Answers are written the way you'd *speak* them — confident, concise, with a tiny example. Read them aloud once or twice.

---

### Q1. What is Infrastructure as Code, and what problem does it solve?

> "Infrastructure as Code means defining your servers, networks, and databases in version-controlled code files instead of clicking around in a cloud console. The problem it solves is the 'snowflake server' nightmare — where every machine was hand-built differently, nobody documented it, and recreating it after a disaster takes days of guesswork. With IaC, your whole environment lives in Git, so I can run one command and get an identical copy. It gives you six superpowers: reproducibility, version control, speed, self-documentation, disaster recovery, and no snowflakes. The analogy I like: building a house from a blueprint instead of from memory — hand the blueprint to ten builders and you get ten identical houses."

---

### Q2. Declarative vs imperative — what's the difference, and which is Terraform?

> "Imperative means you write the *steps*: create the VPC, then the subnet, then the EC2 — like giving a rickshaw driver turn-by-turn directions. Declarative means you describe the *end state* — like telling an Uber 'take me to Koramangala' and letting it figure out the route. Terraform is declarative: I say 'I want one VPC and one EC2,' and Terraform figures out the order, the dependencies, and what already exists. The big win is idempotency — run a declarative config ten times and you still get exactly one VPC, whereas an imperative shell script might create duplicates or fail on the second run."

| | Imperative | Declarative |
|---|---|---|
| You specify | The *steps* (how) | The *end state* (what) |
| Order/dependencies | You manage them | Tool figures them out |
| Re-run safety | Risky (duplicates/errors) | Idempotent |
| Examples | Bash, AWS CLI | Terraform, CloudFormation, Ansible (mostly) |

---

### Q3. Terraform vs Ansible — when do you use which?

> "They do different jobs. Terraform is a *provisioning* tool — it builds infrastructure: VPCs, EC2, RDS, load balancers. Ansible is a *configuration* tool — it gets inside an existing server and installs software, deploys your app, edits config files. The restaurant analogy: Terraform builds the building and runs the plumbing; Ansible sets up the kitchen and trains the staff. In real life you use both together — Terraform creates the EC2, Ansible SSHes in and installs Java and deploys the JAR. Terraform is declarative and keeps a state file; Ansible is agentless, runs over SSH, and is mostly procedural but designed around idempotent modules."

| Aspect | Terraform | Ansible |
|---|---|---|
| Primary job | Provision infrastructure | Configure servers |
| Language | HCL | YAML |
| Model | Declarative + state file | Procedural, mostly idempotent |
| Agent? | No (uses cloud APIs) | No — agentless, SSH |
| State | Yes (`terraform.tfstate`) | No state — checks live system each run |
| Push/Pull | n/a (API calls) | Push (control node pushes over SSH) |
| Best at | Cloud resources, multi-cloud | App deploy, OS config, orchestration |

---

### Q4. Terraform vs CloudFormation?

> "CloudFormation is AWS's native IaC — YAML or JSON, AWS-only, no extra tool to install, and the deepest AWS integration. Terraform is multi-cloud — same HCL language works across AWS, Azure, GCP, and a thousand other providers — with a bigger community and more readable syntax. Most companies pick Terraform because they want the option to go multi-cloud and because HCL is far less verbose than CloudFormation's YAML. You'd pick CloudFormation if you're 100% committed to AWS forever and want zero third-party tooling."

---

### Q5. What is the Terraform state file and why does it matter?

> "The state file — `terraform.tfstate` — is Terraform's memory. It's a JSON map between the resources in my code and the real resources in AWS, storing things like 'aws_instance.web' equals 'i-0abc123'. Without it, Terraform wouldn't know that instance already exists, so every `apply` would try to create a brand-new one. State is also how Terraform calculates the *diff* during `plan` — it compares desired config, recorded state, and live reality. Two critical rules: never commit it to Git, because it contains secrets like database passwords; and for teams, store it remotely in S3 with DynamoDB locking so people don't clobber each other."

---

### Q6. Why must state be stored remotely for a team, and what does locking do?

> "On your laptop, local state is fine. But on a team, if two people each have their own copy and both apply, they overwrite each other's changes and the state diverges from reality — chaos. So you put one shared state in S3, and everyone reads and writes the same file. Locking, via a DynamoDB table, ensures only one `apply` runs at a time — the second person gets 'Error acquiring the state lock' and waits. It's exactly the token system at a railway booking counter: only the person holding the token can book; everyone else queues. Without locking, two simultaneous applies can corrupt the state file."

---

### Q7. Walk me through the core Terraform workflow.

> "Four commands. `terraform init` downloads the provider plugins and sets up the directory — you run it once per project. `terraform plan` previews exactly what will be created, changed, or destroyed without touching anything — it's the restaurant bill before you pay. `terraform apply` actually makes the changes after you confirm with 'yes' — that's paying the bill. And `terraform destroy` tears everything down, which you use to clean up dev environments so you stop paying for idle EC2. The discipline is: always `plan` before `apply`, because `apply` without review is like running `DROP TABLE` without checking the table name."

---

### Q8. What does idempotency mean, and why is it the heart of good IaC?

> "Idempotent means running an operation once or a hundred times gives the same result. Press an elevator button five times — the elevator still comes once. In Ansible, `state: present` for a package means 'make sure it's installed' — if Java's already there, Ansible does nothing instead of reinstalling. In Terraform, the state file makes `apply` idempotent — it only creates what's missing. This matters because automation reruns constantly — in CI, on retries, after failures — and a non-idempotent script that re-installs or duplicates resources on every run is a landmine. Idempotency is what makes IaC safe to run on autopilot."

---

### Q9. What are Terraform modules and why use them?

> "A module is a reusable package of Terraform resources — like a function in programming. Instead of copy-pasting the same VPC code into dev, staging, and prod and changing it in three places when something updates, you write it once as a module and call it three times with different variables. It keeps your code DRY: fix a bug in one place, and every environment inherits the fix. The Terraform Registry has thousands of community modules — the official AWS VPC module, for instance, saves you writing hundreds of lines. Every serious Terraform codebase is mostly module calls."

---

### Q10. Variables vs outputs — what's the difference?

> "Variables are *inputs* — they parameterize your config so the same code works across environments. `var.instance_type` is `t3.micro` in dev and `t3.large` in prod. You set them via `-var`, `.tfvars` files, or `TF_VAR_` environment variables. Outputs are the *opposite direction* — they expose values *after* apply, like the EC2's public IP or the RDS endpoint, so you don't have to dig through the AWS Console, and so other modules can consume them. Quick mnemonic: variables go *in*, outputs come *out*. And mark anything secret as `sensitive = true` so it doesn't leak into logs."

---

### Q11. What are Terraform provisioners, and when should you avoid them?

> "Provisioners run scripts as part of resource creation — `local-exec` runs a command on *your* machine, `remote-exec` runs over SSH on the *new* resource. They're handy for the tutorial pipeline where Terraform writes an Ansible inventory and kicks off a playbook. But HashiCorp explicitly calls them a *last resort*, because they break Terraform's declarative model — they're not tracked in state, they don't roll back cleanly, and failures are messy. The better patterns are: bake your config into the AMI with Packer, use cloud-init `user_data`, or output the IP and run Ansible as a separate CI step with a dynamic inventory. So I use provisioners knowingly, not by default."

---

### Q12. What is configuration drift, and how do you handle it?

> "Drift is when the real infrastructure diverges from what your code says — usually because someone made a manual change in the console, like opening a port during an incident. You *detect* drift by running `terraform plan`: it refreshes state against reality and shows the difference. Then you have two choices: revert — run `apply` to undo the manual change and restore the code's intent — or adopt — update your `.tf` files to match the change, so it's now codified. The team rule is 'all changes go through code.' Drift left unmanaged is how snowflake servers sneak back in, so you catch it early, ideally with a scheduled `plan` in CI that alerts on any diff."

---

### Q13. Why is Ansible 'agentless,' and why is that an advantage?

> "Agentless means Ansible doesn't install any software on the servers it manages — it just needs SSH and Python, which Linux boxes already have. The control machine pushes commands over SSH. Compare that to Chef or Puppet, which need an agent daemon running on every single server, plus a central server, plus certificate management — a lot of moving parts to maintain and secure. Ansible is like a TV remote: nothing installed on the TV, it just sends signals. Chef/Puppet are like a smart-TV app you have to install and update everywhere. Fewer moving parts means easier onboarding and a smaller attack surface."

---

### Q14. Explain Ansible playbooks, roles, inventory, and handlers.

> "The *inventory* is your address book — an INI or YAML list of the servers to manage, grouped by role like `[web_servers]` and `[db_servers]`. A *playbook* is a YAML file describing what to do on which hosts — it's the core of Ansible. Inside it, *tasks* are individual actions: install a package, copy a file, start a service. *Roles* bundle related tasks, files, and templates into a reusable unit — like a Java package grouping related classes — so you can share 'the nginx role' across projects. And *handlers* are special tasks that only run when *notified* by a change — the classic example is 'restart Nginx,' which fires only if the config file actually changed, not on every run. That's what keeps playbooks efficient and non-disruptive."

---

### Q15. Push vs pull configuration management?

> "Push means a central control node initiates and pushes config out to the servers — that's Ansible: you run `ansible-playbook` and it SSHes out to the targets right then. Pull means each server runs an agent that periodically *pulls* its config from a central server — that's Chef and Puppet's default model. Push is simpler and more immediate, great for orchestrated deploys where order matters. Pull scales better to thousands of nodes and self-heals continuously, since each node keeps re-checking. Ansible can actually do pull too with `ansible-pull`, but its default and its strength is push."

| | Push (Ansible) | Pull (Chef/Puppet) |
|---|---|---|
| Who initiates | Central control node | Each server's agent |
| Timing | On-demand, immediate | Periodic (e.g. every 30 min) |
| Agent needed | No | Yes |
| Scales to thousands | Good with effort | Excellent natively |
| Best for | Orchestrated deploys | Continuous large-fleet config |

---

### Q16. What are the top IaC best practices and common mistakes you watch for?

> "Best practices: always store state remotely with locking; always `plan` before `apply`; never make manual changes to Terraform-managed resources; parameterize everything that differs between environments with variables; use modules to stay DRY; tag every resource with Name, Environment, and ManagedBy; pin provider versions; and keep secrets out of code — use environment variables or a vault. The biggest mistakes I actively avoid: committing `terraform.tfstate` to Git, because it holds passwords; hardcoding AMI IDs instead of using a data source; forgetting to destroy dev resources, which quietly bleeds money; and in Ansible, using raw `shell`/`command` modules instead of purpose-built ones, which silently breaks idempotency. I'd rather catch these in review than in a 2 a.m. incident."

---

## 🧠 Memory Hooks — Analogies That Stick Under Pressure

| Concept | Vivid Analogy | Trigger Phrase |
|---|---|---|
| **Infrastructure as Code** | A house blueprint you can hand to 10 builders → 10 identical houses | "Blueprint, not memory" 🏠 |
| **Snowflake server** | Mom's biryani cooked from memory — slightly different every time | "Every snowflake is unique" ❄️ |
| **Declarative (Terraform)** | Telling Uber "take me to Koramangala" — it picks the route | "What, not how" 🚗 |
| **Imperative (shell/CLI)** | Turn-by-turn rickshaw directions you must get exactly right | "Step by step" 🛺 |
| **Idempotency** | Pressing the elevator button 5 times — it still comes once | "Press twice, same result" 🛗 |
| **State file** | Terraform's memory / a map of code → real AWS resource IDs | "The source of truth" 🧠 |
| **State locking** | Railway counter token — only the holder can book, others wait | "One token, one apply" 🎫 |
| **Remote state** | Shared Google Doc vs everyone editing their own copy | "One shared brain" ☁️ |
| **Modules** | Functions in code — write once, call many times | "DRY = don't repeat yourself" 🧩 |
| **Variables / Outputs** | Variables go *in*, outputs come *out* (like a function's params & return) | "In and out" 🔁 |
| **Provisioner** | A hammer Terraform keeps for emergencies — useful, but messy | "Last resort" 🔨 |
| **Drift** | Repainting a wall without updating the blueprint | "Reality vs the plan" 🎨 |
| **Provision vs Configure** | Terraform *builds* the restaurant; Ansible *sets up* the kitchen | "Build, then furnish" 🍽️ |
| **Agentless (Ansible)** | TV remote — nothing installed on the TV, just sends signals | "Remote control" 📺 |
| **Handler** | A motion-sensor light — only fires when something actually moves | "Only on change" 💡 |
| **Push vs Pull** | Push = pizza delivered to you; Pull = you go fetch it yourself | "Delivered vs fetched" 🍕 |
| **Cattle, not pets** | Don't nurse a sick server — replace it like cattle, not a pet | "Replace, don't repair" 🐄 |
| **plan before apply** | The bill preview before you pay at a restaurant | "Check the bill first" 🧾 |

---

## 🎤 Final Interview Tip

When you get an IaC question, **resist the urge to dump everything you know.** Instead, run the golden rule out loud in order: *"IaC is X (definition). Before it, teams suffered Y (problem). Here's a tiny example (snippet). And the best practice is Z."* That four-beat rhythm makes even a 20-second answer sound structured and senior.

And when you genuinely don't know something, **reach for the analogy you *do* know** and reason from it: "I haven't used Pulumi, but since it's also a provisioning tool like Terraform, I'd expect it to keep state and have a plan/apply cycle — the difference being it uses a real language instead of HCL." Interviewers reward *structured reasoning from fundamentals* far more than memorized trivia.

Finally — **mention the safety habits unprompted**: `plan` before `apply`, never commit state, always `destroy` dev resources, all changes through code. Dropping these naturally signals that you've actually run this in anger, not just read about it. That's the tell of someone they can trust with the production AWS account. 🚀

Good luck — you've got this. 💪
