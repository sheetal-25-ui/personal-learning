# Module 7 — Day 5: RAG and AI Agents

## Think of it Like This...

You've built apps that talk to Claude. But there are two big limits we haven't solved:

1. **The model doesn't know YOUR stuff.** It can't read your company's documents, your notes, or anything after its training cutoff.
2. **The model can only talk — it can't DO.** It can't check today's weather, search the web, or send an email.

Today we fix both:

```
   PROBLEM 1: "It doesn't know my data"
   SOLUTION:  RAG → feed it your documents at question time

   PROBLEM 2: "It can only talk, not act"
   SOLUTION:  AGENTS → give it tools it can use
```

These two techniques turn a chatbot into a genuinely useful assistant. This is the frontier — and where most real AI products are being built right now. Let's finish strong!

---

## Part 1: RAG — Retrieval Augmented Generation

### The Problem RAG Solves

Imagine you ask Claude:

> "What's our company's refund policy?"

Claude has **no idea** — your policy isn't in its training data. It might **hallucinate** a plausible-sounding (but wrong!) answer. Not good.

### The RAG Idea (Simpler Than the Name!)

**RAG = look up relevant info first, then let the model answer using it.**

Think of it like an **open-book exam**. Instead of forcing the model to answer from memory, you let it "open the book" to the right page first:

```
   Closed book (plain LLM):    "Um... I think refunds are 30 days?" (guessing!)

   Open book (RAG):            1. Find the refund policy in your docs
                               2. Hand that text to the model
                               3. "Per the policy: refunds within 14 days
                                   with receipt." (accurate!)
```

### The RAG Flow

```
   ┌──────────────────────────────────────────────────────────────┐
   │  SETUP (done once, ahead of time):                              │
   │                                                                 │
   │   Your documents ──► split into chunks ──► turn each into an    │
   │                                            EMBEDDING ──► store   │
   │                                            in a VECTOR DATABASE  │
   └──────────────────────────────────────────────────────────────┘

   ┌──────────────────────────────────────────────────────────────┐
   │  WHEN A USER ASKS A QUESTION:                                    │
   │                                                                 │
   │  1. Question  ──► turn into an embedding                        │
   │  2. SEARCH the vector DB for the most SIMILAR chunks            │
   │  3. Grab the top matching chunks (the "relevant pages")        │
   │  4. Send to the LLM:  question + those chunks                   │
   │  5. LLM answers using the real, retrieved text ──► accurate!   │
   └──────────────────────────────────────────────────────────────┘
```

---

### Embeddings — Searching by MEANING, Not Keywords

Remember embeddings from Day 2? (Lists of numbers that capture meaning.) They're the heart of RAG. The trick: **similar meanings → similar numbers → close together in "meaning space."**

This lets you search by *meaning*, not just matching words:

```
   You search: "How do I get my money back?"

   Keyword search would MISS a doc titled "Refund Policy"
   (no shared words!).

   But embedding search FINDS it — because "get my money back"
   and "refund" are CLOSE in meaning space:

        • "get my money back"
        • "refund policy"        ← close together! ✓
        • "return an item"

        • "cooking recipes"      ← far away ✗
```

### Vector Databases — Where Embeddings Live

A **vector database** stores millions of embeddings and answers one question superfast: *"Which stored chunks are most similar to this one?"*

| Popular vector databases | Note |
|--------------------------|------|
| **Chroma** | Simple, great for learning (runs locally) |
| **Pinecone** | Managed cloud service |
| **FAISS** | Fast library from Meta |
| **Weaviate / Qdrant** | Full-featured open-source options |

They all do the same core job: **store vectors, find the nearest ones.**

---

### A Simple RAG in Code

Here's a minimal, runnable RAG using embeddings + Claude. (We'll keep the "database" as a simple list to show the idea clearly.)

