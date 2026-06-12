# Week 02 — Git Advanced — Interview Answers & Exercise Solutions

> A complete answer key for a self-study DevOps course. Written in friendly, easy language with vivid analogies.
> All examples use **Git commands** (this is DevOps, not Java coding).

---

## 🌟 The Interview Golden Rule

When you answer ANY Git interview question, walk through these four steps in order. It makes you sound senior and confident:

> **Definition → Why it exists → Command / Example → When to use it (and when NOT to)**

Example (in 10 seconds):
- **Definition:** "`git stash` shelves your uncommitted changes."
- **Why:** "So I can switch context without committing half-baked work."
- **Command:** "`git stash`, then `git stash pop` to bring it back."
- **When:** "Use it for an urgent context switch; not for long-term storage — that's what branches are for."

Nail this rhythm and even tricky questions feel easy. 🎯

---

# PART A — Exercise / Practice Solutions

The Plan.md has **11 practice exercises**. Below is a complete, copy-paste-runnable solution for each, with the exact git commands and an explanation of what is happening and why.

> 💡 Run all of these in a throwaway folder (`/tmp/git-practice`) where mistakes are harmless.

---

## Exercise 1 — Branch and Merge ✅

**Goal:** Create a repo, branch off, change a file, merge back.

```bash
# 1. Create a new repository
git init practice-repo && cd practice-repo

# 2. Create a file
echo "Hello World" > app.txt

# 3. Commit
git add . && git commit -m "initial commit"

# 4. Create and switch to a branch
git checkout -b feature/greeting

# 5. Edit app.txt
echo "Namaste World" > app.txt

# 6. Commit the change
git add app.txt && git commit -m "feat: change greeting to Namaste"

# 7. Switch back to main
git checkout main

# 8. Merge the feature branch
git merge feature/greeting

# 9. Check the log
git log --oneline --graph
```

**What happens & why:** Because `main` had no new commits while you worked on `feature/greeting`, Git performs a **fast-forward merge** — it simply slides the `main` pointer forward to the feature commit. No merge commit is created. The graph is a clean straight line. 🟢

**Pro tip:** To force a merge commit even when fast-forward is possible, use `git merge --no-ff feature/greeting`. This preserves the "a branch existed here" story in history.

---

## Exercise 2 — Create and Resolve a Merge Conflict ⚔️

**Goal:** Deliberately collide two edits on the same line and resolve it.

```bash
# 1 & 2. On main, change line 1, commit
git checkout main
echo "Hello from Main" > app.txt
git add app.txt && git commit -m "edit: greeting from main"

# 3. Create a branch from this point
git checkout -b feature/conflict

# 4 & 5. Edit the SAME line differently, commit
echo "Hello from Feature" > app.txt
git add app.txt && git commit -m "edit: greeting from feature"

# 6. Switch to main and make main diverge too
git checkout main
echo "Hello from Main UPDATED" > app.txt
git add app.txt && git commit -m "edit: greeting updated on main"

# 7. Merge — this now CONFLICTS
git merge feature/conflict
```

You will see:

```
Auto-merging app.txt
CONFLICT (content): Merge conflict in app.txt
Automatic merge failed; fix conflicts and then commit the result.
```

**8. Observe the conflict markers** — open `app.txt`:

```
<<<<<<< HEAD
Hello from Main UPDATED
=======
Hello from Feature
>>>>>>> feature/conflict
```

| Marker | Meaning |
|---|---|
| `<<<<<<< HEAD` | Start of YOUR version (current branch, `main`) |
| `=======` | Divider |
| `>>>>>>> feature/conflict` | THEIR version (branch being merged in) |

**9. Resolve manually** — delete ALL markers and write the final line you want:

```bash
echo "Hello from Main and Feature combined" > app.txt
```

**10. Mark resolved and commit:**

```bash
git add app.txt
git commit -m "fix: resolve greeting merge conflict"
```

**Handy escape hatches:**
- `git merge --abort` → cancel the whole merge and go back to before you started.
- `git checkout --ours app.txt` → keep your version entirely.
- `git checkout --theirs app.txt` → keep their version entirely.

---

## Exercise 3 — Rebase Practice 🔁

**Goal:** See how rebase replays your commits on top of an updated main.

```bash
# 1. New branch from main
git checkout main
git checkout -b feature/rebase-test

# 2. Make 3 commits on the feature branch
echo "f1" >> feature.txt && git add . && git commit -m "feat: feature commit 1"
echo "f2" >> feature.txt && git add . && git commit -m "feat: feature commit 2"
echo "f3" >> feature.txt && git add . && git commit -m "feat: feature commit 3"

# 3. Switch to main and make 2 commits (main now DIVERGES)
git checkout main
echo "m1" >> main.txt && git add . && git commit -m "chore: main commit 1"
echo "m2" >> main.txt && git add . && git commit -m "chore: main commit 2"

# 4. Back to the feature branch
git checkout feature/rebase-test

# 5. Rebase onto main
git rebase main

# 6. Compare the history
git log --oneline --graph --all
```

