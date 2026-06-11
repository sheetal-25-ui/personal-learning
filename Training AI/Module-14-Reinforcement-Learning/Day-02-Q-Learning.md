# Module 14 — Day 2: Q-Learning

## From Watching to Teaching

Yesterday you watched agents act. Today you'll build one that actually **learns** — with no neural networks, no fancy libraries beyond NumPy, just a clever idea and a table of numbers. By the end, your agent will teach itself to cross a slippery frozen lake without falling through any holes. And you'll understand the single most famous equation in reinforcement learning, the **Bellman equation**, explained so simply it'll feel obvious.

Let's go.

---

## The Big Idea: Learn the *Value* of Things

How does a good chess player think? Not just "what move can I make?" but "**how good is the situation I'd be in after this move?**" They've learned to *value* positions.

Q-learning is built on exactly this idea. Instead of memorizing a fixed set of moves, the agent learns a **value** for every (situation, action) pair:

> **Q(state, action) = "If I'm in this state and take this action, how much total reward can I expect from here onward?"**

The letter **Q** stands for **Quality** — the quality of taking that action in that state. Once you know the Q-value of every action, picking the best move is easy: **just take the action with the highest Q-value.**

```
At a fork in the road (a STATE), you've learned:
   Q(here, go-left)  = 8.2     ← higher value!
   Q(here, go-right) = 3.5

Decision: go LEFT (it leads to more total reward down the line)
```

---

## Two Kinds of Value Functions

There are two closely related ways to talk about value. You'll see both in RL writing, so let's name them:

| Function | Question it answers | Notation |
|----------|---------------------|----------|
| **State-value** | "How good is it just to *be* in this state?" | `V(s)` |
| **Action-value** | "How good is it to take action `a` in state `s`?" | `Q(s, a)` |

Q-learning focuses on `Q(s, a)`, the **action-value**, because it directly tells the agent *what to do*: in any state, pick the action with the biggest Q. (V tells you how good a spot is, but not which way to move — Q is more actionable.)

---

## The Q-Table — A Giant Cheat-Sheet

If the world has a *finite, countable* number of states and actions, we can store every Q-value in a simple grid called the **Q-table**:

```
                 Action 0   Action 1   Action 2   Action 3
              ┌──────────┬──────────┬──────────┬──────────┐
   State 0    │   0.12   │   0.45   │   0.03   │   0.91   │  ← best action here is 3
   State 1    │   0.88   │   0.10   │   0.22   │   0.05   │  ← best action here is 0
   State 2    │   0.30   │   0.30   │   0.65   │   0.31   │  ← best action here is 2
     ...      │   ...    │   ...    │   ...    │   ...    │
              └──────────┴──────────┴──────────┴──────────┘
```

- **Rows** = states
- **Columns** = actions
- **Each cell** = the Q-value (expected total future reward) for that state-action pair

At the start, every cell is 0 (the agent knows nothing). Through trial and error, the cells fill in with learned values. To act: look at the current state's row and **pick the column with the highest number.**

> This works great when states and actions are few. When there are millions or billions of states (like pixels on a game screen), a table is impossible — that's exactly the problem Day 3 (Deep Q-Networks) solves. For today, tables are perfect.

---

## The Bellman Equation — Without the Fear

Here's the heart of Q-learning. Take a deep breath; we'll build it up piece by piece. The big question is:

> **How should we *update* a Q-value after we try an action and see what happens?**

When you take action `a` in state `s`, you receive reward `r` and land in a new state `s'`. The insight of Richard Bellman is this:

> **The value of where I am should equal the reward I just got, plus the value of the best thing I can do next.**

In plain words:

```
Value of (s, a)  =  immediate reward  +  value of the best next move
```

In symbols (this is the **Bellman equation**):

```
Q(s, a)  =  r  +  γ · max Q(s', a')
                        a'
```

Let's decode every symbol:

