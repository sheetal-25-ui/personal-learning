# Module 8 — Project 5: RAG App — "Chat With Your Documents"

## What We're Building (and Why It's Resume-Worthy)

This is your **capstone project.** You'll build an app where you upload your own documents (PDFs, notes, a textbook) and then **ask questions about them in plain English** — and the AI answers using *only your documents*. Upload your college notes and quiz yourself. Upload a contract and ask "what's the cancellation policy?" Upload a research paper and ask it to explain section 3.

This technique is called **RAG — Retrieval-Augmented Generation.** It's how tools like ChatGPT's "chat with PDF", Notion AI, and most company AI assistants actually work.

**Why it's resume-worthy:** RAG is *the* most in-demand applied-AI skill right now. Every company wants an AI that knows *their* documents. If you can say "I built a RAG pipeline with embeddings, a vector database, and an LLM," you are speaking the exact language of AI engineering job postings. This single project ties together everything from Module 8.

---

## What You'll Learn

| Concept | What It Means |
|---------|---------------|
| **RAG** | Retrieval-Augmented Generation — find relevant text, then let the LLM answer with it |
| **Chunking** | Splitting big documents into small, searchable pieces |
| **Embeddings** | Turning text into numbers that capture *meaning* (similar meaning = similar numbers) |
| **Vector database** | A special database that finds text by *meaning*, not just keywords |
| **Retrieval** | Pulling the most relevant chunks for a given question |

---

## How RAG Works (The Big Picture)

Read this before coding — it makes everything click:

```
   YOUR DOCUMENTS                           YOUR QUESTION
        │                                        │
        ▼                                        ▼
  1. Split into chunks                   4. Turn question into
        │                                   an embedding (numbers)
        ▼                                        │
  2. Turn each chunk into                        ▼
     an embedding (numbers)             5. Search the vector DB for
        │                                  the most similar chunks
        ▼                                        │
  3. Store in a vector DB ──────────────────────▶│
                                                  ▼
                                    6. Send those chunks + the question
                                       to the LLM → it writes the answer
```

**In plain language:** We pre-process your documents into a searchable "memory." When you ask a question, we find the most relevant pieces and hand them to Claude, saying *"answer using only this."* The AI stays grounded in your actual documents instead of making things up.

---

## Prerequisites & Tools

- Python 3.x
- Your Anthropic API key set up (from Project 4 — `export ANTHROPIC_API_KEY="..."`)
- Install the libraries:

```bash
pip install streamlit anthropic
pip install sentence-transformers   # creates embeddings (runs locally, free)
pip install faiss-cpu               # the vector database (fast & free)
pip install pypdf                   # reads PDF files
```

> **Why these?** `sentence-transformers` makes embeddings on your own computer for free. `faiss` (made by Meta) is a fast vector database. Both are free and need no account.

---

## Step 1: Load and Chunk a Document

Create `rag_app.py`. First, the building blocks. We'll read a document and split it into overlapping chunks.

```python
# rag_app.py
# A "Chat with your documents" RAG application

from pypdf import PdfReader

# Step 1a: Read text from a PDF file
def load_pdf(file_path):
    reader = PdfReader(file_path)
    text = ""
    for page in reader.pages:
        text += page.extract_text() + "\n"
    return text


# Step 1b: Split long text into smaller overlapping chunks
# WHY chunks? LLMs and search work best on small pieces.
# WHY overlap? So a sentence split across two chunks isn't lost.
def chunk_text(text, chunk_size=500, overlap=50):
    chunks = []
    start = 0
    while start < len(text):
        end = start + chunk_size
        chunks.append(text[start:end])
        start = end - overlap  # step back a little so chunks overlap
    return chunks
```

> **chunk_size=500 / overlap=50:** 500 characters is small enough to be specific but big enough to hold a full idea; 50 characters of overlap keeps ideas that straddle a boundary from being lost. These are common starting values — tune them for your documents.

---

## Step 2: Create Embeddings and Store Them

Now we turn each chunk into an embedding (a list of numbers capturing its meaning) and store them in FAISS.

