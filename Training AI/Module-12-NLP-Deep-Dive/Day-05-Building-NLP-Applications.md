# Module 12 — Day 5: Building NLP Applications

## Think of it Like This...

You've spent four days collecting superpowers:
- Day 1: 🧹 Clean text
- Day 2: 🔢 Turn text into numbers
- Day 3: 🏷️ Classify, tag, find entities
- Day 4: 🤖 Use Transformer brains

Today, you become the **superhero who combines all those powers** to save the day! 🦸

Imagine you're handed a 50-page report and asked: *"Summarize this, and let me ask questions about it, and help me find the section about budgets."* By the end of today, you'll have built tools that do **exactly that**.

This is where NLP stops being theory and becomes **products people actually use.** Let's build! 🛠️

---

## Today's Three Real Applications

```
+----------------------+-------------------------------------------+
| 1. Summarization     | Long text -> short summary                |
|                      | "Give me the TL;DR" 📝                     |
+----------------------+-------------------------------------------+
| 2. Question          | Document + question -> answer             |
|    Answering (Q&A)   | "Where does it say X?" ❓                  |
+----------------------+-------------------------------------------+
| 3. Semantic Search   | Find by MEANING, not keywords            |
|                      | "Find docs about happiness" -> finds       |
|                      |  "joy", "contentment" too! 🔍              |
+----------------------+-------------------------------------------+
```

---

## Setup

```python
# Install (run in terminal or Colab)
# pip install transformers torch sentence-transformers

from transformers import pipeline
print("Ready to build! 🚀")
```

---

## Application 1: Text Summarization 📝

**The goal:** Take a long piece of text and produce a short, meaningful summary.

### Two Flavors of Summarization

```
   EXTRACTIVE:  Pull out the most important EXISTING sentences.
                (like a highlighter — copy-pastes key sentences)

   ABSTRACTIVE: Write a NEW summary in fresh words.
                (like a human writing notes — rephrases ideas)
                <- This is what modern Transformers do! 🤖
```

### Summarization in Action

```python
from transformers import pipeline

summarizer = pipeline("summarization")

article = """
Artificial intelligence has transformed many industries over the past decade.
In healthcare, AI helps doctors diagnose diseases faster and more accurately by
analyzing medical images. In finance, AI detects fraudulent transactions in real
time, saving billions of dollars. Self-driving cars use AI to navigate roads
safely. However, these advances also raise concerns about job displacement,
privacy, and the ethical use of automated decision-making. Experts agree that
while AI offers enormous benefits, careful regulation and thoughtful deployment
are essential to ensure it helps rather than harms society.
"""

summary = summarizer(article, max_length=50, min_length=20, do_sample=False)
print("SUMMARY:")
print(summary[0]['summary_text'])

# SUMMARY:
# Artificial intelligence has transformed many industries over the past decade.
# AI helps doctors diagnose diseases faster and detects fraudulent transactions.
# Experts agree careful regulation is essential.
```

### Key Parameters

| Parameter | What it does | Why this value |
|-----------|-------------|----------------|
| `max_length=50` | Longest the summary can be (in tokens) | Keep it short and punchy |
| `min_length=20` | Shortest the summary can be | Avoid one-word summaries |
| `do_sample=False` | Always pick the most likely words | Makes output consistent/repeatable |

> 💡 **Real-world uses:** News digests, meeting-notes summaries, "TL;DR" bots, condensing research papers, customer-review summaries.

---

## Application 2: Question Answering (Q&A) ❓

**The goal:** Give the model a chunk of text (the "context") and a question. It finds the answer *within the text*.

```
   CONTEXT: "The Eiffel Tower is 330 meters tall and was
             completed in 1889 in Paris, France."

   QUESTION: "How tall is the Eiffel Tower?"
   ANSWER:   "330 meters"  ✅ (found inside the context!)
```

### Q&A in Action

```python
from transformers import pipeline

qa = pipeline("question-answering")

context = """
The Python programming language was created by Guido van Rossum and first
released in 1991. It is known for its simple, readable syntax and is widely
used in web development, data science, and artificial intelligence. Python is
free and open-source, maintained by the Python Software Foundation.
"""

questions = [
    "Who created Python?",
    "When was Python first released?",
    "What is Python used for?",
]

for q in questions:
    result = qa(question=q, context=context)
    print(f"Q: {q}")
    print(f"A: {result['answer']}  (confidence: {result['score']:.2f})\n")

# Q: Who created Python?
# A: Guido van Rossum  (confidence: 0.98)
#
# Q: When was Python first released?
# A: 1991  (confidence: 0.97)
#
# Q: What is Python used for?
# A: web development, data science, and artificial intelligence  (confidence: 0.65)
```

> ⚠️ **Important:** This kind of Q&A finds answers **inside the provided text**. It won't answer questions about things not in the context. (That's different from ChatGPT, which generates from its own knowledge.)

> 💡 **Real-world uses:** Customer support bots that answer from a help-doc, searching contracts, FAQ systems, study tools.

