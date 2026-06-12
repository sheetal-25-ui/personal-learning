# Week 1: Linux and Networking Basics

## A DevOps Foundation Course for Java Developers

> Written in simple language with real-life Indian examples.
> Every concept covers: What is it? Real-life analogy. Why it exists. How it works. Common mistakes. Practice exercises.

---

# PART 1: LINUX BASICS

---

## 1. Why Linux?

### What is Linux?

Linux is an operating system, just like Windows or macOS. But unlike Windows (which is owned by Microsoft), Linux is **free and open source** — anyone can use it, modify it, and distribute it.

### Real-Life Analogy

Think of it like this:

- **Windows = Automatic car (like driving in Bangalore traffic with automatic gear)**
  - Easy to use, everything is point-and-click
  - But you have less control over what happens under the hood
  - When something goes wrong, you are stuck — you cannot fix it yourself

- **Linux = Manual car (like a Maruti 800 — full control)**
  - You control everything through commands
  - You decide exactly what the system does
  - When something breaks, you can open the hood and fix it

### Why Does Every DevOps Engineer Need Linux?

Here is a fact that surprises most beginners:

- **96%+ of the world's servers run Linux** (including Amazon, Google, Netflix, Flipkart, Zomato — all of them)
- Your Java application that runs perfectly on your Windows laptop? In production, it runs on a Linux server.
- Docker containers? Linux inside.
- Kubernetes? Linux underneath.
- AWS EC2 instances? Linux by default.

**If you do not know Linux, you cannot do DevOps. Period.**

### Why Not Windows Servers?

| Factor         | Linux                          | Windows Server              |
|----------------|--------------------------------|-----------------------------|
| Cost           | Free                           | Expensive license           |
| Performance    | Lightweight, fast              | Heavy, needs more RAM       |
| Stability      | Runs for years without restart | Needs frequent restarts     |
| Customization  | Total control                  | Limited                     |
| Community      | Massive, free help everywhere  | Paid support mostly         |
| Security       | More secure by design          | More targeted by viruses    |

### Common Linux Distributions (Flavors)

Think of Linux distributions like different brands of dal — the base ingredient is the same (Linux kernel), but each brand has its own taste:

| Distribution | Used For                              | Package Manager |
|-------------|---------------------------------------|-----------------|
| Ubuntu      | Most popular for servers and learning | apt             |
| CentOS/RHEL | Enterprise servers (banks, etc.)     | yum / dnf       |
| Amazon Linux| AWS EC2 default                       | yum             |
| Alpine      | Docker containers (very tiny)         | apk             |
| Debian      | Ubuntu is based on this               | apt             |

**For this course, we will use Ubuntu** — it is the most beginner-friendly and most widely used.

---

## 2. The Terminal — Your New Best Friend

### What is the Terminal?

The terminal (also called command line, shell, or console) is a text-based interface where you type commands to tell the computer what to do.

### Real-Life Analogy

Imagine you go to a restaurant:

- **Windows (GUI)** = You look at the menu card with pictures, point at what you want, and the waiter brings it.
- **Linux (Terminal)** = You tell the waiter exactly what you want: "Bring me 2 butter naan, 1 dal makhani, extra butter, no onion." You have precise control over every detail.

### Opening the Terminal

- On Ubuntu Desktop: Press `Ctrl + Alt + T`
- On a remote server (AWS, etc.): You connect via SSH (we will learn this later)
- On macOS: Open the "Terminal" application

### The Prompt

When you open the terminal, you see something like:

```
sheetal@mycomputer:~$
```

This tells you:
- `sheetal` = your username (who you are)
- `mycomputer` = the computer name (which machine)
- `~` = your current location (~ means home directory, like your bedroom in your house)
- `$` = you are a normal user (if you see `#`, you are the root/admin user)

---

## 3. Navigation Commands — Moving Around the File System

### 3.1 pwd — Print Working Directory

**What:** Shows your current location (which folder you are in right now).

**Analogy:** You are in a big building (the computer). `pwd` is like asking "Which floor and room am I in right now?"

```bash
$ pwd
/home/sheetal
```

This means you are in the folder `/home/sheetal`.

**When to use:** When you are lost and need to know where you are. This happens A LOT when you are a beginner.

---

### 3.2 ls — List Directory Contents

**What:** Shows all files and folders in the current directory.

**Analogy:** You walk into a room and look around — "What is in this room?" That is `ls`.

```bash
$ ls
Desktop  Documents  Downloads  Music  Pictures
```

#### Important Flags (Options)

**`ls -l` — Long format (detailed view)**

```bash
$ ls -l
drwxr-xr-x 2 sheetal sheetal 4096 Jun 10 10:30 Desktop
-rw-r--r-- 1 sheetal sheetal  220 Jun  8 09:15 .bashrc
```

What each column means:
```
drwxr-xr-x  2  sheetal  sheetal  4096  Jun 10 10:30  Desktop
|           |   |        |       |     |              |
|           |   |        |       |     |              +-- Name
|           |   |        |       |     +-- Last modified date
|           |   |        |       +-- Size in bytes
|           |   |        +-- Group owner
|           |   +-- File owner
|           +-- Number of links
+-- Permissions (we will learn this in detail later)
```

The first character: `d` = directory (folder), `-` = regular file, `l` = link (shortcut)

**`ls -a` — Show ALL files (including hidden)**

In Linux, files starting with `.` (dot) are hidden. Like how some people keep their money hidden under the mattress — it is there, you just cannot see it normally.

```bash
$ ls -a
.  ..  .bashrc  .ssh  Desktop  Documents
```

- `.` = current directory (this room)
- `..` = parent directory (the room above/outside)
- `.bashrc` = hidden configuration file
- `.ssh` = hidden SSH folder

**`ls -la` — Long format + hidden files (MOST USEFUL combination)**

```bash
$ ls -la
total 48
drwxr-xr-x 6 sheetal sheetal 4096 Jun 10 10:30 .
drwxr-xr-x 3 root    root    4096 Jun  8 09:00 ..
-rw-r--r-- 1 sheetal sheetal  220 Jun  8 09:15 .bashrc
drwx------ 2 sheetal sheetal 4096 Jun  9 14:20 .ssh
drwxr-xr-x 2 sheetal sheetal 4096 Jun 10 10:30 Desktop
```

**`ls -lh` — Human-readable sizes**

Instead of showing `4096` bytes, it shows `4.0K`. Instead of `1048576`, it shows `1.0M`.

```bash
$ ls -lh
-rw-r--r-- 1 sheetal sheetal 4.0K Jun 10 10:30 config.txt
-rw-r--r-- 1 sheetal sheetal 2.5M Jun  9 14:20 app.jar
-rw-r--r-- 1 sheetal sheetal 1.2G Jun  8 09:00 database-dump.sql
```

**`ls -lt` — Sort by time (newest first)**

```bash
$ ls -lt
# Shows most recently modified files at the top
```

**`ls -lS` — Sort by size (largest first)**

```bash
$ ls -lS
# Shows biggest files at the top — useful when disk is full
```

**`ls -R` — Recursive (show contents of subdirectories too)**

```bash
$ ls -R
# Shows everything inside every folder, recursively
```

### Common Mistake with ls

Beginners often forget that `ls` does not show hidden files by default. So they think a directory is empty when it actually has hidden files:

```bash
$ ls
# (nothing shown — looks empty!)

$ ls -a
.env  .gitignore  .config
# Oh! There were hidden files all along!
```

---

### 3.3 cd — Change Directory

**What:** Move to a different directory (folder).

**Analogy:** Walking from one room to another in a building.

```bash
# Go to Desktop
$ cd Desktop

# Go to home directory (shortcut)
$ cd ~
$ cd          # Just "cd" with nothing also goes home

# Go to parent directory (one level up)
$ cd ..

# Go to parent's parent (two levels up)
$ cd ../..

# Go to an absolute path (full address)
$ cd /var/log

# Go to the previous directory (like "back" button)
$ cd -
```

#### Absolute vs Relative Paths

- **Absolute path** = Full address, starts with `/`
  - Like saying: "My house is at 42, MG Road, Bangalore, Karnataka, India"
  - Example: `/home/sheetal/Desktop/projects`

- **Relative path** = Direction from where you are now
  - Like saying: "Go two streets left from here"
  - Example: `../Documents/resume.pdf`

### Common Mistake with cd

```bash
$ cd /Desktop       # WRONG! This looks for Desktop at the root /
$ cd Desktop        # CORRECT! This goes to Desktop inside current folder
$ cd ~/Desktop      # ALSO CORRECT! This goes to Desktop in your home folder
```

---

### 3.4 mkdir — Make Directory

**What:** Creates a new folder.