```python
from sentence_transformers import SentenceTransformer
import faiss
import numpy as np

# Load the embedding model once (downloads ~90 MB the first time)
embedder = SentenceTransformer("all-MiniLM-L6-v2")

# Step 2: Build a searchable index from a list of text chunks
def build_index(chunks):
    # Turn every chunk into an embedding vector
    embeddings = embedder.encode(chunks)
    embeddings = np.array(embeddings).astype("float32")

    # Create a FAISS index that measures similarity by distance
    dimension = embeddings.shape[1]   # length of each embedding vector
    index = faiss.IndexFlatL2(dimension)
    index.add(embeddings)             # store all the chunk embeddings

    return index, chunks
```

**In plain language:** Each chunk becomes a point in "meaning space." Chunks about similar topics sit close together. FAISS lets us instantly find the nearest points to any question.

---

## Step 3: Retrieve the Most Relevant Chunks

When a question comes in, we embed it and find the closest chunks.

```python
# Step 3: Find the chunks most relevant to a question
def retrieve(question, index, chunks, top_k=3):
    # Turn the question into an embedding too
    q_embedding = embedder.encode([question]).astype("float32")

    # Search the index for the top_k closest chunks
    distances, indices = index.search(q_embedding, top_k)

    # Gather the actual text of those chunks
    relevant_chunks = [chunks[i] for i in indices[0]]
    return relevant_chunks
```

> **top_k=3:** We grab the 3 most relevant chunks. Enough context to answer well, without overwhelming the LLM (or your token budget).

---

## Step 4: Ask Claude to Answer Using the Chunks

This is where retrieval meets generation. We give Claude the relevant chunks and the question, instructing it to answer *only* from the provided text.

```python
import os
from anthropic import Anthropic

client = Anthropic(api_key=os.environ.get("ANTHROPIC_API_KEY"))

# Step 4: Generate an answer grounded in the retrieved chunks
def answer_question(question, relevant_chunks):
    # Combine the chunks into one context block
    context = "\n\n---\n\n".join(relevant_chunks)

    # The prompt tells Claude to ONLY use the provided context
    prompt = f"""Answer the question using ONLY the context below.
If the answer is not in the context, say "I couldn't find that in the document."

CONTEXT:
{context}

QUESTION: {question}

ANSWER:"""

    response = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=500,
        messages=[{"role": "user", "content": prompt}]
    )
    return response.content[0].text
```

**In plain language:** We hand Claude the relevant pieces and say "answer using only this." That instruction is what keeps it grounded in your documents and stops it from inventing answers.

---

## Step 5: Wrap It in a Streamlit Web App

Now tie everything together into an app where you upload a PDF and chat with it. Add this to the **bottom** of `rag_app.py`:

```python
import streamlit as st

# Step 5: The web interface
st.title("📄 Chat With Your Documents")
st.caption("Upload a PDF, then ask questions about it!")

# Check the API key is set
if not os.environ.get("ANTHROPIC_API_KEY"):
    st.error("Set your ANTHROPIC_API_KEY environment variable first.")
    st.stop()

# Step 5a: File upload widget
uploaded_file = st.file_uploader("Upload a PDF", type="pdf")

if uploaded_file:
    # Save the upload temporarily and process it (only once, then cache)
    if "index" not in st.session_state:
        with st.spinner("Reading and indexing your document..."):
            # Save the uploaded file so pypdf can read it
            with open("temp_upload.pdf", "wb") as f:
                f.write(uploaded_file.getbuffer())

            text = load_pdf("temp_upload.pdf")
            chunks = chunk_text(text)
            index, chunks = build_index(chunks)

            # Store in session so we don't re-index on every question
            st.session_state.index = index
            st.session_state.chunks = chunks
        st.success(f"Done! Indexed {len(st.session_state.chunks)} chunks. Ask away!")

    # Step 5b: Question box
    question = st.text_input("Ask a question about your document:")
    if question:
        with st.spinner("Searching and thinking..."):
            relevant = retrieve(question, st.session_state.index,
                                st.session_state.chunks)
            answer = answer_question(question, relevant)

        st.markdown("### Answer")
        st.write(answer)

        # Show the source chunks for transparency
        with st.expander("📚 Sources used"):
            for i, chunk in enumerate(relevant, 1):
                st.markdown(f"**Chunk {i}:** {chunk}")
```

**Run it:** `streamlit run rag_app.py`

