# Module 8 — Project 4: AI Chatbot with the Claude API

## What We're Building (and Why It's Resume-Worthy)

You'll build a real **AI chatbot** — one you can have a back-and-forth conversation with — powered by **Anthropic's Claude API** (the same Claude you may have chatted with). It will:
- Remember the conversation (so you can ask follow-up questions)
- Follow a **system prompt** (a personality/instructions you give it)
- Run in your Terminal, and optionally as a **web app** with Streamlit

**Why it's resume-worthy:** Knowing how to *call a large language model from code* is one of the hottest skills in tech right now. Every company is building LLM apps. This project proves you can do it — manage conversation state, write prompts, and handle API keys securely. It's also the foundation for Project 5 (RAG).

> **Bonus for you, Sheetal:** Building on the Claude API is directly relevant to your goal of working at Anthropic. This project is a perfect portfolio piece for that.

---

## What You'll Learn

| Concept | What It Means |
|---------|---------------|
| **LLM API** | Sending text to a model over the internet and getting a response |
| **System prompt** | Instructions that set the AI's role and behavior |
| **Conversation history** | Keeping past messages so the AI has context |
| **API key** | Your secret password to use the service — must be kept private! |
| **Environment variables** | The safe way to store secrets, never in your code |

---

## Prerequisites & Tools

