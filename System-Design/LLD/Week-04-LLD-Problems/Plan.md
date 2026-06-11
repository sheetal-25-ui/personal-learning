# LLD Week 4: Interview Problems — Full Solutions

## How to Approach LLD in a Microsoft Interview

Before diving into problems, understand the PROCESS. The interviewer is not just checking if you can code — they are checking if you can THINK systematically about system design. Here is the process to follow every single time:

```
Step 1 (2-3 min): CLARIFY REQUIREMENTS
   - Ask questions! "Should the system support X?" "What scale are we talking about?"
   - Write down functional requirements (what the system DOES)
   - Write down non-functional requirements (scale, performance, reliability)
   - This shows the interviewer you think before coding

Step 2 (3-5 min): IDENTIFY ENTITIES
   - Nouns in the requirements = Classes
   - "The user parks a car in a spot" -> User, Car, ParkingSpot
   - Draw them on the whiteboard

Step 3 (3-5 min): DEFINE RELATIONSHIPS
   - "A ParkingLot HAS many Floors" (composition/aggregation)
   - "A Car IS-A Vehicle" (inheritance)
   - "A Ticket REFERS TO a ParkingSpot" (association)

Step 4 (15-20 min): WRITE KEY CLASSES
   - Start with the most important 3-4 classes
   - Include the critical methods
   - Use enums for types/statuses
   - Show how classes interact

Step 5 (3-5 min): IDENTIFY DESIGN PATTERNS
   - "I used Strategy here because the fee calculation varies..."
   - "This is an Observer because multiple systems need to react..."
   - The interviewer WANTS to hear pattern names with justification

Step 6 (2-3 min): DISCUSS TRADE-OFFS
   - "If we need thread safety, we would add locks here..."
   - "For scale, this could use a database instead of in-memory..."
   - This shows senior-level thinking
```

**Total: ~30 minutes** — exactly the time you get in a Microsoft interview round.

---

## The Top 10 LLD Problems for Microsoft Interviews

| # | Problem | Difficulty | Microsoft Frequency | Key Patterns |
|---|---------|-----------|-------------------|--------------|
| 1 | **Parking Lot System** | Medium | Very Frequent | Strategy, Factory |
| 2 | **LRU Cache** | Medium | Top 3 at Microsoft | None (data structure focus) |
| 3 | **Library Management** | Easy | Frequent | Observer, Strategy |
| 4 | **Elevator System** | Hard | Frequent | State, Strategy, Observer |
| 5 | **Splitwise (Expense Sharing)** | Medium | Frequent | Strategy, Observer |
| 6 | **Tic Tac Toe** | Easy | Frequent | State |
| 7 | **BookMyShow (Movie Booking)** | Medium | Yes | Observer, Strategy, Singleton |
| 8 | **Snake and Ladder** | Medium | Yes | State, Factory |
| 9 | **Hotel Booking (OYO)** | Medium | Yes | Strategy, Observer |
| 10 | **Food Delivery (Zomato/Swiggy)** | Hard | Yes | All patterns |

---

## Problem 1: Parking Lot System (Full Solution)

### What is this system?

You know the big parking lots at malls like Phoenix Marketcity or Inorbit Mall? When you drive in, you get a ticket at the entrance. You park your car, go shopping, and when you return, you pay based on how long you stayed. The system tracks which spots are free, assigns you a spot, and calculates your fee.

### Functional Requirements

1. The parking lot has multiple floors, each floor has multiple parking spots
2. There are different spot sizes: Compact (for bikes/scooters), Regular (for cars), Large (for buses/SUVs)
3. Different vehicle types: Bike, Car, Bus — each needs a specific spot type
4. When a vehicle enters, the system finds an available spot and issues a ticket
5. When a vehicle exits, the system calculates the fee based on duration and vehicle type
6. The system tracks occupancy (how many spots are free on each floor)
7. Different fee strategies: hourly, flat rate, weekend rates

### Entities (Nouns = Classes)

Let us identify the objects in this system:
- **Vehicle** — the car/bike/bus that comes to park
- **ParkingSpot** — a single spot on a floor
- **ParkingFloor** — a floor with many spots
- **ParkingLot** — the entire lot with many floors (this is the main class)
- **ParkingTicket** — issued when a vehicle enters, closed when it exits
- **FeeStrategy** — how the fee is calculated (Strategy pattern)

### Relationships

```
ParkingLot
  |--- has many ---> ParkingFloor
                       |--- has many ---> ParkingSpot
                                            |--- can hold ---> Vehicle (or None)
  |--- has many ---> ParkingTicket (active tickets)
  |--- has a ------> FeeStrategy (current pricing strategy)
```

- ParkingLot **HAS** ParkingFloors (composition — floors do not exist without the lot)
- ParkingFloor **HAS** ParkingSpots (composition)
- ParkingSpot **REFERS TO** a Vehicle when occupied (association)
- ParkingTicket **REFERS TO** both the Vehicle and the ParkingSpot (association)
- ParkingLot **USES** a FeeStrategy (strategy pattern)

### Full Code Solution

