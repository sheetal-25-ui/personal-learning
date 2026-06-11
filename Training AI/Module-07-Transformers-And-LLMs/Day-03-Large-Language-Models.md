# Module 7 — Day 3: Large Language Models (LLMs)

## Think of it Like This...

You now know the **machine** (the Transformer). But a brand-new Transformer is like a **newborn brain** — the wiring exists, but it knows nothing. It can't spell, can't reason, can't chat.

So how does it become **Claude** — something that writes code, explains physics, and politely refuses harmful requests?

Three stages of growing up:

```
   1. PRETRAINING    →  Read the entire internet. Learn how language works.
                        (Like a child reading millions of books)

   2. FINE-TUNING    →  Practice being a helpful assistant.
                        (Like job training: "here's how to actually help people")

   3. RLHF           →  Get feedback on what's good vs bad.
                        (Like a mentor saying "great answer" or "be kinder")
```

By the end of today, you'll understand exactly how a pile of math becomes a helpful AI assistant. Let's go!

---

## Stage 1: Pretraining — Reading the Whole Internet

The core training task for an LLM is shockingly simple:

> **Predict the next token.** Over and over. Trillions of times.

That's it. The model is shown a piece of real text with the last word hidden, and it guesses what comes next:

```
Training example:  "The sun rises in the ____"

Model guesses:     "west"     ← WRONG!
Correct answer:    "east"
→ Model adjusts its parameters slightly so next time it leans toward "east"

Do this with TRILLIONS of examples from books, websites, code, Wikipedia...
```

### Why Does Predicting the Next Word Make It Smart?

Here's the beautiful part. To predict the next word **really well**, the model is *forced* to learn deep things about the world:

| To correctly finish... | The model must learn... |
|------------------------|-------------------------|
| "The capital of Japan is ___" | **Facts** (Tokyo) |
| "2 + 2 = ___" | **Math** |
| "She was sad because her dog ___" | **Emotions & cause/effect** (died, ran away) |
| "def add(a, b): return ___" | **How to code** (a + b) |
| "Translate 'hello' to French: ___" | **Languages** (bonjour) |

By becoming a world-class "next word predictor," the model accidentally becomes a world-class **knower of everything in its training data.** Predicting language well *requires* understanding the world.

> This stage uses **enormous** amounts of text and computing power — it can cost millions of dollars and take weeks or months on thousands of specialized chips (GPUs/TPUs). This is the expensive part.

---

## What Are "Parameters"? (The Model's Knobs)

You'll hear "this model has 70 billion parameters." What does that mean?

A **parameter** is a single adjustable number inside the model — like a tiny **knob** the model can turn. During training, the model tweaks all its knobs to make better predictions.

```
   Imagine a giant mixing board with BILLIONS of knobs:

   [🎛️] [🎛️] [🎛️] [🎛️] [🎛️] ... (70,000,000,000 of them!)

   Training = slowly turning each knob to the perfect spot
              so the model predicts the next word correctly.
```

| Model size | Parameters | Analogy |
|-----------|-----------|---------|
| Small | ~1 billion | A clever high-schooler |
| Medium | ~10 billion | A knowledgeable college grad |
| Large | ~70–400+ billion | A team of experts |

**More parameters = more "knobs" = more capacity to store knowledge and patterns.** (But also more expensive to run!)

---

## Scaling Laws — Why Bigger Is Smarter

Researchers discovered something remarkable, now called **scaling laws**:

> As you increase **model size**, **training data**, and **computing power** — together — the model gets **predictably** better. No magic ceiling. Just keep scaling.

```
   Model capability
        ▲
        │                                    •  GPT-4 / Claude class
        │                            •
        │                    •
        │              •
        │         •
        │     •   ← small models
        └──────────────────────────────────────►
                  More data + bigger model + more compute
```

This single insight drove the entire AI boom. It's *why* companies spend hundreds of millions training ever-larger models — they know it will reliably pay off. And surprisingly, new abilities (like reasoning step-by-step) sometimes **emerge** suddenly once a model crosses a certain size.

---

## What Is a "Context Window"? (The Model's Working Memory)

The **context window** is how much text the model can "see" at one time — its short-term memory for a single conversation.

