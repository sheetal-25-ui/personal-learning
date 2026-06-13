# Week 2: HashMap & Two Pointers - Deep Concept Guide

---

## PART 1: HASHMAP (Most Important DS for Interviews!)

---

### 1. What is a HashMap?

**Plain English:**
A HashMap is a data structure that stores KEY-VALUE pairs and lets you look
up any value by its key in O(1) average time.

**Real-Life Analogy #1: A Dictionary**
You look up a WORD (key) and get its MEANING (value). You do not read every
page from the beginning. You know roughly where the word is because
dictionaries are organized. A HashMap works similarly but even faster -- it
uses a mathematical trick (hashing) to jump directly to the right location.

**Real-Life Analogy #2: Aadhaar Card System**
India has 1.4 billion people. When you enter your 12-digit Aadhaar number at
a bank or government office, they do not search through 1.4 billion records
one by one. Your Aadhaar number is processed through a formula that tells the
computer EXACTLY where your record is stored. That formula is called a
**hash function**.

```
  Key (Aadhaar)       Hash Function       Bucket (Storage Location)
  +-----------+       +----------+        +-------------------+
  | 8765-4321 | ----> | f(x)     | -----> | Bucket 5:         |
  +-----------+       +----------+        | Name: Sheetal     |
                                          | DOB: 15-Mar-1990  |
                                          +-------------------+
```

**Why is HashMap so important for interviews?**
Because almost every "optimize from O(n^2) to O(n)" problem uses a HashMap.
If you are stuck on a LeetCode problem, the first question to ask yourself
is: "Can a HashMap help me here?" The answer is yes about 40% of the time.

---

### 2. Why Does HashMap Exist?

**The Problem:**
You have data and need to look up values by a key. What are your options?

| Data Structure | Lookup Time | Notes                              |
|----------------|-------------|-------------------------------------|
| Unsorted Array | O(n)       | Must scan every element              |
| Sorted Array   | O(log n)   | Binary search, but insert is O(n)    |
| HashMap        | O(1) avg   | Instant lookup! Insert also O(1)     |

HashMap gives you the BEST of both worlds: fast lookup AND fast insertion.
The tradeoff? It uses more memory and elements are NOT ordered.

---

### 3. How Hashing Works Internally (INTERVIEW FAVORITE!)

**Step 1: The Hash Function**

A hash function takes a key and converts it to an integer (the hash code).

```java
String key = "Sheetal";
int hashCode = key.hashCode();  // returns some integer like 2069541517
```

**Step 2: Mapping Hash Code to Bucket Index**

The HashMap has an internal array of "buckets" (default size 16 in Java).
The hash code is mapped to a bucket index using modulo:

```
bucketIndex = hashCode % numberOfBuckets

Example:
  hashCode("Sheetal") = 2069541517
  bucketIndex = 2069541517 % 16 = 13
  -> Store Sheetal's data in bucket 13
```

**Step 3: Store the Key-Value Pair in That Bucket**

```
Internal bucket array (size 16):

Index:  0    1    2    3    4    5    6    7
      +----+----+----+----+----+----+----+----+
      |null|null|null|null|null|null|null|null|
      +----+----+----+----+----+----+----+----+

Index:  8    9   10   11   12   13   14   15
      +----+----+----+----+----+----+----+----+
      |null|null|null|null|null| KV |null|null|
      +----+----+----+----+----+----+----+----+
                                 ^
                                 |
                          "Sheetal" -> "Engineer"
                          stored at bucket 13
```

**Step 4: Retrieval**

When you call `map.get("Sheetal")`:
1. Compute hashCode("Sheetal") = 2069541517
2. Compute bucket index = 2069541517 % 16 = 13
3. Go directly to bucket 13
4. Return the value "Engineer"

No searching through all entries. Just math. That is why it is O(1).

---

### 4. Collision Handling

**What is a Collision?**
Two different keys get the SAME bucket index.

```
hashCode("Sheetal") % 16 = 13
hashCode("Rahul")   % 16 = 13   <- COLLISION! Both map to bucket 13
```

