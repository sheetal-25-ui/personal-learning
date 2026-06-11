# Module 7 — Day 4: Using LLM APIs

## Think of it Like This...

For three days you've learned how LLMs work *inside*. Today you get to **talk to one with code** — and it's much easier than you think!

Think of an **API** (Application Programming Interface) like ordering food through an app:

```
   YOU (your code)              THE KITCHEN (Claude/GPT servers)
        │                                  │
        │  "I'd like a poem about cats"     │
        │  ──────────────────────────────► │  (the model thinks...)
        │                                  │
        │  ◄────────────────────────────── │
        │     "Whiskers twitch in moonlight..."
        ▼                                  ▼
```

You send a request (your prompt), the LLM "kitchen" cooks it up, and sends back a response. You never see the giant model — you just place orders. Let's write our first order!

---

## Step 0: Getting Set Up (One-Time)

You need two things: the library installed, and an **API key** (your secret password to use the service).

```bash
# Install the official libraries
pip install anthropic openai
```

```bash
# Set your API key as an environment variable (keeps it OUT of your code!)
# Mac/Linux:
export ANTHROPIC_API_KEY="sk-ant-your-key-here"
export OPENAI_API_KEY="sk-your-key-here"
```

> **Safety rule:** NEVER paste your API key directly into your code or share it. Anyone with your key can spend your money! Always load it from an environment variable, like above. (Get keys from console.anthropic.com or platform.openai.com.)

---

## Your First Call — The Claude API

Here's a complete, runnable program that talks to Claude:

```python
# hello_claude.py
import anthropic

# The client automatically reads your ANTHROPIC_API_KEY environment variable
client = anthropic.Anthropic()

message = client.messages.create(
    model="claude-sonnet-4-5",          # which Claude model to use
    max_tokens=300,                      # max length of the REPLY (in tokens)
    messages=[
        {"role": "user", "content": "Explain what an API is in one sentence."}
    ],
)

# The reply text lives inside the response object
print(message.content[0].text)
```

Run it:

```bash
python hello_claude.py
```

```
Output:
An API is a set of rules that lets two software programs talk to each
other, like a waiter taking your order to the kitchen and bringing food back.
```

That's it — you just used the same technology that powers Claude.ai, from your own computer! 🎉

---

## The Same Thing — The OpenAI API

The OpenAI API is almost identical. If you learn one, you basically know the other:

```python
# hello_openai.py
from openai import OpenAI

client = OpenAI()   # reads your OPENAI_API_KEY automatically

response = client.chat.completions.create(
    model="gpt-4o",
    max_tokens=300,
    messages=[
        {"role": "user", "content": "Explain what an API is in one sentence."}
    ],
)

print(response.choices[0].message.content)
```

### Side-by-Side Comparison

| | **Claude (Anthropic)** | **OpenAI** |
|---|------------------------|------------|
| Create client | `anthropic.Anthropic()` | `OpenAI()` |
| Make a call | `client.messages.create(...)` | `client.chat.completions.create(...)` |
| Get the text | `message.content[0].text` | `response.choices[0].message.content` |
| Messages format | `[{"role": ..., "content": ...}]` | `[{"role": ..., "content": ...}]` (same!) |

The **messages format is shared** — a list of turns, each with a `role` and `content`. That's the universal pattern across LLM APIs.

---

## Understanding Roles — Who's Talking?

Every message has a **role**. There are three:

