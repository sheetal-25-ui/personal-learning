# Week 1: Linux and Networking Basics — Interview Answers & Exercise Solutions

> Companion to `Plan.md`. This file gives you **complete worked solutions** to every practice exercise, plus the **Linux & networking interview questions** you are most likely to be asked — each with a confident, spoken-style model answer.

---

## 🌟 The Interview Golden Rule

Whenever an interviewer asks "What is X?" or "How do you do Y?", structure your answer in this exact order. It makes you sound senior, organized, and confident:

> **1. Definition** → say what it is in one clean sentence.
> **2. Why** → explain why it exists / why it matters.
> **3. Command / Example** → show the actual command or config (you are a DevOps engineer — prove it with the terminal).
> **4. Real-world use** → tie it to a real production scenario ("I'd use this when a server runs out of disk...").

**Example in action — "What is `grep`?"**
> "*`grep` searches for a text pattern inside files* **(definition)**. *It exists because in production you have huge log files and you cannot read them line by line* **(why)**. *For instance, `grep -i "error" app.log` finds every error, case-insensitive* **(command)**. *I use it daily to hunt for exceptions in application logs during an incident* **(real-world)**."

Memorize the 4-beat rhythm: **Definition → Why → Command → Real-world.** 🎯

---

# PART A — Exercise / Practice Solutions

Every exercise from `Plan.md` is solved below with **exact commands, expected output, and a short explanation** of each step.

---

## ✅ Exercise 1: Linux Navigation (Beginner)

```bash
# 1. Where am I?
pwd
# Expected: /home/sheetal

# 2. Go to home directory
cd ~
# (or just: cd)

# 3. Create the nested directory structure in one shot using -p (parents)
mkdir -p devops-practice/linux/scripts
mkdir -p devops-practice/linux/logs
mkdir -p devops-practice/networking
mkdir -p devops-practice/docker

# Verify the tree (install 'tree' with: sudo apt install tree)
tree devops-practice
# devops-practice/
# ├── docker
# ├── linux
# │   ├── logs
# │   └── scripts
# └── networking

# 4. Navigate into the scripts folder
cd devops-practice/linux/scripts

# 5. Confirm current location
pwd
# Expected: /home/sheetal/devops-practice/linux/scripts

# 6. Go back to devops-practice/ using a RELATIVE path (up 2 levels)
cd ../..
pwd
# Expected: /home/sheetal/devops-practice

# 7. List ALL files in home (including hidden) in long format
ls -la ~
# -l = long format, -a = all (including dotfiles like .bashrc, .ssh)
```

**💡 Why it works:** `mkdir -p` creates every missing parent directory and never errors if a folder already exists — perfect for scripts. `cd ../..` is relative (two levels up from wherever you are); `ls -la` combines long format + hidden files, the single most useful `ls` form.

---

## ✅ Exercise 2: File Operations (Beginner)

```bash
cd ~/devops-practice/linux

# 1. Create notes.txt
touch notes.txt

# 2. Write a line (> overwrites / creates)
echo "Learning Linux for DevOps" > notes.txt

# 3. Append a second line (>> keeps existing content — do NOT use a single >)
echo "Day 1: Basic commands" >> notes.txt

# 4. View contents
cat notes.txt
# Learning Linux for DevOps
# Day 1: Basic commands

# 5. Make a copy
cp notes.txt notes-backup.txt

# 6. Rename the copy (mv = move/rename)
mv notes-backup.txt backup.txt

# 7. Create 5 empty files at once
touch log1.txt log2.txt log3.txt log4.txt log5.txt
# Shortcut alternative: touch log{1..5}.txt

# 8. Delete only log3.txt
rm log3.txt

# 9. List all .txt files
ls -l *.txt
# backup.txt  log1.txt  log2.txt  log4.txt  log5.txt  notes.txt
```

**⚠️ The classic trap:** Step 3 must use `>>` (append). If you use a single `>`, you **overwrite** the file and lose "Learning Linux for DevOps". Remember: `>` = replace, `>>` = add to the end.

---

## ✅ Exercise 3: grep and find (Intermediate)

