# Module 12 — Day 2: Text Representation

## Think of it Like This...

Yesterday you cleaned and chopped your "vegetables" (words). But here's a problem:

> **Computers can't do math with words. They can only do math with numbers.**

Imagine trying to ask a calculator: *"What is `cat` + `dog`?"* It has no idea! We need to **translate words into numbers** first.

That translation is called **text representation** (or "vectorization"). Think of it like this:

```
   "I love pizza"   --->  [0.2, 0.9, 0.1, 0.0, 0.7]
   (human words)         (numbers a computer understands)
```

Once words are numbers, we can do all sorts of magic: measure how similar two sentences are, train classifiers, build search engines, and more. Today we'll learn **three ways** to do this translation — from simple to genuinely mind-blowing. 🤯

---

## The Big Picture: 3 Ways to Turn Words into Numbers

```
+---------------------+-----------------------------+--------------------+
| Method              | Idea                        | Smartness          |
+---------------------+-----------------------------+--------------------+
| 1. Bag-of-Words     | Count how often each word    | ⭐ Basic            |
|                     | appears                      |                    |
+---------------------+-----------------------------+--------------------+
| 2. TF-IDF           | Count, but downweight        | ⭐⭐ Better          |
|                     | common boring words          |                    |
+---------------------+-----------------------------+--------------------+
| 3. Word Embeddings  | Capture MEANING — similar     | ⭐⭐⭐ Brilliant      |
|                     | words get similar numbers    |                    |
+---------------------+-----------------------------+--------------------+
```

Let's build up from the simplest. 🪜

---

## Method 1: Bag-of-Words (BoW)

**The idea:** Forget grammar and word order. Just count how many times each word appears. Throw all the words into a "bag," shake it up, and count.

### A Tiny Example

Suppose we have two sentences:
- Doc 1: `"the cat sat"`
- Doc 2: `"the dog sat"`

**Step 1 — Build the vocabulary** (all unique words):
```
Vocabulary: [cat, dog, sat, the]
```

**Step 2 — Count each word per document:**

| Document | cat | dog | sat | the |
|----------|-----|-----|-----|-----|
| Doc 1: "the cat sat" | 1 | 0 | 1 | 1 |
| Doc 2: "the dog sat" | 0 | 1 | 1 | 1 |

Now each document is a **vector of numbers**:
- Doc 1 → `[1, 0, 1, 1]`
- Doc 2 → `[0, 1, 1, 1]`

🎉 We just turned sentences into numbers!

### In Code (scikit-learn)

```python
from sklearn.feature_extraction.text import CountVectorizer

docs = ["the cat sat", "the dog sat", "the cat and the dog played"]

vectorizer = CountVectorizer()
X = vectorizer.fit_transform(docs)

# See the vocabulary the model learned
print("Vocabulary:", vectorizer.get_feature_names_out())
# ['and' 'cat' 'dog' 'played' 'sat' 'the']

# See the count matrix
print(X.toarray())
# [[0 1 0 0 1 1]   <- "the cat sat"
#  [0 0 1 0 1 1]   <- "the dog sat"
#  [1 1 1 1 0 2]]  <- "the cat and the dog played"  (note: 'the' appears 2x!)
```

### The Problem with Bag-of-Words 😕

1. **It ignores word order.** "Dog bites man" and "Man bites dog" get the SAME vector! 😱
2. **Common words dominate.** "the" appears everywhere but tells us nothing.
3. **No meaning.** "happy" and "joyful" are treated as totally unrelated.

The first improvement tackles problem #2. Let's go. ⬇️

---

## Method 2: TF-IDF (The Smarter Counter)

You met TF-IDF briefly before — let's go deeper. TF-IDF fixes the "common words dominate" problem with a clever trick.

**The core insight:** A word is important to a document if it appears **a lot in that document** but **rarely in other documents.**

TF-IDF = **TF** × **IDF**. Two parts:

### Part 1: TF (Term Frequency)
*How often does this word appear in THIS document?*

```
TF(word) = (times word appears in doc) / (total words in doc)
```

If "pizza" appears 3 times in a 10-word review → TF = 3/10 = 0.3

### Part 2: IDF (Inverse Document Frequency)
*How rare is this word across ALL documents?*

```
IDF(word) = log( total documents / documents containing the word )
```

- "the" appears in EVERY document → IDF is tiny (close to 0) → gets crushed 🔨
- "quantum" appears in only 1 document → IDF is large → gets boosted 🚀

### Putting Them Together

```
TF-IDF score = TF × IDF
```

```
   HIGH score = word is frequent HERE but rare ELSEWHERE = IMPORTANT! ⭐
   LOW score  = word is everywhere (like "the") = BORING, ignore it
```

### A Visual

