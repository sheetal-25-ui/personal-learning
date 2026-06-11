# Module 13 — Day 5: Vision Transformers & Generative Vision

## Think of it Like This...

For 20 years, everyone "knew" the rule: **images need CNNs, language needs Transformers.** Two separate worlds, two separate toolkits.

Then in 2020, researchers asked a cheeky question: *"What if we just... chop an image into little squares, pretend each square is a word, and feed the whole thing to a Transformer — the exact same kind you learned in Module 7?"*

It sounded crazy. It worked *spectacularly.* 🤯

Today we close the deep dive by tearing down the wall between vision and language. You'll meet the **Vision Transformer (ViT)**, then **CLIP** (which understands images and text *together*), then take your first peek at how AI **generates** images from a sentence. Everything you learned about Transformers in Module 7 is about to pay off in pixels.

---

## Vision Transformers (ViT): An Image Is Just a Sentence of Patches

Remember from Module 7: a Transformer takes a **sequence of tokens** (words) and uses **attention** so every token can "look at" every other token. ViT's whole trick is turning an image into such a sequence.

```
   STEP 1: Chop the image into a grid of fixed patches (e.g. 16x16 pixels each)

      ┌──┬──┬──┬──┐
      │ 1│ 2│ 3│ 4│       Each little square is a "patch."
      ├──┼──┼──┼──┤       A 224x224 image with 16x16 patches
      │ 5│ 6│ 7│ 8│   →   = a sequence of 196 patches.
      ├──┼──┼──┼──┤
      │ 9│10│11│12│       Think of each patch as a "word."
      ├──┼──┼──┼──┤
      │13│14│15│16│
      └──┴──┴──┴──┘

   STEP 2: Flatten each patch into a vector ("embed" it, like a word embedding)

      [patch 1] [patch 2] [patch 3] ... [patch 196]
         ↓         ↓         ↓              ↓
      [vector]  [vector]  [vector]  ...  [vector]    ← a sequence!

   STEP 3: Add POSITION info (so the model knows patch 5 is below patch 1)
   STEP 4: Feed the sequence into a standard Transformer (attention layers!)
   STEP 5: A special [CLS] token's output → the final classification
```

> 🧠 **The whole idea in one line:** *Patches are to ViT what words are to a language Transformer.* Attention then lets every patch consider every other patch — so a patch in the corner can "talk to" a patch in the center directly, in a single step.

### CNN vs. ViT: How They "Look"

```
   CNN                                ViT
   ───                                ───
   Looks LOCALLY first (small         Looks GLOBALLY from the start
   kernels), builds up to global.     (attention connects all patches).

   Strong built-in assumption:        Few assumptions: must LEARN that
   "nearby pixels relate"             nearby patches relate — needs
   (great with less data).            LOTS of data to do well.
```

| | CNN | Vision Transformer |
|---|-----|-------------------|
| Core operation | Convolution (local) | Attention (global) |
| Built-in assumptions | "nearby pixels matter" | almost none |
| Data appetite | Works with less | Needs *lots* (or pretraining) |
| Where it shines | Small/medium datasets | Huge datasets, large scale |

> ⚖️ **The trade-off:** CNNs come with a helpful built-in bias (nearby pixels relate), so they learn from less data. ViTs make fewer assumptions, so with *enough* data they can learn even better patterns — but they're data-hungry. On giant datasets, ViTs match or beat CNNs. Today's best vision models often blend both ideas.

### Run a Pretrained ViT (Runnable)

Hugging Face makes this a few lines. We'll classify an image with a pretrained ViT.

```bash
pip install transformers torch pillow
```

```python
from transformers import ViTImageProcessor, ViTForImageClassification
from PIL import Image
import torch

# Load a ViT pretrained on ImageNet (1000 classes)
processor = ViTImageProcessor.from_pretrained("google/vit-base-patch16-224")
model = ViTForImageClassification.from_pretrained("google/vit-base-patch16-224")
model.eval()

img = Image.open("photo.jpg").convert("RGB")

# The processor does the patching + normalization for us
inputs = processor(images=img, return_tensors="pt")
with torch.no_grad():
    logits = model(**inputs).logits          # raw scores over 1000 classes

predicted_id = logits.argmax(-1).item()
print("Prediction:", model.config.id2label[predicted_id])
# -> Prediction: golden retriever
```

> Notice `patch16-224` in the model name: **16×16 patches**, **224×224** images. That's literally the patch-chopping scheme from our diagram. 🔍

---

## CLIP: Teaching One Model to Understand Images AND Text

Here's where things get magical. **CLIP** (Contrastive Language–Image Pretraining, from OpenAI) learns images and text in the *same space*, so it can compare a picture to a sentence directly.

**How it learned:** CLIP saw ~400 million `(image, caption)` pairs scraped from the internet. It trained two encoders — one for images, one for text — to **agree**: the image's vector should be close to its *correct* caption's vector, and far from wrong captions.

