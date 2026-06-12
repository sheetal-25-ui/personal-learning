# Week 5: AWS Cloud — Complete Guide

**Target Audience:** Java/Spring Boot developer learning DevOps
**Goal:** Understand cloud computing and the most important AWS services hands-on

---

## Part 1: What is Cloud Computing?

### What is it?

Cloud computing means renting computers, storage, and networking from a big company (like Amazon, Microsoft, or Google) instead of buying your own.

That is literally it. Instead of owning hardware, you rent it. You use it over the internet. You pay only for what you use.

### The World Before Cloud

Imagine you are starting a new e-commerce company in Bangalore. You need servers to run your website.

**Without cloud, here is what happens:**

1. **Buy servers** — 3 physical servers cost Rs. 15-20 lakhs each. Total: Rs. 50-60 lakhs.
2. **Set up a server room** — You need AC (24/7 cooling), UPS (power backup), fire suppression, physical security. Another Rs. 10-15 lakhs.
3. **Hire people** — You need at least 2-3 system administrators to maintain the servers. Rs. 50,000-1,00,000/month each.
4. **Wait** — From ordering servers to having them running takes 2-3 months.
5. **Diwali sale comes** — Your website gets 10x traffic. Your 3 servers cannot handle it. Website crashes. Customers go to competitors.
6. **You buy 7 more servers** — Another Rs. 1 crore. Takes 2 more months to set up.
7. **Diwali ends** — Traffic drops back to normal. 7 servers sit idle. You are still paying for electricity, cooling, and maintenance for all 10 servers.

**Total cost: Rs. 1.5-2 crores. Time: 5 months. Flexibility: zero.**

**With cloud (AWS), here is what happens:**

1. **Sign up for AWS** — Free. Takes 5 minutes.
2. **Launch servers** — Click a few buttons. Server is ready in 30 seconds. Cost: Rs. 500-2000/month per server.
3. **Diwali sale comes** — Click "Auto Scaling" to automatically add more servers when traffic increases.
4. **Diwali ends** — Auto Scaling removes the extra servers. You stop paying for them.
5. **No server room, no AC, no hiring system admins.**

**Total cost: Rs. 5,000-50,000/month (depends on usage). Time: 5 minutes. Flexibility: unlimited.**

### Real-Life Analogy: Own Car vs Ola

| Aspect | Own Car (Physical Servers) | Ola (Cloud Computing) |
|--------|---------------------------|----------------------|
| Upfront cost | Rs. 8-15 lakhs to buy | Rs. 0 |
| Monthly cost | EMI + insurance + fuel + maintenance = Rs. 15,000-25,000 even if you barely drive | Pay per ride. Don't ride = don't pay |
| Scaling up | Need a bigger car? Buy another one | Need to go to airport? Book an SUV. Going alone? Book a Mini |
| Scaling down | Car sits in parking 22 hours a day | Not travelling? Pay nothing |
| Maintenance | Your problem (puncture at 11 PM? Good luck) | Ola's problem |
| Setup time | Buy, register, insure = weeks | Open app, book, ride in 5 minutes |

Cloud computing is the Ola model for IT infrastructure.

### Why Does Cloud Computing Exist?

It solves these problems:

1. **High upfront cost** — Small companies cannot afford Rs. 50 lakhs for servers
2. **Wasted capacity** — Most servers run at 10-15% utilization. You pay for 100% but use 15%
3. **Scaling is slow** — Buying new servers takes weeks/months
4. **Maintenance burden** — Patching, hardware failures, cooling, power, security — all your headache
5. **Global reach** — Want to serve customers in US, Europe, and India? You would need data centers in all 3 regions. With cloud, click 3 buttons

---

## Part 2: Cloud Service Models

There are 3 models. Think of them as levels of "how much do you want to manage yourself?"

### IaaS — Infrastructure as a Service

**What is it?** They give you the raw machine (CPU, RAM, disk, network). You install everything else — OS, runtime, application.

**AWS Example:** EC2 (Elastic Compute Cloud)

**Real-life analogy:** Renting an empty flat. The builder gives you walls, floor, ceiling, plumbing, electricity. You bring your own furniture, curtains, kitchen appliances, everything. You maintain the flat yourself.

**You manage:** OS, patches, security, runtime, application, data
**They manage:** Physical hardware, power, cooling, network cables

**When to use:** When you need full control. Custom OS configurations, special software, specific network setups.

### PaaS — Platform as a Service

**What is it?** They give you the machine AND the runtime environment. You just deploy your code.

**AWS Example:** Elastic Beanstalk, AWS App Runner

**Non-AWS Example:** Heroku, Google App Engine

**Real-life analogy:** Renting a furnished flat. Everything is there — beds, sofa, fridge, washing machine. You just bring your clothes and toothbrush. If the washing machine breaks, the landlord fixes it.

**You manage:** Application code, data
**They manage:** OS, patches, runtime (Java, Node.js, Python), scaling, load balancing

**When to use:** When you want to focus on writing code and not worry about servers. Good for startups moving fast.

### SaaS — Software as a Service

**What is it?** Everything is done. You just use the software through a browser or app.

**Examples:** Gmail, Google Docs, Dropbox, Slack, Zoom

**Real-life analogy:** Staying in a hotel. Bed is made, room is cleaned, food is served. You just show up and enjoy. You cannot rearrange the furniture or bring your own mattress.

**You manage:** Your data (emails, documents)
**They manage:** Everything else

**When to use:** When you are a user, not a builder. Your company uses Gmail — you do not install email servers.

### Quick Comparison

| | IaaS | PaaS | SaaS |
|---|---|---|---|
| Control | Maximum | Medium | Minimum |
| Flexibility | Maximum | Medium | Low |
| Effort | Maximum | Medium | Minimum |
| Example | EC2 | Elastic Beanstalk | Gmail |
| Analogy | Empty flat | Furnished flat | Hotel room |
| Who uses it | DevOps/Infra teams | Developers | End users |

