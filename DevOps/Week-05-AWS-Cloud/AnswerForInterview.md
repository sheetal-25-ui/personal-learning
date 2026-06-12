# Week 5: AWS Cloud — Complete Guide — Interview Answers & Exercise Solutions

> **The Interview Golden Rule for every AWS answer** 🏆
> When asked about any AWS service, structure your answer in 4 beats:
> 1. **Definition** — one crisp sentence: what it *is*.
> 2. **What problem it solves** — *why* it exists, the pain before it.
> 3. **Example / use case** — a concrete "in my project I used it to…" moment.
> 4. **Cost / trade-off awareness** — show you think like an engineer who pays the bill (pricing model, when NOT to use it, the cheaper alternative).
>
> Interviewers don't just want the *what* — they want to know you understand *when* and *why*, and that you respect the cost. A candidate who says "S3 is object storage" sounds junior. A candidate who says "S3 is object storage; I'd put frequently-read files in Standard and move 90-day-old logs to Glacier via a lifecycle rule to cut storage cost by ~90%" sounds hireable. Always land the trade-off. 💰

---

## PART A — Exercise / Practice Solutions

The Plan.md ends with 5 hands-on exercises. Here are complete worked solutions for all of them, plus 3 bonus exercises (IAM, Lambda, CloudWatch billing alarm) that interviewers love to ask about.

> ⚙️ **Setup assumption:** AWS CLI v2 is installed and configured (`aws configure` with your IAM access key, secret, default region `ap-south-1`, output `json`). Verify with:
> ```bash
> aws sts get-caller-identity
> # Returns your Account, UserId, and ARN — confirms the CLI is wired up correctly.
> ```

---

### ✅ Exercise 1: Launch EC2 and Run Spring Boot

**Goal:** Get a Spring Boot app running on AWS.

**Console steps (1–3): Account + IAM + MFA**
1. Sign up at aws.amazon.com (free tier).
2. Log in as **root** ONLY to create your admin IAM user: Console → IAM → Users → Add users → name `sheetal-dev` → "Provide console access" → attach `AdministratorAccess` → Create.
3. Enable MFA on **root** (IAM dashboard → "Add MFA" for root) and on `sheetal-dev` (Users → Security credentials → MFA). Use Google Authenticator. **Then log out of root and never use it again for daily work.**

**CLI solution (4–9): Launch, connect, deploy**

```bash
# 4. Create a key pair and a security group, then launch a t2.micro (free tier)

# Create an SSH key pair and save the private key locally
aws ec2 create-key-pair --key-name my-key-pair \
  --query 'KeyMaterial' --output text > my-key-pair.pem
chmod 400 my-key-pair.pem   # SSH refuses keys that are world-readable

# Create a security group
aws ec2 create-security-group \
  --group-name springboot-sg \
  --description "SSH + Spring Boot 8080"

# Allow SSH from YOUR IP only (never 0.0.0.0/0 for port 22!)
MYIP=$(curl -s https://checkip.amazonaws.com)
aws ec2 authorize-security-group-ingress \
  --group-name springboot-sg --protocol tcp --port 22 --cidr ${MYIP}/32

# Allow Spring Boot port 8080 from anywhere (so users can reach the app)
aws ec2 authorize-security-group-ingress \
  --group-name springboot-sg --protocol tcp --port 8080 --cidr 0.0.0.0/0

# Launch the instance (Amazon Linux 2023 AMI — look up the latest ID for your region)
AMI=$(aws ssm get-parameters \
  --names /aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64 \
  --query 'Parameters[0].Value' --output text)

aws ec2 run-instances \
  --image-id $AMI \
  --instance-type t2.micro \
  --key-name my-key-pair \
  --security-groups springboot-sg \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=springboot-demo}]'
```

```bash
# 5. Find the public IP and SSH in
aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=springboot-demo" "Name=instance-state-name,Values=running" \
  --query 'Reservations[].Instances[].PublicIpAddress' --output text
# e.g. 3.110.85.42

ssh -i my-key-pair.pem ec2-user@3.110.85.42

# 6. Install Java 17 (on the instance)
sudo yum install java-17-amazon-corretto -y
java -version

# 7-8. From your LOCAL machine, build the JAR and copy it up
mvn clean package                                  # produces target/myapp.jar
scp -i my-key-pair.pem target/myapp.jar ec2-user@3.110.85.42:/home/ec2-user/

# 9. Run it on EC2 in the background so it survives logout
nohup java -jar myapp.jar > app.log 2>&1 &
curl http://localhost:8080/hello                   # local smoke test
```

