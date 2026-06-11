# Module 8 — Project 1: Movie Recommendation System

## What We're Building (and Why It's Resume-Worthy)

You'll build a system that, when you give it a movie you love, recommends other movies you'll probably enjoy. This is **exactly** what Netflix, Spotify, and Amazon do — and it's a classic interview topic.

We'll build **two** kinds of recommenders:
1. **Content-based filtering** — "You liked *Inception*, here are movies with similar genres/keywords."
2. **Simple collaborative filtering** — "People who rated movies like you did, also liked these."

**Why it's resume-worthy:** Recommendation systems power billions of dollars of revenue. Saying "I built a movie recommender using cosine similarity on a real dataset" is a concrete, technical thing that interviewers love. It shows you understand pandas, feature engineering, and similarity math.

---

## What You'll Learn

| Concept | What It Means |
|---------|---------------|
| **pandas** | The #1 Python library for working with tables of data (rows and columns) |
| **TF-IDF** | A way to turn text (like movie descriptions) into numbers |
| **Cosine similarity** | A math trick to measure how "similar" two things are (0 = nothing alike, 1 = identical) |
| **Collaborative filtering** | Recommending based on what similar users liked |

---

## Prerequisites & Tools

- Python 3.x installed (you did this in Module 2!)
- VS Code
- These libraries — install them in your Terminal:

```bash
pip install pandas scikit-learn numpy
```

> **What's pip?** It's Python's app store — it downloads and installs libraries for you.

---

## Step 1: Get Some Movie Data

For a real project you'd download the **MovieLens** dataset (a famous free dataset). But to keep things simple and runnable *right now*, we'll create a small dataset in code. Later, the Extensions section shows how to swap in the real thing.

Create a file called `movie_recommender.py` and start with this:

```python
# movie_recommender.py
# A movie recommendation system using content-based + collaborative filtering

import pandas as pd  # pandas handles our data tables

# Step 1: Create a small movies dataset
# In a real project you'd load this from a CSV file (we'll show that later)
movies_data = {
    "movie_id": [1, 2, 3, 4, 5, 6, 7, 8],
    "title": [
        "Inception", "Interstellar", "The Dark Knight",
        "The Matrix", "Toy Story", "Finding Nemo",
        "The Notebook", "Titanic"
    ],
    # "tags" describes each movie — genres, themes, keywords
    "tags": [
        "sci-fi dream thriller mind-bending nolan",
        "sci-fi space time travel nolan emotional",
        "action superhero crime nolan dark",
        "sci-fi action virtual reality dystopia",
        "animation kids family toys adventure pixar",
        "animation kids family ocean adventure pixar",
        "romance drama love emotional",
        "romance drama love disaster emotional"
    ]
}

# Turn the dictionary into a pandas DataFrame (a table)
movies = pd.DataFrame(movies_data)

print(movies)  # See our table
```

**Run it:** `python3 movie_recommender.py`

You'll see a neat table of movies. Each movie has a list of `tags` describing it.

---

## Step 2: Turn Text into Numbers (TF-IDF)

Computers can't compare words directly — they need numbers. **TF-IDF** turns each movie's tags into a row of numbers (a "vector"). Words that are rare and meaningful get higher scores; common words get lower scores.

Add this to your file:

```python
from sklearn.feature_extraction.text import TfidfVectorizer

# Step 2: Convert the "tags" text into a matrix of numbers
# TfidfVectorizer learns all the unique words, then scores each movie
vectorizer = TfidfVectorizer()
tfidf_matrix = vectorizer.fit_transform(movies["tags"])

# tfidf_matrix is now a grid: one row per movie, one column per unique word
print("Shape of our number grid:", tfidf_matrix.shape)
# Example output: (8, 25) means 8 movies, 25 unique words
```

**In plain language:** We just translated every movie's description into a list of numbers so the computer can do math on them.

---

## Step 3: Measure Similarity (Cosine Similarity)

Now we compare every movie to every other movie. **Cosine similarity** gives a score from 0 (completely different) to 1 (identical).

```python
from sklearn.metrics.pairwise import cosine_similarity

# Step 3: Calculate how similar every movie is to every other movie
# This gives us an 8x8 grid of similarity scores
similarity_scores = cosine_similarity(tfidf_matrix)

print("Similarity grid shape:", similarity_scores.shape)  # (8, 8)
# similarity_scores[0][1] = how similar movie 0 is to movie 1
```

Movies like *Inception* and *Interstellar* (both sci-fi + Nolan) will have a high score. *Inception* and *The Notebook* will be near zero.

---

## Step 4: Build the Recommendation Function

This is the heart of the project — a function that takes a movie title and returns similar movies.

```python
# Step 4: A function that recommends movies similar to the one you give it
def recommend(movie_title, how_many=3):
    # Find the row number (index) of the movie you typed
    # We compare in lowercase so "inception" and "Inception" both work
    matches = movies[movies["title"].str.lower() == movie_title.lower()]

    if matches.empty:
        return f"Sorry, I don't have '{movie_title}' in my list."

    movie_index = matches.index[0]

    # Get this movie's similarity scores against all others
    scores = list(enumerate(similarity_scores[movie_index]))

    # Sort by score, highest first
    scores = sorted(scores, key=lambda x: x[1], reverse=True)

    # Skip the first one (it's the movie itself — always 100% similar to itself!)
    top_matches = scores[1:how_many + 1]

    # Build a friendly list of recommended titles
    recommendations = []
    for index, score in top_matches:
        recommendations.append(f"{movies.iloc[index]['title']} (match: {score:.0%})")

    return recommendations


# Step 4b: Try it out!
print("\nBecause you watched Inception, we recommend:")
for movie in recommend("Inception"):
    print(" -", movie)

print("\nBecause you watched Toy Story, we recommend:")
for movie in recommend("Toy Story"):
    print(" -", movie)
```