### Which One Will You Use as a DevOps Engineer?

Mostly **IaaS** (EC2, VPC, S3) and some **PaaS** (RDS, ECS). You need the control that IaaS gives to set up things exactly as your company needs.

---

## Part 3: AWS Overview

### Why AWS?

- **Market leader** — 33% of the cloud market. More than Azure (22%) and GCP (11%) combined.
- **Most jobs require it** — Go to Naukri or LinkedIn, search "DevOps." 8 out of 10 job descriptions mention AWS.
- **Most services** — 200+ services. Whatever you need, AWS probably has it.
- **Most documentation and community** — When you are stuck, you will find more answers for AWS than any other cloud.

### AWS vs Azure vs GCP — Quick Comparison

| Feature | AWS | Azure | GCP |
|---------|-----|-------|-----|
| Market share | 33% | 22% | 11% |
| Best for | Startups, most companies | Microsoft shops (.NET, Windows) | Data/ML, Google-heavy orgs |
| Compute | EC2 | Virtual Machines | Compute Engine |
| Kubernetes | EKS | AKS | GKE (best K8s) |
| Serverless | Lambda | Azure Functions | Cloud Functions |
| Database | RDS, DynamoDB | Azure SQL, Cosmos DB | Cloud SQL, Firestore |
| Jobs in India | Most | Second most | Growing |

**Note for career:** Learn AWS first. Once you know AWS, learning Azure or GCP takes 2-3 weeks — the concepts are the same, only the names and UI change.

### Regions and Availability Zones

**What is a Region?**
A region is a geographical area where AWS has data centers. Examples:
- **ap-south-1** = Mumbai, India
- **us-east-1** = N. Virginia, USA
- **eu-west-1** = Ireland

**Why does it matter?** If your users are in India, you want your servers in Mumbai (ap-south-1). A server in the US adds 200-300ms latency — your app feels slow.

**What is an Availability Zone (AZ)?**
Each region has 2 or more separate data centers called AZs. Mumbai (ap-south-1) has 3 AZs:
- ap-south-1a
- ap-south-1b
- ap-south-1c

These are physically separate buildings, with separate power supplies and network connections.

**Why multiple AZs?** If one data center has a power failure, fire, or flood, the others keep running. You deploy your application across multiple AZs so it survives any single data center failure.

**Real-life analogy:** You keep your important documents in 3 different bank lockers in 3 different branches. If one branch floods, your documents in the other 2 branches are safe.

### AWS Free Tier

AWS gives you limited free usage for 12 months after signup:

| Service | Free Tier Limit |
|---------|----------------|
| EC2 | 750 hours/month of t2.micro (1 instance running 24/7) |
| S3 | 5 GB storage |
| RDS | 750 hours/month of db.t2.micro |
| Lambda | 1 million requests/month |
| CloudWatch | 10 custom metrics, 10 alarms |

**Important:** Set up a billing alarm immediately after creating your account. Go to CloudWatch, create an alarm for "Estimated Charges > $5." AWS will email you if you accidentally leave something running.

---

## Part 4: AWS Services — Detailed Guide

### 1. IAM (Identity and Access Management) — Who Can Do What

#### What is it?

IAM controls who can access your AWS account and what they can do. It is the security guard of your AWS account.

#### Real-Life Analogy: Office Building Access

Think of a big IT company office in Bangalore:

- **Root user (Building Owner):** Has the master key. Can go everywhere, change anything, even demolish the building. You should NEVER use this key daily — keep it in a safe.
- **Admin user (CEO):** Has access to all floors, all rooms. Used for management tasks.
- **Developer user (Software Engineer):** Can enter their team's floor, use the meeting rooms, but cannot enter the server room or HR's office.
- **Intern user (ReadOnly):** Can look around (read data) but cannot touch anything (no write access).
- **Temporary role (Delivery Person):** Gets a visitor pass that works for 1 hour. After that, the pass stops working. This is like an IAM Role — temporary access.

#### Key IAM Concepts

**Users:** Individual people who need access. Each developer on your team gets their own IAM user.

**Groups:** A collection of users with the same permissions. Instead of giving permissions to each developer individually, create a "Developers" group, give permissions to the group, then add developers to the group.

**Policies:** JSON documents that define what actions are allowed or denied. Example policy — "Allow read access to S3 buckets":

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:ListBucket"
            ],
            "Resource": "*"
        }
    ]
}
```

**Roles:** Temporary permissions that can be assumed by users, services, or applications. Your EC2 instance can "assume" a role that lets it read from S3 — without storing any password on the instance.

#### Root User vs IAM User

| | Root User | IAM User |
|---|---|---|
| Created when | You sign up for AWS | You create it in IAM |
| Email | Your registration email | Can be any username |
| Permissions | EVERYTHING (cannot be restricted) | Only what you grant |
| Should you use daily? | NEVER | Yes |
| MFA required? | Absolutely yes | Strongly recommended |

**Rule:** Log into root ONLY to create your first IAM admin user and set up billing. Then lock root away.

#### Best Practices

1. **Enable MFA on root immediately.** MFA = Multi-Factor Authentication. Even if someone steals your password, they cannot log in without your phone.
2. **Never use root for daily work.** Create an IAM user with admin permissions instead.
3. **Least privilege.** Give users only the permissions they absolutely need. A developer who only deploys to EC2 does not need access to billing.
4. **Use groups.** Do not attach policies to individual users — attach them to groups and add users to groups.
5. **Use roles for services.** Your EC2 instance needs to read S3? Give it an IAM role, not access keys.

#### Hands-On: Creating an IAM User

```bash
# Step 1: Go to AWS Console → IAM → Users → Add Users
# Step 2: Enter username (e.g., "sheetal-dev")
# Step 3: Select "Provide user access to the AWS Management Console"
# Step 4: Set a password
# Step 5: Click "Attach policies directly" → Search "AdministratorAccess"
# Step 6: Click "Create user"
# Step 7: Save the login URL, username, and password