```
# 10. From your browser:  http://3.110.85.42:8080/hello
```

**Why these choices (interview gold):** SSH restricted to my IP (`/32`) because bots constantly scan the internet for open port 22. `nohup … &` because a plain `java -jar` dies when the SSH session closes. Tagged the instance with `Name` so I can find it later and avoid the "20 untagged instances" mess.

---

### ✅ Exercise 2: Host a Static Website on S3

**Goal:** Understand S3 basics.

```bash
# 1. Create a globally-unique bucket (bucket names are global across ALL AWS accounts)
aws s3 mb s3://sheetal-devops-portfolio-2026 --region ap-south-1

# 2-3. Create and upload a simple page
echo '<h1>Sheetal — DevOps Portfolio</h1>' > index.html
aws s3 cp index.html s3://sheetal-devops-portfolio-2026/

# 4. Enable static website hosting
aws s3 website s3://sheetal-devops-portfolio-2026/ --index-document index.html

# 5a. By default S3 blocks public access — disable that block for this bucket
aws s3api put-public-access-block \
  --bucket sheetal-devops-portfolio-2026 \
  --public-access-block-configuration \
  "BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false"

# 5b. Attach a public-read bucket policy
cat > policy.json <<'EOF'
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": "*",
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::sheetal-devops-portfolio-2026/*"
  }]
}
EOF
aws s3api put-bucket-policy \
  --bucket sheetal-devops-portfolio-2026 --policy file://policy.json
```

```
# 6. Visit:
http://sheetal-devops-portfolio-2026.s3-website.ap-south-1.amazonaws.com
```

**Trade-off to mention:** The raw S3 website endpoint is HTTP-only. For a real site you'd put **CloudFront** (CDN) in front for HTTPS, caching, and a custom domain. S3 + CloudFront is the standard "serverless static site" pattern — no EC2, pay only for storage + requests (pennies).

---

### ✅ Exercise 3: Set Up RDS MySQL and Connect from Spring Boot

**Goal:** Understand managed databases.

```bash
# 1. Create a free-tier MySQL instance (db.t3.micro)
aws rds create-db-instance \
  --db-instance-identifier myapp-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password 'SuperSecretPass123!' \
  --allocated-storage 20 \
  --db-name myappdb \
  --backup-retention-period 7 \
  --no-multi-az          # single-AZ for free-tier learning; enable Multi-AZ in prod

# 2-3. (--db-name above already creates the "myappdb" database in the default VPC)

# Wait until it's available, then grab the endpoint
aws rds wait db-instance-available --db-instance-identifier myapp-db
aws rds describe-db-instances --db-instance-identifier myapp-db \
  --query 'DBInstances[0].Endpoint.Address' --output text
# e.g. myapp-db.abcdefg12345.ap-south-1.rds.amazonaws.com
```

**Allow your app to reach the DB (security group):** Add an inbound rule on the RDS security group for port `3306`, source = the EC2/app security group (NOT 0.0.0.0/0).

```properties
# 4. application.properties on the Spring Boot side
spring.datasource.url=jdbc:mysql://myapp-db.abcdefg12345.ap-south-1.rds.amazonaws.com:3306/myappdb
spring.datasource.username=admin
spring.datasource.password=${DB_PASSWORD}        # injected via env var — never hardcode!
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
```

```bash
# 5-6. Run the app with the secret as an env var, then verify a row lands in RDS
export DB_PASSWORD='SuperSecretPass123!'
java -jar myapp.jar
# POST a User, then confirm:  SELECT * FROM users;  via mysql client over the endpoint
```

**Trade-off:** RDS is "managed" — AWS handles backups, patching, failover. You pay a premium over self-hosting MySQL on EC2, but you stop being a part-time DBA. For learning use single-AZ; for production enable **Multi-AZ** (automatic failover) and **read replicas** for read-heavy traffic.

---

### ✅ Exercise 4: Create a VPC with Public and Private Subnets

**Goal:** Understand networking.