```
   Image encoder              Text encoder
   ┌──────────┐               ┌─────────────────────┐
   │  🐕 photo │ → [vector]    │ "a photo of a dog"  │ → [vector]
   └──────────┘      ║         └─────────────────────┘      ║
                     ║                                       ║
                     ╚════ trained to be CLOSE if they ══════╝
                              match, FAR if they don't
```

Once trained, you can do something incredible: **classify an image into categories you invent on the spot, with no extra training.** This is called **zero-shot classification**.

```
   Give CLIP:  an image  +  a list of text labels you make up
   CLIP returns: how well the image matches EACH label

   Image: 🐕
   Candidate labels: ["a photo of a dog", "a photo of a cat", "a photo of a car"]
                              ↓
   Scores:                  94%            4%             2%
                          WINNER!
```

You never *trained* CLIP on "dog vs cat vs car" — you just *described* the categories in words, and it understood. That's the power of connecting vision to language.

### Run CLIP Zero-Shot (Runnable)

```bash
pip install transformers torch pillow
```

```python
from transformers import CLIPProcessor, CLIPModel
from PIL import Image
import torch

model = CLIPModel.from_pretrained("openai/clip-vit-base-patch32")
processor = CLIPProcessor.from_pretrained("openai/clip-vit-base-patch32")

img = Image.open("mystery.jpg").convert("RGB")

# Invent ANY labels you want — no training needed!
labels = [
    "a photo of a dog",
    "a photo of a cat",
    "a photo of a car",
    "a photo of a pizza",
]

# Process image + texts together
inputs = processor(text=labels, images=img, return_tensors="pt", padding=True)
with torch.no_grad():
    outputs = model(**inputs)
    # logits_per_image: how well the image matches each text
    probs = outputs.logits_per_image.softmax(dim=1)[0]

for label, p in zip(labels, probs):
    print(f"{p.item()*100:5.1f}%   {label}")
```

**Expected output (for a dog photo):**
```
 94.2%   a photo of a dog
  3.1%   a photo of a cat
  1.9%   a photo of a car
  0.8%   a photo of a pizza
```

> 🚀 **Why CLIP is a big deal:** It powers "search your photos by typing a description," content moderation with custom rules, and — crucially — it's the **bridge** that lets text-to-image generators understand your words. Which brings us to...

---

## A Peek at Generative Vision: Making Images from Text

So far, every model has *analyzed* existing images. But how does an AI **create** a brand-new image from a sentence like *"an astronaut riding a horse on Mars"*? Welcome to **diffusion models** — the technology behind Stable Diffusion, DALL·E, and Midjourney.

> 📌 **This is just a teaser — the full deep dive is Module 15.** Here's the core intuition so the magic doesn't feel like a black box.

### The Diffusion Idea: Learn to Un-Blur Noise

The key trick is almost playful. Imagine taking a clear photo and adding a tiny bit of random "static" (noise), over and over, until it's *pure noise* — like an old TV with no signal.

```
   FORWARD (training): add noise step by step
   ┌─────┐   ┌─────┐   ┌─────┐         ┌─────────┐
   │ 🐕  │ → │🐕 ░ │ → │🐕░░░│ → ... → │░░░░░░░░░│  pure noise
   └─────┘   └─────┘   └─────┘         └─────────┘
   clear      a bit     noisier         static

   The model TRAINS by learning to REVERSE one step:
   "given a noisy image, predict the noise and remove a little."
```

Now run it **backward**. Start from pure random noise and repeatedly ask the model: *"remove a little noise."* Step by step, a coherent image *emerges* from the static — like a photo developing in a darkroom. 🌌

```
   REVERSE (generating): start from noise, denoise step by step
   ┌─────────┐         ┌─────┐   ┌─────┐   ┌─────┐
   │░░░░░░░░░│ → ... → │🐕░░░│ → │🐕 ░ │ → │ 🐕  │  a new image!
   └─────────┘         └─────┘   └─────┘   └─────┘
   random noise         emerging          clear picture
```

**Where does the text prompt come in?** This is the beautiful payoff: a **CLIP-style text encoder** (yes — the model you just learned!) turns your prompt into a vector that *guides* the denoising at every step, nudging the emerging image toward *"astronaut,"* *"horse,"* *"Mars."* The vision-language bridge you learned today is exactly what makes text-to-image possible. 🌉

### A Tiny Taste (Runnable — needs a GPU)

If you have a GPU (Colab works great), you can generate an image in a few lines with Hugging Face `diffusers`:

```bash
pip install diffusers transformers accelerate torch
```

```python
from diffusers import StableDiffusionPipeline
import torch

# Load a pretrained text-to-image diffusion model
pipe = StableDiffusionPipeline.from_pretrained(
    "runwayml/stable-diffusion-v1-5",
    torch_dtype=torch.float16          # half-precision = faster, less memory
)
pipe = pipe.to("cuda")                 # diffusion really wants a GPU

prompt = "a cozy cabin in a snowy forest at sunset, digital art"
image = pipe(prompt).images[0]         # generate!
image.save("generated.png")
print("Saved your AI-generated image! 🎨")
```

