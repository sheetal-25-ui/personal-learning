# Module 10 — Day 2: Resume for AI Roles

## Your Resume Has One Job

Get you the interview. That's it. It's not your life story — it's a **30-second sales pitch** that makes a recruiter think *"I need to talk to this person."*

Two hard truths about AI/ML resumes:
1. **A human spends ~7 seconds on the first scan.** Make the top half count.
2. **A robot reads it first.** Most companies use an ATS (Applicant Tracking System) that filters resumes by keywords before a human ever sees them. If the job says "PyTorch" and your resume says "deep learning library," you might get filtered out.

So we write for **both**: clear and human-friendly on top, keyword-rich underneath.

---

## The Structure (In Order)

Use a **single column, single page** (two pages only if you have 5+ years experience). Fancy multi-column designs confuse the ATS robot — keep it simple.

| Section | What goes here | Why |
|---------|----------------|-----|
| **1. Header** | Name, email, phone, LinkedIn, GitHub | So they can contact you |
| **2. Summary** (optional) | 2-line pitch of who you are | Hooks the reader |
| **3. Skills** | Languages, ML frameworks, tools | ATS keywords + fast scan |
| **4. Projects** | Your 3–5 best, with impact | THE most important part for you |
| **5. Experience** | Jobs (even non-AI ones) | Shows you can hold a job |
| **6. Education** | Degree, relevant courses | Baseline qualification |
| **7. Certifications** (optional) | Courses, this AI training | Bonus credibility |

> **For someone breaking into AI, Projects > Experience.** If you don't have AI work experience yet, your projects ARE your experience. Put them above your unrelated jobs.

---

## Section 1: Header

Keep it clean. No need for your full address — city is enough.
```
SHEETAL TYAGI
AI/ML Engineer
Mumbai, India | tyagisheetal2024@gmail.com | +91-XXXXX-XXXXX
linkedin.com/in/sheetal-tyagi | github.com/sheetal-tyagi
```
> **Make the GitHub and LinkedIn clickable links.** Recruiters will click them.

---

## Section 2: Summary (2 Lines, Optional but Powerful)

A short pitch. Tailor it to the role you want.
```
Aspiring AI/ML Engineer with hands-on experience building end-to-end ML
projects across NLP, computer vision, and LLM applications. Strong Python
foundation, eager to apply machine learning to real-world problems.
```
> Skip the empty buzzwords ("hardworking team player, passionate self-starter"). Lead with **what you can build**.

---

## Section 3: Skills (Your Keyword Goldmine)

Group them so they're scannable AND ATS-friendly. List skills you can actually talk about — interviewers will ask.
```
SKILLS
Languages:        Python, SQL
ML / Deep Learning:  PyTorch, scikit-learn, Pandas, NumPy, Matplotlib
LLMs / NLP:       Claude API, embeddings, RAG, vector databases, prompt engineering
Tools:            Git, GitHub, Jupyter, VS Code, Docker (basics)
Concepts:         supervised learning, neural networks, CNNs, transformers, model evaluation
```
> **Honesty rule:** Only list what you can defend in an interview. If you put "Docker," expect a Docker question. "Basics" is an honest, safe qualifier.

---

## Section 4: Projects (The Heart of Your Resume)

This is where you win. Each project gets a title and **2–4 bullet points**. And every bullet follows the magic formula:

### The Impact Formula: Action + Tool + Result

> **[Strong verb] + [what you built with which tool] + [the measurable outcome]**

Compare these:

| ❌ Weak (what beginners write) | ✅ Strong (what gets interviews) |
|-------------------------------|--------------------------------|
| "Made a movie recommender" | "Built a collaborative-filtering recommender in Python/Pandas that suggests 10 personalized movies, achieving an RMSE of 0.89 on 100K ratings" |
| "Used PyTorch for image classification" | "Trained a CNN with transfer learning (ResNet-18) in PyTorch to classify cat vs. dog images at 94% test accuracy" |
| "Built a chatbot" | "Developed an AI chatbot using the Claude API with tool use, enabling it to fetch live data and answer multi-step questions" |

See the difference? **Numbers and specifics turn a hobby into engineering.**

### Strong verbs to start bullets with
*Built, Developed, Trained, Designed, Implemented, Engineered, Optimized, Deployed, Automated, Reduced, Improved, Achieved.*

Avoid weak openers: *worked on, helped with, was responsible for, did.*

### Sample Projects section (using your Module 8 work)
```
PROJECTS

RAG App — Chat With Your Documents     |  github.com/sheetal-tyagi/rag-app
- Built a retrieval-augmented generation pipeline that lets users ask questions
  about their own PDFs, combining embeddings, a vector database, and the Claude API
- Implemented document chunking and semantic search, reducing irrelevant answers
  by retrieving only the top-k most similar passages
- Deployed as a Streamlit web app for live demos

AI Chatbot with Claude API             |  github.com/sheetal-tyagi/ai-chatbot
- Developed a conversational AI assistant using the Claude API with tool use and
  prompt engineering to handle multi-turn, context-aware conversations
- Designed structured prompts and a fallback flow, improving answer relevance

Image Classifier (Cat vs Dog)          |  github.com/sheetal-tyagi/image-classifier
- Trained a CNN in PyTorch using transfer learning (ResNet-18), reaching 94% test
  accuracy on 10K labeled images
- Applied data augmentation and early stopping to reduce overfitting
```