| Symbol | Meaning |
|--------|---------|
| `Q(s, a)` | Value of taking action `a` in state `s` — what we want to learn |
| `r` | The immediate reward we just received |
| `γ` (gamma) | **Discount factor** — how much we care about the future (0 to 1) |
| `max Q(s', a')` | The value of the *best* action available in the next state `s'` |

### What is γ (gamma), the Discount Factor?

`γ` controls how much the agent values future rewards versus immediate ones. It's a number between 0 and 1:

- **γ = 0:** "I only care about the reward RIGHT NOW." (totally short-sighted)
- **γ = 0.99:** "I care almost as much about the future as the present." (very far-sighted)
- **γ = 0.9** is a common middle ground.

> **Why discount the future at all?** Two reasons: (1) a reward now is more certain than a reward later (the future is uncertain), and (2) it keeps the math from blowing up to infinity in tasks that never end. Like "a bird in hand is worth two in the bush."

### The Learning Update Rule

We don't slam the Q-value to the new estimate all at once — that would make learning jumpy and unstable. Instead we **nudge** it a little bit in the right direction, controlled by a **learning rate** `α` (alpha):

```
                  ┌─────────── the "target": what we now think Q should be ──────────┐
Q(s,a)  ←  Q(s,a)  +  α · [ r + γ · max Q(s', a')  −  Q(s,a) ]
                                    a'
           └─old─┘        └──────────── the error (surprise) ─────────────┘
```

Read it as: **new Q = old Q + a small step toward (reward + discounted best future value).**

The bracketed part `[ target − old ]` is the **TD error** ("temporal difference" error) — basically the agent's *surprise*. If reality was better than expected, the error is positive and Q goes up. If worse, Q goes down. The learning rate `α` (a small number like 0.1) controls how big each nudge is.

| Hyperparameter | Symbol | Typical value | What it controls |
|----------------|--------|---------------|------------------|
| Learning rate | `α` (alpha) | 0.1 | How big each update step is |
| Discount factor | `γ` (gamma) | 0.95-0.99 | How much future rewards matter |
| Exploration rate | `ε` (epsilon) | 1.0 → 0.01 | Explore vs. exploit (from Day 1!) |

That's the whole algorithm. Seriously. Let's see it learn.

---

## Our Environment: FrozenLake

FrozenLake is a tiny grid world, perfect for a Q-table. You start at top-left, want to reach the gift at bottom-right, and must avoid holes in the ice.

```
   S  F  F  F        S = Start (safe)
   F  H  F  H        F = Frozen surface (safe to walk)
   F  F  F  H        H = Hole (fall in = episode over, reward 0)
   H  F  F  G        G = Goal (reach it = reward +1, you win!)
```

- **States:** 16 (a 4×4 grid, numbered 0-15)
- **Actions:** 4 (0=Left, 1=Down, 2=Right, 3=Up)
- **Reward:** +1 only for reaching the Goal, 0 everywhere else
- **The twist:** by default the ice is **slippery** — you don't always move where you intend! This makes it genuinely hard.

We'll start with `is_slippery=False` (deterministic) so the learning is crisp and easy to follow, then you'll try the slippery version in the exercises.

---

## Full Runnable Q-Learning Agent

Here is a complete, from-scratch Q-learning agent. Read the comments — every line connects to an idea above.