```bash
# 1. Create the VPC (65,536 IPs)
VPC=$(aws ec2 create-vpc --cidr-block 10.0.0.0/16 \
  --query 'Vpc.VpcId' --output text)

# 2. Public + private subnets
PUB=$(aws ec2 create-subnet --vpc-id $VPC --cidr-block 10.0.1.0/24 \
  --availability-zone ap-south-1a --query 'Subnet.SubnetId' --output text)
PRIV=$(aws ec2 create-subnet --vpc-id $VPC --cidr-block 10.0.2.0/24 \
  --availability-zone ap-south-1b --query 'Subnet.SubnetId' --output text)

# Auto-assign public IPs to instances launched in the public subnet
aws ec2 modify-subnet-attribute --subnet-id $PUB --map-public-ip-on-launch

# 3. Internet Gateway, attached to the VPC
IGW=$(aws ec2 create-internet-gateway --query 'InternetGateway.InternetGatewayId' --output text)
aws ec2 attach-internet-gateway --vpc-id $VPC --internet-gateway-id $IGW

# 4. Public route table: 0.0.0.0/0 -> IGW, then associate the public subnet
RT=$(aws ec2 create-route-table --vpc-id $VPC --query 'RouteTable.RouteTableId' --output text)
aws ec2 create-route --route-table-id $RT \
  --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW
aws ec2 associate-route-table --route-table-id $RT --subnet-id $PUB
# The private subnet uses the VPC's default route table (local only) — no internet route.
```

**5–7. Verify the isolation (the whole point):**
- Launch a **bastion/web EC2 in the public subnet** → it gets a public IP → you can `ssh` to it from your laptop. ✅
- Launch an **app EC2 in the private subnet** → no public IP → SSH from the internet **fails** (no route, no public IP). ✅ This proves it's hidden.
- From the public bastion, `ssh` into the private instance using its **private IP** (`10.0.2.x`). This is the **bastion host pattern** — the private box is reachable only by hopping through the bastion.

**Trade-off:** For the private subnet to reach the internet *outbound* (OS updates, pulling packages) without being reachable inbound, add a **NAT Gateway** in the public subnet and point the private route table's `0.0.0.0/0` at it. NAT Gateways cost ~$32/month + data — a classic "surprise bill" item, so tear it down after the lab.

---

### ✅ Exercise 5: Set Up ALB + Auto Scaling

**Goal:** Understand scalable architecture.

```bash
# 1. Bake an AMI from your configured Spring Boot instance
aws ec2 create-image --instance-id i-0123456789abcdef0 \
  --name "spring-boot-app-v1.0-2026-06-12" \
  --description "AL2023 + Java 17 + app v1.0"
# -> returns ami-xxxx

# 2. Create a Launch Template referencing that AMI
aws ec2 create-launch-template \
  --launch-template-name spring-lt \
  --version-description v1 \
  --launch-template-data '{
    "ImageId":"ami-xxxx",
    "InstanceType":"t2.micro",
    "KeyName":"my-key-pair",
    "SecurityGroupIds":["sg-xxxx"]
  }'

# 3. Target Group on port 8080 with a Spring Boot Actuator health check
TG=$(aws elbv2 create-target-group \
  --name spring-tg --protocol HTTP --port 8080 --vpc-id vpc-xxxx \
  --health-check-path /actuator/health \
  --query 'TargetGroups[0].TargetGroupArn' --output text)

# 4. Application Load Balancer across two public subnets (needs >=2 AZs)
ALB=$(aws elbv2 create-load-balancer --name spring-alb \
  --subnets subnet-pub1 subnet-pub2 --security-groups sg-alb \
  --query 'LoadBalancers[0].LoadBalancerArn' --output text)

# Listener: forward port 80 -> target group
aws elbv2 create-listener --load-balancer-arn $ALB \
  --protocol HTTP --port 80 \
  --default-actions Type=forward,TargetGroupArn=$TG

# 5. Auto Scaling Group: min 1, desired 2, max 4, wired to the LT + target group
aws autoscaling create-auto-scaling-group \
  --auto-scaling-group-name spring-asg \
  --launch-template LaunchTemplateName=spring-lt,Version='$Latest' \
  --min-size 1 --desired-capacity 2 --max-size 4 \
  --target-group-arns $TG \
  --vpc-zone-identifier "subnet-pub1,subnet-pub2"

# Target-tracking policy: keep average CPU ~50%
aws autoscaling put-scaling-policy \
  --auto-scaling-group-name spring-asg \
  --policy-name cpu50 --policy-type TargetTrackingScaling \
  --target-tracking-configuration '{
    "PredefinedMetricSpecification":{"PredefinedMetricType":"ASGAverageCPUUtilization"},
    "TargetValue":50.0
  }'
```