```python
# simple_rag.py
# pip install anthropic openai numpy
import numpy as np
from openai import OpenAI
import anthropic

openai_client = OpenAI()           # we'll use OpenAI just to make embeddings
claude = anthropic.Anthropic()     # and Claude to write the final answer

# 1. OUR "DOCUMENTS" — split into small chunks
documents = [
    "Our refund policy allows returns within 14 days with a receipt.",
    "Customer support is available Monday to Friday, 9am to 5pm.",
    "We ship to over 50 countries, with delivery in 3-7 business days.",
    "Premium members get free shipping and early access to sales.",
]

# 2. Helper: turn any text into an embedding (a list of numbers)
def embed(text):
    result = openai_client.embeddings.create(
        model="text-embedding-3-small",
        input=text,
    )
    return np.array(result.data[0].embedding)

# 3. SETUP: embed every document chunk once
doc_embeddings = [embed(doc) for doc in documents]

# 4. Helper: cosine similarity = how "close" two embeddings are (1 = identical)
def similarity(a, b):
    return np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))

# 5. RETRIEVE: find the most relevant chunk for a question
def retrieve(question):
    q_embedding = embed(question)
    scores = [similarity(q_embedding, d) for d in doc_embeddings]
    best_index = int(np.argmax(scores))      # the closest match
    return documents[best_index]

# 6. ANSWER: give the retrieved text to Claude and ask it to answer
def ask(question):
    context = retrieve(question)
    message = claude.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=200,
        messages=[{
            "role": "user",
            "content": f"""Answer the question using ONLY this information.
If the answer isn't there, say you don't know.

Information: {context}

Question: {question}"""
        }],
    )
    return message.content[0].text

# Try it!
print(ask("How long do I have to return something?"))
# → "You have 14 days to return an item, as long as you have a receipt."
```

```
What just happened:
  Question "How long to return something?" → embedded
  → matched the REFUND chunk (closest in meaning)
  → Claude answered from that real text → accurate, no hallucination!
```

> **Why RAG is everywhere:** It lets companies build AI over their *own* private data (support docs, manuals, contracts) without retraining the model. It's cheaper, updatable (just add new docs!), and dramatically reduces hallucinations. Most "Chat with your PDF" and company-knowledge bots are RAG.

---

## Part 2: AI Agents — When the LLM Can DO Things

### From "Talking" to "Acting"

So far, the LLM only produces **text**. An **agent** is an LLM that can **use tools** to take real actions and gather fresh information.

```
   PLAIN LLM:   You ──► "What's the weather in Tokyo?"
                LLM ──► "I can't check live weather." 😞

   AGENT:       You ──► "What's the weather in Tokyo?"
                LLM ──► [decides to use the weather tool]
                     ──► calls get_weather("Tokyo")
                     ──► tool returns: "18°C, rainy"
                LLM ──► "It's currently 18°C and rainy in Tokyo." 😀
```

### How Agents Work — Tool Use / Function Calling

The magic is **function calling** (also called **tool use**). You describe some tools to the model. When a question needs one, the model **asks you to run it**, you run it and return the result, and the model uses that result to answer.

```
   THE AGENT LOOP:

   ┌─────────────────────────────────────────────────┐
   │ 1. You give the LLM a question + a list of TOOLS  │
   │    it's allowed to use (with descriptions).        │
   ├─────────────────────────────────────────────────┤
   │ 2. LLM decides: "I need the calculator tool."      │
   │    → It outputs: call multiply(a=23, b=47)         │
   ├─────────────────────────────────────────────────┤
   │ 3. YOUR CODE actually runs multiply(23, 47) = 1081│
   │    and sends the result back to the LLM.           │
   ├─────────────────────────────────────────────────┤
   │ 4. LLM uses the result to answer:                  │
   │    "23 times 47 is 1081."                          │
   ├─────────────────────────────────────────────────┤
   │ (For hard tasks, it may loop: think → use a tool → │
   │  see result → use another tool → ... → final answer)│
   └─────────────────────────────────────────────────┘
```

The LLM is the **brain** that decides *which* tool and *when*; your code provides the **hands** that actually do it.

---

### A Simple Agent in Code

Here's a runnable agent that gives Claude a real calculator tool (LLMs are notoriously shaky at exact arithmetic, so this is genuinely useful!):

```python
# simple_agent.py
# pip install anthropic
import anthropic

client = anthropic.Anthropic()

# 1. The actual tool — plain Python that does real work
def calculator(operation, a, b):
    if operation == "multiply":
        return a * b
    if operation == "add":
        return a + b
    return "unknown operation"

# 2. DESCRIBE the tool so the model knows it exists and how to use it
tools = [{
    "name": "calculator",
    "description": "Do exact arithmetic. Use for any math.",
    "input_schema": {
        "type": "object",
        "properties": {
            "operation": {"type": "string", "enum": ["multiply", "add"]},
            "a": {"type": "number"},
            "b": {"type": "number"},
        },
        "required": ["operation", "a", "b"],
    },
}]

messages = [{"role": "user", "content": "What is 1234 multiplied by 5678?"}]

# 3. First call — the model may ask to use the tool
response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=300,
    tools=tools,
    messages=messages,
)

# 4. Did the model request a tool?
if response.stop_reason == "tool_use":
    tool_call = next(b for b in response.content if b.type == "tool_use")
    print(f"Model wants to use: {tool_call.name} with {tool_call.input}")

    # 5. YOUR CODE runs the real tool
    result = calculator(**tool_call.input)
    print(f"Tool result: {result}")

    # 6. Send the result back so the model can finish its answer
    messages.append({"role": "assistant", "content": response.content})
    messages.append({
        "role": "user",
        "content": [{
            "type": "tool_result",
            "tool_use_id": tool_call.id,
            "content": str(result),
        }],
    })

    final = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=300,
        tools=tools,
        messages=messages,
    )
    print("Final answer:", final.content[0].text)
```