This is inevitable. With 16 buckets and thousands of keys, many keys will
share the same bucket. Java handles this using **Separate Chaining**.

**Separate Chaining:**
Each bucket holds a linked list. When multiple keys map to the same bucket,
they form a chain.

```
Bucket 13:
  +-------------------+    +-------------------+    +-----------------+
  | Key: "Sheetal"     | -> | Key: "Rahul"      | -> | Key: "Priya"   |
  | Val: "Engineer"    |    | Val: "Designer"   |    | Val: "Manager" |
  +-------------------+    +-------------------+    +-----------------+

To find "Rahul":
  1. Go to bucket 13              (O(1))
  2. Walk the chain: Sheetal? No. (O(1))
     Rahul? Yes!                  (O(1))
  3. Return "Designer"

Worst case: ALL keys in same bucket -> chain of length n -> O(n) lookup
```

**Java 8+ Optimization: Treeification**
When a chain grows longer than 8 elements, Java converts it from a linked
list to a balanced binary tree (Red-Black tree). This improves worst-case
lookup from O(n) to O(log n).

```
Before (chain > 8):
  Bucket 5: A -> B -> C -> D -> E -> F -> G -> H -> I -> J
  Lookup: O(n) -- must walk the chain

After treeification:
  Bucket 5:
              E
            /   \
           C     H
          / \   / \
         B   D F   I
        /       \   \
       A         G   J

  Lookup: O(log n) -- binary tree search
```

---

### 5. HashMap Operations and Time Complexity

| Operation          | Average | Worst  | What it does                       |
|--------------------|---------|--------|------------------------------------|
| put(key, value)    | O(1)   | O(n)   | Add or update a key-value pair     |
| get(key)           | O(1)   | O(n)   | Retrieve value by key              |
| containsKey(key)   | O(1)   | O(n)   | Check if key exists                |
| containsValue(val) | O(n)   | O(n)   | Check if value exists (slow!)      |
| remove(key)        | O(1)   | O(n)   | Remove a key-value pair            |
| size()             | O(1)   | O(1)   | Number of entries                  |
| keySet()           | O(n)   | O(n)   | Get all keys                       |
| values()           | O(n)   | O(n)   | Get all values                     |
| entrySet()         | O(n)   | O(n)   | Get all key-value pairs            |

**Note:** Worst case O(n) happens when ALL keys collide into the same bucket.
In practice, with a good hash function, collisions are rare and operations
are effectively O(1).

**Load Factor and Resizing:**
- Default initial capacity: 16 buckets
- Default load factor: 0.75
- When 75% of buckets are filled, the HashMap DOUBLES in size (rehashes all entries)
- This is similar to how ArrayList resizes, but more expensive because every
  key must be rehashed to find its new bucket position

---

### 6. Java HashMap Usage