# Step 8: Enable MFA
# Go to IAM → Users → sheetal-dev → Security credentials → MFA → Assign MFA device
# Use an authenticator app like Google Authenticator
```

---

### 2. EC2 (Elastic Compute Cloud) — Virtual Servers

#### What is it?

EC2 lets you rent virtual computers (called "instances") in Amazon's data centers. You choose how powerful they are, what OS they run, and you pay by the hour.

Think of it as: "I want a computer with 4 CPUs, 16 GB RAM, running Ubuntu, in Mumbai. Give me one in 30 seconds."

#### Instance Types Explained

AWS has dozens of instance types. Here are the ones you will actually use:

| Type | Optimized For | Example | Real-Life Analogy |
|------|--------------|---------|-------------------|
| t2, t3 | General purpose (burstable) | t2.micro, t3.medium | Maruti Alto — good for daily commute |
| m5, m6i | General purpose (steady) | m5.large, m6i.xlarge | Toyota Innova — reliable all-rounder |
| c5, c6i | Compute (CPU heavy) | c5.xlarge | Sports car — raw engine power |
| r5, r6i | Memory (RAM heavy) | r5.large | Bus — carries lots of passengers (data) |
| g4, p4 | GPU (ML, graphics) | g4dn.xlarge | Tractor — specialized heavy lifting |

**For your Spring Boot applications:** Start with t3.medium (2 CPUs, 4 GB RAM). It handles most Spring Boot apps comfortably. For production with more traffic, move to m5.large.

**What does "burstable" mean?** t2/t3 instances have a baseline CPU performance (say 20%). When your app is mostly idle, it accumulates "CPU credits." When traffic spikes, it uses those credits to burst up to 100% CPU. Once credits run out, it drops back to baseline.

Real-life analogy: Your prepaid phone plan gives you 1.5 GB/day. If you do not use data for 3 days, the unused data rolls over. One day you download a big file — you use the saved data. If you run out of saved data, speed drops.

#### Launching an EC2 Instance — Step by Step

**Step 1: Choose an AMI (Amazon Machine Image)**

An AMI is the "starting template" for your server. It includes the operating system and sometimes pre-installed software.

Common choices:
- **Amazon Linux 2023** — AWS's own Linux. Lightweight, well-supported. Good default choice.
- **Ubuntu 22.04 LTS** — Most popular Linux. Tons of documentation.
- **Windows Server 2022** — If you need Windows.

For a Spring Boot developer: Choose **Amazon Linux 2023** or **Ubuntu 22.04**.

**Step 2: Choose Instance Type**

Select t2.micro for learning (free tier) or t3.medium for real work.

**Step 3: Configure Key Pair**

A key pair is an SSH key that lets you log into your instance without a password.
- Create a new key pair
- Download the .pem file
- KEEP THIS SAFE. If you lose it, you cannot log into your instance.

```bash
# On your local machine, set permissions on the key file
chmod 400 my-key-pair.pem
```

**Step 4: Network Settings (Security Group)**

A Security Group is a firewall. It controls what traffic can reach your instance.

For a Spring Boot app, you typically need:
- Port 22 (SSH) — so you can log in
- Port 8080 (Spring Boot default) — so users can access your app
- Port 443 (HTTPS) — for secure web traffic

```
Type        | Protocol | Port Range | Source
SSH         | TCP      | 22         | My IP (NOT 0.0.0.0/0 in production!)
Custom TCP  | TCP      | 8080       | 0.0.0.0/0 (anywhere)
HTTPS       | TCP      | 443        | 0.0.0.0/0
```

**Important:** Never open SSH (port 22) to 0.0.0.0/0 (the entire internet) in production. Restrict it to your IP address or use AWS Systems Manager Session Manager instead.

**Step 5: Configure Storage**

Default is 8 GB. For a Spring Boot app with logs, increase to 20-30 GB.

Storage types:
- **gp3** (General Purpose SSD) — Good default. 3000 IOPS baseline. Cheapest SSD.
- **gp2** (Older General Purpose SSD) — Older generation but still common.
- **io1/io2** (Provisioned IOPS) — For databases that need guaranteed performance. Expensive.

**Step 6: Launch**

Click "Launch instance." In about 30 seconds, your server is running.

#### Connecting via SSH

```bash
# Find your instance's public IP in the EC2 console
# Then connect:
ssh -i my-key-pair.pem ec2-user@3.110.85.42

# For Ubuntu AMIs, the username is "ubuntu" instead of "ec2-user":
ssh -i my-key-pair.pem ubuntu@3.110.85.42
```

#### Installing Java and Running Spring Boot on EC2

```bash
# Connect to your EC2 instance first

# Install Java 17
sudo yum install java-17-amazon-corretto -y    # Amazon Linux
# OR
sudo apt update && sudo apt install openjdk-17-jdk -y  # Ubuntu

# Verify
java -version

# Upload your Spring Boot JAR (from your local machine):
scp -i my-key-pair.pem target/myapp.jar ec2-user@3.110.85.42:/home/ec2-user/

# Run it
java -jar myapp.jar

# Run in background (keeps running after you disconnect):
nohup java -jar myapp.jar > app.log 2>&1 &