```bash
# Create a single folder
$ mkdir projects

# Create nested folders (parent folders that do not exist yet)
$ mkdir -p projects/java/spring-boot
# -p = "parents" — create the whole path, do not complain if parent does not exist

# Without -p, this would fail:
$ mkdir projects/java/spring-boot
# ERROR: mkdir: cannot create directory 'projects/java/spring-boot': No such file or directory
# Because "projects" and "java" do not exist yet
```

---

### 3.5 rmdir — Remove Directory

**What:** Removes an EMPTY directory only.

```bash
$ rmdir empty-folder    # Works only if the folder is empty
$ rmdir non-empty       # FAILS — the folder has files inside
```

**In practice, nobody uses rmdir.** Everyone uses `rm -r` instead (next section).

---

### 3.6 rm — Remove (Delete)

**What:** Deletes files and directories. **THIS IS THE MOST DANGEROUS COMMAND IN LINUX.**

**Why dangerous?** Because there is NO recycle bin, NO undo, NO "Are you sure?" dialog. Once deleted, it is GONE FOREVER.

```bash
# Delete a single file
$ rm myfile.txt

# Delete a directory and everything inside it
$ rm -r my-folder
# -r = recursive (go inside folders and delete everything)

# Force delete without asking any questions
$ rm -rf my-folder
# -f = force (do not ask for confirmation, do not show errors)

# Delete multiple files
$ rm file1.txt file2.txt file3.txt

# Delete all .log files in current directory
$ rm *.log
```

#### THE COMMAND THAT DESTROYS EVERYTHING

```bash
$ sudo rm -rf /
```

**NEVER EVER RUN THIS.** This deletes your ENTIRE system — every file, every program, everything. Your computer becomes a brick.

Real story: In 2015, a company called "Hosting.com" accidentally ran a similar command on their production server and lost all their customer data.

**Analogy:** `rm -rf /` is like pouring petrol on your entire house and lighting a match. Everything burns. There is no fire brigade that can save you.

#### Safe Practice

```bash
# Always double-check what you are deleting
$ ls my-folder    # First, look at what is inside
$ rm -ri my-folder  # -i = interactive, asks before each deletion
```

### Common Mistake with rm

```bash
$ rm -rf / home/sheetal/old-files
#        ^ NOTICE THE SPACE!
# This deletes / (root — everything!) AND home/sheetal/old-files
# The space between / and home makes it TWO separate arguments

$ rm -rf /home/sheetal/old-files
# THIS is correct — no space, single path
```

---

## 4. File Commands — Creating and Viewing Files

### 4.1 touch — Create an Empty File

**What:** Creates a new empty file, or updates the timestamp of an existing file.

**Analogy:** Like placing an empty notebook on your desk.

```bash
$ touch notes.txt           # Creates empty file
$ touch file1.txt file2.txt # Creates multiple files at once
```

---

### 4.2 cat — Display File Contents

**What:** Shows the entire content of a file on the screen.

**Analogy:** Opening a notebook and reading the whole thing from first page to last.

```bash
$ cat notes.txt
Hello, this is my notes file.
This is line 2.

# Show line numbers
$ cat -n notes.txt
     1  Hello, this is my notes file.
     2  This is line 2.

# Combine multiple files
$ cat file1.txt file2.txt > combined.txt
```

**Warning:** Do NOT use `cat` on very large files (like a 2GB log file). It will flood your terminal. Use `head` or `tail` instead.

---

### 4.3 head — Show Beginning of File

**What:** Shows the first few lines of a file (default: 10 lines).

```bash
$ head access.log           # Shows first 10 lines
$ head -n 20 access.log     # Shows first 20 lines
$ head -n 5 access.log      # Shows first 5 lines
```

**When to use:** When you want to quickly peek at a file — "What does this file look like?"

---

### 4.4 tail — Show End of File

**What:** Shows the last few lines of a file (default: 10 lines).

```bash
$ tail access.log           # Shows last 10 lines
$ tail -n 20 access.log     # Shows last 20 lines
```

#### tail -f — The MOST IMPORTANT Flag (Follow Mode)

```bash
$ tail -f /var/log/application.log
```

**What this does:** It keeps watching the file in real-time. As new lines are added, they appear on your screen instantly.

**Analogy:** It is like watching a live cricket score — the score keeps updating automatically without you refreshing the page.

**When to use:** When your Java application is running and you want to see logs in real-time:

```bash
# Watching Tomcat logs live
$ tail -f /var/log/tomcat/catalina.out

# Watching multiple log files at once
$ tail -f /var/log/app/*.log
```

Press `Ctrl + C` to stop watching.

---

### 4.5 cp — Copy Files and Directories

**What:** Makes a copy of a file or directory.

```bash
# Copy a file
$ cp original.txt backup.txt

# Copy a file to another directory
$ cp config.properties /tmp/

# Copy a directory (must use -r for recursive)
$ cp -r my-project my-project-backup

# Copy and preserve permissions, timestamps, etc.
$ cp -rp source/ destination/
# -p = preserve attributes
```

---

### 4.6 mv — Move or Rename

**What:** Moves a file to another location OR renames it.

**Analogy:** Like moving furniture from one room to another, or putting a new name plate on the same room.

```bash
# Rename a file
$ mv old-name.txt new-name.txt

# Move a file to another directory
$ mv report.pdf /home/sheetal/Documents/

# Move a directory
$ mv my-project /opt/applications/

# Move multiple files to a directory
$ mv *.log /var/log/archive/
```

---

### 4.7 nano and vim — Text Editors

#### nano — The Easy Editor

**Analogy:** nano is like Notepad on Windows — simple, easy, gets the job done.

```bash
$ nano myfile.txt
```

- Type your text normally
- `Ctrl + O` = Save (O for "Output")
- `Ctrl + X` = Exit
- `Ctrl + K` = Cut a line
- `Ctrl + U` = Paste

#### vim — The Powerful Editor

**Analogy:** vim is like driving a Formula 1 car — extremely powerful but takes time to learn.

vim has two modes:
1. **Normal mode** (default) — for navigating and commands
2. **Insert mode** — for typing text

```bash
$ vim myfile.txt

# You are now in NORMAL mode. You CANNOT type text yet!

# Press 'i' to enter INSERT mode (now you can type)
# Press 'Esc' to go back to NORMAL mode
# In NORMAL mode, type ':wq' and press Enter to save and quit
# In NORMAL mode, type ':q!' and press Enter to quit WITHOUT saving
```

**The most common vim complaint:** "I opened vim and I cannot exit!"

The answer: Press `Esc`, then type `:q!` and press `Enter`.

**For beginners: Use nano.** Switch to vim only after you are comfortable with Linux.

---

## 5. Search Commands — Finding Things

### 5.1 find — Search for Files by Name, Size, Date

**What:** Searches for files and directories in the file system.

**Analogy:** Like searching for a specific file in a huge office cabinet — "Find me all the invoices from March that are bigger than 1 MB."

```bash
# Find by name
$ find /home/sheetal -name "*.java"
# Search in /home/sheetal for all files ending with .java

# Find by name (case-insensitive)
$ find / -iname "readme.md"
# -iname = ignore case (matches README.md, Readme.md, readme.MD, etc.)

# Find by size
$ find /var/log -size +100M
# Find files larger than 100 MB in /var/log
# +100M = more than 100 MB
# -100M = less than 100 MB
# 100M = exactly 100 MB

# Find by modification time
$ find /tmp -mtime +7
# Find files modified MORE than 7 days ago
# -mtime -7 = modified LESS than 7 days ago (recent files)

# Find and delete (BE CAREFUL!)
$ find /tmp -name "*.tmp" -mtime +30 -delete
# Delete all .tmp files older than 30 days in /tmp

# Find by type
$ find /etc -type f    # f = regular files only
$ find /etc -type d    # d = directories only
$ find /etc -type l    # l = symbolic links only

# Find empty files
$ find . -type f -empty

# Find and execute a command on each result
$ find . -name "*.log" -exec rm {} \;
# For each .log file found, delete it
# {} = placeholder for the found filename
# \; = end of the -exec command
```

---

### 5.2 grep — Search INSIDE Files (THE MOST USEFUL COMMAND)

**What:** Searches for a specific text pattern inside files.

**Analogy:** You have 500 notebooks. grep is like saying "Find me every notebook that mentions 'NullPointerException' and show me the exact line."

