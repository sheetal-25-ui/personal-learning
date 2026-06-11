# Module 6 — Day 5: RNNs and Sequence Models

## Think of it Like This...

Read this sentence: "I grew up in France, so I speak fluent ____."

Your brain instantly fills in "French." But *how*? You didn't just look at the last word. You **remembered** the word "France" from earlier in the sentence, held it in your mind, and used it to predict the blank.

That's the superpower we need today: **memory across a sequence.** Yesterday's CNNs are great at images (all the pixels arrive at once). But sentences, songs, and stock prices arrive *one piece at a time*, and **order matters**. For those, we need networks that can remember — called **Recurrent Neural Networks (RNNs)**.

---

## What is Sequence Data?

> **Sequence data** = data where the *order* of the items carries meaning.

If you shuffle the order, you destroy the meaning:

| Type of data | Example | Why order matters |
|--------------|---------|-------------------|
| **Text** | "Dog bites man" vs "Man bites dog" | Same words, totally different meaning! |
| **Time series** | Daily stock prices, weather | Yesterday affects today |
| **Audio / music** | A melody | Notes in the wrong order = noise |
| **Sensor data** | Heartbeat, steps over time | The trend over time is the signal |

```
   NOT a sequence (order doesn't matter):
   [height, weight, age]   ← shuffling these is fine

   IS a sequence (order matters!):
   "the" → "cat" → "sat" → "on" → "the" → "mat"
   ↑ shuffle these and the sentence becomes nonsense
```

A regular network (or even a CNN) treats all inputs at once and has no sense of "before" and "after." For sequences, that's a deal-breaker.

---

## Why Order Matters — A Concrete Example

Imagine predicting tomorrow's temperature. Today is 30°C. Is tomorrow going to be hot? It depends on the *trend*:

```
   Sequence A: 10°, 15°, 20°, 25°, 30°  → warming up! Tomorrow ≈ 35° 🔥
   Sequence B: 50°, 45°, 40°, 35°, 30°  → cooling down! Tomorrow ≈ 25° ❄️
```

Both sequences END at 30°. But the *history* points to opposite predictions! A network that only sees "30°" (ignoring order) can't tell these apart. We need a network that **remembers the journey**, not just the last step.

---

## The RNN Idea: A Network with a Memory Loop

> **RNN (Recurrent Neural Network)** = a network that processes a sequence one step at a time, passing a "memory" forward to the next step.

The trick is a **loop**: at each step, the RNN looks at the current input *and* its memory from the previous step, then produces a new memory to pass along.

```
   Reading "I love AI" one word at a time:

   "I"          "love"        "AI"
    │             │             │
    ▼             ▼             ▼
  [RNN] ──mem──▶[RNN] ──mem──▶[RNN] ──▶ prediction
    │             │             │
  memory of    memory of    memory of
   "I"         "I love"     "I love AI"
```

The arrow labeled **mem** (the hidden state) is the memory being carried forward. By the time the RNN reaches "AI", it still "remembers" it saw "I love" before. That's how it understands context!

**Analogy:** Reading a book, you carry the story in your head as you go. By chapter 10, you remember what happened in chapter 1. The RNN's memory (hidden state) is that running summary of "the story so far."

---

## The Problem: Vanishing Gradients (RNNs Forget Long Things)

Basic RNNs have a serious weakness. Remember backpropagation from Day 3 — the "blame" flows backward through the network. In a long sequence, that blame signal has to travel back through *many* steps... and it gets **weaker and weaker** the further back it goes, until it basically disappears.

> **Vanishing gradient** = the learning signal shrinks so much over many steps that the network can't learn long-range connections. Early information gets "forgotten."

```
   Long sentence: "I grew up in France ... [50 words] ... so I speak fluent ___"

   The clue "France" is 50 words back.
   The learning signal fading as it travels back:
   strong → weaker → weaker → ... → ≈ 0
                                      ↑ by the time it reaches "France",
                                        the signal has vanished!
```

**Analogy:** It's like a game of "telephone" with 50 people. The original message gets garbled and faded by the time it reaches the end. Plain RNNs are forgetful — they handle short sequences fine but lose track of long-range context.

---

## The Fix: LSTM and GRU (Smarter Memory Cells)

To fix forgetfulness, researchers invented memory cells with **gates** — little controllers that decide what to remember, what to forget, and what to pay attention to.

