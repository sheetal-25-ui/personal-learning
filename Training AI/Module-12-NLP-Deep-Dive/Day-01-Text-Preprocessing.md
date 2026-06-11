# Module 12 — Day 1: Text Preprocessing

## Think of it Like This...

Imagine you're a chef and someone hands you a big bag of vegetables straight from the farm. They're covered in mud, some have stems, some are too big to cook whole.

**You can't just throw them in the pot.** First you have to:
- 🚿 **Wash** them (remove dirt)
- 🔪 **Chop** them into pieces
- 🗑️ **Trim** the bits you don't need

**Text preprocessing is exactly this — but for words.**

Raw text from the real world is messy: random capital letters, punctuation, typos, useless filler words. Before any AI can learn from it, we have to **clean and chop** it first.

> **Garbage in → Garbage out.** The best AI model in the world will fail if you feed it dirty text. This step is *that* important.

---

## Why Bother? A Quick Example

Look at these two sentences:

```
"I LOVE this product!!!"
"i love this Product."
```

To a human, these mean the same thing. But to a computer, `"LOVE"`, `"love"`, and `"Love"` are **three completely different things** — like three different words! Preprocessing fixes this so the computer sees them as one.

---

## The Preprocessing Pipeline

```
   RAW TEXT: "The Cats are RUNNING quickly!!! :)"
        |
        v
  [1] Lowercasing      -> "the cats are running quickly!!! :)"
        |
        v
  [2] Regex Cleaning   -> "the cats are running quickly"
        |
        v
  [3] Tokenization     -> ["the","cats","are","running","quickly"]
        |
        v
  [4] Stopword Removal -> ["cats","running","quickly"]
        |
        v
  [5] Stemming/Lemma   -> ["cat","run","quickly"]
        |
        v
   CLEAN TOKENS ready for AI! ✅
```

Let's learn each step.

---

## Step 0: Setup

Run this once to install and download what we need:

```python
# Install (run in terminal or Colab cell)
# pip install nltk

import nltk
nltk.download('punkt')          # for tokenization
nltk.download('punkt_tab')      # newer NLTK versions need this too
nltk.download('stopwords')      # list of filler words
nltk.download('wordnet')        # for lemmatization
nltk.download('averaged_perceptron_tagger_eng')  # for POS (used later)

print("All set! ✅")
```

---

## Step 1: Lowercasing

The simplest step. Make everything lowercase so `"Cat"` and `"cat"` are treated as the same word.

```python
text = "The Cats Are RUNNING Quickly"

lowered = text.lower()
print(lowered)
# Output: the cats are running quickly
```

> ⚠️ **Watch out:** Sometimes case *matters*! "US" (the country) vs "us" (the word). "Apple" (company) vs "apple" (fruit). For most beginner tasks, lowercasing is fine — just know it isn't always free.

---

## Step 2: Regex Cleaning (Removing the Junk)

**Regex** (regular expressions) is a mini-language for finding patterns in text. Think of it as "find and replace" on steroids. We use it to strip out punctuation, numbers, emojis, URLs, etc.

```python
import re

text = "Check this out!!! 😍 Visit https://example.com or call 555-1234."

# Remove URLs
text = re.sub(r'http\S+', '', text)

# Remove anything that is NOT a letter or space
# [^a-z A-Z\s] means "any character that is not a-z, A-Z, or whitespace"
text = re.sub(r'[^a-zA-Z\s]', '', text)

# Collapse multiple spaces into one, and trim edges
text = re.sub(r'\s+', ' ', text).strip()

print(text)
# Output: Check this out  Visit  or call
```

### Regex Cheat Sheet (Beginner Version)

| Pattern | Means | Example match |
|---------|-------|---------------|
| `\d` | any digit | `5`, `7` |
| `\s` | any whitespace (space, tab, newline) | ` ` |
| `\S` | any NON-whitespace | `a`, `!` |
| `\w` | a "word character" (letter/digit/underscore) | `b`, `3` |
| `+` | "one or more of the previous" | `\d+` → `2024` |
| `[abc]` | any one of a, b, or c | `a` |
| `[^abc]` | any character EXCEPT a, b, c | `x` |
| `http\S+` | "http" followed by non-spaces (a URL) | `https://x.com` |

> 💡 Don't memorize regex — keep a cheat sheet handy. Even pros look it up constantly! [regex101.com](https://regex101.com) is a great playground.

---

## Step 3: Tokenization (Chopping into Pieces)

**Tokenization** = splitting text into smaller units called **tokens** (usually words).

You might think *"I'll just split on spaces!"* — and sometimes that works. But look:

