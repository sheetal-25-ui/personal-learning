# Module 8 — Project 3: Sentiment Analyzer

## What We're Building (and Why It's Resume-Worthy)

You'll build a program that reads a piece of text — like a product review or a tweet — and decides whether it's **positive** or **negative**. This is called **sentiment analysis**, and it's one of the most common real-world NLP (Natural Language Processing) tasks.

We'll build it **two ways**:
1. **The classic ML way** — TF-IDF + Logistic Regression with scikit-learn. Fast, simple, and teaches you the fundamentals.
2. **The modern way** — a pre-trained Hugging Face Transformer model. State-of-the-art accuracy with just a few lines.

**Why it's resume-worthy:** Companies use sentiment analysis on customer reviews, support tickets, and social media. Building both a classic and a modern version shows you understand the *evolution* of NLP — and that you know how to use Hugging Face, the GitHub of AI models.

---

## What You'll Learn

| Concept | What It Means |
|---------|---------------|
| **NLP** | Teaching computers to understand human language |
| **TF-IDF** | Turning text into numbers (you saw this in Project 1!) |
| **Logistic Regression** | A simple, fast model that classifies into categories |
| **Transformers** | The architecture behind ChatGPT, Claude, and modern AI |
| **Hugging Face** | A platform with thousands of free, pre-trained AI models |

---

## Prerequisites & Tools

- Python 3.x
- Install the libraries:

```bash
pip install scikit-learn pandas
pip install transformers torch   # for the Hugging Face part (Part 2)
```

> The Hugging Face part downloads a model the first time you run it (a few hundred MB). Be on Wi-Fi.

---

# PART 1: The Classic Approach (scikit-learn)

## Step 1: Create Training Data

Create a file `sentiment_classic.py`. We'll start with a small labeled dataset (text + whether it's positive or negative). Label `1` = positive, `0` = negative.

```python
# sentiment_classic.py
# A sentiment analyzer using TF-IDF + Logistic Regression

import pandas as pd

# Step 1: Labeled training data
# 1 = positive, 0 = negative
data = {
    "text": [
        "I absolutely loved this movie, it was fantastic!",
        "Best purchase I have ever made, highly recommend.",
        "Amazing quality and great customer service.",
        "This is wonderful, exceeded my expectations.",
        "So happy with this product, five stars!",
        "Terrible experience, would not recommend.",
        "The worst product I have ever bought.",
        "Completely disappointed, waste of money.",
        "Awful quality, broke after one day.",
        "I hate it, total garbage and a rip-off.",
    ],
    "label": [1, 1, 1, 1, 1, 0, 0, 0, 0, 0]
}

df = pd.DataFrame(data)
print(df)
```

---

## Step 2: Turn Text into Numbers & Train the Model

```python
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression

# Step 2a: Convert text into TF-IDF number vectors
vectorizer = TfidfVectorizer()
X = vectorizer.fit_transform(df["text"])  # the features (numbers)
y = df["label"]                            # the answers (0 or 1)

# Step 2b: Train a Logistic Regression classifier
# It learns which words point to positive vs negative
model = LogisticRegression()
model.fit(X, y)

print("Model trained!")
```

**In plain language:** TF-IDF turns each sentence into numbers, then Logistic Regression learns that words like "loved," "amazing," "best" lean positive, while "terrible," "worst," "hate" lean negative.

---

## Step 3: Make Predictions

```python
# Step 3: A function to predict sentiment of new text
def predict_sentiment(text):
    # Turn the new text into numbers using the SAME vectorizer
    numbers = vectorizer.transform([text])
    prediction = model.predict(numbers)[0]
    confidence = model.predict_proba(numbers)[0].max()

    label = "Positive 😊" if prediction == 1 else "Negative 😞"
    return f"{label} (confidence: {confidence:.0%})"


# Step 3b: Test it on brand-new sentences
test_sentences = [
    "This is the best thing ever!",
    "I really did not like it at all.",
    "Great product, very happy.",
    "What a horrible waste of time."
]

for sentence in test_sentences:
    print(f'"{sentence}" -> {predict_sentiment(sentence)}')
```

**Run it:** `python3 sentiment_classic.py`

You'll see each sentence classified as positive or negative with a confidence score. With this tiny dataset it works on clear examples but struggles with subtle ones — that's expected.

---

## Step 4: Make It Better with a Real Dataset

The model is only as smart as its training data. For real results, use a bigger dataset. scikit-learn or Hugging Face can give you the IMDB movie reviews dataset (50,000 labeled reviews):

