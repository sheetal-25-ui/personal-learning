# Module 13 — Day 1: Images as Data

## Think of it Like This...

Imagine a giant sheet of graph paper. Every tiny square is colored in. Step back far enough, and all those colored squares blend together into a *picture* — a face, a beach, a cat.

That's literally what a digital image is. Each little square is a **pixel** (short for "picture element"), and each pixel is just a **number** (or a few numbers) describing its color. Your phone screen has millions of these squares glowing at once.

So here's the big realization that unlocks all of computer vision:

> **To a computer, an image is not a picture. It's a grid of numbers.** 🔢

Once you truly *believe* that, everything else — filters, CNNs, object detection — becomes "doing math on a grid of numbers." Let's see it for ourselves. 👀

---

## A Pixel Is Just a Number

For a **grayscale** (black-and-white) image, each pixel is a single number from **0 to 255**:

- `0` = pure black
- `255` = pure white
- `128` = medium gray

```
   A tiny 5x5 grayscale image of a "plus" sign:

   ┌────┬────┬────┬────┬────┐
   │  0 │  0 │255 │  0 │  0 │
   ├────┼────┼────┼────┼────┤
   │  0 │  0 │255 │  0 │  0 │
   ├────┼────┼────┼────┼────┤
   │255 │255 │255 │255 │255 │   ← the bright middle row
   ├────┼────┼────┼────┼────┤
   │  0 │  0 │255 │  0 │  0 │
   ├────┼────┼────┼────┼────┤
   │  0 │  0 │255 │  0 │  0 │
   └────┴────┴────┴────┴────┘
        ↑ this column is bright too
```

See the plus sign hiding in the numbers? The bright `255`s form a `+` on a black `0` background. **That grid IS the image.**

**Why 0–255?** Because each pixel value is stored in one **byte** (8 bits), and 8 bits can represent 2⁸ = 256 different values: 0 through 255. It's a historical, hardware-driven choice that stuck.

---

## Color = Three Numbers per Pixel (RGB)

Color images need more than one number per pixel. The most common system is **RGB**: every pixel is a mix of **R**ed, **G**reen, and **B**lue light.

> **RGB pixel** = three numbers `(R, G, B)`, each 0–255, describing how much red, green, and blue light to glow.

```
   (255,   0,   0) = pure RED
   (  0, 255,   0) = pure GREEN
   (  0,   0, 255) = pure BLUE
   (255, 255, 255) = WHITE  (all colors at full = white light)
   (  0,   0,   0) = BLACK  (no light)
   (255, 255,   0) = YELLOW (red + green)
   (128,   0, 128) = PURPLE
```

So a color image is really **three grids stacked on top of each other** — one for red, one for green, one for blue. We call these the **channels**.

```
        The "channels" of a color image:

        Red channel        Green channel       Blue channel
        ┌─┬─┬─┐            ┌─┬─┬─┐             ┌─┬─┬─┐
        │ │ │ │            │ │ │ │             │ │ │ │
        ├─┼─┼─┤    +       ├─┼─┼─┤      +      ├─┼─┼─┤   =  full color image
        │ │ │ │            │ │ │ │             │ │ │ │
        └─┴─┴─┘            └─┴─┴─┘             └─┴─┴─┘
       (how red?)         (how green?)        (how blue?)
```

A color image of height `H` and width `W` is therefore a 3D grid of shape **`(H, W, 3)`** — height × width × 3 channels.

---

## The Shape of an Image

This is the single most important thing to remember today:

| Image type | Shape | Meaning |
|-----------|-------|---------|
| Grayscale | `(H, W)` | height × width, one number per pixel |
| Color (RGB) | `(H, W, 3)` | height × width × 3 channels |
| A *batch* of color images | `(N, H, W, 3)` | N images stacked together |

> ⚠️ **A famous gotcha:** OpenCV loads color images as **BGR** (Blue-Green-Red), *not* RGB! It's a historical quirk from the early days of the library. If your reds and blues look swapped, this is almost always why. We'll handle it below.

