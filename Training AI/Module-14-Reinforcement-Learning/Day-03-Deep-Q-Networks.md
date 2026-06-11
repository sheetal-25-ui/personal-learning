# Module 14 — Day 3: Deep Q-Networks (DQN)

## When the Cheat-Sheet Gets Too Big

Yesterday's Q-table was beautiful: a tidy grid of 16 states × 4 actions = 64 cells. The agent filled them in and learned a perfect path. But now imagine the agent that made headlines in 2015: DeepMind's AI that learned to play Atari games like Breakout and Space Invaders **directly from the screen pixels.**

How big would *that* Q-table be?

An Atari screen is 210×160 pixels, each with a color. The number of possible screens is astronomically larger than the number of atoms in the observable universe. You could never store, let alone fill in, a table that big. Even CartPole — with its 4 *continuous* numbers — has infinitely many possible states; a table can't have a row for "pole angle = 0.03719284..." and a separate one for "0.03719285...".

Today you'll learn the breakthrough that fixed this: **replace the table with a neural network.** This is the **Deep Q-Network (DQN)** — "deep" because it uses a deep neural net, "Q" because it still learns Q-values.

---

## The Core Insight: Don't Store — Predict

A table **stores** a separate answer for every state. A neural network **predicts** the answer from the state's features. Same goal, totally different mechanism.

```
   Q-TABLE approach (Day 2):              DQN approach (today):

   state ─► [look up row in table]        state ─► [ neural network ] ─► Q-values
            └► Q-values                              (learns a function)

   ✗ Needs a row for every state          ✓ Generalizes to states never seen
   ✗ Impossible for huge/continuous       ✓ Handles continuous & high-dim states
```

Instead of memorizing, the network **learns the *pattern*** connecting states to values. Show it a state it has never seen before, and it can still make a sensible Q-value guess — because it learned the underlying relationship, not just isolated entries. This ability to handle new, unseen inputs is called **generalization**, and it's the superpower neural networks bring to RL.

---

## What the Network Looks Like

For CartPole, the network is small and simple:

```
   INPUT                HIDDEN LAYERS                 OUTPUT
   (the state)          (learn patterns)              (Q-value per action)

   ┌──────────┐                                       ┌──────────────┐
   │ cart pos │──┐      ┌────┐      ┌────┐      ┌────►│ Q(s, LEFT)   │
   │ cart vel │──┼─────►│ 64 │─────►│ 64 │──────┤     │              │
   │ pole ang │──┤      │neur│      │neur│      └────►│ Q(s, RIGHT)  │
   │ pole vel │──┘      └────┘      └────┘            └──────────────┘
   └──────────┘         ReLU        ReLU

   4 numbers in   →   network thinks   →   2 numbers out (one Q per action)
```