**What happens & why:** Rebase rewinds your 3 feature commits, fast-forwards your branch to the tip of `main`, then **re-applies** your commits one by one on top. They get **new hashes** (`f1'`, `f2'`, `f3'`) because their parent changed. The result is a clean, **linear** history with no merge commit — as if you'd started your work after main's latest commit. 📏

**Contrast:** If you had run `git merge main` instead, you'd get a merge commit and a forked graph that preserves the true timeline.

---

## Exercise 4 — Interactive Rebase (Squash) 🗜️

**Goal:** Collapse 5 messy commits into 1 clean commit.

```bash
# 1. On a feature branch, make 5 messy commits
git checkout -b feature/squash-test
echo "1" >> work.txt && git add . && git commit -m "WIP"
echo "2" >> work.txt && git add . && git commit -m "fix typo"
echo "3" >> work.txt && git add . && git commit -m "oops"
echo "4" >> work.txt && git add . && git commit -m "actually works now"
echo "5" >> work.txt && git add . && git commit -m "final version"

# 2. Start interactive rebase over the last 5 commits
git rebase -i HEAD~5
```

An editor opens:

```
pick abc1111 WIP
pick abc2222 fix typo
pick abc3333 oops
pick abc4444 actually works now
pick abc5555 final version
```

**3. Squash all into the first one** — change every line except the top to `squash` (or `s`):

```
pick   abc1111 WIP
squash abc2222 fix typo
squash abc3333 oops
squash abc4444 actually works now
squash abc5555 final version
```

Save and close. A second editor opens to write the **combined** commit message. Delete the old messages and type:

```
feat: add user greeting feature
```

**4. Verify:**

```bash
git log --oneline
# xyz9999 feat: add user greeting feature   <-- ONE clean commit
```

**Quick reference for interactive-rebase verbs:**

| Verb | Effect |
|---|---|
| `pick` | Keep the commit as-is |
| `squash` / `s` | Merge into the commit above, **keep** both messages |
| `fixup` / `f` | Merge into the commit above, **discard** this message |
| `reword` / `r` | Keep the commit but edit its message |
| `edit` / `e` | Pause to amend the commit's content |
| `drop` / `d` | Delete the commit entirely |

> ⚠️ **Golden Rule:** Only squash commits that you have NOT pushed (or that only you work on). Rebase rewrites history.

---

## Exercise 5 — Stash Workflow 🥘

**Goal:** Shelve unfinished work, do a hotfix, then restore.

```bash
# 1. Start editing a file — do NOT commit
echo "half-done feature code" >> feature.txt

# 2. Stash with a descriptive message
git stash push -m "work in progress on feature X"
#    -> working directory is now CLEAN

# 3. Switch to main and make a hotfix
git checkout main
echo "critical fix" >> hotfix.txt
git add . && git commit -m "fix: critical production hotfix"

# 4. Switch back to your branch
git checkout -    # "-" means "the previous branch"

# 5. Restore your stashed work
git stash pop

# 6. Verify your changes are back
git status        # feature.txt shows as modified again

# 7. Confirm the stash is now empty
git stash list    # (no output — pop removed it)
```

**Key distinction:**
- `git stash pop` → restore **and delete** the stash entry.
- `git stash apply` → restore but **keep** the stash (useful to apply the same WIP to several branches).
- `git stash -u` → also stash **untracked** (brand-new) files. Plain `git stash` ignores them! ⚠️

---

## Exercise 6 — Cherry Pick 🍒

**Goal:** Grab ONE bug-fix commit from `develop` onto `main`, leaving the rest behind.

```bash
# 1 & 2. Create develop with 5 commits, one being a bug fix
git checkout main
git checkout -b develop
echo "a" >> d.txt && git add . && git commit -m "feat: notification system"
echo "b" >> d.txt && git add . && git commit -m "fix: prevent duplicate payment"   # <-- THE FIX
echo "c" >> d.txt && git add . && git commit -m "feat: dashboard charts"
echo "d" >> d.txt && git add . && git commit -m "refactor: clean up user service"
echo "e" >> d.txt && git add . && git commit -m "feat: add export to CSV"

# Find the fix commit's hash
git log --oneline
#  e5... feat: add export to CSV
#  d4... refactor: clean up user service
#  c3... feat: dashboard charts
#  b2... fix: prevent duplicate payment   <-- copy this hash
#  a1... feat: notification system

# 3. Switch to the target branch
git checkout main

# 4. Cherry-pick ONLY the fix (replace b2abc with the real hash)
git cherry-pick b2abc

# 5. Verify main has the fix but none of the other develop commits
git log --oneline
```

