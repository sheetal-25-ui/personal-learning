# Module 14 — Day 1: RL Fundamentals

## Learning Like a Puppy

Imagine training a puppy. You don't hand it a textbook. Instead:

- The puppy **does something** (sits, barks, chews a slipper)
- You **react** (treat, praise, or a firm "No!")
- Over time, the puppy learns which behaviors earn treats

That's reinforcement learning in one sentence: **an agent learns to take actions in an environment to maximize reward over time.**

No answer key. No labeled examples. Just try things, see what happens, and get better. By the end of today you'll understand every piece of that loop — and you'll watch a real agent interact with a real environment in code.

---

## The 5 Core Ingredients

Every RL problem, from a puppy to AlphaGo, is built from the same five pieces:

| Piece | Puppy Analogy | CartPole Example | Symbol |
|-------|--------------|------------------|--------|
| **Agent** | The puppy | The thing controlling the cart | — |
| **Environment** | The living room | The cart-and-pole physics world | — |
| **State** | What the puppy senses | Cart position, velocity, pole angle, pole speed | `s` |
| **Action** | What the puppy does | Push cart left or right | `a` |
| **Reward** | Treat or scolding | +1 for every step the pole stays up | `r` |

Let me say each one slowly, because everything else builds on these.

### Agent
The **agent** is the learner and decision-maker. It's the "brain" — the part that looks at the situation and chooses what to do. In our code, the agent is whatever logic decides "left" or "right."

### Environment
The **environment** is everything outside the agent — the world the agent lives in. The environment receives the agent's action, updates itself according to its rules (physics, game logic, etc.), and hands back a new state and a reward. The agent does **not** control the environment's rules; it can only act and observe the consequences.

### State (also called Observation)
The **state** is a snapshot of the situation right now — the information the agent uses to decide. For CartPole, the state is just 4 numbers:

```
[ cart position, cart velocity, pole angle, pole angular velocity ]
  e.g. [ 0.02, -0.14, 0.03, 0.27 ]
```

### Action
The **action** is the move the agent makes. CartPole has just 2 possible actions:

```
0 = push the cart LEFT
1 = push the cart RIGHT
```

### Reward
The **reward** is a single number that tells the agent how good the last step was. It's the *only* signal guiding learning. In CartPole, the reward is **+1 for every timestep the pole hasn't fallen over.** So the agent's goal — survive as long as possible — emerges naturally from "collect as much reward as you can."

> **Key insight:** The agent doesn't know *why* a reward is good or bad. It just gets a number and tries to get more of it over time. Designing the right reward is one of the hardest and most important parts of RL.

---

## The RL Loop — The Heartbeat of Everything

Here's the loop that repeats over and over, thousands or millions of times:

```
        ┌─────────────────────────────────────────────┐
        │                                               │
        │              ┌───────────┐                    │
        │   state s    │           │   action a         │
        └────────────► │   AGENT   │ ──────────────┐    │
                       │  (brain)  │               │    │
                       └───────────┘               ▼    │
                                              ┌──────────────┐
                       ┌─────────────────────│  ENVIRONMENT │
                       │   reward r,          │   (world)    │
                       │   next state s'      └──────────────┘
                       │
                       └──► agent observes, learns, repeats
```

Step by step:

1. The agent observes the current **state** `s`.
2. The agent picks an **action** `a`.
3. The environment processes `a` and returns a **reward** `r` and the **next state** `s'`.
4. The agent (optionally) **learns** from `(s, a, r, s')`.
5. `s'` becomes the new `s`. Go back to step 1.

That's it. That loop is the engine of all reinforcement learning.

---

## Episodes — One Complete Attempt

An **episode** is one full run from start to finish:

- **Start:** the environment resets to a beginning state
- **Middle:** the loop runs — act, observe, act, observe...
- **End (terminal state):** something happens that ends the episode

In CartPole, an episode ends when:
- The pole tips more than ~12 degrees, OR
- The cart drifts off the edge of the track, OR
- The agent survives 500 steps (a win!)

Then we **reset** and start a fresh episode. Learning happens *across* many episodes — early episodes are clumsy (pole falls in 10 steps), later episodes are skilled (pole balances for 500 steps).

```
Episode 1:   ||  (pole falls after 11 steps — total reward 11)
Episode 50:  |||||||  (getting better — total reward 47)
Episode 200: ||||||||||||||||||||  (skilled! — total reward 500, max!)
```

---

## Policy — The Agent's Strategy

The **policy** (written `π`, the Greek letter "pi") is the agent's brain — the rule that maps a state to an action.

> **Policy = "Given what I see, what do I do?"**