```python
from enum import Enum
from datetime import datetime, timedelta
from abc import ABC, abstractmethod

# ============================================================
# ENUMS — Define the types/categories in the system
# Enums prevent typos. You cannot accidentally write "bke" instead of "bike"
# because the IDE will catch it. With strings, typos are silent bugs.
# ============================================================

class VehicleType(Enum):
    """Types of vehicles that can park here."""
    BIKE = "bike"
    CAR = "car"
    BUS = "bus"

class SpotType(Enum):
    """Types of parking spots. Each matches a vehicle type."""
    COMPACT = "compact"    # Small spots near the entrance — for bikes/scooters
    REGULAR = "regular"    # Standard spots — for cars
    LARGE = "large"        # Extra-wide/tall spots — for buses and large SUVs

# ============================================================
# VEHICLE — represents a vehicle coming to park
# This is a simple data class. A vehicle knows its license plate
# and its type — nothing else. It does not know about parking spots
# or fees. (Single Responsibility Principle)
# ============================================================

class Vehicle:
    def __init__(self, license_plate: str, vehicle_type: VehicleType):
        self.license_plate = license_plate
        self.vehicle_type = vehicle_type

    def __str__(self):
        return f"{self.vehicle_type.value.title()} [{self.license_plate}]"

# ============================================================
# PARKING SPOT — a single spot on a floor
# A spot knows its ID, type, floor, and whether it is occupied.
# It can park a vehicle and release it.
# ============================================================

class ParkingSpot:
    def __init__(self, spot_id: str, spot_type: SpotType, floor_number: int):
        self.spot_id = spot_id
        self.spot_type = spot_type
        self.floor_number = floor_number
        self.is_occupied = False
        self.vehicle = None  # Will hold a Vehicle object when occupied

    def park(self, vehicle: Vehicle) -> bool:
        """
        Attempt to park a vehicle in this spot.
        Returns True if successful, False if spot is already occupied.
        """
        if self.is_occupied:
            return False
        self.vehicle = vehicle
        self.is_occupied = True
        return True

    def release(self) -> Vehicle:
        """
        Remove the vehicle from this spot.
        Returns the vehicle that was parked here.
        """
        vehicle = self.vehicle
        self.vehicle = None
        self.is_occupied = False
        return vehicle

    def __str__(self):
        status = f"OCCUPIED by {self.vehicle}" if self.is_occupied else "AVAILABLE"
        return f"Spot {self.spot_id} (Floor {self.floor_number}, {self.spot_type.value}) - {status}"

# ============================================================
# PARKING FLOOR — a floor with many spots
# Knows how to find available spots and track capacity.
# ============================================================

class ParkingFloor:
    def __init__(self, floor_number: int):
        self.floor_number = floor_number
        self.spots = []  # List of ParkingSpot objects on this floor

    def add_spot(self, spot: ParkingSpot):
        """Add a parking spot to this floor."""
        self.spots.append(spot)

    def find_available_spot(self, spot_type: SpotType) -> ParkingSpot:
        """
        Find the first available spot of the given type on this floor.
        Returns None if no spot is available.
        """
        for spot in self.spots:
            if spot.spot_type == spot_type and not spot.is_occupied:
                return spot
        return None

    def get_available_count(self, spot_type: SpotType = None) -> int:
        """Count available spots, optionally filtered by type."""
        return sum(
            1 for spot in self.spots
            if not spot.is_occupied
            and (spot_type is None or spot.spot_type == spot_type)
        )

    def get_total_count(self, spot_type: SpotType = None) -> int:
        """Count total spots, optionally filtered by type."""
        return sum(
            1 for spot in self.spots
            if spot_type is None or spot.spot_type == spot_type
        )

# ============================================================
# PARKING TICKET — issued at entry, closed at exit
# Links a vehicle to a spot and tracks entry/exit times.
# The ticket does NOT calculate fees — that is the FeeStrategy's job.
# ============================================================

class ParkingTicket:
    _ticket_counter = 0  # Class variable for generating unique ticket IDs

    def __init__(self, vehicle: Vehicle, spot: ParkingSpot):
        ParkingTicket._ticket_counter += 1
        self.ticket_id = f"TKT-{ParkingTicket._ticket_counter:04d}"
        self.vehicle = vehicle
        self.spot = spot
        self.entry_time = datetime.now()
        self.exit_time = None
        self.is_paid = False
        self.amount_paid = 0.0

    def close(self, exit_time: datetime = None):
        """Mark the ticket as closed (vehicle is leaving)."""
        self.exit_time = exit_time or datetime.now()

    def get_duration_hours(self) -> float:
        """Calculate how many hours the vehicle was parked."""
        exit = self.exit_time or datetime.now()
        duration = (exit - self.entry_time).total_seconds() / 3600
        return max(duration, 0.5)  # Minimum 30 minutes (half hour)

    def __str__(self):
        duration = f"{self.get_duration_hours():.1f} hours" if self.exit_time else "ACTIVE"
        return (f"Ticket {self.ticket_id}: {self.vehicle} at {self.spot.spot_id} "
                f"({duration})")

# ============================================================
# FEE STRATEGY — Strategy pattern for different pricing models
# The parking lot can switch pricing strategies at runtime.
# For example: normal pricing on weekdays, premium on weekends,
# flat rate during festivals, etc.
# ============================================================

class FeeStrategy(ABC):
    """Base class for all fee calculation strategies."""
    @abstractmethod
    def calculate_fee(self, ticket: ParkingTicket) -> float:
        pass

    @abstractmethod
    def strategy_name(self) -> str:
        pass

class HourlyFeeStrategy(FeeStrategy):
    """
    Standard hourly pricing — different rates per vehicle type.
    This is what most mall parking lots in India use.
    """
    def __init__(self):
        # Rate per hour for each spot type (in Rupees)
        self._rates = {
            SpotType.COMPACT: 20,    # Rs.20/hr for bikes
            SpotType.REGULAR: 40,    # Rs.40/hr for cars
            SpotType.LARGE: 80,      # Rs.80/hr for buses
        }

    def calculate_fee(self, ticket: ParkingTicket) -> float:
        hours = ticket.get_duration_hours()
        rate = self._rates.get(ticket.spot.spot_type, 40)
        # Round up to the next hour (no partial hour discount)
        import math
        billable_hours = math.ceil(hours)
        return billable_hours * rate

    def strategy_name(self):
        return "Hourly Rate"

class FlatRateFeeStrategy(FeeStrategy):
    """
    Flat rate — pay once, park all day.
    Used by airport parking lots (e.g., Mumbai airport parking).
    """
    def __init__(self):
        self._rates = {
            SpotType.COMPACT: 50,     # Rs.50 flat for bikes
            SpotType.REGULAR: 200,    # Rs.200 flat for cars
            SpotType.LARGE: 500,      # Rs.500 flat for buses
        }

    def calculate_fee(self, ticket: ParkingTicket) -> float:
        return self._rates.get(ticket.spot.spot_type, 200)

    def strategy_name(self):
        return "Flat Rate (All Day)"

class WeekendSurgeFeeStrategy(FeeStrategy):
    """
    Weekend/holiday pricing — 1.5x the normal hourly rate.
    Used during Big Billion Days sale at malls, weekends, festivals.
    """
    def __init__(self, base_strategy: HourlyFeeStrategy, multiplier: float = 1.5):
        self._base = base_strategy
        self._multiplier = multiplier

    def calculate_fee(self, ticket: ParkingTicket) -> float:
        base_fee = self._base.calculate_fee(ticket)
        return base_fee * self._multiplier

    def strategy_name(self):
        return f"Weekend Surge ({self._multiplier}x)"

# ============================================================
# PARKING LOT — the main class (Singleton in a real system)
# Manages floors, spots, tickets, and fee calculation.
# This is the "god class" for the parking system — the entry point
# that external code interacts with.
# ============================================================

class ParkingLot:
    # Mapping: which vehicle type needs which spot type
    VEHICLE_TO_SPOT = {
        VehicleType.BIKE: SpotType.COMPACT,
        VehicleType.CAR: SpotType.REGULAR,
        VehicleType.BUS: SpotType.LARGE,
    }

    def __init__(self, name: str, fee_strategy: FeeStrategy = None):
        self.name = name
        self.floors = []                    # List of ParkingFloor objects
        self.active_tickets = {}            # license_plate -> ParkingTicket
        self._fee_strategy = fee_strategy or HourlyFeeStrategy()

    def add_floor(self, floor: ParkingFloor):
        """Add a floor to the parking lot."""
        self.floors.append(floor)

    def set_fee_strategy(self, strategy: FeeStrategy):
        """
        Change the pricing strategy at runtime (Strategy pattern).
        Example: switch to weekend pricing on Saturday morning.
        """
        print(f"[ParkingLot] Pricing changed to: {strategy.strategy_name()}")
        self._fee_strategy = strategy

    def find_available_spot(self, vehicle_type: VehicleType) -> ParkingSpot:
        """
        Search all floors for an available spot matching the vehicle type.
        Returns the first available spot (closest to entrance = lowest floor).
        """
        needed_spot_type = self.VEHICLE_TO_SPOT[vehicle_type]
        for floor in self.floors:
            spot = floor.find_available_spot(needed_spot_type)
            if spot:
                return spot
        return None

    def park_vehicle(self, vehicle: Vehicle) -> ParkingTicket:
        """
        Main entry point: vehicle arrives, find a spot, issue a ticket.
        """
        # Check if vehicle is already parked
        if vehicle.license_plate in self.active_tickets:
            print(f"Vehicle {vehicle.license_plate} is already parked!")
            return None

        # Find an available spot
        spot = self.find_available_spot(vehicle.vehicle_type)
        if not spot:
            print(f"Sorry! No {self.VEHICLE_TO_SPOT[vehicle.vehicle_type].value} "
                  f"spot available for {vehicle}")
            return None

        # Park the vehicle and issue a ticket
        spot.park(vehicle)
        ticket = ParkingTicket(vehicle, spot)
        self.active_tickets[vehicle.license_plate] = ticket

        print(f"Parked {vehicle} at Spot {spot.spot_id} (Floor {spot.floor_number})")
        print(f"  Ticket: {ticket.ticket_id}")
        return ticket

    def exit_vehicle(self, license_plate: str, exit_time: datetime = None) -> float:
        """
        Main exit point: vehicle is leaving, calculate fee, free the spot.
        """
        ticket = self.active_tickets.get(license_plate)
        if not ticket:
            print(f"No active ticket found for {license_plate}")
            return 0

        # Close the ticket (set exit time)
        ticket.close(exit_time)

        # Calculate fee using the current strategy
        fee = self._fee_strategy.calculate_fee(ticket)
        ticket.amount_paid = fee
        ticket.is_paid = True

        # Free the spot
        ticket.spot.release()

        # Remove from active tickets
        del self.active_tickets[license_plate]

        print(f"Vehicle {license_plate} exited from Spot {ticket.spot.spot_id}")
        print(f"  Duration: {ticket.get_duration_hours():.1f} hours")
        print(f"  Strategy: {self._fee_strategy.strategy_name()}")
        print(f"  Fee: Rs.{fee:.0f}")
        return fee

    def get_availability(self) -> dict:
        """Show available spots per floor and type."""
        report = {}
        for floor in self.floors:
            floor_key = f"Floor {floor.floor_number}"
            report[floor_key] = {}
            for spot_type in SpotType:
                available = floor.get_available_count(spot_type)
                total = floor.get_total_count(spot_type)
                if total > 0:
                    report[floor_key][spot_type.value] = f"{available}/{total}"
        return report

    def display_availability(self):
        """Pretty-print the availability board (like the LED display at the mall entrance)."""
        print(f"\n{'=' * 50}")
        print(f"  {self.name} — Parking Availability")
        print(f"{'=' * 50}")
        report = self.get_availability()
        for floor_name, spots in report.items():
            print(f"  {floor_name}:")
            for spot_type, count in spots.items():
                print(f"    {spot_type.title()}: {count} available")
        print(f"{'=' * 50}\n")


# ============================================================
# BUILD AND TEST THE SYSTEM
# ============================================================

# Create the parking lot
lot = ParkingLot("Phoenix Marketcity Parking", HourlyFeeStrategy())

# Create floors and spots
for floor_num in range(1, 4):  # 3 floors
    floor = ParkingFloor(floor_num)

    # Each floor has: 10 compact, 20 regular, 5 large spots
    for i in range(1, 11):
        floor.add_spot(ParkingSpot(f"F{floor_num}-C{i:02d}", SpotType.COMPACT, floor_num))
    for i in range(1, 21):
        floor.add_spot(ParkingSpot(f"F{floor_num}-R{i:02d}", SpotType.REGULAR, floor_num))
    for i in range(1, 6):
        floor.add_spot(ParkingSpot(f"F{floor_num}-L{i:02d}", SpotType.LARGE, floor_num))

    lot.add_floor(floor)

# Show initial availability
lot.display_availability()

# Park some vehicles
car1 = Vehicle("MH-02-AB-1234", VehicleType.CAR)
car2 = Vehicle("MH-04-CD-5678", VehicleType.CAR)
bike1 = Vehicle("MH-12-EF-9012", VehicleType.BIKE)
bus1 = Vehicle("MH-01-GH-3456", VehicleType.BUS)

lot.park_vehicle(car1)
lot.park_vehicle(car2)
lot.park_vehicle(bike1)
lot.park_vehicle(bus1)

# Show availability after parking
lot.display_availability()

# Simulate exit after some hours
# (We fake the exit time to simulate 3 hours of parking)
exit_time = datetime.now() + timedelta(hours=3)
lot.exit_vehicle("MH-02-AB-1234", exit_time)
# Fee: Rs.120 (3 hours x Rs.40/hr for regular spot)

# Switch to weekend pricing and exit another vehicle
lot.set_fee_strategy(WeekendSurgeFeeStrategy(HourlyFeeStrategy(), 1.5))
lot.exit_vehicle("MH-04-CD-5678", exit_time)
# Fee: Rs.180 (3 hours x Rs.40/hr x 1.5 surge = Rs.180)
```

### Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `FeeStrategy` and its subclasses | Different pricing algorithms (hourly, flat, surge) that can be swapped at runtime. The ParkingLot does not need to change when a new pricing model is added. |
| **Factory** (implicit) | `VEHICLE_TO_SPOT` mapping | Maps vehicle types to appropriate spot types. Could be extracted into a formal factory if the mapping logic becomes complex. |
| **Singleton** (would add) | `ParkingLot` | In a real system, there is one ParkingLot instance per physical location. Not shown here for simplicity. |

### Extension Points (discuss in interview)

- **Multiple entry/exit gates** — Add Gate class, track which gate a vehicle entered/exited
- **Reservation system** — Reserve a spot in advance (like BookMyShow)
- **Electric vehicle charging spots** — New SpotType with charging capability
- **Automatic Number Plate Recognition (ANPR)** — No physical ticket, camera captures plate
- **Multi-threading** — Add locks around spot.park() and spot.release() for concurrent access

---

## Problem 2: LRU Cache (Full Solution)

### What is this system?

LRU stands for "Least Recently Used." It is a cache (fast temporary storage) that automatically removes the OLDEST unused item when it runs out of space. Think of it as your phone's recent apps list — it shows the most recently used apps, and when the list is full, the least recently used app gets removed.

**Real-world example:** When you browse Flipkart, the product pages you visited recently are cached. If you go back to a recently viewed product, it loads instantly (from cache). But the cache has limited memory. When it is full and you visit a new product, the product you viewed LONGEST AGO gets evicted to make room.

**Why this is Microsoft's favorite:** It tests your knowledge of data structures (HashMap + Doubly Linked List), time complexity analysis, and edge case handling — all in one problem.

### Functional Requirements

1. `get(key)` — Return the value if key exists, otherwise return -1. Accessing a key makes it "recently used."
2. `put(key, value)` — Insert or update a key-value pair. If the cache is full, evict the least recently used item first.
3. Both operations must be O(1) time complexity (this is the hard part!)

### Why do we need BOTH a HashMap and a Doubly Linked List?

Think about what each data structure is good at:

| Operation | HashMap | Linked List | Array |
|-----------|---------|-------------|-------|
| Find by key | O(1) | O(n) | O(n) |
| Insert at front | N/A | O(1) | O(n) |
| Remove specific item | O(1) by key | O(1) if you have the node | O(n) |
| Know the order of access | No | Yes | Yes |

- **HashMap alone** gives O(1) lookup but does not track ORDER of access (which item was used least recently)
- **Linked List alone** tracks order but gives O(n) lookup (you must traverse to find an item)
- **Together:** HashMap gives O(1) lookup to find the node, and the Linked List tracks the order. Best of both worlds.

### Full Code Solution

