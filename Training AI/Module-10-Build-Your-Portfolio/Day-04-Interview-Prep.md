# Module 10 — Day 4: Interview Prep

## The Map Before the Journey

The scariest thing about AI/ML interviews is not knowing what's coming. Today we fix that. Once you can see the whole landscape, prep becomes a checklist instead of a fog of anxiety.

Here's the big picture: **AI/ML interviews are not one interview — they're a series of different rounds, each testing a different skill.** You don't need to be perfect at all of them on day one. You need a plan.

---

## The 4 Types of Rounds

Almost every AI/ML interview (especially at big tech and AI labs) is some mix of these four:

| Round | What it tests | How common |
|-------|---------------|------------|
| **1. Coding / DSA** | Can you write correct, efficient code? Data structures & algorithms. | Very — almost always at big tech |
| **2. ML Theory / Fundamentals** | Do you actually understand how ML works? | Very common for ML roles |
| **3. ML System Design** | Can you architect a real ML system end-to-end? | Common for mid/senior; growing for all |
| **4. Behavioral** | Are you someone people want to work with? | Always |

Let's break each one down.

---

## Round 1: Coding / DSA (Data Structures & Algorithms)

**What it tests:** Given a problem, can you pick the right data structure, write a correct solution, and reason about its time/space complexity? Usually on a whiteboard or a shared editor (LeetCode-style).

**Honest truth (read this carefully):**
> **Big-tech and AI-lab roles — Anthropic, Microsoft, Google, OpenAI — require strong DSA.** Even for ML engineering roles, you will likely face LeetCode-style coding rounds. This is the part that filters the most candidates. It is learnable, but it takes consistent practice over weeks/months. **Do not skip it.**

**What shows up:**
- Arrays, strings, hash maps (most common)
- Two pointers, sliding window
- Trees, graphs (BFS/DFS)
- Recursion, dynamic programming
- Sorting, searching, binary search
- Big-O time and space complexity (you must be able to state it)

**Where to practice:** LeetCode (the standard), NeetCode (curated lists + videos), HackerRank.

> **This is exactly what Module 11 goes deep on.** Today, just understand that it's coming and start warming up. The deep DSA grind is its own dedicated module — point yourself there after this module.

---

## Round 2: ML Theory / Fundamentals

**What it tests:** Whether you understand the *why* behind ML, not just how to call `model.fit()`. They want to know you won't be lost when a model misbehaves in the real world.

**Format:** Conversational. "Explain X." "Why would you choose A over B?" "What happens if...?"

### Key concepts you MUST be able to explain simply

You should be able to explain each of these to a smart friend in 1–2 minutes:

| Concept | Be ready to explain |
|---------|---------------------|
| **Supervised vs. unsupervised** | Labeled data vs. finding patterns without labels |
| **Overfitting vs. underfitting** | Memorizing vs. not learning enough; how to detect & fix each |
| **Bias–variance tradeoff** | Why simple models underfit and complex ones overfit |
| **Train / validation / test split** | Why you need all three; data leakage |
| **Regularization (L1/L2, dropout)** | How it fights overfitting |
| **Gradient descent** | How models learn by minimizing a loss function |
| **Loss functions** | MSE for regression, cross-entropy for classification — and why |
| **Evaluation metrics** | Accuracy vs. precision vs. recall vs. F1 — and when each matters |
| **Precision vs. recall** | The trade-off; when you'd favor one (e.g., spam vs. cancer detection) |
| **Confusion matrix** | TP/FP/TN/FN and what they mean |
| **Cross-validation** | Why k-fold gives a more reliable estimate |
| **Feature engineering / scaling** | Why features matter; normalization/standardization |
| **Neural networks basics** | Layers, weights, activations, backpropagation (high level) |
| **CNNs** | Why they're good for images (local patterns, parameter sharing) |
| **Transformers & attention** | Why attention beat RNNs; "the model weighs which words matter" |
| **Embeddings** | Turning text/items into vectors that capture meaning |
| **RAG** | Retrieve relevant docs, then let the LLM answer using them |
| **Handling imbalanced data** | Resampling, class weights, choosing the right metric |

> **The classic trap:** "Accuracy is 99%!" — but the data was 99% one class, so the model is useless. Be ready to explain why **precision, recall, and F1** matter more than raw accuracy on imbalanced data. This question comes up constantly.

> **You've already learned all of these in Modules 4–7.** This round is about *articulating* what you know clearly. Practice explaining out loud.

---

## Round 3: ML System Design

**What it tests:** Can you design a complete machine learning system, not just a model? "How would you build a recommendation system for YouTube?" "Design a spam filter." "How would you build a system to detect fraudulent transactions?"

They're not looking for a perfect answer — they're watching **how you think**: do you ask clarifying questions, consider trade-offs, and think end-to-end?

