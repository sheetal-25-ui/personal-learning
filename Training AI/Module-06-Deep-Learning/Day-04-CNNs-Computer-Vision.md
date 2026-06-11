# Module 6 — Day 4: CNNs and Computer Vision

## Think of it Like This...

Look at a photo of a cat. How does your brain know it's a cat? You don't check every single pixel one by one. Instead, your eyes scan for **features**: pointy ears, whiskers, fur texture, two eyes. You spot these little patterns *anywhere* in the image, then your brain combines them: "ears + whiskers + fur = cat!"

A **Convolutional Neural Network (CNN)** does exactly this. It scans an image looking for small features (edges, then shapes, then objects), wherever they appear. CNNs are the AI behind face unlock, self-driving cars, medical scans, and photo apps.

Today you'll build one that recognizes handwritten digits — and watch it *see*. 👁️

---

## Why Regular Networks Struggle with Images

Yesterday's networks used `nn.Linear` layers. Why not just use those for images? Two big problems:

### Problem 1: Images are HUGE
A small color photo of 200×200 pixels has 200 × 200 × 3 (red, green, blue) = **120,000 numbers**! If the first layer had even 1,000 neurons, that's *120 million weights* in just one layer. Way too many. 😵

### Problem 2: Position shouldn't matter
A cat in the top-left corner is still a cat in the bottom-right corner. But a regular network treats every pixel position as completely separate — it would have to *re-learn* "cat ears" for every possible position. Wasteful!

```
   Regular network sees an image as one giant flat list:
   [0.2, 0.8, 0.1, 0.9, 0.3, ... 120,000 numbers ...]
   ↑ loses all the 2D structure! No idea which pixels are neighbors.
```

CNNs fix both problems. Here's how.

---

## The Big Idea: Convolution (Sliding a Filter)

> **Convolution** = sliding a small "filter" across the image to detect a feature wherever it appears.

A **filter** (also called a *kernel*) is a tiny grid of numbers — say 3×3. You slide it across the whole image, and at each spot it checks: **"Does the pattern I'm looking for appear here?"**

```
   Image (5x5)              Filter (3x3)
   slides across →          looks for vertical edges
   ┌─┬─┬─┬─┬─┐              ┌─┬─┬─┐
   │ │ │ │ │ │              │+│0│-│
   ├─┼─┼─┼─┼─┤              ├─┼─┼─┤
   │ │▓│▓│▓│ │   the 3x3    │+│0│-│
   ├─┼─┼─┼─┼─┤   filter     ├─┼─┼─┤
   │ │▓│▓│▓│ │   slides     │+│0│-│
   ├─┼─┼─┼─┼─┤   over each  └─┴─┴─┘
   │ │ │ │ │ │   3x3 patch
   └─┴─┴─┴─┴─┘
```

**Analogy:** Imagine a stencil shaped like a "vertical line." You slide it over a drawing. Wherever the drawing has a strong vertical line, the stencil "lights up." That's a filter detecting vertical edges!

### What do filters detect?
- **Early filters** detect simple things: horizontal edges, vertical edges, blobs of color
- **Later filters** combine those into shapes: corners, curves, circles
- **Even later filters** detect whole objects: an eye, a wheel, a cat face

```
   Layer 1 filters    Layer 2 filters       Layer 3 filters
   ────────────       ──────────────        ──────────────
   /  |  ─  \         circles, corners      eyes, ears, wheels
   (simple edges)     (combinations)        (full objects)
```

