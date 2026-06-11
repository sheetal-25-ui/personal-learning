# Module 13 — Day 3: Transfer Learning & Fine-Tuning

## Think of it Like This...

Imagine you need to hire someone to identify rare bird species in your backyard. You have two options:

1. **Raise a child from birth**, teach them everything about the world (what eyes are, what feathers look like, depth, color, light), and *then* teach them birds. Takes 20 years. 😩
2. **Hire an experienced wildlife photographer** who already understands eyes, feathers, light, and shapes — and just spend a weekend teaching them *your* specific birds. Done by Monday. 😎

Option 2 is **transfer learning.** Someone already trained a giant network on *millions* of images (it learned edges, textures, shapes, objects — the "understanding the world" part). You take that network and just teach it the *last little bit* for your specific task.

This is, hands down, the **single most useful skill** in practical computer vision. Almost no one trains from scratch anymore. Today you'll do it yourself. 🚀

---

## Why It Works (You Already Know This!)

Remember from Day 2: **early CNN layers learn generic, reusable features** — edges, textures, color blobs — and only the *late* layers learn task-specific objects.

```
   A ResNet trained on ImageNet (cats, cars, planes, 1000 things):

   ┌─────────────────────────────────────────┐  ┌──────────┐
   │   EARLY + MIDDLE LAYERS (the backbone)   │  │  FINAL   │
   │   edges, textures, shapes, object parts  │→ │  LAYER   │→ 1000 classes
   │   ← USEFUL FOR ALMOST ANY IMAGE TASK!    │  │ (specific)│
   └─────────────────────────────────────────┘  └──────────┘
            KEEP THIS (it's gold)                 ↑ REPLACE THIS
```

Edges and textures look the same whether you're classifying ImageNet cats or *your* photos of dogs, plants, or X-rays. So we **keep the backbone** and just **replace the final layer** with one sized for *our* classes.

---

## Two Flavors: Feature Extraction vs. Fine-Tuning

There are two ways to do transfer learning, and the difference is *which parts you allow to change during training*:

```
   ┌──────────────────────────────────────────────────────────────┐
   │  FEATURE EXTRACTION (freeze the backbone)                      │
   │  • Backbone: FROZEN (weights don't change)                    │
   │  • Only train the new final layer                             │
   │  • Fastest, needs the least data, hardest to overfit          │
   │  • Best when your data is small or similar to ImageNet        │
   └──────────────────────────────────────────────────────────────┘

   ┌──────────────────────────────────────────────────────────────┐
   │  FINE-TUNING (unfreeze some/all of the backbone)              │
   │  • Backbone: TRAINABLE (but with a tiny learning rate)        │
   │  • Train the whole thing gently, adjusting the features too   │
   │  • More powerful, needs more data, can overfit if careless    │
   │  • Best when you have more data or it's different from ImageNet│
   └──────────────────────────────────────────────────────────────┘
```

| | Feature Extraction | Fine-Tuning |
|---|---|---|
| Backbone weights | Frozen ❄️ | Trainable 🔥 (small learning rate) |
| Data needed | Little (hundreds) | More (thousands ideal) |
| Speed | Fast | Slower |
| Overfitting risk | Low | Higher |
| Use when... | Small/similar data | Larger/different data |

**The pro move:** Start with feature extraction. If you need more accuracy and have the data, *then* unfreeze and fine-tune gently. We'll do feature extraction in the main example and show the fine-tuning tweak right after.

---

## Setup

```bash
pip install torch torchvision pillow matplotlib
```

> 💻 **Run this on Google Colab with the GPU on** (Runtime → Change runtime type → GPU). Training will be many times faster.

---

## Organize Your Images Like This

torchvision's `ImageFolder` is wonderfully simple: **put each class's images in its own folder**, and it figures out the labels from the folder names automatically.

```
   my_dataset/
   ├── train/
   │   ├── cats/        ← all cat training photos go here
   │   │   ├── cat1.jpg
   │   │   └── cat2.jpg
   │   ├── dogs/
   │   │   ├── dog1.jpg
   │   │   └── dog2.jpg
   │   └── rabbits/
   │       └── rabbit1.jpg
   └── val/             ← validation photos (held back to test honestly)
       ├── cats/
       ├── dogs/
       └── rabbits/
```