```python
# Optional: train on a real dataset of 50,000 movie reviews
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score

# (After loading a big dataset into df with the same 'text'/'label' columns)
X = vectorizer.fit_transform(df["text"])
X_train, X_test, y_train, y_test = train_test_split(X, df["label"], test_size=0.2)

model.fit(X_train, y_train)
predictions = model.predict(X_test)
print(f"Accuracy: {accuracy_score(y_test, predictions):.1%}")
# On the real IMDB dataset, this approach reaches ~88% accuracy!
```

> **Tip:** `train_test_split` holds back 20% of the data so you can honestly check how well the model does on text it's never seen. Always test on unseen data!

---

# PART 2: The Modern Approach (Hugging Face)

Now the magic. Instead of training our own model, we'll use one that experts already trained on millions of examples. This takes about **3 lines of code** and gives world-class accuracy.

## Step 5: Use a Pre-Trained Transformer

Create a new file `sentiment_modern.py`:

```python
# sentiment_modern.py
# Sentiment analysis using a pre-trained Hugging Face model

from transformers import pipeline

# Step 1: Load a ready-made sentiment-analysis pipeline
# The first run downloads the model (~250 MB). After that it's cached.
classifier = pipeline("sentiment-analysis")

# Step 2: Analyze some text!
texts = [
    "I absolutely loved this movie, it was fantastic!",
    "This is the worst product I have ever bought.",
    "The food was okay, nothing special but not bad.",
    "I'm so excited about my new job!"
]

for text in texts:
    result = classifier(text)[0]
    label = result["label"]        # POSITIVE or NEGATIVE
    score = result["score"]        # confidence 0-1
    print(f'"{text}"')
    print(f"   -> {label} (confidence: {score:.0%})\n")
```

**Run it:** `python3 sentiment_modern.py`

This uses a Transformer model (the same family as Claude and ChatGPT) and handles nuance far better than our simple classic model. Notice how little code it took — that's the power of pre-trained models!

---

## Step 6: Build a Mini Interactive Tool

Let's make it usable — type a sentence and get an instant answer. Add to `sentiment_modern.py`:

```python
# Step 6: Interactive mode — keep analyzing until the user types 'quit'
print("=== Sentiment Analyzer (type 'quit' to exit) ===")
while True:
    user_text = input("\nEnter some text: ")
    if user_text.lower() == "quit":
        print("Goodbye!")
        break
    result = classifier(user_text)[0]
    print(f"   -> {result['label']} (confidence: {result['score']:.0%})")
```

Now you have an interactive sentiment tool you can demo live!

---

## Classic vs Modern

| | Classic (scikit-learn) | Modern (Hugging Face) |
|---|---|---|
| **Setup** | Train it yourself | Download & use instantly |
| **Accuracy** | Good (~88% with enough data) | Excellent (~95%+) |
| **Understands nuance** | Weak (just keywords) | Strong (understands context) |
| **Speed** | Very fast | Slower (bigger model) |
| **Teaches you** | How ML fundamentals work | How to use modern AI |

**Why build both?** Interviewers want to know you understand *fundamentals* (classic) AND can use *modern tools* (Hugging Face). Showing both is a strong signal.

---

## Extensions & Improvements

1. **Multi-class sentiment** — go beyond positive/negative to also detect "neutral" and even emotions (joy, anger, sadness). Hugging Face has models for this — try `pipeline("text-classification", model="j-hartmann/emotion-english-distilroberta-base")`.
2. **Analyze real reviews** — scrape or paste Amazon/Yelp reviews and chart how many are positive vs negative.
3. **Other languages** — Hugging Face has multilingual models; analyze Hindi or French text.
4. **Build a web app** with Streamlit so people can paste text in a browser (you'll learn Streamlit in Projects 4 & 5).
5. **Compare the two models** on the same 100 sentences and report where they disagree — a great portfolio analysis.

---

## How to Show This on Your Portfolio / GitHub

1. Create a repo `sentiment-analyzer` with both `sentiment_classic.py` and `sentiment_modern.py`.
2. Add a `requirements.txt` (`scikit-learn`, `pandas`, `transformers`, `torch`).
3. In your `README.md`:
   - Explain the **two approaches** and when you'd use each.
   - Show example outputs (screenshots).
   - Include a short table comparing accuracy.
4. **Portfolio tip:** Add a section *"What I learned"* explaining the difference between traditional ML (TF-IDF + Logistic Regression) and Transformers. This shows depth of understanding — exactly what AI companies look for.

> **One-line resume bullet:** *"Built a sentiment analyzer two ways — a classic TF-IDF + Logistic Regression model in scikit-learn and a Transformer-based classifier using Hugging Face — and compared their accuracy and trade-offs."*

---

## What's Next?

In **Project 4**, you'll build something you can actually *talk to*: an **AI chatbot powered by the Claude API**. You'll learn how to call a large language model, keep conversation history, write a system prompt, and protect your API key. This is the gateway to building real AI apps!