- **Input layer:** the state (4 numbers for CartPole).
- **Hidden layers:** the "thinking" layers that learn patterns (we'll use two layers of 64 neurons).
- **Output layer:** one Q-value per possible action (2 for CartPole).

To pick an action: feed in the state, read the output Q-values, and choose the action with the highest one — exactly like reading a row of the Q-table, except now the row is *computed* on the fly.

---

## Training the Network: Same Bellman Idea, New Tool

Here's the elegant part: **the learning target is still the Bellman equation from Day 2.** We just train the network to *predict* it.

Recall the Bellman target:

```
target = r + γ · max Q(s', a')
```

In Q-learning we *nudged* a table cell toward this target. In DQN, we treat it as a **supervised learning** problem (remember Module 1!):

- **Input:** the state `s`
- **What the network currently predicts:** `Q(s, a)`
- **What it *should* predict (the label):** `r + γ · max Q(s', a')`
- **Loss:** how far the prediction is from the target (mean squared error)

Then we use **gradient descent** (Module 6!) to adjust the network's weights so its prediction moves closer to the target. RL becomes "supervised learning where the labels come from the Bellman equation and the agent generates its own data."

> **Mind-bending detail:** the target itself depends on the network (it contains `Q(s', a')`). So the network is chasing a target that moves as it learns. This is part of why naive DQN is unstable — and why the two tricks below were invented.

---

## Trick #1: Experience Replay

The first problem: consecutive experiences are **highly correlated.** If the agent steps left, then left, then left, those three samples are nearly identical — and training a neural network on a stream of similar, correlated samples makes it forget old lessons and learn poorly. (Neural nets assume training data is shuffled and independent; a live stream of experience badly violates that.)

**Solution: store experiences in a memory buffer and train on random batches.**

```
   As the agent plays, save each step into a REPLAY BUFFER:

   ┌─────────────────────── Replay Buffer (a big list) ──────────────────────┐
   │ (s, a, r, s', done)  (s, a, r, s', done)  (s, a, r, s', done)  ...       │
   │  experience #1        experience #2        experience #3                 │
   └──────────────────────────────────────────────────────────────────────────┘

   To train: pull out a RANDOM BATCH (say 64 experiences) and learn from them.
             ↑ random sampling breaks the correlation between consecutive steps
```

Benefits:
- **Breaks correlation:** random sampling gives the network a diverse, shuffled mix.
- **Reuses data:** each experience can be learned from many times — far more sample-efficient than throwing each step away.
- **Stabilizes training:** more like the i.i.d. (independent) data neural nets expect.

Each stored item is a 5-tuple: `(state, action, reward, next_state, done)`. The `done` flag matters — if an episode ended, there's no "next state value" to add, so the target is just `r`.

---

## Trick #2: The Target Network

The second problem: we're chasing a moving target. Every time we update the network, the Bellman target (which uses the same network) shifts too — like a dog chasing its own tail. This causes wild oscillations and divergence.

**Solution: keep a second, frozen copy of the network just for computing targets.**

```
   ONLINE network    →  used to PICK actions and is updated every step
   TARGET network    →  a slightly-old COPY, used only to compute the target;
                        frozen, then refreshed every N steps

   target = r + γ · max Q_TARGET(s', a')   ← uses the stable, frozen copy
                          a'
```

Every few hundred or thousand steps, we copy the online network's weights into the target network. The target now changes only occasionally, in big steady steps, rather than constantly — giving the online network a **stable target to aim at.** This single trick was the difference between DQN diverging and DQN mastering Atari.

---

## Putting It Together: The DQN Algorithm

```
1.  Initialize the ONLINE network (random weights)
2.  Copy it to make the TARGET network
3.  Create an empty REPLAY BUFFER
4.  For each step the agent takes:
      a. Pick an action with ε-greedy (explore vs. exploit — still here from Day 1!)
      b. Take the action; observe (s, a, r, s', done)
      c. Store that experience in the replay buffer
      d. Sample a random BATCH from the buffer
      e. Compute Bellman targets using the TARGET network
      f. Train the ONLINE network to match those targets (gradient descent)
      g. Every N steps, copy ONLINE weights → TARGET network
5.  Decay ε over time. Repeat until the agent is good.
```

Notice every idea you already know is in here: states, actions, rewards, ε-greedy, the Bellman equation. DQN just swaps the table for a network and adds two stabilizing tricks.

---

## Code Sketch: DQN for CartPole (PyTorch)

This is a **runnable sketch** — readable and complete enough to run, but kept compact so the *structure* shines through rather than getting lost in production polish. Install PyTorch first: `pip install torch gymnasium numpy`.

```python
import gymnasium as gym
import numpy as np
import random
from collections import deque
import torch
import torch.nn as nn

# ── The Q-network: state (4 numbers) → Q-value per action (2 numbers) ─────
class QNetwork(nn.Module):
    def __init__(self, n_states, n_actions):
        super().__init__()
        self.net = nn.Sequential(
            nn.Linear(n_states, 64),   # 64 neurons: small but plenty for CartPole
            nn.ReLU(),
            nn.Linear(64, 64),
            nn.ReLU(),
            nn.Linear(64, n_actions),  # one output Q-value per action
        )

    def forward(self, x):
        return self.net(x)


# ── Setup ─────────────────────────────────────────────────────────────────
env = gym.make("CartPole-v1")
n_states = env.observation_space.shape[0]   # 4
n_actions = env.action_space.n              # 2

online_net = QNetwork(n_states, n_actions)        # picks actions, gets trained
target_net = QNetwork(n_states, n_actions)        # frozen copy for stable targets
target_net.load_state_dict(online_net.state_dict())  # start them identical

optimizer = torch.optim.Adam(online_net.parameters(), lr=1e-3)  # 1e-3 is a solid default
loss_fn = nn.MSELoss()                             # squared error toward Bellman target

# ── Hyperparameters (each with a reason) ──────────────────────────────────
replay_buffer = deque(maxlen=10000)   # 10k recent experiences; old ones drop off
batch_size = 64                       # 64 = good balance of speed vs. stable gradients
gamma = 0.99                          # value the future highly for long balancing
epsilon = 1.0                         # start fully exploring
epsilon_min = 0.01
epsilon_decay = 0.995                 # decay per episode
target_update_freq = 10               # copy weights to target net every 10 episodes
num_episodes = 400

# ── Training loop ─────────────────────────────────────────────────────────
for episode in range(num_episodes):
    state, info = env.reset()
    total_reward = 0
    done = False

    while not done:
        # ── ε-greedy action selection ──
        if random.random() < epsilon:
            action = env.action_space.sample()                     # explore
        else:
            with torch.no_grad():
                q_values = online_net(torch.tensor(state, dtype=torch.float32))
                action = int(torch.argmax(q_values))               # exploit

        # ── Step the environment and store the experience ──
        next_state, reward, terminated, truncated, info = env.step(action)
        done = terminated or truncated
        replay_buffer.append((state, action, reward, next_state, done))
        state = next_state
        total_reward += reward

        # ── Learn from a random batch (experience replay) ──
        if len(replay_buffer) >= batch_size:
            batch = random.sample(replay_buffer, batch_size)
            states, actions, rewards, next_states, dones = zip(*batch)

            states      = torch.tensor(np.array(states), dtype=torch.float32)
            actions     = torch.tensor(actions, dtype=torch.int64).unsqueeze(1)
            rewards     = torch.tensor(rewards, dtype=torch.float32)
            next_states = torch.tensor(np.array(next_states), dtype=torch.float32)
            dones       = torch.tensor(dones, dtype=torch.float32)

            # Q(s, a) the online net currently predicts for the actions taken
            current_q = online_net(states).gather(1, actions).squeeze(1)

            # Bellman target using the FROZEN target network
            with torch.no_grad():
                max_next_q = target_net(next_states).max(dim=1).values
                # If episode ended (done=1), there is no future value to add
                target_q = rewards + gamma * max_next_q * (1 - dones)

            # Train online net to make current_q match target_q
            loss = loss_fn(current_q, target_q)
            optimizer.zero_grad()
            loss.backward()
            optimizer.step()

    # ── End-of-episode bookkeeping ──
    epsilon = max(epsilon_min, epsilon * epsilon_decay)

    # Periodically refresh the target network (Trick #2)
    if episode % target_update_freq == 0:
        target_net.load_state_dict(online_net.state_dict())

    if (episode + 1) % 20 == 0:
        print(f"Episode {episode + 1:3d} | reward: {total_reward:5.0f} | epsilon: {epsilon:.3f}")

env.close()
```

Run it and you'll watch the reward climb from ~20 (random, Day 1!) toward 200, 400, even the 500 ceiling — the agent *learning to balance the pole* with no hand-coded rules. (RL training is noisy, so the reward will bounce around as it improves — that's normal.)