**What happens & why:** Cherry-pick copies the *changes* from that one commit and creates a **brand-new commit on main with a different hash**. The original on `develop` is untouched. This is the classic **hotfix backport** pattern.

> ⚠️ Because the hash differs, if you later merge `develop` into `main`, that change may appear **twice**. Git is usually smart enough to handle it, but be aware.

**Multiple at once:** `git cherry-pick hash1 hash2 hash3` (specific commits) or `git cherry-pick hashA..hashB` (a range).

---

## Exercise 7 — Tags and Semantic Versioning 🏷️

**Goal:** Tag three milestone commits following SemVer (MAJOR.MINOR.PATCH).

```bash
# 1. Three commits: initial release, new feature, bug fix
echo "v1 base" >> release.txt && git add . && git commit -m "release: initial version"
git tag -a v1.0.0 -m "First stable release"          # 2. tag initial

echo "search feature" >> release.txt && git add . && git commit -m "feat: add search"
git tag -a v1.1.0 -m "Release 1.1.0: search feature" # 3. MINOR bump (new feature)

echo "bugfix" >> release.txt && git add . && git commit -m "fix: search crash"
git tag -a v1.1.1 -m "Release 1.1.1: search crash fix" # 4. PATCH bump (bug fix)

# 5. List all tags
git tag

# 6. Show details of a tag
git show v1.1.0

# 7. Push all tags to the remote
git push origin --tags
```

**SemVer cheat-sheet — which number to bump:**

| Change type | Example | Bump | Result |
|---|---|---|---|
| Bug fix (backward compatible) | Fix null pointer | **PATCH** | 1.1.0 → 1.1.1 |
| New feature (backward compatible) | Add search endpoint | **MINOR** | 1.1.1 → 1.2.0 |
| Breaking change | Rename `/users` → `/accounts` | **MAJOR** | 1.2.0 → 2.0.0 |

> 💡 **Annotated** tags (`-a`) store author, date, and a message — always use them for releases. **Lightweight** tags (`git tag v1.0.0`) are just a sticky note with no metadata.

---

## Exercise 8 — Git Bisect 🔍

**Goal:** Use binary search to find the commit that introduced "BUG".

```bash
# 1 & 2. Create 10 commits; commit #6 introduces the bug
git checkout -b bisect-test
for i in 1 2 3 4 5; do echo "line $i" >> code.txt && git add . && git commit -m "good commit $i"; done
echo "BUG" >> code.txt && git add . && git commit -m "commit 6 - introduces bug"
for i in 7 8 9 10; do echo "line $i" >> code.txt && git add . && git commit -m "commit $i"; done

# 3. Manual bisect
git bisect start
git bisect bad                 # current (HEAD) is BAD — bug present
git bisect good HEAD~9         # 9 commits back was GOOD — no bug

#   Git checks out a middle commit. Test it:
#   grep "BUG" code.txt
#   - if "BUG" found  -> git bisect bad
#   - if not found    -> git bisect good
#   Repeat ~3-4 times until Git announces the first bad commit.

# 4. AUTOMATE it — let a command decide good/bad for you
git bisect reset               # clean up the manual run first
git bisect start
git bisect bad HEAD
git bisect good HEAD~9
git bisect run grep -L "BUG" code.txt
#   grep -L prints the filename only if "BUG" is NOT present, exiting 0 (good)
#   and exits 1 (bad) when "BUG" IS present — exactly what bisect needs.

# Clean up when done
git bisect reset
```

**Why it's fast:** With 1024 commits you'd need only ~10 checks (log₂ 1024). Bisect halves the search space each step — same idea as guessing a number between 1 and 100 in 7 tries. 🧮

> 📌 Bisect convention: exit code **0 = good**, **1–127 (except 125) = bad**, **125 = skip/untestable**.

---

## Exercise 9 — Undo Everything (Reset / Revert / Reflog) ⏪

**Part A — `git reset` (the three modes):**