- Python 3.x
- An **Anthropic API account** (we'll set this up in Step 1)
- Install the library:

```bash
pip install anthropic
pip install streamlit   # only needed for the optional web app at the end
```

---

## Step 1: Get Your API Key (and Keep It Safe!)

1. Go to [console.anthropic.com](https://console.anthropic.com) and sign up / log in.
2. Add a small amount of credit (a few dollars is plenty for learning — each chat costs a fraction of a cent).
3. Go to **API Keys → Create Key**. Copy the key (it starts with `sk-ant-...`).

### ⚠️ The Golden Rule of API Keys

**NEVER put your API key directly in your code. NEVER commit it to GitHub.** If a key leaks publicly, someone can run up charges on your account. Instead, store it as an **environment variable** — a value your computer holds outside your code.

**On Mac, set it in your Terminal:**

```bash
export ANTHROPIC_API_KEY="sk-ant-paste-your-key-here"
```

> This lasts for your current Terminal session. To make it permanent, add that line to your `~/.zshrc` file. To check it worked: `echo $ANTHROPIC_API_KEY`

Your Python code will read the key from this environment variable — so the key never appears in your files.

---

## Step 2: Your First API Call

Create a file `chatbot.py`. Let's make sure the connection works:

```python
# chatbot.py
# A command-line chatbot powered by Claude

import os
from anthropic import Anthropic

# Step 1: Read the API key from the environment variable (NOT hardcoded!)
api_key = os.environ.get("ANTHROPIC_API_KEY")
if not api_key:
    print("ERROR: Please set your ANTHROPIC_API_KEY environment variable.")
    print('Run: export ANTHROPIC_API_KEY="sk-ant-your-key"')
    exit()

# Step 2: Create the client that talks to Claude
client = Anthropic(api_key=api_key)

# Step 3: Send a single message and print the reply
response = client.messages.create(
    model="claude-sonnet-4-5",   # the model to use (fast and capable)
    max_tokens=300,               # the longest reply we'll allow
    messages=[
        {"role": "user", "content": "Hello! In one sentence, what are you?"}
    ]
)

# The reply text lives inside response.content[0].text
print(response.content[0].text)
```

**Run it:** `python3 chatbot.py`

If you see Claude introduce itself — congratulations, you just called an LLM from your own code! If you get an authentication error, double-check your API key environment variable.

---

## Step 3: Add Conversation Memory

A real chatbot remembers what you said. We do this by keeping a **list of messages** and sending the whole history each time. Replace your file with this:

```python
# chatbot.py
# A conversational chatbot that remembers the conversation

import os
from anthropic import Anthropic

# Read the key safely
api_key = os.environ.get("ANTHROPIC_API_KEY")
if not api_key:
    print('ERROR: Set ANTHROPIC_API_KEY first. Run: export ANTHROPIC_API_KEY="sk-ant-..."')
    exit()

client = Anthropic(api_key=api_key)

# Step 1: The system prompt sets Claude's personality and rules
SYSTEM_PROMPT = (
    "You are a friendly, encouraging AI tutor named Aria. "
    "You explain things simply, like you're teaching a smart beginner. "
    "Keep answers concise and use everyday examples."
)

# Step 2: This list holds the whole conversation so Claude has context
conversation_history = []

print("=== Chat with Aria (type 'quit' to exit) ===\n")

# Step 3: The chat loop
while True:
    # Get the user's message
    user_input = input("You: ")
    if user_input.lower() in ("quit", "exit", "bye"):
        print("Aria: Goodbye! Keep learning!")
        break

    # Add the user's message to the history
    conversation_history.append({"role": "user", "content": user_input})

    # Step 4: Send the ENTIRE history + system prompt to Claude
    response = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=500,
        system=SYSTEM_PROMPT,            # the personality/instructions
        messages=conversation_history    # the full conversation so far
    )

    # Get Claude's reply text
    reply = response.content[0].text
    print(f"Aria: {reply}\n")

    # Step 5: Add Claude's reply to the history so it remembers next turn
    conversation_history.append({"role": "assistant", "content": reply})
```

**Run it and have a real conversation!** Try:
- "What is machine learning?"
- Then: "Can you give me an example?" ← It remembers the topic because of the history!

**In plain language:** Each turn, we append your message to a list, send the whole list to Claude, get a reply, and append that too. That growing list *is* the memory.

---

## Step 4: Understanding the Key Pieces

| Piece | What It Does |
|-------|--------------|
| `system=SYSTEM_PROMPT` | Sets the AI's role — change this to make a pirate, a chef, a coding coach, anything! |
| `messages=conversation_history` | The full back-and-forth, so Claude has context |
| `max_tokens=500` | Caps reply length (1 token ≈ ¾ of a word). 500 ≈ a few paragraphs |
| `"role": "user"` / `"assistant"` | Marks who said what — Claude needs this to follow the conversation |

**Try changing the system prompt** to `"You are a sarcastic pirate who answers every question with nautical metaphors."` and watch the personality change completely. That's prompt engineering!

---

## Step 5 (Optional): Turn It into a Web App with Streamlit

Streamlit turns a Python script into a web app with almost no extra work. Create `chatbot_web.py`:

```python
# chatbot_web.py
# A web-based chatbot using Streamlit + Claude
# Run with:  streamlit run chatbot_web.py

import os
import streamlit as st
from anthropic import Anthropic

# Set up the page
st.title("💬 Chat with Aria")
st.caption("An AI tutor powered by Claude")

# Read the API key
api_key = os.environ.get("ANTHROPIC_API_KEY")
if not api_key:
    st.error("Please set your ANTHROPIC_API_KEY environment variable.")
    st.stop()

client = Anthropic(api_key=api_key)

SYSTEM_PROMPT = (
    "You are a friendly AI tutor named Aria. Explain things simply "
    "and encourage the learner. Keep answers concise."
)

# Streamlit reruns the whole script on every interaction, so we store
# the conversation in 'session_state' to make it persist between turns.
if "messages" not in st.session_state:
    st.session_state.messages = []

# Show the conversation so far
for msg in st.session_state.messages:
    with st.chat_message(msg["role"]):
        st.markdown(msg["content"])

# A chat input box at the bottom
if prompt := st.chat_input("Ask me anything..."):
    # Show and store the user's message
    st.session_state.messages.append({"role": "user", "content": prompt})
    with st.chat_message("user"):
        st.markdown(prompt)

    # Get Claude's reply
    with st.chat_message("assistant"):
        response = client.messages.create(
            model="claude-sonnet-4-5",
            max_tokens=500,
            system=SYSTEM_PROMPT,
            messages=st.session_state.messages
        )
        reply = response.content[0].text
        st.markdown(reply)

    # Store the reply
    st.session_state.messages.append({"role": "assistant", "content": reply})
```

**Run it:** `streamlit run chatbot_web.py`

A browser tab opens with a real chat interface — message bubbles and all! You can share this or deploy it online for free. **This is a genuinely impressive thing to show people.**

---

## Extensions & Improvements

1. **Streaming responses** — make Claude's reply appear word-by-word as it's generated (feels much more alive). Use `client.messages.stream(...)`.
2. **Multiple personalities** — add a dropdown to switch between "Tutor", "Code Reviewer", "Story Writer" system prompts.
3. **Save conversations** — write each chat to a text file so you can revisit them.
4. **Add a token/cost counter** — show how many tokens each message used.
5. **Deploy it** — push to GitHub and deploy free on [Streamlit Community Cloud](https://streamlit.io/cloud) so anyone can use it via a link.

---

## How to Show This on Your Portfolio / GitHub

1. Create a repo `claude-chatbot` with `chatbot.py` and `chatbot_web.py`.
2. **CRITICAL — protect your key:** Add a `.gitignore` file containing `.env` and never commit your key. Double-check before pushing! In your README, explain that the key is read from an environment variable.
   ```
   # .gitignore
   .env
   __pycache__/
   ```
3. Add a `requirements.txt` (`anthropic`, `streamlit`).
4. In your `README.md`: explain what it does, show a screenshot of the web chat, and include clear setup instructions (`export ANTHROPIC_API_KEY=...` then `streamlit run chatbot_web.py`).
5. **Portfolio tip:** If you deploy it on Streamlit Cloud, put the live link at the top of your README. A working, clickable demo is worth ten code samples.

> **Security note:** When deploying, use the platform's **secrets manager** (Streamlit Cloud has one) to store the API key — never paste it into code, even for deployment.

> **One-line resume bullet:** *"Built a conversational AI chatbot with the Anthropic Claude API featuring system prompts and conversation memory, deployed as a Streamlit web app with secure API-key handling."*

---

## What's Next?

In **Project 5** — the capstone — you'll combine everything into a **RAG app: "Chat with your documents."** You'll load your own files, let the AI search them, and answer questions using only your data. This is one of the most in-demand AI skills in the industry today, and it builds directly on the Claude API you just learned.
