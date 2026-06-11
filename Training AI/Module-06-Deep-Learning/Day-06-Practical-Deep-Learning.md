# Module 6 — Day 6: Practical Deep Learning

## Think of it Like This...

You've learned to drive (Days 1–5). You can build networks, train them, do vision and sequences. But there's a difference between *knowing how to drive* and *being a confident driver who knows the shortcuts, avoids the potholes, and never runs out of gas.*

Today is about those real-world driving skills: using free GPUs the smart way, feeding data properly, **saving your work**, dodging the common mistakes that trip up every beginner, knowing when deep learning is even the right tool, and discovering Hugging Face — the place where you can grab world-class AI models for free.

This is the lesson that turns you from a *learner* into a *practitioner*. Let's go! 🛠️

---

## Tip 1: Getting the Most from Google Colab's Free GPU

A **GPU** (Graphics Processing Unit) is a chip that does thousands of math operations at once — perfect for neural networks. Training that takes *hours* on a regular CPU can take *minutes* on a GPU. And Colab gives you one for **free**!

### Turn on the GPU:
1. In Colab: menu → **Runtime → Change runtime type**
2. Set **Hardware accelerator** to **GPU** → Save

### Make sure your code actually uses it:
```python
import torch

# This line picks the GPU if available, otherwise the CPU
device = "cuda" if torch.cuda.is_available() else "cpu"
print("Using:", device)

# Move BOTH your model AND your data to that device:
model = model.to(device)
images, labels = images.to(device), labels.to(device)
```

> **Common beginner mistake:** Turning on the GPU but forgetting `.to(device)`. Then everything runs on the CPU anyway (slow!), OR you get a "tensors on different devices" error. **Rule: model AND data must be on the same device.**