```bash
cd ~/devops-practice/linux

# 1. Create the log file (heredoc writes a multi-line block)
cat > application.log << 'EOF'
2024-06-10 10:00:01 INFO  Application started
2024-06-10 10:00:05 DEBUG Connecting to database
2024-06-10 10:00:06 INFO  Database connected
2024-06-10 10:01:15 WARN  High memory usage detected
2024-06-10 10:02:30 ERROR NullPointerException in UserService.java:42
2024-06-10 10:02:30 ERROR Stack trace: at com.app.UserService.getUser(UserService.java:42)
2024-06-10 10:03:00 INFO  Request processed in 250ms
2024-06-10 10:04:00 DEBUG Cache miss for key: user_123
2024-06-10 10:05:00 ERROR Connection refused: database server unreachable
2024-06-10 10:05:01 WARN  Retrying database connection...
2024-06-10 10:05:05 INFO  Database reconnected
EOF

# 2. Count ERROR lines  (-c = count)
grep -c "ERROR" application.log
# Expected: 3

# 3. Show all WARN and ERROR lines (-E = extended regex, | = OR)
grep -E "WARN|ERROR" application.log

# 4. Show ERROR lines with 2 lines of context before AND after
grep -B 2 -A 2 "ERROR" application.log
# -B 2 = 2 lines Before, -A 2 = 2 lines After  (-C 2 does both at once)

# 5. Show all lines that are NOT DEBUG (-v = invert match)
grep -v "DEBUG" application.log

# 6. Count how many lines of EACH log level exist
grep -oE "INFO|DEBUG|WARN|ERROR" application.log | sort | uniq -c | sort -rn
#       3 ERROR
#       3 INFO
#       2 WARN
#       2 DEBUG
# -o = print ONLY the matched word, then sort | uniq -c counts duplicates,
# sort -rn puts the biggest count on top.

# 7. Find all .txt files under devops-practice
find ~/devops-practice -name "*.txt"

# 8. Find files larger than 1KB in home
find ~ -type f -size +1k
# +1k = greater than 1 kilobyte; -type f restricts to regular files
```

**💡 Star pipeline (step 6):** `grep -o ... | sort | uniq -c | sort -rn` is the canonical "count occurrences and rank them" recipe. `uniq` only collapses **adjacent** duplicates, which is why you **must** `sort` first.

---

## ✅ Exercise 4: Permissions (Intermediate)

```bash
cd ~/devops-practice/linux/scripts

# 1. Create the script
cat > check-server.sh << 'EOF'
#!/bin/bash
echo "Server check at: $(date)"
echo "Hostname: $(hostname)"
echo "Uptime: $(uptime)"
echo "Disk usage:"
df -h /
echo "Memory:"
free -m
EOF

# 2. Try to run it — IT FAILS
./check-server.sh
# bash: ./check-server.sh: Permission denied
#  WHY? A newly created file has NO execute (x) permission. Linux refuses
#  to run a file as a program unless it is marked executable.

# 3. Fix permissions — add execute
chmod +x check-server.sh

# 4. Run and verify
./check-server.sh
# Now it prints date, hostname, uptime, disk, memory.

# 5. Only YOU can read + execute (nobody else) -> 700 = rwx --- ---
chmod 700 check-server.sh
ls -l check-server.sh
# -rwx------ 1 sheetal sheetal ... check-server.sh

# 6. Create a secret and lock it to owner-only read/write
touch secret.txt
chmod 600 secret.txt        # rw- --- ---

# 7. Verify
ls -la secret.txt check-server.sh
# -rw------- 1 sheetal sheetal ... secret.txt
# -rwx------ 1 sheetal sheetal ... check-server.sh
```

**🔢 Number math:** `700` = owner `rwx`(7), group `---`(0), others `---`(0). `600` = owner `rw-`(6), nothing for anyone else. SSH keys and secrets must be `600` — Linux refuses keys that are more open.

---

## ✅ Exercise 5: Piping and Text Processing (Intermediate)

### Part 1 — using `application.log`

```bash
cd ~/devops-practice/linux

# a. Total number of lines
wc -l application.log
# 11 application.log

# b. Extract timestamps (first two columns = date + time)
awk '{print $1, $2}' application.log
# 2024-06-10 10:00:01
# 2024-06-10 10:00:05  ... etc.

# c. Extract log levels and count each
awk '{print $3}' application.log | sort | uniq -c | sort -rn
#       3 ERROR
#       3 INFO
#       2 WARN
#       2 DEBUG
# (column 3 is the log level in this format)

# d. Find the NullPointerException line, show only filename:linenumber
grep "NullPointerException" application.log | grep -oE "[A-Za-z]+\.java:[0-9]+"
# UserService.java:42
```

### Part 2 — using `employees.csv`

```bash
cat > employees.csv << 'EOF'
Name,Department,Salary
Rahul,Engineering,85000
Priya,Marketing,65000
Amit,Engineering,90000
Sneha,Engineering,78000
Raj,Marketing,70000
Pooja,HR,60000
Vikram,Engineering,95000
EOF

# a. Extract Name and Salary columns (fields 1 and 3, comma-delimited)
cut -d',' -f1,3 employees.csv
# Name,Salary
# Rahul,85000 ... etc.

# b. Find all Engineering employees
grep "Engineering" employees.csv
# Rahul,Engineering,85000
# Amit,Engineering,90000
# Sneha,Engineering,78000
# Vikram,Engineering,95000

# c. Sort by salary, highest first (column 3, numeric, reverse)
tail -n +2 employees.csv | sort -t',' -k3 -rn
# Vikram,Engineering,95000
# Amit,Engineering,90000
# Rahul,Engineering,85000 ...
# tail -n +2 skips the header row; -t',' = comma separator; -k3 = column 3;
# -rn = reverse numeric.

# d. Count employees per department
tail -n +2 employees.csv | cut -d',' -f2 | sort | uniq -c | sort -rn
#       4 Engineering
#       2 Marketing
#       1 HR
```

