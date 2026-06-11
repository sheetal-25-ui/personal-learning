# Module 10 — Day 1: GitHub Portfolio

## Why GitHub Matters (A Lot)

When you apply for an AI/ML job, the recruiter has 2 minutes and a stack of 200 resumes. A link to a clean GitHub with real projects is the single fastest way to say: **"I can actually build things, not just talk about them."**

Here's the truth:
- **Recruiters and hiring managers WILL click your GitHub.** It's often the first thing they check after your resume.
- **A good repo beats a fancy degree** for engineering roles. Code is proof.
- **It's free credibility.** Anyone in the world can see your work, including people at Anthropic, Microsoft, and Google.

Think of GitHub as your **public lab notebook**. You did the hard work in Module 8 (5 real projects). Now we make that work *visible and impressive*.

---

## What a Recruiter Actually Looks For

Before we build anything, know what they're scanning for. This is your checklist for every repo:

| What they check | What "good" looks like |
|-----------------|------------------------|
| **Does it have a README?** | Yes — clear, with screenshots and how to run it |
| **Can I tell what it does in 10 seconds?** | One-sentence description at the top |
| **Is the code organized?** | Folders make sense, no 1000-line single file |
| **Does it run?** | Clear setup steps, `requirements.txt` present |
| **Is it real or copy-pasted?** | Your own commits, your own explanations |
| **Is the profile active?** | Green squares (commits), pinned projects |

**The #1 mistake beginners make:** uploading code with NO README. A repo with no README looks abandoned. We will never do that.

---

## Step 1: Create a Professional GitHub Account

1. Go to **github.com** → Sign up
2. **Pick a clean username.** Use your real name if possible: `sheetal-tyagi`, `sheetaltyagi`, or `styagi-ai`. Avoid `cooldev2099` or `xX_sheetal_Xx`.
3. Add a **real photo** (or a clean avatar) and your real name.
4. Write a one-line bio: *"AI/ML Engineer | Building with PyTorch & LLMs | Open to opportunities"*

> Your GitHub URL becomes part of your professional identity. Put it on your resume and LinkedIn.

---

## Step 2: Push a Project to GitHub (The Right Way)

You'll do this for each of your 5 Module 8 projects. Here's the full flow for ONE project.

### A. Install Git (one time)
```bash
git --version
```
If you don't have it, on Mac it'll prompt you to install. Or get it from git-scm.com.

### B. Set up your identity (one time)
```bash
git config --global user.name "Sheetal Tyagi"
git config --global user.email "tyagisheetal2024@gmail.com"
```

### C. Create the repo on GitHub
1. Click the **+** (top right) → **New repository**
2. Name it clearly: `movie-recommendation-system` (lowercase, dashes, descriptive)
3. Add a description: *"Collaborative filtering movie recommender built with Pandas + scikit-learn"*
4. Make it **Public**
5. Click **Create repository**

### D. Push your local code
In Terminal, go to your project folder and run:
```bash
cd ~/Desktop/movie-recommendation-system

git init
git add .
git commit -m "Initial commit: movie recommendation system"
git branch -M main
git remote add origin https://github.com/sheetal-tyagi/movie-recommendation-system.git
git push -u origin main
```

**Done!** Refresh the GitHub page — your code is live.

### E. Add a `.gitignore` (so you don't upload junk)
Create a file named `.gitignore` in your project with:
```
# Python
__pycache__/
*.pyc
venv/
.env

# Data / models (too big for GitHub)
*.csv
*.pkl
*.pth
data/

# Mac
.DS_Store
```
> **Never** upload API keys (`.env`), virtual environments, or huge data/model files. If a file is over ~50 MB, don't push it — explain in the README how to download it instead.

---

## Step 3: Write a Great README (Copy This Template)

The README is the **most important file** in your repo. Save this as `README.md` in each project folder and fill in the blanks.

```markdown
# Movie Recommendation System

One-line description: Recommends movies to users using collaborative filtering,
trained on the MovieLens dataset.

![Demo screenshot](screenshot.png)

## What It Does
- Takes a user's past ratings and suggests 10 movies they'll likely enjoy
- Uses item-based collaborative filtering with cosine similarity
- Handles ~100,000 ratings across 1,000+ movies

## Tech Stack
- Python 3.11
- Pandas, NumPy, scikit-learn
- Jupyter Notebook for analysis

## How It Works (Short Version)
1. Load and clean the ratings data with Pandas
2. Build a user-item matrix
3. Compute similarity between movies using cosine similarity
4. For a given user, rank unseen movies by predicted rating

## Results
- Achieves an RMSE of 0.89 on the test set
- Recommendations feel sensible (e.g., recommends "Toy Story 2" to "Toy Story" fans)

## How to Run It
```bash
# 1. Clone the repo
git clone https://github.com/sheetal-tyagi/movie-recommendation-system.git
cd movie-recommendation-system

# 2. Install dependencies
pip install -r requirements.txt

# 3. Download the data (link below) and place in data/
#    Dataset: https://grouplens.org/datasets/movielens/

# 4. Run
python recommend.py
```

## What I Learned
- How collaborative filtering actually works under the hood
- Why the user-item matrix gets sparse and how to handle it
- Trade-offs between user-based and item-based filtering

## Future Improvements
- Add matrix factorization (SVD) for better accuracy
- Build a simple web UI with Streamlit
```

### Why each section matters
| Section | Why it's there |
|---------|----------------|
| **One-liner + screenshot** | Recruiter understands it in 10 seconds |
| **What It Does** | Shows you can explain value, not just code |
| **Tech Stack** | Matches keywords recruiters search for |
| **How It Works** | Proves you understand your own project |
| **Results** | Numbers = credibility (RMSE, accuracy, etc.) |
| **How to Run** | Shows it's a working, real thing |
| **What I Learned** | Shows growth mindset — interviewers love this |