```bash
git checkout -b undo-test
echo "1" >> u.txt && git add . && git commit -m "commit 1"
echo "2" >> u.txt && git add . && git commit -m "commit 2"
echo "3" >> u.txt && git add . && git commit -m "commit 3"

# --soft: undo the commit, KEEP changes STAGED
git reset --soft HEAD~1
git status        # changes from "commit 3" are GREEN (staged) — ready to re-commit
git commit -m "commit 3"   # re-commit to continue

# --mixed (default): undo the commit, keep changes but UNSTAGE them
git reset --mixed HEAD~1
git status        # changes are RED (unstaged) — need git add again
git add . && git commit -m "commit 3"

# --hard: undo the commit AND DELETE the changes
git reset --hard HEAD~1
git status        # clean — changes are GONE from working directory
```

| Mode | Commit | Staging area | Working directory |
|---|---|---|---|
| `--soft` | Undone | **Kept (staged)** | Kept |
| `--mixed` | Undone | Unstaged | Kept |
| `--hard` | Undone | Deleted | **Deleted** ⚠️ |

**Part B — `git revert` (safe undo, makes a new commit):**

```bash
echo "mistake" >> u.txt && git add . && git commit -m "feat: a mistake"

# Revert it — creates a NEW commit that cancels the changes
git revert HEAD --no-edit

git log --oneline
#   ... Revert "feat: a mistake"   <-- the undo commit
#   ... feat: a mistake            <-- the original is STILL in history
```

**Part C — `git reflog` recovery (the panic button):**

```bash
# Make 3 important commits
echo "imp1" >> u.txt && git add . && git commit -m "important 1"
echo "imp2" >> u.txt && git add . && git commit -m "important 2"
echo "imp3" >> u.txt && git add . && git commit -m "important 3"

# Disaster: nuke them
git reset --hard HEAD~3        # "they're gone!" 😱

# Reflog remembers every HEAD position
git reflog
#   abc1234 HEAD@{0}: reset: moving to HEAD~3
#   def5678 HEAD@{1}: commit: important 3   <-- recover to here
#   ...

# Recover everything
git reset --hard def5678        # (use the real hash for "important 3")
git log --oneline               # all 3 commits are BACK 🎉
```

> 🛟 Reflog is your safety net for ~90 days. Even a `reset --hard` is recoverable as long as the commit existed in your local history.

---

## Exercise 10 — .gitignore 🚫

**Goal:** Ignore build/secret/IDE files, and fix a file that was committed before being ignored.

```bash
# 1. Create a Java-style project structure
mkdir -p src/main/java target .idea
touch .env application-secret.yml
touch target/app.class .idea/workspace.xml
touch src/main/java/App.java

# 2. Create .gitignore
cat > .gitignore << 'EOF'
# Build output
target/
*.class

# IDE
.idea/

# Secrets
.env
application-secret.yml
EOF

# 3. Verify ignored files do NOT appear
git status
#   Only .gitignore and src/main/java/App.java show as untracked.
#   target/, .idea/, .env, application-secret.yml are hidden. ✅

# 4. Intentionally force-add a secret (the -f overrides .gitignore)
git add -f .env && git commit -m "oops: accidentally committed .env"

# 5. .env is already in .gitignore (step 2), so no change needed there.

# 6. Untrack it WITHOUT deleting the file from disk
git rm --cached .env
git commit -m "chore: stop tracking .env (it stays on disk)"

# 7. Verify .env is no longer tracked
git status        # .env is now ignored again; the local file still exists
git ls-files | grep .env   # (no output — Git no longer tracks it)
```

**Key insight:** `.gitignore` only prevents **future** tracking — it does NOT untrack files already committed. Use `git rm --cached <file>` to stop tracking while keeping the file locally.

> 🔐 **Security note:** If a real secret was committed, removing it from the latest commit is NOT enough — it still lives in history. Rotate the credentials immediately and scrub history with BFG Repo Cleaner or `git filter-repo`.

**Pattern reference:**

| Pattern | Matches |
|---|---|
| `*.log` | Every `.log` file, any folder |
| `target/` | The `target` folder and all its contents |
| `!keep.log` | Exception: DO track this despite `*.log` |
| `**/temp/` | Any folder named `temp`, any depth |
| `doc/*.pdf` | PDFs directly in `doc/` only |

---

## Exercise 11 — Complete Workflow (Combines Everything) 🏆

**Scenario:** You're building an e-commerce app and a hotfix interrupts your cart feature. This stitches together branching, stash, cherry-pick/merge, tags, and interactive rebase.