**💡 `tail -n +2`** means "start from line 2" — the idiomatic way to skip a CSV header before sorting/counting.

---

## ✅ Exercise 6: Shell Scripting (Advanced)

### Script 1 — `system-health.sh`

```bash
#!/bin/bash
# system-health.sh — snapshot of system health, saved with today's date

# WHY a timestamped filename: keeps a daily history instead of overwriting
REPORT="$HOME/health-$(date +%F).txt"   # %F = YYYY-MM-DD

{
  echo "===== System Health Report: $(date) ====="

  echo -e "\n--- Disk Usage ---"
  df -h
  # Warn on any filesystem over 80%
  df -h --output=pcent,target | tail -n +2 | while read -r PCT MOUNT; do
      USE=${PCT%\%}                       # strip the % sign
      if [ "$USE" -gt 80 ] 2>/dev/null; then
          echo "  ⚠️  WARNING: $MOUNT is ${USE}% full!"
      fi
  done

  echo -e "\n--- Memory Usage ---"
  free -m

  echo -e "\n--- Top 5 CPU-consuming processes ---"
  ps aux --sort=-%cpu | head -6     # header + top 5

  echo -e "\n--- Logged-in users ---"
  echo "Count: $(who | wc -l)"
  who
} | tee "$REPORT"                    # tee = print to screen AND save to file

echo "Report saved to $REPORT"
```

```bash
# Run it:
chmod +x system-health.sh
./system-health.sh
```

### Script 2 — `log-analyzer.sh`

```bash
#!/bin/bash
# log-analyzer.sh <logfile> — summarize a log file

LOGFILE="$1"                          # $1 = first argument

# Guard clause: fail loudly if no file or file missing
if [ -z "$LOGFILE" ] || [ ! -f "$LOGFILE" ]; then
    echo "Usage: $0 <logfile>   (file must exist)"
    exit 1
fi

REPORT="${LOGFILE}.summary.txt"

{
  echo "===== Log Summary for $LOGFILE — $(date) ====="
  echo "Total lines: $(wc -l < "$LOGFILE")"

  for LEVEL in ERROR WARN INFO DEBUG; do
      COUNT=$(grep -c "$LEVEL" "$LOGFILE")
      echo "$LEVEL lines: $COUNT"
  done

  echo -e "\n--- Top 5 most common ERROR messages ---"
  grep "ERROR" "$LOGFILE" | sort | uniq -c | sort -rn | head -5
} | tee "$REPORT"

echo "Summary saved to $REPORT"
```

```bash
chmod +x log-analyzer.sh
./log-analyzer.sh application.log
```

### 3 — Cron job to run `system-health.sh` every hour

```bash
crontab -e
# Add this line (run at minute 0 of every hour). Use ABSOLUTE paths in cron!
0 * * * * /home/sheetal/devops-practice/linux/scripts/system-health.sh >> /var/log/health.log 2>&1
```

**⚠️ Cron gotcha:** Cron uses a minimal `PATH`. Always use **absolute paths** to both your script and any binaries (e.g. `/usr/bin/java`), and redirect output with `>> file 2>&1` so you can debug failures.

---

## ✅ Exercise 7: Networking (Intermediate)

```bash
# 1. Your machine's IP
ip addr show          # modern; look for "inet 192.168.x.x"
# or the one-liner:
hostname -I | awk '{print $1}'

# 2. Ping google.com, note response time (-c 4 = 4 pings then stop)
ping -c 4 google.com
# 64 bytes from 142.250.x.x: icmp_seq=1 ttl=118 time=12.3 ms  <-- the time

# 3. Find flipkart.com's IP
nslookup flipkart.com
# Address: 163.53.x.x

# 4. Find gmail.com's mail servers (MX records)
dig gmail.com MX +short
# 5 gmail-smtp-in.l.google.com.   (etc.)

# 5. GET request
curl https://httpbin.org/get

# 6. POST with a JSON body
curl -X POST https://httpbin.org/post \
  -H "Content-Type: application/json" \
  -d '{"name": "Sheetal", "course": "DevOps"}'

# 7. Just the HTTP status code of google.com
curl -s -o /dev/null -w "%{http_code}\n" https://google.com
# 301   (Google redirects http->https / adds www)

# 8. Which ports are listening locally
ss -tlnp        # modern; or: netstat -tlnp
# t=tcp, l=listening, n=numeric, p=process

# 9. Route to google.com
traceroute google.com      # or: tracepath google.com
```