---

## Setup

```bash
pip install opencv-python numpy matplotlib
```

We'll use **OpenCV** (imported as `cv2`) for image operations, **NumPy** because images *are* NumPy arrays, and **Matplotlib** to display results.

---

## Seeing an Image as Numbers (Runnable)

Let's prove that an image is just an array. We'll make a tiny image by hand — no file needed.

```python
import numpy as np
import matplotlib.pyplot as plt

# -------------------------------------------------------------
# Build the 5x5 "plus sign" from the diagram above, BY HAND.
# Just a NumPy array of numbers between 0 and 255.
# dtype=uint8 means "unsigned 8-bit integer" = exactly 0-255.
# -------------------------------------------------------------
plus = np.array([
    [  0,   0, 255,   0,   0],
    [  0,   0, 255,   0,   0],
    [255, 255, 255, 255, 255],
    [  0,   0, 255,   0,   0],
    [  0,   0, 255,   0,   0],
], dtype=np.uint8)

print("Shape:", plus.shape)        # (5, 5)  -> 5 rows, 5 cols, grayscale
print("Data type:", plus.dtype)    # uint8
print("Value at center pixel:", plus[2, 2])  # 255 (the bright middle)

# Display it (cmap='gray' tells matplotlib these are grayscale values)
plt.imshow(plus, cmap='gray', vmin=0, vmax=255)
plt.title("An image is just an array!")
plt.show()
```

**Expected output:**
```
Shape: (5, 5)
Data type: uint8
Value at center pixel: 255
```
...and a window showing a white plus sign on a black background. **You just drew an image with pure numbers.** 🎉

---

## Loading a Real Image with OpenCV

```python
import cv2
import numpy as np
import matplotlib.pyplot as plt

# Load an image from a file (use any .jpg/.png you have)
img = cv2.imread("photo.jpg")   # returns a NumPy array in BGR order!

print("Shape:", img.shape)      # e.g. (720, 1280, 3) -> H x W x 3 channels
print("Type:", img.dtype)       # uint8
print("One pixel (row 0, col 0):", img[0, 0])  # e.g. [B, G, R] = [34 67 120]

# OpenCV loads as BGR, but matplotlib expects RGB. Convert so colors are right:
img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

plt.imshow(img_rgb)
plt.title(f"Loaded image: {img.shape[1]} x {img.shape[0]} pixels")
plt.axis("off")
plt.show()
```

> 🧠 **Remember:** `img.shape` is `(H, W, 3)`. So `img.shape[0]` is the **height** and `img.shape[1]` is the **width** — they're in the *opposite* order from how we usually say "width × height." Easy to mix up!

---

## Basic Operation 1: Resize

Resizing changes how many pixels an image has. Neural networks usually need all inputs to be the *same* size (e.g., 224×224), so resizing is one of the most common operations in all of computer vision.

```python
import cv2

img = cv2.imread("photo.jpg")

# Resize to an exact size (width, height) -- note OpenCV wants (W, H) here!
small = cv2.resize(img, (224, 224))
print("Resized to:", small.shape)   # (224, 224, 3)

# Resize by a scale factor instead (half the size)
half = cv2.resize(img, None, fx=0.5, fy=0.5)
print("Half size:", half.shape)
```

> ⚠️ **Another ordering gotcha:** `cv2.resize` takes the new size as `(width, height)`, but `img.shape` reports `(height, width, ...)`. OpenCV is inconsistent on purpose-of-confusion. Always double-check!

---

## Basic Operation 2: Crop (It's Just Array Slicing!)

Cropping a region of an image is just **slicing the NumPy array** — no special function needed. This is the payoff of understanding that images are arrays.

```python
import cv2

img = cv2.imread("photo.jpg")    # shape (H, W, 3)

# Crop rows 50 to 250, and columns 100 to 400.
# Format: img[ y_start:y_end , x_start:x_end ]
crop = img[50:250, 100:400]

print("Original:", img.shape)
print("Cropped:", crop.shape)    # (200, 300, 3)
```

