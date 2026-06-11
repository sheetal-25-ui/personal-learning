# Module 7 — Day 2: The Transformer Architecture

## Think of it Like This...

Yesterday you learned that **attention** lets a word focus on the right other words. But attention by itself is just one ingredient. Today we build the whole **recipe** — the full Transformer — the machine that turns "Once upon a" into "time".

Think of the Transformer like a **factory assembly line for understanding language**:
- Raw materials come in (your text)
- They get converted into a form the machine understands (numbers)
- They pass through stations that refine the meaning (attention layers)
- A finished product comes out (the next word)

Let's walk the assembly line, station by station.

---

## Station 1: Tokens — Breaking Text into Pieces

Computers can't read letters. They only understand **numbers**. So the first job is to chop your text into small pieces called **tokens**, and give each token an ID number.

A token is usually a word or **part of a word**:

```
Text:    "Transformers are amazing!"

Tokens:  ["Transform", "ers", " are", " amazing", "!"]
              ▼          ▼      ▼        ▼        ▼
IDs:        [8291,      525,   389,    8056,    0]
```

Notice "Transformers" got split into "Transform" + "ers". That's normal — this lets the model handle rare words and made-up words by combining familiar pieces.

> **Rule of thumb:** 1 token ≈ ¾ of a word in English. 100 tokens ≈ 75 words. (You'll care about this on Day 4 when we talk about cost!)

---

## Station 2: Embeddings — Turning Tokens into Meaning

A token ID like `8056` is just a label — it doesn't *mean* anything yet. So each token ID is turned into an **embedding**: a long list of numbers (a "vector") that captures the token's **meaning**.

```
Token "amazing" → embedding → [0.21, -0.84, 0.55, ..., 0.07]
                               └──────── 768+ numbers ────────┘
```

The magic of embeddings: **words with similar meanings get similar number lists.** They live close together in "meaning space."

```
        Meaning Space (simplified to 2D)

   happy •   • joyful
            • glad
                          • dog
                       • puppy
   sad •                  • cat
      • unhappy

  → "happy" and "joyful" sit close together
  → "dog" and "puppy" sit close together
  → "happy" is FAR from "dog"
```

This is how the model "knows" that *king* relates to *queen* the way *man* relates to *woman* — it's all encoded as distances and directions in this number space.

---

## Station 3: Positional Encoding — Teaching the Model Word Order

Here's a sneaky problem. Attention looks at **all words at once** (that's its superpower). But that means it has **no built-in sense of order**! To attention, these two sentences look identical:

```
"Dog bites man"   vs   "Man bites dog"
```

Same words → same embeddings → but **very** different meanings! (One is a normal day, the other is news.)

**The fix: positional encoding.** We add a little "position signal" to each token's embedding so the model knows where it sits in the sentence.

```
   "Dog"   embedding + position#1  →  "Dog (at slot 1)"
   "bites" embedding + position#2  →  "bites (at slot 2)"
   "man"   embedding + position#3  →  "man (at slot 3)"
```

Think of it like **assigned seats in a theater**. Everyone has a ticket (their meaning), but the seat number (position) tells you who's where. Now "Dog at slot 1" is clearly different from "Dog at slot 3."

---

## Station 4: Multi-Head Attention — Paying Attention Many Ways at Once

On Day 1 you learned single attention. The Transformer uses **multi-head attention** — running attention **several times in parallel**, each "head" focusing on a *different kind* of relationship.

Imagine reading a sentence with **multiple expert readers**, each looking for one thing:

```
Sentence: "The tired student finally finished her long essay"

  Head 1 (grammar):    links "student" ←→ "finished"  (who did the action?)
  Head 2 (adjectives): links "tired" ←→ "student"      (describing whom?)
  Head 3 (ownership):  links "her" ←→ "essay"          (whose essay?)
  Head 4 (size/detail):links "long" ←→ "essay"         (how long?)
```