```bash
# 6. Get the ALB DNS name and hit it
aws elbv2 describe-load-balancers --names spring-alb \
  --query 'LoadBalancers[0].DNSName' --output text
# e.g. spring-alb-123456.ap-south-1.elb.amazonaws.com

# 7. Make repeated requests — note the instance hostname rotates between the two boxes
for i in {1..10}; do curl -s http://spring-alb-123456.ap-south-1.elb.amazonaws.com/hello; done
```

**The pattern to articulate:** ASG launches instances **from the AMI** → they auto-register with the **Target Group** → the **ALB** health-checks `/actuator/health` and only routes to healthy ones → CPU spikes trigger the scaling policy to add instances, and they deregister automatically when scaled down. This **ALB + ASG** combo is the textbook resilient, self-healing, auto-scaling web tier.

---

### 🎁 Bonus Exercise 6: Create an IAM User, Group, and Role (least privilege)

```bash
# Group with a managed policy, user added to the group (never attach to users directly)
aws iam create-group --group-name Developers
aws iam attach-group-policy --group-name Developers \
  --policy-arn arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess
aws iam create-user --user-name dev-priya
aws iam add-user-to-group --user-name dev-priya --group-name Developers

# A ROLE for EC2 to read S3 — no access keys ever stored on the instance
cat > trust.json <<'EOF'
{ "Version":"2012-10-17",
  "Statement":[{"Effect":"Allow",
    "Principal":{"Service":"ec2.amazonaws.com"},
    "Action":"sts:AssumeRole"}] }
EOF
aws iam create-role --role-name ec2-s3-read --assume-role-policy-document file://trust.json
aws iam attach-role-policy --role-name ec2-s3-read \
  --policy-arn arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess
# Then create an instance profile and attach it to the EC2 instance.
```

**Why roles beat keys:** The EC2 instance *assumes* the role and gets **temporary, auto-rotating credentials**. Nothing secret sits on disk to leak. This is the single most important IAM best practice for services.

---

### 🎁 Bonus Exercise 7: A Day-1 Billing Alarm (the mistake everyone makes)

```bash
# Alert when estimated charges exceed $5 (billing metrics live in us-east-1)
aws cloudwatch put-metric-alarm \
  --alarm-name billing-over-5usd \
  --namespace AWS/Billing --metric-name EstimatedCharges \
  --dimensions Name=Currency,Value=USD \
  --statistic Maximum --period 21600 \
  --threshold 5 --comparison-operator GreaterThanThreshold \
  --evaluation-periods 1 \
  --alarm-actions arn:aws:sns:us-east-1:111122223333:billing-alerts \
  --region us-east-1
```

**Why:** Free tier has limits (e.g., 750 EC2 hours/month). Leave a second instance running and you blow past it. This alarm emails you before a tiny mistake becomes a big bill.

---

### 🎁 Bonus Exercise 8: A Lambda Triggered by S3 Upload

**Use case:** Auto-generate a thumbnail whenever an image lands in a bucket.

```bash
# Package and create a Python Lambda
zip function.zip handler.py
aws lambda create-function \
  --function-name thumbnailer \
  --runtime python3.12 --handler handler.lambda_handler \
  --role arn:aws:iam::111122223333:role/lambda-s3-role \
  --zip-file fileb://function.zip --timeout 30 --memory-size 256

# Wire S3 "object created" events to invoke it (configure notification on the bucket)
aws lambda add-permission --function-name thumbnailer \
  --statement-id s3invoke --action lambda:InvokeFunction \
  --principal s3.amazonaws.com --source-arn arn:aws:s3:::my-uploads-bucket
```

**Trade-off:** Lambda is perfect here — runs ~2 seconds per upload, scales to 1000s of concurrent images, costs nothing when idle. But it caps at **15-minute** execution, so a long video-encode job belongs on EC2/ECS, not Lambda.

---

## PART B — Interview Questions & Model Answers

> 💬 Answers are written in **spoken style** — say them out loud the way you'd say them in a real interview. Confident, structured, lands the trade-off.

### Q1. What is cloud computing, and why would a company move to it?

"Cloud computing is **renting** computers, storage, and networking from a provider like AWS over the internet, instead of buying and running your own hardware. It solves four big pains: the **huge upfront cost** of buying servers, **wasted capacity** because owned servers sit idle most of the time, **slow scaling** because buying hardware takes weeks, and the **maintenance burden** of cooling, power, patching, and hiring sysadmins. The analogy I use is **owning a car versus using Ola** — with your own car you pay EMI, insurance, and parking even when it's idle; with Ola you pay per ride and someone else handles maintenance. Cloud is the Ola model for IT: pay only for what you use, scale up in seconds, scale down to zero."