**💡 Status-code one-liner (step 7)** is gold for health checks in scripts: `-o /dev/null` throws away the body, `-w "%{http_code}"` prints only the number.

---

## ✅ Exercise 8: SSH (Advanced)

```bash
# 1. Generate a modern ed25519 key pair
ssh-keygen -t ed25519 -C "sheetal@example.com"
# Creates: ~/.ssh/id_ed25519 (PRIVATE — never share)
#          ~/.ssh/id_ed25519.pub (PUBLIC — safe to copy)

# 2. Copy the public key to the remote server
ssh-copy-id ubuntu@54.210.123.45
# (appends your .pub to the server's ~/.ssh/authorized_keys)

# 3. SSH in WITHOUT a password — the key authenticates you
ssh ubuntu@54.210.123.45

# 4. Add an SSH config entry so you can type "ssh myserver"
cat >> ~/.ssh/config << 'EOF'
Host myserver
    HostName 54.210.123.45
    User ubuntu
    IdentityFile ~/.ssh/id_ed25519
EOF
chmod 600 ~/.ssh/config
ssh myserver          # now this is all you type

# 5. Copy a file TO the server
scp ./report.txt myserver:/home/ubuntu/

# 6. Copy a file FROM the server
scp myserver:/var/log/app.log ./app.log
```

**🔐 Key permissions:** If you use a `.pem` key (AWS), it MUST be `chmod 600 key.pem` or SSH rejects it with "permissions too open". The private key never leaves your machine; only the public key goes on servers.

---

## ✅ Exercise 9: Combined Challenge — "The app is slow" 🔥

This is essentially a **mini incident-response runbook** — a very common interview scenario. Here is every command, in order, with the reasoning.

```bash
# 1. Which filesystem is fullest? (look at the Use% column)
df -h

# 2. Is memory low? (look at "available", not just "free")
free -m

# 3. Find the Java process + its CPU/MEM
ps aux | grep [j]ava       # the [j] trick stops grep matching itself
# Or sort everything by CPU and look at the top:
ps aux --sort=-%cpu | head -5

# 4. Last 100 log lines, errors only
tail -100 /var/log/myapp/application.log | grep -iE "error|exception"

# 5. Is the app actually listening on 8080?
ss -tlnp | grep 8080
# If nothing prints, the app is NOT listening -> that's your problem.

# 6. Can we reach the DB server?
ping -c 4 10.0.1.50
# Also check the DB PORT is open (more useful than ping):
nc -zv 10.0.1.50 3306      # nc = netcat; -z scan, -v verbose

# 7 & 8 — script it and schedule every 15 minutes (below)
```

### The diagnostic script

```bash
#!/bin/bash
# diagnose.sh — capture a health snapshot when the app is slow
REPORT="/var/log/diagnose-$(date +%F_%H-%M).txt"
DB_HOST="10.0.1.50"
APP_PORT=8080

{
  echo "===== Diagnostic Report: $(date) ====="
  echo -e "\n[1] Disk:";      df -h
  echo -e "\n[2] Memory:";    free -m
  echo -e "\n[3] Java proc:"; ps aux | grep [j]ava
  echo -e "\n[4] Recent errors:"
  tail -100 /var/log/myapp/application.log | grep -iE "error|exception"
  echo -e "\n[5] Port $APP_PORT listening?:"; ss -tlnp | grep "$APP_PORT" || echo "NOT LISTENING!"
  echo -e "\n[6] DB reachable?:"; ping -c 2 "$DB_HOST" > /dev/null 2>&1 \
      && echo "$DB_HOST is UP" || echo "$DB_HOST is DOWN!"
} | tee "$REPORT"
```

```bash
chmod +x diagnose.sh

# Schedule every 15 minutes
crontab -e
*/15 * * * * /home/sheetal/scripts/diagnose.sh >> /var/log/diagnose-cron.log 2>&1
```

**🗣️ How to narrate this in an interview:** "I always go top-down through the resource stack — *disk → memory → CPU → process → logs → network*. Nine times out of ten 'slow' or 'down' is a full disk, an OOM-killed process, or a dependency (DB) that's unreachable. I check each layer with one command before guessing."

---

# PART B — Interview Questions & Model Answers

These are the questions interviewers reach for most often in DevOps/SRE phone screens. Each answer is in **spoken style** — say it roughly like this and you'll sound confident.

---

### Q1. Explain Linux file permissions and how `chmod` numbers work.

> "Every file has three permission sets — for the **owner**, the **group**, and **others**. Each set has read (`r`), write (`w`), and execute (`x`). In octal, **read = 4, write = 2, execute = 1**, and you add them up per group. So `755` means owner `rwx` (7), group `r-x` (5), others `r-x` (5). I use `644` for normal config files, `755` for scripts and directories, and `600` for secrets and SSH keys. For example, `chmod 600 id_rsa` locks a private key to the owner only — SSH actually refuses keys that are more open than that."

