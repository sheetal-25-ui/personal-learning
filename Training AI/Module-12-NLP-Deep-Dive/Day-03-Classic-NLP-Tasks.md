# Module 12 — Day 3: Classic NLP Tasks

## Think of it Like This...

You've learned to **clean** text (Day 1) and turn it into **numbers** (Day 2). Now comes the fun part — actually **doing useful things** with text!

Think of yourself as a brand-new office assistant who just got handed a stack of documents. Your boss asks you to:

- 📊 **Sort** these reviews into "positive" and "negative" piles → *Text Classification & Sentiment*
- 🔍 **Highlight** every person's name, company, and date → *Named Entity Recognition*
- 🏷️ **Label** which words are nouns, verbs, adjectives → *Part-of-Speech Tagging*

These are the **classic NLP tasks** — the bread and butter of language AI. Once you can do these, you can build spam filters, review analyzers, resume scanners, and more. Let's go! 🚀

---

## The 4 Tasks We'll Master Today

```
+-------------------------+------------------------------------------+
| Task                    | What it does                             |
+-------------------------+------------------------------------------+
| 1. Text Classification  | Sort documents into categories           |
|                         | (spam/not-spam, sports/politics/tech)     |
+-------------------------+------------------------------------------+
| 2. Sentiment Analysis   | Decide if text is positive or negative   |
|                         | (a special kind of classification)        |
+-------------------------+------------------------------------------+
| 3. Named Entity         | Find names of people, places, orgs,       |
|    Recognition (NER)    | dates, money in text                      |
+-------------------------+------------------------------------------+
| 4. Part-of-Speech       | Label each word's grammatical role        |
|    (POS) Tagging        | (noun, verb, adjective...)                |
+-------------------------+------------------------------------------+
```

---

## Setup

```python
# Install (run in terminal or Colab)
# pip install spacy scikit-learn
# python -m spacy download en_core_web_sm   <- the English model

import spacy
nlp = spacy.load("en_core_web_sm")   # load the small English model
print("spaCy ready! ✅")
```

> 💡 spaCy is like a Swiss Army knife — one tool does tokenizing, POS tagging, NER, and more. scikit-learn is our machine-learning workhorse for classification.

---

## Task 1 & 2: Text Classification + Sentiment Analysis

These are the same idea: **read text → predict a label.** Sentiment analysis is just classification where the labels are "positive" / "negative."

### How It Works (The Recipe)

```
  Training data:                        Train a model:
  +-------------------+--------+
  | "I love it!"      | pos    |        TEXT  -> [TF-IDF numbers] -> CLASSIFIER -> LABEL
  | "Worst ever"      | neg    |
  | "Amazing food"    | pos    |        Then on NEW text:
  | "Terrible service"| neg    |        "Great!" -> [numbers] -> model -> "pos" ✅
  +-------------------+--------+
```

Remember from Day 2: we turn text into TF-IDF numbers, then feed those numbers to a classifier. Let's build a real sentiment classifier!

### Building a Sentiment Classifier (scikit-learn)

```python
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import make_pipeline

# 1. Our tiny training dataset (text + label)
texts = [
    "I love this movie, it was fantastic",
    "Absolutely wonderful experience",
    "Best purchase I have ever made",
    "Great quality and fast delivery",
    "I hate this, complete waste of money",
    "Terrible, would not recommend",
    "Awful service and rude staff",
    "Worst product ever, very disappointed",
]
labels = ["positive", "positive", "positive", "positive",
          "negative", "negative", "negative", "negative"]

# 2. Build a pipeline: TF-IDF -> Logistic Regression classifier
#    A pipeline chains steps so you can train & predict in one shot.
model = make_pipeline(TfidfVectorizer(), LogisticRegression())

# 3. Train it!
model.fit(texts, labels)

# 4. Predict on NEW, unseen reviews
new_reviews = [
    "This is amazing, I love it",
    "Horrible, total garbage",
    "Pretty good overall"
]
predictions = model.predict(new_reviews)

for review, pred in zip(new_reviews, predictions):
    print(f"'{review}' -> {pred}")

# 'This is amazing, I love it' -> positive
# 'Horrible, total garbage'    -> negative
# 'Pretty good overall'        -> positive
```