Each head learns to spot a different pattern. Then their findings are **combined** into one rich understanding. More heads = more types of relationships captured at once.

> Real models use many heads — for example, 12, 16, or more per layer.

---

## Station 5: The Full Transformer Block

Now we stack the pieces into a **Transformer block** — the repeating unit of the whole model. One block looks like this:

```
            ┌─────────────────────────────────┐
   input ──►│  Multi-Head Attention           │
            │  (words look at each other)     │
            └───────────────┬─────────────────┘
                            │  + add the original input back ("residual")
                            │  + normalize (keep numbers tidy)
                            ▼
            ┌─────────────────────────────────┐
            │  Feed-Forward Network           │
            │  (think hard about each word)   │
            └───────────────┬─────────────────┘
                            │  + add back + normalize
                            ▼
                         output ──► (feeds into the NEXT block)
```

Two main parts:
1. **Multi-head attention** — words gather context from each other.
2. **Feed-forward network** — each word "thinks" about what it gathered (a mini neural network applied to each position).

Plus two helpers in every block:
- **Residual connections** ("add the input back") — so the model doesn't lose the original info. Like keeping a copy of your draft while you edit.
- **Layer normalization** — keeps the numbers from getting too big or too small, so training stays stable.

**The key move: stack these blocks dozens of times.** Each layer builds a deeper understanding:

```
  Layer 1:  basic word meanings + nearby words
  Layer 2:  short phrases ("long essay")
  Layer 5:  who-did-what-to-whom
  Layer 12: overall meaning, tone, intent
  ...
  Layer 80: deep, abstract understanding (in big models)
```

---

## Encoder vs Decoder — Two Flavors of Transformer

The original 2017 Transformer had two halves. Modern models usually use just one. Here's the difference:

| | **Encoder** | **Decoder** |
|---|-------------|-------------|
| **Job** | *Understand* a whole input | *Generate* new text |
| **Sees** | The entire input at once (both directions) | Only words **so far** (can't peek ahead) |
| **Great for** | Classification, search, embeddings | Writing, chatting, completing text |
| **Example model** | BERT (Google search) | **GPT, Claude, Gemini, Llama** |

### Why Decoders Can't Peek Ahead

When generating text, the model must predict the **next** word using only the words **before** it — otherwise it would be cheating (looking at the answer!). This is called **masked attention** — future words are hidden behind a "mask."

```
Generating: "The cat sat on the ___"

  When predicting the blank, the model can see:
    "The" "cat" "sat" "on" "the"   ✓ allowed
    [next word]                     ✗ masked (hidden!)
```

> **The big three — GPT, Claude, and Gemini — are all decoder-only models.** They're built to *generate*.

---

## How GPT Generates Text — One Token at a Time

This is the part everyone wonders about. How does Claude write a whole paragraph? **It predicts ONE token, adds it to the sentence, then repeats.** That's the entire trick.

```
You type:  "The capital of France is"

Step 1: Model reads the whole prompt → predicts next token
        → "Paris"   (it considered many options:)

           Paris  ████████████████  92%
           the    ██  4%
           a      █  2%
           located █ 1%
           ...

Step 2: Add "Paris" to the text:
        "The capital of France is Paris"
        → predict next token → "."

Step 3: "The capital of France is Paris."
        → predict next → [END] (model decides it's done)
```

It's like the world's most well-read **autocomplete**. Each new token is fed back in, and the model predicts the next one — over and over — until it decides to stop.

### The Full Picture (ASCII Diagram)

```
   YOU: "The capital of France is"
        │
        ▼
   ┌──────────────────────────────────────┐
   │  TOKENIZER  → ["The"," capital",...]  │   chop into tokens
   ├──────────────────────────────────────┤
   │  EMBEDDINGS → meaning vectors          │   give each token meaning
   │  + POSITIONAL ENCODING                 │   add "which slot am I in"
   ├──────────────────────────────────────┤
   │  TRANSFORMER BLOCK  1                   │ ┐
   │   ├─ Masked Multi-Head Attention        │ │
   │   └─ Feed-Forward Network               │ │  stacked
   │  TRANSFORMER BLOCK  2                   │ │  many
   │  TRANSFORMER BLOCK  3                   │ │  times
   │        ...                              │ │
   │  TRANSFORMER BLOCK  N                   │ ┘
   ├──────────────────────────────────────┤
   │  OUTPUT LAYER → probability for every  │   "what comes next?"
   │  possible next token                    │
   └──────────────────────────────────────┘
        │
        ▼
   NEXT TOKEN: "Paris"  ──┐
        ▲                  │  feed it back in and repeat!
        └──────────────────┘
```

That loop — predict, append, predict, append — is how every word you've ever read from an AI was written.

---

## A Tiny Bit of Real Code (See It Yourself!)

You can watch tokenization and embeddings happen with Hugging Face. (Optional — just for the curious!)

```python
# pip install transformers torch
from transformers import GPT2Tokenizer

tokenizer = GPT2Tokenizer.from_pretrained("gpt2")

text = "Transformers are amazing!"
tokens = tokenizer.tokenize(text)
ids = tokenizer.encode(text)

print("Tokens:", tokens)
print("IDs:   ", ids)
# Tokens: ['Trans', 'formers', 'Ġare', 'Ġamazing', '!']
# IDs:    [8291, 687, 389, 4998, 0]
# (The Ġ symbol just means "there's a space before this token")
```

Try changing the text and re-running — see how different words split into different numbers of tokens!

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **Token** | A small piece of text (word or part-word) |
| **Embedding** | A list of numbers capturing a token's meaning |
| **Positional encoding** | A signal added to tell the model word order |
| **Multi-head attention** | Running attention several ways at once |
| **Feed-forward network** | The "thinking" step applied to each word |
| **Residual connection** | Keeping the original input so info isn't lost |
| **Encoder** | Transformer half that *understands* input |
| **Decoder** | Transformer half that *generates* text (GPT, Claude!) |
| **Masked attention** | Hiding future words so the model can't cheat |

---

## Practice Exercises

### Exercise 1: Count the Tokens
Estimate how many tokens these phrases are (remember: ~1 token ≈ ¾ of a word):
1. "I love learning about AI" (5 words)
2. A 200-word email
3. A 1000-word essay

(If you have Python, run the code above to check your guesses!)

### Exercise 2: Why Order Matters
Write two sentences that use the **exact same words** but mean completely different things because of word order (like "Dog bites man" / "Man bites dog"). Explain why **positional encoding** is needed to tell them apart.

### Exercise 3: Be the Attention Heads
Take this sentence: *"The hungry cat quickly ate the small fish."*
Pretend you have 3 attention heads. Write down one relationship each head might focus on (e.g., adjective→noun, subject→verb, etc.).

### Exercise 4: Trace the Generation
You give a model the prompt **"Roses are red, violets are"**. Write out, step by step, the 2–3 tokens you think it would generate one at a time, and how each generated token gets added back before the next prediction.

### Exercise 5 (Bonus): Encoder or Decoder?
For each task, say whether you'd want an **encoder** or a **decoder**:
1. Writing a poem
2. Detecting if an email is spam
3. Chatting with a user
4. Searching for similar documents

---

## What's Next?

Tomorrow in **Day 03 — Large Language Models**, we go from "how the machine works" to "how it's *trained* to be smart":
- **Pretraining**: predicting the next token across the entire internet
- **Scaling laws**: why bigger really is smarter
- **Fine-tuning** and **RLHF**: how Claude learned to be helpful and safe
- What "**parameters**" and "**context window**" really mean
- Why LLMs sometimes confidently make things up (**hallucinations**)

---

Amazing work! You now understand the actual architecture behind ChatGPT and Claude — something most software engineers can't explain. You're building real AI engineering knowledge. See you on Day 3!