---

## Q-Table vs. DQN — Side by Side

| | Q-Table (Day 2) | Deep Q-Network (Today) |
|---|---|---|
| **Stores values how?** | A grid of numbers | A neural network |
| **States it handles** | Few, discrete | Huge, continuous, even raw pixels |
| **Generalizes to new states?** | No (each state separate) | Yes (learns the pattern) |
| **Memory needed** | One cell per state-action | Fixed-size network, regardless of state count |
| **Extra machinery** | None | Experience replay + target network |
| **Good for** | Small toy problems | CartPole, Atari, real-world control |

---

## Why DQN Was a Landmark

In 2015, DeepMind published DQN playing **49 different Atari games** from raw pixels using the *exact same* network architecture and hyperparameters — no game-specific tuning. It reached human-level or better on most of them. This was a watershed moment: it showed that a single, general learning algorithm could master many different tasks from raw sensory input. It's a direct ancestor of the systems that later beat world champions at Go, StarCraft, and Dota 2.

> **Honest note:** DQN works for problems with a small, *discrete* set of actions (left/right, the few Atari buttons). For continuous control — a robot arm choosing any angle, a car choosing any steering amount — picking "the max action" from infinitely many options doesn't work cleanly. That's where **policy gradient** methods come in. And those, conveniently, are tomorrow's topic — and the bridge to how modern language models like Claude are trained.