> ⏳ **Heads up:** This downloads a multi-gigabyte model and needs a GPU. If you don't have one, just read along — we'll go *deep* on diffusion (how it really works, fine-tuning, ControlNet, and more) in **Module 15**. Today's goal is just the *aha*: generation is "learning to reverse noise, guided by text."

---

## The Big Picture: How It All Connects

```
   Module 6:  CNNs              → machines learned to SEE
   Module 7:  Transformers      → machines learned to read & write LANGUAGE
   Module 13: ViT + CLIP        → those two worlds MERGE 🌉
                                     (images become sequences; text guides vision)
   Module 15: Diffusion         → machines learned to CREATE images from words
```

The wall between vision and language is gone. Modern models (like the ones powering today's chat assistants with image understanding) are *multimodal* — they handle pixels and words in one unified system. You now understand the building blocks behind all of it. 🎓

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **Vision Transformer (ViT)** | A Transformer applied to images by splitting them into patches |
| **Patch** | A small square of an image, treated like a "word" token |
| **Attention** | Lets every patch consider every other patch (from Module 7) |
| **CLIP** | A model that understands images and text in the same space |
| **Zero-shot classification** | Classifying into categories you describe in words, with no training |
| **Multimodal** | A model that handles more than one data type (e.g., images + text) |
| **Diffusion model** | Generates images by reversing a noise-adding process |
| **Text-to-image** | Creating a new image from a text prompt (DALL·E, Stable Diffusion) |
| **Prompt** | The text description guiding image generation |

---

## Practice Exercises

### Exercise 1: ViT vs. CNN on the Same Photo
Run the pretrained ViT on a few photos. Then run a pretrained ResNet (from Day 2/3) on the *same* photos. Do they agree? When they disagree, which one do *you* think is right?

### Exercise 2: Get Creative with CLIP
Run the CLIP example, but invent unusual, specific labels for one photo — e.g., for a beach photo try `["a relaxing vacation", "a dangerous storm", "a crowded city"]`. How well does CLIP capture *mood* and *concepts*, not just objects? This reveals how much it "understands."

### Exercise 3: Build a Mini Image Search
Take 5 of your own photos. For each, run CLIP against the *same* set of text labels and record the scores. Now flip it: given a text query like "something I'd eat," which of your 5 photos scores highest? You just built a tiny text-based image search engine!

### Exercise 4: Patch Math
A ViT uses 16×16 patches on a 224×224 image. How many patches is that in total? (Work it out: 224÷16 along each side, then multiply.) Why does the patch count matter for the Transformer's speed? (Hint: attention compares every patch to every other.)

### Exercise 5 (Think): Why Does Text-to-Image Need CLIP?
Explain in 3–4 sentences how a CLIP-style text encoder lets a diffusion model turn the words *"a red bicycle"* into the *right* picture. Use the idea that text and images live in the "same space." (This is the bridge we'll cross fully in Module 15.)

---

## What's Next?

🎉 **You just finished the Computer Vision Deep Dive!** Take a second to appreciate how far you've come in five days:

- **Day 1:** Images are grids of numbers (pixels, channels, OpenCV)
- **Day 2:** The legendary CNN architectures and what each layer learns
- **Day 3:** Transfer learning — bending a pretrained giant to *your* task
- **Day 4:** Detection & segmentation — *what*, *where*, and *which pixels*
- **Day 5:** ViT, CLIP, and the bridge to image generation

You can now load images, transform them, classify them, fine-tune models, detect and segment objects, and you understand the cutting-edge models connecting vision to language. That's a *serious*, professional-level toolkit. 🏆

### Two roads from here:

➡️ **Module 14: Reinforcement Learning** — A completely different way to learn. Instead of learning from labeled examples, an agent learns by **trial and error** — taking actions, getting rewards, and improving its strategy. It's how AI mastered chess, Go, video games, and how modern AI assistants are tuned with human feedback. If you've ever wondered how a computer learns to *play* and *win*, that's next.

➡️ **Module 15: Generative AI & Diffusion** — The full story behind today's teaser. How diffusion models *really* work, how to fine-tune them on your own style, guidance and ControlNet, and the art of prompting. If today's image-generation peek lit a spark, this is where it becomes a flame.

We recommend heading to **Module 14** next to round out your understanding of *how machines learn* — then circling back to Module 15 to dive into *creating*.

---

You've completed an advanced module that many working engineers haven't formally studied. You didn't just learn the buzzwords — you understand *why* ResNet has skip connections, *why* transfer learning works, *how* YOLO sees in real time, and *how* an image can be a sentence of patches. That depth is rare and valuable. Be proud of this one. 🌟

Onward to Reinforcement Learning in **Module 14**! 🚀