```
   img[50:250, 100:400]
        │      │
        │      └── columns (x): 100 → 400  → width 300
        └── rows (y): 50 → 250            → height 200
```

---

## Basic Operation 3: Filters (Blurring & Sharpening)

Remember **convolution** from Module 6? You slide a small **kernel** (a grid of numbers) over the image to compute a new value for each pixel. Blurring and sharpening are exactly this!

### Blur — average each pixel with its neighbors

```python
import cv2

img = cv2.imread("photo.jpg")

# Gaussian blur: a (15, 15) kernel averages each pixel with nearby ones.
# Bigger kernel = blurrier. The 0 means "compute the spread automatically".
blurred = cv2.GaussianBlur(img, (15, 15), 0)

cv2.imwrite("blurred.jpg", blurred)   # save the result to a file
```

**Why does averaging blur things?** Because sharp differences between neighboring pixels (edges, fine detail) get *smoothed out* when you mix each pixel with its neighbors. It's like squinting your eyes.

### Sharpen — a custom kernel you design yourself

```python
import cv2
import numpy as np

img = cv2.imread("photo.jpg")

# A classic sharpening kernel. It boosts the center pixel and
# subtracts its neighbors, exaggerating differences (edges).
sharpen_kernel = np.array([
    [ 0, -1,  0],
    [-1,  5, -1],
    [ 0, -1,  0]
])

sharp = cv2.filter2D(img, -1, sharpen_kernel)   # apply OUR kernel
cv2.imwrite("sharpened.jpg", sharp)
```

```
   How filter2D works (the "convolution" from Module 6):

   For each pixel, line up the 3x3 kernel over it and its 8 neighbors,
   multiply each pair, sum them up → that's the new pixel value.

      neighbors        kernel          multiply & sum
      ┌──┬──┬──┐      ┌──┬──┬──┐
      │a │b │c │      │ 0│-1│ 0│       new = 5*e
      ├──┼──┼──┤  ⊗   ├──┼──┼──┤   =       - b - d - f - h
      │d │e │f │      │-1│ 5│-1│       (boosts the center,
      ├──┼──┼──┤      ├──┼──┼──┤        subtracts neighbors)
      │g │h │i │      │ 0│-1│ 0│
      └──┴──┴──┘      └──┴──┴──┘
```

This is the *exact same operation* a CNN's convolutional layer performs — except a CNN **learns** the kernel numbers automatically, while here we typed them in by hand. Same machinery, different source of the numbers! 🤯

---

## Basic Operation 4: Edge Detection

**Edges** are where the brightness changes sharply — the outline of objects. Detecting edges is a foundational step in old-school computer vision, and CNNs' first layers learn to do this automatically.

The **Canny edge detector** is the classic algorithm:

```python
import cv2
import matplotlib.pyplot as plt

img = cv2.imread("photo.jpg")

# Step 1: convert to grayscale (edges are about brightness, not color)
gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

# Step 2: run Canny. The two numbers are thresholds:
#   below 100  -> definitely NOT an edge
#   above 200  -> definitely an edge
#   in between -> an edge only if connected to a strong edge
edges = cv2.Canny(gray, threshold1=100, threshold2=200)

print("Edges shape:", edges.shape)   # same H x W, but only 0s and 255s

plt.subplot(1, 2, 1)
plt.imshow(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
plt.title("Original"); plt.axis("off")

plt.subplot(1, 2, 2)
plt.imshow(edges, cmap='gray')
plt.title("Edges"); plt.axis("off")
plt.show()
```

The result is a black image with **white outlines** wherever the original had a strong edge — like a coloring-book version of your photo. ✏️

---

## Putting It All Together: A Mini Image Pipeline

Here's a realistic preprocessing pipeline — the kind of thing you'd do *before* feeding an image to a neural network:

```python
import cv2
import numpy as np

def preprocess(path, size=224):
    """Load -> fix colors -> resize -> normalize to 0..1 floats."""
    img = cv2.imread(path)                       # 1. load (BGR, uint8)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)   # 2. BGR -> RGB
    img = cv2.resize(img, (size, size))          # 3. uniform size for the network
    img = img.astype(np.float32) / 255.0         # 4. scale 0-255 -> 0.0-1.0
    return img

result = preprocess("photo.jpg")
print("Final shape:", result.shape)   # (224, 224, 3)
print("Value range:", result.min(), "to", result.max())  # 0.0 to 1.0
```

> **Why divide by 255?** Neural networks train far more smoothly when inputs are small numbers around 0–1 instead of 0–255. This "normalization" step is in almost every vision pipeline (you saw `ToTensor()` do this for you in Module 6 — now you know what it's doing under the hood!).

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **Pixel** | One tiny square of the image; a single number (gray) or 3 numbers (color) |
| **Channel** | One color layer — Red, Green, or Blue |
| **RGB** | The Red-Green-Blue color system; OpenCV uses **BGR** order though! |
| **Shape** | The dimensions of the image array: `(H, W)` or `(H, W, 3)` |
| **uint8** | The data type for pixel values: integers 0–255 |
| **Kernel / Filter** | A small grid slid over the image to blur, sharpen, or detect edges |
| **Convolution** | Sliding-and-summing a kernel over an image |
| **Normalization** | Scaling pixel values (e.g. 0–255 → 0.0–1.0) so networks train better |
| **Canny** | A classic edge-detection algorithm |

---

## Practice Exercises

### Exercise 1: Draw with Numbers
Make a NumPy array by hand (like the plus sign) that draws a different shape — a letter "L", a diagonal line, or a smiley face — in a 7×7 grid. Display it with `plt.imshow(..., cmap='gray')`. Can you "see" your shape in the numbers before you display it?

### Exercise 2: Split the Channels
Load a colorful photo and display its **Red, Green, and Blue channels separately** as three grayscale images. (Hint: `img_rgb[:, :, 0]` is the red channel, `[:, :, 1]` green, `[:, :, 2]` blue.) Which channel is brightest for a photo of grass? For the sky?

### Exercise 3: Build a "Cartoon" Filter
Combine operations: blur an image, run edge detection on it, then think about how you'd overlay the dark edges onto the blurred image. (Bonus: OpenCV has `cv2.stylization()` and `cv2.pencilSketch()` — try them and compare to your handmade version!)

### Exercise 4: Brightness Hack
Since pixels are just numbers, you can brighten an image by *adding* to every pixel and darken it by *subtracting*. Try `bright = cv2.add(img, 50)` and `dark = cv2.subtract(img, 50)`. Display all three. Why does using `cv2.add` matter instead of plain `img + 50`? (Hint: what happens when 200 + 100 exceeds 255?)

### Exercise 5 (Think): Why Resize Everything?
A neural network's first layer expects a fixed number of inputs. Explain in your own words why every image must be resized to the same dimensions before training. What information might we *lose* when we squish a tall photo into a square?

---

## What's Next?

You now understand the *atoms* of computer vision: images are arrays, color is channels, and filters are just convolutions on those arrays. 

Tomorrow in **Day 02**, we go deeper into the networks that learn these filters *automatically*. We'll:
- Recap how CNNs work (now that you know what convolution really is)
- Tour the **famous architectures** that defined the field — **LeNet**, **AlexNet**, **VGG**, and **ResNet**
- Understand the breakthrough idea of **skip connections** that let networks go hundreds of layers deep
- See **what each layer actually learns**, from edges to textures to whole objects

You learned to do image math by hand today. Tomorrow you meet the legendary networks that learned to do it on their own. 🏛️

---

Fantastic start to the deep dive! The single idea you nailed today — *an image is just a grid of numbers* — is the foundation everything else stands on. Every fancy vision model in this module is, at heart, doing clever math on those grids. See you in Day 2! 🔢