```java
import java.util.HashMap;
import java.util.Map;

// ===== CREATION =====
HashMap<String, Integer> map = new HashMap<>();

// Better practice: declare as Map interface
Map<String, Integer> ages = new HashMap<>();


// ===== ADDING ENTRIES =====
ages.put("Sheetal", 25);
ages.put("Rahul", 30);
ages.put("Priya", 28);
// If key already exists, put() OVERWRITES the old value:
ages.put("Sheetal", 26);  // Sheetal's age updated to 26


// ===== RETRIEVING =====
int age = ages.get("Sheetal");         // 26
Integer val = ages.get("Unknown");     // null (key not found)

// Safe retrieval with default value:
int safe = ages.getOrDefault("Unknown", -1);  // -1 (key not found, return default)


// ===== CHECKING =====
boolean hasSheetal = ages.containsKey("Sheetal");   // true
boolean hasAge30   = ages.containsValue(30);         // true (but O(n)!)


// ===== REMOVING =====
ages.remove("Rahul");     // removes Rahul
ages.remove("Priya", 28); // removes only if Priya's value is 28


// ===== SIZE AND EMPTY =====
int size = ages.size();        // number of entries
boolean empty = ages.isEmpty(); // true if no entries


// ===== ITERATING (3 WAYS) =====

// Way 1: Iterate over keys
for (String name : ages.keySet()) {
    System.out.println(name + " -> " + ages.get(name));
}

// Way 2: Iterate over values (when you do not need keys)
for (int a : ages.values()) {
    System.out.println(a);
}

// Way 3: Iterate over entries (MOST EFFICIENT -- no extra get() call)
for (Map.Entry<String, Integer> entry : ages.entrySet()) {
    System.out.println(entry.getKey() + " -> " + entry.getValue());
}


// ===== USEFUL METHODS FOR DSA =====

// Merge: add to existing value or set if absent
ages.merge("Sheetal", 1, Integer::sum);
// If "Sheetal" exists: new value = old value + 1
// If "Sheetal" absent: value = 1

// putIfAbsent: only add if key does not exist
ages.putIfAbsent("NewPerson", 20);

// computeIfAbsent: useful for building lists/sets as values
Map<String, List<Integer>> groups = new HashMap<>();
groups.computeIfAbsent("even", k -> new ArrayList<>()).add(2);
groups.computeIfAbsent("even", k -> new ArrayList<>()).add(4);
// groups = {"even": [2, 4]}
```

---

### 7. HashMap vs HashSet vs TreeMap vs LinkedHashMap

| Type           | Stores         | Order            | Null Keys | Time     |
|----------------|----------------|------------------|-----------|----------|
| HashMap        | Key-Value pairs| No order         | 1 null    | O(1) avg |
| LinkedHashMap  | Key-Value pairs| Insertion order  | 1 null    | O(1) avg |
| TreeMap        | Key-Value pairs| Sorted by key    | No null   | O(log n) |
| HashSet        | Keys only      | No order         | 1 null    | O(1) avg |
| LinkedHashSet  | Keys only      | Insertion order  | 1 null    | O(1) avg |
| TreeSet        | Keys only      | Sorted           | No null   | O(log n) |

**When to use which:**

```
Need key-value? Use HashMap (most common)
Need only "does this exist?" Use HashSet
Need sorted keys? Use TreeMap
Need insertion order? Use LinkedHashMap
Need sorted unique elements? Use TreeSet
```

**HashSet is just a HashMap with dummy values:**
```java
HashSet<String> set = new HashSet<>();
set.add("apple");      // internally: map.put("apple", PRESENT)
set.contains("apple"); // internally: map.containsKey("apple")
```

---

### 8. Frequency Map Pattern (MOST USED PATTERN!)

**Counting occurrences using HashMap:**

```java
// Template: Count frequency of each element
public Map<Integer, Integer> buildFrequencyMap(int[] nums) {
    Map<Integer, Integer> freq = new HashMap<>();
    for (int num : nums) {
        freq.put(num, freq.getOrDefault(num, 0) + 1);
        // or: freq.merge(num, 1, Integer::sum);
    }
    return freq;
}

// Example:
// nums = [1, 3, 2, 1, 3, 1]
// freq = {1: 3, 3: 2, 2: 1}
```

**Common uses of frequency maps:**
1. Find majority element (appears more than n/2 times)
2. Find first unique character
3. Check if two strings are anagrams
4. Group anagrams together
5. Find top K frequent elements

**Example: Check if Two Strings are Anagrams**

```java
// Anagram: same characters, different order
// "listen" and "silent" are anagrams

public boolean isAnagram(String s, String t) {
    if (s.length() != t.length()) return false;

    int[] freq = new int[26];  // for lowercase letters only

    for (int i = 0; i < s.length(); i++) {
        freq[s.charAt(i) - 'a']++;   // increment for s
        freq[t.charAt(i) - 'a']--;   // decrement for t
    }

    for (int count : freq) {
        if (count != 0) return false; // if any count is not zero, not anagram
    }
    return true;
}
```

---

## PART 2: TWO POINTERS TECHNIQUE

---

### 1. What is the Two Pointer Technique?

