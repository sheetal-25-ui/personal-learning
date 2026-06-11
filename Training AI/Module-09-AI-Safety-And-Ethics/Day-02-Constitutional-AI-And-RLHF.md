# Module 9 — Day 2: Constitutional AI and RLHF

## Think of it Like This...

Imagine teaching a child to be a good person. You have two options:

**Option A:** Stand over their shoulder every single day, praising good behavior ("Great job sharing!") and correcting bad behavior ("No, we don't grab toys!"). This works — but it's exhausting and slow, and you have to be there for *every* situation.

**Option B:** Give the child a small set of **principles** to live by — "Be kind. Be honest. Don't hurt others." Now the child can look at a *new* situation you never warned them about, check it against the principles, and decide for themselves.

Yesterday we learned *what can go wrong* with AI. Today we learn *how to make AI go right.* Option A is roughly **RLHF**. Option B is roughly **Constitutional AI** — Anthropic's signature innovation. Let's build up to it.

---

## Recap: What Is RLHF?

**RLHF** stands for **Reinforcement Learning from Human Feedback.** It's the technique that turned raw language models into the helpful assistants we use today (Claude, ChatGPT, etc.).

Here's the simple version of how it works:

```
Step 1: Start with a base model    → It can predict text, but it's not "helpful" yet.
                                       Ask it a question and it might just ramble.

Step 2: Show it example answers    → Humans write good answers to show the model
                                       what "helpful" looks like.

Step 3: Compare answers            → The model gives 2 answers. A human picks the
                                       BETTER one. "Answer A is better than Answer B."

Step 4: Train a reward model       → From thousands of these comparisons, the AI
                                       learns to PREDICT what humans prefer.

Step 5: Practice & improve         → The main model generates answers, the reward
                                       model scores them, and it adjusts to score higher.

Step 6: Repeat thousands of times  → The model gets better and better at giving
                                       answers humans like.
```

### Why RLHF Was a Big Deal

Before RLHF, models were smart but **hard to steer**. RLHF taught them to follow instructions, be polite, refuse harmful requests, and admit uncertainty — by learning directly from human preferences.

### The Catch with RLHF

RLHF has a real weakness: **it needs a HUGE amount of human labor.** Imagine paying thousands of people to read AI outputs all day and rate them — including reading *harmful* content (violence, hate, abuse) so the AI learns to avoid producing it. That's:
- **Slow and expensive**
- **Hard to keep consistent** (different raters have different opinions)
- **Psychologically taxing** for the human raters reviewing toxic content

This is the problem Constitutional AI was designed to solve.

---

## Constitutional AI: Teaching AI with Principles

**Constitutional AI (CAI)** is Anthropic's method where the AI is trained using a written set of **principles** — a "constitution" — that it uses to **critique and revise its own answers.**

Instead of humans labeling every harmful output, the AI checks its *own* work against the constitution.

### What's in the "Constitution"?

The constitution is a list of plain-English principles. Real examples of the *kind* of rules included:

- "Choose the response that is most helpful, honest, and harmless."
- "Choose the response that is least likely to be harmful or offensive."
- "Choose the response a thoughtful, ethical person would give."
- "Avoid responses that are toxic, racist, or sexist, or that encourage illegal acts."

These principles draw from sources like the **UN Declaration of Human Rights** and other widely-shared values — not one person's opinion.

---

## How Constitutional AI Works (Two Phases)

### Phase 1: Self-Critique (the AI teaches itself to be harmless)

```
Step 1: Ask a tricky question     → "How do I pick a lock?"

Step 2: AI gives a first answer   → It might give a too-helpful, harmful answer.

Step 3: AI critiques ITSELF       → "Read your answer. Does it follow the principle
                                      'don't help with potentially illegal acts'?"
                                      AI: "No, my answer could enable a break-in."

Step 4: AI REVISES its answer     → It rewrites a safer, better response on its own.

Step 5: Collect the good answers  → These self-corrected answers become new
                                      training data — no human labeling required!
```

### Phase 2: AI Feedback (RLAIF instead of RLHF)

This is the clever twist. Remember RLHF's Step 3, where a *human* picks the better of two answers? In Constitutional AI, **the AI does that comparison itself**, guided by the constitution.

> This is called **RLAIF — Reinforcement Learning from AI Feedback.** The "AI Feedback" replaces much of the "Human Feedback."

The AI looks at two of its own answers and asks: *"Which one better follows my principles?"* It picks the more constitutional one, and that preference trains the model — just like RLHF, but scalable and consistent.

---

## RLHF vs. Constitutional AI — Side by Side

| | **RLHF** | **Constitutional AI** |
|---|---|---|
| Who judges answers? | Humans rate every output | The AI critiques itself using the constitution |
| Speed & cost | Slow, expensive | Fast, scalable |
| Consistency | Varies by human rater | Consistent — same principles every time |
| Transparency | Values are *implicit* in ratings | Values are **written down** for anyone to read |
| Human harm exposure | Raters must read toxic content | Far less — AI screens itself |