```bash
# 1. Initialize and create main with initial code
git init ecommerce && cd ecommerce
echo "e-commerce app" > README.md
git add . && git commit -m "chore: initial project setup"

# 2. Set up .gitignore for Java
printf "target/\n.idea/\n.env\n*.class\n" > .gitignore
git add .gitignore && git commit -m "chore: add gitignore"

# 3. Create a develop branch
git checkout -b develop

# 4. Create feature/cart from develop — add cart code
git checkout -b feature/cart
echo "cart logic v1" > cart.txt
git add . && git commit -m "feat: start shopping cart"

# 5 & 6. Urgent bug arrives — stash the in-progress cart work
echo "cart logic v2 (incomplete)" >> cart.txt   # unfinished edit
git stash push -m "WIP shopping cart"

# 7. Create hotfix/payment-fix from main
git checkout main
git checkout -b hotfix/payment-fix
echo "payment fix" > payment.txt
git add . && git commit -m "fix: payment double-charge bug"

# 8. Merge the hotfix into BOTH main and develop
git checkout main && git merge hotfix/payment-fix
git checkout develop && git merge hotfix/payment-fix

# 9. Tag main as a patch release
git checkout main
git tag -a v1.0.1 -m "Patch: payment double-charge fix"

# 10. Back to feature/cart and restore the stash
git checkout feature/cart
git stash pop

# 11. Finish the cart with 4 messy commits
git add . && git commit -m "WIP cart"
echo "qty" >> cart.txt && git add . && git commit -m "fix typo"
echo "totals" >> cart.txt && git add . && git commit -m "oops forgot totals"
echo "done" >> cart.txt && git add . && git commit -m "final"

# 12. Squash the 4 messy commits into 1 clean one
git rebase -i HEAD~4
#   set the first to 'pick', the other three to 'squash',
#   then write the message: "feat: add shopping cart"

# 13. Merge feature/cart into develop
git checkout develop && git merge feature/cart

# 14. Create a release branch from develop
git checkout -b release/v1.1.0
#   (final testing / version bumps happen here)

# 15. Merge release into main
git checkout main && git merge release/v1.1.0

# 16. Tag the minor release
git tag -a v1.1.0 -m "Release 1.1.0: shopping cart"

# 17. Admire the history
git log --oneline --graph --all
```

This single exercise exercises **every** Week-02 concept: branching strategy (Git Flow), stash, hotfix + cherry-pick-style backport via merge, SemVer tags, and interactive rebase squashing. If you can do this from memory, you're interview-ready. 💪

---

# PART B — Interview Questions & Model Answers

These are the **18 most likely Git questions** for a DevOps interview, each with a confident, spoken-style answer you can deliver out loud.

---

### Q1. What is the difference between `git merge` and `git rebase`? 🔀

> **Merge** combines two branches by creating a new **merge commit** with two parents — it preserves the true, branching history. **Rebase** instead **replays** my commits on top of another branch, rewriting them with new hashes to produce a clean, linear history.
>
> I use **rebase** to update my local feature branch with the latest `main` (keeps history tidy), and **merge** to bring a finished feature into `main` (preserves the fact that a branch existed). The cardinal rule: **never rebase a shared branch** that others have pulled, because it rewrites history they depend on.

| | Merge | Rebase |
|---|---|---|
| History | Preserved, branching | Rewritten, linear |
| Extra commit | Yes (merge commit) | No |
| Hashes | Unchanged | Changed |
| Safe on shared branches? | ✅ Yes | ❌ No |
| Best for | Integrating finished features | Updating your own feature branch |

---

### Q2. Explain `git reset --soft`, `--mixed`, and `--hard`. 🎚️

> All three move the branch pointer back to an earlier commit; they differ in what they do to your changes.
>
> - **`--soft`** undoes the commit but keeps the changes **staged** — perfect for redoing a commit message or adding more files.
> - **`--mixed`** (the default) undoes the commit and keeps the changes but **unstages** them — I'll need to `git add` again.
> - **`--hard`** undoes the commit **and deletes** the changes from the working directory — the nuclear option.

| Mode | Commit | Staging | Working dir |
|---|---|---|---|
| `--soft` | Undone | Kept (staged) | Kept |
| `--mixed` | Undone | Unstaged | Kept |
| `--hard` | Undone | Gone | **Gone** ⚠️ |

> Memory hook: **soft** = gentle (everything kept & staged), **hard** = brutal (everything wiped).

---

### Q3. When do you use `git revert` instead of `git reset`? ↩️

> **`reset`** rewrites history by moving the branch pointer backward — safe only for commits I **haven't pushed**. **`revert`** creates a **new commit** that cancels out a previous one, leaving the original in history — safe for commits that **are already pushed** and shared.
>
> So the rule is simple: **not pushed → reset; already pushed → revert.** Revert never rewrites history, so it won't break my teammates' clones.

```bash
git revert <hash>        # undo a pushed commit safely
git revert -m 1 <merge>  # undo a merge commit (keep parent #1)
```

---

### Q4. What is `git cherry-pick` and when would you use it? 🍒