```
Output:
Model wants to use: calculator with {'operation': 'multiply', 'a': 1234, 'b': 5678}
Tool result: 7006652
Final answer: 1234 multiplied by 5678 equals 7,006,652.
```

The model **decided on its own** to reach for the calculator, your code ran it, and the model used the exact result. That's an agent! 🤖

> **Real-world tools agents use:** web search, reading/writing files, querying databases, calling other APIs, running code, sending emails, controlling a browser. Combine RAG + tools + a planning loop and you get the powerful AI agents being built today.

---

### RAG vs Agents — Quick Comparison

| | **RAG** | **Agent** |
|---|---------|-----------|
| **Purpose** | Give the model your *knowledge* | Give the model the ability to *act* |
| **Core trick** | Retrieve relevant text, then answer | Use tools/functions to do things |
| **Solves** | "It doesn't know my data" | "It can't take actions" |
| **Example** | Chat with your company's PDFs | Book a flight, search the web, run code |

> They combine beautifully: a customer-support **agent** might **use a RAG tool** to look up policy, **another tool** to check your order status, then write a personalized reply. Knowledge *plus* action.

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **RAG** | Retrieve relevant docs, then let the LLM answer from them |
| **Chunk** | A small piece of a document, ready to embed and store |
| **Embedding** | A list of numbers capturing meaning (used to search by meaning) |
| **Vector database** | Stores embeddings; finds the most similar ones fast |
| **Similarity** | How "close" two embeddings are in meaning space |
| **Agent** | An LLM that can use tools to take actions |
| **Tool use / function calling** | The LLM asks your code to run a function |
| **Agent loop** | think → use tool → see result → repeat → answer |

---

## Practice Exercises

### Exercise 1: Design a RAG System
Imagine building a "Chat with my school textbook" app. Answer:
1. What would your **documents** be?
2. How might you **chunk** them (by chapter? paragraph? page?)
3. Write one example **question** and describe which chunk RAG should retrieve.

### Exercise 2: Keyword vs Meaning Search
For the question *"How do I cancel my subscription?"*, list 2 document titles that a **keyword** search would MISS but an **embedding** (meaning) search would FIND. Explain why.

### Exercise 3: Extend the RAG Code
Take the `simple_rag.py` example and add 3 new document chunks about a topic you choose (e.g., a pizza shop's menu, hours, and delivery area). Then ask 2 questions and confirm it retrieves the right chunk.

### Exercise 4: Design an Agent
You want an AI travel assistant. List **3 tools** you'd give it (with a one-line description of what each does). Then trace the agent loop for the request: *"Find me a hotel in Paris under $200 and tell me the weather."*

### Exercise 5 (Bonus): Add a Tool to the Agent
Modify `simple_agent.py` to add a second tool, `get_length(text)`, that returns the number of characters in a piece of text. Ask the model "How many characters are in the word 'transformer'?" and watch it pick the new tool.

---

## What's Next?

Congratulations — you've completed **Module 7**! You now understand, end to end:

- ✅ **Attention** — the breakthrough idea (Day 1)
- ✅ The **Transformer architecture** (Day 2)
- ✅ How **LLMs are trained** — pretraining, RLHF, hallucinations (Day 3)
- ✅ **Calling LLM APIs** and prompt engineering (Day 4)
- ✅ **RAG and agents** — giving LLMs your data and real-world abilities (Day 5)

That's the full stack of how ChatGPT, Claude, and Gemini actually work — and how to build with them.

### Coming Up — Module 8: Hands-On Projects! 🚀

In **Module 8**, you'll stop learning concepts and start **building real things**. You'll combine everything from this module into complete, working projects — things like:
- A **smart chatbot** with a personality and memory
- A **"Chat with your documents"** RAG app
- A simple **AI agent** that uses tools to get things done
- Your own creative project, start to finish

You've built an incredible foundation. Now you get to use it to make things that genuinely impress people.

---

You did it! You went from "what is attention?" to building RAG systems and AI agents in just five days. That's the exact knowledge that separates an AI *user* from an AI *engineer* — and you now have it. Take a moment to be proud. See you in Module 8, where we build! 🎉