```
   ┌─────────── CONTEXT WINDOW ───────────┐
   │  your prompt + the whole conversation │
   │  + any documents you pasted           │  ← all must fit in here!
   │  + the model's reply being written    │
   └───────────────────────────────────────┘

   Measured in TOKENS (remember: ~¾ word each)
```

| Context window | Roughly equals | Use case |
|----------------|----------------|----------|
| 4,000 tokens | ~3,000 words | Short chats |
| 128,000 tokens | ~96,000 words | A long report |
| 1,000,000 tokens | ~750,000 words | Entire books / huge codebases |

> **If a conversation exceeds the context window, the oldest parts "fall off" and the model forgets them** — like a whiteboard that's full, where you must erase the top to write at the bottom. This is why a very long chat sometimes "forgets" what you said at the start.

Claude, GPT, and Gemini all offer large context windows today (hundreds of thousands to millions of tokens), which is a big deal — it lets you feed them whole documents.

---

## Stage 2: Fine-Tuning — Teaching It to Be an Assistant

After pretraining, the model is a brilliant **autocomplete** — but it doesn't know how to be *helpful*. Ask it a question and it might just continue the question, or ramble.

**Fine-tuning** takes the pretrained model and trains it further on a smaller, curated set of **example conversations** that show the desired behavior:

```
   Example conversation used for fine-tuning:

   User:      "How do I boil an egg?"
   Assistant: "Here's a simple method:
               1. Place eggs in a pot, cover with water.
               2. Bring to a boil, then simmer 9–12 min.
               3. Cool in ice water and peel. Enjoy!"
```

Show thousands of these polite, helpful, well-formatted examples, and the model learns the **pattern of being a good assistant** — answer directly, be clear, be organized.

> Fine-tuning is also how companies make **specialized** models — e.g., fine-tune on medical text for a healthcare assistant, or on legal documents for a contract reviewer.

---

## Stage 3: RLHF — Learning from Human Feedback (Step by Step)

This is the secret sauce that made ChatGPT and Claude feel so good. **RLHF = Reinforcement Learning from Human Feedback.** Long name, simple idea: *let humans rate answers, and train the model to produce the kind humans prefer.*

```
   STEP 1 — Generate options
   ┌──────────────────────────────────────────┐
   │ Ask the model the same question, get       │
   │ several different answers:                  │
   │   Answer A: short and rude                  │
   │   Answer B: helpful and kind                │
   │   Answer C: rambling and off-topic          │
   └──────────────────────────────────────────┘
                     │
                     ▼
   STEP 2 — Humans rank them
   ┌──────────────────────────────────────────┐
   │ Human reviewers rank: B  >  A  >  C         │
   │ ("B is the best, C is the worst")           │
   └──────────────────────────────────────────┘
                     │
                     ▼
   STEP 3 — Train a "reward model"
   ┌──────────────────────────────────────────┐
   │ A second AI learns to PREDICT what humans   │
   │ will like → it becomes an automatic judge.  │
   └──────────────────────────────────────────┘
                     │
                     ▼
   STEP 4 — Improve the main model
   ┌──────────────────────────────────────────┐
   │ The main model practices millions of times, │
   │ trying to get HIGH scores from the judge.   │
   │ → It learns to be helpful, honest, harmless.│
   └──────────────────────────────────────────┘
```

**The result:** a model that doesn't just predict likely text, but produces text *humans actually find good* — polite, helpful, and safe.

> **Anthropic's twist — Constitutional AI:** Instead of relying only on human ratings, Anthropic gives Claude a written set of principles (a "constitution") and has the AI critique and improve **its own** answers against those principles. This makes Claude's helpful, harmless behavior more scalable and transparent. This is core to how Claude works!

---

## Meet the Models — Claude, GPT, Gemini, Llama

| Model | Made by | Known for |
|-------|---------|-----------|
| **Claude** | **Anthropic** | Safety, thoughtful reasoning, long documents, coding. Trained with **Constitutional AI**. |
| **GPT** | OpenAI | Powers ChatGPT; broad capability, huge ecosystem |
| **Gemini** | Google DeepMind | Strong multimodal (text + images + video), search integration |
| **Llama** | Meta | **Open-weights** — you can download and run it yourself |

> **Open vs closed:** Claude, GPT, and Gemini are **closed** (you use them via API/website). **Llama** is **open-weights** — Meta releases the actual parameters so developers can run it on their own computers and customize it freely.

