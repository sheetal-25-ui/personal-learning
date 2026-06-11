# Module 14: Reinforcement Learning — How AI Learns by Doing

## Teach a Machine to Learn From Experience (Just Like You Did)

Remember learning to ride a bicycle? Nobody handed you a labeled dataset of "correct handlebar angles." You wobbled, you fell, you adjusted, and eventually you flew down the street. That trial-and-error-with-feedback loop **is** reinforcement learning. This is the branch of AI behind game-playing champions, walking robots, and — surprisingly — the politeness and helpfulness of ChatGPT and Claude.

This is an **advanced** module. You'll get the most out of it if you've already met supervised and unsupervised learning (Module 1, Day 3) and have some comfort with Python and neural networks (Modules 2, 3, 6). But don't worry — we'll build every idea up gently, with analogies, diagrams, and runnable code.

## 4 Days

| Day | Topic | What You'll Learn |
|-----|-------|-------------------|
| **Day 01** | RL Fundamentals | Agents, environments, states, actions, rewards, the RL loop, policies, and the famous exploration-vs-exploitation tradeoff — using Gymnasium's CartPole |
| **Day 02** | Q-Learning | Value functions, the Q-table, the Bellman equation (explained simply), and a complete runnable Q-learning agent that solves FrozenLake |
| **Day 03** | Deep Q-Networks (DQN) | Why tables stop working, using a neural network to approximate Q-values, experience replay, target networks, and a DQN code sketch |
| **Day 04** | Policy Gradients & Modern RL | REINFORCE, actor-critic, PPO (the algorithm behind RLHF!), and how today's large language models are trained with reinforcement learning |

## What You'll Need

```bash
pip install gymnasium numpy matplotlib
# For Day 03 (optional but recommended):
pip install torch
```

> **Note:** Gymnasium is the modern, maintained successor to OpenAI Gym. If you see old tutorials using `import gym`, the new way is `import gymnasium as gym`. The ideas are identical.

## After This Module

- You'll understand the core vocabulary of RL — and be able to explain it to a friend over chai
- You'll have trained an agent **from scratch** that learns to balance a pole and cross a frozen lake
- You'll understand DQN, the algorithm that taught AI to play Atari games at superhuman level
- You'll understand PPO and RLHF — the secret sauce that turns a raw language model into a helpful assistant
- You'll be ready for **Module 15: Generative AI**, where RLHF becomes a key character in the story of how modern AI is built

## The Big Picture

```
Supervised Learning:    "Here is the right answer. Copy it."
Unsupervised Learning:  "Here is data. Find the hidden structure."
Reinforcement Learning: "Here is a world. Go figure out what works."  ← YOU ARE HERE
```

RL is the type of learning most like how humans and animals actually learn. It's harder than the others — there's no answer key, feedback is delayed and noisy, and the agent shapes its own training data by the actions it takes. But when it works, it's magic.

Let's begin.