```bash
# Basic search
$ grep "ERROR" application.log
# Shows every line containing "ERROR" in application.log

# Search in multiple files
$ grep "ERROR" *.log
# Search all .log files in current directory

# Recursive search (search in all files in all subdirectories)
$ grep -r "TODO" /home/sheetal/project/
# -r = recursive — goes into every folder

# Case-insensitive search
$ grep -i "error" application.log
# -i = ignore case — matches Error, ERROR, error, eRrOr

# Show line numbers
$ grep -n "ERROR" application.log
# -n = line number
# Output: 142:ERROR: Connection refused
#         387:ERROR: Timeout expired

# Show lines BEFORE and AFTER the match (context)
$ grep -B 3 -A 5 "NullPointerException" app.log
# -B 3 = show 3 lines Before the match
# -A 5 = show 5 lines After the match
# This is INCREDIBLY useful for debugging — you see what happened before the error

# Count matches
$ grep -c "ERROR" application.log
# -c = count — shows "47" meaning 47 lines matched

# Show only filenames that contain the match
$ grep -rl "password" /etc/
# -l = list filenames only (do not show the actual matching lines)

# Invert match (show lines that do NOT match)
$ grep -v "DEBUG" application.log
# -v = invert — shows everything EXCEPT lines with "DEBUG"

# Search for exact word (not partial matches)
$ grep -w "error" application.log
# -w = word — matches "error" but NOT "errors" or "error_handler"

# Use regex (regular expressions)
$ grep -E "ERROR|WARN|FATAL" application.log
# -E = extended regex — matches lines with ERROR OR WARN OR FATAL
```

**grep is the command you will use the MOST in your DevOps career.** Learn it well.

---

### 5.3 which — Find Where a Command Lives

**What:** Shows the full path of a command.

```bash
$ which java
/usr/bin/java

$ which python3
/usr/bin/python3

$ which mvn
/usr/local/bin/mvn
```

**When to use:** When you have multiple versions of Java or Python installed and you want to know which one is being used.

---

### 5.4 locate — Fast File Search (Uses a Database)

**What:** Finds files by name, but much faster than `find` because it uses a pre-built database.

```bash
$ locate application.properties
# Instantly shows all files named application.properties

# Update the database first (if recently created files are not showing)
$ sudo updatedb
```

**Difference from find:** `find` searches the disk in real-time (slow but accurate). `locate` searches a database (fast but may show stale results if the database is not updated).

---

## 6. File Permissions — Who Can Do What

### The Concept

**Analogy:** Think of a company office in Bangalore:

- **Owner (User)** = The person whose desk it is. They have full access to everything on their desk.
- **Group** = The team/department. Team members can see shared documents but cannot delete the owner's personal files.
- **Others** = People from other departments. They can see the notice board (read) but cannot change anything.

### Understanding Permission Strings

```
-rwxr-xr--
```

Let us break this down:

```
-    rwx    r-x    r--
|    |      |      |
|    |      |      +-- Others: read only
|    |      +-- Group: read + execute
|    +-- Owner: read + write + execute
+-- File type (- = file, d = directory)
```

Each position means:
- `r` = Read (can see the content) — value: **4**
- `w` = Write (can modify the content) — value: **2**
- `x` = Execute (can run the file as a program) — value: **1**
- `-` = No permission — value: **0**

### The Number System

Add up the values for each group:

| Permission | Calculation | Number |
|-----------|-------------|--------|
| rwx       | 4 + 2 + 1  | 7      |
| rw-       | 4 + 2 + 0  | 6      |
| r-x       | 4 + 0 + 1  | 5      |
| r--       | 4 + 0 + 0  | 4      |
| ---       | 0 + 0 + 0  | 0      |

### Common Permission Numbers

**777 — Everyone can do everything (DANGEROUS!)**

```
rwxrwxrwx = Owner: everything, Group: everything, Others: everything
```

**Analogy:** Leaving your house door wide open with a sign "Come in, take whatever you want." NEVER use 777 on a server.

**755 — Owner: full, Others: read + execute**

```
rwxr-xr-x = Owner: everything, Group: read+execute, Others: read+execute
```

**Analogy:** Your house — you can do everything. Guests can walk in and look around, but cannot move furniture. Used for: directories, scripts, program files.

**644 — Owner: read+write, Others: read only**

```
rw-r--r-- = Owner: read+write, Group: read only, Others: read only
```

**Analogy:** A company notice board — you wrote the notice (can change it), everyone else can only read it. Used for: regular files, configuration files.

**600 — Owner only, nobody else**

```
rw------- = Owner: read+write, Group: nothing, Others: nothing
```

**Analogy:** Your personal diary with a lock — only you can read or write. Used for: SSH keys, passwords, secrets.

### chmod — Change Permissions

```bash
# Using numbers
$ chmod 755 deploy.sh     # Owner: rwx, Group: r-x, Others: r-x
$ chmod 644 config.txt    # Owner: rw-, Group: r--, Others: r--
$ chmod 600 id_rsa        # Owner: rw-, Group: ---, Others: ---

# Using symbols
$ chmod +x script.sh      # Add execute permission for everyone
$ chmod u+x script.sh     # Add execute for user (owner) only
$ chmod g+w file.txt      # Add write for group
$ chmod o-r file.txt      # Remove read from others

# Recursive (apply to all files in a directory)
$ chmod -R 755 /var/www/html/
# -R = recursive — apply to everything inside
```

### chown — Change Owner

```bash
# Change owner
$ sudo chown sheetal myfile.txt

# Change owner and group
$ sudo chown sheetal:developers myfile.txt

# Recursive
$ sudo chown -R sheetal:sheetal /home/sheetal/projects/
```

### chgrp — Change Group

```bash
$ sudo chgrp developers project-folder/
```

### Common Mistakes

```bash
# Mistake 1: Setting 777 on everything
$ chmod -R 777 /var/www/
# This is a SECURITY NIGHTMARE. Anyone can modify your website files.

# Mistake 2: SSH key permissions too open
$ ssh -i my-key.pem server
# ERROR: Permissions 0644 for 'my-key.pem' are too open.
# FIX:
$ chmod 600 my-key.pem

# Mistake 3: Script won't run
$ ./deploy.sh
# bash: ./deploy.sh: Permission denied
# FIX:
$ chmod +x deploy.sh
```

---

## 7. Process Management — Controlling Running Programs

### 7.1 ps — Show Running Processes

**Analogy:** Like looking at the task manager — "What programs are currently running?"

```bash
# Show your processes
$ ps
  PID TTY          TIME CMD
 1234 pts/0    00:00:00 bash
 1256 pts/0    00:00:00 ps

# Show ALL processes (most useful form)
$ ps aux
# a = all users
# u = user-friendly format
# x = include background processes

$ ps aux | grep java
# Find all running Java processes
# Output: sheetal  12345  5.2  8.1 2048000 java -jar my-app.jar
```

**Understanding ps aux output:**

```
USER    PID  %CPU %MEM    VSZ   RSS TTY STAT START   TIME COMMAND
sheetal 1234  5.2  8.1 204800 16384 ?   Sl   10:30   1:23 java -jar app.jar
```

- **PID** = Process ID (unique number for this process — like an Aadhaar number)
- **%CPU** = How much CPU it is using
- **%MEM** = How much memory it is using
- **STAT** = Status (S=sleeping, R=running, Z=zombie)

---

### 7.2 top / htop — Live Process Monitor

```bash
$ top
# Shows a live, updating view of all processes — like a dashboard
# Press 'q' to quit
# Press 'M' to sort by memory usage
# Press 'P' to sort by CPU usage

$ htop
# Same as top but prettier and easier to use (may need to install: sudo apt install htop)
```

**Analogy:** `top` is like looking at the speedometer and fuel gauge while driving — it shows you in real-time how your system is performing.

---

### 7.3 kill — Stop a Process

```bash
# Graceful kill (asks the process to stop nicely)
$ kill 12345
# Sends SIGTERM — "Please stop when you are ready"

# Force kill (stops immediately, no questions asked)
$ kill -9 12345
# Sends SIGKILL — "STOP RIGHT NOW"
# Use this when a process is stuck and won't respond to normal kill

# Kill by name
$ killall java
# Kills ALL processes named "java"

# Kill by name (pattern match)
$ pkill -f "spring-boot"
# Kills any process whose command line contains "spring-boot"
```

**When to use `-9`:** Only when a normal `kill` does not work. The `-9` flag does not give the process a chance to clean up (close files, save state, release locks). Think of it like pulling the power plug vs. shutting down properly.

---

### 7.4 Background and Foreground Processes

```bash
# Run a command in the background
$ java -jar app.jar &
# The & puts it in the background — your terminal stays free

# Move a running process to background
# Press Ctrl + Z (pauses the process)
$ bg
# Resumes it in the background

# Bring a background process to foreground
$ fg

# List background jobs
$ jobs
```

### 7.5 nohup — Keep Running After You Disconnect

```bash
$ nohup java -jar app.jar > app.log 2>&1 &
```

**What this does:**
- `nohup` = "No Hang Up" — the process keeps running even if you close the terminal or disconnect SSH
- `> app.log` = Send output to app.log file
- `2>&1` = Send errors to the same file (we will explain this in the piping section)
- `&` = Run in background

**Analogy:** Normally, when you leave the office (close terminal), your computer shuts down all your programs. `nohup` is like telling a program: "Keep running even after I leave."