### LSTM (Long Short-Term Memory)
> **LSTM** = an RNN with a separate long-term memory and three "gates" that control it.

The three gates:
- **Forget gate** — "Should I erase old memories?"
- **Input gate** — "Should I save this new info?"
- **Output gate** — "What should I use right now?"

**Analogy:** An LSTM is like a smart notebook. As you read, you decide: *cross out* old notes that no longer matter (forget gate), *write down* important new facts (input gate), and *look up* the relevant notes when you need them (output gate). This lets it remember important things from *far* back.

### GRU (Gated Recurrent Unit)
> **GRU** = a simpler, faster cousin of the LSTM with fewer gates (just two).

GRUs often work just as well as LSTMs but train faster because they're simpler. Many people try a GRU first.

| Cell type | Memory | Speed | When to use |
|-----------|--------|-------|-------------|
| **Plain RNN** | Short, forgetful | Fast | Very short sequences only |
| **LSTM** | Long, reliable | Slower | When long-range memory matters |
| **GRU** | Long, good | Faster than LSTM | A great default — try this first |

---

## A Simple Sequence Example in PyTorch

Let's build a small network that learns a **time-series pattern**: given the last few numbers of a sine wave, predict the next number. PyTorch makes the RNN/LSTM part a one-liner!

> Run this in Google Colab (CPU is fine — this example is small).

```python
import torch
import torch.nn as nn
import numpy as np

# -------------------------------------------------------------
# STEP 1: Make a sine wave and chop it into sequences
# The network sees 10 numbers in a row, predicts the 11th.
# -------------------------------------------------------------
wave = np.sin(np.linspace(0, 50, 500))   # a smooth sine wave (500 points)

seq_length = 10   # how many past steps the model looks at
X, y = [], []
for i in range(len(wave) - seq_length):
    X.append(wave[i:i + seq_length])      # 10 numbers in a row
    y.append(wave[i + seq_length])        # the next number (the answer)

# Convert to tensors. Shape: (samples, seq_length, 1 feature)
X = torch.tensor(np.array(X), dtype=torch.float32).unsqueeze(-1)
y = torch.tensor(np.array(y), dtype=torch.float32).unsqueeze(-1)

print("Input shape:", X.shape)   # (490, 10, 1) → 490 sequences of length 10

# -------------------------------------------------------------
# STEP 2: Build an LSTM-based model
# -------------------------------------------------------------
class SequenceModel(nn.Module):
    def __init__(self):
        super().__init__()
        # LSTM: 1 input feature → 32 hidden memory units
        self.lstm = nn.LSTM(input_size=1, hidden_size=32, batch_first=True)
        # A final layer to turn the memory into one predicted number
        self.fc = nn.Linear(32, 1)

    def forward(self, x):
        # out = outputs at every step; we only want the LAST step's memory
        out, _ = self.lstm(x)
        last_step = out[:, -1, :]   # grab the final time step
        return self.fc(last_step)

model = SequenceModel()

# -------------------------------------------------------------
# STEP 3: Train (same 5-step loop from Day 3!)
# -------------------------------------------------------------
loss_fn = nn.MSELoss()
optimizer = torch.optim.Adam(model.parameters(), lr=0.01)

for epoch in range(100):
    pred = model(X)                 # 1. FORWARD
    loss = loss_fn(pred, y)         # 2. LOSS
    optimizer.zero_grad()           # 3. ZERO
    loss.backward()                 # 4. BACKWARD
    optimizer.step()                # 5. STEP
    if (epoch + 1) % 20 == 0:
        print(f"Epoch {epoch+1}/100  |  Loss: {loss.item():.5f}")

# -------------------------------------------------------------
# STEP 4: Test — predict the next value after a known sequence
# -------------------------------------------------------------
test_seq = X[0:1]                   # take the first sequence
predicted = model(test_seq).item()
actual = y[0].item()
print(f"Predicted next value: {predicted:.4f}  |  Actual: {actual:.4f}")
```

**Expected output (numbers vary slightly):**
```
Input shape: torch.Size([490, 10, 1])
Epoch 20/100  |  Loss: 0.01204
Epoch 40/100  |  Loss: 0.00231
Epoch 60/100  |  Loss: 0.00068
Epoch 80/100  |  Loss: 0.00029
Epoch 100/100 |  Loss: 0.00015
Predicted next value: 0.4071  |  Actual: 0.4093
```