# Check if it is running:
curl http://localhost:8080/actuator/health
```

#### Elastic IP — Permanent Public IP

By default, when you stop and start an EC2 instance, its public IP changes. This is a problem if you have DNS pointing to that IP.

An Elastic IP is a static (permanent) public IP that you associate with your instance.

```
EC2 Console → Elastic IPs → Allocate Elastic IP address → Associate with your instance
```

**Warning:** An Elastic IP is free while attached to a running instance. If you detach it or stop the instance, AWS charges you about $3.60/month. This is to prevent people from hoarding IPs.

#### EC2 Pricing Models

| Model | How it Works | Analogy | Discount |
|-------|-------------|---------|----------|
| On-Demand | Pay by the hour. No commitment. | Ola ride — pay per km | None (full price) |
| Reserved | Commit to 1 or 3 years. | Monthly Ola pass — committed but cheaper | 40-60% off |
| Spot | Bid for unused capacity. AWS can take it back with 2 min notice. | Ola Share — cheapest but might get cancelled | 60-90% off |
| Savings Plans | Commit to $/hour spend for 1-3 years. Flexible across instance types. | Annual gym membership — use any equipment | 30-50% off |

**For learning:** On-Demand with t2.micro (free tier).
**For production:** Reserved Instances for base load + On-Demand or Spot for spikes.
**For batch jobs (data processing, ML training):** Spot Instances — if the job gets interrupted, just restart it.

---

### 3. AMI (Amazon Machine Image) — Server Snapshot/Template

#### What is it?

An AMI is a complete snapshot of a server — the operating system, all installed software, all configurations, everything. You can use an AMI to launch new servers that are exact copies.

#### Real-Life Analogy

Think of a master photocopy. You spend 2 hours setting up a server perfectly — installing Java, configuring firewalls, setting environment variables, tuning the OS. Instead of doing this for every new server, you take an AMI (the master copy). Now you can create 100 identical servers from that AMI in minutes.

Like a rubber stamp. Make the stamp once (effort), then stamp 1000 letters (fast and identical).

#### Why is AMI Important for DevOps?

1. **Auto Scaling needs AMIs.** When Auto Scaling launches a new instance during a traffic spike, it creates it from an AMI. If the AMI already has Java, your app, and all config, the instance is ready to serve traffic immediately.

2. **Consistency.** Every server launched from the same AMI is identical. No "works on my machine" problems.

3. **Disaster recovery.** Your production server dies? Launch a new one from the AMI. Takes 30 seconds instead of 2 hours of manual setup.

#### Creating Your Own AMI

```bash
# Step 1: Set up an EC2 instance exactly how you want
# - Install Java, Docker, your app, monitoring agents, etc.
# - Configure everything

# Step 2: In EC2 Console
# Select instance → Actions → Image and templates → Create image
# Give it a name: "spring-boot-app-v1.0-2026-06-11"
# Add a description: "Ubuntu 22.04 + Java 17 + Docker + Spring Boot app v1.0"
# Click Create image

# Step 3: The AMI appears in EC2 → AMIs
# You can now launch new instances from this AMI
```

**Naming convention tip:** Include the date and version in your AMI name. You will create many AMIs over time, and you need to know which one is the latest.

#### AWS Marketplace AMIs

AWS Marketplace has pre-configured AMIs made by third parties:
- Jenkins server — ready to go
- WordPress — just add your content
- GitLab — self-hosted Git server
- Bitnami stacks — pre-configured application stacks

These save time for common setups but cost more (vendor adds a per-hour fee on top of EC2 cost).

---

### 4. S3 (Simple Storage Service) — File Storage

#### What is it?

S3 is unlimited file storage in the cloud. You can store anything — images, videos, backups, logs, ZIP files, static websites. There is no limit on how much you can store.

#### Real-Life Analogy

Think of a warehouse with infinite shelves. You bring a box (file), put a label on it (key/filename), and put it on a shelf (bucket). You can come back anytime and get your box using the label.

#### Key Concepts

**Bucket:** A container for your files. Like a top-level folder. Each bucket has a globally unique name.

```
my-company-app-backups        (bucket)
  ├── 2026/01/backup.zip      (object)
  ├── 2026/02/backup.zip      (object)
  └── images/logo.png         (object)
```

**Object:** A file stored in a bucket. Each object has:
- Key (filename/path)
- Value (the file content)
- Metadata (content type, creation date, custom tags)

**Bucket names are globally unique.** You cannot create a bucket called "test" because someone else already has it. Use a name like "sheetal-learning-devops-2026."

#### Storage Classes

Not all data needs the same access speed. S3 offers different storage classes at different prices:

| Storage Class | Access Speed | Cost | Use Case | Analogy |
|--------------|-------------|------|----------|---------|
| Standard | Instant | Highest | Frequently accessed files | Fridge — quick access, costs electricity |
| Standard-IA | Instant | 40% less | Accessed monthly | Storeroom — go once a month, cheaper rent |
| One Zone-IA | Instant | 50% less | Non-critical data, accessed rarely | Cheap storeroom in one location |
| Glacier Instant | Instant | 60% less | Archive, accessed quarterly | Filing cabinet in office — quick but archived |
| Glacier Flexible | Hours (3-12h) | 70% less | Archive, accessed yearly | Warehouse in another city — takes time to get |
| Glacier Deep | Hours (12-48h) | 90% less | Compliance archives, almost never accessed | Bank locker — rarely opened, extremely cheap |

**Real-life analogy for the full picture:**
- Standard = Fridge. Daily use items (milk, vegetables). Quick access. Costs the most electricity.
- IA = Storeroom. Seasonal items (winter clothes, festival decorations). Access a few times a year.
- Glacier = Warehouse. Old furniture, tax papers from 5 years ago. Rarely needed but legally required to keep.

#### Hosting a Static Website on S3

You can host a simple HTML/CSS/JS website directly from S3. No server needed.

```bash
# Step 1: Create a bucket with a unique name
aws s3 mb s3://sheetal-devops-portfolio

# Step 2: Upload your website files
aws s3 cp index.html s3://sheetal-devops-portfolio/
aws s3 cp style.css s3://sheetal-devops-portfolio/