**Plain English:**
Use two indices (pointers) that move through the data structure based on
conditions. Instead of checking every pair with two nested loops O(n^2),
the pointers move intelligently to find the answer in O(n).

**Real-Life Analogy:**
Imagine a long bookshelf sorted alphabetically. You need to find two books
whose combined price equals exactly Rs. 500. Two people search:
- Person A starts from the LEFT (cheapest books)
- Person B starts from the RIGHT (most expensive books)

They call out their prices:
- "Rs. 100 + Rs. 600 = Rs. 700. Too much!" -> Person B moves LEFT (cheaper)
- "Rs. 100 + Rs. 450 = Rs. 550. Still too much!" -> Person B moves LEFT
- "Rs. 100 + Rs. 400 = Rs. 500. FOUND IT!"

They never need to check every possible pair. They narrow down intelligently.
That is Two Pointers.

---

### 2. Types of Two Pointers

#### Type 1: Opposite Direction (Converging Pointers)

**Setup:** One pointer at the START, one at the END. They move TOWARD each other.
**Use when:** Array is SORTED + you need to find a pair.

```
        left                        right
         |                            |
         v                            v
       [1,  2,  4,  6,  8,  10,  12]
        ^                          ^
        |__________________________|
              They move inward
```

**Classic Example: Two Sum II (sorted array)**

```java
// Find two numbers in sorted array that sum to target
public int[] twoSum(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;

    while (left < right) {
        int sum = nums[left] + nums[right];

        if (sum == target) {
            return new int[]{left, right};  // found the pair
        } else if (sum < target) {
            left++;   // sum too small, need bigger number -> move left RIGHT
        } else {
            right--;  // sum too big, need smaller number -> move right LEFT
        }
    }
    return new int[]{};  // no pair found
}
```

**Step-by-step trace:**

```
Array: [2, 7, 11, 15],  Target: 9

Step 1: left=0, right=3
  sum = 2 + 15 = 17 > 9
  Move right LEFT: right = 2

Step 2: left=0, right=2
  sum = 2 + 11 = 13 > 9
  Move right LEFT: right = 1

Step 3: left=0, right=1
  sum = 2 + 7 = 9 == 9
  FOUND! Return [0, 1]
```

**Why does this work?**
- Array is sorted. nums[left] is the smallest available, nums[right] is largest.
- If sum > target: making left bigger would only increase sum more. So decrease
  right instead.
- If sum < target: making right smaller would only decrease sum more. So
  increase left instead.
- This guarantees we never miss a valid pair while only taking O(n) steps.

**Other problems using converging pointers:**
- Container With Most Water
- Trapping Rain Water
- Valid Palindrome
- 3Sum (uses two pointers inside a loop)

---

#### Type 2: Same Direction (Fast-Slow Pointers)

**Setup:** Both pointers start at the BEGINNING. One moves faster than the other.
**Use when:** Modify array in-place, remove elements, or detect patterns.

```
      slow  fast
       |     |
       v     v
      [0, 1, 0, 3, 12, 0, 5]
       ^  ^
       |  |
       Both move right, fast goes ahead
```

**Classic Example: Remove Duplicates from Sorted Array**

```java
// Remove duplicates in-place, return new length
// Input:  [1, 1, 2, 2, 3]
// Output: [1, 2, 3, _, _], return 3

public int removeDuplicates(int[] nums) {
    if (nums.length == 0) return 0;

    int slow = 0;  // points to the last unique element placed

    for (int fast = 1; fast < nums.length; fast++) {
        if (nums[fast] != nums[slow]) {
            // found a new unique element
            slow++;
            nums[slow] = nums[fast];  // place it next to the last unique
        }
        // if equal, just skip (fast moves forward, slow stays)
    }

    return slow + 1;  // number of unique elements
}
```

**Step-by-step trace:**