---

### Q2. Explain IaaS vs PaaS vs SaaS.

"These are three levels of **how much you manage yourself**.

| Model | You manage | Provider manages | AWS example | Analogy |
|---|---|---|---|---|
| **IaaS** | OS, runtime, app, data | Hardware, power, network | EC2 | Empty flat — bring all your own furniture |
| **PaaS** | App code + data only | OS, runtime, scaling, LB | Elastic Beanstalk | Furnished flat — landlord fixes the fridge |
| **SaaS** | Just your data | Everything | (Gmail, not really yours to build) | Hotel room — just show up |

As a DevOps engineer I live mostly in **IaaS** — EC2, VPC, S3 — because I need that control to wire infrastructure exactly how the company needs, plus some PaaS like RDS where I happily let AWS run the database."

---

### Q3. What's the difference between a Region and an Availability Zone?

"A **Region** is a geographic area with AWS data centers — like `ap-south-1` which is Mumbai. An **Availability Zone** is one or more physically separate data centers *within* a region, with independent power and networking — Mumbai has `1a`, `1b`, `1c`. I pick a region close to my **users** to cut latency — Mumbai for Indian users saves 200–300 ms versus US. I spread my app across **multiple AZs** so that if one data center floods or loses power, the others keep serving. The analogy: keeping copies of important documents in three different bank branches — if one floods, the others are safe."

---

### Q4. IAM Users vs Roles — when do you use each?

"An IAM **User** is a permanent identity for a **person** — each developer gets one, with a password and optionally access keys. An IAM **Role** is a set of **temporary** permissions that any user, service, or application can *assume* — and it issues short-lived, auto-rotating credentials, with no stored secret.

Rule of thumb: **users for humans, roles for machines and temporary access.** The killer example: if my EC2 instance needs to read S3, I never put access keys on the box — I attach a **role**, and the instance assumes it to get temporary credentials. Nothing secret to leak. The analogy is a permanent employee badge (user) versus a visitor pass that expires in an hour (role)."

---

### Q5. Root user vs IAM user — how should you treat the root account?

"The **root user** is created when you sign up and has **unrestricted** power — it can even close the account, and you *can't* limit it. An **IAM user** has only the permissions you grant.

Best practice: log in as root **exactly once** — to create your first admin IAM user and set up billing — then **enable MFA on root and lock it away.** Do all daily work through IAM users. The analogy is the building owner's master key: you keep it in a safe and use your normal access card day to day."

---

### Q6. What is EC2, and how do you choose an instance type?

"EC2 — Elastic Compute Cloud — lets me rent virtual servers ('instances') by the hour, choosing CPU, RAM, OS, and region. I choose the **instance family** by workload:

| Family | Optimized for | Analogy |
|---|---|---|
| t2/t3 | General purpose, **burstable** | Maruti Alto — daily commute |
| m5/m6i | General purpose, steady | Innova — reliable all-rounder |
| c5/c6i | Compute / CPU-heavy | Sports car — raw power |
| r5/r6i | Memory / RAM-heavy | Bus — carries lots of data |
| g4/p4 | GPU (ML, graphics) | Tractor — specialized heavy lifting |

For a typical Spring Boot app I start with **t3.medium** (2 vCPU, 4 GB). 'Burstable' means t2/t3 build up **CPU credits** while idle and spend them to burst to 100% during spikes — like rollover data on a prepaid plan."

---

### Q7. Explain EC2 pricing models. Which would you pick?

"Four models, trading commitment for discount:

| Model | How it works | Discount | Best for |
|---|---|---|---|
| **On-Demand** | Pay per hour, no commitment | 0% | Learning, unpredictable load |
| **Reserved** | Commit 1 or 3 years | 40–60% | Steady base-load production |
| **Spot** | Bid on spare capacity, can be reclaimed with 2-min notice | 60–90% | Fault-tolerant batch jobs, ML training |
| **Savings Plans** | Commit to $/hour spend, flexible across types | 30–50% | Flexible long-term commitment |

My production strategy: **Reserved Instances for the steady base load + On-Demand or Spot for spikes.** For learning, On-Demand t2.micro under the free tier. The Spot caveat — only for work that can be **interrupted and restarted**, never your main web server."

---

### Q8. What is an AMI and why does it matter for DevOps?