---

## Application 3: Semantic Search 🔍

This is the coolest one. Regular search matches **keywords**. Semantic search matches **meaning**.

```
   KEYWORD SEARCH for "happy":
     ✅ finds: "I am happy"
     ❌ misses: "I feel joyful"   (no word "happy"!)

   SEMANTIC SEARCH for "happy":
     ✅ finds: "I am happy"
     ✅ finds: "I feel joyful"     (understands joy ≈ happy!) 🎯
     ✅ finds: "What a wonderful day"
```

### How It Works (Using Embeddings from Day 2!)

```
   1. Turn every document into an embedding (a meaning-vector)
   2. Turn the search query into an embedding too
   3. Find documents whose vectors are CLOSEST to the query's vector

         query: "happy" -> [0.8, 0.2, ...]
                              |
              measure distance to each document
                              |
              closest documents = best matches! ✅
```

We measure "closeness" with **cosine similarity** (from Day 2): 1.0 = identical meaning, 0 = unrelated.

### Building a Semantic Search Engine

```python
from sentence_transformers import SentenceTransformer, util

# Load a model that turns whole sentences into embeddings
# 'all-MiniLM-L6-v2' is small, fast, and great for beginners
model = SentenceTransformer('all-MiniLM-L6-v2')

# Our "database" of documents
documents = [
    "The cat is sleeping on the warm sofa.",
    "Stock markets fell sharply this morning.",
    "I feel so joyful and content today!",
    "The recipe calls for two cups of flour.",
    "Investors are worried about the economy.",
    "My puppy loves to play in the garden.",
]

# Step 1: Embed all documents (do this once, store the results)
doc_embeddings = model.encode(documents)

# Step 2: Embed the search query
query = "happiness and good mood"
query_embedding = model.encode(query)

# Step 3: Find the most similar documents
similarities = util.cos_sim(query_embedding, doc_embeddings)[0]

# Sort and show top results
import numpy as np
top_indices = np.argsort(-similarities)  # minus = descending order

print(f"🔍 Searching for: '{query}'\n")
print("Best matches:")
for idx in top_indices[:3]:
    print(f"   [{similarities[idx]:.2f}] {documents[idx]}")

# Best matches:
#   [0.61] I feel so joyful and content today!     <- found "joyful"! 🎯
#   [0.22] My puppy loves to play in the garden.
#   [0.15] The cat is sleeping on the warm sofa.
```

🎉 Notice it found *"I feel so joyful"* even though we searched for *"happiness"* — no shared keywords, but the **meaning** matched! That's semantic search. This is the technology behind modern search engines and the "retrieval" in AI chatbots.

> 💡 **Real-world uses:** Smart document search, recommendation systems, finding duplicate questions, and the "retrieval" step in RAG (Retrieval-Augmented Generation) chatbots.

---

## Putting It All Together: A Document Assistant 🤖

Let's combine all three into one practical tool — a mini "research assistant" that can summarize a document, answer questions about it, and search a collection:

```python
from transformers import pipeline
from sentence_transformers import SentenceTransformer, util
import numpy as np

class DocumentAssistant:
    """A mini NLP-powered research assistant. 🤖"""

    def __init__(self):
        print("Loading models... (one-time, may take a minute)")
        self.summarizer = pipeline("summarization")
        self.qa = pipeline("question-answering")
        self.embedder = SentenceTransformer('all-MiniLM-L6-v2')
        self.documents = []
        self.doc_embeddings = None
        print("Assistant ready! ✅\n")

    def load_documents(self, docs):
        """Store documents and pre-compute their embeddings for search."""
        self.documents = docs
        self.doc_embeddings = self.embedder.encode(docs)

    def summarize(self, text, max_length=60):
        """Summarize a long piece of text."""
        result = self.summarizer(text, max_length=max_length, min_length=20, do_sample=False)
        return result[0]['summary_text']

    def ask(self, question, context):
        """Answer a question using the given context."""
        result = self.qa(question=question, context=context)
        return result['answer'], result['score']

    def search(self, query, top_k=2):
        """Find the most relevant documents by meaning."""
        query_emb = self.embedder.encode(query)
        sims = util.cos_sim(query_emb, self.doc_embeddings)[0]
        top = np.argsort(-sims)[:top_k]
        return [(self.documents[i], float(sims[i])) for i in top]


# ---- Try it out! ----
assistant = DocumentAssistant()

article = """
Climate change is one of the biggest challenges facing humanity. Rising global
temperatures cause melting ice caps, rising sea levels, and more extreme weather.
Scientists urge governments to reduce carbon emissions by switching to renewable
energy like solar and wind power. Individuals can help by using less energy,
driving less, and eating more plant-based foods.
"""

# 1. Summarize
print("📝 SUMMARY:")
print("  ", assistant.summarize(article), "\n")

# 2. Ask a question
answer, score = assistant.ask("How can individuals help with climate change?", article)
print(f"❓ Q&A: {answer} (confidence: {score:.2f})\n")

# 3. Semantic search across a collection
assistant.load_documents([
    "Solar panels convert sunlight into electricity.",
    "The new cafe downtown serves great coffee.",
    "Wind turbines generate clean renewable energy.",
    "My favorite movie is a science fiction adventure.",
])
print("🔍 SEARCH for 'green energy sources':")
for doc, score in assistant.search("green energy sources"):
    print(f"   [{score:.2f}] {doc}")
```