```python
text = "I can't believe it's done."

# Naive way:
print(text.split())
# ['I', "can't", 'believe', "it's", 'done.']
#  Notice: "done." has a period stuck to it. And "can't" — is that one token or two?
```

Real tokenizers handle these edge cases smartly:

```python
from nltk.tokenize import word_tokenize, sent_tokenize

text = "I can't believe it's done. Let's celebrate!"

# Word tokenization
words = word_tokenize(text)
print(words)
# ['I', 'ca', "n't", 'believe', 'it', "'s", 'done', '.', 'Let', "'s", 'celebrate', '!']

# Sentence tokenization (split into sentences)
sentences = sent_tokenize(text)
print(sentences)
# ['I can\'t believe it\'s done.', "Let's celebrate!"]
```

Notice the tokenizer split `"can't"` into `"ca"` + `"n't"` — it understands contractions! And punctuation became its own token. Smart. 🧠

### Types of Tokens

```
SENTENCE level:  "The cat sat. The dog ran."  -> ["The cat sat.", "The dog ran."]
WORD level:      "The cat sat"                -> ["The", "cat", "sat"]
SUBWORD level:   "unhappiness"                -> ["un", "happiness"]  (used by BERT! Day 4)
CHARACTER level: "cat"                         -> ["c", "a", "t"]
```

For now, **word-level** is what we'll use most.

---

## Step 4: Stopword Removal

**Stopwords** are super-common words that carry little meaning: *the, is, a, an, in, of, and, to...*

In a sentence like *"the cat is on the mat"*, the words that actually matter are *"cat"* and *"mat"*. The rest is glue.

```python
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize

stop_words = set(stopwords.words('english'))
print(list(stop_words)[:10])
# ['i', 'me', 'my', 'myself', 'we', 'our', ...]

text = "the cat is sitting on the comfortable mat"
tokens = word_tokenize(text)

# Keep only words that are NOT stopwords
filtered = [word for word in tokens if word.lower() not in stop_words]
print(filtered)
# ['cat', 'sitting', 'comfortable', 'mat']
```

See how the *meaning* survives even though half the words are gone? That's the goal.

> ⚠️ **Careful:** Removing stopwords helps for tasks like topic detection, but can HURT for tasks like sentiment ("not good" → "good" loses the "not"!). Always ask: *does this word matter for my task?*

---

## Step 5: Stemming vs. Lemmatization

Words have many forms: *run, runs, running, ran*. We often want to reduce them all to a single base form so the computer treats them as one concept. There are two ways:

### Stemming — The Fast & Crude Way ✂️

Stemming chops off endings using simple rules. It's fast but sometimes produces non-words.

```python
from nltk.stem import PorterStemmer

stemmer = PorterStemmer()

words = ["running", "runs", "ran", "easily", "fairly", "studies"]
for w in words:
    print(f"{w:10} -> {stemmer.stem(w)}")

# running    -> run
# runs       -> run
# ran        -> ran      (stemming missed this — it's rule-based!)
# easily     -> easili   (not a real word! but consistent)
# fairly     -> fairli
# studies    -> studi
```

### Lemmatization — The Smart & Slow Way 🧠

Lemmatization uses a dictionary to find the real **base word** (called a **lemma**). Slower, but produces real words.

```python
from nltk.stem import WordNetLemmatizer

lemmatizer = WordNetLemmatizer()

words = ["running", "runs", "studies", "better", "geese"]
for w in words:
    # pos='v' tells it to treat the word as a verb
    print(f"{w:10} -> {lemmatizer.lemmatize(w, pos='v')}")

# running    -> run
# runs       -> run
# studies    -> study
# better     -> better
# geese      -> geese
```

### Quick Comparison