> Cherry-pick copies the changes from **one specific commit** and applies them onto my current branch as a **new commit** (with a new hash). The classic use case is a **hotfix backport**: a critical fix is sitting in `develop` among many untested commits, but I only need *that one fix* on `main`. I cherry-pick it rather than merging all of develop.
>
> `git cherry-pick <hash>`. I'm careful with it though — if I cherry-pick a lot, I should probably just merge, and I remember the cherry-picked commit has a different hash than the original, which can cause duplicates after a later merge.

---

### Q5. What is `git stash` and how does it work? 🥘

> `git stash` shelves my uncommitted changes and reverts my working directory to a clean state, so I can switch tasks without committing half-finished work. It's like setting a half-cooked dish aside, answering the door, then putting it back on the stove.
>
> - `git stash` → save and clean.
> - `git stash pop` → restore and remove the stash.
> - `git stash apply` → restore but keep the stash (to reuse on multiple branches).
> - `git stash list` → see all stashes; `git stash -u` → also include untracked files.
>
> I use it for quick context switches — a production hotfix interrupting feature work — not as long-term storage; that's what branches are for.

---

### Q6. What's the difference between `git fetch` and `git pull`? 📥

> **`git fetch`** downloads the latest commits from the remote into my local tracking branches but **does NOT change my working files** — it just updates my knowledge of the remote. **`git pull`** is `fetch` + `merge` — it downloads *and* immediately merges those changes into my current branch.
>
> I prefer `fetch` then review with `git log origin/main` before deciding to merge or rebase — it's safer. `pull` is the quick one-step version. Tip: `git pull --rebase` rebases instead of merging, avoiding noisy merge commits.

| | `git fetch` | `git pull` |
|---|---|---|
| Downloads remote commits | ✅ | ✅ |
| Modifies your working branch | ❌ | ✅ (auto-merges) |
| Equivalent to | — | `fetch` + `merge` |

---

### Q7. What is `HEAD`, and what is a "detached HEAD"? 🧭

> **`HEAD`** is a pointer to my current location — usually it points at the branch I'm on, which in turn points at the latest commit. A **detached HEAD** happens when I check out a specific **commit hash or tag** directly instead of a branch, so HEAD points straight at a commit with no branch attached.
>
> In that state, any new commits I make aren't on a branch and can be lost once I switch away. If I want to keep work done there, I create a branch: `git checkout -b new-branch`. To get back to normal I just `git checkout main`.

```bash
git checkout abc1234     # detached HEAD
git checkout -b fix-here # rescue work by giving it a branch
```

---

### Q8. What is a fast-forward merge? ⏩

> A fast-forward merge happens when the target branch hasn't diverged — it has **no new commits** since I branched off. Git simply slides the branch pointer forward to my latest commit; **no merge commit** is created and history stays perfectly linear.
>
> If both branches have new commits, a fast-forward isn't possible and Git makes a real merge commit instead. I can force a merge commit even when fast-forward is possible with `git merge --no-ff` — useful when I want history to clearly show that a feature branch existed.

---

### Q9. How do you undo the last commit? 🔙

> It depends on whether I've pushed it:
>
> - **Not pushed, want to keep & redo:** `git reset --soft HEAD~1` (changes stay staged).
> - **Not pushed, want to discard everything:** `git reset --hard HEAD~1`.
> - **Just fix the message:** `git commit --amend`.
> - **Already pushed:** `git revert HEAD` — creates a safe undo commit without rewriting shared history.
>
> The golden rule: if it's been pushed and others may have it, **always revert, never reset.**

---

### Q10. How do you resolve a merge conflict? ⚔️

> A conflict happens when two branches change the **same lines** of the same file and Git can't decide which to keep. The steps:
>
> 1. Run `git status` to see which files conflict.
> 2. Open each file and find the markers: `<<<<<<< HEAD` (my version), `=======` (divider), `>>>>>>> branch` (their version).
> 3. Edit to the final desired content and **delete all the markers**.
> 4. `git add <file>` to mark it resolved, then `git commit`.
>
> If things go sideways I can `git merge --abort` to start over. To avoid conflicts in the first place I pull frequently and keep branches short-lived.

---

### Q11. Explain Git branching strategies: Git Flow vs Trunk-Based vs GitHub Flow. 🌳

> - **Git Flow** is the most structured: five branch types (`main`, `develop`, `feature/*`, `release/*`, `hotfix/*`). Best for **large teams with scheduled releases** like a banking app — but it's heavy.
> - **Trunk-Based** has everyone commit to `main` via tiny, short-lived branches (hours, not days), hiding incomplete work behind **feature flags**. Best for teams with **strong CI/CD** deploying many times a day — Google, Microsoft.
> - **GitHub Flow** is the simplest: just `main` plus feature branches, everything merged via **Pull Requests**, and `main` is always deployable. Best for **small-to-medium teams** and open source.