```python
class Node:
    """
    A node in the doubly linked list.
    Each node stores a key-value pair and pointers to the previous and next nodes.

    Why doubly linked? Because we need to remove nodes from the MIDDLE of the list
    in O(1) time. With a singly linked list, removing a node requires traversing
    from the head to find the previous node — that is O(n).
    With a doubly linked list, each node knows its previous and next, so removal
    is just updating 4 pointers — O(1).
    """
    def __init__(self, key: int = 0, value: int = 0):
        self.key = key
        self.value = value
        self.prev = None  # Pointer to the previous node
        self.next = None  # Pointer to the next node

    def __str__(self):
        return f"({self.key}: {self.value})"


class LRUCache:
    """
    LRU Cache implementation using a HashMap + Doubly Linked List.

    The linked list maintains the ORDER of access:
    - HEAD (front) = Most Recently Used (MRU)
    - TAIL (back) = Least Recently Used (LRU)

    The HashMap provides O(1) lookup:
    - key -> Node (so we can jump directly to any node in the list)

    Visual representation:
        HashMap: {1: Node1, 2: Node2, 3: Node3}

        Doubly Linked List (most recent on left):
        [DUMMY_HEAD] <-> [Node3] <-> [Node1] <-> [Node2] <-> [DUMMY_TAIL]
                          (MRU)                    (LRU)

    Why dummy head and tail?
    They simplify the code by eliminating edge cases. Without dummies,
    every insert/remove must check "is this the head?" and "is this the tail?"
    With dummies, there is ALWAYS a node before and after any real node.
    """

    def __init__(self, capacity: int):
        """
        Initialize the cache with a fixed capacity.
        Once the cache has this many items, adding a new item
        evicts the least recently used item.
        """
        self.capacity = capacity
        self.cache = {}  # HashMap: key -> Node

        # Create dummy head and tail nodes
        # These are sentinel nodes — they never hold real data.
        # They exist only to simplify insertion/removal logic.
        self.head = Node()  # Dummy head (left boundary)
        self.tail = Node()  # Dummy tail (right boundary)
        self.head.next = self.tail
        self.tail.prev = self.head
        # Initial state: head <-> tail (empty list)

    def _remove(self, node: Node):
        """
        Remove a node from its current position in the linked list.
        This is O(1) because we have direct access to prev and next.

        Before: ... <-> [A] <-> [node] <-> [B] <-> ...
        After:  ... <-> [A] <-> [B] <-> ...
        """
        prev_node = node.prev
        next_node = node.next
        prev_node.next = next_node  # A's next now points to B
        next_node.prev = prev_node  # B's prev now points to A
        # 'node' is now disconnected from the list

    def _add_to_front(self, node: Node):
        """
        Add a node right after the dummy head (making it the most recently used).
        This is O(1) — just update 4 pointers.

        Before: head <-> [X] <-> ...
        After:  head <-> [node] <-> [X] <-> ...
        """
        node.prev = self.head          # node's prev = head
        node.next = self.head.next     # node's next = whatever was after head
        self.head.next.prev = node     # old first real node's prev = node
        self.head.next = node          # head's next = node

    def get(self, key: int) -> int:
        """
        Get the value for a key.
        If the key exists: return its value AND move it to the front (most recently used).
        If the key does not exist: return -1.

        Time complexity: O(1)
        """
        if key not in self.cache:
            return -1

        # Key exists — find the node via HashMap (O(1))
        node = self.cache[key]

        # Move to front: remove from current position, add to front
        # This marks it as "most recently used"
        self._remove(node)
        self._add_to_front(node)

        return node.value

    def put(self, key: int, value: int):
        """
        Insert or update a key-value pair.
        If key already exists: update the value and move to front.
        If key does not exist:
            - If cache is full: evict the least recently used (node before tail)
            - Add the new key-value pair to the front

        Time complexity: O(1)
        """
        if key in self.cache:
            # Key already exists — update value and move to front
            node = self.cache[key]
            node.value = value
            self._remove(node)
            self._add_to_front(node)
        else:
            # Key does not exist — might need to evict
            if len(self.cache) >= self.capacity:
                # Cache is full! Evict the least recently used item.
                # The LRU item is the node just before the dummy tail.
                lru_node = self.tail.prev
                self._remove(lru_node)
                del self.cache[lru_node.key]  # Remove from HashMap too!

            # Create a new node and add it to the front
            new_node = Node(key, value)
            self.cache[key] = new_node
            self._add_to_front(new_node)

    def display(self):
        """Print the cache contents from most recently used to least recently used."""
        items = []
        current = self.head.next
        while current != self.tail:
            items.append(f"{current.key}:{current.value}")
            current = current.next
        print(f"Cache ({len(self.cache)}/{self.capacity}): [{' -> '.join(items)}]")
        print(f"  (Left = Most Recent, Right = Least Recent)")


# ============================================================
# WALKTHROUGH — step by step
# ============================================================

cache = LRUCache(3)  # Capacity of 3

# Put 3 items
cache.put(1, 100)
cache.display()  # [1:100]

cache.put(2, 200)
cache.display()  # [2:200 -> 1:100]

cache.put(3, 300)
cache.display()  # [3:300 -> 2:200 -> 1:100]
# Cache is now FULL (3/3)

# Access key 1 — moves it to the front (most recent)
value = cache.get(1)
print(f"\nget(1) = {value}")
cache.display()  # [1:100 -> 3:300 -> 2:200]
# Notice: 1 moved to front, 2 is now the LRU

# Put a new item (4) — cache is full, so LRU (key 2) gets evicted
cache.put(4, 400)
print(f"\nput(4, 400) — evicts key 2 (least recently used)")
cache.display()  # [4:400 -> 1:100 -> 3:300]
# Key 2 is GONE — it was the least recently used

# Try to get evicted key
value = cache.get(2)
print(f"\nget(2) = {value}")  # -1 (not found — it was evicted!)

# Update existing key
cache.put(3, 999)
print(f"\nput(3, 999) — updates value and moves to front")
cache.display()  # [3:999 -> 4:400 -> 1:100]
```

### Time and Space Complexity

| Operation | Time | Why |
|-----------|------|-----|
| `get(key)` | O(1) | HashMap lookup O(1) + linked list remove/add O(1) |
| `put(key, value)` | O(1) | HashMap lookup O(1) + linked list remove/add O(1) + eviction O(1) |
| Space | O(capacity) | HashMap stores at most `capacity` entries, linked list has same nodes |

### Design Patterns Used

This problem is primarily a data structure problem, not a design pattern problem. However:
- **Singleton** could be applied if the cache is shared application-wide
- **Strategy** could be applied for different eviction policies (LRU, LFU, FIFO)
- **Observer** could notify when items are evicted (e.g., write-back to database on eviction)

---

## Problem 3: Library Management System (Full Solution)

### What is this system?