# Step 3: Enable static website hosting
# S3 Console → Bucket → Properties → Static website hosting → Enable
# Index document: index.html

# Step 4: Set bucket policy to allow public reads
# Bucket → Permissions → Bucket Policy:
```

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::sheetal-devops-portfolio/*"
        }
    ]
}
```

Your website is now live at: `http://sheetal-devops-portfolio.s3-website.ap-south-1.amazonaws.com`

#### S3 Versioning

Enable versioning to keep every previous version of a file. If someone accidentally overwrites or deletes a file, you can recover the old version.

```bash
# Enable versioning on a bucket
aws s3api put-bucket-versioning \
  --bucket sheetal-devops-portfolio \
  --versioning-configuration Status=Enabled
```

#### S3 Lifecycle Policies

Automatically move data to cheaper storage as it ages:

```
Rule: "Archive old logs"
- After 30 days → Move to Standard-IA
- After 90 days → Move to Glacier Flexible
- After 365 days → Delete
```

This saves money automatically. You do not have to manually manage files.

---

### 5. RDS (Relational Database Service) — Managed Database

#### What is it?

RDS is a managed database service. "Managed" means AWS handles all the boring database admin work — backups, patches, failover, hardware maintenance. You just connect and run queries.

#### Real-Life Analogy

Imagine you love biryani. You have two choices:
- **Self-managed (EC2 + MySQL):** Buy rice, spices, meat. Cook yourself. Clean the kitchen. Store leftovers properly. If the gas goes out, figure it out yourself.
- **Managed (RDS):** Go to a restaurant. Order biryani. Eat it. They handle cooking, cleaning, gas, storage. If one chef is sick, another chef takes over without you knowing.

RDS is the restaurant option for databases.

#### What "Managed" Actually Means

When you run MySQL on an EC2 instance yourself, YOU must:
- Install MySQL
- Configure it for performance
- Set up automated backups (and test that they actually work)
- Apply security patches
- Handle hardware failures (disk dies? You are on your own)
- Set up replication for high availability

With RDS, AWS does ALL of this automatically. You just:
- Choose the database engine (MySQL, PostgreSQL, etc.)
- Choose the instance size
- Connect your Spring Boot app

#### Supported Database Engines

| Engine | Notes |
|--------|-------|
| MySQL | Most popular open-source DB. Spring Boot works great with it. |
| PostgreSQL | Advanced open-source DB. Better for complex queries. |
| MariaDB | MySQL fork. Compatible with MySQL. |
| Oracle | Enterprise. Expensive licensing. |
| SQL Server | Microsoft's DB. For .NET shops. |
| Aurora | AWS's own engine. MySQL/PostgreSQL compatible but 5x faster. Costs more. |

**For learning:** Use MySQL or PostgreSQL (free tier eligible).
**For production at a startup:** Aurora (if budget allows) or PostgreSQL.

#### Multi-AZ Deployment — Automatic Failover

When you enable Multi-AZ, RDS creates a standby copy of your database in a different Availability Zone. If the primary database fails (hardware issue, AZ outage), RDS automatically switches to the standby. Your app does not even notice — the endpoint stays the same.

```
                   ┌──────────────────┐
                   │   Your App       │
                   │ (Spring Boot)    │
                   └────────┬─────────┘
                            │ connects to RDS endpoint
                   ┌────────▼─────────┐
                   │  RDS Endpoint    │
                   │ (stays the same) │
                   └───┬─────────┬────┘
                       │         │
              ┌────────▼──┐  ┌──▼────────┐
              │ Primary   │  │ Standby   │
              │ (AZ-1a)   │  │ (AZ-1b)   │
              │ Reads +   │  │ Sync copy │
              │ Writes    │  │ (auto     │
              └───────────┘  │ failover) │
                             └───────────┘
```

**Real-life analogy:** Like having two mobile phones with the same number. If your primary phone's battery dies, calls automatically go to the second phone. The caller does not know or care which phone rings.

#### Read Replicas — Scale Reads

If your application does lots of reads (e.g., product catalog, search results), you can create read replicas. These are copies of your database that handle SELECT queries, reducing the load on the primary.

```
Your app → Write queries → Primary DB
Your app → Read queries  → Read Replica 1, Read Replica 2, Read Replica 3
```

You can have up to 5 read replicas per RDS instance.

#### Connecting Spring Boot to RDS

```properties
# application.properties
spring.datasource.url=jdbc:mysql://my-db-instance.abcdefg12345.ap-south-1.rds.amazonaws.com:3306/mydb
spring.datasource.username=admin
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
```

**Important:** Never hardcode the password. Use environment variables or AWS Secrets Manager.

---

### 6. VPC (Virtual Private Cloud) — Your Private Network

#### What is it?

A VPC is your own private, isolated section of the AWS cloud. Think of it as your own private network where you control everything — which servers can talk to each other, which ones can access the internet, and what traffic is allowed in or out.

#### Real-Life Analogy: Gated Community

Think of a gated community (like Prestige Lakeside Habitat or Sobha Dream Acres in Bangalore):

- **The community** = Your VPC. You have full control over what happens inside.
- **Main gate** = Internet Gateway. This is how residents (your servers) access the outside world, and how visitors (users) come in.
- **Roads inside** = Subnets. Different lanes for different areas.
- **Clubhouse area (near main gate)** = Public Subnet. Accessible from outside. Your web servers go here.
- **Individual homes (inside, away from gate)** = Private Subnet. Not directly accessible from outside. Your database servers go here.
- **Security guard at each building** = Security Groups. Controls who can enter each building.
- **Community rules board** = Network ACLs (NACLs). Community-wide rules — "no entry after 10 PM" applies to the whole community.

#### Key VPC Components

**Subnets: Public vs Private**

A subnet is a range of IP addresses within your VPC.