> 📁 No labels file, no CSV — the folder *name* IS the label. To follow along without your own photos, you can grab any small image dataset, or use torchvision's built-in datasets (we'll mention an alternative at the end).

---

## The Full Project: Classify Your Own Images (Runnable)

We'll fine-tune **ResNet-18** to classify into *your* categories. Read the comments — every line is explained.

```python
import torch
import torch.nn as nn
import torchvision
from torchvision import datasets, transforms, models

# =============================================================
# STEP 1: Define how to preprocess images
# =============================================================
# Pretrained models expect specific preprocessing:
#  - resized to 224x224
#  - normalized with ImageNet's mean & std (the values the model was trained with)
# Using DIFFERENT normalization than the model was trained on hurts accuracy!
imagenet_mean = [0.485, 0.456, 0.406]   # ImageNet's per-channel mean (R,G,B)
imagenet_std  = [0.229, 0.224, 0.225]   # ImageNet's per-channel std

# Training transform INCLUDES augmentation (random flips/crops) for free extra data
train_tf = transforms.Compose([
    transforms.RandomResizedCrop(224),       # random crop + resize (augmentation)
    transforms.RandomHorizontalFlip(),        # 50% chance to mirror (augmentation)
    transforms.ToTensor(),                    # -> tensor, scales 0-255 to 0.0-1.0
    transforms.Normalize(imagenet_mean, imagenet_std),
])

# Validation transform: NO augmentation — we want consistent, honest testing
val_tf = transforms.Compose([
    transforms.Resize(256),
    transforms.CenterCrop(224),
    transforms.ToTensor(),
    transforms.Normalize(imagenet_mean, imagenet_std),
])

# =============================================================
# STEP 2: Load YOUR images with ImageFolder
# =============================================================
train_data = datasets.ImageFolder("my_dataset/train", transform=train_tf)
val_data   = datasets.ImageFolder("my_dataset/val",   transform=val_tf)

class_names = train_data.classes      # e.g. ['cats', 'dogs', 'rabbits']
num_classes = len(class_names)
print("Classes:", class_names)        # folder names become the labels!
print("Training images:", len(train_data))

train_loader = torch.utils.data.DataLoader(train_data, batch_size=32, shuffle=True)
val_loader   = torch.utils.data.DataLoader(val_data,   batch_size=32)

# =============================================================
# STEP 3: Load a pretrained model and SURGERY the final layer
# =============================================================
device = "cuda" if torch.cuda.is_available() else "cpu"

model = models.resnet18(weights="DEFAULT")   # pretrained on ImageNet!

# --- Feature extraction: FREEZE the backbone so its knowledge stays put ---
for param in model.parameters():
    param.requires_grad = False               # don't update these weights

# --- Replace the final layer to match OUR number of classes ---
# resnet18's final layer is called `fc`. It outputs 1000 (ImageNet); we want num_classes.
# The NEW layer's parameters DO have requires_grad=True by default, so only it trains.
num_features = model.fc.in_features           # 512 for resnet18
model.fc = nn.Linear(num_features, num_classes)

model = model.to(device)
print("Training on:", device)

# =============================================================
# STEP 4: Loss and optimizer
# =============================================================
loss_fn = nn.CrossEntropyLoss()               # standard for multi-class classification
# IMPORTANT: optimize ONLY the params that require grad (just the new fc layer).
optimizer = torch.optim.Adam(
    filter(lambda p: p.requires_grad, model.parameters()),
    lr=0.001                                  # 0.001 is a safe Adam default
)

# =============================================================
# STEP 5: The training loop (same 5 steps you know from Module 6!)
# =============================================================
epochs = 5                                    # few epochs needed — backbone is pretrained
for epoch in range(epochs):
    # ---- train ----
    model.train()                             # training mode (enables dropout etc.)
    for images, labels in train_loader:
        images, labels = images.to(device), labels.to(device)
        preds = model(images)                 # 1. FORWARD
        loss = loss_fn(preds, labels)         # 2. LOSS
        optimizer.zero_grad()                 # 3. ZERO old gradients
        loss.backward()                       # 4. BACKWARD (backprop)
        optimizer.step()                      # 5. STEP (update weights)

    # ---- validate (check accuracy on UNSEEN images) ----
    model.eval()                              # eval mode (disables dropout)
    correct = total = 0
    with torch.no_grad():                     # no gradients needed for testing
        for images, labels in val_loader:
            images, labels = images.to(device), labels.to(device)
            preds = model(images)
            predicted = preds.argmax(1)
            correct += (predicted == labels).sum().item()
            total += labels.size(0)
    acc = 100 * correct / total
    print(f"Epoch {epoch+1}/{epochs}  -  validation accuracy: {acc:.2f}%")

print("Done! 🎉")
```

