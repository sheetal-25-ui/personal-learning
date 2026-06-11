# Module 6 — Day 2: Your First Neural Network with PyTorch

## Think of it Like This...

Yesterday you learned how a neural network *thinks*. Today you become the **builder**.

Imagine you learned how a car engine works (pistons, fuel, spark). Today, instead of just reading about it, you walk into a garage and actually **build a tiny engine** with your own hands. It won't be a Ferrari yet — but it'll run!

By the end of this lesson you'll have a real, working neural network in code, and you'll watch it make a prediction. Exciting!

---

## What is PyTorch?

> **PyTorch** = A free Python toolkit that does all the hard neural-network math for you.

Instead of writing the multiply-add-activate math by hand for millions of neurons, PyTorch does it instantly. It's the same tool used by **Anthropic, Meta, and Tesla**. You're learning the real thing.

---

## Where to Run This Code: Google Colab (Free!)

You don't need a powerful computer. We use **Google Colab** — a free website that runs Python in your browser and even gives you a **free GPU** (a chip that makes AI training fast).

**How to start:**
1. Go to **colab.research.google.com**
2. Click **"New Notebook"**
3. Type code in a cell, press **Shift + Enter** to run it
4. (For GPU later: menu → Runtime → Change runtime type → GPU)

That's your AI lab — completely free!

---

## Step 1: Import PyTorch

Good news: Colab already has PyTorch installed! So you just import it.

```python
# Import the main PyTorch library
import torch

# Import the "neural network" toolbox (we'll call it nn for short)
import torch.nn as nn

# Check the version and whether a GPU is available
print("PyTorch version:", torch.__version__)
print("GPU available?", torch.cuda.is_available())
```

**Expected output (roughly):**
```
PyTorch version: 2.x.x
GPU available? False     # (or True if you turned on the GPU)
```

> If you ever run this on your own computer instead of Colab, install PyTorch first with:
> `pip install torch` — but in Colab you can skip that. ✅

---

## Step 2: Meet the Tensor (PyTorch's Way of Storing Numbers)

Before building a network, you need to understand **tensors**.

> A **tensor** = a container for numbers. That's really all it is.

It's just like a box of numbers, but it can be different shapes:

| Shape | Name | Example | Real-world thing |
|-------|------|---------|------------------|
| Single number | Scalar | `5` | A temperature |
| A list | Vector | `[1, 2, 3]` | Test scores |
| A grid | Matrix | `[[1, 2], [3, 4]]` | A spreadsheet / grayscale image |
| A stack of grids | 3D Tensor | (many grids) | A color image (red, green, blue) |

Why tensors and not normal Python lists? Because tensors can run on a **GPU** and do math *super* fast. Let's make some:

```python
# A scalar (single number)
a = torch.tensor(5.0)

# A vector (1D list of numbers)
b = torch.tensor([1.0, 2.0, 3.0])

# A matrix (2D grid of numbers)
c = torch.tensor([[1.0, 2.0],
                  [3.0, 4.0]])

print("Scalar:", a)
print("Vector:", b)
print("Matrix:\n", c)
print("Shape of matrix c:", c.shape)   # tells us the dimensions
```

**Expected output:**
```
Scalar: tensor(5.)
Vector: tensor([1., 2., 3.])
Matrix:
 tensor([[1., 2.],
        [3., 4.]])
Shape of matrix c: torch.Size([2, 2])
```

### Tensors do math easily
```python
x = torch.tensor([1.0, 2.0, 3.0])
y = torch.tensor([10.0, 20.0, 30.0])

print("Add:", x + y)          # element by element
print("Multiply:", x * y)     # element by element
print("Sum of x:", x.sum())   # adds them all up
```

**Expected output:**
```
Add: tensor([11., 22., 33.])
Multiply: tensor([10., 40., 90.])
Sum of x: tensor(6.)
```

See? Tensors are just friendly boxes of numbers that do math fast. 📦

---

## Step 3: Build a Neural Network with `nn.Module`

In PyTorch, you build a network by making a **class** that inherits from `nn.Module`. Don't panic — it's a simple recipe with just two parts:

1. **`__init__`** — list the layers you want (the "ingredients")
2. **`forward`** — describe how data flows through them (the "recipe steps")

Let's build a tiny network. It will take **2 inputs**, pass them through one hidden layer of **3 neurons**, and produce **1 output**.

```
   INPUT          HIDDEN          OUTPUT
   (2 numbers)    (3 neurons)     (1 number)
   
   (o) \        / (o) \
        \      /       \
   (o) ---×---×---(o)----×----> answer
        /      \       /
              \ (o) /
   
   Layer 1: Linear(2 → 3) then ReLU
   Layer 2: Linear(3 → 1)
```

```python
import torch
import torch.nn as nn

# Define our tiny neural network
class TinyNet(nn.Module):
    def __init__(self):
        super().__init__()  # required setup line — always include it

        # Layer 1: takes 2 inputs, produces 3 outputs (the hidden neurons)
        # "Linear" = the multiply-by-weights-and-add-bias step from Day 1
        self.layer1 = nn.Linear(in_features=2, out_features=3)

        # The activation function (ReLU) — adds the "curves" so we can learn
        self.relu = nn.ReLU()

        # Layer 2: takes the 3 hidden outputs, produces 1 final answer
        self.layer2 = nn.Linear(in_features=3, out_features=1)

    # forward() describes how data flows through the layers
    def forward(self, x):
        x = self.layer1(x)   # multiply + add bias (input → hidden)
        x = self.relu(x)     # apply activation (zero out negatives)
        x = self.layer2(x)   # hidden → final output
        return x

# Create an instance of our network
model = TinyNet()
print(model)
```

