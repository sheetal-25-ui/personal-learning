# Module 1 — Day 3: Types of Machine Learning

## The 3 Types of Machine Learning

Think of 3 different ways to learn cooking:

1. **Supervised Learning** = Learning with a recipe book (teacher gives answers)
2. **Unsupervised Learning** = Experimenting in kitchen (no recipe, find patterns yourself)
3. **Reinforcement Learning** = Learning by trial and error (taste and adjust)

---

## 1. Supervised Learning (Most Common — 80% of AI)

> **You give data WITH correct answers. AI learns the pattern.**

### How it Works
```
Training:
  Input: Photo of animal    →  Label: "Cat"
  Input: Photo of animal    →  Label: "Dog"
  Input: Photo of animal    →  Label: "Cat"
  ... (repeat 10,000 times)

After Training:
  Input: NEW photo of animal  →  AI predicts: "Cat" (95% confident)
```

### Real World Examples

| Problem | Input Data | Label (Answer) |
|---------|-----------|----------------|
| Spam filter | Email text | Spam / Not Spam |
| House price prediction | Size, location, rooms | Price (Rs. 50 lakhs) |
| Medical diagnosis | X-ray image | Disease / No Disease |
| Language translation | English sentence | Hindi sentence |
| Credit score | Income, age, history | Approve / Reject |

### Two Sub-Types

**Classification** = Predict a CATEGORY (yes/no, cat/dog, spam/not-spam)
- Output is a label from a fixed set
- Example: "Is this email spam?" → Yes or No

**Regression** = Predict a NUMBER (price, temperature, score)
- Output is a continuous number
- Example: "What will this house sell for?" → Rs. 52,00,000

---

## 2. Unsupervised Learning

> **You give data WITHOUT answers. AI finds hidden patterns on its own.**

### How it Works
```
Input: 10,000 customer purchase records (no labels!)
AI discovers: "There are 4 types of customers!"
  - Group 1: Budget shoppers (buy cheap, buy often)
  - Group 2: Premium buyers (buy expensive, buy rarely)
  - Group 3: Festival shoppers (only buy during sales)
  - Group 4: New customers (bought only once)
```

You never told AI these groups exist — it FOUND them!

### Real World Examples

| Problem | What AI Discovers |
|---------|-------------------|
| Customer segmentation | Groups of similar customers |
| Anomaly detection | Unusual transactions (fraud!) |
| Topic discovery | What topics people discuss on Twitter |
| Recommendation engine | "Customers who bought X also bought Y" |
| Gene analysis | Groups of related genes |

### Key Technique: Clustering
- AI groups similar things together
- Like sorting a mixed bag of candies by color — nobody told AI the colors, it figured it out

---

## 3. Reinforcement Learning (How Game AI and Robots Learn)

> **AI learns by trying things and getting rewards or punishments.**

### How it Works
```
AI plays a game:
  Action: Move left     → Result: Fell into a pit    → Reward: -10 (bad!)
  Action: Move right    → Result: Collected a coin    → Reward: +5 (good!)
  Action: Jump          → Result: Avoided enemy       → Reward: +10 (great!)

After 1,000,000 games:
  AI becomes a master player — it learned which actions give the best rewards!
```

### Real World Examples

| Problem | Action | Reward |
|---------|--------|--------|
| Self-driving car | Steer, brake, accelerate | Safe driving = reward, crash = punishment |
| Game AI (Chess, Go) | Move a piece | Win = reward, lose = punishment |
| Robot walking | Move leg joints | Walking forward = reward, falling = punishment |
| Ad placement | Show ad A or B | User clicks = reward, user ignores = punishment |
| ChatGPT/Claude | Generate response | Human likes it = reward, human dislikes = punishment |

### Fun Fact
Google's AlphaGo (reinforcement learning) beat the world champion at Go — a game with more possible positions than atoms in the universe!

---

## Quick Comparison Table

| | Supervised | Unsupervised | Reinforcement |
|---|---|---|---|
| **Data has answers?** | Yes | No | No (but gets feedback) |
| **Goal** | Predict answers | Find patterns | Maximize reward |
| **Like a student who...** | Studies with answer key | Groups notes by topic themselves | Learns by playing games |
| **Most used for** | Prediction, classification | Clustering, discovery | Games, robotics, AI chatbots |
| **% of real AI jobs** | ~80% | ~15% | ~5% |

---

## Which Type for Which Problem?

### Quick Decision Guide
```
Do you have labeled data (correct answers)?
├── YES → Supervised Learning
│   ├── Predicting a category? → Classification
│   └── Predicting a number? → Regression
│
└── NO
    ├── Want to find groups/patterns? → Unsupervised Learning
    └── Want AI to learn by trial/error? → Reinforcement Learning
```

---

## Practice Exercises

### Exercise 1: Classify the Type
For each problem, identify: Supervised, Unsupervised, or Reinforcement Learning?

1. Netflix recommending movies based on your watch history
2. A robot learning to walk
3. Detecting cancer from medical images (doctor-labeled)
4. Grouping news articles by topic (no labels given)
5. Teaching AI to play Ludo
6. Predicting tomorrow's stock price
7. Finding unusual patterns in bank transactions
8. A chatbot learning to give better responses from user feedback
9. Predicting which students might drop out
10. Automatically organizing your photos into albums

### Exercise 2: Design an AI System
Pick ONE of these problems:
- Predict IPL match winner
- Detect fake reviews on Amazon
- A music DJ that learns your taste

For your chosen problem, write:
1. What type of ML would you use? Why?
2. What data would you need?
3. What would the input and output be?
4. How would you know if the AI is working well?

### Exercise 3: Reinforcement Learning Game
Imagine you're training an AI to deliver food (like Zomato delivery):
1. What are the possible ACTIONS?
2. What gives POSITIVE rewards?
3. What gives NEGATIVE rewards? 
4. What would the AI learn after 100,000 deliveries?

---

## What's Next?

Tomorrow in **Day 04**, we'll cover:
- A brief history of AI (how we got here)
- The AI boom — why NOW?
- AI jobs and career paths
- Where AI is going in the next 5 years

You've completed 3 days! You now understand the fundamentals of AI better than most people. Keep it up!