| Role | Who it is | Example |
|------|-----------|---------|
| **system** | Your instructions to the AI (its "job description") | "You are a friendly math tutor for kids." |
| **user** | The human (you, or your app's user) | "What is 7 times 8?" |
| **assistant** | The AI's replies | "7 times 8 is 56!" |

A multi-turn conversation is just a growing list of these:

```python
messages = [
    {"role": "user",      "content": "Hi! My name is Sheetal."},
    {"role": "assistant", "content": "Hello Sheetal! Nice to meet you."},
    {"role": "user",      "content": "What's my name?"},   # AI will remember!
]
```

> **Key insight:** The model has no memory between calls! It only "remembers" the conversation because **you send the whole history every time.** That's why long chats use more tokens (and cost more).

---

## System Prompts — Setting the AI's Personality

The **system prompt** is the most powerful tool you have. It shapes *how* the AI behaves for the whole conversation. In the Claude API it's a separate `system` parameter:

```python
import anthropic
client = anthropic.Anthropic()

message = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=200,
    system="You are a pirate. Answer every question in pirate speak, "
           "and keep answers under 3 sentences.",     # ← the system prompt!
    messages=[
        {"role": "user", "content": "What is the capital of France?"}
    ],
)
print(message.content[0].text)
```

```
Output:
Arr, the capital o' France be Paris, ye landlubber! A fine port city it be,
full o' fancy bread and shiny towers. Now hand over yer baguette!
```

Change the system prompt and the AI's entire behavior changes — tutor, lawyer, coding assistant, customer-support bot. **This is how real apps customize the AI.**

> In OpenAI, you do the same thing by adding a `{"role": "system", "content": "..."}` message at the start of the messages list.

---

## Prompt Engineering Basics — Getting Great Answers

**Prompt engineering** = the skill of writing instructions that get you exactly what you want. Here are the core techniques:

### 1. Be Clear and Specific

```
   ❌ Vague:    "Tell me about dogs."
   ✅ Specific: "List 5 dog breeds good for small apartments,
                with one sentence on why each suits apartment life."
```

### 2. Show Examples (Few-Shot Prompting)

Give the model a pattern to follow. This is incredibly powerful:

```python
prompt = """Convert these to formal English.

Casual: "gonna grab lunch brb"
Formal: "I am going to get lunch and will return shortly."

Casual: "ur idea is dope lets do it"
Formal: "Your idea is excellent; let's proceed with it."

Casual: "no clue what ur talking about"
Formal:"""    # ← the model continues the pattern!
```

Showing examples ("few-shot") often works far better than just describing what you want ("zero-shot").

### 3. Tell It to Think Step by Step

For reasoning problems, ask the model to show its work — it gets answers right more often:

```
   "Solve this step by step: If a shirt costs $40 and is
    25% off, what's the final price? Show your reasoning."
```

### 4. Specify the Format

```
   "Respond ONLY with valid JSON in this shape:
    {\"name\": ..., \"age\": ..., \"city\": ...}"
```

| Technique | What it does |
|-----------|-------------|
| **Be specific** | Removes guesswork |
| **Few-shot examples** | Teaches the pattern by demonstration |
| **Step by step** | Improves reasoning/accuracy |
| **Specify format** | Makes output predictable and parseable |
| **Use a system prompt** | Sets consistent role & tone |

---

## Temperature — The Creativity Dial

**Temperature** controls how random vs predictable the model's answers are:

```
   temperature = 0.0          temperature = 1.0
   ┌─────────────┐            ┌─────────────┐
   │  FOCUSED    │            │  CREATIVE   │
   │  Predictable│            │  Surprising │
   │  Consistent │            │  Varied     │
   └─────────────┘            └─────────────┘
   Same answer              Different answer
   every time               each time
```

| Temperature | Best for | Example |
|-------------|----------|---------|
| **0.0 – 0.3** | Facts, code, data extraction | "Extract the date from this text" |
| **0.4 – 0.7** | Balanced — most general chat | Q&A, explanations |
| **0.8 – 1.0** | Creative writing, brainstorming | Poems, story ideas, names |

```python
message = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=100,
    temperature=0.9,        # ← crank up creativity for brainstorming
    messages=[{"role": "user", "content": "Give me 5 fun names for a coffee shop."}],
)
```

> **Tip:** For anything where correctness matters (math, code, pulling out facts), use a **low** temperature. For anything creative, go higher.

---

## Handling the Response (Properly)

Real code checks what came back. The response object carries useful info:

```python
import anthropic
client = anthropic.Anthropic()

message = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=150,
    messages=[{"role": "user", "content": "Write a haiku about coding."}],
)

# 1. The actual text
print("Reply:", message.content[0].text)

# 2. Why it stopped: "end_turn" (finished) or "max_tokens" (hit your limit!)
print("Stop reason:", message.stop_reason)

# 3. Token usage — this is what you pay for!
print("Input tokens: ", message.usage.input_tokens)
print("Output tokens:", message.usage.output_tokens)
```

```
Output:
Reply: Fingers on the keys / Logic blooms in quiet lines / Bugs hide, then are found
Stop reason: end_turn
Input tokens:  16
Output tokens: 22
```

> If `stop_reason` is `"max_tokens"`, the reply got **cut off** — raise `max_tokens` and try again!

### Always Wrap API Calls in Error Handling

Networks fail, keys expire, limits get hit. Handle it gracefully:

```python
import anthropic
client = anthropic.Anthropic()

try:
    message = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=100,
        messages=[{"role": "user", "content": "Hello!"}],
    )
    print(message.content[0].text)
except anthropic.RateLimitError:
    print("Slow down — too many requests. Wait and retry.")
except anthropic.APIError as e:
    print(f"Something went wrong with the API: {e}")
```

---

## Cost and Token Awareness — Don't Get Surprised!

**You pay per token** — both what you send (input) and what you get back (output). Output usually costs more than input.

```
   COST  =  (input tokens  × input price)
          + (output tokens × output price)

   Remember: 1 token ≈ ¾ word. 1,000 tokens ≈ 750 words.
```

### Things That Quietly Burn Tokens

| Token-eater | Why it adds up |
|-------------|----------------|
| **Long conversations** | You resend the *entire* history every turn |
| **Pasting big documents** | A 50-page PDF = tens of thousands of tokens *per call* |
| **Verbose system prompts** | Sent on every single request |
| **High `max_tokens`** | Allows longer (pricier) replies |

### Money-Saving Habits

```python
# 1. Use a SMALLER/CHEAPER model when you don't need the most powerful one
model="claude-haiku-..."   # cheaper & faster for simple tasks

# 2. Cap output length
max_tokens=150             # don't allow a giant reply if you need a short one

# 3. Trim old conversation turns you no longer need
# 4. Ask for concise answers in your prompt: "Answer in 2 sentences."
```

> **Tip:** Use `message.usage` (shown above) to log token counts while developing. It's the easiest way to catch a prompt that's accidentally huge.

---

## Putting It All Together — A Mini Chatbot

Here's a complete, runnable chatbot that remembers the conversation:

```python
# chatbot.py
import anthropic

client = anthropic.Anthropic()
conversation = []   # we store the full history here

print("Chatbot ready! Type 'quit' to exit.\n")

while True:
    user_input = input("You: ")
    if user_input.lower() == "quit":
        break

    # Add the user's message to history
    conversation.append({"role": "user", "content": user_input})

    # Send the WHOLE history so the bot remembers context
    message = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=300,
        system="You are a warm, encouraging study buddy. Keep replies short.",
        messages=conversation,
    )

    reply = message.content[0].text
    print(f"Bot: {reply}\n")

    # Add the bot's reply to history so it's remembered next turn
    conversation.append({"role": "assistant", "content": reply})
```

Run `python chatbot.py` and have a real conversation — every concept from today, working together!

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **API** | A way for your code to talk to the LLM service |
| **API key** | Your secret password for the service (never share it!) |
| **Role** | Who's speaking: `system`, `user`, or `assistant` |
| **System prompt** | Instructions that set the AI's behavior/personality |
| **Prompt engineering** | Writing prompts that get great results |
| **Few-shot** | Giving examples in the prompt to teach a pattern |
| **Temperature** | Creativity dial (0 = focused, 1 = creative) |
| **max_tokens** | Maximum length of the reply |
| **stop_reason** | Why the model stopped (`end_turn` vs `max_tokens`) |
| **usage** | Token counts — what you're billed for |

---

## Practice Exercises

### Exercise 1: Your First Call
Write and run a program (Claude *or* OpenAI) that asks the model: *"Explain photosynthesis to a 7-year-old."* Then change the `max_tokens` to 50 and run again. What's different about the answer?

### Exercise 2: Personality Swap
Take the pirate example and change the system prompt to make the AI behave as: (a) a Shakespearean poet, and (b) a no-nonsense robot that only answers in bullet points. Ask each the same question and compare.

### Exercise 3: Temperature Experiment
Ask the model "Give me a name for a pet dragon" **five times** at `temperature=0.0`, then **five times** at `temperature=1.0`. Write down what you notice about repetition vs variety.

### Exercise 4: Few-Shot Translator
Write a few-shot prompt (like the formal-English example) that converts casual movie reviews into one-line professional summaries. Give it 2 examples, then a 3rd to complete.

### Exercise 5 (Bonus): Token Budgeting
Modify the mini chatbot to print `message.usage.input_tokens` and `message.usage.output_tokens` after each reply. Have a 6-message conversation and watch the input tokens **grow** every turn. Explain in one sentence *why* they grow.

---

## What's Next?

Tomorrow in **Day 05 — RAG and AI Agents**, we make LLMs dramatically more powerful:
- **RAG** (Retrieval Augmented Generation): give the model **your own** documents so it answers from real data (and hallucinates less!)
- **Embeddings + vector databases**: how AI "searches by meaning"
- **AI agents**: LLMs that **use tools** and **take actions** (function calling)
- We'll sketch a real RAG pipeline and a simple agent in code!

---

Brilliant work! You can now write code that talks to Claude and GPT, shape their behavior, and stay smart about cost. You're officially building with LLMs. One more day to go — see you on Day 5!