```
Array: [1, 1, 2, 2, 3]

Initial: slow=0, fast=1
  nums[1]=1 == nums[0]=1 -> skip (duplicates)

fast=2:
  nums[2]=2 != nums[0]=1 -> new unique!
  slow=1, nums[1]=2
  Array: [1, 2, 2, 2, 3]

fast=3:
  nums[3]=2 == nums[1]=2 -> skip

fast=4:
  nums[4]=3 != nums[1]=2 -> new unique!
  slow=2, nums[2]=3
  Array: [1, 2, 3, 2, 3]

Return slow+1 = 3 (first 3 elements are the answer)
```

**Other problems using same-direction pointers:**
- Move Zeroes
- Remove Element
- Sort Colors (Dutch National Flag -- uses 3 pointers)

---

#### Type 3: Fast-Slow Pointers (Linked List Specific)

**Setup:** Both start at head. Fast moves 2 steps, slow moves 1 step.
**Use when:** Find middle of linked list, detect cycles.

```
Start:
  slow -> [1] -> [2] -> [3] -> [4] -> [5] -> null
  fast -> [1]

After 1 iteration:
  slow at [2], fast at [3]

After 2 iterations:
  slow at [3], fast at [5]

Fast reaches end. Slow is at the MIDDLE!
```

We will cover this more in Week 4 (Linked Lists).

---

### 3. When to Use Two Pointers (Pattern Recognition)

| Trigger Words in Problem | Two Pointer Type | Examples |
|--------------------------|-----------------|----------|
| Sorted array + find pair | Opposite direction | Two Sum II, 3Sum |
| Palindrome check | Opposite direction | Valid Palindrome |
| Container/water problems | Opposite direction | Container With Most Water |
| Remove/modify IN PLACE | Same direction | Remove Duplicates, Move Zeroes |
| Partition array | Same direction | Sort Colors, Partition |
| Linked list middle/cycle | Fast-slow | Middle of Linked List, Cycle Detection |
| Merge two sorted arrays | Two pointers, one per array | Merge Sorted Array |

**Decision Flow:**

```
Is the array/string SORTED?
  |
  +--YES--> Do you need to find a PAIR?
  |           |
  |           +--YES--> Opposite direction two pointers
  |           |
  |           +--NO---> Consider binary search
  |
  +--NO---> Do you need to modify IN PLACE?
              |
              +--YES--> Same direction (fast-slow)
              |
              +--NO---> Consider HashMap or Sliding Window
```

---

### 4. Three Sum (Combining Sorting + Two Pointers)

**Problem:** Find all unique triplets that sum to zero.

This is one of the most asked interview problems. It combines sorting with
two pointers brilliantly.

**Approach:**
1. Sort the array
2. For each element i, use two pointers to find pairs that sum to -nums[i]
3. Skip duplicates to avoid duplicate triplets

```java
public List<List<Integer>> threeSum(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    Arrays.sort(nums);  // sort first!

    for (int i = 0; i < nums.length - 2; i++) {
        // Skip duplicate values for i
        if (i > 0 && nums[i] == nums[i - 1]) continue;

        int left = i + 1;
        int right = nums.length - 1;
        int target = -nums[i];  // we need two numbers that sum to this

        while (left < right) {
            int sum = nums[left] + nums[right];

            if (sum == target) {
                result.add(Arrays.asList(nums[i], nums[left], nums[right]));

                // Skip duplicates for left and right
                while (left < right && nums[left] == nums[left + 1]) left++;
                while (left < right && nums[right] == nums[right - 1]) right--;

                left++;
                right--;
            } else if (sum < target) {
                left++;
            } else {
                right--;
            }
        }
    }
    return result;
}
```

**Trace with [-1, 0, 1, 2, -1, -4]:**

```
After sorting: [-4, -1, -1, 0, 1, 2]

i=0: nums[i]=-4, target=4
  left=1(-1), right=5(2): sum=1 < 4, left++
  left=2(-1), right=5(2): sum=1 < 4, left++
  left=3(0),  right=5(2): sum=2 < 4, left++
  left=4(1),  right=5(2): sum=3 < 4, left++
  left=5, right=5: left >= right, stop

i=1: nums[i]=-1, target=1
  left=2(-1), right=5(2): sum=1 == 1 -> FOUND [-1, -1, 2]
  Skip dups, left=3, right=4
  left=3(0), right=4(1): sum=1 == 1 -> FOUND [-1, 0, 1]
  left=4, right=3: left >= right, stop

i=2: nums[i]=-1, SKIP (same as nums[1])

i=3: nums[i]=0, target=0
  left=4(1), right=5(2): sum=3 > 0, right--
  left=4, right=4: left >= right, stop

Result: [[-1, -1, 2], [-1, 0, 1]]
```