**Expected output (your numbers depend on your data):**
```
Classes: ['cats', 'dogs', 'rabbits']
Training images: 600
Training on: cuda
Epoch 1/5  -  validation accuracy: 84.50%
Epoch 2/5  -  validation accuracy: 90.00%
Epoch 3/5  -  validation accuracy: 92.50%
Epoch 4/5  -  validation accuracy: 93.00%
Epoch 5/5  -  validation accuracy: 94.00%
```

🤯 **94% accuracy with only 600 photos and 5 epochs!** Training from scratch would need tens of thousands of images and hours of work to get close. *That's* the power of standing on the shoulders of a pretrained giant.

---

## Switching to Real Fine-Tuning

Want to squeeze out more accuracy and you have enough data? **Unfreeze the backbone** and train it gently. Just two changes to the code above:

```python
# CHANGE 1: Don't freeze the backbone — let everything be trainable.
# (Simply DELETE the "for param in model.parameters(): requires_grad = False" loop,
#  or set them all back to True after replacing fc.)
for param in model.parameters():
    param.requires_grad = True

# CHANGE 2: Use a MUCH smaller learning rate so we nudge the
# pretrained weights gently instead of wrecking them.
optimizer = torch.optim.Adam(model.parameters(), lr=1e-5)  # 0.00001, very small!
#                                                  ↑ tiny LR is the key to fine-tuning
```

> ⚠️ **Why such a tiny learning rate?** The backbone already knows great features. A big learning rate would yank those carefully-learned weights all over the place and *destroy* the knowledge you're trying to reuse. Fine-tuning = *gentle* nudges. Think "polishing," not "repainting."

**A common pro recipe:** freeze the backbone and train just `fc` for a few epochs first (feature extraction), *then* unfreeze and fine-tune the whole thing with a tiny learning rate. You get the best of both.

---

## Using Your Trained Model on a New Photo

After training, here's how to predict on a single fresh image:

```python
from PIL import Image
import torch

def predict(image_path, model, class_names, transform, device):
    model.eval()
    img = Image.open(image_path).convert("RGB")     # load & ensure 3 channels
    x = transform(img).unsqueeze(0).to(device)      # transform + add batch dim -> (1,3,224,224)
    with torch.no_grad():
        logits = model(x)                            # raw scores
        probs = torch.softmax(logits, dim=1)         # turn scores into probabilities
        conf, idx = probs.max(1)                     # best class + its confidence
    label = class_names[idx.item()]
    print(f"Prediction: {label}  ({conf.item()*100:.1f}% confident)")
    return label

predict("mystery_animal.jpg", model, class_names, val_tf, device)
# -> Prediction: dogs  (97.3% confident)
```

> 🧠 **`unsqueeze(0)`** adds a "batch dimension." Networks always expect a *batch* of images — shape `(N, 3, 224, 224)` — even when N is just 1. **`softmax`** converts the raw output scores into probabilities that sum to 100%.

---

## Saving and Loading Your Model

Don't lose your hard-earned training! Save it:

```python
# Save just the learned weights (the recommended way)
torch.save(model.state_dict(), "my_classifier.pth")

# Later... load it back (rebuild the same architecture first, then load weights)
model = models.resnet18()
model.fc = nn.Linear(model.fc.in_features, num_classes)   # match the saved shape!
model.load_state_dict(torch.load("my_classifier.pth"))
model.eval()
```