| | Git Flow | Trunk-Based | GitHub Flow |
|---|---|---|---|
| Complexity | High | Low | Low |
| Branches | 5 types | 1 + tiny | 2 types |
| Releases | Scheduled | Continuous | Continuous |
| Best for | Large/regulated teams | Fast CI/CD teams | Small-medium teams |

---

### Q12. What is the staging area (index) in Git? 🛒

> The staging area — also called the **index** — is a middle layer between my working directory and the repository. When I `git add` a file, I'm placing it in the staging area, saying "this change is ready to be part of the next commit." Then `git commit` snapshots exactly what's staged.
>
> It's like a **shopping cart**: I add items to the cart (`git add`) without buying yet, and checkout (`git commit`) finalizes the purchase. This two-step design lets me craft precise commits — e.g. staging only some of my changes with `git add -p`.

> Git's three areas: **Working Directory** (your edits) → **Staging Area** (`git add`) → **Repository** (`git commit`).

---

### Q13. What is squashing and why squash commits? 🗜️

> Squashing combines several commits into one. During development I make messy commits — "WIP", "fix typo", "oops" — but my team doesn't need to see those. Before opening a PR I run `git rebase -i HEAD~N` and mark the extras as `squash` (or `fixup`) so they fold into one clean commit like "feat: add user login."
>
> It's like sending my manager the **final report**, not all my rough drafts with coffee stains. Many teams also enable **"Squash and merge"** in GitHub so each PR lands as a single tidy commit on `main`.

---

### Q14. What are Git tags and how do they differ from branches? 🏷️

> A **tag** is a permanent label pointing to a **specific commit** — typically marking a release like `v2.1.0`. Unlike a **branch**, a tag doesn't move; it's a fixed bookmark. Branches advance with every new commit, tags stay put.
>
> There are two kinds: **lightweight** (just a name) and **annotated** (`git tag -a v1.0.0 -m "..."`, storing author/date/message). I always use annotated for releases. Tags aren't pushed by default — I run `git push origin --tags`. They let me instantly check out exactly which code shipped as "version 2.1.3" when a customer reports a bug.

---

### Q15. What are Git hooks and give a real example. 🪝

> Git hooks are scripts that run **automatically** at certain points in the Git workflow — like airport security checkpoints you can't skip. They live in `.git/hooks/`.
>
> Common ones: **`pre-commit`** (run a linter or format code before the commit is made), **`commit-msg`** (enforce commit message format), and **`pre-push`** (run tests before pushing). A pre-commit hook that runs the linter and `exit 1`s on failure will **block** a bad commit.
>
> The catch: hooks are **local** and not committed to the repo, so new teammates don't get them automatically. Teams fix this with tools like **Husky** or by enforcing the same checks **server-side in CI/CD**, which can't be bypassed.

---

### Q16. What is `git bisect` and how does it help debugging? 🔍

> `git bisect` uses **binary search** to pinpoint the exact commit that introduced a bug. I tell Git a known **good** commit and a known **bad** one; it checks out the midpoint and asks me to test it. Based on my "good"/"bad" answer it halves the range and repeats — so with 1000 commits I only need about 10 checks instead of 1000.
>
> I can even automate it: `git bisect run <test-command>` lets a script return the verdict at each step, and Git finds the guilty commit hands-free. `git bisect reset` cleans up afterward. It's a lifesaver for "this worked last release but is broken now."

---

### Q17. What is `git reflog` and when does it save you? 🛟

> `git reflog` records **every position HEAD has been at** — every commit, checkout, reset, and rebase — like a browser history for Git. Its superpower: recovering "lost" commits after a mistake.
>
> If I run `git reset --hard HEAD~3` and panic because three commits vanished, they're still in the reflog. I find the hash from before the reset and run `git reset --hard <hash>` to bring everything back. It also recovers accidentally deleted branches. Entries persist for about **90 days** before garbage collection, so it's a safety net, not permanent storage.

---

### Q18. Monorepo vs Multi-repo — what's the trade-off? 🏠

> A **monorepo** keeps all services in **one repository**; a **multi-repo** gives each service its **own**. 
>
> Monorepos make **code sharing and atomic cross-service refactors** trivial — one PR updates an API and all its callers — but they grow huge and need special tooling (Bazel, Nx). Multi-repos give teams **autonomy and simple per-repo CI/CD and access control**, but sharing code means publishing versioned packages and coordinating across repos.
>
> Google and Meta run massive monorepos; Amazon and Netflix lean multi-repo for microservice independence. I'd pick **monorepo** for tightly-coupled services and small-to-medium teams, **multi-repo** for large, independent teams with very different tech stacks.