- **Public subnet:** Has a route to the Internet Gateway. Instances here can be accessed from the internet. Put your web servers and load balancers here.
- **Private subnet:** No direct route to the internet. Instances here are hidden from the outside world. Put your databases and application servers here.

```
VPC (10.0.0.0/16) — You get 65,536 IP addresses
├── Public Subnet 1 (10.0.1.0/24) — 256 IPs — AZ 1a
├── Public Subnet 2 (10.0.2.0/24) — 256 IPs — AZ 1b
├── Private Subnet 1 (10.0.3.0/24) — 256 IPs — AZ 1a
└── Private Subnet 2 (10.0.4.0/24) — 256 IPs — AZ 1b
```

**Internet Gateway (IGW):** Connects your VPC to the internet. Without it, nothing in your VPC can reach the internet.

**NAT Gateway:** Lets instances in the private subnet access the internet (to download updates, patches) WITHOUT being accessible from the internet. Traffic goes out, but nothing can come in uninvited.

Real-life analogy: Your home (private subnet) has a door (NAT Gateway). You can go outside (access internet). But random people on the street cannot walk into your house.

**Route Tables:** Rules that tell traffic where to go.

Public subnet route table:
```
Destination     | Target
10.0.0.0/16     | local (stay inside VPC)
0.0.0.0/0       | igw-xxx (go to Internet Gateway)
```

Private subnet route table:
```
Destination     | Target
10.0.0.0/16     | local (stay inside VPC)
0.0.0.0/0       | nat-xxx (go to NAT Gateway)
```

#### Security Groups vs NACLs

| Feature | Security Group | NACL |
|---------|---------------|------|
| Level | Instance (per server) | Subnet (per network segment) |
| Rules | Allow only | Allow AND Deny |
| State | Stateful — if you allow inbound, outbound response is automatic | Stateless — you must explicitly allow both |
| Default | Deny all inbound, allow all outbound | Allow all |
| Analogy | Security guard at building door | Rules board at community gate |

**In practice:** Use Security Groups for most things. NACLs are an extra layer for defense in depth.

#### Typical VPC Setup for a Spring Boot App

```
Internet
    │
    ▼
Internet Gateway
    │
    ▼
Public Subnet ──── ALB (Load Balancer)
    │
    ▼
Private Subnet ──── EC2 (Spring Boot App)
    │
    ▼
Private Subnet ──── RDS (MySQL Database)
```

- Users hit the ALB (in the public subnet)
- ALB forwards traffic to EC2 (in the private subnet)
- EC2 talks to RDS (also in the private subnet)
- The database is never exposed to the internet

---

### 7. ELB (Elastic Load Balancer) — Distribute Traffic

#### What is it?

A load balancer distributes incoming traffic across multiple servers. If you have 3 servers running your Spring Boot app, the load balancer sends each request to a different server, so no single server gets overwhelmed.

#### Real-Life Analogy

Think of a bank with 5 counters. Without a token system (load balancer), everyone crowds counter 1. Counter 1 person is stressed, counters 2-5 are free. With a token system, each customer is directed to the next available counter. Everyone is served equally.

#### Types of Load Balancers

| Type | Best For | Layer | Use Case |
|------|---------|-------|----------|
| ALB (Application LB) | HTTP/HTTPS traffic | Layer 7 | Web apps, REST APIs, microservices |
| NLB (Network LB) | TCP/UDP traffic | Layer 4 | Gaming servers, real-time streaming, extreme performance |
| CLB (Classic LB) | Legacy | Both | Do not use for new projects |

**For your Spring Boot REST APIs:** Always use ALB.

#### How ALB Works

```
User Request → ALB → Target Group → EC2 Instance
                                  → EC2 Instance
                                  → EC2 Instance
```

- **Target Group:** A group of EC2 instances that receive traffic. The ALB sends requests to instances in the target group.
- **Health Checks:** The ALB pings each instance every 30 seconds (e.g., GET /actuator/health). If an instance stops responding, the ALB stops sending traffic to it.
- **Sticky Sessions:** If you want a user to always go to the same server (because of session data), enable sticky sessions. But better practice: make your app stateless and store sessions in Redis.

---

### 8. Auto Scaling Group — Automatic Scaling

#### What is it?

Auto Scaling automatically increases or decreases the number of EC2 instances based on demand. When traffic goes up, it launches more servers. When traffic goes down, it terminates the extras.

#### Real-Life Analogy

Think of an Ola/Uber driver fleet. During morning office rush (8-10 AM), Ola has 10,000 drivers active. At 2 PM (quiet time), only 3,000 drivers are needed. At 6 PM (evening rush), back to 10,000. Ola does not keep 10,000 drivers active 24/7 — that would be a waste of money.

Auto Scaling does the same for your servers.

#### How It Works

1. **Launch Template:** Defines what kind of instance to create (AMI, instance type, security groups, key pair). It is the blueprint.

2. **Auto Scaling Group (ASG):** Manages the fleet.
   - Minimum: 2 instances (never go below this, even at 3 AM)
   - Desired: 3 instances (the normal state)
   - Maximum: 10 instances (never go above this, even during Diwali sale)

3. **Scaling Policy:** Rules for when to scale.
   - Target tracking: "Keep average CPU at 50%"
   - Step scaling: "If CPU > 70%, add 2 instances. If CPU > 90%, add 4 instances."
   - Scheduled: "Every Friday at 6 PM, set desired to 8 instances" (weekly sale)

```
Normal Day:    [Server 1] [Server 2] [Server 3]
                                                    CPU: 40%
Diwali Sale:   [Server 1] [Server 2] [Server 3] [Server 4] [Server 5] [Server 6]
                                                    CPU: 55%
After Sale:    [Server 1] [Server 2] [Server 3]
                                                    CPU: 35%
```

#### ASG + ALB Together

This is the bread and butter of scalable architecture:

1. ALB receives all incoming traffic
2. ALB distributes traffic to instances in the ASG
3. When ASG adds new instances, they automatically register with the ALB
4. When ASG removes instances, they automatically deregister from the ALB
5. ALB health checks ensure only healthy instances get traffic

---

### 9. Route 53 — DNS Service

#### What is it?

Route 53 is AWS's DNS (Domain Name System) service. DNS converts human-readable domain names (like www.example.com) into IP addresses (like 3.110.85.42) that computers understand.

#### Real-Life Analogy

DNS is the phone directory of the internet. You do not call your friend by dialing "192.168.1.45" — you search for their name "Rahul" in your contacts. Your phone looks up the number and dials it. Similarly, when you type "www.flipkart.com," DNS looks up the IP address and connects you.

#### Key Features

- **Register domains:** Buy domain names like sheetal-devops.com
- **DNS records:** Map names to IPs
  - A record: sheetal-devops.com → 3.110.85.42
  - CNAME: www.sheetal-devops.com → sheetal-devops.com
  - Alias: sheetal-devops.com → my-alb-1234.ap-south-1.elb.amazonaws.com

#### Routing Policies

| Policy | How it Works | Use Case |
|--------|-------------|----------|
| Simple | One domain → One IP | Small websites |
| Weighted | 70% traffic to Server A, 30% to Server B | Canary deployments |
| Latency | Route to the region closest to the user | Global apps |
| Failover | Primary → Backup if primary is unhealthy | Disaster recovery |
| Geolocation | Users in India → Mumbai servers, US users → Virginia | Compliance, localization |

---

### 10. Lambda — Serverless Computing

#### What is it?

Lambda lets you run code without managing any server. You write a function, upload it to Lambda, and AWS runs it whenever something triggers it. You pay only when the function actually runs — not while it sits idle.

#### Real-Life Analogy

Think of an auto-reply on WhatsApp (like a business account). You set up the message once: "Thanks for reaching out! We will respond within 1 hour."

- You do not keep your phone on 24/7 waiting for messages
- When a message arrives, the auto-reply fires automatically
- Between messages, nothing is running (and you pay nothing)
- If 100 messages arrive at once, 100 auto-replies fire simultaneously

Lambda works the same way.

#### When to Use Lambda

| Use Case | Why Lambda? |
|----------|-------------|
| Process image uploads (resize, thumbnail) | Triggered by S3 upload. Runs for 2 seconds, done. |
| Webhook handler (Stripe, Razorpay callbacks) | Triggered by API Gateway. Runs for 500ms. |
| Scheduled tasks (daily report, cleanup) | Triggered by CloudWatch Events. Runs at midnight. |
| Process SQS messages | Triggered by queue message. Runs per message. |

#### When NOT to Use Lambda

- Long-running processes (Lambda max timeout: 15 minutes)
- Applications that need to run continuously (use EC2 or ECS)
- Applications with very high and consistent traffic (Lambda per-request cost adds up)

#### Lambda Pricing

- First 1 million requests per month: FREE
- After that: $0.20 per million requests
- Plus compute time: $0.0000166667 per GB-second

For most small applications, Lambda is effectively free.

---

### 11. CloudWatch — Monitoring and Logging

#### What is it?

CloudWatch is AWS's monitoring service. It collects metrics (CPU usage, memory, disk), stores logs, and sends alerts when something goes wrong.

#### Real-Life Analogy

Think of the dashboard in your car. It shows:
- Speed (like CPU usage)
- Fuel level (like disk space)
- Engine temperature (like memory usage)
- Warning light comes on if something is wrong (like a CloudWatch alarm)

You do not drive without looking at the dashboard. You should not run servers without CloudWatch.

#### Key Features

**Metrics:** Numbers that CloudWatch collects automatically:
- EC2: CPU utilization, network in/out, disk reads/writes
- RDS: Database connections, read/write latency, free storage
- ALB: Request count, response time, error rate

**Alarms:** "Alert me when something crosses a threshold"
```
Alarm: "High CPU"
Metric: EC2 CPU Utilization
Threshold: > 80% for 5 minutes
Action: Send email to ops-team@company.com
```

**Logs:** Application logs sent from your Spring Boot app.
```bash
# Install CloudWatch Agent on EC2 to send application logs
# Your Spring Boot app writes to /var/log/myapp/app.log
# CloudWatch Agent reads this file and sends it to CloudWatch Logs
```

**Dashboards:** Visual dashboard with graphs showing all your metrics in one place.

---

### 12. SQS and SNS — Messaging Services

#### SQS (Simple Queue Service) — Message Queue

**What is it?** A queue where one service puts messages and another service reads them.

**Real-life analogy:** A ticket counter at IRCTC. When you book a ticket online, your request goes into a queue. The backend system processes requests one by one, in order. Even if 10,000 people book at the same time, no requests are lost — they wait in the queue.

**Use case:** Your Spring Boot app receives an order. Instead of processing the payment, sending the email, updating inventory, and generating the invoice all at once (which takes 10 seconds), you put a message in the queue for each task. Separate services pick up and process each message independently.

```
Order API → SQS Queue → Payment Service picks up message → processes payment
                      → Email Service picks up message → sends confirmation
                      → Inventory Service picks up message → updates stock
```

#### SNS (Simple Notification Service) — Pub/Sub

**What is it?** A notification system. One publisher sends a message, and ALL subscribers receive it.

**Real-life analogy:** A newspaper. The newspaper publisher prints one edition, and all 50,000 subscribers get a copy. Unlike a queue (one message, one reader), a notification goes to everyone.

**Use case:** When an order is placed, publish to an SNS topic. The email service, SMS service, analytics service, and push notification service all subscribe to that topic and all receive the message.

---

## Part 5: Common Mistakes

1. **Using root account for daily work.** Create IAM users. Enable MFA on root. Lock it away.