**Cheat-sheet:**

| Octal | Symbolic | Meaning | Use for |
|-------|----------|---------|---------|
| 777 | `rwxrwxrwx` | everyone everything ⚠️ | never on a server |
| 755 | `rwxr-xr-x` | owner full, rest read+exec | scripts, directories, binaries |
| 644 | `rw-r--r--` | owner write, rest read | config files, web assets |
| 700 | `rwx------` | owner only, full | private scripts |
| 600 | `rw-------` | owner read/write only | SSH keys, secrets, `.pem` |

---

### Q2. What's the difference between symbolic (`chmod +x`) and numeric (`chmod 755`) modes?

> "Numeric mode sets **all** permission bits at once to an exact value — `chmod 755` overwrites whatever was there. Symbolic mode **adjusts** specific bits relative to current state — `chmod +x` just adds execute for everyone, `chmod u+x` adds it only for the owner, `chmod o-r` removes read from others. I use numeric when I want a known-exact result, and symbolic for quick tweaks like making one script executable."

---

### Q3. How do you find a process and kill it? Explain the kill signals.

> "I find it with `ps aux | grep <name>` or `pgrep`, which gives me the **PID**. Then `kill <PID>` sends **SIGTERM (15)** — a polite 'please shut down', which lets the app close files and flush state. If it's hung and ignores that, I escalate to `kill -9 <PID>`, which is **SIGKILL** — the kernel terminates it instantly, no cleanup. There's also **SIGHUP (1)**, often used to tell a daemon to reload its config without restarting. Rule of thumb: always try a graceful `kill` first; `-9` is the last resort because it can leave locks or corrupt state."

**Signal cheat-sheet:**

| Signal | Number | `kill` | Meaning |
|--------|--------|--------|---------|
| SIGTERM | 15 | `kill PID` (default) | graceful stop — app can clean up |
| SIGKILL | 9 | `kill -9 PID` | force kill — instant, no cleanup |
| SIGHUP | 1 | `kill -1 PID` | hang-up — often "reload config" |
| SIGINT | 2 | `Ctrl + C` | interrupt from keyboard |
| SIGSTOP | 19 | `Ctrl + Z` | pause/suspend a process |

---

### Q4. How do you find which process is using a specific port (say 8080)?

> "Three reliable ways. My go-to is `ss -tlnp | grep 8080` — `t`=tcp, `l`=listening, `n`=numeric, `p`=show the process. The older equivalent is `netstat -tlnp | grep 8080`. Or `lsof -i :8080`, which lists the open file/socket and its PID. Once I have the PID I can `kill` it. This is the exact command I run when a deploy fails with 'Address already in use' — a stale process is still holding the port."

```bash
ss -tlnp | grep 8080
sudo lsof -i :8080
sudo fuser 8080/tcp        # also prints the PID
```

---

### Q5. Walk me through what happens when you type a URL and press Enter.

> "Roughly eight stages. **(1) DNS resolution** — the browser checks its cache, then the OS and `/etc/hosts`, then a DNS resolver walks Root → `.com` TLD → the domain's nameserver to get the IP. **(2)** The browser opens a **TCP connection** to that IP on port 443 via the three-way handshake (SYN, SYN-ACK, ACK). **(3)** For HTTPS, a **TLS handshake** negotiates encryption and validates the certificate. **(4)** The browser sends an **HTTP request** — method, path, headers. **(5)** The request may pass through a **load balancer / reverse proxy** like Nginx, which forwards it to a backend app server. **(6)** The server processes it, maybe hits a database, and returns an **HTTP response** with a status code. **(7)** The browser **renders** the HTML, then fetches CSS, JS, and images. **(8)** The connection is reused or closed. As a DevOps engineer I care about every hop because that's where latency and failures hide — slow DNS, a 502 from the proxy, an unreachable backend."

---

### Q6. TCP vs UDP — when do you use each?

> "**TCP** is connection-oriented and reliable — it does a handshake, guarantees delivery, retransmits lost packets, and keeps everything in order, but that overhead makes it slower. **UDP** is connectionless and fire-and-forget — no handshake, no guarantee, no ordering, but it's fast and low-latency. You use **TCP** for anything where correctness matters: web (HTTP/HTTPS), SSH, email, database connections. You use **UDP** where speed beats perfection and a dropped packet is fine: video calls, live streaming, online gaming, and **DNS** lookups. The mental model: TCP is registered post with tracking; UDP is shouting across a field."

| | TCP | UDP |
|---|-----|-----|
| Connection | yes (handshake) | none |
| Reliable / ordered | yes | no |
| Speed | slower | faster |
| Examples | HTTP, SSH, email, DB | DNS, video, gaming, VoIP |