---

## 8. Disk and Memory Commands

### 8.1 df — Disk Free Space

```bash
$ df -h
Filesystem      Size  Used Avail Use% Mounted on
/dev/sda1        50G   35G   15G  70% /
/dev/sdb1       200G  120G   80G  60% /data
```

- `-h` = human readable (shows GB, MB instead of raw bytes)

**When to use:** When your application crashes with "No space left on device" — this tells you which disk is full.

---

### 8.2 du — Disk Usage (Who is Using the Space?)

```bash
$ du -sh /var/log/
2.5G    /var/log/

$ du -sh /var/log/*
1.2G    /var/log/syslog
800M    /var/log/application.log
500M    /var/log/nginx/

# Sort by size (find the biggest space hogs)
$ du -sh /var/log/* | sort -rh | head -10
# -r = reverse (biggest first)
# -h = human-readable sort
```

- `-s` = summary (total for each argument, not every subfolder)
- `-h` = human readable

---

### 8.3 free — Memory Usage

```bash
$ free -m
              total        used        free      shared  buff/cache   available
Mem:           7982        3421        1205         234        3356        4027
Swap:          2048         128        1920
```

- `-m` = show in megabytes
- `-g` = show in gigabytes

**Key thing to understand:** "available" is more important than "free". Linux uses free memory for caching (buff/cache) which it can release when needed. So if "free" is low but "available" is high, you are fine.

**Analogy:** Your fridge (RAM) has 8 shelves. 3 are used for today's meals (used). 1 is empty (free). 4 have yesterday's leftovers that you can throw out anytime to make space (buff/cache). So you actually have 5 shelves available = 1 free + 4 cache.

---

## 9. User Management

### 9.1 whoami — Who Am I?

```bash
$ whoami
sheetal
```

---

### 9.2 sudo — Super User Do (Admin Power)

**Analogy:** In an office, you are a regular employee. `sudo` is like temporarily getting the boss's master key card — you can open any door, but you need to enter a password first.

```bash
$ sudo apt update            # Run a command as admin
$ sudo vim /etc/hosts        # Edit a system file that needs admin access
```

---

### 9.3 su — Switch User

```bash
$ su - john                  # Switch to user "john" (needs john's password)
$ sudo su -                  # Switch to root user (the ultimate admin)
```

---

### 9.4 useradd and passwd

```bash
# Create a new user
$ sudo useradd -m -s /bin/bash devuser
# -m = create home directory
# -s /bin/bash = set default shell to bash

# Set password for the new user
$ sudo passwd devuser
# Enter new password: ****
# Retype new password: ****

# Add user to a group
$ sudo usermod -aG docker devuser
# -aG = append to group (do not remove from other groups)
```

---

## 10. Package Management — Installing Software

### Analogy

Package managers are like app stores for Linux:
- **apt** (Ubuntu/Debian) = Google Play Store
- **yum** (CentOS/RHEL) = Apple App Store

### apt (Ubuntu/Debian)

```bash
# Update the package list (like refreshing the app store)
$ sudo apt update

# Upgrade all installed packages to latest versions
$ sudo apt upgrade

# Install a package
$ sudo apt install nginx
$ sudo apt install openjdk-17-jdk

# Remove a package
$ sudo apt remove nginx

# Search for a package
$ apt search docker

# Show package info
$ apt show nginx
```

### yum (CentOS/RHEL/Amazon Linux)

```bash
$ sudo yum update
$ sudo yum install nginx
$ sudo yum remove nginx
$ yum search docker
```

---

## 11. Text Processing Commands

These commands are INCREDIBLY useful for processing log files, CSV data, and configuration files.

### 11.1 wc — Word Count

```bash
$ wc access.log
  10542  84336 1048576 access.log
# Lines  Words  Bytes  Filename

$ wc -l access.log    # Count lines only
10542 access.log

$ cat error.log | grep "ERROR" | wc -l
47
# "How many ERROR lines are in the error log?" Answer: 47
```

---

### 11.2 sort — Sort Lines

```bash
$ sort names.txt              # Sort alphabetically
$ sort -r names.txt           # Reverse sort (Z to A)
$ sort -n numbers.txt         # Numeric sort (1, 2, 10 not 1, 10, 2)
$ sort -t',' -k2 data.csv    # Sort CSV by 2nd column
# -t',' = field separator is comma
# -k2 = sort by column 2
```

---

### 11.3 uniq — Remove Duplicate Lines

```bash
# IMPORTANT: uniq only removes ADJACENT duplicates, so sort first!
$ sort access.log | uniq
$ sort error-codes.txt | uniq -c | sort -rn
# -c = count occurrences
# This shows the most common error codes at the top
```

---

### 11.4 cut — Extract Columns

```bash
# Extract specific columns from delimited data
$ cut -d',' -f1,3 employees.csv
# -d',' = delimiter is comma
# -f1,3 = fields 1 and 3

$ cut -d':' -f1 /etc/passwd
# Get all usernames (first field in /etc/passwd, colon-separated)
```

---

### 11.5 awk — Pattern Scanning and Processing

**awk is like a mini programming language for text.** Here are the basics:

```bash
# Print specific columns (awk uses spaces as default separator)
$ awk '{print $1}' access.log
# Print the first column (IP address in an access log)

$ awk '{print $1, $9}' access.log
# Print columns 1 (IP) and 9 (status code)

# Custom separator
$ awk -F',' '{print $2}' data.csv
# -F',' = field separator is comma

# Filter rows
$ awk '$9 == 500 {print $0}' access.log
# Print lines where column 9 (status code) is 500

# Sum a column
$ awk '{sum += $10} END {print sum}' access.log
# Sum all values in column 10 (response size) and print the total
```

---

### 11.6 sed — Stream Editor (Find and Replace)

```bash
# Replace text in a file
$ sed 's/old-text/new-text/' file.txt
# Replace first occurrence on each line

$ sed 's/old-text/new-text/g' file.txt
# g = global — replace ALL occurrences on each line

# Edit file in-place (modify the actual file)
$ sed -i 's/localhost/192.168.1.100/g' config.properties
# -i = in-place edit

# Delete lines matching a pattern
$ sed '/DEBUG/d' application.log
# Delete all lines containing "DEBUG"

# Print specific line numbers
$ sed -n '10,20p' file.txt
# -n = suppress default output
# p = print only lines 10 to 20
```

---

## 12. Piping and Redirection — Connecting Commands Together

### The Pipe `|` — Output of One Command Becomes Input of the Next

**Analogy:** Like a factory assembly line. The first worker cuts the cloth, passes it to the second worker who stitches, passes it to the third who irons, and the finished product comes out.

```bash
# Find how many ERROR lines are in the log
$ cat access.log | grep "ERROR" | wc -l

# Find the top 10 IP addresses hitting your server
$ cat access.log | awk '{print $1}' | sort | uniq -c | sort -rn | head -10

# Find large files and sort by size
$ du -sh /var/log/* | sort -rh | head -5
```

### Output Redirection

```bash
# > (overwrite) — Write output to a file (REPLACES existing content)
$ echo "Hello" > greeting.txt
$ ls -la > file-list.txt

# >> (append) — Add output to end of file (KEEPS existing content)
$ echo "World" >> greeting.txt
$ date >> logfile.txt

# Difference:
$ echo "Line 1" > file.txt    # file contains: Line 1
$ echo "Line 2" > file.txt    # file contains: Line 2 (Line 1 is GONE!)
$ echo "Line 3" >> file.txt   # file contains: Line 2 and Line 3
```

### Error Redirection

In Linux, there are two types of output:
- **stdout (1)** = Standard output (normal messages)
- **stderr (2)** = Standard error (error messages)

```bash
# Redirect only errors to a file
$ java -jar app.jar 2> errors.log
# 2> = redirect stderr to errors.log

# Redirect both output and errors to the same file
$ java -jar app.jar > app.log 2>&1
# > app.log = send stdout to app.log
# 2>&1 = send stderr to the same place as stdout

# Discard all output (send to /dev/null — the black hole)
$ command > /dev/null 2>&1
# Nothing appears on screen, nothing saved — everything is discarded
```

**Analogy for /dev/null:** It is like a dustbin with no bottom — anything you throw in disappears forever. Used when you want to run a command but do not care about its output.

### Real-World Pipeline Example

Scenario: Your Java application has a 10 GB access log file. You need to find how many unique IP addresses caused 500 (Internal Server Error) responses in the last hour.

```bash
$ tail -10000 access.log | grep " 500 " | awk '{print $1}' | sort | uniq -c | sort -rn | head -20
```

What this does, step by step:
1. `tail -10000 access.log` — Take the last 10,000 lines (roughly last hour)
2. `grep " 500 "` — Keep only lines with status code 500
3. `awk '{print $1}'` — Extract just the IP address (first column)
4. `sort` — Sort the IPs alphabetically
5. `uniq -c` — Count how many times each IP appears
6. `sort -rn` — Sort by count, highest first
7. `head -20` — Show top 20

