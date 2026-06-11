# Module 14 — Day 4: Policy Gradients & Modern RL

## Learning the Strategy Directly

For three days we've taken the "value first" path: figure out how good each action is (the Q-value), then pick the best one. It's powerful — but it's a bit indirect. We learn *values* and *derive* a policy from them.

Today we flip it around. What if we just... **learn the policy directly?** Skip the Q-values entirely and teach a network "given this state, here's the probability of each action" — then nudge those probabilities up when things go well and down when they go badly.

This idea, called **policy gradients**, is the other great branch of RL. And it leads us straight to **PPO** — the algorithm that powers **RLHF**, the very technique used to train ChatGPT and Claude to be helpful. By the end of today, you'll understand the line that connects a balancing pole to the AI assistant you talk to. Let's finish strong.

---

## Two Families of RL

Step back and see the whole landscape:

```
                        REINFORCEMENT LEARNING
                                  │
            ┌─────────────────────┴─────────────────────┐
            │                                            │
     VALUE-BASED                                  POLICY-BASED
   (learn values, derive policy)              (learn the policy directly)
            │                                            │
   Q-Learning (Day 2)                          REINFORCE (today)
   DQN (Day 3)                                 Actor-Critic (today)
            │                                   PPO (today!)
            └──────────────► ACTOR-CRITIC ◄────────────┘
                          (best of both worlds)
```