🏆 **You just built a real, multi-feature NLP application!** This is the kind of tool that powers actual products. Save this code — it's a fantastic portfolio piece!

---

## The Complete NLP Pipeline (Everything You Learned)

Here's the full journey of this module, start to finish:

```
   RAW MESSY TEXT
        |
        |  [Day 1] Clean: lowercase, tokenize, remove junk
        v
   CLEAN TOKENS
        |
        |  [Day 2] Represent: TF-IDF or embeddings
        v
   NUMBERS (VECTORS)
        |
        |  [Day 3] Classic tasks: classify, NER, POS, sentiment
        |  [Day 4] Transformers: BERT, Hugging Face, context
        v
   UNDERSTANDING
        |
        |  [Day 5] Applications: summarize, Q&A, semantic search
        v
   USEFUL PRODUCT! 🎉
```

You now understand the **entire pipeline** that real NLP engineers use. From a pile of raw text to a working app. That's a genuinely valuable skill! 💪

---

## Tips for Building Real NLP Apps

| Tip | Why it matters |
|-----|---------------|
| **Start with pre-trained models** | Don't train from scratch — borrow brains (Day 4) |
| **Pre-compute embeddings** | Embed your documents once, store them, reuse for every search |
| **Handle long text** | Models have length limits — split long docs into chunks |
| **Always check confidence scores** | Low confidence = the model is unsure; show a fallback |
| **Use small models first** | "distil" / "MiniLM" models run on a laptop and are plenty good |
| **Test with weird inputs** | Empty strings, emojis, other languages — real users are messy! |

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|---------------|
| **Summarization** | Condensing long text into a short version |
| **Extractive** | Summary made of copied key sentences |
| **Abstractive** | Summary written in fresh, new words |
| **Question Answering** | Finding an answer inside a given text |
| **Context** | The text the model searches for an answer |
| **Semantic search** | Searching by meaning, not keywords |
| **Sentence embedding** | A meaning-vector for a whole sentence |
| **Cosine similarity** | How close two vectors are (0–1) |
| **RAG** | Retrieval + AI generation (search then answer) |

---

## Practice Exercises

### Exercise 1: Build a News Summarizer
Find a news article online (a few paragraphs). Run it through the summarizer. Try three different `max_length` values (30, 60, 100) and compare the summaries. Which length is most useful? Does the summary capture the main point?

### Exercise 2: FAQ Bot
Write a paragraph describing a fictional product (e.g., a smartwatch — its battery life, price, features). Then use the Q&A pipeline to ask it 4 questions. Did it find the right answers? What happens if you ask a question whose answer ISN'T in the text? (Try it — note the confidence score.)

### Exercise 3: Build Your Own Semantic Search
Create a list of 8 sentences on mixed topics (sports, food, technology, emotions). Use `sentence-transformers` to build a semantic search. Then search for 3 queries using words NOT in your sentences (e.g., search "athletics" when your doc says "running"). Did meaning-based matching work?

### Exercise 4 (Challenge): Extend the DocumentAssistant
Add a new method to the `DocumentAssistant` class called `sentiment(text)` that uses a sentiment pipeline to return whether a piece of text is positive or negative. Test your upgraded assistant by summarizing AND analyzing the sentiment of a product review. You now have a 4-feature NLP tool! 🚀

---

## What's Next? 🎓

**Congratulations, Sheetal — you've completed Module 12!** 🎉🎉🎉

Take a moment to appreciate how far you've come. You can now:
- ✅ Clean and prepare any text
- ✅ Turn words into meaningful numbers
- ✅ Classify text, detect sentiment, and find entities
- ✅ Wield Transformer models like BERT
- ✅ Build real apps: summarizers, Q&A systems, and semantic search

That's a **complete NLP skill set** — the same foundation professional NLP engineers use every day. You should be genuinely proud! 🌟

### Coming Up: Module 13 — Computer Vision 👁️

You've taught computers to understand **language**. Next, we tackle the other great frontier: teaching computers to **see**.

In **Module 13: Computer Vision**, you'll learn how AI:
- 📷 Recognizes objects in photos (cats, cars, faces)
- 🎨 Understands images pixel by pixel
- 🚗 Powers self-driving cars and medical imaging
- 🖼️ Even *generates* brand-new images

Just like NLP turns words into numbers, computer vision turns **pixels** into numbers — and a lot of what you learned here (embeddings, Transformers, classification) carries right over!

You mastered how machines understand text. Now get ready to teach them to see. The adventure continues! See you in Module 13! 👁️✨