**The best part:** The network *learns* what filters to use during training (Day 3's loop!). You don't design the filters — backpropagation discovers them automatically. 🤯

---

## Pooling: Shrinking While Keeping What Matters

After convolution, images can still be big. **Pooling** shrinks them down while keeping the important info.

> **Max Pooling** = look at a small square (say 2×2) and keep only the biggest number, throwing away the rest.

```
   Before pooling (4x4)        After 2x2 max pooling (2x2)
   ┌──┬──┬──┬──┐               ┌────┬────┐
   │ 1│ 3│ 2│ 0│               │  6 │  4 │   ← kept the max of each
   ├──┼──┼──┼──┤               ├────┼────┤      2x2 block
   │ 6│ 2│ 4│ 1│               │  8 │  9 │
   ├──┼──┼──┼──┤               └────┴────┘
   │ 5│ 8│ 9│ 3│
   ├──┼──┼──┼──┤
   │ 2│ 1│ 0│ 7│
   └──┴──┴──┴──┘
```

**Analogy:** Pooling is like shrinking a high-resolution photo to a thumbnail. You lose fine detail, but you can still tell what it is — and now it's smaller and faster to process. It also makes the network care less about *exactly* where a feature is (more robust).

---

## The CNN Recipe

A typical CNN stacks these blocks, then finishes with regular layers:

```
   IMAGE
     │
   [Convolution] → find features (edges, shapes)
     │
   [ReLU]        → activation (Day 1!)
     │
   [Pooling]     → shrink it down
     │
   ... repeat the Conv→ReLU→Pool block a few times ...
     │
   [Flatten]     → turn the 2D grid into a flat list
     │
   [Linear]      → regular layers make the final decision
     │
   ANSWER ("this is a 7!")
```

The convolution+pooling blocks are the "eyes" that find features. The Linear layers at the end are the "brain" that makes the final call.

---

## Full Runnable Example: Recognize Handwritten Digits (MNIST)

**MNIST** is the "hello world" of computer vision — 70,000 tiny (28×28) grayscale images of handwritten digits 0–9. Let's build a CNN to recognize them!

> **Run this in Google Colab with the GPU on** (Runtime → Change runtime type → GPU). It makes training much faster!

```python
import torch
import torch.nn as nn
import torchvision
import torchvision.transforms as transforms

# -------------------------------------------------------------
# STEP 1: Load the MNIST dataset (PyTorch downloads it for you!)
# -------------------------------------------------------------
transform = transforms.ToTensor()   # convert images to tensors (0–1 numbers)

train_data = torchvision.datasets.MNIST(
    root="./data", train=True, download=True, transform=transform)
test_data = torchvision.datasets.MNIST(
    root="./data", train=False, download=True, transform=transform)

# DataLoaders feed the data in batches (Day 3!)
train_loader = torch.utils.data.DataLoader(train_data, batch_size=64, shuffle=True)
test_loader = torch.utils.data.DataLoader(test_data, batch_size=64)

print("Training images:", len(train_data))   # 60,000
print("Test images:", len(test_data))        # 10,000

# -------------------------------------------------------------
# STEP 2: Build the CNN
# -------------------------------------------------------------
class DigitCNN(nn.Module):
    def __init__(self):
        super().__init__()
        # Conv layer: 1 input channel (grayscale) → 16 filters, 3x3 each
        self.conv1 = nn.Conv2d(in_channels=1, out_channels=16, kernel_size=3, padding=1)
        # Second conv layer: 16 → 32 filters
        self.conv2 = nn.Conv2d(16, 32, kernel_size=3, padding=1)
        self.relu = nn.ReLU()
        self.pool = nn.MaxPool2d(2, 2)   # 2x2 pooling, halves the size each time
        # After two poolings, 28x28 → 14x14 → 7x7. We have 32 filters.
        # So flattened size = 32 * 7 * 7
        self.fc = nn.Linear(32 * 7 * 7, 10)   # final layer → 10 digits (0–9)

    def forward(self, x):
        x = self.pool(self.relu(self.conv1(x)))   # conv → relu → pool
        x = self.pool(self.relu(self.conv2(x)))   # conv → relu → pool
        x = x.view(x.size(0), -1)                 # flatten the 2D grid to a list
        x = self.fc(x)                            # final decision
        return x

# Use the GPU if available (much faster!)
device = "cuda" if torch.cuda.is_available() else "cpu"
model = DigitCNN().to(device)
print("Training on:", device)

# -------------------------------------------------------------
# STEP 3: Loss function and optimizer (Day 3!)
# -------------------------------------------------------------
loss_fn = nn.CrossEntropyLoss()   # for picking 1 of many categories
optimizer = torch.optim.Adam(model.parameters(), lr=0.001)

# -------------------------------------------------------------
# STEP 4: The training loop (same 5 steps as Day 3!)
# -------------------------------------------------------------
epochs = 3   # 3 passes is enough to get great accuracy on MNIST
for epoch in range(epochs):
    for images, labels in train_loader:
        images, labels = images.to(device), labels.to(device)

        predictions = model(images)            # 1. FORWARD
        loss = loss_fn(predictions, labels)    # 2. LOSS
        optimizer.zero_grad()                  # 3. ZERO
        loss.backward()                        # 4. BACKWARD (backprop!)
        optimizer.step()                       # 5. STEP

    print(f"Epoch {epoch+1}/{epochs} done. Last batch loss: {loss.item():.4f}")

# -------------------------------------------------------------
# STEP 5: Test it — how accurate is it on UNSEEN images?
# -------------------------------------------------------------
correct = 0
total = 0
with torch.no_grad():   # no training here, just checking — saves memory
    for images, labels in test_loader:
        images, labels = images.to(device), labels.to(device)
        outputs = model(images)
        predicted = outputs.argmax(1)   # pick the digit with highest score
        correct += (predicted == labels).sum().item()
        total += labels.size(0)

print(f"Accuracy on 10,000 test images: {100 * correct / total:.2f}%")
```

**Expected output (your numbers will vary slightly):**
```
Training images: 60000
Test images: 10000
Training on: cuda
Epoch 1/3 done. Last batch loss: 0.0843
Epoch 2/3 done. Last batch loss: 0.0521
Epoch 3/3 done. Last batch loss: 0.0237
Accuracy on 10,000 test images: 98.71%
```

🎉 **Over 98% accurate** on digits it has *never seen before* — after just 3 epochs! Notice we tested on the separate `test_data` (images held back from training), which proves the network truly *learned* and didn't just memorize (no overfitting!).

---

## Transfer Learning — Borrowing a Pre-Trained Brain

Training a big CNN from scratch needs LOTS of images and time. But there's a brilliant shortcut: **transfer learning**.

> **Transfer learning** = take a network someone *already trained* on millions of images, and reuse its knowledge for *your* task.

**Analogy:** Instead of teaching a brand-new student from birth, you hire a graduate who already knows a lot about the world, and just teach them the *specific* job you need. Way faster!

Big companies trained huge CNNs (like **ResNet** and **VGG**) on millions of images. Those networks already know how to detect edges, textures, and shapes. You just swap out their final layer and train *only that part* on your own (much smaller) dataset.

```python
import torchvision.models as models

# Load ResNet18, already trained on millions of images!
pretrained = models.resnet18(weights="DEFAULT")

# Freeze all its learned weights so we don't undo its knowledge
for param in pretrained.parameters():
    param.requires_grad = False

# Replace ONLY the final layer to match OUR number of classes (say, 5)
pretrained.fc = nn.Linear(pretrained.fc.in_features, 5)

# Now train ONLY the new final layer on your own small dataset!
# (Same training loop as before, but it learns fast since the
#  hard work — edge & shape detection — is already done.)
```

> **Why this is huge:** Transfer learning lets you build a great image classifier with just a few hundred photos and a few minutes of training — instead of millions of photos and days of training. It's how most real-world computer vision projects are built today.

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **CNN** | A network designed to "see" images |
| **Convolution** | Sliding a small filter to detect features |
| **Filter / Kernel** | A small grid that detects one pattern (edge, shape) |
| **Pooling** | Shrinking the image while keeping key info |
| **Flatten** | Turning the 2D grid into a flat list for the final layers |
| **MNIST** | The "hello world" digit-recognition dataset |
| **Transfer learning** | Reusing a pre-trained network for your own task |

---

## Practice Exercises

### Exercise 1: Run the Digit Classifier
Run the full MNIST example in Colab (turn on the GPU!). Report:
1. The final accuracy.
2. How long training took (much faster with GPU vs CPU — try both!).

### Exercise 2: Make It Bigger or Smaller
Change the number of filters (e.g., `out_channels=8` instead of 16, or `64`). Retrain. Does accuracy change? Does it train faster or slower with fewer filters?

### Exercise 3: Train Longer
Change `epochs = 3` to `epochs = 6`. Does accuracy improve? At some point it stops improving much — can you see where the "diminishing returns" kick in?

### Exercise 4: See What It Got Wrong
After training, write a small loop that finds images the model predicted incorrectly. Display a few with `matplotlib`. Are they hard even for *you* to read? (Sometimes the AI's mistakes are genuinely ambiguous handwriting!)

### Exercise 5 (Think): When to Use Transfer Learning
You want to build an app that tells apart 3 dog breeds, but you only have 200 photos total. Would you train a CNN from scratch or use transfer learning? Why?

---

## What's Next?

Tomorrow in **Day 05**, we move from images to **sequences** — data where *order matters*, like text and time series. You'll learn:
- Why sentences and stock prices need a different kind of network
- **RNNs** (Recurrent Neural Networks) — networks with a "memory"
- The **vanishing gradient** problem (why early RNNs forgot things)
- **LSTM** and **GRU** — smarter memory cells that remember longer
- A simple sequence prediction example
- And a sneak peek at **Transformers**, which replaced RNNs for most modern NLP (the topic of Module 7!)

Your network learned to *see* today. Tomorrow it learns to *remember*. 🧠

---

You built a real computer-vision AI today — one that recognizes handwriting better than many humans can read it! CNNs power face unlock, medical imaging, and self-driving cars, and now you understand exactly how they work. That's a genuinely impressive milestone. See you in Day 5! 📸