**Time: O(n^2)** -- outer loop O(n) x inner two pointers O(n).
**Space: O(1)** -- excluding the output list. Sorting is in-place.

---

### 5. HashMap + Two Pointers Combined Patterns

**Pattern: Two Sum (Unsorted Array) -- HashMap Approach**

When the array is NOT sorted, two pointers do not work directly. Use HashMap.

```java
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>();
    // key = number, value = its index

    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];

        if (seen.containsKey(complement)) {
            return new int[]{seen.get(complement), i};
        }

        seen.put(nums[i], i);  // store current number and its index
    }
    return new int[]{};
}
```

**Trace with nums=[2, 7, 11, 15], target=9:**

```
i=0: num=2, complement=7, seen={}. 7 not in seen. Add {2:0}
i=1: num=7, complement=2, seen={2:0}. 2 IS in seen! Return [0, 1]
```

**Time: O(n)**. One pass through the array.
**Space: O(n)**. HashMap stores up to n entries.

---

### 6. Group Anagrams (HashMap + Sorting)

**Problem:** Group strings that are anagrams of each other.
Input: ["eat", "tea", "tan", "ate", "nat", "bat"]
Output: [["eat","tea","ate"], ["tan","nat"], ["bat"]]

**Key Insight:** Anagrams have the SAME sorted characters.
"eat" sorted = "aet"
"tea" sorted = "aet"  <- same!
"tan" sorted = "ant"
"nat" sorted = "ant"  <- same!

Use sorted version as the HashMap key.

```java
public List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> map = new HashMap<>();

    for (String s : strs) {
        // Sort the characters to create a "signature"
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        String key = new String(chars);

        // Group all strings with same signature
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
    }

    return new ArrayList<>(map.values());
}
```

---

### 7. Subarray Sum Equals K (HashMap + Prefix Sum)

**Problem:** Find the total number of contiguous subarrays whose sum equals k.

**Key Insight:** Combine prefix sum with HashMap.

If prefix[j] - prefix[i] = k, then the subarray from i+1 to j sums to k.
Rearranging: prefix[i] = prefix[j] - k.

So at each position j, count how many previous prefix sums equal (current prefix sum - k).

```java
public int subarraySum(int[] nums, int k) {
    Map<Integer, Integer> prefixCount = new HashMap<>();
    prefixCount.put(0, 1);  // empty prefix has sum 0, seen once

    int count = 0;
    int prefixSum = 0;

    for (int num : nums) {
        prefixSum += num;

        // How many previous prefix sums equal (prefixSum - k)?
        int target = prefixSum - k;
        if (prefixCount.containsKey(target)) {
            count += prefixCount.get(target);
        }

        // Record current prefix sum
        prefixCount.put(prefixSum, prefixCount.getOrDefault(prefixSum, 0) + 1);
    }

    return count;
}
```

**Trace with nums=[1, 1, 1], k=2:**

```
Initial: prefixCount={0:1}, count=0, prefixSum=0

i=0: num=1, prefixSum=1
  target=1-2=-1. Not in map. count=0.
  prefixCount={0:1, 1:1}

i=1: num=1, prefixSum=2
  target=2-2=0. In map (count=1). count=0+1=1.
  prefixCount={0:1, 1:1, 2:1}

i=2: num=1, prefixSum=3
  target=3-2=1. In map (count=1). count=1+1=2.
  prefixCount={0:1, 1:1, 2:1, 3:1}

Answer: 2 (subarrays [1,1] starting at index 0 and [1,1] starting at index 1)
```