A policy can be:
- **Simple:** "If the pole is leaning right, push right." (a hand-written rule)
- **A lookup table:** for every possible state, store the best action
- **A neural network:** feed in the state, get out the action (we'll do this in Days 3-4)

The entire goal of RL is to **find a good policy** — one that earns lots of reward. When people say "the model learned to play the game," they mean "the policy improved."

---

## Exploration vs. Exploitation — The Central Dilemma

Here's the tension that makes RL genuinely hard and genuinely interesting.

Imagine you move to a new city with 100 restaurants:

- **Exploitation** = go to the best restaurant you've found *so far*. Safe, reliably good.
- **Exploration** = try a *new* restaurant you've never visited. Risky — might be amazing, might be terrible.

If you **only exploit**, you might eat at a "pretty good" place forever and never discover the incredible biryani two streets over. If you **only explore**, you waste every meal on random gambles and never enjoy the great places you found.

The art is **balancing** the two.

| Strategy | Behavior | Problem |
|----------|----------|---------|
| Pure exploitation | Always pick best-known action | Gets stuck on "good enough," misses better options |
| Pure exploration | Always pick a random action | Never uses what it learned, performs terribly |
| **Balanced** | Mostly exploit, sometimes explore | The winning recipe |

### The ε-greedy Strategy (Epsilon-Greedy)

The simplest, most popular way to balance them. Pick a small number `ε` (epsilon), say 0.1:

```
With probability ε (10%):   take a RANDOM action   (explore)
With probability 1-ε (90%): take the BEST action   (exploit)
```

A common trick is **decay**: start with high ε (explore a lot early, when you know nothing) and slowly lower it (exploit more later, once you've learned). We'll use exactly this in Day 2.

```
Early training:   ε = 1.0  → 100% random (pure exploration, "I know nothing")
Mid training:     ε = 0.3  → 30% random
Late training:    ε = 0.05 → 5% random  (mostly using what I learned)
```

---

## Let's Meet CartPole — Our Running Example

CartPole is the "Hello, World!" of reinforcement learning. A pole is balanced on a cart; you slide the cart left or right to keep the pole upright. Simple to describe, surprisingly tricky to master.

```
        |   ← pole (don't let it fall!)
        |
      ┌─────┐
      │cart │   ← you can push this left or right
      └──○─○──┘
   ═══════════════  ← track
```

### Code: Watch a Random Agent Play

Let's start by watching a **random** agent — one whose policy is "pick left or right by coin flip." It will be terrible, and that's the point. This shows you the RL loop in real code.

```python
import gymnasium as gym

# Create the CartPole environment
env = gym.make("CartPole-v1")

# --- Inspect what we're working with ---
print("State (observation) space:", env.observation_space)
# Box(4,) → 4 continuous numbers: position, velocity, angle, angular velocity
print("Action space:", env.action_space)
# Discrete(2) → 2 actions: 0 (push left), 1 (push right)

# --- Run ONE episode with a random policy ---
state, info = env.reset(seed=42)   # reset() returns the starting state
total_reward = 0
done = False
step_count = 0

while not done:
    # The RANDOM policy: just pick an action uniformly at random
    action = env.action_space.sample()

    # Take the action — this is the core RL loop step!
    next_state, reward, terminated, truncated, info = env.step(action)

    # 'terminated' = pole fell / cart off track (real failure)
    # 'truncated'  = hit the 500-step time limit (a win, in this case)
    done = terminated or truncated

    total_reward += reward
    step_count += 1
    state = next_state   # the next state becomes the current state

print(f"Episode finished after {step_count} steps.")
print(f"Total reward: {total_reward}")
# A random agent typically lasts only ~10-30 steps. Pathetic! But now you've
# seen the full loop: reset → act → observe → repeat → done.

env.close()
```

### Understanding `env.step()` — The Most Important Line

That one line, `env.step(action)`, *is* the environment doing its job. It returns 5 things:

| Returned value | Meaning |
|----------------|---------|
| `next_state` | The new state after your action |
| `reward` | The reward for this step (+1 in CartPole) |
| `terminated` | `True` if the episode ended by failure/success condition |
| `truncated` | `True` if it ended by a time/step limit |
| `info` | Extra debugging data (usually ignore it) |

### Code: Run Many Episodes and See the Average

```python
import gymnasium as gym

env = gym.make("CartPole-v1")
num_episodes = 20            # 20 episodes is enough to see the average behavior
episode_rewards = []

for episode in range(num_episodes):
    state, info = env.reset()
    total_reward = 0
    done = False

    while not done:
        action = env.action_space.sample()   # still a random policy
        state, reward, terminated, truncated, info = env.step(action)
        done = terminated or truncated
        total_reward += reward

    episode_rewards.append(total_reward)
    print(f"Episode {episode + 1:2d}: reward = {total_reward}")

avg = sum(episode_rewards) / len(episode_rewards)
print(f"\nAverage reward over {num_episodes} episodes: {avg:.1f}")
print("(A random agent scores ~22. A *solved* CartPole scores ~475+.)")

env.close()
```

The random agent stays around an average of ~22. Over the next three days, we'll build agents that *learn* and crush this number.

---

## A Tiny Hand-Coded "Smart" Policy (Just for Intuition)

Before we do any real learning, here's a non-learning policy that already beats random — to prove that a *good policy* is all you need. The rule: **push in the direction the pole is leaning.**

```python
import gymnasium as gym

env = gym.make("CartPole-v1")

def simple_policy(state):
    # state[2] is the pole ANGLE. Positive = leaning right.
    pole_angle = state[2]
    # If leaning right (positive angle), push right (action 1) to catch it.
    # If leaning left (negative angle), push left (action 0).
    return 1 if pole_angle > 0 else 0

state, info = env.reset(seed=0)
total_reward = 0
done = False

while not done:
    action = simple_policy(state)          # use our smart rule, not random
    state, reward, terminated, truncated, info = env.step(action)
    done = terminated or truncated
    total_reward += reward

print(f"Smart hand-coded policy total reward: {total_reward}")
# This naive rule already does noticeably better than random — proof that
# the policy is what matters. RL's job is to DISCOVER good policies automatically.

env.close()
```

Notice: we *wrote* this policy by hand. The whole promise of RL is that the agent **discovers** policies like this (and far better ones) on its own, just from reward signals. That's what we'll build starting tomorrow.

---

## Vocabulary Cheat-Sheet

| Term | Plain meaning |
|------|---------------|
| **Agent** | The learner / decision-maker |
| **Environment** | The world the agent acts in |
| **State (`s`)** | Snapshot of the current situation |
| **Action (`a`)** | A move the agent can make |
| **Reward (`r`)** | A number scoring the last action |
| **Episode** | One full run from start to terminal state |
| **Policy (`π`)** | The strategy mapping states → actions |
| **Return** | The *total* reward collected over an episode |
| **Exploration** | Trying new actions to discover what works |
| **Exploitation** | Using the best-known action |
| **ε-greedy** | Mostly exploit, occasionally explore at random |

---

## Practice Exercises

### Exercise 1: Identify the Ingredients
For each scenario, name the **agent, environment, state, action,** and **reward**:

1. A self-driving car navigating Mumbai traffic
2. An AI learning to play the mobile game Subway Surfers
3. A thermostat keeping a room at 24°C
4. A trading bot buying and selling stocks
5. A robot vacuum cleaning your living room

### Exercise 2: Explore vs. Exploit in Real Life
1. Describe a decision **you** made this week that was "exploitation" (sticking with a known good choice).
2. Describe one that was "exploration" (trying something new).
3. If you set `ε = 0.1`, out of 100 restaurant visits, roughly how many would be brand-new restaurants?
4. Why is it smart to start with high ε and decrease it over time? Explain in one or two sentences.

### Exercise 3: Run and Modify the Code
1. Run the "random agent" code. What average reward do you get over 20 episodes?
2. Run the "smart hand-coded policy." How much better is it than random?
3. **Improve the hand-coded policy:** modify `simple_policy` to also consider the pole's *angular velocity* (`state[3]`). Hint: if the pole is leaning right *and* falling further right, you definitely want to push right. Try combining `state[2]` and `state[3]`. Can you get a higher score?
4. Change `gym.make("CartPole-v1")` to `gym.make("CartPole-v1", render_mode="human")` and run a single episode. You'll see a window pop up showing the cart and pole live! (Close it when done.)

### Exercise 4: Design a Reward
You want to train an agent to make tea. List 3 things you'd give **positive** reward for and 2 things you'd give **negative** reward for. Then explain: what might go wrong if your only reward was "+1 when a cup of tea exists"? (Think about shortcuts the agent might take — this is called *reward hacking*.)

---

## What's Next?

Today you learned the language of reinforcement learning: agents, environments, states, actions, rewards, the loop, episodes, policies, and the all-important exploration-vs-exploitation tradeoff. You even ran real agents in a real environment.

Tomorrow in **Day 2: Q-Learning**, we go from *watching* agents to *teaching* them. You'll learn:
- **Value functions** — how to measure "how good is this state?"
- The **Q-table** — a cheat-sheet of the best action for every situation
- The **Bellman equation** — the beautiful little formula at the heart of RL (explained without scary math)
- A **complete, runnable agent** that learns to cross a frozen lake all by itself

You've just taken your first real step into how machines learn from experience. This is the same family of ideas that powers game-playing champions and the AI assistants you use every day. Fantastic start — see you tomorrow!