---

## Practice Exercises

### Exercise 1: Reason About Scale
1. A Q-table for chess would need a row for every possible board position (estimated ~10⁴⁰+). Explain in your own words why a Q-table is hopeless here but a neural network is at least *possible*.
2. CartPole's state is 4 continuous numbers. Why can't you make a Q-table for it, even though there are "only" 4 numbers? (Hint: how many distinct values can a decimal number take?)
3. Name one real-world problem from your own life where the number of possible "states" would be far too large for a table.

### Exercise 2: Understand the Two Tricks
1. **Experience replay:** In one or two sentences, explain why training on consecutive (correlated) game steps is bad for a neural network, and how random sampling from a buffer fixes it.
2. **Target network:** Explain the "dog chasing its own tail" problem and how freezing a copy of the network helps.
3. In the code, find the line `target_q = rewards + gamma * max_next_q * (1 - dones)`. Why do we multiply by `(1 - dones)`? What would go wrong if we forgot it?

### Exercise 3: Run and Experiment
1. Run the DQN code. Roughly how many episodes until the reward consistently passes 200?
2. Set `target_update_freq = 1` (update the target network every episode, almost like no target network at all). Does training become less stable / noisier? Why?
3. Shrink the replay buffer to `maxlen=200` and reduce `batch_size` to 8. How does performance change, and why? (Think about diversity of samples.)
4. Add `render_mode="human"` to `gym.make` and watch a trained agent balance the pole live for one episode. (Train first, then run a no-exploration test episode.)

### Exercise 4: Connect the Dots
Without looking back, list which ideas from **Day 1** and **Day 2** reappear inside DQN. (There are at least four: think about action selection, the update target, the loop, and what "Q" means.) Write one sentence for each describing where it shows up in the DQN algorithm.

---

## What's Next?

Today you crossed a major threshold. You learned why lookup tables hit a wall, and how swapping in a neural network — stabilized by experience replay and a target network — lets RL scale to huge, continuous, even pixel-based worlds. You wrote a DQN that teaches itself to balance a pole.

But we ended on a cliffhanger: DQN struggles with continuous actions, and there's a whole other family of methods that learns the **policy directly** instead of learning values first. That family turns out to be the key to one of the most important AI breakthroughs of our time.

Tomorrow in **Day 4: Policy Gradients & Modern RL**, you'll learn:
- **Policy gradients** — learning the policy directly, no Q-table required
- **REINFORCE** — the foundational policy-gradient algorithm, explained simply
- **Actor-Critic** — combining the best of value methods and policy methods
- **PPO** — the workhorse algorithm behind **RLHF**, the technique that turned raw language models into the helpful assistants you talk to every day

You're now fluent in the value-based half of reinforcement learning. Tomorrow we complete the picture — and connect everything back to the AI you actually use. See you there!