**Expected output:**
```
TinyNet(
  (layer1): Linear(in_features=2, out_features=3, bias=True)
  (relu): ReLU()
  (layer2): Linear(in_features=3, out_features=1, bias=True)
)
```

🎉 **You just built a neural network!** PyTorch automatically created all the random weights and biases inside those `Linear` layers for you.

---

## Step 4: Run a Forward Pass (Make a Prediction!)

Remember the **forward pass** from Day 1? Feed data IN, get an answer OUT. Let's do it:

```python
# Make up some input: 2 numbers (because our network expects 2 inputs)
sample_input = torch.tensor([1.0, 2.0])

# Pass it through the network — this triggers the forward() method
prediction = model(sample_input)

print("Input:", sample_input)
print("Network's prediction:", prediction)
```

**Expected output (your numbers will differ — weights are random!):**
```
Input: tensor([1., 2.])
Network's prediction: tensor([0.4213], grad_fn=<AddBackward0>)
```

> **Wait — why is the answer "random-looking"?**
> Because we haven't *trained* the network yet! Right now its weights are random guesses, so its answer is meaningless. Training (Day 3!) is what nudges those weights until the answers become good. Today's win is simply: **you built a network and ran data through it.** 🙌

---

## Step 5: Peek Inside — See the Weights

Want to see those weights and biases the network is "learning"? Take a look:

```python
# Loop through every learnable parameter (weights and biases)
for name, param in model.named_parameters():
    print(name, "→ shape", tuple(param.shape))
```

**Expected output:**
```
layer1.weight → shape (3, 2)    # 3 neurons, each with 2 weights (one per input)
layer1.bias   → shape (3,)      # one bias per hidden neuron
layer2.weight → shape (1, 3)    # 1 output neuron with 3 weights
layer2.bias   → shape (1,)      # one bias for the output
```

These numbers are exactly what gets adjusted during training. You're looking at the network's "brain"!

---

## Bonus: A Quicker Way with `nn.Sequential`

For simple networks where data just flows straight through, PyTorch offers a shortcut — `nn.Sequential` lets you stack layers in a list:

```python
import torch.nn as nn

# Same network as before, but written in fewer lines
quick_model = nn.Sequential(
    nn.Linear(2, 3),   # input → hidden
    nn.ReLU(),         # activation
    nn.Linear(3, 1)    # hidden → output
)

print(quick_model)
print("Prediction:", quick_model(torch.tensor([1.0, 2.0])))
```

Both approaches work! Use the `nn.Module` class when you need full control, and `nn.Sequential` for quick-and-simple stacks.

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **PyTorch** | The toolkit that does neural-network math for you |
| **Tensor** | A container of numbers (scalar, vector, matrix...) |
| **`nn.Module`** | The base recipe for building a network |
| **`nn.Linear`** | A layer that does multiply-by-weights + add-bias |
| **`forward()`** | Describes how data flows through the network |
| **`nn.Sequential`** | A shortcut to stack layers in order |
| **Colab** | Free browser tool to run this code (with free GPU) |

---

## Practice Exercises

### Exercise 1: Make Your Own Tensors
In Colab, create:
1. A vector of your 5 favorite numbers
2. A 2×3 matrix (any numbers you like)
3. Print the `.shape` of each
4. Add 10 to every number in your vector (hint: `your_tensor + 10`)

### Exercise 2: Build a Bigger Network
Copy the `TinyNet` class but change it so it has:
- **4 inputs** instead of 2
- A hidden layer with **8 neurons** instead of 3
- Still **1 output**

Print the model and confirm the shapes look right. (You'll need to feed it 4 input numbers for a forward pass!)

### Exercise 3: Two Hidden Layers
Modify the network to have **two** hidden layers (with a ReLU after each). For example: `Linear(2→5) → ReLU → Linear(5→3) → ReLU → Linear(3→1)`. Run a forward pass. Congratulations — you just built a *deeper* network!

### Exercise 4: Investigate the Randomness
Run a forward pass, note the prediction. Now create a brand-new model (`model2 = TinyNet()`) and run the same input. Is the answer different? Why? (Hint: random weights!)

---

## What's Next?

Tomorrow in **Day 03**, we finally make the network *smart*! You'll learn:
- **Loss functions** — how the network measures "how wrong am I?"
- **Backpropagation** — how it figures out which weights to fix (intuition only — no scary calculus!)
- **Optimizers** (SGD and Adam) — the tools that actually nudge the weights
- The **training loop** — epochs and batches
- A full, runnable training example with a **loss curve graph** you can watch go down

This is the lesson where your random-guessing network learns to actually give *good* answers. 🚀

---

You went from theory to a real, running neural network today. You built it, ran data through it, and even peeked at its brain. That's a huge step — most people never get this far. See you in Day 3, builder! 🔧
