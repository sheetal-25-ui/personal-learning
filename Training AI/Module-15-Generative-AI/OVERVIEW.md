# Module 15 — Generative AI

## Welcome to the Module That Makes Computers *Create*

Up until now in this course, most of the AI you've met has been a **judge**: it looks at something and gives you a verdict. "Is this spam? Yes." "Is this a cat or a dog? Cat." "Will this customer churn? Probably."

This module is about a completely different superpower: **making brand-new things**. Images that never existed. Paragraphs nobody ever wrote. Voices, songs, and even video — all conjured from a text prompt or a spark of noise.

That's **Generative AI** — the technology behind ChatGPT, Midjourney, Stable Diffusion, DALL·E, Sora, and the AI art and writing tools that took the world by storm.

> **The big idea:** Instead of teaching a computer to *recognize* a cat, we teach it to *paint* a cat that has never existed before.

---

## Think of it Like This...

Imagine two kinds of art students:

- **Student A (the critic):** Can look at any painting and instantly tell you "that's a Van Gogh" or "that's a fake." Brilliant at *judging*. But ask them to paint something and... nothing.
- **Student B (the artist):** Has studied thousands of paintings so deeply that they can pick up a brush and create a *new* painting in any style you ask for.

Old-school AI was mostly Student A. **Generative AI is Student B.** This module teaches you how Student B's brain works.

---

## What You'll Learn (5 Days)

| Day | Topic | What You'll Walk Away With |
|-----|-------|----------------------------|
| **Day 1** | What Is Generative AI? | The difference between *judging* and *creating*; the whole landscape (text, image, audio, video); why it all exploded around 2022 |
| **Day 2** | Autoencoders & VAEs | How to squeeze data into a tiny "latent space" and rebuild it; the first real generative architecture |
| **Day 3** | GANs | The "forger vs. detective" game that produced the first jaw-dropping fake faces |
| **Day 4** | Diffusion Models | How adding and removing noise became the secret sauce behind Stable Diffusion — *with real runnable code* |
| **Day 5** | Multimodal & Frontier | Models that see, hear, and speak (GPT-4o, Gemini, Claude with vision); text-to-video; where this is all heading |

---

## How These Days Fit Together

```
Day 1  ──►  WHAT is generative AI, and WHY now?
              │
Day 2  ──►  AUTOENCODERS / VAEs  (the first "compress then create" idea)
              │   learn a "latent space"
              ▼
Day 3  ──►  GANs  (two networks fight → sharp, realistic images)
              │   great images, but hard to train
              ▼
Day 4  ──►  DIFFUSION  (noise in, picture out → today's image king)
              │   powers Stable Diffusion, DALL·E, Midjourney
              ▼
Day 5  ──►  MULTIMODAL & FRONTIER  (text + image + audio + video, all at once)
```

Each day builds on the last. By Day 5 you'll understand the *family tree* of generative models — and where the next breakthroughs are coming from.

---

## What You Need

- You've finished (or skimmed) the earlier modules — you should be comfortable with the idea of a **neural network**, **training**, and a **model**. If those words feel fuzzy, peek back at Module 1 and the deep learning module.
- A computer with Python. For Day 4 we'll install Hugging Face's `diffusers` library and actually generate an image. A GPU helps a lot but isn't strictly required (we'll show you a CPU-friendly path too).
- Curiosity. That's the main one. 🙂

---

## A Gentle Warning (and a Promise)

Generative AI has some **math** under the hood — probability, latent spaces, loss functions. We're going to explain all of it with analogies, pictures, and tiny code sketches *before* we ever show a scary equation. You will **not** need a math degree.

By the end of this module, you'll be able to explain — at a dinner party — exactly how an AI turns the words "a cozy cabin in a snowy forest at sunset" into a beautiful image. That's a genuinely impressive party trick.

---

## What's Next?

Turn to **Day 1** and let's start with the single most important distinction in this entire module: the difference between an AI that **judges** and an AI that **creates**. Once that clicks, everything else falls into place.

Let's go create something. 🎨