🎉 You just trained an AI to read emotions in text! (In real life you'd use thousands of examples, not 8 — but the recipe is identical.)

### Bonus: Get Confidence Scores

```python
# How SURE is the model? predict_proba gives probabilities
probs = model.predict_proba(["This is amazing, I love it"])
print("Classes:", model.classes_)        # ['negative' 'positive']
print("Probabilities:", probs[0].round(2))  # [0.18 0.82] -> 82% sure it's positive
```

### Quick Sentiment Without Training (VADER)

Sometimes you don't want to train anything. **VADER** is a rule-based sentiment tool that works out of the box — great for social media:

```python
# pip install nltk
import nltk
nltk.download('vader_lexicon')
from nltk.sentiment import SentimentIntensityAnalyzer

sia = SentimentIntensityAnalyzer()

for text in ["I love this!! 😍", "This is okay I guess", "Absolutely terrible 😡"]:
    score = sia.polarity_scores(text)
    print(f"{text:30} -> {score['compound']:+.2f}")
# compound score: +1 = very positive, -1 = very negative, 0 = neutral
# Note: VADER even understands emojis and ALL CAPS for emphasis!
```

---

## Task 3: Named Entity Recognition (NER)

**NER finds and labels the "important things" in text** — people, places, companies, dates, money, etc. It's like a highlighter that knows *what kind* of thing each highlight is.

### Example

```
   INPUT:  "Apple was founded by Steve Jobs in California in 1976
            and is now worth $3 trillion."

   OUTPUT:
   +------------+----------+
   | Apple      | ORG      |  (organization)
   | Steve Jobs | PERSON   |
   | California | GPE      |  (geo-political entity / location)
   | 1976       | DATE     |
   | $3 trillion| MONEY    |
   +------------+----------+
```

### NER with spaCy

```python
import spacy
nlp = spacy.load("en_core_web_sm")

text = ("Apple was founded by Steve Jobs in California in 1976 "
        "and is now worth $3 trillion. Tim Cook is the current CEO.")

doc = nlp(text)

print("Entities found:")
for ent in doc.ents:
    print(f"   {ent.text:15} -> {ent.label_:8} ({spacy.explain(ent.label_)})")

# Apple           -> ORG      (Companies, agencies, institutions)
# Steve Jobs      -> PERSON   (People, including fictional)
# California      -> GPE      (Countries, cities, states)
# 1976            -> DATE     (Absolute or relative dates)
# $3 trillion     -> MONEY    (Monetary values)
# Tim Cook        -> PERSON   (People, including fictional)
```

### Common Entity Labels

| Label | Meaning | Examples |
|-------|---------|----------|
| `PERSON` | People | Steve Jobs, Marie Curie |
| `ORG` | Organizations | Apple, NASA, Google |
| `GPE` | Countries/cities/states | India, London, Texas |
| `DATE` | Dates | 1976, last Tuesday |
| `MONEY` | Money amounts | $3 trillion, ₹500 |
| `TIME` | Times | 3 PM, two hours |
| `PRODUCT` | Products | iPhone, Tesla Model 3 |
| `LOC` | Non-GPE locations | the Pacific Ocean, Mount Everest |

> 💡 **Tip:** Use `spacy.explain("LABEL")` anytime you forget what a label means!

### Visualizing NER (Super Cool 😎)

```python
from spacy import displacy

doc = nlp("Microsoft hired Sarah from Toronto for $200,000 last March.")

# In Jupyter/Colab, this renders colorful highlighted text:
displacy.render(doc, style="ent", jupyter=True)

# To save as an HTML file instead:
html = displacy.render(doc, style="ent", page=True)
with open("ner_output.html", "w", encoding="utf-8") as f:
    f.write(html)
print("Saved! Open ner_output.html in your browser 🎨")
```

### Real-World Uses of NER
- 📄 **Resume parsing** — auto-extract names, companies, skills
- 📰 **News analysis** — track which companies/people are mentioned
- 🏥 **Medical records** — find drug names, dosages, conditions
- 💬 **Chatbots** — extract "book a flight to *Paris* on *Friday*"

---

## Task 4: Part-of-Speech (POS) Tagging

**POS tagging labels each word's grammatical role** — is it a noun, verb, adjective, etc.? This helps the computer understand sentence *structure*.

### Example

```
   "The quick brown fox jumps"
     |     |     |    |    |
    DET   ADJ   ADJ  NOUN  VERB
   (the) (quick)(brown)(fox)(jumps)
```

### POS Tagging with spaCy

```python
import spacy
nlp = spacy.load("en_core_web_sm")

doc = nlp("The quick brown fox jumps over the lazy dog")

print(f"{'WORD':10} {'POS':8} {'EXPLANATION'}")
print("-" * 45)
for token in doc:
    print(f"{token.text:10} {token.pos_:8} {spacy.explain(token.pos_)}")

# The        DET      determiner
# quick      ADJ      adjective
# brown      ADJ      adjective
# fox        NOUN     noun
# jumps      VERB     verb
# over       ADP      adposition
# the        DET      determiner
# lazy       ADJ      adjective
# dog        NOUN     noun
```

### Common POS Tags

| Tag | Meaning | Examples |
|-----|---------|----------|
| `NOUN` | noun (thing) | cat, freedom, idea |
| `VERB` | action word | run, think, is |
| `ADJ` | adjective (describes) | happy, big, blue |
| `ADV` | adverb (describes verb) | quickly, very |
| `PRON` | pronoun | he, she, it, they |
| `DET` | determiner | the, a, this |
| `ADP` | preposition | in, on, over |
| `PROPN` | proper noun (name) | London, Sheetal |

### Why POS Tagging Matters

POS tags help disambiguate words! The word "book" can be a noun or a verb:

```python
doc1 = nlp("I read a book")        # book = NOUN
doc2 = nlp("I will book a flight") # book = VERB

print(doc1[3].text, "->", doc1[3].pos_)  # book -> NOUN
print(doc2[2].text, "->", doc2[2].pos_)  # book -> VERB
```

This is hugely useful for: extracting key phrases (find all nouns), grammar checkers, and understanding sentence meaning.

---

## Putting It All Together: A "Document Analyzer"

Let's combine everything into one tool that analyzes any piece of text:

```python
import spacy
nlp = spacy.load("en_core_web_sm")

def analyze_document(text):
    doc = nlp(text)
    print(f"📄 ANALYZING: \"{text}\"\n")

    # 1. Entities
    print("🔍 Named Entities:")
    if doc.ents:
        for ent in doc.ents:
            print(f"   • {ent.text} ({ent.label_})")
    else:
        print("   (none found)")

    # 2. Key nouns (often the "topics")
    nouns = [t.text for t in doc if t.pos_ in ("NOUN", "PROPN")]
    print(f"\n🏷️ Key nouns/topics: {nouns}")

    # 3. Actions (verbs)
    verbs = [t.lemma_ for t in doc if t.pos_ == "VERB"]
    print(f"⚡ Actions (verbs): {verbs}")

    # 4. Sentence count
    print(f"\n📊 Number of sentences: {len(list(doc.sents))}")

analyze_document(
    "Elon Musk announced that Tesla will build a new factory in Germany next year. "
    "The company plans to hire 10,000 workers."
)
```

This little function is the seed of a real document-understanding system! 🌱

---

## Which Tool for Which Job?

```
Sorting text into categories (spam, topic, sentiment)?
   -> scikit-learn classifier (TF-IDF + LogisticRegression)

Quick sentiment on social media / reviews, no training?
   -> NLTK VADER

Finding names, places, dates, money?
   -> spaCy NER

Understanding grammar / extracting key phrases?
   -> spaCy POS tagging

Want one tool for everything?
   -> spaCy does NER + POS + tokenizing all at once
```

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|---------------|
| **Text Classification** | Sorting text into categories |
| **Sentiment Analysis** | Detecting positive/negative emotion |
| **NER** | Finding & labeling names, places, dates, etc. |
| **POS Tagging** | Labeling each word's grammatical role |
| **Entity** | A real-world "thing" (person, place, org) |
| **Pipeline** | Chained steps (vectorize → classify) |
| **Lemma** | The base form of a word (running → run) |
| **VADER** | A no-training sentiment tool for social media |

---

## Practice Exercises

### Exercise 1: Build a Spam Classifier
Create a tiny dataset of 6 "spam" messages (e.g., "WIN a FREE prize NOW!!!") and 6 "ham" (normal) messages. Train a TF-IDF + LogisticRegression classifier. Then test it on 3 new messages you make up. Did it get them right? Print the confidence scores too.

### Exercise 2: Entity Hunter
Find a paragraph from a news article online. Run spaCy NER on it and list every entity it finds, grouped by type (all PERSONs together, all ORGs together, etc.). Were any entities missed or mislabeled? Note them down.

### Exercise 3: Noun & Verb Extractor
Write a function that takes any sentence and returns two lists: all the nouns and all the verbs (use their lemma form). Test it on: *"The hungry cats were chasing the running mice through the old house."* What do the nouns tell you about the topic?

### Exercise 4 (Challenge): Sentiment Comparison
Take 5 product reviews (find some on Amazon, or write your own). Run them through BOTH (a) your trained scikit-learn classifier and (b) NLTK VADER. Make a table comparing the two. Where do they agree? Where do they disagree, and why might that be?

---

## What's Next?

Fantastic work, Sheetal! 🎉 You can now classify text, detect sentiment, find named entities, and tag parts of speech — these are skills used in real companies every single day.

But here's a secret: everything we did today works *okay*, but it doesn't truly *understand* language the way humans do. The classifier doesn't know that "the movie wasn't bad" is actually positive!

Tomorrow in **Day 4: Modern NLP with Transformers**, we level up to the technology behind ChatGPT and Claude: **BERT and Transformers**. You'll use pre-trained models from **Hugging Face** that have read billions of words and understand context like never before. We'll do classification, NER, and even peek at fine-tuning — with just a few lines of code. The future of NLP awaits! 🤖✨