Think about your local library (or a college library like IIT's). You go in, browse books, pick one, go to the counter, show your library card, and the librarian issues the book to you. You have a due date, and if you return late, you pay a fine. The system tracks which books are available, who borrowed what, and when things are due.

### Functional Requirements

1. The library has books organized by category (Fiction, Non-Fiction, Science, etc.)
2. Members can search for books by title, author, or category
3. Members can borrow books (max 5 at a time)
4. Members must return books by the due date (14 days)
5. Late returns incur a fine (Rs.5 per day)
6. The system tracks book availability in real-time
7. When a popular book is returned, members on the waitlist are notified (Observer pattern)

### Full Code Solution

```python
from enum import Enum
from datetime import datetime, timedelta
from abc import ABC, abstractmethod

class BookStatus(Enum):
    AVAILABLE = "available"
    BORROWED = "borrowed"
    RESERVED = "reserved"
    LOST = "lost"

class BookCategory(Enum):
    FICTION = "Fiction"
    NON_FICTION = "Non-Fiction"
    SCIENCE = "Science"
    TECHNOLOGY = "Technology"
    HISTORY = "History"

# ============================================================
# OBSERVER PATTERN — for waitlist notifications
# When a popular book is returned, all members waiting for it
# are automatically notified. Without Observer, the librarian
# would have to manually check the waitlist and call each member.
# ============================================================

class BookObserver(ABC):
    """Any class that wants to be notified when a book becomes available."""
    @abstractmethod
    def on_book_available(self, book):
        pass

class Book:
    """
    Represents a single copy of a book in the library.
    Note: A library might have multiple COPIES of the same TITLE.
    Each copy is a separate Book object with its own status.
    """
    def __init__(self, isbn: str, title: str, author: str,
                 category: BookCategory, copy_number: int = 1):
        self.isbn = isbn
        self.title = title
        self.author = author
        self.category = category
        self.copy_number = copy_number
        self.status = BookStatus.AVAILABLE
        self._waitlist = []  # Observer pattern — list of members waiting for this book

    def add_to_waitlist(self, observer: BookObserver):
        """Subscribe a member to be notified when this book is available."""
        if observer not in self._waitlist:
            self._waitlist.append(observer)
            print(f"  {observer.name} added to waitlist for '{self.title}'")

    def notify_waitlist(self):
        """Notify all waiting members that this book is now available."""
        for observer in self._waitlist:
            observer.on_book_available(self)
        self._waitlist.clear()

    def __str__(self):
        return f"'{self.title}' by {self.author} [{self.status.value}]"

class BorrowRecord:
    """
    Tracks a single borrow transaction — who borrowed which book and when.
    This is like the slip the librarian stamps with the due date.
    """
    def __init__(self, book: Book, member, borrow_date: datetime = None):
        self.book = book
        self.member = member
        self.borrow_date = borrow_date or datetime.now()
        self.due_date = self.borrow_date + timedelta(days=14)  # 2-week borrowing period
        self.return_date = None
        self.fine = 0.0

    def calculate_fine(self, return_date: datetime = None) -> float:
        """Calculate late return fine: Rs.5 per day after due date."""
        actual_return = return_date or datetime.now()
        if actual_return > self.due_date:
            days_late = (actual_return - self.due_date).days
            self.fine = days_late * 5  # Rs.5 per day
        return self.fine

    def __str__(self):
        status = "RETURNED" if self.return_date else f"DUE: {self.due_date.strftime('%d-%b-%Y')}"
        return f"'{self.book.title}' -> {self.member.name} ({status})"

class Member(BookObserver):
    """
    A library member who can borrow and return books.
    Also acts as an Observer — gets notified when waitlisted books become available.
    """
    MAX_BOOKS = 5  # Maximum books a member can borrow at once

    def __init__(self, member_id: str, name: str, email: str):
        self.member_id = member_id
        self.name = name
        self.email = email
        self.borrowed_books = []     # List of BorrowRecords
        self.borrow_history = []     # All past borrow records
        self.total_fines = 0.0

    def can_borrow(self) -> bool:
        """Check if member can borrow more books."""
        active_borrows = [r for r in self.borrowed_books if r.return_date is None]
        return len(active_borrows) < self.MAX_BOOKS

    def on_book_available(self, book):
        """Observer callback — called when a waitlisted book is returned."""
        print(f"  [NOTIFICATION] {self.name}: '{book.title}' is now available! Hurry to the library!")

    def __str__(self):
        active = len([r for r in self.borrowed_books if r.return_date is None])
        return f"Member {self.name} ({active}/{self.MAX_BOOKS} books borrowed)"

class Library:
    """
    The main library system — manages books, members, and borrowing.
    This is the Facade that external code interacts with.
    """
    def __init__(self, name: str):
        self.name = name
        self.books = []        # All book copies in the library
        self.members = {}      # member_id -> Member

    def add_book(self, book: Book):
        """Add a book copy to the library's collection."""
        self.books.append(book)

    def register_member(self, member: Member):
        """Register a new library member."""
        self.members[member.member_id] = member
        print(f"Registered: {member.name} ({member.member_id})")

    def search_by_title(self, title: str) -> list:
        """Search for books by title (case-insensitive partial match)."""
        return [b for b in self.books if title.lower() in b.title.lower()]

    def search_by_author(self, author: str) -> list:
        """Search for books by author name."""
        return [b for b in self.books if author.lower() in b.author.lower()]

    def search_by_category(self, category: BookCategory) -> list:
        """Search for books by category."""
        return [b for b in self.books if b.category == category]

    def search_available(self, title: str) -> Book:
        """Find an available copy of a book by title."""
        matches = self.search_by_title(title)
        for book in matches:
            if book.status == BookStatus.AVAILABLE:
                return book
        return None

    def borrow_book(self, member_id: str, title: str,
                    borrow_date: datetime = None) -> BorrowRecord:
        """
        A member borrows a book. The core transaction of the library.
        """
        # Validate member
        member = self.members.get(member_id)
        if not member:
            print(f"Member {member_id} not found!")
            return None

        # Check if member can borrow
        if not member.can_borrow():
            print(f"{member.name} has reached the maximum borrowing limit ({Member.MAX_BOOKS} books)!")
            return None

        # Find an available copy
        book = self.search_available(title)
        if not book:
            print(f"No available copy of '{title}' found.")
            # Offer to add to waitlist
            all_copies = self.search_by_title(title)
            if all_copies:
                print(f"  All {len(all_copies)} copies are currently borrowed.")
                all_copies[0].add_to_waitlist(member)
            return None

        # Create the borrow record
        record = BorrowRecord(book, member, borrow_date)
        book.status = BookStatus.BORROWED
        member.borrowed_books.append(record)

        print(f"{member.name} borrowed '{book.title}'")
        print(f"  Due date: {record.due_date.strftime('%d-%b-%Y')}")
        return record

    def return_book(self, member_id: str, title: str,
                    return_date: datetime = None) -> float:
        """
        A member returns a book. Calculates fine if late.
        Notifies waitlisted members if any.
        """
        member = self.members.get(member_id)
        if not member:
            print(f"Member {member_id} not found!")
            return 0

        # Find the active borrow record for this book
        record = None
        for r in member.borrowed_books:
            if r.book.title.lower() == title.lower() and r.return_date is None:
                record = r
                break

        if not record:
            print(f"{member.name} does not have '{title}' borrowed!")
            return 0

        # Process the return
        actual_return = return_date or datetime.now()
        record.return_date = actual_return
        fine = record.calculate_fine(actual_return)
        member.total_fines += fine

        # Make the book available again
        record.book.status = BookStatus.AVAILABLE

        print(f"{member.name} returned '{record.book.title}'")
        if fine > 0:
            days_late = (actual_return - record.due_date).days
            print(f"  Late by {days_late} days. Fine: Rs.{fine:.0f}")
        else:
            print(f"  Returned on time. No fine.")

        # Notify waitlisted members (Observer pattern in action!)
        record.book.notify_waitlist()

        # Move to history
        member.borrow_history.append(record)

        return fine


# ============================================================
# TEST THE SYSTEM
# ============================================================

# Create the library
library = Library("Central Library, IIT Bombay")

# Add books
library.add_book(Book("978-0-13-468599-1", "Clean Code", "Robert C. Martin",
                       BookCategory.TECHNOLOGY))
library.add_book(Book("978-0-13-468599-1", "Clean Code", "Robert C. Martin",
                       BookCategory.TECHNOLOGY, copy_number=2))
library.add_book(Book("978-0-06-112008-4", "To Kill a Mockingbird", "Harper Lee",
                       BookCategory.FICTION))
library.add_book(Book("978-0-07-013151-4", "The C Programming Language", "Kernighan & Ritchie",
                       BookCategory.TECHNOLOGY))

# Register members
sheetal = Member("M001", "Sheetal", "sheetal@example.com")
rahul = Member("M002", "Rahul", "rahul@example.com")
library.register_member(sheetal)
library.register_member(rahul)

# Sheetal borrows Clean Code
library.borrow_book("M001", "Clean Code")
# Sheetal borrowed 'Clean Code'. Due: 17-Jun-2026

# Rahul also wants Clean Code — borrows the second copy
library.borrow_book("M002", "Clean Code")
# Rahul borrowed 'Clean Code'. Due: 17-Jun-2026

# A third person wants Clean Code — but both copies are borrowed!
priya = Member("M003", "Priya", "priya@example.com")
library.register_member(priya)
library.borrow_book("M003", "Clean Code")
# No available copy. Priya added to waitlist.

# Sheetal returns Clean Code (on time)
library.return_book("M001", "Clean Code")
# Sheetal returned 'Clean Code'. No fine.
# [NOTIFICATION] Priya: 'Clean Code' is now available! Hurry to the library!

# Rahul returns Clean Code LATE (5 days late)
late_return = datetime.now() + timedelta(days=19)  # 19 days (5 days late)
library.return_book("M002", "Clean Code", late_return)
# Rahul returned 'Clean Code'. Late by 5 days. Fine: Rs.25
```

### Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Observer** | `Book._waitlist` and `Member.on_book_available()` | When a popular book is returned, all waitlisted members are automatically notified without the library having to manually track who wants which book. |
| **Facade** | `Library` class | Provides a simple interface (`borrow_book`, `return_book`, `search`) hiding the complexity of managing books, members, records, and notifications. |
| **Strategy** (extension) | Fine calculation could use different strategies | Flat fine vs. tiered fine (Rs.5/day for first week, Rs.10/day after) vs. percentage of book value. |

---

## Problem 4: Splitwise / Expense Sharing System (Full Solution)

### What is this system?

You go on a trip to Goa with 4 friends. One person pays for the hotel (Rs.12,000), another pays for the cab (Rs.3,000), another pays for dinner (Rs.4,000). At the end, you need to figure out: who owes whom how much money? Splitwise solves this by tracking all expenses and calculating the simplest way to settle debts.

### Functional Requirements

1. Users can create groups (e.g., "Goa Trip," "Flat Expenses")
2. Users can add expenses with different split types:
   - **Equal split** — divide equally among selected members
   - **Exact split** — specify exact amounts for each member
   - **Percentage split** — specify percentage for each member
3. The system calculates who owes whom
4. The system simplifies debts (minimize the number of transactions)
5. Users can settle up (record a payment between two users)

### Full Code Solution

```python
from enum import Enum
from datetime import datetime
from abc import ABC, abstractmethod

class SplitType(Enum):
    EQUAL = "equal"
    EXACT = "exact"
    PERCENTAGE = "percentage"

# ============================================================
# STRATEGY PATTERN — Different ways to split an expense
# Why Strategy? Because the splitting algorithm varies by type,
# and we might add new types later (e.g., by shares, by weight)
# ============================================================

class SplitStrategy(ABC):
    @abstractmethod
    def calculate_splits(self, total_amount: float, participants: list,
                         split_details: dict = None) -> dict:
        """
        Returns a dict of {user_id: amount_owed} for each participant.
        The amounts should sum to total_amount.
        """
        pass

class EqualSplitStrategy(SplitStrategy):
    """Split equally among all participants."""
    def calculate_splits(self, total_amount, participants, split_details=None):
        per_person = round(total_amount / len(participants), 2)
        # Handle rounding — give the remainder to the first person
        splits = {p: per_person for p in participants}
        remainder = round(total_amount - (per_person * len(participants)), 2)
        if remainder != 0:
            splits[participants[0]] = round(splits[participants[0]] + remainder, 2)
        return splits

class ExactSplitStrategy(SplitStrategy):
    """Each participant pays an exact specified amount."""
    def calculate_splits(self, total_amount, participants, split_details=None):
        if not split_details:
            raise ValueError("Exact split requires split_details {user_id: amount}")
        # Validate that amounts sum to total
        total_specified = sum(split_details.values())
        if abs(total_specified - total_amount) > 0.01:
            raise ValueError(
                f"Specified amounts ({total_specified}) do not sum to total ({total_amount})")
        return split_details

class PercentageSplitStrategy(SplitStrategy):
    """Each participant pays a specified percentage."""
    def calculate_splits(self, total_amount, participants, split_details=None):
        if not split_details:
            raise ValueError("Percentage split requires split_details {user_id: percentage}")
        # Validate percentages sum to 100
        total_pct = sum(split_details.values())
        if abs(total_pct - 100) > 0.01:
            raise ValueError(f"Percentages must sum to 100, got {total_pct}")
        return {user: round(total_amount * pct / 100, 2)
                for user, pct in split_details.items()}

# Factory for creating the right strategy
SPLIT_STRATEGIES = {
    SplitType.EQUAL: EqualSplitStrategy(),
    SplitType.EXACT: ExactSplitStrategy(),
    SplitType.PERCENTAGE: PercentageSplitStrategy(),
}

# ============================================================
# CORE CLASSES
# ============================================================

class User:
    def __init__(self, user_id: str, name: str, phone: str = ""):
        self.user_id = user_id
        self.name = name
        self.phone = phone

    def __str__(self):
        return self.name

class Expense:
    """
    A single expense — someone paid for something, and it is split among members.
    Example: Rahul paid Rs.3000 for the cab, split equally among 4 friends.
    """
    _counter = 0

    def __init__(self, description: str, total_amount: float,
                 paid_by: User, split_type: SplitType,
                 participants: list, split_details: dict = None):
        Expense._counter += 1
        self.expense_id = f"EXP-{Expense._counter:04d}"
        self.description = description
        self.total_amount = total_amount
        self.paid_by = paid_by
        self.split_type = split_type
        self.created_at = datetime.now()

        # Use Strategy pattern to calculate how much each person owes
        strategy = SPLIT_STRATEGIES[split_type]
        participant_ids = [p.user_id for p in participants]
        self.splits = strategy.calculate_splits(
            total_amount, participant_ids, split_details
        )
        # Store participant objects for display
        self._participants = {p.user_id: p for p in participants}

    def display(self):
        print(f"\n  {self.expense_id}: {self.description}")
        print(f"  Total: Rs.{self.total_amount:.2f} | Paid by: {self.paid_by.name}")
        print(f"  Split ({self.split_type.value}):")
        for user_id, amount in self.splits.items():
            name = self._participants.get(user_id, user_id)
            print(f"    {name}: Rs.{amount:.2f}")

class Group:
    """
    A group of people who share expenses.
    Example: "Goa Trip 2026" with 4 friends.
    """
    def __init__(self, group_id: str, name: str, created_by: User):
        self.group_id = group_id
        self.name = name
        self.created_by = created_by
        self.members = {}      # user_id -> User
        self.expenses = []     # List of Expense objects
        self.settlements = []  # List of (from_user, to_user, amount) tuples

        # Add creator as first member
        self.add_member(created_by)

    def add_member(self, user: User):
        self.members[user.user_id] = user
        print(f"  {user.name} joined group '{self.name}'")

    def add_expense(self, description: str, total_amount: float,
                    paid_by: User, split_type: SplitType = SplitType.EQUAL,
                    participants: list = None, split_details: dict = None):
        """
        Add an expense to the group.
        If participants not specified, splits among ALL group members.
        """
        if participants is None:
            participants = list(self.members.values())

        expense = Expense(description, total_amount, paid_by,
                         split_type, participants, split_details)
        self.expenses.append(expense)
        expense.display()
        return expense

    def get_balances(self) -> dict:
        """
        Calculate net balance for each member.
        Positive = others owe you money (you overpaid)
        Negative = you owe money (you underpaid)

        Example:
          Rahul paid Rs.3000 for 4 people (Rs.750 each)
          Rahul's balance: +2250 (he paid 3000 but his share was only 750)
          Each other person's balance: -750 (they owe their share)
        """
        balances = {uid: 0.0 for uid in self.members}

        for expense in self.expenses:
            # The payer PAID the full amount (positive)
            payer_id = expense.paid_by.user_id
            balances[payer_id] += expense.total_amount

            # Each participant OWES their split amount (negative)
            for user_id, amount in expense.splits.items():
                balances[user_id] -= amount

        # Account for settlements
        for from_id, to_id, amount in self.settlements:
            balances[from_id] += amount   # Payer reduces their debt
            balances[to_id] -= amount     # Receiver's credit decreases

        # Round to avoid floating point artifacts
        return {uid: round(bal, 2) for uid, bal in balances.items()}

    def get_simplified_debts(self) -> list:
        """
        Calculate the MINIMUM number of transactions needed to settle all debts.

        Algorithm (greedy):
        1. Calculate net balance for each person
        2. Separate into creditors (positive balance) and debtors (negative balance)
        3. Match the biggest debtor with the biggest creditor
        4. Transfer the minimum of the two amounts
        5. Repeat until all balances are zero

        This minimizes the number of transactions.
        Example: If A owes B Rs.100 and B owes C Rs.100,
        instead of two transactions, just A pays C Rs.100 directly.
        """
        balances = self.get_balances()

        # Separate into creditors (owed money) and debtors (owe money)
        creditors = []  # (user_id, amount) — positive balances
        debtors = []    # (user_id, amount) — negative balances

        for uid, balance in balances.items():
            if balance > 0.01:
                creditors.append([uid, balance])
            elif balance < -0.01:
                debtors.append([uid, -balance])  # Store as positive for easier math

        # Sort both by amount (descending) for greedy matching
        creditors.sort(key=lambda x: x[1], reverse=True)
        debtors.sort(key=lambda x: x[1], reverse=True)

        transactions = []
        i, j = 0, 0
        while i < len(debtors) and j < len(creditors):
            debtor_id, debt = debtors[i]
            creditor_id, credit = creditors[j]

            # Transfer the smaller of the two amounts
            transfer = min(debt, credit)
            transactions.append((debtor_id, creditor_id, round(transfer, 2)))

            # Update remaining balances
            debtors[i][1] -= transfer
            creditors[j][1] -= transfer

            # Move to next debtor/creditor if settled
            if debtors[i][1] < 0.01:
                i += 1
            if creditors[j][1] < 0.01:
                j += 1

        return transactions

    def settle(self, from_user_id: str, to_user_id: str, amount: float):
        """Record a settlement (payment) between two members."""
        self.settlements.append((from_user_id, to_user_id, amount))
        from_name = self.members[from_user_id].name
        to_name = self.members[to_user_id].name
        print(f"  Settlement: {from_name} paid Rs.{amount:.2f} to {to_name}")

    def show_balances(self):
        """Display who owes what."""
        balances = self.get_balances()
        print(f"\n{'=' * 50}")
        print(f"  Balances for '{self.name}'")
        print(f"{'=' * 50}")
        for uid, balance in balances.items():
            name = self.members[uid].name
            if balance > 0.01:
                print(f"  {name}: +Rs.{balance:.2f} (is owed)")
            elif balance < -0.01:
                print(f"  {name}: -Rs.{abs(balance):.2f} (owes)")
            else:
                print(f"  {name}: settled up!")

    def show_simplified_debts(self):
        """Show the minimum transactions needed to settle all debts."""
        transactions = self.get_simplified_debts()
        print(f"\n  Simplified settlements:")
        if not transactions:
            print(f"  Everyone is settled up!")
            return
        for from_id, to_id, amount in transactions:
            from_name = self.members[from_id].name
            to_name = self.members[to_id].name
            print(f"  {from_name} -> pays Rs.{amount:.2f} -> {to_name}")


# ============================================================
# TEST THE SYSTEM — Goa Trip Example
# ============================================================

# Create users
sheetal = User("U1", "Sheetal", "9876543210")
rahul = User("U2", "Rahul", "9876543211")
priya = User("U3", "Priya", "9876543212")
amit = User("U4", "Amit", "9876543213")

# Create a group
print("Creating group: Goa Trip 2026")
goa_trip = Group("G1", "Goa Trip 2026", sheetal)
goa_trip.add_member(rahul)
goa_trip.add_member(priya)
goa_trip.add_member(amit)

# Add expenses
print("\n--- Adding Expenses ---")

# Sheetal paid for the hotel (split equally among all 4)
goa_trip.add_expense("Hotel (2 nights)", 12000, sheetal)
# Each person owes Rs.3000

# Rahul paid for the cab (split equally)
goa_trip.add_expense("Cab (Airport + Sightseeing)", 3200, rahul)
# Each person owes Rs.800

# Priya paid for dinner, but Amit ate more, so split by percentage
goa_trip.add_expense(
    "Dinner at Thalassa", 4000, priya,
    SplitType.PERCENTAGE,
    split_details={"U1": 20, "U2": 20, "U3": 25, "U4": 35}
)
# Sheetal: Rs.800, Rahul: Rs.800, Priya: Rs.1000, Amit: Rs.1400

# Amit paid for water sports, but only 3 participated (not Priya)
goa_trip.add_expense(
    "Water Sports", 6000, amit,
    SplitType.EQUAL,
    participants=[sheetal, rahul, amit]  # Priya opted out
)
# Sheetal: Rs.2000, Rahul: Rs.2000, Amit: Rs.2000

# Show balances
goa_trip.show_balances()

# Show simplified settlements
goa_trip.show_simplified_debts()
```

### Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `SplitStrategy` and subclasses | Different ways to split expenses (equal, exact, percentage). New split types can be added without changing existing code. |
| **Factory** (implicit) | `SPLIT_STRATEGIES` dict | Maps SplitType enum to the right strategy object. |
| **Facade** | `Group` class | Provides simple methods like `add_expense()` and `show_balances()` that hide the complexity of balance calculation and debt simplification. |

---

## Problem 5: Tic Tac Toe (Full Solution)

### What is this system?

The classic 3x3 grid game. Two players take turns placing their symbol (X or O) on the board. First player to get 3 in a row (horizontal, vertical, or diagonal) wins. If all 9 squares are filled with no winner, it is a draw.

### Functional Requirements

1. A 3x3 board (extendable to NxN)
2. Two players take turns
3. Validate moves (cannot place on an occupied cell)
4. Detect win condition (3 in a row)
5. Detect draw (board full, no winner)
6. Support human vs. human play

### Full Code Solution

```python
from enum import Enum

class Symbol(Enum):
    X = "X"
    O = "O"
    EMPTY = " "

class Player:
    """Represents a player in the game."""
    def __init__(self, name: str, symbol: Symbol):
        self.name = name
        self.symbol = symbol
        self.wins = 0

    def __str__(self):
        return f"{self.name} ({self.symbol.value})"

class Board:
    """
    The game board — an NxN grid.
    Handles placing symbols, checking validity, and detecting wins.
    """
    def __init__(self, size: int = 3):
        self.size = size
        # Create a 2D grid filled with EMPTY
        self.grid = [[Symbol.EMPTY for _ in range(size)] for _ in range(size)]
        self.moves_made = 0

    def place(self, row: int, col: int, symbol: Symbol) -> bool:
        """
        Place a symbol on the board.
        Returns True if successful, False if invalid move.
        """
        # Validate bounds
        if not (0 <= row < self.size and 0 <= col < self.size):
            print(f"  Invalid! Row and column must be between 0 and {self.size - 1}")
            return False

        # Validate cell is empty
        if self.grid[row][col] != Symbol.EMPTY:
            print(f"  Invalid! Cell ({row},{col}) is already occupied by {self.grid[row][col].value}")
            return False

        self.grid[row][col] = symbol
        self.moves_made += 1
        return True

    def check_winner(self, symbol: Symbol) -> bool:
        """
        Check if the given symbol has won.
        Must check: all rows, all columns, both diagonals.
        """
        n = self.size

        # Check each row — does any row have all cells matching the symbol?
        for row in range(n):
            if all(self.grid[row][col] == symbol for col in range(n)):
                return True

        # Check each column
        for col in range(n):
            if all(self.grid[row][col] == symbol for row in range(n)):
                return True

        # Check main diagonal (top-left to bottom-right)
        if all(self.grid[i][i] == symbol for i in range(n)):
            return True

        # Check anti-diagonal (top-right to bottom-left)
        if all(self.grid[i][n - 1 - i] == symbol for i in range(n)):
            return True

        return False

    def is_full(self) -> bool:
        """Check if the board is completely filled (draw condition)."""
        return self.moves_made == self.size * self.size

    def reset(self):
        """Clear the board for a new game."""
        self.grid = [[Symbol.EMPTY for _ in range(self.size)] for _ in range(self.size)]
        self.moves_made = 0

    def display(self):
        """Pretty-print the board."""
        n = self.size
        print()
        for row in range(n):
            cells = [f" {self.grid[row][col].value} " for col in range(n)]
            print("|".join(cells))
            if row < n - 1:
                print("-" * (4 * n - 1))
        print()

class Game:
    """
    The game controller — manages turns, validates moves, and determines the outcome.
    Uses the State pattern concept: the game has states (IN_PROGRESS, WON, DRAW).
    """

    def __init__(self, player1_name: str = "Player 1", player2_name: str = "Player 2",
                 board_size: int = 3):
        self.board = Board(board_size)
        self.players = [
            Player(player1_name, Symbol.X),
            Player(player2_name, Symbol.O),
        ]
        self.current_turn = 0  # Index into self.players (0 or 1)
        self.winner = None
        self.is_over = False

    def current_player(self) -> Player:
        """Return the player whose turn it is."""
        return self.players[self.current_turn]

    def make_move(self, row: int, col: int) -> bool:
        """
        The current player makes a move at (row, col).
        Returns True if the move was valid and processed.
        """
        if self.is_over:
            print("Game is already over!")
            return False

        player = self.current_player()
        print(f"{player.name}'s turn ({player.symbol.value}): placing at ({row}, {col})")

        # Try to place the symbol
        if not self.board.place(row, col, player.symbol):
            return False

        # Display the board after the move
        self.board.display()

        # Check if this move wins the game
        if self.board.check_winner(player.symbol):
            self.winner = player
            self.is_over = True
            player.wins += 1
            print(f"{'=' * 30}")
            print(f"  {player.name} WINS!")
            print(f"{'=' * 30}")
            return True

        # Check for draw
        if self.board.is_full():
            self.is_over = True
            print(f"{'=' * 30}")
            print(f"  It's a DRAW!")
            print(f"{'=' * 30}")
            return True

        # Switch turns
        self.current_turn = 1 - self.current_turn  # Toggle between 0 and 1
        return True

    def reset(self):
        """Reset for a new game, keeping the same players."""
        self.board.reset()
        self.current_turn = 0
        self.winner = None
        self.is_over = False


# ============================================================
# PLAY A GAME
# ============================================================

game = Game("Sheetal", "Rahul")

print("Let's play Tic Tac Toe!")
print(f"{game.players[0]} vs {game.players[1]}")
game.board.display()

# Simulate a game where Sheetal (X) wins with a diagonal
game.make_move(0, 0)  # Sheetal: X at top-left
game.make_move(0, 1)  # Rahul:  O at top-center
game.make_move(1, 1)  # Sheetal: X at center (diagonal building)
game.make_move(0, 2)  # Rahul:  O at top-right
game.make_move(2, 2)  # Sheetal: X at bottom-right -> WINS! (diagonal)
```

### Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **State** (conceptual) | Game has states: IN_PROGRESS, WON, DRAW | The game behaves differently based on whether it is in progress or over. `make_move` rejects moves when the game is over. |

---

## Practice Exercises

### Exercise 1 (Saturday): Design a Hotel Booking System (OYO-style)

**Estimated time: 45 minutes**

**What is the system?** Think of OYO or MakeMyTrip hotel booking. Users search for hotels by city and date, select a room type, and book it. The hotel has different room types at different prices.

**Requirements:**
1. Hotels have rooms of different types: Standard, Deluxe, Suite
2. Each room type has a price per night
3. Users can search for available rooms by city, check-in date, and check-out date
4. Users can book a room (creates a reservation)
5. Reservations can be cancelled (with different cancellation policies based on how far in advance)
6. Pricing varies: weekday vs. weekend rates, seasonal surcharges

**Your task:**
1. Identify all classes (nouns)
2. Define relationships between them
3. Write the full code with these patterns:
   - **Strategy** for pricing (weekday/weekend/seasonal)
   - **Strategy** for cancellation policy (flexible: full refund, moderate: 50% refund, strict: no refund)
   - **Observer** to notify hotel staff when a booking is made/cancelled
4. Test with a realistic scenario (book 3 rooms at a Mumbai hotel, cancel one)

---

### Exercise 2 (Saturday): Design an Elevator System

**Estimated time: 60 minutes (this is the hardest problem)**

**What is the system?** Think about the elevators in a 20-floor office building like those in Cyber City, Gurgaon. Multiple elevators serve different floors. When you press the button on a floor, the system decides which elevator to send.

**Requirements:**
1. Building has N floors and M elevators
2. Each elevator has a current floor, a direction (UP/DOWN/IDLE), and a list of pending requests
3. External buttons on each floor: UP button, DOWN button
4. Internal buttons inside each elevator: floor number buttons
5. The system must decide which elevator handles each request (scheduling algorithm)
6. Elevators move one floor at a time, stopping at requested floors

**Your task:**
1. Use the **State** pattern for elevator states: IDLE, MOVING_UP, MOVING_DOWN, DOOR_OPEN
2. Use the **Strategy** pattern for elevator scheduling: NEAREST_ELEVATOR, LOOK (scan), FCFS (first-come-first-served)
3. Use the **Observer** pattern to update the floor display panels when an elevator moves
4. Simulate: 3 elevators, 10 floors, 5 simultaneous requests

---

### Exercise 3 (Sunday): Design BookMyShow (Movie Booking)

**Estimated time: 45 minutes**

**What is the system?** BookMyShow — search for movies, select a theater, pick seats, and pay. Each show has a seating chart, and seats are reserved in real-time to prevent double-booking.

**Requirements:**
1. Movies are showing at different theaters at different times
2. Each show has a seating layout with different categories (Silver, Gold, Platinum)
3. Users can search movies by city and date
4. Users can select seats (shown available/booked on a seat map)
5. Selected seats are temporarily held (5-minute lock) while the user pays
6. If payment fails or times out, seats are released
7. Confirmation notification sent after successful booking

**Your task:**
1. Identify all entities: Movie, Theater, Screen, Show, Seat, Booking, User
2. Use **Strategy** for pricing (different prices per seat category, weekend vs weekday)
3. Use **Observer** to send confirmation notifications
4. Use **State** for seat status: AVAILABLE -> TEMPORARILY_HELD -> BOOKED (or back to AVAILABLE if timeout)
5. Handle the double-booking edge case

---

### Exercise 4 (Sunday): Design a Food Delivery System (Zomato/Swiggy)

**Estimated time: 60 minutes (the most comprehensive problem)**

**What is the system?** Zomato or Swiggy — users browse restaurants, add items to cart, place orders, and track delivery in real-time. The system matches orders with delivery partners and handles payments.

**Requirements:**
1. Restaurants have menus with items (name, price, category, availability)
2. Users can search restaurants by cuisine, rating, or location
3. Users can add items to cart and place orders
4. The system assigns a delivery partner to each order
5. Order goes through states: PLACED -> CONFIRMED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
6. Each state transition triggers notifications (Observer pattern)
7. Different delivery fee strategies: flat fee, distance-based, free above Rs.500

**Your task:**
1. This problem uses ALL patterns you have learned:
   - **Factory** — create different notification types (push, SMS, email)
   - **Strategy** — delivery fee calculation, restaurant search/sort algorithms
   - **Observer** — order status notifications to user, restaurant, and delivery partner
   - **State** — order lifecycle
   - **Facade** — OrderService that orchestrates the entire order flow
   - **Singleton** — NotificationManager, DeliveryPartnerPool
2. Write the full system. This is the closest to a real Microsoft interview LLD question.

---

## Self-Check

Before considering yourself interview-ready, verify:

- [ ] I can design Parking Lot from scratch in 30 minutes (with Strategy for fees)
- [ ] I can implement LRU Cache with O(1) operations and explain why each data structure is needed
- [ ] I can design Library Management with Observer for waitlist notifications
- [ ] I can design Splitwise with Strategy for split types and debt simplification
- [ ] I can design Tic Tac Toe with proper win/draw detection
- [ ] I completed at least 2 of the 4 practice exercises
- [ ] For any given problem, I can: identify classes, define relationships, pick patterns, and write key classes in 30 minutes
- [ ] I can explain which patterns I used and WHY (not just "because it is a design pattern")
- [ ] I know all 10 design patterns from Weeks 2-3 and can apply them naturally
- [ ] I can discuss trade-offs and extension points for any design
