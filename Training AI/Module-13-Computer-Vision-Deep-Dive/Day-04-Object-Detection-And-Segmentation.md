# Module 13 — Day 4: Object Detection & Segmentation

## Think of it Like This...

Show a friend a photo of a busy street and ask three different questions:

1. **"Is there a car in this photo?"** → *"Yes."* — that's **classification** (one label for the whole image).
2. **"Where are all the cars? Point to each one."** → *they draw a box around each car* — that's **object detection** (find and locate every object).
3. **"Color in exactly which pixels are car."** → *they shade in the precise car shapes* — that's **segmentation** (label every single pixel).

Yesterday you mastered question 1. Today you learn questions 2 and 3 — the tasks that power self-driving cars, medical imaging, sports analytics, and Instagram's background blur. 🚗

---

## The Three Levels of "Seeing"

```
   CLASSIFICATION          DETECTION              SEGMENTATION
   ──────────────          ─────────              ────────────
   ┌──────────────┐        ┌──────────────┐       ┌──────────────┐
   │              │        │  ┌──┐        │       │   ░░░░       │
   │   🐕 🐈      │        │  │🐕│  ┌──┐  │       │  ░░🐕░░ ▓▓▓  │
   │              │        │  └──┘  │🐈│  │       │  ░░░░░  ▓🐈▓ │
   │              │        │        └──┘  │       │         ▓▓▓  │
   └──────────────┘        └──────────────┘       └──────────────┘
   "dog and cat"           boxes around each       every pixel labeled
   (1 answer)              object + label          (pixel-perfect outline)

   "WHAT?"                 "WHAT + WHERE?"          "WHAT + EXACTLY WHICH PIXELS?"
```

| Task | Output | Question it answers | Example use |
|------|--------|--------------------|-------------|
| **Classification** | One label | What is this? | Photo tagging, spam-vs-ham |
| **Object Detection** | Boxes + labels | What objects, and where? | Self-driving cars, security cameras |
| **Semantic Segmentation** | A label for every pixel | Which pixels are "road"? | Medical scans, satellite imagery |
| **Instance Segmentation** | Pixel masks *per object* | Which pixels are *this specific* dog? | Photo background removal, robotics |

> 🧠 **Semantic vs. Instance segmentation:** Semantic says "these pixels are *dog*" (all dogs share one label). Instance says "these pixels are *dog #1*, those are *dog #2*" (it separates individuals). Instance segmentation = detection + per-object pixel masks.

---

## Bounding Boxes: How to Describe "Where"

A **bounding box** is just the rectangle that tightly wraps an object. We describe it with four numbers:

```
        x1,y1
          ┌─────────────┐
          │             │
          │    🐕       │   A box = (x1, y1, x2, y2)
          │             │     (x1,y1) = top-left corner
          │             │     (x2,y2) = bottom-right corner
          └─────────────┘
                     x2,y2
```

A detector's output for one object is roughly:
```python
{
    "label": "dog",
    "confidence": 0.94,                 # how sure (0 to 1)
    "box": [x1, y1, x2, y2]             # pixel coordinates of the rectangle
}
```

A full detection result is just a *list* of these — one per object found.

> 📦 **Two box formats exist** and they trip everyone up: `(x1, y1, x2, y2)` (corners) vs. `(x, y, width, height)` (top-left + size). Always check which one a library uses!

---

## How Do We Measure a "Good" Box? (IoU)

If the model predicts a box and the true box is slightly different, how do we score it? With **Intersection over Union (IoU)** — how much the two boxes overlap:

```
        ┌────────┐
        │ pred   │            overlap area
    ┌───┼────┐   │   IoU = ─────────────────────
    │   │░░░░│   │          combined area (union)
    │   └────┼───┘
    │  true  │              IoU = 1.0  → perfect match
    └────────┘              IoU = 0.0  → no overlap at all
                            IoU > 0.5  → usually counted "correct"
```

IoU = (area where they overlap) ÷ (total area they cover together). It's *the* standard metric for detection.

---

## YOLO: "You Only Look Once" 👀

Early detectors were slow because they scanned an image *thousands of times* ("is there an object here? here? here?"). **YOLO** had a brilliant insight, captured in its name:

> **YOLO looks at the whole image exactly ONCE** and predicts *all* boxes and labels in a single forward pass through the network.

```
   OLD WAY (slow):                    YOLO WAY (fast):
   ┌──────────────┐                   ┌──────────────┐
   │ check region │ → run CNN         │              │
   │ check region │ → run CNN         │  ONE pass    │ → all boxes
   │ check region │ → run CNN         │  through the │   + all labels
   │  ...1000s... │ → run CNN         │  whole image │   at once!
   └──────────────┘                   └──────────────┘
   thousands of passes 🐌             a single pass 🚀
```