---

### Q7. Explain the OSI model (or at least the layers that matter).

> "It's a 7-layer model describing how data moves across a network — **Please Do Not Throw Sausage Pizza Away**: **Physical, Data Link, Network, Transport, Session, Presentation, Application**. In practice I mostly reason about four: **Layer 3 (Network)** is IP addressing and routing; **Layer 4 (Transport)** is TCP/UDP and ports; **Layer 7 (Application)** is HTTP, DNS, SSH. When someone says 'a Layer 4 load balancer' they mean it routes on IP+port without reading the request; a 'Layer 7 load balancer' like Nginx reads the HTTP path and headers to route. Knowing which layer a problem lives on tells me which tool to grab — `ping`/`traceroute` for L3, `ss`/`telnet` for L4, `curl` for L7."

**Layer mnemonic:**

| # | Layer | Carries | Example / tool |
|---|-------|---------|----------------|
| 7 | Application | app data | HTTP, DNS, SSH — `curl` |
| 4 | Transport | segments | TCP/UDP, ports — `ss`, `telnet` |
| 3 | Network | packets | IP, routing — `ping`, `traceroute` |
| 2 | Data Link | frames | MAC, switches |
| 1 | Physical | bits | cables, Wi-Fi |

---

### Q8. How does DNS resolution work?

> "DNS turns a human name like `flipkart.com` into an IP. The lookup goes: browser cache → OS cache and `/etc/hosts` → the **DNS resolver** (your ISP or `8.8.8.8`). If the resolver doesn't know, it asks the **Root** server, which points to the **`.com` TLD** server, which points to the domain's **authoritative nameserver**, which returns the actual IP. That IP is then cached based on its TTL — usually minutes to hours. I test this with `dig flipkart.com +short` or `nslookup`. It's also a classic gotcha: if a site 'isn't loading' but the IP pings fine, it's often a DNS or `/etc/hosts` issue."

**DNS record cheat-sheet:**

| Record | Maps to | Example |
|--------|---------|---------|
| A | IPv4 address | `flipkart.com → 163.53.78.128` |
| AAAA | IPv6 address | `→ 2404:6800:...` |
| CNAME | another name (alias) | `www → flipkart.com` |
| MX | mail server | `→ mail.flipkart.com` |
| TXT | text (SPF, verification) | email security |
| NS | nameserver | `→ ns1.flipkart.com` |

---

### Q9. What are the common ports I should know?

> "I keep a mental list of the everyday ones. Ports 0–1023 are 'well-known' and need root to bind."

| Port | Service | Port | Service |
|------|---------|------|---------|
| 22 | SSH | 3306 | MySQL |
| 80 | HTTP | 5432 | PostgreSQL |
| 443 | HTTPS | 6379 | Redis |
| 25 | SMTP (mail) | 27017 | MongoDB |
| 53 | DNS | 8080 | Tomcat / alt-HTTP |
| 21 | FTP | 9092 | Kafka |

> "Knowing these lets me read a firewall rule or an `ss` output at a glance — if I see something listening on 6379 I instantly know it's Redis."

---

### Q10. What is SSH and how does key-based auth work?

> "SSH — Secure Shell — is an encrypted protocol to log into and run commands on a remote server, on port 22. Instead of passwords (guessable, brute-forceable), I use a **key pair**: a **private key** that stays on my machine and a **public key** that I put in the server's `~/.ssh/authorized_keys`. When I connect, the server challenges me and only my private key can answer — without ever sending it over the wire. I generate keys with `ssh-keygen -t ed25519`, push the public key with `ssh-copy-id`, and the private key must be `chmod 600` or SSH refuses it. For AWS I connect with `ssh -i key.pem ubuntu@<ip>`."

---

### Q11. `grep`, `awk`, and `sed` — what's the difference?

> "All three are text-processing tools but with different jobs. **`grep`** *finds* lines matching a pattern — `grep ERROR app.log`. **`awk`** *extracts and computes* on columns — it's a mini language; `awk '{print $1}'` pulls the first field, and `awk '{sum+=$10} END{print sum}'` sums a column. **`sed`** *edits* a stream — find-and-replace and line deletion; `sed -i 's/old/new/g' file` does an in-place global replace. The way I remember it: **grep finds, awk slices, sed substitutes.**"

```bash
grep -i "error" app.log              # FIND error lines (case-insensitive)
awk -F',' '{print $2}' data.csv      # SLICE column 2 from a CSV
sed -i 's/localhost/10.0.0.5/g' conf # SUBSTITUTE text, in place
```

---

### Q12. What's the difference between a soft (symbolic) link and a hard link?