---

## 13. Linux File System — Rooms in the House

**Analogy:** Think of the Linux file system like a big house with many rooms, each with a specific purpose.

```
/                    The main entrance (root of everything)
├── /home            Bedrooms — each user has their own room
│   ├── /home/sheetal
│   └── /home/john
├── /etc             Study room — all configuration files live here
│   ├── /etc/nginx/
│   ├── /etc/hosts
│   └── /etc/passwd
├── /var             Storage room — files that change often (logs, databases)
│   ├── /var/log/    All log files
│   ├── /var/www/    Web server files
│   └── /var/lib/    Application data (databases, Docker)
├── /tmp             Guest room — temporary files (cleaned on reboot)
├── /usr             Library — user programs and documentation
│   ├── /usr/bin/    User commands (like git, java, python)
│   └── /usr/local/  Manually installed software
├── /opt             Extra storage room — optional/third-party software
│   └── /opt/tomcat/
├── /bin             Tool shed — essential commands (ls, cp, mv)
├── /sbin            Admin tool shed — system admin commands
├── /dev             Device connections (hard disks, USB)
├── /proc            System info dashboard (virtual files showing system state)
├── /root            The owner's master bedroom (root user's home)
└── /boot            The engine room (kernel and boot files)
```

### Key Directories for DevOps

| Directory | What You Will Find There |
|-----------|------------------------|
| `/etc/nginx/` | Nginx web server configuration |
| `/etc/hosts` | Manual DNS entries (like a personal phone directory) |
| `/var/log/` | ALL log files (system, application, web server) |
| `/var/log/syslog` | System-wide log |
| `/opt/` | Where you install Tomcat, Jenkins, etc. |
| `/tmp/` | Temporary files — do NOT store important data here |
| `/home/sheetal/.ssh/` | Your SSH keys |
| `/etc/crontab` | Scheduled tasks |

---

## 14. Shell Scripting — Automating Your Work

### What is Shell Scripting?

**Analogy:** Instead of cooking the same recipe every morning (typing the same commands every day), you write the recipe down once and say "follow this recipe every morning." That recipe is a shell script.

### Your First Script

Create a file called `hello.sh`:

```bash
#!/bin/bash
# This is a comment — it explains what the script does
# The first line (#!/bin/bash) tells Linux which interpreter to use

echo "Hello, I am a shell script!"
echo "Today's date is: $(date)"
echo "You are logged in as: $(whoami)"
echo "You are in: $(pwd)"
```

Run it:
```bash
$ chmod +x hello.sh    # Make it executable
$ ./hello.sh           # Run it
```

### Variables

```bash
#!/bin/bash

# Defining variables (NO spaces around the = sign!)
NAME="Sheetal"
APP_VERSION="2.5.1"
LOG_DIR="/var/log/myapp"

# Using variables (use $ prefix)
echo "Hello, $NAME"
echo "Deploying version $APP_VERSION"
echo "Logs will be in $LOG_DIR"

# Command output as a variable
CURRENT_DATE=$(date +"%Y-%m-%d")
SERVER_IP=$(hostname -I | awk '{print $1}')

echo "Date: $CURRENT_DATE"
echo "Server IP: $SERVER_IP"
```

**Common Mistake:**
```bash
NAME = "Sheetal"    # WRONG! Spaces around = cause an error
NAME="Sheetal"      # CORRECT!
```

### If/Else — Making Decisions

```bash
#!/bin/bash

DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')

if [ "$DISK_USAGE" -gt 80 ]; then
    echo "WARNING: Disk usage is ${DISK_USAGE}% — running cleanup!"
    # Clean old log files
    find /var/log -name "*.gz" -mtime +30 -delete
elif [ "$DISK_USAGE" -gt 60 ]; then
    echo "NOTICE: Disk usage is ${DISK_USAGE}% — keep an eye on it."
else
    echo "OK: Disk usage is ${DISK_USAGE}% — all good."
fi
```

**Comparison operators:**
- `-gt` = greater than
- `-lt` = less than
- `-eq` = equal to
- `-ne` = not equal to
- `-ge` = greater than or equal to
- `-le` = less than or equal to
- For strings: `=` (equal), `!=` (not equal)

### For Loops

```bash
#!/bin/bash

# Loop through a list
for SERVER in web1 web2 web3 db1 db2; do
    echo "Checking server: $SERVER"
    ping -c 1 $SERVER > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "  $SERVER is UP"
    else
        echo "  $SERVER is DOWN!"
    fi
done

# Loop through files
for FILE in /var/log/*.log; do
    SIZE=$(du -sh "$FILE" | awk '{print $1}')
    echo "$FILE — $SIZE"
done

# Loop with numbers
for i in {1..10}; do
    echo "Iteration $i"
done
```

### Functions

```bash
#!/bin/bash

# Define a function
check_service() {
    SERVICE_NAME=$1  # $1 = first argument passed to the function

    if systemctl is-active --quiet "$SERVICE_NAME"; then
        echo "[OK] $SERVICE_NAME is running"
    else
        echo "[FAIL] $SERVICE_NAME is NOT running!"
        echo "Attempting to restart $SERVICE_NAME..."
        sudo systemctl restart "$SERVICE_NAME"
    fi
}

# Call the function
check_service nginx
check_service mysql
check_service docker
```

### A Practical Backup Script

```bash
#!/bin/bash
# backup.sh — Backup application files and database dump

# Configuration
APP_DIR="/opt/myapp"
BACKUP_DIR="/backups"
DATE=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="$BACKUP_DIR/myapp_backup_$DATE.tar.gz"
MAX_BACKUPS=7   # Keep only the last 7 backups to save disk space

# Step 1: Create backup directory if it does not exist
mkdir -p "$BACKUP_DIR"

# Step 2: Create the backup
echo "Starting backup at $(date)"
tar -czf "$BACKUP_FILE" "$APP_DIR"

# Step 3: Check if backup was successful
if [ $? -eq 0 ]; then
    BACKUP_SIZE=$(du -sh "$BACKUP_FILE" | awk '{print $1}')
    echo "Backup successful: $BACKUP_FILE ($BACKUP_SIZE)"
else
    echo "BACKUP FAILED!"
    exit 1
fi

# Step 4: Remove old backups (keep only the last MAX_BACKUPS)
BACKUP_COUNT=$(ls -1 "$BACKUP_DIR"/myapp_backup_*.tar.gz 2>/dev/null | wc -l)
if [ "$BACKUP_COUNT" -gt "$MAX_BACKUPS" ]; then
    echo "Cleaning old backups (keeping last $MAX_BACKUPS)..."
    ls -1t "$BACKUP_DIR"/myapp_backup_*.tar.gz | tail -n +$((MAX_BACKUPS + 1)) | xargs rm -f
fi

echo "Backup complete at $(date)"
```

---

## 15. Cron Jobs — Scheduling Tasks

### What is Cron?

**Analogy:** Cron is like setting an alarm on your phone. "Every morning at 6 AM, play this song." Similarly, cron says "Every night at 2 AM, run this backup script."

### Crontab Syntax

```
* * * * * command_to_run
| | | | |
| | | | +-- Day of week (0=Sunday, 1=Monday, ..., 6=Saturday)
| | | +---- Month (1-12)
| | +------ Day of month (1-31)
| +-------- Hour (0-23)
+---------- Minute (0-59)
```

### Examples

```bash
# Edit your crontab
$ crontab -e

# View your current crontab
$ crontab -l

# Run backup every day at 2:00 AM
0 2 * * * /home/sheetal/scripts/backup.sh >> /var/log/backup.log 2>&1

# Clear temp files every Sunday at midnight
0 0 * * 0 find /tmp -mtime +7 -delete

# Check disk space every 30 minutes
*/30 * * * * /home/sheetal/scripts/check-disk.sh

# Run every 5 minutes
*/5 * * * * /home/sheetal/scripts/health-check.sh

# Run at 9:30 AM on weekdays only (Mon-Fri)
30 9 * * 1-5 /home/sheetal/scripts/morning-report.sh

# Run on the 1st of every month at midnight
0 0 1 * * /home/sheetal/scripts/monthly-cleanup.sh
```

### Common Mistake with Cron

The MOST common mistake: your script works when you run it manually but FAILS in cron. This happens because cron runs in a minimal environment without your usual PATH settings.

```bash
# WRONG (may fail in cron because 'java' is not in cron's PATH):
0 2 * * * java -jar /opt/app/cleanup.jar

# CORRECT (use full path to java):
0 2 * * * /usr/bin/java -jar /opt/app/cleanup.jar

# Or set PATH at the top of crontab:
PATH=/usr/local/bin:/usr/bin:/bin
0 2 * * * java -jar /opt/app/cleanup.jar
```