**Run it.** You should see something like:

```
Because you watched Inception, we recommend:
 - Interstellar (match: 47%)
 - The Dark Knight (match: 28%)
 - The Matrix (match: 24%)

Because you watched Toy Story, we recommend:
 - Finding Nemo (match: 71%)
 - ...
```

**That's a working content-based recommender!** 

---

## Step 5: Add Simple Collaborative Filtering

Content-based filtering only looks at the movie itself. **Collaborative filtering** looks at *people*: "Users who liked the same movies as you also liked X."

Here's the idea with a small ratings table. Add this to a **new file** called `collaborative.py`:

```python
# collaborative.py
# Recommend movies based on what similar users rated highly

import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity

# Step 1: A ratings table — rows are users, columns are movies, values are ratings (1-5)
# A 0 means "this user hasn't watched/rated this movie"
ratings_data = {
    "Inception":       [5, 4, 0, 5, 1],
    "Interstellar":    [5, 5, 0, 4, 0],
    "The Dark Knight": [4, 0, 0, 5, 2],
    "Toy Story":       [1, 0, 5, 0, 5],
    "Finding Nemo":    [0, 1, 5, 0, 4],
}
users = ["Aisha", "Ben", "Chen", "Diya", "Emma"]
ratings = pd.DataFrame(ratings_data, index=users)

print("Ratings table:")
print(ratings)

# Step 2: Find how similar each user is to every other user
user_similarity = cosine_similarity(ratings)
user_sim_df = pd.DataFrame(user_similarity, index=users, columns=users)

print("\nUser similarity:")
print(user_sim_df.round(2))


# Step 3: Recommend movies for a target user
def recommend_for_user(target_user, how_many=2):
    # Find the most similar OTHER user
    similar_users = user_sim_df[target_user].sort_values(ascending=False)
    similar_users = similar_users.drop(target_user)  # remove themselves
    closest_user = similar_users.index[0]

    print(f"\n{target_user} is most similar to {closest_user}")

    # Find movies the closest user rated highly that the target hasn't seen
    target_ratings = ratings.loc[target_user]
    closest_ratings = ratings.loc[closest_user]

    recommendations = []
    for movie in ratings.columns:
        # target hasn't seen it (rating 0) but the similar user loved it (rating >= 4)
        if target_ratings[movie] == 0 and closest_ratings[movie] >= 4:
            recommendations.append(movie)

    return recommendations[:how_many]


# Step 4: Try it!
recs = recommend_for_user("Aisha")
print(f"Recommended for Aisha: {recs}")
```

**Run it:** `python3 collaborative.py`

**In plain language:** We measured which users have similar taste, found Aisha's "taste twin," and recommended movies that twin loved but Aisha hasn't seen yet.

---

## How the Two Approaches Compare

| | Content-Based | Collaborative |
|---|---|---|
| **Looks at** | The movie's features (genre, tags) | What users rated |
| **Good for** | New users with no history | Discovering surprising picks |
| **Weakness** | Recommends only "more of the same" | Needs lots of user data ("cold start" problem) |
| **Real-world** | Used together (hybrid) in Netflix, Spotify | |

---

## Extensions & Improvements (Make It Impressive)

1. **Use the real MovieLens dataset.** Download it from [grouplens.org/datasets/movielens](https://grouplens.org/datasets/movielens/) (the "ml-latest-small" version). Then load it:
   ```python
   movies = pd.read_csv("movies.csv")   # has movieId, title, genres
   ratings = pd.read_csv("ratings.csv") # has userId, movieId, rating
   ```
   Replace the tiny dictionaries with these real CSVs (thousands of movies!).

2. **Combine genres + descriptions** into the `tags` column for richer content matching.

3. **Build a hybrid recommender** that blends both scores — this is what real systems do.

4. **Add a simple web interface** with Streamlit so people can type a movie and see recommendations in a browser (you'll learn Streamlit in Projects 4 and 5).

5. **Handle typos** — let the user type "incepton" and still find "Inception" using fuzzy matching (`pip install thefuzz`).

---

## How to Show This on Your Portfolio / GitHub

1. Create a GitHub repo called `movie-recommender`.
2. Include:
   - `movie_recommender.py` and `collaborative.py`
   - A `requirements.txt` file listing your libraries (`pandas`, `scikit-learn`, `numpy`)
   - A **`README.md`** that explains: what it does, how to run it, a screenshot of the output, and a 2-sentence explanation of cosine similarity.
3. **Portfolio tip:** In your README, add a section titled *"How it works"* and explain TF-IDF and cosine similarity in plain English. Interviewers love when you can explain the *why*, not just paste code.

> **One-line resume bullet:** *"Built content-based and collaborative-filtering movie recommendation engines in Python using pandas and scikit-learn (TF-IDF + cosine similarity)."*

---

## What's Next?

In **Project 2**, you'll train an actual neural network — a Convolutional Neural Network (CNN) — to look at images and tell cats from dogs. We'll run it on a free GPU using Google Colab. This is real deep learning!
