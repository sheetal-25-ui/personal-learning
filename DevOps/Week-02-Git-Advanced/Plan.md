# Week 02 — Git Advanced

> A complete guide for Java developers. Written in easy language with real-life Indian examples.

---

## Table of Contents

1. [Git Basics Recap](#1-git-basics-recap)
2. [Branching Strategies](#2-branching-strategies)
3. [Merge vs Rebase](#3-merge-vs-rebase)
4. [Merge Conflicts](#4-merge-conflicts)
5. [Pull Requests](#5-pull-requests)
6. [Git Stash](#6-git-stash)
7. [Cherry Pick](#7-cherry-pick)
8. [Git Tags](#8-git-tags)
9. [Git Hooks](#9-git-hooks)
10. [.gitignore](#10-gitignore)
11. [Git Bisect](#11-git-bisect)
12. [Undoing Mistakes](#12-undoing-mistakes)
13. [Monorepo vs Multi-repo](#13-monorepo-vs-multi-repo)
14. [Practice Exercises](#14-practice-exercises)

---

## 1. Git Basics Recap

### What is Git?

Git is a **version control system** — it tracks every change you make to your code,
who made it, when they made it, and why.

### Real-Life Analogy

Think of Git like the **revision history in Google Docs**. You write something, your
colleague edits it, you edit it again — and at any point you can go back to any
previous version. Git does the same thing but for your entire codebase.

Or think of it like a **savings account passbook**. Every deposit and withdrawal is
recorded with date, amount, and balance. You can always trace back what happened
and when. Git is your code's passbook.

### The Essential Commands

#### `git init` — Start tracking a project

```bash
mkdir my-java-project
cd my-java-project
git init
```

This creates a hidden `.git/` folder that stores all the history. Think of it as
opening a new passbook for your project.

#### `git clone` — Copy someone else's project

```bash
git clone https://github.com/spring-projects/spring-boot.git
```

Real-life: Your friend has a recipe book. Instead of writing down each recipe by hand,
you photocopy the entire book. `clone` gives you the complete history too.

#### `git status` — Check what has changed

```bash
git status
```

This shows you:
- **Untracked files** — new files Git does not know about yet
- **Modified files** — files you changed but have not staged
- **Staged files** — files ready to be committed

Think of `git status` like checking your kitchen before cooking — what ingredients
are out, what is prepped, what is ready to go into the pot.

#### `git add` — Stage changes for commit

```bash
# Add a specific file
git add src/main/java/App.java

# Add all changed files
git add .
```

Staging is like putting items in your shopping cart on Flipkart. You have not bought
them yet (committed), but you have decided "yes, I want these."

#### `git commit` — Save a snapshot

```bash
git commit -m "feat: add user login endpoint"
```

This is the actual purchase — your changes are now permanently recorded in history.
Every commit has a unique ID (hash), a message, author, and timestamp.

**Golden Rule**: Commit messages should explain **WHY**, not what.
- Bad: `updated file`
- Good: `fix: prevent duplicate login when user clicks button twice`

#### `git push` — Send commits to remote (GitHub/GitLab)

```bash
git push origin main
```

You have been writing in your notebook (local). Now you are uploading it to the cloud
(remote) so your team can see it. Like uploading a file to Google Drive.

#### `git pull` — Get latest changes from remote

```bash
git pull origin main
```

Your teammate pushed new code. You need to download it to your laptop.
Think of it like refreshing your WhatsApp to get new messages.

`git pull` = `git fetch` (download) + `git merge` (combine with your code)

#### `git log` — See commit history

```bash
# Simple log
git log --oneline

# Detailed with graph
git log --oneline --graph --all
```

This is reading the passbook — every transaction (commit) is listed with
date, who did it, and what they wrote as the message.

### Common Mistakes in Basics

| Mistake | What happens | Fix |
|---------|-------------|-----|
| Forgetting to `git add` before `git commit` | Nothing gets committed | Always run `git status` first |
| Pushing to wrong branch | Code goes to production | Always check `git branch` before push |
| Committing `.env` files | Secrets leak publicly | Use `.gitignore` (covered later) |
| Vague commit messages | Nobody knows what changed 3 months later | Write clear WHY messages |

---

## 2. Branching Strategies

### What is a Branch?

A branch is a **separate copy of your code** where you can work without disturbing
anyone else. When you are done, you merge it back.

### Real-Life Analogy

Imagine a **highway with service lanes**. The main highway is your production code.
Service lanes are branches — cars (developers) can take the service lane, do their
work, and merge back onto the highway when ready. Nobody on the highway is affected
by what is happening in the service lane.

### Why Do Branching Strategies Exist?

When one developer works alone, branches are simple. But when 10 people work on the
same project? Chaos. Branching strategies are like **traffic rules** — without them,
everyone crashes into each other.

---

### 2.1 Git Flow

Git Flow is the most structured branching strategy. It has **five types of branches**.

#### The Five Branches

```
main (production)
  |
  +-- develop (testing/staging)
        |
        +-- feature/login-page
        +-- feature/payment-gateway
        |
  +-- release/v2.0
  |
  +-- hotfix/critical-bug-fix
```

#### Real-Life: Restaurant Analogy

| Branch | Restaurant Equivalent | Purpose |
|--------|----------------------|---------|
| `main` | The live menu that customers see | Production code. Only tested, approved dishes go here |
| `develop` | The test kitchen | Where chefs try combining dishes. Not served to customers yet |
| `feature/*` | Chef experimenting with a new paneer recipe | Individual experiments. If it works, send to test kitchen |
| `release/*` | Final tasting before adding to menu | Last round of testing before production |
| `hotfix/*` | "Cockroach found in soup — FIX NOW!" | Emergency fix directly on production |

#### How It Works — Step by Step

```bash
# 1. You are on develop branch
git checkout develop

# 2. Create a feature branch
git checkout -b feature/user-login

# 3. Work on your feature, commit regularly
git add .
git commit -m "feat: add login form UI"
git commit -m "feat: add login API integration"

# 4. When feature is done, merge back to develop
git checkout develop
git merge feature/user-login

# 5. When develop is ready for release
git checkout -b release/v2.0
# ... do final testing, bug fixes ...

# 6. Release is approved — merge to main AND develop
git checkout main
git merge release/v2.0
git tag v2.0.0

git checkout develop
git merge release/v2.0

# 7. Emergency hotfix on production
git checkout main
git checkout -b hotfix/fix-payment-crash
# ... fix the bug ...
git checkout main
git merge hotfix/fix-payment-crash
git checkout develop
git merge hotfix/fix-payment-crash
```

#### When to Use Git Flow

- Large teams (10+ developers)
- Products with scheduled releases (like a banking app that releases every 2 weeks)
- When you need strict control over what goes to production

#### When NOT to Use

- Small teams or solo projects (too much overhead)
- Continuous deployment environments (too slow)

---

### 2.2 Trunk-Based Development

#### What is It?

Everyone commits to `main` (the trunk) directly or through very short-lived branches
(alive for hours, not days or weeks).

#### Real-Life Analogy

Think of a **shared Google Doc**. Everyone edits the same document at the same time.
There are no separate copies floating around. If you need to make a change, you just
make it. The document is always the latest version.

#### How It Works

```bash
# 1. Pull latest main
git checkout main
git pull origin main

# 2. Create a tiny branch (lives for hours, not days)
git checkout -b add-email-validation

# 3. Make your change (small, focused)
git add .
git commit -m "feat: add email format validation"

# 4. Push and create PR
git push origin add-email-validation
# Create PR, get quick review, merge same day

# 5. Delete the branch
git branch -d add-email-validation
```

#### Key Rules

1. **Branches live for hours**, not weeks
2. **Commits are small** — one logical change per commit
3. **Feature flags** hide incomplete features (the code is in main but turned off)
4. **CI/CD runs on every commit** to main — if tests fail, fix immediately

#### Who Uses This?

- **Google** — thousands of developers, one giant repository, trunk-based
- **Microsoft** — moved Windows development to trunk-based
- **Facebook** — similar approach with short-lived branches

#### When to Use

- Teams with strong CI/CD pipelines
- Teams that deploy multiple times per day
- When you want fast iteration speed

#### When NOT to Use

- Teams without automated testing (you will break production constantly)
- When you need formal release cycles

---

### 2.3 GitHub Flow

#### What is It?

The simplest branching strategy. Only two concepts: `main` and feature branches.
Everything goes through Pull Requests.

#### How It Works

```
main (always deployable)
  |
  +-- feature/add-search
  +-- feature/fix-header
  +-- feature/update-docs
```

```bash
# 1. Create branch from main
git checkout main
git pull origin main
git checkout -b feature/add-search

# 2. Work and commit
git add .
git commit -m "feat: add search bar to header"

# 3. Push and open Pull Request
git push origin feature/add-search
# Open PR on GitHub, get code review

# 4. After approval, merge to main
# Main is deployed automatically

# 5. Delete branch
git branch -d feature/add-search
```

#### Real-Life Analogy

Like a **suggestion box at office**. You write your suggestion (feature branch),
put it in the box (PR), the manager reviews it (code review), and if approved,
it becomes policy (merged to main).

#### When to Use

- Small to medium teams
- Web applications with continuous deployment
- Open source projects

### Comparison Table

| Feature | Git Flow | Trunk-Based | GitHub Flow |
|---------|----------|-------------|-------------|
| Complexity | High | Low | Low |
| Branches | 5 types | 1 + tiny | 2 types |
| Release cycle | Scheduled | Continuous | Continuous |
| Best for | Large teams, scheduled releases | Fast-moving teams, strong CI | Small-medium teams |
| Risk of merge conflicts | High (long-lived branches) | Low (short branches) | Medium |

---

## 3. Merge vs Rebase

> This is THE most confusing Git topic. Read this section slowly.

### What is Merge?

Merge takes two branches and **staples them together**. It creates a new "merge commit"
that has two parents — one from each branch.

### What is Rebase?

Rebase takes your branch and **replays it on top of another branch**. It rewrites
history to make it look like you started your work from the latest point.

### Real-Life Analogy

**Merge = Scrapbook style**

You and your friend both write diary entries for the week. On Sunday, you take both
diaries and staple them together into one book. Both timelines are preserved — you
can see who wrote what and when, but the timeline jumps back and forth.

**Rebase = Neat rewrite style**

Instead of stapling, you rewrite your entries so they come AFTER your friend's entries.
The diary reads in a neat, linear order — but the dates on your entries are changed.
The content is the same, but the timeline is faked.

### ASCII Diagrams

#### Before — Both branches have diverged

```
          A---B---C  feature/login
         /
    D---E---F---G    main
```

Your feature branch started at commit E. Meanwhile, main got commits F and G.

#### After MERGE

```
          A---B---C
         /         \
    D---E---F---G---M    main (M = merge commit)
```

Merge creates a new commit M that combines both branches. The history shows
the full picture — both branches existed, both timelines are visible.

#### After REBASE

```
                    A'--B'--C'  feature/login (rebased)
                   /
    D---E---F---G              main
```

Rebase moved A, B, C to AFTER G. The commits become A', B', C' (new hashes
because their parent changed). It looks like you started your work after G,
not after E. Clean, linear, but history is rewritten.

### How to Merge

```bash
# You are on feature/login, want to bring main's changes in
git checkout feature/login
git merge main
# This creates a merge commit
```

### How to Rebase

```bash
# You are on feature/login, want to replay on top of main
git checkout feature/login
git rebase main
# This replays your commits on top of main
```

### When to Use Which?

| Situation | Use | Why |
|-----------|-----|-----|
| Updating your feature branch with latest main | **Rebase** | Keeps history clean, no unnecessary merge commits |
| Merging a finished feature into main | **Merge** | Preserves the fact that the feature existed as a branch |
| Working alone on a feature branch | **Rebase** | Clean history, no one else is affected |
| Branch shared with others | **Merge** | Rebase rewrites history, breaks their copies |
| Open source PR | **Rebase** (squash) | Maintainers want clean, linear history |

### The Golden Rule of Rebase

> **NEVER rebase a branch that other people are working on.**

If you rebase a shared branch, you rewrite its history. Everyone else's copy becomes
incompatible. They will get conflicts, duplicate commits, and chaos.

```
SAFE to rebase:   Your local feature branch that only YOU work on
NEVER rebase:     main, develop, or any branch others have pulled
```

Real-life: Imagine you rewrite the minutes of a meeting that 10 people already
have a copy of. Now their copy does not match the "official" one. Everyone is
confused about what actually happened.

### Interactive Rebase — Squash Commits Before PR

Your feature branch has messy commits:

```
abc1234 WIP login
def5678 fix typo
ghi9012 oops forgot file
jkl3456 actual login implementation
mno7890 final cleanup
```

Before creating a PR, clean this up using interactive rebase:

```bash
# Squash the last 5 commits
git rebase -i HEAD~5
```

This opens an editor:

```
pick abc1234 WIP login
pick def5678 fix typo
pick ghi9012 oops forgot file
pick jkl3456 actual login implementation
pick mno7890 final cleanup
```

Change to:

```
pick abc1234 WIP login
squash def5678 fix typo
squash ghi9012 oops forgot file
squash jkl3456 actual login implementation
squash mno7890 final cleanup
```

Now all five commits become ONE clean commit:

```
xyz9999 feat: implement user login with validation
```

#### Why Squash?

Your team does not need to see your 47 "WIP" and "fix typo" commits. They want
ONE commit that says "here is the login feature." Think of it like sending a
final report to your manager — not your rough drafts with coffee stains.

### Common Mistakes with Merge and Rebase

| Mistake | Consequence | Prevention |
|---------|------------|------------|
| Rebasing main | Everyone's copies break | Never rebase shared branches |
| Not pulling before merge | Conflicts pile up | Always `git pull` first |
| Force pushing after rebase without thinking | Overwrites teammates' work | Only force push YOUR branches |
| Not squashing before PR | Messy history with "WIP" commits | Interactive rebase before PR |

---

## 4. Merge Conflicts

### What Are Merge Conflicts?

A merge conflict happens when **two people edit the same line in the same file**
and Git does not know which version to keep.

### Real-Life Analogy

You and your roommate both edit the Wi-Fi password on a shared sticky note at the
same time. You wrote "Mango123" and your roommate wrote "Papaya456". When you
compare notes, there is a conflict — which one is the real password?

Git faces the same problem. Two developers changed line 42 of `UserService.java`.
Git cannot automatically pick one — it needs a human to decide.

### Why Do Conflicts Happen?

1. Two people edit the same line of the same file on different branches
2. One person deletes a file that another person edited
3. Both people add different content at the same location

### Step-by-Step Conflict Resolution

#### Step 1 — The conflict appears

```bash
git merge feature/payment
# Auto-merging src/main/java/UserService.java
# CONFLICT (content): Merge conflict in src/main/java/UserService.java
# Automatic merge failed; fix conflicts and then commit the result.
```

#### Step 2 — Open the conflicted file

Git marks the conflict in the file like this:

```java
public class UserService {

    public String getUserGreeting(User user) {
<<<<<<< HEAD
        return "Welcome back, " + user.getFullName() + "!";
=======
        return "Hello, " + user.getFirstName() + "! Good to see you.";
>>>>>>> feature/payment
    }
}
```

**What do these markers mean?**

| Marker | Meaning |
|--------|---------|
| `<<<<<<< HEAD` | Start of YOUR version (the branch you are on) |
| `=======` | Divider between the two versions |
| `>>>>>>> feature/payment` | End of THEIR version (the branch being merged) |

#### Step 3 — Decide which version to keep

You have three choices:

**Option A — Keep yours:**
```java
return "Welcome back, " + user.getFullName() + "!";
```

**Option B — Keep theirs:**
```java
return "Hello, " + user.getFirstName() + "! Good to see you.";
```

**Option C — Combine both (most common):**
```java
return "Welcome back, " + user.getFullName() + "! Good to see you.";
```

Delete ALL conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`) and write the final version.

#### Step 4 — Mark as resolved and commit

```bash
git add src/main/java/UserService.java
git commit -m "fix: resolve merge conflict in UserService greeting method"
```

### VS Code Merge Editor

VS Code makes conflict resolution visual. When you open a conflicted file, you see:

- **Accept Current Change** (green) — keep your version
- **Accept Incoming Change** (blue) — keep their version
- **Accept Both Changes** — keep both, one after the other
- **Compare Changes** — see them side by side

This is MUCH easier than editing conflict markers manually. Most Java developers
use IntelliJ IDEA which has a similar 3-way merge tool.

### Tips to Avoid Conflicts

1. **Pull frequently** — the longer you wait, the more your branch drifts
2. **Keep branches short-lived** — merge within 1-2 days, not 2 weeks
3. **Communicate with your team** — "I am working on UserService.java today"
4. **Split large files** — if everyone edits the same file, the file might be too big
5. **Rebase before merge** — brings your branch up to date with main first

---

## 5. Pull Requests (PRs)

### What is a Pull Request?

A Pull Request is a **formal request to merge your branch into another branch**.
It includes your code changes, a description, and a space for team members to review.

### Real-Life Analogy

Think of a PR like submitting an **expense report at your company**. You fill out the
form (write code), attach receipts (tests), describe what the expense was for
(PR description), and submit it for your manager's approval (code review). The manager
can approve, ask for clarification, or reject it.

### Writing Good PR Descriptions

A PR description should answer three questions:
1. **What** did you change?
2. **Why** did you change it?
3. **How** can someone test it?

#### Example of a Good PR Description

```markdown
## Summary
- Add email validation to the user registration endpoint
- Return 400 Bad Request when email format is invalid

## Why
Users were able to register with emails like "asdf" and "hello@",
which caused failures in the email verification service downstream.
Bug reported by QA team on 2024-03-15.

## How to Test
1. POST to /api/users with body: {"email": "invalid", "name": "Test"}
   - Should return 400 with message "Invalid email format"
2. POST to /api/users with body: {"email": "test@gmail.com", "name": "Test"}
   - Should return 201 Created

## Screenshots
(attach if UI changes)
```

#### Example of a Bad PR Description

```markdown
fixed stuff
```

This tells your reviewer absolutely nothing. Do not do this.

### Code Review Best Practices

#### As a Reviewer

1. **Be kind but honest** — "This could be improved by..." not "This is wrong"
2. **Explain WHY** — "Use `Optional` here because `null` will cause NPE in the mapper" is better than "Change this"
3. **Focus on logic, not style** — let linters handle formatting
4. **Test the code** — pull the branch and run it locally if the change is significant
5. **Respond quickly** — a PR waiting 3 days for review blocks the developer

#### As a PR Author

1. **Keep PRs small** — 200-400 lines is ideal. 2000 lines? Nobody will review it properly.
2. **Self-review first** — read your own diff before requesting review
3. **Link the ticket/issue** — "Fixes #234" or "Relates to JIRA-567"
4. **Add comments on tricky parts** — if something is non-obvious, explain it in the PR

### Approvals and Requesting Changes

| Action | When to Use |
|--------|------------|
| **Approve** | Code looks good, tests pass, no concerns |
| **Request Changes** | There are bugs, security issues, or missing tests that MUST be fixed |
| **Comment** | Suggestions or questions, but not blocking the merge |

Most teams require **at least 1-2 approvals** before a PR can be merged. This is
configured in GitHub under branch protection rules.

---

## 6. Git Stash

### What is Git Stash?

Stash temporarily saves your uncommitted changes so you can work on something else,
then come back and restore them later.

### Real-Life Analogy

You are cooking dinner. The **dal is half-done on the stove**. Suddenly, the doorbell
rings — your neighbor needs urgent help. You **put the dal aside (stash)**, help your
neighbor, come back, and **put the dal back on the stove (stash pop)**. You resume
exactly where you left off.

### Why Does Stash Exist?

Common scenario: You are working on a feature. Your manager pings you: "There is a
critical bug in production. Fix it NOW." But you have half-written code that you do
not want to commit (it does not even compile yet). What do you do?

**Stash it.** Switch to the bug fix, complete it, then come back and restore your
work-in-progress.

### How It Works

#### `git stash` — Save current changes

```bash
# You have modified files but do not want to commit yet
git stash
# Output: Saved working directory and index state WIP on feature/login
```

Your working directory is now clean — as if you never made those changes.

#### `git stash pop` — Restore and remove from stash

```bash
git stash pop
# Your changes are back, and the stash entry is deleted
```

#### `git stash apply` — Restore but KEEP in stash

```bash
git stash apply
# Changes are restored, but the stash entry remains (you can apply again)
```

Use `apply` when you want to apply the same changes to multiple branches.

#### `git stash list` — See all stashes

```bash
git stash list
# stash@{0}: WIP on feature/login: abc1234 add login form
# stash@{1}: WIP on main: def5678 update readme
# stash@{2}: WIP on feature/payment: ghi9012 add payment gateway
```

You can have multiple stashes stacked up like a pile of plates.

#### `git stash drop` — Delete a specific stash

```bash
git stash drop stash@{1}
# Deletes the stash at position 1
```

#### `git stash clear` — Delete ALL stashes

```bash
git stash clear
# Everything is gone. Use carefully.
```

### Named Stashes

Give your stash a description so you remember what it was:

```bash
git stash push -m "half-done payment validation"
```

```bash
git stash list
# stash@{0}: On feature/payment: half-done payment validation
```

Much better than "WIP on feature/payment" which tells you nothing.

### Common Mistakes with Stash

| Mistake | What happens | Fix |
|---------|-------------|-----|
| Forgetting you stashed something | Work is "lost" for weeks | Run `git stash list` regularly |
| Stashing untracked files without `-u` | New files are NOT stashed | Use `git stash -u` to include untracked |
| Stash conflicts on pop | If the code changed since you stashed | Resolve conflicts like merge conflicts |

---

## 7. Cherry Pick

### What is Cherry Pick?

Cherry pick takes **one specific commit** from one branch and applies it to another branch.
It does not merge the entire branch — just that one commit.

### Real-Life Analogy

Your colleague's branch has 50 commits. One of them fixes a critical bug you also need.
You do not want all 50 commits — just that one bug fix. Cherry pick is like going to
a thali buffet and picking **just the gulab jamun** from the dessert section, without
taking the entire thali.

### Why Does It Exist?

The most common use case: **hotfixes**.

Your team is working on `develop` branch with 30 new commits. One of those commits
fixes a critical production bug. Production runs on `main`. You cannot merge all of
`develop` into `main` (those 29 other commits are not tested yet). But you need THAT
ONE fix on `main`. Cherry pick it.

### How It Works

```bash
# 1. Find the commit hash you want
git log --oneline develop
# abc1234 feat: add notification system
# def5678 fix: prevent duplicate payment processing    <-- THIS ONE
# ghi9012 feat: add dashboard charts
# jkl3456 refactor: clean up user service

# 2. Switch to the target branch
git checkout main

# 3. Cherry pick the specific commit
git cherry-pick def5678

# 4. The fix is now on main, without any other develop commits
git log --oneline
# xyz9999 fix: prevent duplicate payment processing    <-- cherry picked
# ... (rest of main history)
```

### Cherry Picking Multiple Commits

```bash
# Pick a range
git cherry-pick abc1234..def5678

# Pick specific commits (not a range)
git cherry-pick abc1234 def5678 ghi9012
```

### When to Use Cherry Pick

| Scenario | Use Cherry Pick? |
|----------|-----------------|
| Hotfix from develop to main | Yes |
| Backporting a fix to an older release branch | Yes |
| Moving a feature between branches | No — use merge/rebase instead |
| Regular feature development | No — this is not what cherry pick is for |

### Common Mistakes

1. **Cherry picking too often** — if you cherry pick 10 commits, you should probably just merge
2. **Forgetting that cherry pick creates a NEW commit** — the hash changes. The original commit on develop and the cherry-picked commit on main are different commits with different hashes
3. **Duplicate commits after merge** — if you cherry pick a commit to main and then later merge develop into main, that change exists as two separate commits

---

## 8. Git Tags

### What is a Tag?

A tag is a **permanent label on a specific commit**. It is like putting a bookmark
in a book — "this is where version 2.0 starts."

### Real-Life Analogy

Think of tags like **milestones on a highway**: "Mumbai 100km", "Pune 200km".
The highway keeps going, but the milestones mark important points. Similarly,
your code keeps evolving with commits, but tags mark important releases.

### Why Do Tags Exist?

When a customer reports a bug in "version 2.1.3", you need to know EXACTLY which
commit that version was. Tags give you that. Without tags, you would have to search
through hundreds of commits to find the right one.

### Types of Tags

#### Lightweight Tags

```bash
git tag v1.0.0
```

Just a name pointing to a commit. No extra information.

#### Annotated Tags (recommended)

```bash
git tag -a v1.0.0 -m "First stable release with user auth and payment"
```

Includes the tagger's name, date, and a message. Use this for releases.

### How to Work with Tags

```bash
# Create a tag
git tag -a v2.1.0 -m "Release 2.1.0: added search feature"

# List all tags
git tag

# See tag details
git show v2.1.0

# Push a specific tag to remote
git push origin v2.1.0

# Push ALL tags
git push origin --tags

# Tag an older commit (you forgot to tag at release time)
git tag -a v1.5.0 abc1234 -m "Retrospective tag for release 1.5.0"

# Delete a local tag
git tag -d v1.0.0-beta

# Delete a remote tag
git push origin --delete v1.0.0-beta
```

### Semantic Versioning (SemVer)

Almost every software project uses this format: **MAJOR.MINOR.PATCH**

```
v2.1.3
| | |
| | +-- PATCH: Bug fixes (backward compatible)
| +---- MINOR: New features (backward compatible)
+------ MAJOR: Breaking changes (NOT backward compatible)
```

#### When to Increment What?

| Change | Example | Version Change |
|--------|---------|---------------|
| Fix a bug | Fix null pointer in login | v2.1.3 -> v2.1.4 |
| Add new feature (existing API unchanged) | Add search endpoint | v2.1.4 -> v2.2.0 |
| Change existing API (old code breaks) | Rename `/users` to `/accounts` | v2.2.0 -> v3.0.0 |

#### Real-Life Examples

- **PATCH** (2.1.3 -> 2.1.4): Swiggy fixes the "order placed but no confirmation" bug. The app works the same, just without the bug.
- **MINOR** (2.1.4 -> 2.2.0): Swiggy adds "Schedule Delivery" feature. Everything else works as before, there is just a new button.
- **MAJOR** (2.2.0 -> 3.0.0): Swiggy redesigns the entire ordering flow. Old shortcuts/bookmarks may not work. Your muscle memory needs updating.

### Pre-release Tags

```
v2.0.0-alpha.1    (very early, might be broken)
v2.0.0-beta.1     (feature complete, but may have bugs)
v2.0.0-rc.1       (release candidate — almost ready)
v2.0.0            (stable release)
```

---

## 9. Git Hooks

### What Are Git Hooks?

Git hooks are **scripts that run automatically** at specific points in the Git workflow.
They are like automatic checkpoints — before you commit, before you push, after you
merge, etc.

### Real-Life Analogy

Think of airport security. Before you board a flight (push code), you go through
**mandatory security checks** (hooks). If your bag has prohibited items (lint errors,
failing tests), you are stopped. You cannot bypass the checkpoint — you must fix the
issue first.

### Why Do Hooks Exist?

To **automate quality checks**. Instead of relying on developers to remember "run
linter before commit" and "run tests before push," hooks enforce it automatically.

### Common Hooks

| Hook | When It Runs | Common Use |
|------|-------------|------------|
| `pre-commit` | Before `git commit` executes | Run linter, format code, check for secrets |
| `commit-msg` | After writing commit message | Enforce commit message format |
| `pre-push` | Before `git push` executes | Run tests, check branch name |
| `post-merge` | After a merge completes | Install new dependencies, run migrations |
| `pre-rebase` | Before rebase starts | Warn if rebasing a shared branch |

### Setting Up Hooks

Hooks live in `.git/hooks/` directory. Create a file with the hook name (no extension):

#### Example: Pre-commit hook (run linter)

```bash
# .git/hooks/pre-commit
#!/bin/sh

echo "Running linter..."
./mvnw checkstyle:check

if [ $? -ne 0 ]; then
    echo "Linter failed! Fix the issues before committing."
    exit 1    # exit 1 = reject the commit
fi

echo "Linter passed!"
exit 0    # exit 0 = allow the commit
```

Make it executable:

```bash
chmod +x .git/hooks/pre-commit
```

#### Example: Pre-push hook (run tests)

```bash
# .git/hooks/pre-push
#!/bin/sh

echo "Running tests before push..."
./mvnw test

if [ $? -ne 0 ]; then
    echo "Tests failed! Fix them before pushing."
    exit 1
fi

echo "All tests passed. Pushing..."
exit 0
```

### Using Husky (for Node.js/React projects)

For frontend projects, **Husky** is a popular tool that makes hooks easier to manage:

```bash
npm install husky --save-dev
npx husky init
```

```bash
# .husky/pre-commit
npm run lint
npm run test
```

### For Java Projects

Use the **Maven Git Hook plugin** or simply place scripts in `.git/hooks/`.
Many Java teams use CI/CD (Jenkins, GitHub Actions) instead of local hooks
because the JVM is slow to start for quick pre-commit checks.

### The Problem with Hooks

Hooks are **local only** — they live in `.git/hooks/` which is NOT committed to the
repository. New team members will not have them. Solutions:

1. **Store hooks in a `hooks/` directory** in the repo, and add setup instructions
2. **Use a tool like Husky** that auto-installs hooks from package.json
3. **Rely on CI/CD** — server-side checks cannot be bypassed (unlike local hooks)

---

## 10. .gitignore

### What is .gitignore?

A file that tells Git: "Do NOT track these files." Any file or folder pattern listed
in `.gitignore` will be invisible to Git.

### Real-Life Analogy

When you pack for a trip, you have a mental list of things NOT to pack — your pillow,
your fridge, your sofa. `.gitignore` is that list for your code. It tells Git what
to leave behind.

### Why Does It Exist?

Some files should NEVER go into your repository:

1. **Secrets** — `.env` files with API keys, passwords, database credentials
2. **Build artifacts** — compiled `.class` files, `target/` folder (can be rebuilt)
3. **Dependencies** — `node_modules/` (can be re-downloaded with `npm install`)
4. **IDE settings** — `.idea/`, `.vscode/` (personal preferences, not project code)
5. **OS files** — `.DS_Store` (Mac), `Thumbs.db` (Windows)

### .gitignore for a Java/Spring Boot Project

```gitignore
# ===== Build Output =====
target/
build/
*.class
*.jar
*.war

# ===== IDE Files =====
.idea/
*.iml
.vscode/
.settings/
.project
.classpath
*.swp

# ===== Environment and Secrets =====
.env
.env.local
.env.production
application-local.yml
application-secret.yml

# ===== Logs =====
*.log
logs/

# ===== OS Files =====
.DS_Store
Thumbs.db

# ===== Dependency Directories =====
node_modules/

# ===== Temporary Files =====
*.tmp
*.bak
*.swp
```

### .gitignore Patterns

| Pattern | What It Matches |
|---------|----------------|
| `*.log` | All files ending in `.log` in any directory |
| `target/` | The `target` folder and everything inside it |
| `!important.log` | Exception — DO track this file even though `*.log` says ignore |
| `**/temp/` | Any folder named `temp` at any depth |
| `doc/*.pdf` | PDF files in the `doc/` folder only (not subdirectories) |
| `doc/**/*.pdf` | PDF files in `doc/` and all its subdirectories |

### What If You Already Committed a File and THEN Added It to .gitignore?

Adding a file to `.gitignore` does NOT remove it from Git history. You need to
explicitly untrack it:

```bash
# Remove from Git tracking (but keep the file on disk)
git rm --cached .env

# Now add .env to .gitignore
echo ".env" >> .gitignore

# Commit the removal
git add .gitignore
git commit -m "chore: remove .env from tracking and add to gitignore"
```

### Common Mistake

**Committing `.env` files with secrets, then adding to `.gitignore` later.**
The secrets are already in Git history and can be found by anyone with access.
If this happens, you must:
1. Rotate ALL the leaked credentials immediately
2. Use `git filter-branch` or BFG Repo Cleaner to remove the file from history
3. Force push the cleaned history

This is a serious security incident. Always set up `.gitignore` BEFORE your first commit.

---

## 11. Git Bisect

### What is Git Bisect?

Git bisect uses **binary search** to find the exact commit that introduced a bug.
Instead of checking every commit one by one, it cuts the search space in half
each time.

### Real-Life Analogy

You come home and find that the **water purifier is not working**. It was working
last Monday. Between Monday and today (Friday), your family used it many times.
Instead of checking every single use one by one:

1. Was it working on Wednesday? **Yes**
2. Was it working on Thursday? **No**
3. Was it working on Thursday morning? **Yes**
4. Was it working on Thursday afternoon? **No**

Found it! Something happened between Thursday morning and Thursday afternoon.
That is binary search — cutting the problem in half each time.

With 100 commits, you only need about 7 checks (log2 of 100) instead of 100.

### How It Works

```bash
# 1. Start bisect
git bisect start

# 2. Tell Git the current commit is BAD (bug exists)
git bisect bad

# 3. Tell Git a known GOOD commit (before the bug existed)
git bisect good abc1234

# Git will now checkout a commit halfway between good and bad
# Bisecting: 50 revisions left to test after this (roughly 6 steps)
# [def5678] feat: update payment service
```

```bash
# 4. Test the current commit — does the bug exist?
./mvnw test
# If bug exists:
git bisect bad
# If bug does NOT exist:
git bisect good
```

```bash
# 5. Git checks out another commit. Repeat step 4.
# After ~6-7 steps:
# def5678 is the first bad commit
# Author: Rahul <rahul@company.com>
# Date: Mon Mar 11 14:32:00 2024
# Message: refactor: optimize user query
```

```bash
# 6. Done! Clean up.
git bisect reset
```

### Automating Bisect

If you have a test that detects the bug, you can automate the entire process:

```bash
git bisect start
git bisect bad HEAD
git bisect good v2.0.0
git bisect run ./mvnw test -pl user-service -Dtest=PaymentTest
```

Git will run the test at each step and automatically mark good/bad. It finds
the guilty commit without any manual intervention.

### When to Use Bisect

- The bug exists now but did not exist in a previous version
- You have many commits between "working" and "broken"
- You can reliably reproduce the bug (or have a test for it)

---

## 12. Undoing Mistakes

> Every developer eventually runs `git push` and immediately thinks "Oh no."
> This section is your safety net.

### 12.1 git reset — Go Back in Time

Reset moves your branch pointer backward to a previous commit. There are three modes:

#### `--soft` — Undo commit, KEEP changes staged

```bash
git reset --soft HEAD~1
```

What happens:
- The last commit is undone
- Your changes are still staged (green in `git status`)
- You can re-commit with a better message or add more changes

Real-life: You sealed an envelope (committed) but forgot to add a document inside.
`--soft` opens the envelope — the letter is still there, ready to be re-sealed.

#### `--mixed` (default) — Undo commit, KEEP changes but unstage them

```bash
git reset HEAD~1
# or equivalently
git reset --mixed HEAD~1
```

What happens:
- The last commit is undone
- Your changes are in the working directory (red in `git status`)
- They are NOT staged — you need to `git add` again

Real-life: You sealed the envelope and put a stamp on it. `--mixed` opens the
envelope AND removes the stamp. The letter is still there, but you need to re-stamp
and re-seal.

#### `--hard` — Undo commit AND DELETE all changes

```bash
git reset --hard HEAD~1
```

What happens:
- The last commit is undone
- All changes are **permanently deleted** from working directory
- There is no undo for this (except reflog — covered below)

Real-life: You sealed the envelope, then threw it in a shredder. The letter is gone.

#### Summary Table

| Mode | Commit | Staging Area | Working Directory |
|------|--------|-------------|-------------------|
| `--soft` | Undone | Changes kept (staged) | Changes kept |
| `--mixed` | Undone | Changes unstaged | Changes kept |
| `--hard` | Undone | Changes deleted | Changes deleted |

#### Reset Multiple Commits

```bash
# Undo last 3 commits
git reset --soft HEAD~3

# Reset to a specific commit
git reset --hard abc1234
```

### 12.2 git revert — Safe Undo (Creates a New Commit)

Revert creates a **new commit** that undoes the changes of a previous commit.
It does NOT delete history — it adds to it.

```bash
# Revert the last commit
git revert HEAD

# Revert a specific commit
git revert abc1234
```

What happens:

```
BEFORE: A --- B --- C --- D       (D introduced a bug)

AFTER:  A --- B --- C --- D --- D'  (D' undoes D's changes)
```

D still exists in history, but D' cancels out its changes.

### Real-Life Analogy

**Reset** is like tearing pages out of your diary — those days never happened.

**Revert** is like writing a new entry: "Everything I wrote on March 15 was wrong.
Here is the correction." The original entry is still there, but the correction
is recorded too.

### Reset vs Revert — When to Use Which

| Situation | Use | Why |
|-----------|-----|-----|
| Undo a commit you have NOT pushed yet | **Reset** | Safe — nobody else has seen it |
| Undo a commit you HAVE pushed | **Revert** | Does not rewrite history others depend on |
| Undo a merge commit | **Revert** | Reset on shared branches causes chaos |
| "Oops wrong commit message" (not pushed) | **Reset --soft** | Re-commit with correct message |
| "I want to completely erase the last 3 commits" (not pushed) | **Reset --hard** | Nuclear option, use carefully |

**Golden Rule**: If the commit has been pushed and others might have pulled it,
always use **revert**. Never reset a shared branch.

### 12.3 git reflog — Your Emergency Recovery Tool

Reflog is a log of **every position your HEAD has been at**. Even if you `reset --hard`
and "delete" commits, reflog remembers where they were.

### Real-Life Analogy

Your browser history. Even if you close a tab, you can go to history and find it.
Reflog is Git's browser history — it tracks every move, even the ones you tried to undo.

### How to Use Reflog

```bash
git reflog
# abc1234 HEAD@{0}: reset: moving to HEAD~3
# def5678 HEAD@{1}: commit: feat: add payment gateway
# ghi9012 HEAD@{2}: commit: feat: add user dashboard
# jkl3456 HEAD@{3}: commit: feat: add login page
```

#### Recover "Deleted" Commits

You ran `git reset --hard HEAD~3` and lost 3 commits. Panic!

```bash
# 1. Check reflog — your commits are still there
git reflog

# 2. Find the commit BEFORE the reset
# def5678 HEAD@{1}: commit: feat: add payment gateway

# 3. Reset back to that commit
git reset --hard def5678

# 4. Your commits are back!
```

#### Recover a Deleted Branch

```bash
# Oops, deleted the branch
git branch -D feature/important-work

# Find where the branch was pointing
git reflog
# ... look for the last commit on that branch ...
# xyz9999 HEAD@{5}: commit: feat: complete important work

# Recreate the branch at that commit
git checkout -b feature/important-work xyz9999
```

### Important Note About Reflog

Reflog entries expire after **90 days** by default. After that, Git garbage collection
may permanently remove unreferenced commits. So reflog is a safety net, not a permanent
archive.

### The Complete "Undo" Decision Tree

```
Did I push the commit?
|
+-- NO (only local)
|   |
|   +-- Want to redo the commit message? -----> git reset --soft HEAD~1
|   +-- Want to add more changes? ------------> git reset --soft HEAD~1
|   +-- Want to completely erase it? ----------> git reset --hard HEAD~1
|   +-- Want to unstage files? ----------------> git reset HEAD~1 (mixed)
|
+-- YES (already pushed)
    |
    +-- Want to undo safely? ------------------> git revert <commit-hash>
    +-- Accidentally pushed secrets? ----------> Rotate credentials IMMEDIATELY,
    |                                            then use BFG Repo Cleaner
    +-- Need to undo a merge? -----------------> git revert -m 1 <merge-commit>
```

---

## 13. Monorepo vs Multi-repo

### What is a Monorepo?

**One repository** contains ALL the code for ALL services/projects in an organization.

### What is Multi-repo?

Each service/project has its **own separate repository**.

### Real-Life Analogy

**Monorepo** = A **joint family home**. Everyone lives under one roof. Shared kitchen,
shared living room, shared bills. Easy to coordinate, but when one person makes a mess,
everyone is affected.

**Multi-repo** = **Separate apartments**. Each family has their own space. Complete
independence, but coordinating a family dinner requires more effort.

### Visual Comparison

```
MONOREPO                          MULTI-REPO
===========                       ===========

my-company/                       user-service/
  |-- user-service/                 |-- src/
  |-- payment-service/              |-- pom.xml
  |-- notification-service/         |-- .git/
  |-- shared-libs/
  |-- .git/                       payment-service/
                                    |-- src/
(ONE .git for everything)           |-- pom.xml
                                    |-- .git/

                                  notification-service/
                                    |-- src/
                                    |-- pom.xml
                                    |-- .git/

                                  (SEPARATE .git for each)
```

### Comparison

| Aspect | Monorepo | Multi-repo |
|--------|----------|------------|
| **Code sharing** | Easy — everything is in one place | Need to publish shared libraries (Maven/npm packages) |
| **Refactoring** | One commit can update all services | Must update each repo separately and coordinate versions |
| **CI/CD** | Complex — need to figure out what changed | Simple — each repo has its own pipeline |
| **Repository size** | Can become huge (GBs) | Each repo stays small and focused |
| **Team autonomy** | Less — everyone shares the same repo | More — each team owns their repo |
| **Dependency management** | Always using latest version (it is right there) | Version pinning, compatibility matrices |
| **Access control** | Harder — everyone can see everything | Easy — repo-level permissions |
| **Onboarding** | Clone once, have everything | Clone 15 repos, set up each one |

### Who Uses What?

| Company | Strategy | Why |
|---------|----------|-----|
| **Google** | Monorepo | 1 billion files, 86 TB. Custom tooling (Piper/CitC). "One version of truth." |
| **Facebook** | Monorepo | Mercurial (not Git). Custom tools to handle scale. |
| **Microsoft** | Monorepo (for Windows) | VFS for Git — virtual filesystem that downloads files on demand |
| **Netflix** | Multi-repo | Hundreds of microservices, each with its own repo. Teams are fully autonomous. |
| **Amazon** | Multi-repo | Each service team owns their repo. "Two pizza teams." |
| **Uber** | Monorepo | Started multi-repo, moved to mono for better code sharing |

### When to Choose Which?

#### Choose Monorepo When:

- Your services share a lot of code
- You want atomic changes across services (one PR updates API and all clients)
- Your team is small to medium (under 50 developers)
- You want easy refactoring

#### Choose Multi-repo When:

- Teams are large and independent
- Services have very different tech stacks (Java backend, React frontend, Python ML)
- You need strict access control
- Your repositories would become unreasonably large

### Tools for Monorepos

Since Git was not designed for massive repos, these tools help:

| Tool | What It Does |
|------|-------------|
| **Nx** | Build system for monorepos (mainly JS/TS) |
| **Turborepo** | Fast build system, smart caching |
| **Bazel** | Google's build tool, language-agnostic |
| **Lerna** | Manage JS monorepos (npm packages) |
| **Maven modules** | Java — parent POM with child modules |

### Java Monorepo Example (Maven Multi-Module)

```xml
<!-- Root pom.xml -->
<project>
    <groupId>com.mycompany</groupId>
    <artifactId>parent</artifactId>
    <packaging>pom</packaging>

    <modules>
        <module>user-service</module>
        <module>payment-service</module>
        <module>notification-service</module>
        <module>shared-libs</module>
    </modules>
</project>
```

---

## 14. Practice Exercises

### Exercise 1 — Branch and Merge

```
1. Create a new repository: git init practice-repo && cd practice-repo
2. Create a file: echo "Hello World" > app.txt
3. Commit: git add . && git commit -m "initial commit"
4. Create a branch: git checkout -b feature/greeting
5. Edit app.txt: echo "Namaste World" > app.txt
6. Commit the change
7. Switch back to main: git checkout main
8. Merge the feature branch: git merge feature/greeting
9. Check the log: git log --oneline --graph
```

### Exercise 2 — Create and Resolve a Merge Conflict

```
1. On main: edit app.txt line 1 to "Hello from Main"
2. Commit
3. Create branch: git checkout -b feature/conflict
4. Edit app.txt line 1 to "Hello from Feature"
5. Commit
6. Switch to main: git checkout main
7. Merge: git merge feature/conflict
8. Observe the conflict markers in app.txt
9. Resolve the conflict manually
10. Commit the resolution
```

### Exercise 3 — Rebase Practice

```
1. Create a new branch from main: git checkout -b feature/rebase-test
2. Make 3 commits with different messages
3. Switch to main and make 2 commits
4. Switch back to feature/rebase-test
5. Rebase onto main: git rebase main
6. Compare the log with and without rebase (use git log --oneline --graph --all)
```

### Exercise 4 — Interactive Rebase (Squash)

```
1. On a feature branch, create 5 messy commits:
   - "WIP"
   - "fix typo"
   - "oops"
   - "actually works now"
   - "final version"
2. Run: git rebase -i HEAD~5
3. Squash all into one clean commit: "feat: add user greeting feature"
4. Verify with: git log --oneline
```

### Exercise 5 — Stash Workflow

```
1. Start editing a file (do NOT commit)
2. git stash push -m "work in progress on feature X"
3. Switch to main and make a hotfix commit
4. Switch back to your branch
5. git stash pop
6. Verify your changes are back
7. Try: git stash list (should be empty after pop)
```

### Exercise 6 — Cherry Pick

```
1. Create branch develop with 5 commits
2. One of those commits should be a "bug fix"
3. Switch to main
4. Cherry pick ONLY the bug fix commit
5. Verify with git log that main has the fix but not the other commits
```

### Exercise 7 — Tags and Semantic Versioning

```
1. Make 3 commits representing: initial release, new feature, bug fix
2. Tag the first as v1.0.0
3. Tag after the new feature as v1.1.0
4. Tag after the bug fix as v1.1.1
5. List all tags: git tag
6. Show details of v1.1.0: git show v1.1.0
7. Push all tags: git push origin --tags
```

### Exercise 8 — Git Bisect

```
1. Create 10 commits. In commit 6, introduce a "bug" (add the text "BUG" to a file)
2. Commits 7-10 should NOT fix the bug
3. Use git bisect to find which commit introduced "BUG"
4. Automate it: git bisect run grep -L "BUG" app.txt
```

### Exercise 9 — Undo Everything

```
Part A — Reset:
1. Make 3 commits
2. git reset --soft HEAD~1 (observe: changes are staged)
3. git reset --mixed HEAD~1 (observe: changes are unstaged)
4. git reset --hard HEAD~1 (observe: changes are gone)

Part B — Revert:
5. Make a commit with a "mistake"
6. git revert HEAD
7. Check git log — both the mistake and the revert are in history

Part C — Reflog Recovery:
8. Make 3 important commits
9. git reset --hard HEAD~3 (panic — commits are "gone")
10. git reflog (find the commits)
11. git reset --hard <hash> (recover everything)
```

### Exercise 10 — .gitignore

```
1. Create a Java project structure:
   mkdir -p src/main/java target .idea
   touch .env application-secret.yml
   touch target/app.class .idea/workspace.xml
   touch src/main/java/App.java

2. Create a .gitignore file with appropriate patterns (target/, .idea/, .env, etc.)

3. Run git status — verify ignored files do NOT appear

4. Intentionally add .env: git add -f .env && git commit
5. Now add .env to .gitignore
6. Run git rm --cached .env && git commit
7. Verify .env is no longer tracked
```

### Exercise 11 — Complete Workflow (Combines Everything)

```
Scenario: You are working on an e-commerce app.

1. Initialize a repo, create main branch with initial code
2. Set up .gitignore for Java
3. Create a develop branch
4. Create feature/cart from develop — add shopping cart code
5. While working on cart, get a "urgent bug" notification
6. Stash your cart work
7. Create hotfix/payment-fix from main
8. Fix the bug, commit, merge to main AND develop
9. Tag main as v1.0.1 (patch release)
10. Go back to feature/cart, pop your stash
11. Finish the cart feature with 4 messy commits
12. Interactive rebase to squash into 1 clean commit
13. Merge feature/cart into develop
14. Create release/v1.1.0 from develop
15. Merge release into main
16. Tag as v1.1.0
17. Run git log --oneline --graph --all and admire the beautiful history
```

---

## Quick Reference Card

```
BASICS                          BRANCHING
------                          ---------
git init                        git branch <name>
git clone <url>                 git checkout <branch>
git add <file>                  git checkout -b <new-branch>
git commit -m "message"         git merge <branch>
git push origin <branch>        git rebase <branch>
git pull origin <branch>        git branch -d <branch>
git status
git log --oneline --graph

STASH                           UNDO
-----                           ----
git stash                       git reset --soft HEAD~1
git stash push -m "msg"         git reset --mixed HEAD~1
git stash pop                   git reset --hard HEAD~1
git stash list                  git revert <hash>
git stash apply                 git reflog
git stash drop stash@{N}

TAGS                            ADVANCED
----                            --------
git tag -a v1.0.0 -m "msg"     git cherry-pick <hash>
git tag                         git bisect start/bad/good
git push origin --tags          git rebase -i HEAD~N
git show <tag>                  git stash -u (include untracked)

CLEANUP
-------
.gitignore                      git rm --cached <file>
```

---

**End of Week 02 — Git Advanced**

You now have the tools to handle branching strategies, merge conflicts, history
rewriting, emergency recovery, and team collaboration workflows. The key is
practice — the exercises above cover every concept. Do them on a throwaway repo
where mistakes do not matter. That is the fastest way to learn.