> 💾 **Why `state_dict` and not the whole model?** Saving just the weights (`state_dict`) is portable and version-safe. Saving the whole object can break when PyTorch or your code changes. Save the weights, rebuild the architecture in code, load the weights in. This is the recommended PyTorch pattern.

---

## No Photos Handy? Use a Built-in Dataset

To practice the *exact same pipeline* without collecting images, swap `ImageFolder` for a built-in dataset like **CIFAR-10** (60,000 tiny photos in 10 classes):

```python
from torchvision import datasets, transforms

tf = transforms.Compose([
    transforms.Resize(224),            # upscale CIFAR's 32x32 to ResNet's 224
    transforms.ToTensor(),
    transforms.Normalize([0.485,0.456,0.406], [0.229,0.224,0.225]),
])
train_data = datasets.CIFAR10(root="./data", train=True, download=True, transform=tf)
val_data   = datasets.CIFAR10(root="./data", train=False, download=True, transform=tf)
class_names = train_data.classes   # ['airplane','automobile','bird','cat',...]
# ...then plug into the SAME Steps 3–5 above (set num_classes = 10).
```

Everything else stays identical — that's the beauty of a clean pipeline.

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **Transfer learning** | Reusing a network trained on one task for a new task |
| **Pretrained model** | A network already trained (e.g. on ImageNet) — comes with learned weights |
| **Backbone** | The feature-extracting body you keep and reuse |
| **Feature extraction** | Freeze the backbone, train only a new final layer |
| **Fine-tuning** | Gently retrain the whole network with a tiny learning rate |
| **Freeze** | Set `requires_grad = False` so weights don't change during training |
| **ImageFolder** | torchvision tool that reads labels from folder names |
| **Normalization (ImageNet)** | Scaling inputs with the exact mean/std the model was trained on |
| **Data augmentation** | Random flips/crops to create more training data for free |
| **state_dict** | A model's learned weights — the recommended thing to save/load |

---

## Practice Exercises

### Exercise 1: Run It on CIFAR-10
Use the built-in CIFAR-10 swap above and run the full training loop (set `num_classes = 10`). Report your validation accuracy after 5 epochs. How does it compare to training a small CNN from scratch on the same data (from Module 6)?

### Exercise 2: Freeze vs. Fine-Tune
Run the project twice: once with the backbone frozen (feature extraction) and once with it unfrozen at `lr=1e-5` (fine-tuning). Compare final accuracy *and* training time. Which won? Was the extra accuracy worth the extra time?

### Exercise 3: Try a Different Backbone
Swap `resnet18` for `resnet50` or `mobilenet_v2`. Watch out — different models name their final layer differently! For `mobilenet_v2` the classifier is `model.classifier[1]`, not `model.fc`. Print the model to find it. Does the bigger backbone improve accuracy?

### Exercise 4: Make a Confusion Report
After training, run predictions over the validation set and count which classes get confused for which (e.g., cats predicted as dogs). Print a simple count. Which two classes does your model mix up most? Does that make sense to you as a human?

### Exercise 5 (Think): Spot the Overfitting
Imagine training accuracy hits 99% but validation accuracy is stuck at 70%. What's happening, and which lever would you pull first — more data augmentation, fewer epochs, freezing more layers, or collecting more images? Explain your reasoning.

---

## What's Next?

You can now take a world-class pretrained network and bend it to *your* task with a handful of photos. This is the workhorse skill behind most real computer-vision products.

But so far we've only asked *"what is in this image?"* — a single label for the whole picture. Tomorrow we ask harder questions: *"**where** are the objects, and **how many**?"*

In **Day 04 — Object Detection & Segmentation**, you'll learn:
- The difference between **classification**, **detection**, and **segmentation** (with pictures!)
- How **bounding boxes** work and the intuition behind **YOLO** ("You Only Look Once")
- What **segmentation** does — labeling every single pixel
- How to run a **pretrained object detector** on your own photo and draw the boxes

You taught a model *what* today. Tomorrow you teach it *where*. 📦

---

Outstanding work! Transfer learning is the moment computer vision goes from "cool theory" to "I can actually build this." With the pipeline you wrote today, you could build a plant identifier, a quality-control inspector, or a custom photo sorter — this weekend. That's real, deployable skill. See you in Day 4! 🎯