---

### 8. When to Use HashMap?

**Pattern recognition triggers:**

| Problem says...                        | Think HashMap for...              |
|----------------------------------------|-----------------------------------|
| "Find if element exists"               | O(1) lookup                       |
| "Count occurrences / frequency"        | Frequency map                     |
| "Find pairs that sum to target"        | Two Sum pattern                   |
| "Group items by some property"         | Group Anagrams pattern            |
| "First unique / non-repeating"         | Frequency count, then scan        |
| "Longest substring without repeats"    | Track last position of each char  |
| "Subarray sum equals K"                | Prefix sum + HashMap              |
| "Check if two things are anagrams"     | Compare frequency maps            |

---

### 9. Common Mistakes

1. **Using wrong key type**
   ```java
   // int[] does NOT work as HashMap key (uses reference equality)
   Map<int[], String> bad = new HashMap<>();  // WRONG

   // Convert to String first
   Map<String, String> good = new HashMap<>();
   good.put(Arrays.toString(arr), "value");   // CORRECT
   ```

2. **Forgetting getOrDefault**
   ```java
   // WRONG: NullPointerException if key not found
   map.put(key, map.get(key) + 1);

   // CORRECT: provide default value
   map.put(key, map.getOrDefault(key, 0) + 1);
   ```

3. **Modifying HashMap while iterating**
   ```java
   // WRONG: ConcurrentModificationException
   for (String key : map.keySet()) {
       if (someCondition) map.remove(key);
   }

   // CORRECT: use Iterator
   Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
   while (it.hasNext()) {
       Map.Entry<String, Integer> entry = it.next();
       if (someCondition) it.remove();
   }
   ```

4. **Two pointer on UNSORTED array**
   ```java
   // Converging two pointers REQUIRE sorted array
   // If unsorted -> use HashMap approach instead
   ```

5. **Not handling duplicates in Two Pointers**
   ```java
   // In 3Sum, forgetting to skip duplicates gives duplicate triplets
   // Always add: while (left < right && nums[left] == nums[left+1]) left++;
   ```

6. **Confusing HashMap (key-value) with HashSet (keys only)**
   ```java
   // Use HashMap when you need to store associated data (value)
   // Use HashSet when you only need to check existence
   Set<Integer> seen = new HashSet<>();
   seen.add(5);
   seen.contains(5);  // true
   ```

---

### 10. Quick Summary

```
+-------------------+------------------------------------------+
| Concept           | Key Takeaway                             |
+-------------------+------------------------------------------+
| HashMap           | Key-value store, O(1) avg operations     |
| Hash Function     | Converts key -> bucket index via modulo  |
| Collision          | Same bucket -> chaining (linked list)    |
| Java 8+ treeify   | Chain > 8 -> Red-Black tree, O(log n)    |
| HashSet            | Keys only, no values, O(1) contains     |
| TreeMap            | Sorted keys, O(log n) operations        |
| Frequency Map     | Count occurrences: getOrDefault(k, 0)+1  |
| Two Pointers       | Replace O(n^2) nested loop with O(n)    |
| Opposite direction | Sorted array, find pairs, palindromes   |
| Same direction     | In-place modification, remove elements  |
| Two Sum (sorted)   | Two pointers O(n)                       |
| Two Sum (unsorted) | HashMap O(n)                            |
| 3Sum               | Sort + outer loop + two pointers O(n^2) |
| Prefix + HashMap   | Subarray sum equals K                   |
+-------------------+------------------------------------------+

Time Complexities to MEMORIZE:
  HashMap put/get/contains:  O(1) average, O(n) worst
  HashSet add/contains:      O(1) average
  TreeMap put/get:           O(log n)
  Two Pointers:              O(n) typically
  3Sum:                      O(n^2) with two pointers
  Two Sum HashMap:           O(n) time, O(n) space
```

---

**You are now ready to tackle Week 2 LeetCode problems!**
The HashMap and Two Pointers are the two most versatile tools in DSA.
If you master these, you can solve 40% of all interview problems.