> **Pro tip:** Always include at least one screenshot or GIF. A picture of your project working is worth a thousand words. Use Mac's screenshot tool (Cmd+Shift+4) and save it as `screenshot.png` in the repo.

---

## Step 4: Add a `requirements.txt`

This file lists what someone needs to install. Generate it automatically:
```bash
pip freeze > requirements.txt
```
Or write it by hand (cleaner) with just what the project actually uses:
```
pandas==2.2.0
numpy==1.26.0
scikit-learn==1.4.0
```

---

## Step 5: Pin Your Best Projects

Pinned repos appear at the top of your profile. You get **6 pins** — use them wisely.

1. Go to your profile page
2. Click **Customize your pins**
3. Select your 5 Module 8 projects (in order from most impressive to least)

**Suggested pin order for your projects:**
1. **RAG App (Chat with your documents)** — most advanced, most in-demand skill
2. **AI Chatbot with Claude API** — LLM/API skills, very relevant in 2026
3. **Image Classifier (Cat vs Dog)** — deep learning + PyTorch
4. **Sentiment Analyzer** — NLP
5. **Movie Recommendation System** — classic ML fundamentals

> Lead with your strongest, most modern work. RAG and LLM projects are exactly what AI labs hire for right now.

---

## Step 6: Create a Profile README (The Secret Weapon)

A **profile README** is a special repo that shows up at the top of your GitHub profile. Most beginners don't know this exists — having one instantly makes you look more professional.

### How to create it
1. Create a new repo with the **exact same name as your username** (e.g., `sheetal-tyagi`)
2. Check "Add a README"
3. Edit the `README.md`

### Copy this template
```markdown
# Hi, I'm Sheetal 👋

I'm an aspiring **AI/ML Engineer** focused on machine learning, deep learning,
and large language models. I love turning messy data and big ideas into working software.

## 🔭 What I'm Working On
- Building end-to-end ML projects (recommendation, NLP, computer vision)
- Learning RAG, embeddings, and LLM application development
- Sharpening my data structures & algorithms (interview prep)

## 🛠️ Tech I Use
**Languages:** Python, SQL
**ML/DL:** PyTorch, scikit-learn, Pandas, NumPy
**LLMs:** Claude API, embeddings, vector databases, RAG
**Tools:** Git, Jupyter, VS Code

## 📌 Featured Projects
- [RAG App — Chat With Your Documents](link) — retrieval-augmented generation pipeline
- [AI Chatbot with Claude API](link) — LLM app with tool use
- [Image Classifier](link) — CNN built in PyTorch with transfer learning

## 📫 Reach Me
- LinkedIn: [your-linkedin]
- Email: tyagisheetal2024@gmail.com

*Open to AI/ML Engineer roles. Let's build something.*
```

> Fill in real links once your projects are pushed. This README is the first thing people see — make it count.

---

## Step 7: Keep the Green Squares Going

GitHub shows a contribution graph (those green squares). An active graph signals "this person codes regularly."

- **Commit a little, often** — even small improvements to your projects count
- Don't fake it with empty commits — recruiters can tell
- Aim for steady activity while job hunting (a few commits a week)

---

## How to Present Your Module 8 Projects

For each of your 5 projects, make sure it has:

- [ ] A descriptive repo name (lowercase-with-dashes)
- [ ] A repo description (one line, under the repo name)
- [ ] A complete README (use the template above)
- [ ] At least one screenshot
- [ ] A `requirements.txt`
- [ ] A `.gitignore`
- [ ] Clean, organized code (no secrets, no junk files)
- [ ] A "What I Learned" section

Here's a quick reference for what each project demonstrates — use these phrases in your READMEs and resume:

| Project | Headline skill | Keywords to include |
|---------|----------------|---------------------|
| Movie Recommender | Classic ML | collaborative filtering, Pandas, cosine similarity |
| Image Classifier | Computer vision | CNN, PyTorch, transfer learning |
| Sentiment Analyzer | NLP | text classification, tokenization, scikit-learn |
| AI Chatbot | LLM apps | Claude API, prompt engineering, tool use |
| RAG App | Modern AI | embeddings, vector database, retrieval-augmented generation |

---

## Practice Exercises

### Exercise 1: Create your GitHub account
- Sign up, pick a clean username, add a photo and bio.
- Write down your profile URL: `github.com/__________`

### Exercise 2: Push your first project
- Take your Movie Recommendation System (or any Module 8 project)
- Follow Steps 2–4 to push it with a README, requirements.txt, and .gitignore
- Confirm it appears live on GitHub

### Exercise 3: Write 5 READMEs
- Use the template to write a complete README for **all 5** Module 8 projects
- Each must have: one-liner, screenshot, tech stack, how to run, results, what you learned

### Exercise 4: Build your profile README
- Create the special `<username>` repo
- Fill in the profile README template with your real info and project links

### Exercise 5: Pin and polish
- Pin all 5 projects in the suggested order
- Run through the "How to Present Your Module 8 Projects" checklist for each repo

---

## What's Next?

Tomorrow in **Day 02**, we'll build your **AI/ML resume** — the document that gets you the interview:
- How to structure a resume for AI roles
- How to describe projects with impact (action + tool + result)
- How to tailor it for ML Engineer vs Data Scientist vs Research roles
- A sample resume snippet you can copy

Your work is now public and professional. Recruiters can finally *see* what you can do. This is a huge step — be proud of it!