2. **Opening port 22 (SSH) to 0.0.0.0/0.** Bots scan the entire internet for open SSH ports. Restrict to your IP or use Session Manager.

3. **Not setting up billing alerts.** You will forget to turn off an instance. Set a billing alarm at $5 or $10 on Day 1.

4. **Choosing the wrong region.** If your users are in India, use ap-south-1 (Mumbai). Launching in us-east-1 adds 200-300ms latency to every request.

5. **Hardcoding database passwords in application.properties.** Use environment variables or AWS Secrets Manager.

6. **Using public subnets for databases.** Your database should ALWAYS be in a private subnet. Only the load balancer should be public.

7. **Not enabling Multi-AZ for production RDS.** If the single-AZ database fails, your app is completely down. Multi-AZ gives automatic failover.

8. **Ignoring the Free Tier limits.** t2.micro is free but only for 750 hours/month. If you launch 2 t2.micro instances, that is 1500 hours — you will be charged for the extra 750 hours.

9. **Not tagging resources.** After a month, you will have 20 instances and no idea which project each belongs to. Tag everything with Name, Environment, and Project.

10. **Creating resources manually instead of learning Infrastructure as Code.** Manual creation does not scale. You will learn Terraform in Week 7.

---

## Part 6: Practice Exercises

### Exercise 1: Launch EC2 and Run Spring Boot

**Goal:** Get a Spring Boot app running on AWS

1. Sign up for AWS (free tier)
2. Create an IAM admin user (not root)
3. Enable MFA on root and IAM user
4. Launch a t2.micro EC2 instance with Amazon Linux 2023
5. SSH into the instance
6. Install Java 17
7. Create a simple Spring Boot app with a /hello endpoint
8. Build the JAR locally and SCP it to EC2
9. Run the JAR on EC2
10. Access it from your browser at http://<EC2-PUBLIC-IP>:8080/hello

### Exercise 2: Host a Static Website on S3

**Goal:** Understand S3 basics

1. Create an S3 bucket (globally unique name)
2. Create a simple HTML page (your DevOps portfolio or resume)
3. Upload it to S3
4. Enable static website hosting
5. Set the bucket policy for public read access
6. Access your website via the S3 website URL

### Exercise 3: Set Up RDS MySQL and Connect from Spring Boot

**Goal:** Understand managed databases

1. Create a free tier RDS MySQL instance (db.t2.micro or db.t3.micro)
2. Put it in the default VPC
3. Create a database called "myappdb"
4. Update your Spring Boot application.properties with the RDS endpoint
5. Run your app (locally or on EC2) and verify it connects to RDS
6. Add a simple entity (e.g., User) and verify data is saved in RDS

### Exercise 4: Create a VPC with Public and Private Subnets

**Goal:** Understand networking

1. Create a new VPC (10.0.0.0/16)
2. Create a public subnet (10.0.1.0/24) and a private subnet (10.0.2.0/24)
3. Create and attach an Internet Gateway
4. Create route tables:
   - Public route table: 0.0.0.0/0 → Internet Gateway
   - Private route table: default (local only)
5. Launch an EC2 in the public subnet (web server) — verify you can SSH in
6. Launch an EC2 in the private subnet (app server) — verify you CANNOT SSH from the internet
7. From the public instance, SSH to the private instance (bastion host pattern)

### Exercise 5: Set Up ALB + Auto Scaling

**Goal:** Understand scalable architecture

1. Create an AMI from your Spring Boot EC2 instance
2. Create a Launch Template using that AMI
3. Create a Target Group (port 8080, health check: /actuator/health)
4. Create an ALB and associate the Target Group
5. Create an Auto Scaling Group:
   - Min: 1, Desired: 2, Max: 4
   - Use the Launch Template and Target Group
6. Access your app via the ALB DNS name
7. Observe that requests are distributed between instances (check which instance handles each request using the server's hostname)

---

## Quick Reference: AWS Service Cheat Sheet

| Need | AWS Service | One-Line Description |
|------|------------|---------------------|
| Virtual server | EC2 | Rent a computer in the cloud |
| File storage | S3 | Unlimited file storage |
| Managed database | RDS | Database without the headache |
| Private network | VPC | Your own isolated network in AWS |
| Load balancing | ELB/ALB | Distribute traffic across servers |
| Auto scaling | ASG | Add/remove servers based on demand |
| DNS | Route 53 | Map domain names to servers |
| Serverless functions | Lambda | Run code without servers |
| Monitoring | CloudWatch | Watch your servers, get alerts |
| Message queue | SQS | Decouple services with a queue |
| Notifications | SNS | Send messages to many receivers |
| Access control | IAM | Who can do what in your account |
| Container service | ECS/EKS | Run Docker containers (covered in Docker/K8s weeks) |
| Secrets | Secrets Manager | Store passwords and API keys safely |

---

## Week 5 Checklist

After completing this week, you should be able to:

- [ ] Explain cloud computing to a non-technical person
- [ ] Explain IaaS vs PaaS vs SaaS with examples
- [ ] Create an AWS account and set it up securely (IAM user, MFA, billing alarm)
- [ ] Launch an EC2 instance and SSH into it
- [ ] Deploy a Spring Boot JAR on EC2
- [ ] Create an S3 bucket and host a static website
- [ ] Set up an RDS MySQL instance and connect Spring Boot to it
- [ ] Explain VPC, subnets, security groups, and how traffic flows
- [ ] Set up an ALB with health checks
- [ ] Understand Auto Scaling concepts
- [ ] Know when to use Lambda vs EC2
- [ ] Set up CloudWatch alarms for basic monitoring

---

## What is Next?

In Week 6, you will learn CI/CD Pipelines — how to automatically build, test, and deploy your Spring Boot app to AWS every time you push code. This is where everything comes together: Docker (Week 3) + Kubernetes (Week 4) + AWS (this week) + CI/CD (next week).