```
Word "the":                          Word "quantum":
  appears in 100/100 docs              appears in 2/100 docs
  IDF = log(100/100) = 0               IDF = log(100/2) = 1.7
  TF-IDF ≈ 0  (ignored! 🗑️)            TF-IDF = high  (important! ⭐)
```

### In Code

```python
from sklearn.feature_extraction.text import TfidfVectorizer

docs = [
    "the cat sat on the mat",
    "the dog sat on the log",
    "quantum physics is fascinating"
]

vectorizer = TfidfVectorizer()
X = vectorizer.fit_transform(docs)

import pandas as pd
df = pd.DataFrame(X.toarray(), columns=vectorizer.get_feature_names_out())
print(df.round(2))

# Notice: 'the' gets a LOW score (it's everywhere)
#         'quantum', 'physics' get HIGH scores (rare & distinctive)
```

### Why TF-IDF Is Great
- ✅ Automatically downweights useless common words
- ✅ Highlights words that *distinguish* documents
- ✅ Still simple and fast — works great for search engines and classification!

### Why It's Still Limited
- ❌ Still ignores word order
- ❌ Still no understanding of *meaning* — "car" and "automobile" are unrelated to it

To capture meaning, we need something fundamentally smarter. Enter **embeddings**. 🌟

---

## Method 3: Word Embeddings (The Game Changer) 🌟

Here's the breakthrough idea that powers modern NLP:

> **Represent each word as a vector of numbers, learned in such a way that words with similar MEANINGS get similar vectors.**

With BoW and TF-IDF, "happy" and "joyful" were completely unrelated. With embeddings, they end up **close together** in number-space because they mean similar things!

### The Intuition: A Map of Words

Imagine plotting every word on a giant map. Words that mean similar things cluster together:

```
        sad  unhappy
          \   /
       gloomy        (negative emotions cluster here)

                            happy  joyful
                              \    /
                            cheerful   (positive emotions cluster here)


   king ---- queen
    |          |          (royalty cluster, with gender as a direction →)
   man  ----  woman
```

Each word is a point, described by a list of numbers (its **embedding**). Typically 100–300 numbers per word.

```python
# A word embedding is just a list of numbers, e.g.:
king  = [0.50, 0.68, 0.21, ..., 0.11]   # 300 numbers
queen = [0.52, 0.70, 0.19, ..., 0.13]   # very SIMILAR numbers to king!
banana= [0.01, 0.90, 0.45, ..., 0.88]   # totally DIFFERENT numbers
```

### The Famous Magic Trick: Word Math! ✨

Because words become vectors, you can do **arithmetic with meaning**:

```
   king  -  man  +  woman  ≈  queen   👑
   paris -  france + italy ≈  rome    🇮🇹
```

This actually works! The vector for "king" minus "man" plus "woman" lands near "queen." The model learned the *concept* of royalty and gender as **directions** in number-space. Mind = blown. 🤯

### How Do Embeddings Learn Meaning? (The Word2Vec Idea)

The core trick comes from a famous quote in linguistics:

> *"You shall know a word by the company it keeps."*

Meaning: **words that appear in similar contexts probably mean similar things.**

```
"I drank a cup of ___ in the morning"
   -> coffee? tea? juice?  (these words appear in similar contexts!)
```

**Word2Vec** trains a small neural network on a simple game:
- Show it a word, ask it to predict the surrounding words (or vice versa)
- Slide this window across millions of sentences
- The number patterns the network learns *become* the embeddings

```
   "the fluffy cat sat on the mat"
                ^^^
         predict neighbors of "cat":
         "fluffy", "sat"  -> network adjusts cat's vector
```

After seeing "cat" and "kitten" surrounded by similar words millions of times, their vectors naturally drift close together. No human ever told it "cat and kitten are similar" — it figured that out from data! 🧠

### Word2Vec vs. GloVe (Two Famous Recipes)

| | **Word2Vec** (Google, 2013) | **GloVe** (Stanford, 2014) |
|---|---|---|
| Approach | Predict neighboring words | Count word co-occurrences globally |
| Analogy | Learns by "sliding window" practice | Learns by studying a big co-occurrence table |
| Result | Both produce similar, high-quality embeddings | Both produce similar, high-quality embeddings |

Both give you the same magical result: words-as-vectors that capture meaning. The differences are mostly under the hood.

---

## Let's Play With Real Embeddings! (gensim)