> "A **hard link** is a second name pointing to the *same* underlying data (same inode) — delete the original and the data still lives on through the hard link; they're indistinguishable. A **soft/symbolic link** is a tiny pointer file that stores a *path* to the target — like a Windows shortcut. If you delete the target, the symlink dangles and breaks. Symlinks can cross filesystems and point to directories; hard links can't. I create them with `ln target hardlink` and `ln -s target symlink`. In practice I almost always use symlinks — e.g., pointing `/opt/app/current` at `/opt/app/releases/v2.5` so a deploy is just re-pointing the link."

| | Hard link | Soft (symbolic) link |
|---|-----------|----------------------|
| Points to | same inode (data) | a path/name |
| Survives target deletion | yes | no (dangles) |
| Cross-filesystem | no | yes |
| Link to a directory | no | yes |
| Command | `ln a b` | `ln -s a b` |

---

### Q13. How do you check disk, memory, and CPU usage?

> "**Disk:** `df -h` shows free space per filesystem (I watch the `Use%` column), and `du -sh *` shows what's eating space in a directory. **Memory:** `free -m` — and the key column is `available`, not `free`, because Linux uses spare RAM for cache that it releases on demand. **CPU and live processes:** `top` or `htop` for a real-time dashboard, or `ps aux --sort=-%cpu | head` for a one-shot top-consumers list. When something's wrong my reflex order is df → free → top, checking each resource in turn."

```bash
df -h                       # disk free per filesystem
du -sh /var/log/* | sort -rh | head   # biggest space hogs
free -m                     # memory (watch "available")
top    # or: htop           # live CPU/mem dashboard
ps aux --sort=-%cpu | head  # top CPU consumers
```

---

### Q14. What's the difference between `>` and `>>`, and what is `2>&1`?

> "`>` **overwrites** a file with the command's output — existing content is wiped. `>>` **appends** to the end, preserving what's there. Then there are two output streams: **stdout** (file descriptor 1) for normal output, and **stderr** (descriptor 2) for errors. `2>&1` means 'send stderr to wherever stdout is going', so `java -jar app.jar > app.log 2>&1` captures both normal logs and errors in one file. And `> /dev/null 2>&1` discards everything — `/dev/null` is the black hole. I use this constantly when running things from cron or background jobs."

---

### Q15. `find` vs `locate` — what's the difference?

> "**`find`** searches the live filesystem in real time — accurate and flexible (by name, size, age, type, and it can run an action with `-exec`), but slower on big trees. **`locate`** searches a pre-built database (`updatedb`) so it's near-instant, but results can be stale if the DB hasn't refreshed. I use `find` when I need precision or freshly created files — like `find /var/log -name '*.gz' -mtime +30 -delete` to clean old archives — and `locate` for a quick 'where does this file live' lookup."

---

### Q16. How do you keep a process running after you log out / close SSH?

> "By default, closing the terminal sends SIGHUP and kills your foreground jobs. To survive a disconnect I have a few options. The classic is **`nohup`**: `nohup java -jar app.jar > app.log 2>&1 &` — 'no hang-up', detached, output to a file, running in the background. For interactive sessions I prefer **`tmux`** or **`screen`**, where I can detach and reattach later. But for anything real and long-lived in production, the right answer is a **systemd service** — it handles start, stop, restart-on-crash, and logging properly. `nohup` is for quick one-offs; systemd is for services that matter."

---

### Q17. How does a request reach a Java app running on port 8080 when users type a normal URL?

> "Users hit `https://mysite.com`, which is port **443** (or 80 for plain HTTP). But the Java app — Spring Boot or Tomcat — listens on **8080**, an unprivileged port. So I put a **reverse proxy** like Nginx in front: it terminates TLS on 443, then forwards the request internally to `localhost:8080`. That's `proxy_pass http://127.0.0.1:8080;`. This pattern also gives me load balancing, caching, and a single place to manage certificates. The flow is: **browser → Nginx:443 → app:8080 → response back the same path.**"

```nginx
server {
    listen 443 ssl;
    server_name mysite.com;
    location / {
        proxy_pass http://127.0.0.1:8080;   # forward to the Java app
        proxy_set_header Host $host;
    }
}
```

---

### Q18. Explain HTTP status codes and what each class means.

> "They group by the first digit. **2xx = success** — 200 OK, 201 Created, 204 No Content. **3xx = redirection** — 301 permanent move, 302 temporary, 304 use-your-cache. **4xx = client error, the caller's fault** — 400 bad request, 401 not authenticated, 403 authenticated but forbidden, 404 not found, 429 too many requests. **5xx = server error, our fault** — 500 internal error, 502 bad gateway (proxy can't reach the backend), 503 service unavailable, 504 gateway timeout (backend too slow). As a DevOps engineer the distinction I care most about is **4xx vs 5xx**: a spike in 5xx means *our* system is broken and I get paged; a spike in 4xx usually means a client or bad deploy is sending wrong requests. And **502/504** almost always point at the proxy-to-backend link."

---

### Q19. How do you debug a server that's "not reachable"? (bonus / very common)

