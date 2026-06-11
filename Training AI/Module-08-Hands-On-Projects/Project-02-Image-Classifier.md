# Module 8 — Project 2: Image Classifier (Deep Learning)

## What We're Building (and Why It's Resume-Worthy)

You'll build a **neural network that looks at a picture and tells you what's in it.** Specifically, we'll classify images from CIFAR-10 (a famous dataset of 10 object types: airplanes, cars, birds, cats, dogs, etc.). Then you'll learn how to do the same with **transfer learning** — borrowing a pre-trained "expert" network for faster, better results.

**Why it's resume-worthy:** This is *actual deep learning* — Convolutional Neural Networks (CNNs) are the technology behind self-driving cars, medical imaging, and face unlock. Saying "I trained a CNN in PyTorch and used transfer learning" is a serious, hireable skill. Most beginners never get a real network training; you will.

---

## What You'll Learn

| Concept | What It Means |
|---------|---------------|
| **PyTorch** | The most popular deep-learning library (used by Meta, Tesla, OpenAI) |
| **CNN** | A neural network designed to "see" images by detecting edges, shapes, patterns |
| **Training** | Showing the network thousands of labeled images so it learns |
| **GPU** | A super-fast chip that makes training 10-50x faster (Colab gives you one free!) |
| **Transfer learning** | Reusing a network someone already trained on millions of images |

---

## Prerequisites & Tools