```python
import gymnasium as gym
import numpy as np

# ── 1. Create the environment ────────────────────────────────────────────
# is_slippery=False makes movement deterministic (easier to learn first).
env = gym.make("FrozenLake-v1", is_slippery=False)

n_states = env.observation_space.n   # 16 states (the 4x4 grid)
n_actions = env.action_space.n       # 4 actions (left, down, right, up)

# ── 2. Initialize the Q-table to all zeros ───────────────────────────────
# Rows = states, columns = actions. The agent starts knowing nothing.
q_table = np.zeros((n_states, n_actions))

# ── 3. Hyperparameters (each with a reason) ──────────────────────────────
alpha = 0.8        # learning rate: fairly high; deterministic env learns fast
gamma = 0.95       # discount factor: care a lot about reaching the future goal
epsilon = 1.0      # start fully exploring (we know nothing yet)
epsilon_min = 0.01 # never stop exploring entirely
epsilon_decay = 0.9995  # slowly shift from exploring to exploiting each episode
num_episodes = 5000     # plenty of practice runs for this small problem

rewards_per_episode = []

# ── 4. The training loop ─────────────────────────────────────────────────
for episode in range(num_episodes):
    state, info = env.reset()
    done = False
    total_reward = 0

    while not done:
        # ── ε-greedy action selection (from Day 1!) ──
        if np.random.random() < epsilon:
            action = env.action_space.sample()        # EXPLORE: random action
        else:
            action = np.argmax(q_table[state])         # EXPLOIT: best known action

        # ── Take the action; observe reward and next state ──
        next_state, reward, terminated, truncated, info = env.step(action)
        done = terminated or truncated

        # ── THE BELLMAN UPDATE — the core of Q-learning ──
        # target = reward + gamma * (best Q-value available in the next state)
        best_next = np.max(q_table[next_state])
        td_target = reward + gamma * best_next
        td_error = td_target - q_table[state, action]   # the "surprise"
        q_table[state, action] += alpha * td_error      # nudge toward target

        state = next_state
        total_reward += reward

    # ── Decay epsilon: explore less as we learn more ──
    epsilon = max(epsilon_min, epsilon * epsilon_decay)
    rewards_per_episode.append(total_reward)

    # Print progress every 500 episodes
    if (episode + 1) % 500 == 0:
        recent_success = np.mean(rewards_per_episode[-500:])
        print(f"Episode {episode + 1:5d} | "
              f"success rate (last 500): {recent_success:.2%} | "
              f"epsilon: {epsilon:.3f}")

env.close()

# ── 5. Inspect the learned Q-table ───────────────────────────────────────
print("\nLearned Q-table (rows=states, cols=[Left, Down, Right, Up]):")
np.set_printoptions(precision=2, suppress=True)
print(q_table)
```

Expected output looks something like:

```
Episode   500 | success rate (last 500): 45.20% | epsilon: 0.779
Episode  1000 | success rate (last 500): 91.80% | epsilon: 0.607
Episode  1500 | success rate (last 500): 99.60% | epsilon: 0.472
...
Episode  5000 | success rate (last 500): 100.00% | epsilon: 0.082
```

The agent went from random flailing (~0% success) to **reaching the goal essentially every time.** It learned a perfect path — purely from a +1 reward at the very end. No one told it the route. It discovered it.

---

## Watching the Trained Agent Play

After training, let's turn off exploration entirely (`ε = 0`, pure exploitation) and watch the agent follow its learned policy:

```python
import numpy as np

# Run a few episodes using ONLY the learned Q-table (no exploration)
test_env = gym.make("FrozenLake-v1", is_slippery=False)
wins = 0
test_episodes = 100   # 100 runs gives a clear success-rate estimate

for _ in range(test_episodes):
    state, info = test_env.reset()
    done = False
    while not done:
        action = np.argmax(q_table[state])   # always pick the best learned action
        state, reward, terminated, truncated, info = test_env.step(action)
        done = terminated or truncated
        if reward == 1:
            wins += 1

print(f"Trained agent reached the goal in {wins}/{test_episodes} runs.")
test_env.close()
```

For the non-slippery lake, a well-trained agent reaches the goal **100/100 times.** That's a policy learned entirely from experience.

### Reading the Learned Policy as Directions

We can extract the *best action* for each state and print it as a map of arrows:

```python
import numpy as np

# Map action numbers to arrow symbols (0=Left, 1=Down, 2=Right, 3=Up)
arrows = ['←', '↓', '→', '↑']
policy = [arrows[np.argmax(q_table[s])] for s in range(16)]

print("Learned policy (the agent's plan from each cell):")
for row in range(4):
    print('  '.join(policy[row * 4: row * 4 + 4]))
```