---

## Hallucinations — When the AI Confidently Makes Things Up

Here's a critical thing to understand: **LLMs can state false information with total confidence.** This is called a **hallucination.**

```
   You:    "What year did Albert Einstein win the Nobel Prize
            for the theory of relativity?"

   AI:     "Einstein won the Nobel Prize for relativity in 1921."

   Reality: He won in 1921, but for the PHOTOELECTRIC EFFECT,
            not relativity! The AI mixed it up — confidently.
```

### Why Does This Happen?

Remember the model's actual job: **predict the most likely next token.** It is not looking anything up in a database of facts. It's generating text that *sounds* right based on patterns. Usually that lines up with the truth — but not always.

```
   The model is a brilliant "sounds-right" machine,
   NOT a "looks-it-up" machine.

   "Sounds plausible"  ≠  "Is true"
```

### How to Protect Yourself

| Tip | Why |
|-----|-----|
| **Verify important facts** | Dates, names, numbers, legal/medical claims — always double-check |
| **Ask for sources** | Then confirm the sources are real |
| **Give it the data** | Paste the document so it answers from real text, not memory (this is **RAG** — coming on Day 5!) |
| **Be skeptical of specifics** | Confidence is NOT accuracy |

> Hallucinations are an active research problem. They're getting rarer in newer models, but they have **not** been eliminated. Treat an LLM like a brilliant, fast, *occasionally overconfident* assistant — not an infallible oracle.

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **Pretraining** | Learning language by predicting the next token on huge text |
| **Parameter** | One adjustable "knob" inside the model |
| **Scaling laws** | Bigger model + more data + more compute = predictably smarter |
| **Context window** | How much text the model can see at once (its working memory) |
| **Fine-tuning** | Extra training to specialize behavior (e.g., be a helpful assistant) |
| **RLHF** | Training the model using human preference rankings |
| **Constitutional AI** | Anthropic's method: the AI improves itself using written principles |
| **Hallucination** | When the model confidently states something false |
| **Open-weights** | A model whose parameters you can download (e.g., Llama) |

---

## Practice Exercises

### Exercise 1: Why Next-Word Prediction Teaches Everything
Pick 3 of your own "fill in the blank" sentences (like "The opposite of hot is ___"). For each, write down what **kind of knowledge** the model must have to fill it in correctly (a fact? math? grammar? emotion?).

### Exercise 2: Context Window Budget
A model has a context window of 8,000 tokens. You paste a 5,000-word document.
1. Roughly how many tokens is the document? (Hint: ~1.3 tokens per word)
2. Will it fit? How much room is left for your question and the model's answer?

### Exercise 3: Rank the Answers (Be an RLHF Reviewer!)
Imagine three answers to "How do I stay motivated while studying?":
- **A:** "Just try harder."
- **B:** "Break study into 25-min focused blocks, take short breaks, and reward yourself after each session. Track small wins!"
- **C:** "Motivation is a complex neurochemical phenomenon involving dopamine pathways..." (10 more paragraphs)

Rank them best→worst and explain your ranking in one sentence each. (Congrats — you just did the human-feedback step of RLHF!)

### Exercise 4: Spot the Hallucination Risk
Which of these questions is MOST likely to produce a hallucination, and why?
1. "What's a synonym for happy?"
2. "What were the exact attendance numbers at the 1923 county fair in a small town?"
3. "Write me a poem about autumn."

### Exercise 5 (Bonus): Open vs Closed
In your own words, explain one advantage and one disadvantage of an **open-weights** model (like Llama) versus a **closed** model (like Claude).

---

## What's Next?

Tomorrow in **Day 04 — Using LLM APIs**, we stop theorizing and start **building**! You'll write real, runnable Python that:
- Calls the **Claude API** (Anthropic SDK) and the **OpenAI API**
- Uses **prompt engineering** (clear instructions, examples, system prompts, temperature)
- Handles responses and stays aware of **tokens and cost**

Get ready to talk to an LLM with your own code!

---

Incredible progress! You now understand how raw math becomes a helpful, safe AI assistant — pretraining, scaling, fine-tuning, RLHF, and why it sometimes hallucinates. This is genuinely advanced knowledge. See you on Day 4, where we start building!