### A framework you can reuse for any ML system design question
1. **Clarify the problem** — Ask: What's the goal? What's the scale? What's the success metric?
2. **Frame it as an ML problem** — Is it classification? Regression? Ranking? Recommendation?
3. **Data** — What data do we have? How do we collect labels? Any privacy concerns?
4. **Features** — What features would help? How do we process them?
5. **Model** — Start simple (baseline), then justify a more complex model.
6. **Evaluation** — Offline metrics (precision/recall/AUC) AND online metrics (clicks, revenue, A/B test).
7. **Serving & scale** — How do we deploy it? Latency? Batch vs. real-time?
8. **Monitoring** — How do we detect when the model degrades (data drift)? Retraining plan.

> **Mention trade-offs out loud.** "We could use a deep model for accuracy, but a simpler model is faster to serve and easier to debug — I'd start simple and measure." Interviewers love hearing trade-off reasoning.

> Module 11/advanced modules go deeper on system design. For now, learn this 8-step framework — it'll carry you through most questions.

---

## Round 4: Behavioral

**What it tests:** Are you reliable, collaborative, honest, and able to handle setbacks? Even brilliant engineers get rejected for poor behavioral signals.

**Common questions:**
- "Tell me about a project you're proud of."
- "Tell me about a time you faced a hard bug / failure. What did you do?"
- "Tell me about a time you disagreed with someone."
- "Why this company? Why AI?"

**The method:** Use **STAR** — Situation, Task, Action, Result. (We'll practice this fully tomorrow in Day 05.)

> Your Module 8 projects are your best behavioral material. "Tell me about a project" → talk about your RAG app, the bug you hit, how you solved it, the result. Prepare 3–4 stories in advance.

---

## What Each Round Tests — Quick Summary

| Round | Real question behind it | Your prep |
|-------|------------------------|-----------|
| Coding/DSA | "Can you code under pressure?" | LeetCode practice (Module 11) |
| ML Theory | "Do you really understand ML?" | Explain concepts out loud |
| System Design | "Can you build real systems?" | Learn the 8-step framework |
| Behavioral | "Do we want to work with you?" | Prepare STAR stories |

---

## Your Study Plan

Tailor the timeline to how much time you have, but follow this structure. A realistic ramp for someone job-hunting:

### Weekly rhythm (sustainable, ~10–15 hrs/week)
| Day | Focus |
|-----|-------|
| Mon | DSA — 2 LeetCode problems (easy → medium) |
| Tue | ML theory — pick 3 concepts, explain each out loud + write a paragraph |
| Wed | DSA — 2 problems (focus on a pattern: arrays/hashmaps) |
| Thu | System design — work through 1 design question using the framework |
| Fri | DSA — 2 problems + review the week's mistakes |
| Sat | Mock interview (use the Day 05 techniques) + polish a project |
| Sun | Rest / light reading / behavioral story prep |

### The priority order (if time is tight)
1. **DSA** — biggest filter, hardest to fake, takes longest. Start now.
2. **ML theory** — you already know it; practice explaining it.
3. **Behavioral** — prep 3–4 STAR stories; low effort, high payoff.
4. **System design** — learn the framework; deepen over time.

> **Start DSA early.** It's the slowest skill to build. If you wait until you have an interview scheduled, it's too late. A little every day compounds.

---

## Resources

| Resource | Use it for |
|----------|-----------|
| **LeetCode** | DSA practice (the standard for coding rounds) |
| **NeetCode (neetcode.io)** | Curated DSA roadmap + video explanations |
| **"Cracking the Coding Interview"** | Classic DSA/interview book |
| **"Designing Machine Learning Systems" (Chip Huyen)** | The go-to ML system design book |
| **"Machine Learning Interviews" (Susan Shu / various)** | ML-specific interview prep |
| **Pramp / interviewing.io** | Free/paid live mock interviews with peers |
| **Your Module 4–8 notes** | ML theory refresher — you already wrote these! |
| **Glassdoor / company blogs** | Real interview questions per company |

---

## Practice Exercises

### Exercise 1: Self-assess each round
- Rate yourself 1–5 on each of the four rounds. Be honest.
- This tells you where to spend your time.

### Exercise 2: Explain 5 ML concepts out loud
- Pick 5 concepts from the ML Theory table.
- Record yourself explaining each in under 2 minutes (your phone is fine).
- Listen back. Were you clear? Did you use jargon you couldn't explain?

### Exercise 3: Do your first 3 LeetCode problems
- Create a LeetCode account.
- Solve 3 **Easy** problems on arrays/strings/hash maps.
- For each, state the time and space complexity out loud.

### Exercise 4: Run one system design question
- Take: *"Design a movie recommendation system"* (you literally built one!).
- Walk through all 8 steps of the framework on paper.

### Exercise 5: Build your study schedule
- Write out your weekly rhythm based on the table, adapted to your real free time.
- Block the time in your calendar. Treat it like a job.

---

## What's Next?

Tomorrow in **Day 05** — the final lesson of this module — we **practice**:
- Sample questions for every round, with model answers and approaches
- The STAR method for behavioral interviews, with full examples
- How to use Claude/ChatGPT as your personal mock interviewer (24/7, free)
- A confidence pep talk to send you into interviews ready

You now have the full map of AI/ML interviews. The fog is gone — what's left is practice, and practice is just repetition. You've got this!