You'll see a sensible path of arrows winding from Start to Goal while steering clear of the holes. That arrow map **is** the policy the agent discovered.

---

## Why This Is Remarkable

Pause and appreciate what happened. The agent was given:
- No map of the lake
- No instructions
- A reward of +1 *only* at the very end (and 0 the entire rest of the time)

And from *only* that single delayed reward, propagated backward through the Bellman equation update over thousands of attempts, it figured out a complete, reliable strategy. The reward "flows backward" through the Q-table: the goal's +1 teaches the cell next to the goal, which teaches the cell next to *that*, and so on, until every cell knows the way home.

```
Reward propagation over training (intuition):

  Early:   only the goal cell has any value
  Later:   value "spreads" backward, one cell at a time, toward the start
  Done:    every cell points the right direction
```

---

## Practice Exercises

### Exercise 1: Tune the Hyperparameters
Run the training code and experiment:
1. Set `gamma = 0.1`. Does the agent still learn? Why might a low discount hurt on a task where reward only comes at the very end?
2. Set `epsilon_decay = 1.0` (epsilon never decays, stays at 1.0 → always random). What happens to the success rate? Why?
3. Set `alpha = 0.01` (tiny learning rate). Does it still learn within 5000 episodes? What changes?

### Exercise 2: The Slippery Lake (Harder!)
Change `is_slippery=False` to `is_slippery=True` in **both** the training and testing code.
1. The ice is now slippery — actions only succeed sometimes. Re-run training. Does the success rate reach 100%? Why not? What's a realistic ceiling?
2. Slippery FrozenLake is genuinely hard. Try increasing `num_episodes` to 20000 and lowering `alpha` to 0.1 (a smaller step is more stable when outcomes are random). Does the success rate improve?
3. In one sentence: why does randomness in the environment make a perfect 100% success rate impossible here?

### Exercise 3: Trace the Bellman Update by Hand
Suppose `alpha = 0.5`, `gamma = 0.9`, and currently `Q(s, a) = 2.0`. The agent takes action `a`, receives reward `r = 1`, and lands in state `s'` where the best action has value `max Q(s', a') = 4.0`.
1. Compute the TD target: `r + gamma * max Q(s', a')`.
2. Compute the TD error: `target − Q(s, a)`.
3. Compute the new `Q(s, a)` after the update.
4. Did the value go up or down? Was the agent pleasantly surprised or disappointed?

### Exercise 4: Print and Interpret the Policy
1. Run the "arrow map" code on a successfully trained (non-slippery) agent.
2. Trace the arrows from Start (top-left) to Goal (bottom-right). Does the path make sense? Does it avoid the holes?
3. Look at the arrows printed *on* a hole cell or an unreachable cell. Why might those be "garbage" / arbitrary? (Hint: how often did the agent actually visit and learn from those cells?)

---

## What's Next?

Today you built a learning agent from scratch and met the Bellman equation — the engine that turns sparse, delayed rewards into smart behavior. You watched a Q-table fill itself in and discover a path no one taught it.

But there's a catch we hinted at: the Q-table only works when states and actions are few enough to list. What about a video game with millions of pixel combinations, or a robot with continuous sensors? You'd need a table with more rows than there are atoms in the universe.

Tomorrow in **Day 3: Deep Q-Networks (DQN)**, we solve exactly this. You'll learn:
- **Why tables don't scale** — and the precise moment they break down
- How to replace the table with a **neural network** that *approximates* Q-values for any state
- **Experience replay** and **target networks** — two clever tricks that made DQN actually work
- The code sketch behind the famous Atari-playing AI that learned from raw pixels

You're now doing real reinforcement learning — the same core idea behind systems that beat world champions. Excellent work today. See you tomorrow!