---

## 📋 Command Cheat-Sheet

```
BASICS                          BRANCHING & MERGING
------                          -------------------
git init                        git checkout -b <name>   # create + switch
git clone <url>                 git merge <branch>       # combine (merge commit)
git add <file> | .              git merge --no-ff <b>    # force a merge commit
git commit -m "msg"             git rebase <branch>      # replay, linear history
git status                      git rebase -i HEAD~N     # interactive (squash)
git log --oneline --graph --all git branch -d <name>     # delete merged branch

UNDOING                         STASH
-------                         -----
git reset --soft  HEAD~1        git stash                # shelve changes
git reset --mixed HEAD~1        git stash -u             # include untracked
git reset --hard  HEAD~1        git stash pop            # restore + remove
git revert <hash>               git stash apply          # restore, keep stash
git revert -m 1 <merge>         git stash list
git commit --amend              git stash push -m "msg"
git reflog                      git stash drop stash@{N}

ADVANCED                        TAGS
--------                        ----
git cherry-pick <hash>          git tag -a v1.0.0 -m "msg"
git cherry-pick A..B            git tag                  # list
git bisect start/bad/good       git show <tag>
git bisect run <cmd>            git push origin --tags
git fetch                       git tag -d <tag>         # delete local

CONFLICTS                       CLEANUP
---------                       -------
git merge --abort               .gitignore
git checkout --ours <file>      git rm --cached <file>   # untrack, keep file
git checkout --theirs <file>    git push origin --delete <branch/tag>
```

---

## 🧠 Memory Hooks — Analogies That Stick

| Concept | Analogy | Trigger to Remember |
|---|---|---|
| **Staging area / `git add`** | 🛒 Shopping cart | Items in the cart, not bought yet — `commit` is checkout |
| **`git commit`** | 🧾 The actual purchase / passbook entry | Permanently recorded with a receipt (hash) |
| **Branch** | 🛣️ Service lane off the highway | Do your work without disturbing main traffic |
| **`git merge`** | 📎 Stapling two diaries together | Both timelines kept; jumps back and forth |
| **`git rebase`** | ✍️ Rewriting your diary entries to come after | Clean linear story, but dates (hashes) faked |
| **Interactive rebase squash** | 📄 Final report, not rough drafts | No "WIP" and "coffee-stain" commits |
| **`git stash`** | 🥘 Dal set aside while you answer the door | Shelve, switch, come back, `pop` |
| **`git cherry-pick`** | 🍒 Taking just the gulab jamun from the thali | One commit, not the whole branch |
| **`git tag`** | 🛣️ Milestone marker "Mumbai 100km" | A fixed bookmark for a release |
| **Git hooks** | 🛂 Airport security checkpoint | Auto-checks you can't skip before commit/push |
| **`.gitignore`** | 🧳 The "do NOT pack" list | Leave secrets, builds, node_modules behind |
| **`git bisect`** | 🔍 Guess-the-number in 7 tries | Binary search to the guilty commit |
| **`git reset --hard`** | 🗑️ Throwing the envelope in a shredder | Changes permanently gone |
| **`git revert`** | 📝 "My March 15 entry was wrong, here's the fix" | New commit cancels old; history preserved |
| **`git reflog`** | 🌐 Browser history | Recover "deleted" commits within ~90 days |
| **`HEAD`** | 📍 "You are here" pin on a map | Detached = pin floating on a commit, not a branch |
| **Fast-forward merge** | ⏩ Sliding a bookmark forward | No divergence, no merge commit |
| **Monorepo** | 🏠 Joint family home | Everyone under one roof, shared everything |
| **Multi-repo** | 🏢 Separate apartments | Independent, but coordination takes effort |

---

## 🎤 Final Interview Tip

When you're asked a Git question, **don't just recite the command — tell the story**. Use the four-beat rhythm: *what it is → why it exists → the command → when (and when NOT) to use it.* 

And whenever you can, end with a **safety caveat** — "...but I'd never rebase a shared branch," or "...if it's pushed, I revert instead of reset." That one extra sentence is what separates a junior who *memorized* Git from a senior who *understands* it. Interviewers notice. 🌟

Now go practice every exercise in PART A on a throwaway repo where mistakes don't matter — muscle memory is what makes you calm and confident in the room. You've got this! 💪

---

**End of Week 02 — Git Advanced — Interview Answers & Exercise Solutions**