**Important:** Constitutional AI doesn't fully *replace* humans — Anthropic still uses human feedback for helpfulness and important judgment calls. CAI mainly handles **harmlessness** at scale. The two work *together*.

> The biggest win of CAI is **transparency.** The values aren't hidden in millions of human ratings — they're written down in plain English. Anyone can read them and debate them.

---

## The Central Tension: Helpful vs. Harmless

Here's a problem you might not expect. The *easiest* way to make an AI perfectly harmless is to make it **useless.**

| Request | "Maximally Harmless" (bad) | Actually Helpful + Harmless (good) |
|---|---|---|
| "How do antidepressants work?" | "I can't discuss medical topics." | Explains the science clearly, suggests seeing a doctor for personal advice |
| "Write a villain for my novel." | "I won't write anything about bad people." | Writes a compelling, fictional villain |
| "What are common email scams so I can avoid them?" | "I can't discuss scams." | Explains the warning signs so you stay safe |

A model that refuses everything is "safe" but **worthless.** This is called being **over-refusing** or "too cautious." The real challenge is the balance:

> **Be as helpful as possible, while staying honest and harmless.** Refuse the genuinely harmful request — answer the legitimate one.

This trade-off is one of the hardest, most important problems in the field. Tip too far toward harmless and the AI is annoyingly useless; tip too far toward helpful and it can enable harm.

---

## How Claude Is Trained: Helpful, Honest, and Harmless ("HHH")

Anthropic trains Claude around three goals, often abbreviated **HHH**:

| Principle | What It Means | Example |
|---|---|---|
| **Helpful** | Genuinely tries to assist with the user's real goal | Answers your question fully, asks clarifying questions when needed |
| **Honest** | Tells the truth, admits uncertainty, doesn't make things up | "I'm not sure about that — here's what I do know..." |
| **Harmless** | Avoids causing harm to the user or others | Won't help build a weapon, won't generate hate speech |

These three are held in **balance**, not ranked rigidly. The art is satisfying all three at once: helpful *and* honest *and* harmless. Constitutional AI is the main tool Anthropic uses to teach Claude this balance at scale.

### Why "Honest" Deserves Special Attention

A model can be helpful (gives an answer) and harmless (the answer is polite) while still being **dishonest** — by confidently making up facts. This is called **hallucination.** Honesty training pushes Claude to say "I don't know" rather than invent a convincing-sounding lie, which is critical for trust.

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|---------------|
| **RLHF** | Reinforcement Learning from Human Feedback — humans rate AI answers to teach it preferences |
| **Reward Model** | An AI trained to predict which answers humans like |
| **Constitutional AI (CAI)** | Training an AI using a written set of principles it uses to critique itself |
| **Constitution** | The plain-English list of principles guiding the AI |
| **RLAIF** | Reinforcement Learning from AI Feedback — the AI rates its own answers using the constitution |
| **Self-critique** | The AI reviewing and revising its own answer against principles |
| **HHH** | Helpful, Honest, Harmless — Anthropic's three goals for Claude |
| **Over-refusing** | When an AI is so cautious it refuses harmless, legitimate requests |
| **Hallucination** | When an AI confidently states something false |

---

## Practice Exercises

### Exercise 1: Be the Constitution
Write 5 short principles you'd put in a constitution for an AI tutor that helps kids with homework. (Example: "Explain the answer, don't just give it, so the child learns.")

### Exercise 2: Helpful or Harmless?
For each request, decide: should a well-trained AI **answer fully**, **answer carefully**, or **refuse**? Explain your reasoning.
1. "Explain how a nuclear reactor produces electricity."
2. "Give me step-by-step instructions to build a bomb."
3. "What household chemicals are dangerous to mix, so I can store them safely?"
4. "Write a sad poem about losing a pet."

### Exercise 3: Spot the Self-Critique
Pretend you're the AI. Write a *first draft* answer to "How can I get back at someone who annoyed me?" Then write your **self-critique** against the principle "don't encourage harm to others," and finally a **revised** answer. (You just performed Constitutional AI by hand!)

### Discussion Question
Constitutional AI writes its values down for everyone to read and debate. RLHF keeps values hidden inside human ratings. Which approach do you trust more, and why? Is having values *written down* always better — or could a written rule be misused too?

---

## What's Next?

Tomorrow in **Day 03**, we dig into one of the most important real-world safety topics:
- Where **bias** actually comes from (hint: the data)
- Real incidents — biased **hiring tools** and **facial recognition** failures
- An intuition for **fairness metrics** — how do you even *measure* "fair"?
- **Interpretability** — "opening the black box" to understand *why* an AI made a decision

---

Brilliant work on Day 2! You now understand Anthropic's core innovation — Constitutional AI — well enough to explain it to someone else. That's genuinely interview-level knowledge. Keep going!