---

---

# PART 2: NETWORKING

---

## 16. How the Internet Works

### The Letter Sending Analogy

Imagine sending a letter from Mumbai to Delhi:

1. **You write a letter** (the data you want to send — like typing google.com)
2. **You put it in an envelope** (the data gets wrapped in a **packet**)
3. **You write the address on the envelope** (the **IP address** — like 142.250.190.78)
4. **You drop it at the post office** (your **router** — the device that knows where to send things)
5. **The post office looks up the best route** (the router decides the fastest path)
6. **The letter travels through multiple post offices** (multiple **routers** across the internet)
7. **It arrives at the destination post office** (Google's server receives the packet)
8. **Google reads your letter, writes a reply, and sends it back the same way** (the response)

### Key Mapping

| Real Life           | Networking Term   | Example                     |
|--------------------|-------------------|-----------------------------|
| House address      | IP Address        | 192.168.1.100              |
| Envelope           | Packet            | Contains your data          |
| Post office        | Router            | Forwards packets to the next stop |
| Phone directory    | DNS               | Converts "google.com" to 142.250.190.78 |
| Apartment number   | Port              | :80 (HTTP), :443 (HTTPS)   |
| Language you write in | Protocol       | HTTP, HTTPS, SSH, FTP       |

---

## 17. IP Addresses — The Address System of the Internet

### What is an IP Address?

Every device connected to a network has a unique address, just like every house has a unique postal address. This address is the **IP address** (Internet Protocol address).

### IPv4 Format

```
192.168.1.100
```

Four numbers separated by dots. Each number ranges from 0 to 255.

**Total possible IPv4 addresses:** About 4.3 billion. That sounds like a lot, but with billions of devices worldwide, we are running out — which is why IPv6 was invented (but IPv4 is still dominant in daily use).

### Public vs Private IP

**Public IP** — Your address on the internet (visible to the whole world).

**Analogy:** Like your office building's address — "Infosys Building, Electronic City, Bangalore." Anyone in the world can send a letter to this address.

**Private IP** — Your address inside your local network (visible only within your home/office).

**Analogy:** Like your desk number inside the office — "Desk 42, 3rd Floor." Only people inside the building know this address. People outside the building cannot directly reach Desk 42 — they send mail to the building address, and the receptionist (router) delivers it to your desk.

**Private IP ranges (memorize these):**

| Range                         | Common Use                    |
|-------------------------------|-------------------------------|
| 10.0.0.0 - 10.255.255.255    | Large corporate networks      |
| 172.16.0.0 - 172.31.255.255  | Medium networks               |
| 192.168.0.0 - 192.168.255.255| Home and small office networks|

### Special IP Addresses

**`127.0.0.1` (localhost)** — This always means "this computer itself."

**Analogy:** Like saying "my own house." No matter who says it, it refers to where they are standing right now.

```bash
# These all mean the same thing — "connect to myself"
$ curl http://127.0.0.1:8080
$ curl http://localhost:8080
```

**`0.0.0.0`** — "Listen on ALL network interfaces."

**Analogy:** If localhost is "my own room," then 0.0.0.0 is "all the doors and windows of my house." When your server binds to 0.0.0.0, it accepts connections from everywhere — from inside the computer AND from outside.

```properties
# In your Spring Boot application.properties:
server.address=0.0.0.0    # Accept connections from any IP
server.address=127.0.0.1  # Accept connections ONLY from this machine
```

---

## 18. Ports — Apartment Numbers Inside a Building

### What is a Port?

An IP address tells you which building (computer) to go to. But a computer runs many services — web server, database, SSH, etc. How does the network know which service you want? **Ports!**

**Analogy:**

- **IP address = Building** (e.g., "Prestige Tech Park")
- **Port = Apartment/Office number** (e.g., "Suite 443")

So `192.168.1.100:8080` means "Go to building 192.168.1.100 and knock on door number 8080."

### Common Ports (MUST Memorize)

| Port | Service        | Analogy                               |
|------|---------------|---------------------------------------|
| 22   | SSH           | The back door (secure remote access)  |
| 80   | HTTP          | The main entrance (regular web)       |
| 443  | HTTPS         | The main entrance with security guard (encrypted web) |
| 3306 | MySQL         | The database storage room             |
| 5432 | PostgreSQL    | Another database storage room         |
| 6379 | Redis         | The quick-access cache shelf          |
| 8080 | Tomcat/Alt HTTP| The side entrance (Java apps often run here) |
| 27017| MongoDB       | Another database room                 |
| 9092 | Kafka         | The message queue room                |

### Port Ranges

| Range         | Name        | Description                          |
|---------------|-------------|--------------------------------------|
| 0 - 1023      | Well-known  | Reserved for standard services (need root to use) |
| 1024 - 49151  | Registered  | Used by applications (Tomcat, Node.js, etc.) |
| 49152 - 65535 | Dynamic     | Temporary ports for client connections |

### Common Mistake

Your Java app runs on port 8080 locally, but when you deploy to a server, users still type `http://yoursite.com` (which goes to port 80 by default). You need a reverse proxy (like Nginx) that listens on port 80 and forwards traffic to port 8080.

```
User's browser --> yoursite.com:80 (Nginx) --> localhost:8080 (Tomcat/Spring Boot)
```

---

## 19. DNS — The Internet's Phone Directory

### What is DNS?

When you type `google.com` in your browser, the computer needs to find Google's IP address (like 142.250.190.78). DNS (Domain Name System) is the system that converts human-friendly names to IP addresses.

**Analogy:** You want to call your friend Rahul. You do not remember his phone number. So you open your phone's contact list (DNS), search for "Rahul," and find his number (IP address). Now you can make the call.

### How DNS Resolution Works — Step by Step

When you type `www.flipkart.com` in your browser:

```
Step 1: Browser checks its own cache
        "Have I visited this site recently? Do I already know the IP?"
        If yes → use the cached IP. Done.

Step 2: OS checks its local cache and /etc/hosts file
        /etc/hosts is like a personal phone directory — you can manually add entries
        $ cat /etc/hosts
        127.0.0.1    localhost
        192.168.1.50  mydb-server    # Custom entry

Step 3: Ask the DNS Resolver (usually your ISP's DNS or 8.8.8.8)
        "Hey resolver, what is the IP for www.flipkart.com?"

Step 4: Resolver asks Root DNS Server
        "I do not know. Let me ask the top authority."
        Root says: "I do not know flipkart.com, but .com domains are handled
                    by this server. Go ask them."

Step 5: Resolver asks .com TLD (Top Level Domain) Server
        ".com server, where is flipkart.com?"
        TLD says: "flipkart.com is managed by these nameservers:
                   ns1.flipkart.com, ns2.flipkart.com"

Step 6: Resolver asks Flipkart's Nameserver
        "What is the IP of www.flipkart.com?"
        Nameserver says: "163.53.78.128"

Step 7: Resolver returns the IP to your browser
        Browser connects to 163.53.78.128 and loads the website

Step 8: Result is cached for future use (typically 5 minutes to 24 hours)
```

### DNS Record Types

| Record Type | Purpose | Example |
|------------|---------|---------|
| A          | Maps domain to IPv4 address | flipkart.com → 163.53.78.128 |
| AAAA       | Maps domain to IPv6 address | google.com → 2404:6800:4009:... |
| CNAME      | Alias (one domain points to another) | www.flipkart.com → flipkart.com |
| MX         | Mail server | flipkart.com → mail.flipkart.com |
| NS         | Nameserver | flipkart.com → ns1.flipkart.com |
| TXT        | Text info (verification, SPF) | Used for email security |

---

## 20. HTTP/HTTPS — How the Web Communicates

### What is HTTP?

HTTP (HyperText Transfer Protocol) is the language browsers and servers use to talk to each other.

**Analogy:** When you go to a restaurant:
- You (browser) look at the menu and say "I want butter chicken" (HTTP **request**)
- The waiter goes to the kitchen (server), gets the butter chicken, and brings it back (HTTP **response**)

### HTTP Request

Every HTTP request has:
```
GET /api/users/123 HTTP/1.1       <-- Method, Path, Version
Host: api.myapp.com               <-- Which server
Authorization: Bearer abc123xyz   <-- Who you are (authentication)
Content-Type: application/json    <-- What format the data is in
```

### HTTP Methods

| Method  | Purpose        | Analogy                          |
|---------|---------------|----------------------------------|
| GET     | Read/fetch data | "Show me the menu"              |
| POST    | Create new data | "I want to order butter chicken" |
| PUT     | Replace data entirely | "Change my entire order to biryani" |
| PATCH   | Update part of data | "Add extra raita to my order" |
| DELETE  | Remove data    | "Cancel my order"               |

### HTTP Status Codes (MUST Know)

**2xx — Success (green light)**
| Code | Meaning | When |
|------|---------|------|
| 200  | OK | Everything worked |
| 201  | Created | New resource created (after POST) |
| 204  | No Content | Success, but nothing to return (after DELETE) |

**3xx — Redirection (yellow light)**
| Code | Meaning | When |
|------|---------|------|
| 301  | Moved Permanently | Old URL has permanently moved to new URL |
| 302  | Found (Temporary Redirect) | Temporary redirect |
| 304  | Not Modified | Use your cached version, nothing changed |

**4xx — Client Error (your mistake)**
| Code | Meaning | When |
|------|---------|------|
| 400  | Bad Request | You sent invalid data |
| 401  | Unauthorized | You are not logged in |
| 403  | Forbidden | You are logged in but do not have permission |
| 404  | Not Found | The page/resource does not exist |
| 405  | Method Not Allowed | You used GET instead of POST, etc. |
| 429  | Too Many Requests | Slow down! You are making too many requests |

**5xx — Server Error (server's mistake)**
| Code | Meaning | When |
|------|---------|------|
| 500  | Internal Server Error | Something broke on the server |
| 502  | Bad Gateway | The proxy/load balancer cannot reach the backend |
| 503  | Service Unavailable | Server is overloaded or down for maintenance |
| 504  | Gateway Timeout | The backend took too long to respond |

### HTTP vs HTTPS

- **HTTP** = Your conversation with the server is in **plain text** — anyone in the middle can read it. Like shouting your credit card number in a crowded bus.
- **HTTPS** = Your conversation is **encrypted** — even if someone intercepts it, they cannot read it. Like whispering into the server's ear.

HTTPS uses SSL/TLS certificates to encrypt the communication. Every modern website should use HTTPS.

---

## 21. TCP vs UDP — Two Ways to Send Data

### TCP (Transmission Control Protocol) — The Reliable Way

**Analogy:** Sending a package via India Post with tracking:
- You get confirmation that the package was delivered
- If it gets lost, it is sent again
- Packages arrive in order (package 1, then 2, then 3)
- Slower because of all the checking

Used for: Web browsing (HTTP/HTTPS), email, file transfer, SSH — anything where you cannot afford to lose data.

### UDP (User Datagram Protocol) — The Fast Way

**Analogy:** Shouting across a cricket field:
- You shout "CATCH!" — maybe the other person hears, maybe not
- You do not wait for confirmation
- Some words might get lost in the wind
- Much faster because there is no back-and-forth

Used for: Video calls (Zoom), live streaming, online gaming, DNS lookups — where speed matters more than perfection. If a few video frames are lost, you do not want to wait for them — you just skip ahead.

### Comparison

| Feature          | TCP              | UDP            |
|-----------------|------------------|----------------|
| Reliable?       | Yes (confirms delivery) | No (best effort) |
| Ordered?        | Yes              | No             |
| Speed           | Slower           | Faster         |
| Connection      | Connection-based | Connectionless |
| Use case        | Web, email, SSH  | Video, gaming, DNS |

---

## 22. SSH — Secure Remote Access

### What is SSH?

SSH (Secure Shell) lets you log into a remote server and control it from your terminal, securely.

**Analogy:** Imagine you have an office in Delhi but you are sitting at home in Mumbai. SSH is like having a secure, encrypted phone line to your Delhi office where you can give commands and your Delhi team executes them.

### Basic SSH Connection

```bash
$ ssh username@server-ip
$ ssh sheetal@192.168.1.50
$ ssh ubuntu@54.210.123.45    # Connecting to AWS EC2
```

### SSH with Key-Based Authentication

Password authentication is insecure. Key-based authentication is the standard.

**Analogy:** Instead of remembering a password (which can be guessed), you have a special key pair:
- **Private key** = Your house key (kept secret, never shared) — stored on YOUR computer
- **Public key** = The lock on the door (given to the server) — stored on the SERVER

Only your private key can open the lock. Even if someone sees the lock, they cannot make a key for it.

```bash
# Step 1: Generate SSH key pair
$ ssh-keygen -t ed25519 -C "sheetal@example.com"
# -t ed25519 = key type (modern and secure)
# -C = comment (usually your email, for identification)
# This creates:
#   ~/.ssh/id_ed25519       (private key — NEVER share this)
#   ~/.ssh/id_ed25519.pub   (public key — safe to share)

# Step 2: Copy public key to the server
$ ssh-copy-id username@server-ip
# This adds your public key to the server's ~/.ssh/authorized_keys file

# Step 3: Now you can connect without a password
$ ssh username@server-ip
# No password needed! Your private key authenticates you automatically.
```

### SSH with a Specific Key File (like AWS .pem files)

```bash
$ ssh -i ~/keys/my-aws-key.pem ubuntu@54.210.123.45
# -i = identity file (path to the private key)

# Remember: key file must have strict permissions!
$ chmod 600 ~/keys/my-aws-key.pem
```

### SCP — Secure Copy (Transfer Files)

```bash
# Copy file FROM your computer TO the server
$ scp local-file.txt username@server:/path/to/destination/

# Copy file FROM the server TO your computer
$ scp username@server:/var/log/app.log /tmp/

# Copy an entire directory
$ scp -r my-project/ username@server:/opt/apps/
# -r = recursive (copies everything inside the directory)
```

### SSH Config File — No More Typing Long Commands

Instead of typing `ssh -i ~/keys/my-key.pem ubuntu@54.210.123.45` every time, create a config file:

```bash
$ nano ~/.ssh/config
```

```
Host my-server
    HostName 54.210.123.45
    User ubuntu
    IdentityFile ~/keys/my-aws-key.pem

Host db-server
    HostName 10.0.1.50
    User admin
    IdentityFile ~/keys/db-key.pem
```

Now you can simply type:
```bash
$ ssh my-server
$ ssh db-server
```

---

## 23. curl — Testing APIs from Terminal

### What is curl?

curl is a command-line tool to make HTTP requests. As a Java developer, you use Postman to test APIs. curl does the same thing but from the terminal.

**Why curl over Postman?** Because on a server (which has no GUI), you cannot open Postman. You only have the terminal. curl is your API testing tool on servers.

```bash
# Simple GET request
$ curl https://api.github.com
# Returns the response body (usually JSON)

# See response headers
$ curl -I https://google.com
# -I = head only (shows headers, not the body)

# See everything (verbose mode — useful for debugging)
$ curl -v https://api.github.com
# -v = verbose — shows the full request and response including headers, SSL handshake, etc.

# POST request with JSON data
$ curl -X POST https://api.example.com/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Sheetal", "email": "sheetal@example.com"}'
# -X POST = HTTP method
# -H = Header
# -d = Data (request body)

# PUT request
$ curl -X PUT https://api.example.com/users/123 \
  -H "Content-Type: application/json" \
  -d '{"name": "Sheetal Updated"}'

# DELETE request
$ curl -X DELETE https://api.example.com/users/123

# With authentication
$ curl -H "Authorization: Bearer your-token-here" https://api.example.com/protected

# Follow redirects
$ curl -L https://short-url.com
# -L = follow redirects (3xx responses)

# Save response to a file
$ curl -o output.html https://example.com
# -o = output to file

# Download a file (keeps the original filename)
$ curl -O https://example.com/installer.tar.gz
# -O = remote filename

# Silent mode (no progress bar)
$ curl -s https://api.example.com/health
# -s = silent

# Show only the HTTP status code
$ curl -s -o /dev/null -w "%{http_code}" https://api.example.com/health
# -o /dev/null = discard the body
# -w "%{http_code}" = print just the status code
# Output: 200
```

---

## 24. Network Commands

### 24.1 ping — Is the Server Alive?

**Analogy:** Knocking on someone's door — "Hello, are you there?"

```bash
$ ping google.com
PING google.com (142.250.190.78): 56 data bytes
64 bytes from 142.250.190.78: icmp_seq=0 ttl=118 time=12.3 ms
64 bytes from 142.250.190.78: icmp_seq=1 ttl=118 time=11.8 ms
# Press Ctrl+C to stop

# Send only 4 pings
$ ping -c 4 google.com
# -c 4 = count (send 4 pings and stop)
```

**What the output tells you:**
- `time=12.3 ms` — Round-trip time (lower is better; under 50ms is good for Indian servers)
- `ttl=118` — Time To Live (how many hops the packet can make before being discarded)
- If you get "Request timeout" — the server is down or blocking pings

---

### 24.2 traceroute — The Route Your Data Takes

**Analogy:** If ping is "Are you there?", traceroute is "Show me every post office my letter passed through to reach you."

```bash
$ traceroute google.com
 1  192.168.1.1 (192.168.1.1)  1.234 ms   # Your router
 2  10.0.0.1 (10.0.0.1)  5.678 ms         # ISP's first router
 3  72.14.215.85  12.345 ms               # ISP's backbone
 4  ...                                    # More hops
 8  142.250.190.78  15.678 ms             # Google's server

# On some systems, use tracepath instead:
$ tracepath google.com
```

**When to use:** When a server is slow and you want to find WHERE the delay is happening.

---

### 24.3 netstat — Network Statistics

Shows active network connections and listening ports.

```bash
# Show all listening ports
$ netstat -tlnp
# -t = TCP connections
# -l = listening (waiting for connections)
# -n = show numbers (not hostnames — faster)
# -p = show process using the port

# Output:
Proto  Local Address    Foreign Address  State    PID/Program name
tcp    0.0.0.0:22      0.0.0.0:*       LISTEN   1234/sshd
tcp    0.0.0.0:80      0.0.0.0:*       LISTEN   5678/nginx
tcp    0.0.0.0:8080    0.0.0.0:*       LISTEN   9012/java

# Modern alternative: ss (socket statistics)
$ ss -tlnp
# Same output, but faster
```

**When to use:** "Is my Java app actually running and listening on port 8080?"

---

### 24.4 nslookup — Query DNS

```bash
$ nslookup google.com
Server:     8.8.8.8
Address:    8.8.8.8#53

Non-authoritative answer:
Name:   google.com
Address: 142.250.190.78
```

**When to use:** To check what IP address a domain name resolves to.

---

### 24.5 dig — DNS Lookup (More Detailed)

```bash
$ dig google.com

;; ANSWER SECTION:
google.com.     300     IN      A       142.250.190.78

# Query specific record type
$ dig google.com MX        # Mail server records
$ dig google.com NS        # Name server records
$ dig google.com CNAME     # Alias records

# Short answer only
$ dig +short google.com
142.250.190.78
```

**When to use:** When you need detailed DNS information for debugging. `dig` gives you more info than `nslookup`.

---

### 24.6 ifconfig / ip addr — Your Network Configuration

```bash
# Old way (may need to install: sudo apt install net-tools)
$ ifconfig
eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>
      inet 192.168.1.100  netmask 255.255.255.0  broadcast 192.168.1.255

# Modern way
$ ip addr
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP>
    inet 192.168.1.100/24 brd 192.168.1.255 scope global eth0
```

**When to use:** "What is my IP address on this machine?"

---

---

# PRACTICE EXERCISES

---

## Exercise 1: Linux Navigation (Beginner)

```
1. Open your terminal and find out where you are (which directory)
2. Go to your home directory
3. Create the following directory structure:
   devops-practice/
   ├── linux/
   │   ├── scripts/
   │   └── logs/
   ├── networking/
   └── docker/
4. Navigate to devops-practice/linux/scripts/
5. Print your current location to confirm
6. Go back to devops-practice/ using a relative path
7. List ALL files in your home directory (including hidden ones) in long format
```

---

## Exercise 2: File Operations (Beginner)

```
1. Inside devops-practice/linux/, create a file called notes.txt
2. Write "Learning Linux for DevOps" into notes.txt (use echo and redirection)
3. Add another line: "Day 1: Basic commands" (use append, not overwrite!)
4. View the contents of notes.txt
5. Make a copy of notes.txt called notes-backup.txt
6. Rename notes-backup.txt to backup.txt
7. Create 5 empty files: log1.txt, log2.txt, log3.txt, log4.txt, log5.txt
8. Delete only log3.txt
9. List all .txt files in the current directory
```

---

## Exercise 3: grep and find (Intermediate)

```
1. Create a file called application.log with the following content:
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

2. Count how many ERROR lines are in the file
3. Show all WARN and ERROR lines
4. Show all ERROR lines with 2 lines of context before and after
5. Show all lines that are NOT DEBUG level
6. Count how many lines of each log level exist (hint: use grep, sort, uniq)
7. Find all .txt files in your devops-practice directory
8. Find all files larger than 1KB in your home directory
```

---

## Exercise 4: Permissions (Intermediate)

```
1. Create a script called check-server.sh in your scripts directory:
   #!/bin/bash
   echo "Server check at: $(date)"
   echo "Hostname: $(hostname)"
   echo "Uptime: $(uptime)"
   echo "Disk usage:"
   df -h /
   echo "Memory:"
   free -m

2. Try to run it: ./check-server.sh (it should fail — why?)
3. Fix the permissions so you can run it
4. Run it and verify the output
5. Change permissions so only you can read and execute it (no one else)
6. Create a file called secret.txt and set permissions to 600
7. Verify with ls -la that the permissions look correct
```

---

## Exercise 5: Piping and Text Processing (Intermediate)

```
1. Using your application.log file from Exercise 3:
   a. Count the total number of lines
   b. Extract just the timestamps (first two columns) from each line
   c. Extract just the log levels (INFO, DEBUG, WARN, ERROR) and count each
   d. Find the line with "NullPointerException" and show only the filename and line number

2. Create a file called employees.csv:
   Name,Department,Salary
   Rahul,Engineering,85000
   Priya,Marketing,65000
   Amit,Engineering,90000
   Sneha,Engineering,78000
   Raj,Marketing,70000
   Pooja,HR,60000
   Vikram,Engineering,95000

   a. Extract only the Name and Salary columns
   b. Find all Engineering department employees
   c. Sort by salary (highest first)
   d. Count how many employees are in each department
```

---

## Exercise 6: Shell Scripting (Advanced)

```
1. Write a script called system-health.sh that:
   - Shows the current date and time
   - Shows disk usage and warns if any filesystem is over 80%
   - Shows memory usage
   - Shows the top 5 CPU-consuming processes
   - Shows the number of logged-in users
   - Saves the output to a file with today's date in the filename

2. Write a script called log-analyzer.sh that:
   - Takes a log filename as an argument
   - Counts total lines
   - Counts ERROR, WARN, INFO, and DEBUG lines separately
   - Shows the top 5 most common error messages
   - Saves a summary to a report file

3. Set up a cron job that runs system-health.sh every hour
```

---

## Exercise 7: Networking (Intermediate)

```
1. Find your computer's IP address using ifconfig or ip addr
2. Ping google.com and note the response time
3. Use nslookup to find the IP address of flipkart.com
4. Use dig to find the mail servers (MX records) for gmail.com
5. Use curl to make a GET request to https://httpbin.org/get
6. Use curl to make a POST request to https://httpbin.org/post with JSON body:
   {"name": "Sheetal", "course": "DevOps"}
7. Use curl to check just the HTTP status code of https://google.com
8. Check which ports are currently listening on your machine
9. Use traceroute to see the route to google.com
```

---

## Exercise 8: SSH (Advanced)

```
If you have access to a second machine (or an AWS free tier EC2 instance):

1. Generate an SSH key pair (ed25519)
2. Copy the public key to the remote server
3. SSH into the server without a password
4. Create an SSH config entry for the server
5. Use SCP to copy a file to the server
6. Use SCP to copy a file from the server to your machine
```

---

## Exercise 9: Combined Challenge (Advanced)

```
Scenario: You are a DevOps engineer. Your Java application is running on a
Linux server. Users report that the application is slow.

Tasks:
1. Check disk space (which filesystem is fullest?)
2. Check memory usage (is the system running low on memory?)
3. Find the Java process and check how much CPU/memory it uses
4. Check the application logs for errors in the last 100 lines
5. Check if the application port (8080) is listening
6. Ping the database server (assume IP 10.0.1.50) — is it reachable?
7. Write a script that does all of the above and saves results to a report file
8. Schedule this script to run every 15 minutes using cron

Write out every command you would use, in order.
```

---

## Quick Reference Card

### Must-Know Commands

| Task | Command |
|------|---------|
| Where am I? | `pwd` |
| What is here? | `ls -la` |
| Go somewhere | `cd /path/to/dir` |
| Read a file | `cat file` or `less file` |
| Search in files | `grep "text" file` |
| Find files | `find /path -name "*.java"` |
| Edit a file | `nano file` |
| Check disk | `df -h` |
| Check memory | `free -m` |
| Check processes | `ps aux \| grep java` |
| Kill a process | `kill -9 PID` |
| Check ports | `netstat -tlnp` or `ss -tlnp` |
| Remote connect | `ssh user@server` |
| Test an API | `curl -v URL` |
| Check DNS | `dig domain.com` |
| Watch logs live | `tail -f /var/log/app.log` |

---

## What is Next?

In **Week 2**, we will cover:
- **Docker** — Packaging your Java application into containers
- **Docker Compose** — Running multiple containers together
- **Container networking** — How containers talk to each other

The Linux and networking knowledge from this week is the foundation for EVERYTHING that follows in DevOps. Practice these commands daily until they become second nature.

> "In Linux, the terminal is your paintbrush, the file system is your canvas, and the commands are your colors. The more you practice, the more beautiful your work becomes."