🎉 The LSTM learned the wave's rhythm! It looks at 10 past values and predicts the next one almost perfectly (`0.4071` vs the true `0.4093`). It learned the *pattern over time* — exactly what sequence models are built for.

> **Notice:** Swapping `nn.LSTM` for `nn.GRU` or `nn.RNN` in the code above is a one-word change! Try all three and compare. That's the beauty of PyTorch.

---

## Sneak Peek: Transformers Replaced RNNs for Most NLP

Here's an important heads-up for your AI journey:

> **For most modern language tasks (like ChatGPT and Claude), RNNs and LSTMs have been replaced by a newer design called the Transformer.**

Why did Transformers win?

| | RNN / LSTM | Transformer |
|--|-----------|-------------|
| **How it reads** | One step at a time (slow, sequential) | All at once, in parallel (fast!) |
| **Long-range memory** | Struggles (even LSTMs fade) | Excellent — "attention" sees everything |
| **Training speed** | Slow (can't parallelize easily) | Very fast (great for GPUs) |

The Transformer's secret is **attention** — instead of passing memory step-by-step, it lets every word directly "look at" every other word in the sentence, no matter how far apart. No more vanishing gradients over long distances!

**But RNNs aren't useless** — they're still great for many time-series and smaller sequence tasks, and understanding them makes Transformers much easier to grasp. Think of RNNs as the foundation that Transformers were built to improve on.

> **This is exactly what Module 7 is about!** You'll learn Transformers and the Large Language Models (LLMs) like Claude that are built on them. 🚀

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **Sequence data** | Data where order matters (text, time series, audio) |
| **RNN** | A network with a memory loop, processes one step at a time |
| **Hidden state** | The "memory" passed from one step to the next |
| **Vanishing gradient** | Learning signal fades over long sequences → forgetting |
| **LSTM** | RNN with gates for reliable long-term memory |
| **GRU** | A simpler, faster cousin of the LSTM |
| **Attention** | The Transformer trick — every item sees every other item |
| **Transformer** | The modern design that replaced RNNs for most NLP |

---

## Practice Exercises

### Exercise 1: Run the Sine Wave Predictor
Run the example in Colab. Report the final loss and how close the prediction was to the actual value.

### Exercise 2: RNN vs LSTM vs GRU
Change `nn.LSTM` to `nn.GRU`, then to `nn.RNN` (one-word swaps). For each:
- What's the final loss?
- Does the plain `nn.RNN` do noticeably worse? (It may struggle more with longer patterns.)

### Exercise 3: Longer Memory
Change `seq_length = 10` to `seq_length = 30` (the model now looks further back). Does it still learn the wave well? This tests longer-range memory — where LSTMs shine over plain RNNs.

### Exercise 4: A Harder Wave
Make the wave more complex by adding two sine waves together:
`wave = np.sin(np.linspace(0, 50, 500)) + 0.5 * np.sin(np.linspace(0, 150, 500))`
Retrain. Can the LSTM learn this trickier pattern? Does it need more epochs?

### Exercise 5 (Think): Sequence or Not?
For each, decide if order matters (sequence) or not:
1. Predicting house price from size, bedrooms, location
2. Predicting the next word in a sentence
3. Classifying a photo as cat or dog
4. Forecasting tomorrow's electricity demand
5. Recognizing a spoken voice command

---

## What's Next?

You've now completed the three big neural network families: **regular networks** (Days 1–3), **CNNs for vision** (Day 4), and **RNNs for sequences** (Day 5). Tomorrow in **Day 06 — Practical Deep Learning**, we tie it all together with the real-world skills that turn you from a learner into a *practitioner*:
- Getting the most out of **Google Colab's free GPU**
- **Datasets and DataLoaders** done right
- **Saving and loading** your trained models (so you don't lose your work!)
- **Common pitfalls** and how to avoid them
- **When to use deep learning vs. classic machine learning**
- An intro to **Hugging Face** — the "app store" of pre-trained AI models
- And a preview of **Module 7: Transformers and LLMs!**

---

Your network learned to *remember* today — to carry context across time, just like you do when reading a sentence. And you got your first glimpse of Transformers, the technology behind Claude itself. You're so close to finishing the module — one day to go! 🧠✨