- A **Google account** (for Google Colab — that's it!)
- No installation needed. **Colab runs in your browser and gives you a free GPU.**

> **Why Colab?** Training neural networks needs a GPU. Your laptop probably doesn't have a fast one, and it would take hours. Colab gives you a free GPU in the cloud. This is how real ML engineers prototype.

---

## Step 1: Open Google Colab and Turn On the GPU

1. Go to [colab.research.google.com](https://colab.research.google.com)
2. Click **New Notebook**.
3. **Turn on the free GPU:** Click menu **Runtime → Change runtime type → Hardware accelerator → GPU → Save**. This is the most important step!
4. Verify the GPU is on. In the first cell, paste and run (press **Shift+Enter** to run a cell):

```python
import torch

# Check if we have a GPU available
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"Using device: {device}")
# You want this to say "cuda" — that means the GPU is active!
```

If it says `cuda`, you're good. If it says `cpu`, go back to step 3.

---

## Step 2: Load the CIFAR-10 Dataset

CIFAR-10 has 60,000 tiny color images (32x32 pixels) across 10 categories. PyTorch can download it for us automatically.

In a new cell:

```python
import torch
import torchvision
import torchvision.transforms as transforms

# Step 2a: Define how to prepare each image
# - ToTensor: turns the image into numbers PyTorch understands
# - Normalize: scales pixel values to make training more stable
transform = transforms.Compose([
    transforms.ToTensor(),
    transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))
])

# Step 2b: Download the training and test sets
train_set = torchvision.datasets.CIFAR10(
    root="./data", train=True, download=True, transform=transform
)
test_set = torchvision.datasets.CIFAR10(
    root="./data", train=False, download=True, transform=transform
)

# Step 2c: Loaders feed images to the network in batches of 32
# (Training on batches is faster than one image at a time)
train_loader = torch.utils.data.DataLoader(train_set, batch_size=32, shuffle=True)
test_loader = torch.utils.data.DataLoader(test_set, batch_size=32, shuffle=False)

# The 10 categories, in order
classes = ["airplane", "car", "bird", "cat", "deer",
           "dog", "frog", "horse", "ship", "truck"]

print(f"Training images: {len(train_set)}")  # 50000
print(f"Test images: {len(test_set)}")        # 10000
```

---

## Step 3: Peek at the Images

Always look at your data! Add a cell:

```python
import matplotlib.pyplot as plt
import numpy as np

# Grab one batch of images
images, labels = next(iter(train_loader))

# Show the first 6 images with their labels
fig, axes = plt.subplots(1, 6, figsize=(12, 2))
for i in range(6):
    # Undo the normalization so colors look right
    img = images[i] / 2 + 0.5
    axes[i].imshow(np.transpose(img.numpy(), (1, 2, 0)))
    axes[i].set_title(classes[labels[i]])
    axes[i].axis("off")
plt.show()
```

You'll see 6 small images, each labeled. Cool — that's what the network will learn from!

---

## Step 4: Build the CNN

This is the network. Don't worry about every detail — the comments explain what each layer does.

```python
import torch.nn as nn
import torch.nn.functional as F

# Step 4: Define our Convolutional Neural Network
class SimpleCNN(nn.Module):
    def __init__(self):
        super().__init__()
        # Convolution layers DETECT FEATURES (edges, textures, shapes)
        # conv1: takes 3 color channels in, produces 32 feature maps
        self.conv1 = nn.Conv2d(3, 32, kernel_size=3, padding=1)
        self.conv2 = nn.Conv2d(32, 64, kernel_size=3, padding=1)

        # Pooling SHRINKS the image, keeping the important parts
        self.pool = nn.MaxPool2d(2, 2)

        # Fully-connected layers make the final DECISION
        # After two poolings, a 32x32 image becomes 8x8 with 64 channels
        self.fc1 = nn.Linear(64 * 8 * 8, 128)
        self.fc2 = nn.Linear(128, 10)  # 10 outputs = 10 categories

        self.dropout = nn.Dropout(0.25)  # randomly "turns off" neurons to prevent overfitting

    def forward(self, x):
        # The path an image takes through the network:
        x = self.pool(F.relu(self.conv1(x)))   # detect features, then shrink
        x = self.pool(F.relu(self.conv2(x)))   # detect more features, then shrink
        x = torch.flatten(x, 1)                # flatten into a single long list
        x = F.relu(self.fc1(x))
        x = self.dropout(x)
        x = self.fc2(x)                        # final scores for each category
        return x


# Create the model and move it to the GPU
model = SimpleCNN().to(device)
print(model)
```

**In plain language:** The convolution layers act like a stack of magnifying glasses that learn to spot edges, then shapes, then whole objects. The final layers vote on which of the 10 categories it is.

---

## Step 5: Train the Network

Training = showing the network images, letting it guess, telling it how wrong it was, and nudging it to do better. We repeat this many times (each full pass is an "epoch").

```python
import torch.optim as optim

# Step 5a: The "loss function" measures how wrong the network's guesses are
criterion = nn.CrossEntropyLoss()

# Step 5b: The "optimizer" adjusts the network to reduce that wrongness
# lr=0.001 is the learning rate — how big each adjustment is.
# 0.001 is a safe, common starting value for the Adam optimizer.
optimizer = optim.Adam(model.parameters(), lr=0.001)

# Step 5c: Train for 5 epochs (5 full passes through the data)
# More epochs = better accuracy but longer training. 5 is a good start.
EPOCHS = 5

for epoch in range(EPOCHS):
    running_loss = 0.0
    for images, labels in train_loader:
        # Move data to the GPU
        images, labels = images.to(device), labels.to(device)

        optimizer.zero_grad()           # reset previous adjustments
        outputs = model(images)         # network makes guesses
        loss = criterion(outputs, labels)  # measure how wrong
        loss.backward()                 # figure out how to improve
        optimizer.step()                # apply the improvement

        running_loss += loss.item()

    avg_loss = running_loss / len(train_loader)
    print(f"Epoch {epoch + 1}/{EPOCHS} — average loss: {avg_loss:.3f}")

print("Finished training!")
```

Watch the loss number go **down** each epoch — that means it's learning! On a GPU this takes about 1-2 minutes. (On CPU it would take 30+ minutes — now you see why we use the GPU.)

---

## Step 6: Test How Accurate It Is

```python
# Step 6: Check accuracy on images the network has NEVER seen
correct = 0
total = 0

model.eval()  # put model in "evaluation mode"
with torch.no_grad():  # we're not training, so don't track gradients (faster)
    for images, labels in test_loader:
        images, labels = images.to(device), labels.to(device)
        outputs = model(images)
        _, predicted = torch.max(outputs, 1)  # pick the highest-scoring category
        total += labels.size(0)
        correct += (predicted == labels).sum().item()

accuracy = 100 * correct / total
print(f"Accuracy on 10,000 test images: {accuracy:.1f}%")
```

A simple CNN like this usually reaches **65-75%** after 5 epochs. (Random guessing would be 10%, so this is genuinely learning!)

---

## Step 7 (Bonus): Transfer Learning — The Pro Move

Instead of training from scratch, we borrow **ResNet18** — a network already trained on millions of images. We just teach it our 10 categories. This is faster and far more accurate.

```python
import torchvision.models as models

# Step 7a: Load ResNet18 with its pre-trained knowledge
pretrained = models.resnet18(weights=models.ResNet18_Weights.DEFAULT)

# Step 7b: Freeze the existing layers (keep their learned knowledge)
for param in pretrained.parameters():
    param.requires_grad = False

# Step 7c: Replace the final layer with one for OUR 10 categories
num_features = pretrained.fc.in_features
pretrained.fc = nn.Linear(num_features, 10)
pretrained = pretrained.to(device)

# Now only the new final layer trains — much faster!
# (Train it using the same Step 5 loop, swapping `model` for `pretrained`
#  and `optimizer` to optimize pretrained.fc.parameters())
optimizer = optim.Adam(pretrained.fc.parameters(), lr=0.001)
print("ResNet18 ready for transfer learning!")
```

> **Note:** ResNet expects larger images, so for best results add `transforms.Resize(224)` to your transform in Step 2. Transfer learning typically pushes accuracy to **85%+**.

---

## CNN vs Transfer Learning

| | Train Your Own CNN | Transfer Learning |
|---|---|---|
| **Speed** | Slower, needs more data | Fast, needs less data |
| **Accuracy** | Good (65-75%) | Great (85%+) |
| **When to use** | Learning, custom problems | Almost always in real projects |

---

## Extensions & Improvements

1. **Cats vs Dogs:** Download the Kaggle "Dogs vs Cats" dataset and adapt this to a 2-category classifier. (More relatable than CIFAR-10!)
2. **Save your trained model:** `torch.save(model.state_dict(), "model.pth")` so you don't retrain every time.
3. **Upload your own photo** and have the model classify it — this is a great demo!
4. **Plot a confusion matrix** to see which categories the model confuses (cats vs dogs is a classic mix-up).
5. **Train longer** (10-20 epochs) and add data augmentation (`transforms.RandomHorizontalFlip()`) to boost accuracy.

---

## How to Show This on Your Portfolio / GitHub

1. Click **File → Download → Download .ipynb** in Colab to save your notebook.
2. Create a GitHub repo `image-classifier`, upload the `.ipynb`.
   - **GitHub renders Colab notebooks beautifully** — your graphs, images, and outputs show up automatically. This makes for an impressive, visual portfolio piece.
3. In your `README.md`, include: a screenshot of sample classified images, your final accuracy number, and a sentence explaining what a CNN does.
4. Add an **"Open in Colab" badge** so recruiters can run it with one click:
   ```markdown
   [![Open In Colab](https://colab.research.google.com/assets/colab-badge.svg)](YOUR_COLAB_LINK)
   ```

> **One-line resume bullet:** *"Trained a Convolutional Neural Network in PyTorch on CIFAR-10 (70% accuracy) and applied transfer learning with ResNet18 to reach 85%+, using a free Colab GPU."*

---

## What's Next?

In **Project 3**, you'll move from images to text: a **Sentiment Analyzer** that reads a review and decides if it's positive or negative. We'll start simple with scikit-learn, then upgrade to a state-of-the-art Hugging Face model. Welcome to NLP!