| Feature | Stemming | Lemmatization |
|---------|----------|---------------|
| Method | Chops endings with rules | Uses a dictionary |
| Speed | ⚡ Very fast | 🐢 Slower |
| Output | May not be a real word (`studi`) | Always a real word (`study`) |
| `"better"` → | `better` | `good` (if it knows it's an adjective) |
| Best for | Quick prototypes, search engines | Tasks needing accuracy |

> 💡 **Rule of thumb:** Use **lemmatization** when accuracy matters. Use **stemming** when speed matters and you're processing millions of documents.

---

## Bonus: N-grams (Looking at Word Pairs)

A single word loses context. *"good"* is positive, but *"not good"* is negative! **N-grams** capture sequences of N words together.

```
TEXT: "New York is great"

Unigrams (1-gram): ["New", "York", "is", "great"]
Bigrams  (2-gram): ["New York", "York is", "is great"]
Trigrams (3-gram): ["New York is", "York is great"]
```

Notice how the bigram `"New York"` keeps the city name together — much more meaningful than `"New"` and `"York"` separately!

```python
from nltk.util import ngrams
from nltk.tokenize import word_tokenize

text = "I love natural language processing"
tokens = word_tokenize(text)

# Generate bigrams (pairs)
bigrams = list(ngrams(tokens, 2))
print(bigrams)
# [('I', 'love'), ('love', 'natural'), ('natural', 'language'), ('language', 'processing')]

# Generate trigrams (triples)
trigrams = list(ngrams(tokens, 3))
print(trigrams)
# [('I', 'love', 'natural'), ('love', 'natural', 'language'), ('natural', 'language', 'processing')]
```

---

## Putting It All Together: A Full Cleaning Function

Here's a reusable function combining everything you learned today:

```python
import re
import nltk
from nltk.tokenize import word_tokenize
from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer

# Set these up once
stop_words = set(stopwords.words('english'))
lemmatizer = WordNetLemmatizer()

def preprocess(text):
    """Clean and tokenize raw text into a list of meaningful lemmas."""
    # 1. Lowercase
    text = text.lower()
    # 2. Remove URLs and non-letters (regex cleaning)
    text = re.sub(r'http\S+', '', text)
    text = re.sub(r'[^a-z\s]', '', text)
    # 3. Tokenize
    tokens = word_tokenize(text)
    # 4. Remove stopwords AND tiny words
    tokens = [t for t in tokens if t not in stop_words and len(t) > 1]
    # 5. Lemmatize
    tokens = [lemmatizer.lemmatize(t) for t in tokens]
    return tokens

# Try it!
sample = "The CATS were RUNNING quickly!!! Check http://cats.com 😻"
print(preprocess(sample))
# Output: ['cat', 'running', 'quickly', 'check']
```

🎉 You just built a real preprocessing pipeline that production systems use!

---

## Common Mistakes to Avoid

| Mistake | Why it's bad | Fix |
|---------|-------------|-----|
| Removing stopwords for sentiment analysis | Loses "not", "no", "never" | Keep negation words |
| Lowercasing names/places | "Apple" company vs "apple" fruit | Sometimes skip lowercasing |
| Forgetting to remove URLs | Garbage tokens like "httpswww" | Always clean URLs first |
| Stemming when you need real words | `studi`, `easili` look broken | Use lemmatization |
| Over-cleaning | Removing too much loses meaning | Clean only what hurts your task |

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|---------------|
| **Token** | A single piece of text (usually a word) |
| **Tokenization** | Chopping text into tokens |
| **Stopwords** | Common filler words (the, is, a) we often remove |
| **Stemming** | Crudely chopping word endings (fast) |
| **Lemmatization** | Finding the real base word using a dictionary (accurate) |
| **Regex** | A pattern language for finding/cleaning text |
| **N-gram** | A sequence of N words taken together |
| **Corpus** | A large collection of text (plural: corpora) |

---

## Practice Exercises

### Exercise 1: Clean a Messy Tweet
Take this tweet and write code to clean it using the `preprocess()` function above:
```
"OMG!!! Just watched the BEST movie EVER 🎬🍿 @friend check it out https://movie.link #amazing"
```
Print the cleaned tokens. What got removed? Was anything important lost?

### Exercise 2: Stemming vs. Lemmatization Showdown
For these words — `["caring", "cars", "leaves", "happier", "running", "better"]` — print both the **stem** and the **lemma** side by side in a table. Which method gives more sensible results? Write down 2 observations.

### Exercise 3: Build Your Own N-grams
Write code that takes the sentence `"machine learning is really fun"` and prints:
1. All unigrams
2. All bigrams
3. All trigrams

Then answer: which n-gram size best captures the phrase "machine learning"?

### Exercise 4 (Challenge): Negation-Aware Cleaning
Modify the `preprocess()` function so it does NOT remove negation words (`not`, `no`, `never`). Test it on `"I do not like this at all"`. Compare the output with and without your change. Why does this matter for sentiment analysis?

---

## What's Next?

Awesome work, Sheetal! 🎉 You now know how to take messy real-world text and turn it into clean, meaningful tokens.

But here's the thing: **computers don't understand words — they understand numbers.** So tomorrow in **Day 2: Text Representation**, we'll learn how to turn those clean tokens into numbers (vectors) that a machine learning model can actually use. We'll cover Bag-of-Words, TF-IDF, and the magical **word embeddings** that let computers understand that "king" and "queen" are related!

This is where text starts to feel like real AI. See you tomorrow! 🚀