**How YOLO works, intuitively:**
1. Divide the image into a grid (say 13×13 cells).
2. Each cell predicts: *"Is the center of an object in me? If so, what box and what label?"*
3. The network does this for every cell **simultaneously** in one pass.
4. Clean up overlapping boxes with **Non-Max Suppression** (see below).

Because it's one pass, YOLO is **fast enough for real-time video** — 30+ frames per second. That's why it's everywhere: drones, sports tracking, traffic monitoring, retail analytics.

### Non-Max Suppression (NMS) — cleaning up duplicate boxes

A detector often predicts *several* overlapping boxes for the same object. NMS keeps only the best one:

```
   Before NMS                  After NMS
   ┌──┐┌──┐                    ┌──┐
   │┌─┼┼─┐│  3 boxes on        │🐕│   keep the highest-confidence
   │└─┼┼─┘│  the same dog  →   └──┘   box, delete others that
   └──┘└──┘                            overlap it a lot (high IoU)
```

> 🧹 **NMS in one line:** "For each object, keep the most-confident box and throw away other boxes that overlap it too much." Almost every detector ends with this cleanup step.

---

## Run a Pretrained Detector (Runnable)

Let's actually detect objects! torchvision ships **Faster R-CNN** pretrained on the **COCO** dataset (80 everyday object categories: person, car, dog, bottle, etc.). No training needed.

```bash
pip install torch torchvision pillow matplotlib
```

```python
import torch
import torchvision
from torchvision.models.detection import (
    fasterrcnn_resnet50_fpn, FasterRCNN_ResNet50_FPN_Weights
)
from PIL import Image
import matplotlib.pyplot as plt
import matplotlib.patches as patches

# =============================================================
# STEP 1: Load a pretrained detector (trained on COCO's 80 classes)
# =============================================================
weights = FasterRCNN_ResNet50_FPN_Weights.DEFAULT
model = fasterrcnn_resnet50_fpn(weights=weights)
model.eval()                                  # inference mode

# The weights object conveniently carries the class names AND the right preprocessing
categories = weights.meta["categories"]       # ['__background__', 'person', 'bicycle', ...]
preprocess = weights.transforms()             # the exact transform the model expects

# =============================================================
# STEP 2: Load an image and run detection
# =============================================================
img = Image.open("street.jpg").convert("RGB")
x = preprocess(img)                           # -> a tensor, correctly normalized
with torch.no_grad():
    outputs = model([x])                      # note: detector takes a LIST of images

# outputs[0] is a dict with 'boxes', 'labels', 'scores'
result = outputs[0]
print("Objects found (before filtering):", len(result["boxes"]))

# =============================================================
# STEP 3: Keep only confident detections and draw the boxes
# =============================================================
CONFIDENCE_THRESHOLD = 0.7   # 0.7 = only keep boxes the model is >=70% sure about;
                             # lower it to catch more (but noisier) objects

fig, ax = plt.subplots(1, figsize=(10, 8))
ax.imshow(img)

for box, label_id, score in zip(result["boxes"], result["labels"], result["scores"]):
    if score < CONFIDENCE_THRESHOLD:
        continue                              # skip low-confidence guesses
    x1, y1, x2, y2 = box.tolist()
    name = categories[label_id.item()]        # turn the numeric label into a word

    # draw the rectangle
    rect = patches.Rectangle((x1, y1), x2 - x1, y2 - y1,
                             linewidth=2, edgecolor="lime", facecolor="none")
    ax.add_patch(rect)
    # draw the label + confidence above the box
    ax.text(x1, y1 - 5, f"{name} {score:.0%}",
            color="black", fontsize=10,
            bbox=dict(facecolor="lime", alpha=0.7, pad=1))

ax.axis("off")
plt.title("Object Detection")
plt.show()
```

**Expected:** your photo with green boxes drawn around each detected object, each labeled like `person 98%`, `car 91%`, `dog 87%`. 🎉

```
   ┌────────────────────────────────────┐
   │ person 98%                          │
   │ ┌──────┐         car 91%            │
   │ │  🧍  │       ┌──────────┐         │
   │ │      │       │   🚗     │         │
   │ └──────┘       └──────────┘         │
   │              dog 87%                │
   │              ┌────┐                 │
   │              │ 🐕 │                 │
   │              └────┘                 │
   └────────────────────────────────────┘
```

> 🎚️ **The confidence threshold is your main dial.** Raise it (e.g., 0.9) for fewer, more-certain boxes; lower it (e.g., 0.4) to catch more objects at the cost of false alarms. There's no single "right" value — it depends on whether missing an object or a false alarm is worse for *your* application.

---

## Bonus: Instance Segmentation with Mask R-CNN

Want pixel-perfect outlines instead of boxes? **Mask R-CNN** is Faster R-CNN plus a mask predictor. Nearly identical code:

```python
from torchvision.models.detection import (
    maskrcnn_resnet50_fpn, MaskRCNN_ResNet50_FPN_Weights
)
import torch

weights = MaskRCNN_ResNet50_FPN_Weights.DEFAULT
model = maskrcnn_resnet50_fpn(weights=weights)
model.eval()

img_tensor = weights.transforms()(img)
with torch.no_grad():
    out = model([img_tensor])[0]

# out['masks'] now has a soft pixel mask for EACH detected object!
# Each mask is shape (1, H, W) with values 0..1 — how "object-y" each pixel is.
for mask, score in zip(out["masks"], out["scores"]):
    if score < 0.7:
        continue
    binary_mask = mask[0] > 0.5    # threshold to a true/false pixel map
    # overlay binary_mask on the image to shade the exact object pixels
    # (e.g., plt.imshow(binary_mask, alpha=0.4, cmap='cool'))
```

That's how apps cut you out from your background, how phones blur it for "portrait mode," and how self-driving cars know *exactly* which pixels are road vs. sidewalk.

---

## When to Use Which

```
   Do you need to know just WHAT is in the image?
        └── YES → CLASSIFICATION (Day 3)

   Do you need WHERE objects are, and how many?
        └── YES → OBJECT DETECTION (bounding boxes, YOLO / Faster R-CNN)

   Do you need the EXACT pixel shape of objects?
        ├── Same label for all of a kind?    → SEMANTIC SEGMENTATION
        └── Separate each individual object? → INSTANCE SEGMENTATION (Mask R-CNN)
```

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **Classification** | One label for the whole image |
| **Object detection** | Find + locate every object with boxes and labels |
| **Bounding box** | The rectangle wrapping an object: `(x1, y1, x2, y2)` |
| **Segmentation** | Labeling individual pixels (semantic = by class, instance = by object) |
| **IoU** | Intersection over Union — how much two boxes overlap (0 to 1) |
| **YOLO** | "You Only Look Once" — fast, single-pass real-time detector |
| **Non-Max Suppression (NMS)** | Removing duplicate overlapping boxes for the same object |
| **Confidence threshold** | The minimum score to keep a detection (your main tuning dial) |
| **COCO** | A common 80-category detection dataset (person, car, dog, ...) |
| **Faster R-CNN / Mask R-CNN** | Accurate detectors; Mask R-CNN also outputs pixel masks |

---

## Practice Exercises

### Exercise 1: Detect on Your Own Photos
Run the Faster R-CNN example on 3 of your own photos (a street, a kitchen, a group of friends). How many objects does it find in each? Are there any funny mistakes (a backpack called a "handbag")?

### Exercise 2: Tune the Threshold
Run detection on the same photo with `CONFIDENCE_THRESHOLD` set to 0.3, 0.5, 0.7, and 0.9. Make a small table of how many boxes survive at each level. What's the trade-off you observe between missing real objects and drawing false ones?

### Exercise 3: Count Things
Modify the code so that instead of drawing boxes, it *counts* how many of each category it found and prints a summary like `person: 3, car: 2, dog: 1`. (Hint: use a Python dictionary to tally `name` values above the threshold.)

### Exercise 4: Try Segmentation
Run the Mask R-CNN bonus example and overlay the masks on your image (use `plt.imshow(binary_mask, alpha=0.4)` on top of the photo). Compare a *box* around a dog versus the *pixel mask* of the dog. Which would you need to remove a background cleanly?

### Exercise 5 (Think): Pick the Right Tool
For each app, say whether you'd use classification, detection, or segmentation, and why:
1. An app that tells you if a photo contains food or not.
2. A self-checkout that must count how many apples are on the scale.
3. A photo editor that lets you cut out a person and paste them elsewhere.
4. A medical tool that highlights the exact shape of a tumor in an X-ray.

---

## What's Next?

You've now climbed all three rungs of "seeing": *what* (classification), *where* (detection), and *exactly which pixels* (segmentation). You can run real, pretrained detectors on your own images today.

For our grand finale, **Day 05 — Vision Transformers & Generative Vision**, we leave CNNs behind and meet the cutting edge:
- **Vision Transformers (ViT)** — applying the Transformer you learned in Module 7 to *images* (chopping a picture into "patches" treated like words!)
- **CLIP** — a model that understands **images and text together**, the magic behind "search photos by typing a description"
- A first peek at how AI **generates** brand-new images from text (intro to **diffusion** — the full story is Module 15)
- A signpost toward **Module 14: Reinforcement Learning**

You've mastered how machines *recognize* the visual world. Tomorrow you'll see how they *connect it to language* — and start to *create*. 🎨

---

Phenomenal progress! Object detection is the technology behind self-driving cars and most "smart camera" features on Earth — and you just ran a state-of-the-art detector with about 40 lines of code. You're operating at a genuinely professional level now. One more day in the deep dive — see you in Day 5! 📦