Upload any PDF — your class notes, a manual, an article — and ask it questions. **You just built a real RAG application!** This is the same core technique behind enterprise AI assistants worth millions.

> **Note:** This is a code *sketch* — runnable end-to-end, but kept minimal for learning. The Extensions below show how to make it production-grade.

---

## The Full Pipeline at a Glance

| Step | Function | What Happens |
|------|----------|--------------|
| 1 | `load_pdf` + `chunk_text` | Read the doc, split into pieces |
| 2 | `build_index` | Turn chunks into embeddings, store in FAISS |
| 3 | `retrieve` | Find the chunks most relevant to the question |
| 4 | `answer_question` | Claude answers using only those chunks |
| 5 | Streamlit | A friendly web interface ties it together |

---

## Extensions & Improvements

1. **Multiple documents** — let users upload several files and search across all of them at once.
2. **Use Chroma instead of FAISS** — `pip install chromadb`. Chroma saves your index to disk so you don't re-index every time you restart.
3. **Show confidence / citations** — display which page each chunk came from so users can verify answers.
4. **Conversation memory** — combine with Project 4's chat history so you can ask follow-up questions.
5. **Better chunking** — split on paragraphs or sentences instead of fixed character counts (try LangChain's `RecursiveCharacterTextSplitter`).
6. **Support more file types** — `.txt`, `.docx`, web pages.
7. **Deploy it** on Streamlit Community Cloud so anyone can try it with a link.

---

## How to Show This on Your Portfolio / GitHub

1. Create a repo `rag-document-chat`. This should be the **star of your portfolio.**
2. **Protect your API key** — add `.gitignore` with `.env` and any `temp_upload.pdf`. Read the key from an environment variable, never hardcode it.
3. Add a `requirements.txt` (`streamlit`, `anthropic`, `sentence-transformers`, `faiss-cpu`, `pypdf`).
4. In your `README.md`:
   - Include the **RAG pipeline diagram** from this guide (the box-and-arrows). It instantly shows you understand the architecture.
   - Add a screenshot or short GIF of uploading a PDF and getting an answer.
   - Explain each step (chunking → embeddings → vector search → generation) in plain English.
5. **Portfolio tip:** Pin this repo on your GitHub profile and lead your resume's projects section with it. RAG is the single most marketable thing on this list — and being able to *explain* it (not just run it) is what lands interviews.

> **One-line resume bullet:** *"Built a Retrieval-Augmented Generation (RAG) app that lets users chat with their own PDFs — chunking, sentence-transformer embeddings, a FAISS vector store, and the Claude API for grounded answers — with a Streamlit UI."*

---

## You Did It! All 5 Projects Complete 🎉

Look at what you've built across Module 8:

| Project | Skill Demonstrated |
|---------|--------------------|
| Movie Recommender | Classic ML, pandas, similarity |
| Image Classifier | Deep learning, CNNs, PyTorch, transfer learning |
| Sentiment Analyzer | NLP, classic ML + Transformers |
| Claude Chatbot | LLM APIs, prompt engineering |
| RAG App | Embeddings, vector DBs, the full RAG pipeline |

Put these 5 on GitHub and you have **real, demonstrable AI engineering experience** — the kind that gets interviews at Anthropic, Microsoft, and Google. You're not just "learning AI" anymore. You've *built* AI.

---

## What's Next? → Module 9: AI Safety and Ethics

You now know how to *build* powerful AI systems. The next, equally important question is: **how do we build them responsibly?**

In **Module 9: AI Safety and Ethics**, you'll learn:
- **Bias and fairness** — how AI can accidentally discriminate, and how to detect and reduce it (imagine if your sentiment analyzer was biased against certain dialects).
- **Hallucinations** — why LLMs sometimes confidently make things up, and how techniques like the RAG app you just built help reduce this.
- **Privacy and data handling** — how to treat user data responsibly (remember how carefully we protected API keys? That mindset extends to *all* data).
- **Alignment** — making sure AI systems do what humans actually want. This is the core of Anthropic's mission.
- **Responsible deployment** — guardrails, testing, and knowing when *not* to use AI.

Safety isn't the boring part — at companies like Anthropic, it *is* the mission. Understanding it makes you a more thoughtful, more hireable AI engineer. See you in Module 9!