### Colab free-tier reality check:
- Sessions **time out** after a while (and disconnect if you're idle) — so **save your model** before that happens (Tip 3!).
- Free GPU hours are limited per day. Don't waste them — test your code on a tiny subset first, *then* run the full training.

---

## Tip 2: Datasets and DataLoaders Done Right

You met these in Day 4. Let's make them stick. They're how you feed data to your network efficiently.

| Tool | Job | Analogy |
|------|-----|---------|
| **Dataset** | Holds your data and knows how to fetch one item | A library of books |
| **DataLoader** | Serves the data in batches, can shuffle it | A librarian handing you a stack at a time |

```python
from torch.utils.data import DataLoader

train_loader = DataLoader(
    train_data,
    batch_size=64,      # 64 examples per batch (balance: bigger=faster but more memory)
    shuffle=True        # shuffle each epoch so the model doesn't memorize the order
)
```

> **Why shuffle?** If you always feed data in the same order, the network can pick up on the *order* instead of the *content* — a sneaky form of overfitting. Shuffle the *training* data. (Don't bother shuffling test data — order doesn't affect evaluation.)

### Building a Dataset from your OWN data:
```python
from torch.utils.data import Dataset

class MyDataset(Dataset):
    def __init__(self, features, labels):
        self.features = features
        self.labels = labels

    def __len__(self):
        return len(self.features)          # how many items total

    def __getitem__(self, idx):
        return self.features[idx], self.labels[idx]   # fetch one item

# Now wrap it in a DataLoader and you're ready to train!
my_data = MyDataset(my_features, my_labels)
my_loader = DataLoader(my_data, batch_size=32, shuffle=True)
```

This simple pattern works for *any* data — your own photos, CSV files, text, anything!

---

## Tip 3: Saving and Loading Models (NEVER Lose Your Work!)

You trained a model for 30 minutes. The Colab session disconnects. **All gone** — unless you saved it. Don't let this happen to you!

### Save the trained model:
```python
# Save just the learned weights (the recommended way)
torch.save(model.state_dict(), "my_model.pth")
print("Model saved!")
```

> **What is `state_dict()`?** It's a dictionary of all your network's learned weights and biases — its entire "brain." Saving this saves everything the network learned.

### Load it back later:
```python
# Step 1: recreate the SAME model structure (the empty network)
model = DigitCNN()                  # must match the architecture you saved!

# Step 2: load the saved weights into it
model.load_state_dict(torch.load("my_model.pth"))
model.eval()                        # switch to evaluation mode (turns off dropout)
print("Model loaded and ready!")
```

### In Colab, save to Google Drive so it survives a disconnect:
```python
from google.colab import drive
drive.mount('/content/drive')                          # connect your Drive
torch.save(model.state_dict(), "/content/drive/MyDrive/my_model.pth")
```

> **Golden rule:** Save your model after training, *and* periodically *during* long training runs (checkpoints). Future-you will be grateful. 💾

---

## Tip 4: Common Pitfalls (and How to Dodge Them)

Every beginner hits these. Now you'll see them coming! 🚧

| Pitfall | What happens | The fix |
|---------|--------------|---------|
| **Forgot `optimizer.zero_grad()`** | Gradients pile up, training goes haywire | Always zero grads each loop (Day 3's 5 steps!) |
| **Learning rate too high** | Loss explodes to huge numbers or `NaN` | Lower it (try `0.001`) |
| **Learning rate too low** | Loss barely moves | Raise it (try `0.01`) |
| **Model & data on different devices** | "Expected all tensors on same device" error | `.to(device)` on both |
| **Forgot `model.eval()` when testing** | Dropout stays on, predictions wobble | Call `model.eval()` before testing, `model.train()` before training |
| **Data not normalized** | Training is slow or unstable | Scale inputs to a small range (e.g., 0–1) |
| **Overfitting** | Great on training, bad on new data | More data, dropout, or regularization (Day 3!) |
| **Testing on training data** | Fake-high accuracy that fools you | Always test on a *separate* held-out set |

> **The #1 lesson:** If loss becomes `NaN` (not-a-number) or explodes, your learning rate is almost always too high. Lower it first. This single tip will save you hours!

---

## Tip 5: When to Use Deep Learning vs. Classic Machine Learning

Deep learning is powerful, but it's **not always the right tool.** A common beginner mistake is reaching for a giant neural network when a simple model would work better, faster, and cheaper.

```
   Use CLASSIC ML            Use DEEP LEARNING
   (Module 5 stuff:          (this module:
   decision trees,           neural networks,
   linear regression)        CNNs, RNNs)
   ─────────────────         ─────────────────
   • Small data              • Lots of data
     (hundreds–thousands)      (tens of thousands+)
   • Tabular/spreadsheet     • Images, audio, text
     data                    • Complex patterns
   • Need to explain         • Raw, unstructured
     WHY (interpretable)       data
   • Limited compute         • Have a GPU
```

| Situation | Better choice | Why |
|-----------|---------------|-----|
| Predicting house prices from a spreadsheet | Classic ML (e.g., random forest) | Tabular data, fast, explainable |
| Recognizing objects in photos | Deep Learning (CNN) | Images need feature detection |
| Spam detection on 500 emails | Classic ML | Small data, simple patterns |
| Translating languages | Deep Learning (Transformer) | Complex sequences, tons of data |
| Forecasting sales from 12 monthly numbers | Classic ML | Tiny dataset — DL would overfit |

> **Rule of thumb:** Start simple. If a classic ML model solves your problem well, you're done — no need for a neural network. Reach for deep learning when the data is large, raw, and complex (images, audio, language). Don't use a rocket ship to cross the street!

---

## Tip 6: Hugging Face — The "App Store" of AI Models

Here's a game-changer for your AI journey: **Hugging Face**.

> **Hugging Face** = a free website hosting *hundreds of thousands* of pre-trained AI models that you can download and use in a few lines of code.

Remember transfer learning from Day 4 (borrowing a pre-trained brain)? Hugging Face is where you find those brains — for vision, text, audio, and more. Instead of training from scratch, you grab a model someone already trained on massive data.

### Use a powerful AI model in 3 lines:
```python
# First install the library (in Colab):  !pip install transformers

from transformers import pipeline

# Grab a ready-made sentiment-analysis model — already trained!
classifier = pipeline("sentiment-analysis")

result = classifier("I absolutely love learning about AI!")
print(result)
```

**Expected output:**
```
[{'label': 'POSITIVE', 'score': 0.9998}]
```

That's a state-of-the-art language model working in 3 lines — no training needed! 🤯

### What you'll find on Hugging Face:
- **Text models** — sentiment, translation, summarization, chatbots
- **Image models** — classification, object detection, image generation
- **Audio models** — speech-to-text, text-to-speech
- **Datasets** — thousands of ready-to-use datasets for practice

> Hugging Face is one of the most important tools in modern AI. Bookmark it (huggingface.co). You'll use it constantly — and the models there are mostly built on the **Transformers** you're about to learn in Module 7!

---

## Module 6 Recap: Look How Far You've Come!

Take a victory lap — here's everything you learned this module:

| Day | What you mastered |
|-----|-------------------|
| **Day 1** | How a neural network thinks — neurons, weights, layers, activations |
| **Day 2** | Building real networks in PyTorch with tensors and `nn.Module` |
| **Day 3** | Training — loss, backpropagation, optimizers, the training loop |
| **Day 4** | CNNs — teaching AI to *see* images (98%+ on digit recognition!) |
| **Day 5** | RNNs/LSTMs — teaching AI to *remember* sequences |
| **Day 6** | Practical skills — GPUs, saving models, pitfalls, Hugging Face |

You went from "what's a neuron?" to building computer-vision and sequence models, and using state-of-the-art AI in 3 lines. That's *genuinely* impressive. 🎉

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **GPU** | A chip that makes training fast (free in Colab!) |
| **DataLoader** | Serves data in shuffled batches for training |
| **`state_dict()`** | A model's saved "brain" (weights and biases) |
| **`model.eval()` / `model.train()`** | Switch between testing and training modes |
| **NaN loss** | A red flag — usually means learning rate too high |
| **Classic ML vs Deep Learning** | Simple/small/tabular vs large/raw/complex |
| **Hugging Face** | The "app store" of free pre-trained AI models |

---

## Practice Exercises

### Exercise 1: Save and Reload
Take any model you trained this module (the MNIST CNN or the sine-wave LSTM). Save its `state_dict()`, then in a fresh code cell, recreate the model and load the weights back. Confirm it still makes good predictions. (This is a skill you'll use forever!)

### Exercise 2: Try Hugging Face
Install `transformers` in Colab and run the sentiment-analysis pipeline on 5 sentences of your own — some positive, some negative, some neutral. Does it get them right? Where does it struggle?

### Exercise 3: Explore More Pipelines
Hugging Face's `pipeline` supports many tasks. Try `pipeline("summarization")` on a paragraph of text, or `pipeline("translation_en_to_fr")` on an English sentence. Pick one and report what it produced.

### Exercise 4: DL or Classic ML?
For each problem, decide deep learning or classic ML, and explain in one sentence why:
1. Predicting if a loan applicant will default, from 8 spreadsheet columns
2. Detecting tumors in 50,000 medical X-ray images
3. Recommending products from a customer's purchase history (small store, 300 customers)
4. Building a voice assistant that understands spoken commands

### Exercise 5: Debug the Pitfall
A friend's training loss suddenly jumps to `NaN` after a few steps. List the **first two things** you'd check based on Tip 4, and what you'd change.

### Exercise 6 (Capstone): Your Own Mini-Project
Pick a small dataset (Hugging Face Datasets, or MNIST/CIFAR from torchvision) and build an end-to-end project: load data with a DataLoader → build a network → train with the 5-step loop → test on held-out data → save the model. You now have ALL the skills to do this!

---

## What's Next? — Module 7: Transformers and LLMs! 🚀

You've built the foundation. Now comes the exciting part — the technology behind **Claude, ChatGPT, and the entire modern AI revolution**.

In **Module 7**, you'll learn:
- **Transformers** — the design that replaced RNNs and powers nearly all modern AI
- **Attention** — the brilliant mechanism that lets models "focus" on what matters (you got a taste in Day 5!)
- **Large Language Models (LLMs)** — how models like Claude are built, trained, and used
- **Tokens, embeddings, and prompts** — how AI actually reads and understands text
- Using and even fine-tuning these models with Hugging Face

Everything you learned in this module — neurons, training loops, backprop, attention, transfer learning, Hugging Face — comes together to explain how the most powerful AI in the world actually works.

---

You finished Module 6 — Deep Learning! That's a serious accomplishment. You now understand the machinery behind self-driving cars, medical AI, voice assistants, and the language models reshaping the world. And you can *build* these things yourself. Be proud — and get ready, because Module 7 is where it all clicks into the AI you talk to every day. See you there! 🎓✨