```python
# pip install gensim

import gensim.downloader as api

# Download a small pre-trained model (~66 MB, trained on Wikipedia)
# This may take a minute the first time
print("Loading model... (this downloads ~66MB the first time)")
model = api.load("glove-wiki-gigaword-50")  # 50-dimensional GloVe vectors

# 1. See a word's vector
print("Vector for 'king' (first 5 numbers):", model['king'][:5])

# 2. Find similar words
print("\nWords most similar to 'king':")
for word, score in model.most_similar('king', topn=5):
    print(f"   {word}: {score:.2f}")
# queen, prince, monarch, kingdom, throne ...

# 3. THE MAGIC: king - man + woman = ?
print("\nking - man + woman =")
result = model.most_similar(positive=['king', 'woman'], negative=['man'], topn=1)
print(f"   {result[0][0]} (score: {result[0][1]:.2f})")
# queen! 👑

# 4. Measure similarity between two words (0 = unrelated, 1 = identical)
print("\nSimilarity scores:")
print(f"   cat vs dog:    {model.similarity('cat', 'dog'):.2f}")   # high ~0.9
print(f"   cat vs banana: {model.similarity('cat', 'banana'):.2f}") # low ~0.3
```

Try it yourself — it feels like magic the first time you see `king - man + woman = queen`! ✨

---

## Comparing All Three Methods

Let's see how each represents the word "happy":

```
Bag-of-Words:   happy = [0, 0, 1, 0, 0]   (just a position; "happy" is column 3)
                "joyful" = [0,0,0,0,1]      (totally different, NO relation to happy)

TF-IDF:         happy = [0, 0, 0.7, 0, 0]  (weighted, but still no relation to "joyful")

Embeddings:     happy  = [0.8, 0.2, 0.9, ...]  (300 meaningful numbers)
                joyful = [0.8, 0.3, 0.85,...]  (SIMILAR numbers -> close in meaning! ✅)
```

| Feature | Bag-of-Words | TF-IDF | Embeddings |
|---------|:---:|:---:|:---:|
| Easy to understand | ✅ | ✅ | 🤔 |
| Fast | ✅ | ✅ | ⚡ (once trained) |
| Downweights common words | ❌ | ✅ | ✅ |
| Captures **meaning** | ❌ | ❌ | ✅✅✅ |
| Handles synonyms | ❌ | ❌ | ✅ |
| Vector size | Huge (= vocabulary size) | Huge | Small & dense (50–300) |

---

## When to Use What?

```
Need something quick & interpretable for small data?
   -> TF-IDF (great default, surprisingly strong!)

Need to understand meaning, synonyms, or do semantic search?
   -> Word Embeddings

Building a search engine or document classifier?
   -> Start with TF-IDF, upgrade to embeddings if needed

Working with modern AI (chatbots, LLMs)?
   -> Embeddings everywhere (we'll see them again Days 4 & 5!)
```

> 💡 **Surprising truth:** For many real-world text classification tasks, plain old TF-IDF + a simple classifier *beats* fancy models — and trains in seconds. Don't underestimate the simple tools!

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|---------------|
| **Vector** | A list of numbers representing something |
| **Vectorization** | Turning text into numbers |
| **Bag-of-Words** | Counting word occurrences (ignores order) |
| **TF-IDF** | Counting, but downweighting common words |
| **TF** | How often a word appears in one document |
| **IDF** | How rare a word is across all documents |
| **Embedding** | A dense vector that captures a word's *meaning* |
| **Word2Vec / GloVe** | Famous methods for learning word embeddings |
| **Cosine similarity** | A measure of how "close" two vectors are (0–1) |

---

## Practice Exercises

### Exercise 1: Build a Bag-of-Words by Hand
On paper (no code!), build the BoW vectors for these three sentences:
- "I like cats"
- "I like dogs"
- "cats like dogs"

First list the vocabulary, then write each sentence as a count vector. Which two sentences have the most similar vectors?

### Exercise 2: TF-IDF Detective
Using `TfidfVectorizer` on these docs:
```python
docs = ["the movie was great great great",
        "the food was terrible",
        "the the the the the service"]
```
Print the TF-IDF matrix. Which word has the LOWEST scores everywhere, and why? Which word best distinguishes each document?

### Exercise 3: Word Math Experiments
Load the GloVe model from today's lesson and try these analogies:
1. `paris - france + japan = ?`
2. `walking - walk + swim = ?`
3. `bigger - big + small = ?`

Did they work? Write down what you got. (Not all analogies work perfectly — that's normal and interesting!)

### Exercise 4 (Challenge): Find the Odd One Out
Use `model.doesnt_match(["breakfast", "lunch", "dinner", "car"])` to find which word doesn't belong in a list. Try 3 different lists of your own. Does the model's intuition match yours?

---

## What's Next?

Brilliant work, Sheetal! 🎉 You now understand the secret behind how computers grasp the *meaning* of words — from simple counting all the way to magical word math.

Now that we can turn text into numbers, we can finally **do useful things** with it! Tomorrow in **Day 3: Classic NLP Tasks**, we'll build real applications:
- A **sentiment classifier** that tells happy reviews from angry ones
- A **Named Entity Recognizer** that finds people, places, and dates in text
- A **POS tagger** that labels nouns, verbs, and adjectives

We'll use spaCy and scikit-learn to make it all work. Get ready to build things that actually solve problems! 🛠️