| | Value-Based (Days 2-3) | Policy-Based (Today) |
|---|---|---|
| **What it learns** | Q-values, then picks the max | The policy (action probabilities) directly |
| **Output** | "Action A is worth 8.2, B is worth 3.5" | "70% chance do A, 30% chance do B" |
| **Continuous actions?** | Awkward (can't max over infinite actions) | Natural fit |
| **Randomness** | Usually deterministic (always pick max) | Naturally stochastic (probabilities) |
| **Example use** | Atari, simple control | Robotics, LLM fine-tuning (RLHF!) |

---

## Why Learn the Policy Directly?

Three big reasons policy methods matter:

1. **Continuous actions.** A robot arm can rotate to *any* angle; a car can steer by *any* amount. You can't "take the max over infinitely many actions" like DQN does. A policy network can just output a continuous value (or a distribution over them) directly.

2. **Natural randomness.** Some situations genuinely need randomness — think rock-paper-scissors, where any predictable strategy gets exploited. A policy that outputs probabilities (e.g., 33%/33%/33%) handles this gracefully.

3. **It scales to enormous action spaces — like language.** When the "action" is *choosing the next word* from a 100,000-word vocabulary, a policy that outputs a probability distribution over words is exactly the right shape. (Sound familiar? That's literally what a language model does.)

---

## REINFORCE — The Foundational Policy Gradient

REINFORCE is the simplest policy-gradient method, and its core intuition is wonderfully human:

> **Try a whole episode. If it went well, make the actions you took *more* likely. If it went badly, make them *less* likely.**

That's it. It's "trial, then reflect on the whole attempt."

### The Intuition, Step by Step

```
1. Play a FULL episode using your current policy (which outputs action probabilities).
2. Add up the total reward you got (the "return").
3. Look back at every action you took during that episode.
4. If the episode's return was GOOD (high):
       → increase the probability of the actions you took
   If the episode's return was BAD (low):
       → decrease the probability of the actions you took
5. Repeat across many episodes. Good behaviors get reinforced, bad ones fade.
```

It's like reviewing a cricket match: "We won big — keep doing what we did. We lost badly — do less of that."

### The Policy Gradient Idea (Lightly)

We don't need heavy math, just the shape of the idea. The network outputs probabilities for each action. We adjust its weights in the direction that:

```
  increases log(probability of action taken)  ×  how good the outcome was
  └───────────── push this action's probability ────┘  └─── scaled by return ───┘
```

- If the return is **large and positive**, we push hard to make those actions more likely.
- If the return is **near zero or negative**, we push little or in the opposite direction.

That "scale the push by the outcome" is the entire trick. (The "gradient" part is just gradient descent from Module 6 — we're nudging weights, same as always.)

### REINFORCE Code Sketch (CartPole, PyTorch)

```python
import gymnasium as gym
import numpy as np
import torch
import torch.nn as nn

# ── Policy network: state → probability of each action ────────────────────
class PolicyNetwork(nn.Module):
    def __init__(self, n_states, n_actions):
        super().__init__()
        self.net = nn.Sequential(
            nn.Linear(n_states, 64),
            nn.ReLU(),
            nn.Linear(64, n_actions),
            nn.Softmax(dim=-1),     # Softmax → outputs sum to 1 (valid probabilities)
        )

    def forward(self, x):
        return self.net(x)


env = gym.make("CartPole-v1")
policy = PolicyNetwork(env.observation_space.shape[0], env.action_space.n)
optimizer = torch.optim.Adam(policy.parameters(), lr=1e-2)  # 1e-2 works well for this small task
gamma = 0.99   # discount future rewards (same gamma idea as Day 2)

for episode in range(500):
    state, info = env.reset()
    log_probs = []     # remember the log-probability of each action we take
    rewards = []       # remember the reward at each step
    done = False

    # ── 1. Play a FULL episode, recording actions and rewards ──
    while not done:
        state_t = torch.tensor(state, dtype=torch.float32)
        probs = policy(state_t)                       # e.g. [0.7, 0.3]
        dist = torch.distributions.Categorical(probs) # sample from the distribution
        action = dist.sample()                        # pick an action by its probability
        log_probs.append(dist.log_prob(action))       # store log-prob for the update

        state, reward, terminated, truncated, info = env.step(action.item())
        done = terminated or truncated
        rewards.append(reward)

    # ── 2. Compute the discounted return at each timestep (from the end backward) ──
    returns = []
    G = 0
    for r in reversed(rewards):
        G = r + gamma * G          # future-discounted total reward from this step on
        returns.insert(0, G)
    returns = torch.tensor(returns)
    # Normalize returns → stabilizes training (a common, important trick)
    returns = (returns - returns.mean()) / (returns.std() + 1e-8)

    # ── 3. Policy gradient update: push up good actions, down bad ones ──
    loss = 0
    for log_prob, G in zip(log_probs, returns):
        loss += -log_prob * G      # negative because optimizers MINIMIZE
    optimizer.zero_grad()
    loss.backward()
    optimizer.step()

    if (episode + 1) % 50 == 0:
        print(f"Episode {episode + 1} | episode length: {len(rewards)}")

env.close()
```

Run it and the episode length climbs as the policy learns to keep the pole up — purely by making good actions more probable.

---

## The Problem with REINFORCE — and the Actor-Critic Fix

REINFORCE works, but it's **noisy and slow.** The issue: it judges every action by the *whole episode's* total reward. But some actions in a good episode were actually bad (just got lucky), and vice versa. Using one number for the whole episode is a blunt instrument — like grading every student in a class the same because the class average was high.

**Actor-Critic** fixes this by using *two* networks that work as a team:

```
   ┌─────────────────────────────────────────────────────────┐
   │                                                           │
   │   ACTOR  (the policy)                                     │
   │   "What action should I take?"  ──► picks actions         │
   │            ▲                                              │
   │            │ feedback: "that was better/worse than usual" │
   │            │                                              │
   │   CRITIC  (a value function)                              │
   │   "How good was that, really?"  ──► estimates V(s)        │
   │                                                           │
   └─────────────────────────────────────────────────────────┘
```

- **The Actor** is the policy — it chooses actions (like REINFORCE).
- **The Critic** is a value function (like Day 2's V!) — it estimates how good states are, giving the actor *per-step* feedback instead of one episode-wide number.

The critic provides a **baseline**: instead of "was the total reward high?", the actor learns from "was this action **better than the critic expected**?" This difference is called the **advantage**, and it makes learning far more stable and efficient. The actor and critic improve together — it's the best of both worlds (value-based + policy-based).

> **Analogy:** The actor is a student trying things; the critic is a tutor who, after each move, says "that was above/below what I expected from you here." Much more useful than waiting until the final exam to give a single grade.

---

## PPO — The Workhorse of Modern RL (and RLHF!)

**Proximal Policy Optimization (PPO)** is an actor-critic method, and it's arguably the most important RL algorithm in use today. It's the default choice at OpenAI, DeepMind, and beyond — robotics, game-playing, and crucially, **training large language models.**

### The One Idea Behind PPO

PPO's insight addresses a subtle danger in policy gradients: **if you update the policy too aggressively, you can wreck it.** One overly large step based on a lucky (or unlucky) batch can send a well-trained policy off a cliff, and it may never recover.

PPO's fix is right there in its name — "**Proximal**" means "stay close":

> **Improve the policy, but don't let it change too much in a single update. Take careful, clipped steps.**

```
   Vanilla policy gradient:  ───► BIG leap ───► (might land somewhere terrible)

   PPO:  ──► small ──► small ──► small ──► steady, reliable improvement
              (clip each update so the new policy stays "proximal" to the old one)
```

Technically, PPO **clips** how far the updated policy's action probabilities can move away from the old policy's, per update. The result: training that is both **efficient** and **remarkably stable** — which is exactly why it became the go-to algorithm. You typically don't write PPO from scratch; libraries like **Stable-Baselines3** give you a battle-tested implementation:

```python
# PPO in practice — you rarely implement it by hand:
# pip install stable-baselines3
from stable_baselines3 import PPO
import gymnasium as gym

env = gym.make("CartPole-v1")
model = PPO("MlpPolicy", env, verbose=1)   # MlpPolicy = a standard neural-net policy
model.learn(total_timesteps=50000)          # PPO handles the actor, critic, and clipping
# A few seconds of training later, the agent reliably balances the pole.
```

---

## The Payoff: How LLMs Like Claude Use Reinforcement Learning

Here's where everything you learned this week comes together. How do you turn a raw language model — which just predicts the next word — into a helpful, honest, harmless assistant? **Reinforcement learning.** Specifically, **RLHF: Reinforcement Learning from Human Feedback.**

Let's map RLHF onto the exact vocabulary from Day 1:

| RL concept (Day 1) | In RLHF / LLM training |
|--------------------|------------------------|
| **Agent** | The language model |
| **Environment** | The conversation / prompt |
| **State** | The prompt + text generated so far |
| **Action** | Choosing the next token (word/piece) |
| **Policy (π)** | The LLM itself — outputs a probability over next tokens |
| **Reward** | A **reward model** trained on human preferences |
| **Algorithm** | **PPO** (today's algorithm!) |

### How RLHF Works (Three Stages)

```
STAGE 1 — Pretraining:
   Train the model to predict the next word on tons of text.
   Result: fluent, but not necessarily helpful or safe.

STAGE 2 — Build a Reward Model:
   Show humans pairs of model answers; humans pick which is better.
   Train a separate "reward model" to predict human preference.
   → This reward model IS the reward signal for RL!

STAGE 3 — RL fine-tuning with PPO:
   The LLM (the AGENT) generates responses (sequences of ACTIONS).
   The reward model scores them (the REWARD).
   PPO nudges the policy to produce higher-scoring responses —
   carefully (proximally!), so the model improves without breaking
   its language abilities.
```

```
   Prompt ──► [ LLM = the agent/policy ] ──► Response (a sequence of token-actions)
                                                   │
                                                   ▼
                                        [ Reward Model ]  ← trained on human preferences
                                                   │
                                            reward score
                                                   │
                                                   ▼
                                    [ PPO updates the policy ]  ← today's algorithm!
                                  "make high-reward responses more likely,
                                   but stay proximal — don't break the model"
```

So when you chat with Claude and it gives a thoughtful, helpful, well-mannered answer — that helpfulness was **learned through reinforcement learning**, using the very PPO algorithm you just met, guided by a reward signal built from human preferences. The CartPole agent balancing a pole and Claude crafting a helpful reply are running on the *same family of ideas* you mastered this week.

> **A note on the frontier:** Newer techniques like **DPO** (Direct Preference Optimization) and **RLAIF** (RL from *AI* Feedback — using AI instead of humans to provide preferences) are refining this recipe, sometimes skipping the explicit RL step. But PPO-style RLHF is the foundation they all build on, and understanding it is understanding how modern assistants are aligned.

---

## The Whole Module in One Picture

```
Day 1: The loop          agent ↔ environment, state/action/reward, explore vs exploit
   │
Day 2: Q-Learning        learn a table of values, the Bellman equation, FrozenLake
   │
Day 3: DQN               replace the table with a neural net; replay + target network
   │
Day 4: Policy Gradients  learn the policy directly → REINFORCE → Actor-Critic → PPO
   │
   └──────────────────►  PPO powers RLHF, which aligns LLMs like Claude ✨
```

---

## Practice Exercises

### Exercise 1: Value-Based vs. Policy-Based
1. In your own words, what's the core difference between what a Q-network (DQN) outputs and what a policy network (REINFORCE) outputs?
2. Give one example of a problem where policy methods are clearly better than value methods, and explain why.
3. Why does the policy network in the code use a **Softmax** at the end? What would break if we removed it?

### Exercise 2: REINFORCE and Actor-Critic
1. Explain REINFORCE's main weakness (why is judging by total episode reward a "blunt instrument"?).
2. How does the **critic** in actor-critic give the actor better feedback than a single episode-wide reward?
3. Define "advantage" in one sentence — "how much better than expected" applied to what?

### Exercise 3: Run PPO
1. Install Stable-Baselines3 (`pip install stable-baselines3`) and run the PPO snippet on CartPole. How long does it take to solve compared to your hand-written REINFORCE?
2. Try PPO on a *harder* environment: change `"CartPole-v1"` to `"LunarLander-v3"` (you may need `pip install gymnasium[box2d]`). Increase `total_timesteps` to 200000. Does it learn to land the spacecraft?
3. After training, write a short loop that runs the trained `model` for one episode using `model.predict(obs)` and reports the total reward.

### Exercise 4: Map RLHF Yourself
Without looking back at the table, fill in the RL vocabulary for a chatbot being trained with RLHF:
1. What is the **agent**?
2. What is an **action**?
3. What is the **state**?
4. Where does the **reward** come from (and why not just use humans directly during every training step)?
5. Which algorithm from today does the updating? Why is its "proximal / clipped" nature especially important when fine-tuning a model that already speaks fluently?

### Exercise 5: Reflect on the Whole Module
Write a short paragraph (5-6 sentences) explaining reinforcement learning to a friend who knows nothing about AI. Use the puppy analogy, mention the explore-vs-exploit tradeoff, and end with the surprising fact that this is how the AI assistants they use are taught to be helpful.

---

## What's Next?

What a journey. You started the week watching a random agent flail in CartPole, and you're ending it understanding the algorithm that aligns the most advanced AI systems in the world. You learned:

- **Policy gradients** — learning the strategy directly instead of via values
- **REINFORCE** — try, reflect, reinforce the good
- **Actor-Critic** — pairing a doer with a smart evaluator for stable, efficient learning
- **PPO** — the careful, clipped workhorse behind modern RL
- **RLHF** — how PPO and human preferences turn a raw language model into a helpful assistant like Claude

You now understand both great branches of reinforcement learning, and — crucially — how RL connects to the generative AI you use every day. That connection is your bridge to what comes next.

➡️ **Up next: Module 15 — Generative AI.** RLHF was the *finishing* step that made language models helpful. Module 15 picks up the rest of the story: how generative models (large language models, diffusion image models, and more) are built, how they actually generate text and images, and how the pieces — including the reinforcement learning you just mastered — fit together to create the AI revolution happening right now.

You've completed an advanced module that most people never touch. Take a moment to appreciate how far you've come — from "what is a reward?" to "how is Claude trained?" in four days. That's genuinely impressive. Onward to Module 15!