"An AMI — Amazon Machine Image — is a complete snapshot of a server: OS, installed software, configs, everything. It's a **master template** for launching identical instances. It matters for three reasons: **Auto Scaling** launches new instances from an AMI, so if the AMI already has Java and my app baked in, the new box serves traffic in seconds; **consistency**, so every instance is identical — no 'works on my machine'; and **disaster recovery** — if a server dies I relaunch from the AMI in 30 seconds instead of an hour of manual setup. The analogy is a rubber stamp: make it once, stamp a thousand identical copies fast."

---

### Q9. What is S3, and what are storage classes?

"S3 — Simple Storage Service — is **object storage** with effectively unlimited capacity. I store files ('objects') in 'buckets', each object has a key (path), the data, and metadata. Bucket names are **globally unique** across all of AWS.

The key cost lever is **storage classes** — match the class to how often you access the data:

| Class | Access | Cost | Use case |
|---|---|---|---|
| Standard | Instant | Highest | Frequently accessed |
| Standard-IA | Instant | ~40% less | Accessed monthly |
| One Zone-IA | Instant | ~50% less | Non-critical, rare |
| Glacier Instant | Instant | ~60% less | Quarterly archive |
| Glacier Flexible | Hours | ~70% less | Yearly archive |
| Glacier Deep | Hours | ~90% less | Compliance, almost never |

The analogy: Standard is your **fridge** (daily items, costs the most), IA is the **storeroom** (seasonal), Glacier is the **warehouse** (old tax papers). I use **lifecycle policies** to auto-move data — e.g., logs to IA after 30 days, Glacier after 90, delete after a year — which cuts storage cost dramatically with zero manual effort."

---

### Q10. EBS vs S3 vs EFS — what's the difference?

"All three store data, but they're built for different jobs:

| | EBS | S3 | EFS |
|---|---|---|---|
| Type | **Block** storage (a virtual disk) | **Object** storage | **File** storage (shared) |
| Attached to | One EC2 instance (one AZ) | Accessed over HTTP API, anywhere | Many EC2 instances at once |
| Use case | OS disk, database volume | Files, backups, static sites, logs | Shared filesystem across a fleet |
| Analogy | The hard drive in your laptop | Infinite Dropbox | A shared network drive in the office |

So: **EBS** is the boot/database disk for a single instance, **S3** is for files and objects you access via API from anywhere, and **EFS** is when multiple instances need to read/write the *same* filesystem simultaneously."

---

### Q11. What is RDS, and what does "managed" actually mean?

"RDS — Relational Database Service — is a **managed** database. 'Managed' means AWS does the boring, error-prone DBA work: provisioning, **automated backups**, patching, hardware replacement, and failover. I just pick the engine — MySQL, PostgreSQL, MariaDB, Oracle, SQL Server, or Aurora — choose a size, and connect my app. The analogy is **cooking biryani yourself vs ordering at a restaurant**: self-managed MySQL on EC2 means I buy ingredients, cook, clean, and handle a gas outage at midnight; RDS means I just order and eat. The trade-off is I pay a premium and give up some low-level tuning control — worth it to stop being a part-time DBA."

---

### Q12. How do Multi-AZ and Read Replicas differ in RDS?

"They solve different problems and people mix them up.

- **Multi-AZ** is for **high availability**. RDS keeps a **synchronous standby** copy in another AZ; if the primary fails, it **automatically fails over** and the endpoint stays the same — my app barely notices. The standby does **not** serve traffic; it's purely a hot backup. Analogy: two phones with the same number — if one dies, calls ring on the other.
- **Read Replicas** are for **scaling reads**. They're **asynchronous** copies that *do* serve `SELECT` queries, offloading the primary. Up to 5 per instance. Analogy: extra cashiers handling read-only queries while the main one handles writes.

So Multi-AZ = survive failures; Read Replicas = handle read-heavy load. In production I often use both."

---

### Q13. What is a VPC? Walk me through public vs private subnets.

"A VPC — Virtual Private Cloud — is my own isolated network inside AWS where I control IP ranges, routing, and what traffic flows in and out. The analogy is a **gated community**: the VPC is the community, the **Internet Gateway** is the main gate, **subnets** are the internal roads, **security groups** are guards at each building.

- A **public subnet** has a route to the Internet Gateway, so instances there are reachable from the internet — I put **load balancers and web servers** here.
- A **private subnet** has no direct internet route — I put **databases and app servers** here so they're hidden.