> "I work outward layer by layer. **Layer 3:** `ping <ip>` — does the host respond at all? Then `traceroute` to see where packets die. **Layer 4:** is the *port* open? `nc -zv host 443` or `telnet host 443` — a host can be up but the service down. **DNS:** `dig domain +short` — does the name even resolve, and to the right IP? **Local to the box (if I can SSH in):** `ss -tlnp` to confirm the service is listening, `systemctl status nginx` to check it's running, and the firewall (`ufw status` / security group). The discipline is: never guess — isolate which layer is broken with one command each."

---

## 🛠️ Master Command Cheat-Sheet

| Task | Command |
|------|---------|
| Where am I | `pwd` |
| List all + hidden | `ls -la` |
| Search inside files | `grep -rni "text" .` |
| Find files | `find / -name "*.log"` |
| Change permissions | `chmod 755 file` / `chmod +x file` |
| Change owner | `chown user:group file` |
| Live processes | `top` / `htop` |
| Find a process | `ps aux \| grep java` |
| Kill gracefully / force | `kill PID` / `kill -9 PID` |
| Disk free / usage | `df -h` / `du -sh *` |
| Memory | `free -m` |
| Port → process | `ss -tlnp \| grep 8080` / `lsof -i :8080` |
| Test connectivity | `ping host` / `nc -zv host port` |
| Trace route | `traceroute host` |
| DNS lookup | `dig domain +short` / `nslookup domain` |
| Test an API | `curl -v URL` |
| HTTP status only | `curl -s -o /dev/null -w "%{http_code}" URL` |
| Remote login | `ssh user@host` / `ssh -i key.pem user@host` |
| Copy over SSH | `scp file user@host:/path` |
| Watch logs live | `tail -f /var/log/app.log` |

---

## 🧠 Memory Hooks — Vivid Analogies

| Concept | Analogy 🎭 | Trigger to recall it |
|---------|-----------|----------------------|
| Linux vs Windows | Manual car vs automatic car | "Full control under the hood" |
| Terminal | Telling the waiter your exact order | precise commands, no menu |
| `rm -rf /` | Pouring petrol on your house & lighting it | NEVER run, no undo |
| Permissions `rwx` | Office: **owner** at their desk, **group** = team, **others** = other departments | who can touch the file |
| chmod numbers | r=**4**, w=**2**, x=**1** → add them up | 7=rwx, 6=rw, 5=r-x |
| `600` on keys | A diary with a personal lock | only you |
| PID | An Aadhaar number for a process | unique per process |
| `kill -9` | Pulling the power plug vs shutting down | force, no cleanup |
| `nohup` | "Keep working even after I leave the office" | survives logout |
| `tail -f` | Watching a live cricket score | logs update in real time |
| `free` available | Fridge with reclaimable leftovers | "available" > "free" |
| `/dev/null` | A bottomless dustbin | output disappears |
| Pipe `\|` | Factory assembly line | output → next input |
| IP address | A building's street address | which machine |
| **Port = apartment number** | Building 192.168.1.5, **Suite 8080** | which service on the machine |
| localhost `127.0.0.1` | "My own house" | always means this machine |
| `0.0.0.0` | "All doors and windows of my house" | listen on every interface |
| DNS | Phone contact list (name → number) | resolves name to IP |
| Router | Post office forwarding letters | next-hop routing |
| TCP | Registered post with tracking | reliable, ordered, slower |
| UDP | Shouting "CATCH!" across a field | fast, no guarantee |
| HTTP vs HTTPS | Shouting your card number vs whispering | plain text vs encrypted |
| SSH keys | Lock on the door (public) + the only key (private) | key-based auth |
| Reverse proxy | Receptionist directing visitors to the right desk | Nginx :443 → app :8080 |
| OSI layers | **P**lease **D**o **N**ot **T**hrow **S**ausage **P**izza **A**way | Physical→Application |
| 5xx vs 4xx | 5xx = chef burned it; 4xx = customer ordered wrong | server fault vs client fault |

---

## 🎯 Final Interview Tip

When you don't know an exact answer, **don't freeze and don't bluff** — narrate your *approach*. DevOps interviewers care far more about how you **reason through a problem** than whether you memorized a flag. Say things like: *"I'd start by checking the obvious resources — disk, memory, then the process and logs — and isolate which layer the failure is on before changing anything."* That mindset — **methodical, top-down, evidence-before-action** — is exactly what separates a real engineer from someone reciting commands.

And always close the loop with the golden rule: **Definition → Why → Command → Real-world.** Practice these commands daily on a real Ubuntu box or a free-tier EC2 instance until your fingers know them by heart. The terminal rewards repetition. 🚀

> "An interview isn't a memory test — it's a *thinking-out-loud* test. Show your reasoning, reach for the right command, and tie it to a real production story."