> **Always link each project to its GitHub repo.** This connects yesterday's work (Day 01) directly to your resume.

---

## Section 5: Experience

List your jobs — even non-AI ones. Frame them to show transferable skills (problem-solving, ownership, data, automation).
```
EXPERIENCE

Data Analyst — Acme Corp                              2022 – 2024
- Automated weekly reporting with Python and Pandas, cutting manual work by 8 hours/week
- Built dashboards that informed decisions for a team of 15
```
> No AI experience? That's fine. Show that you **deliver results and can code**. The projects carry the AI story.

---

## Section 6: Education
```
EDUCATION

B.Tech in Computer Science — XYZ University           2018 – 2022
Relevant coursework: Linear Algebra, Probability, Data Structures, Machine Learning
```

---

## Section 7: Certifications / Training (Optional)
```
TRAINING
- AI/ML Engineering Self-Study Program (2026) — Python, ML, Deep Learning,
  Transformers/LLMs, 5 hands-on projects
```
> Your structured AI training IS worth listing. It shows initiative and a real curriculum.

---

## Tailoring: Same Resume, Different Emphasis

These three roles overlap but want different things. **Reorder your skills and projects to match the role.** Don't lie — just lead with what's relevant.

| | **ML Engineer** | **Data Scientist** | **ML Research** |
|--|----------------|--------------------|-----------------|
| **What they care about** | Building & shipping models into software | Insights from data, experiments, statistics | Novel methods, papers, deep math |
| **Lead with** | Engineering: PyTorch, deployment, APIs, clean code | Analysis: Pandas, SQL, statistics, A/B testing, visualization | Theory: math, papers read/implemented, from-scratch builds |
| **Highlight these projects** | RAG App, Chatbot, Image Classifier (deployed) | Movie Recommender, Sentiment Analyzer (analysis-heavy) | Anything you built *from scratch* + papers reproduced |
| **Resume keywords** | deployment, production, API, scalable, Docker | experimentation, statistics, insights, SQL, dashboards | research, novel, reproduced, mathematics, publications |

> **Practical tip:** Keep ONE master resume, then make a tailored copy per role type. Save them as `resume-mle.pdf`, `resume-ds.pdf`, etc. Adjust the summary line and project order — that's usually enough.

---

## ATS Survival Checklist (Don't Get Filtered Out)

- [ ] **Save as PDF** (unless the application asks for `.docx`)
- [ ] **Single column** layout — no fancy tables/text boxes that robots can't read
- [ ] **Standard section headers** ("Skills," "Experience," "Projects" — not "My Journey")
- [ ] **Match keywords from the job posting** — if it says "TensorFlow" and you know it, include it
- [ ] **No images/icons for important text** (the robot can't read text inside an image)
- [ ] **Standard font** (Arial, Calibri, Helvetica), size 10–12
- [ ] **Spell-check** — a single typo can cost you the interview

---

## Common Mistakes to Avoid

| Mistake | Fix |
|---------|-----|
| Listing every skill you've ever touched | List only what you can defend in an interview |
| Vague bullets ("worked on ML stuff") | Use Action + Tool + Result with numbers |
| No GitHub links | Link every project to its repo |
| 2+ pages with no experience | One page. Tight. Punchy. |
| Buzzword soup ("synergistic AI-driven solutions") | Plain, specific language |
| Same resume for every job | Tailor the top + project order per role |
| No numbers anywhere | Add metrics: accuracy, RMSE, time saved, dataset size |
| Lying about skills | Don't. You WILL be caught in the interview. |

---

## Practice Exercises

### Exercise 1: Build your skills section
- Write your Skills section using the grouped format above.
- Rule: every skill must be one you could survive a 5-minute interview question on.

### Exercise 2: Rewrite 5 project bullets
- For each Module 8 project, write 2–3 bullets using **Action + Tool + Result**.
- Each bullet must contain at least one number (accuracy, dataset size, time, etc.).

### Exercise 3: Write your 2-line summary
- Draft a summary tailored to **ML Engineer** roles.
- Then write a second version tailored to **Data Scientist** roles. Notice what changes.

### Exercise 4: Assemble the full resume
- Put all sections together on ONE page, in the recommended order.
- Save as `resume-mle.pdf`.

### Exercise 5: ATS check
- Run through the ATS Survival Checklist line by line.
- Find a real AI/ML job posting online, list 10 keywords from it, and confirm how many appear in your resume.

---

## What's Next?

Tomorrow in **Day 03**, we'll set up **LinkedIn and networking** — where most AI jobs actually get filled (through people, not job boards):
- Optimizing your LinkedIn profile for recruiters
- "Building in public" to attract opportunities
- Message templates for reaching out and asking for referrals
- Communities to join

You now have a resume that sells your skills with real proof. That's the document that opens doors — keep it sharp and tailored. Great work today!