A typical layout: users hit the **ALB** in a public subnet → it forwards to **EC2** in a private subnet → which talks to **RDS** in a private subnet. The database is never exposed to the internet. For private instances to reach *out* (updates) without being reachable *in*, I add a **NAT Gateway** in the public subnet."

---

### Q14. Security Groups vs NACLs — what's the difference?

"Both are firewalls but at different layers:

| | Security Group | NACL |
|---|---|---|
| Level | **Instance** (per server) | **Subnet** (per segment) |
| Rules | Allow only | Allow **and** Deny |
| State | **Stateful** — allow inbound, response is auto-allowed | **Stateless** — must allow both directions explicitly |
| Default | Deny all in, allow all out | Allow all |

In practice I use **Security Groups for almost everything** because they're stateful and simpler. NACLs are a coarse extra layer at the subnet level for **defense in depth** — for example, explicitly blocking a malicious IP range across a whole subnet. Analogy: SG is the guard at each building door; NACL is the rules board at the community gate."

---

### Q15. What is a Load Balancer? ALB vs NLB?

"A load balancer distributes incoming traffic across multiple servers so no single one gets overwhelmed — the analogy is a **bank token system** directing customers to the next free counter.

| | ALB | NLB |
|---|---|---|
| Layer | 7 (HTTP/HTTPS) | 4 (TCP/UDP) |
| Routing | Path/host-based, smart | Ultra-fast, connection-based |
| Use case | Web apps, REST APIs, microservices | Gaming, streaming, extreme throughput |

For Spring Boot REST APIs I **always use ALB** because it's Layer 7 and can route by path (e.g., `/api` to one target group, `/images` to another). The ALB also runs **health checks** — it pings `/actuator/health`, and if an instance stops responding it stops sending it traffic. I avoid the old Classic LB for new projects."

---

### Q16. How does Auto Scaling work, and how does it pair with a load balancer?

"An **Auto Scaling Group** automatically adds or removes EC2 instances based on demand, between a **min**, **desired**, and **max**. It uses a **Launch Template** (the blueprint: AMI, type, security group) and a **scaling policy** — like target-tracking 'keep average CPU at 50%', step scaling, or scheduled scaling for a known weekly sale.

Paired with an ALB it's the standard resilient web tier: the **ALB** receives traffic, the **ASG** scales the fleet, **new instances auto-register** with the ALB's target group and **deregister** when removed, and **health checks** ensure only healthy boxes get traffic. The analogy is Ola's driver fleet — 10,000 drivers at morning rush, 3,000 at 2 PM — you don't keep peak capacity running 24/7 and pay for idle servers. That elasticity is the whole point: you pay for what demand actually requires."

---

### Q17. What is Lambda, and when would you NOT use it?

"Lambda is **serverless** compute — I upload a function, AWS runs it on a trigger (S3 upload, API Gateway call, scheduled event, SQS message), and I **pay only while it runs**, nothing when idle. First 1 million requests/month are free. The analogy is a WhatsApp business **auto-reply**: set it once, it fires per message, costs nothing between messages, and if 100 messages arrive at once, 100 replies fire in parallel.

I'd **not** use Lambda for: long-running jobs (15-minute max timeout), anything that must run **continuously** (use EC2/ECS), or **very high, steady** traffic where per-request cost adds up beyond what a reserved EC2 instance would cost. Great for event-driven, bursty, short tasks like image thumbnailing or webhook handlers."

---

### Q18. What is CloudWatch, and what's the difference between SQS and SNS?

"**CloudWatch** is AWS's monitoring service — it collects **metrics** (CPU, memory, disk, request count), stores **logs**, and fires **alarms** when a threshold is crossed (e.g., CPU > 80% for 5 minutes → email ops). The analogy is a **car dashboard**: speed, fuel, engine temp, and a warning light. You don't drive without it; you don't run servers without it.

For messaging, **SQS vs SNS** is queue vs broadcast:

| | SQS | SNS |
|---|---|---|
| Pattern | **Queue** — one message, one consumer | **Pub/Sub** — one message, all subscribers |
| Analogy | IRCTC ticket queue, processed in order | A newspaper — every subscriber gets a copy |
| Use case | Decouple + buffer work | Fan-out a notification to many services |

So SQS **decouples** services and absorbs spikes (the consumer pulls when ready), while SNS **fans out** one event to many subscribers (email + SMS + analytics all at once). They're often combined: SNS publishes to multiple SQS queues."

---

### Q19. Explain the AWS Shared Responsibility Model.

"AWS splits security into two halves. **AWS is responsible for security *of* the cloud** — the physical data centers, hardware, networking, and the virtualization layer. **I'm responsible for security *in* the cloud** — my OS patches, my IAM users and policies, my security group rules, encrypting my data, and not opening port 22 to the world. The line shifts with the service: with EC2 (IaaS) I patch the OS; with RDS or Lambda, AWS patches more of the stack and I'm left with data, access, and config. The interview point: AWS securing the building doesn't mean I can leave my apartment door unlocked."

---

### Q20. How do you secure access to AWS resources? (best-practices roundup)

"My checklist:
1. **Enable MFA on root, lock root away**, do daily work as IAM users.
2. **Least privilege** — grant only the permissions needed; a deploy bot doesn't need billing.
3. **Use groups**, not per-user policies — attach to a `Developers` group and add users.
4. **Use roles for services** — EC2 reads S3 via a role, never stored access keys.
5. **Never hardcode secrets** — use environment variables or **Secrets Manager**.
6. **Restrict security groups** — SSH (22) to my IP only, databases reachable only from the app's security group, DBs in **private subnets**.
7. **Set a billing alarm** on day one.
8. **Tag everything** (Name, Environment, Project) for accountability.

The theme is **least privilege + no standing secrets + defense in depth.**"

---

## 🧠 Memory Hooks — Analogies to Lock It In

| Service | Analogy | Trigger phrase |
|---|---|---|
| **Cloud vs On-prem** | Ola vs owning a car | "Pay per ride, no parking fees" |
| **IaaS / PaaS / SaaS** | Empty flat / furnished flat / hotel room | "How much do I bring myself?" |
| **Region vs AZ** | Documents in 3 different bank branches | "Survive a flood in one branch" |
| **EC2** | A rented computer in the cloud | "Give me a PC in 30 seconds" |
| **t2/t3 burstable** | Rollover data on a prepaid plan | "Save credits, burst later" |
| **AMI** | A rubber stamp / master photocopy | "Stamp 1000 identical servers" |
| **S3** | An infinite warehouse / Dropbox | "Put a labelled box on a shelf" |
| **S3 storage classes** | Fridge → storeroom → warehouse | "Match cost to how often you reach for it" |
| **EBS vs S3 vs EFS** | Laptop drive / Dropbox / office shared drive | "Block, object, shared file" |
| **IAM** | Security guard with a rulebook | "Who can do what" |
| **IAM Role** | A visitor pass that expires in an hour | "Temporary, no stored key" |
| **Root user** | The building owner's master key in a safe | "Use once, then lock away" |
| **RDS** | Ordering biryani at a restaurant | "Let the chef handle the kitchen" |
| **Multi-AZ** | Two phones, same number | "Calls ring on the backup" |
| **VPC** | A gated community | "Your private office floor in AWS" |
| **Public vs Private subnet** | Clubhouse by the gate vs homes inside | "Web servers out front, DBs hidden" |
| **NAT Gateway** | A door you can exit but strangers can't enter | "Outbound only" |
| **Security Group vs NACL** | Guard at the door vs rules board at the gate | "Per-instance vs per-subnet" |
| **ALB** | Bank token system | "Next free counter, please" |
| **Auto Scaling** | Ola's driver fleet by time of day | "10k at rush, 3k at 2 PM" |
| **Route 53** | The internet's phone directory | "Name → IP" |
| **Lambda** | WhatsApp auto-reply | "Fires per message, idle = free" |
| **CloudWatch** | Your car's dashboard | "Speed, fuel, warning light" |
| **SQS** | IRCTC ticket queue | "One message, one reader, in order" |
| **SNS** | A newspaper to all subscribers | "One message, everyone gets a copy" |
| **Shared Responsibility** | AWS guards the building, you lock your door | "Security OF vs IN the cloud" |

---

## 🎯 Final Interview Tip

When you don't know an exact number or flag, **don't bluff — reason out loud with the trade-off lens.** Interviewers care far more about *judgment* than memorized syntax. If asked "how would you store user-uploaded invoices?", a strong answer walks the trade-off: "S3 for durability and unlimited scale, Standard class for recent invoices, a lifecycle rule to Glacier after a year for the compliance archive, versioning on so an accidental overwrite is recoverable, and a private bucket with access via IAM roles — never public." That single answer touches durability, **cost**, recovery, and **security** — the four things every AWS interviewer is silently checking for. End every answer by naming the trade-off you considered, and you'll sound like an engineer who's actually paid an AWS bill. 💪☁️
