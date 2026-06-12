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
                                            |--- can hold ---> Vehicle (or null)
  |--- has many ---> ParkingTicket (active tickets)
  |--- has a ------> FeeStrategy (current pricing strategy)
```

- ParkingLot **HAS** ParkingFloors (composition — floors do not exist without the lot)
- ParkingFloor **HAS** ParkingSpots (composition)
- ParkingSpot **REFERS TO** a Vehicle when occupied (association)
- ParkingTicket **REFERS TO** both the Vehicle and the ParkingSpot (association)
- ParkingLot **USES** a FeeStrategy (strategy pattern)

### Full Code Solution

```java
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

// ============================================================
// ENUMS — Define the types/categories in the system
// Enums prevent typos. You cannot accidentally write "bke" instead of "BIKE"
// because the IDE will catch it. With strings, typos are silent bugs.
// ============================================================

enum VehicleType {
    // Types of vehicles that can park here.
    BIKE("bike"),
    CAR("car"),
    BUS("bus");

    private final String value;
    VehicleType(String value) { this.value = value; }
    public String getValue() { return value; }
}

enum SpotType {
    // Types of parking spots. Each matches a vehicle type.
    COMPACT("compact"),    // Small spots near the entrance — for bikes/scooters
    REGULAR("regular"),    // Standard spots — for cars
    LARGE("large");        // Extra-wide/tall spots — for buses and large SUVs

    private final String value;
    SpotType(String value) { this.value = value; }
    public String getValue() { return value; }
}

// ============================================================
// VEHICLE — represents a vehicle coming to park
// This is a simple data class. A vehicle knows its license plate
// and its type — nothing else. It does not know about parking spots
// or fees. (Single Responsibility Principle)
// ============================================================

class Vehicle {
    private String licensePlate;
    private VehicleType vehicleType;

    public Vehicle(String licensePlate, VehicleType vehicleType) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
    }

    public String getLicensePlate() { return licensePlate; }
    public VehicleType getVehicleType() { return vehicleType; }

    @Override
    public String toString() {
        String typeName = vehicleType.getValue().substring(0, 1).toUpperCase()
                + vehicleType.getValue().substring(1);
        return typeName + " [" + licensePlate + "]";
    }
}

// ============================================================
// PARKING SPOT — a single spot on a floor
// A spot knows its ID, type, floor, and whether it is occupied.
// It can park a vehicle and release it.
// ============================================================

class ParkingSpot {
    private String spotId;
    private SpotType spotType;
    private int floorNumber;
    private boolean isOccupied;
    private Vehicle vehicle; // Will hold a Vehicle object when occupied

    public ParkingSpot(String spotId, SpotType spotType, int floorNumber) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.floorNumber = floorNumber;
        this.isOccupied = false;
        this.vehicle = null;
    }

    /**
     * Attempt to park a vehicle in this spot.
     * Returns true if successful, false if spot is already occupied.
     */
    public boolean park(Vehicle vehicle) {
        if (this.isOccupied) {
            return false;
        }
        this.vehicle = vehicle;
        this.isOccupied = true;
        return true;
    }

    /**
     * Remove the vehicle from this spot.
     * Returns the vehicle that was parked here.
     */
    public Vehicle release() {
        Vehicle v = this.vehicle;
        this.vehicle = null;
        this.isOccupied = false;
        return v;
    }

    public String getSpotId() { return spotId; }
    public SpotType getSpotType() { return spotType; }
    public int getFloorNumber() { return floorNumber; }
    public boolean isOccupied() { return isOccupied; }
    public Vehicle getVehicle() { return vehicle; }

    @Override
    public String toString() {
        String status = isOccupied ? "OCCUPIED by " + vehicle : "AVAILABLE";
        return "Spot " + spotId + " (Floor " + floorNumber + ", " + spotType.getValue() + ") - " + status;
    }
}

// ============================================================
// PARKING FLOOR — a floor with many spots
// Knows how to find available spots and track capacity.
// ============================================================

class ParkingFloor {
    private int floorNumber;
    private List<ParkingSpot> spots; // List of ParkingSpot objects on this floor

    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>();
    }

    /** Add a parking spot to this floor. */
    public void addSpot(ParkingSpot spot) {
        spots.add(spot);
    }

    /**
     * Find the first available spot of the given type on this floor.
     * Returns null if no spot is available.
     */
    public ParkingSpot findAvailableSpot(SpotType spotType) {
        for (ParkingSpot spot : spots) {
            if (spot.getSpotType() == spotType && !spot.isOccupied()) {
                return spot;
            }
        }
        return null;
    }

    /** Count available spots, optionally filtered by type. */
    public int getAvailableCount(SpotType spotType) {
        int count = 0;
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied() && (spotType == null || spot.getSpotType() == spotType)) {
                count++;
            }
        }
        return count;
    }

    /** Count total spots, optionally filtered by type. */
    public int getTotalCount(SpotType spotType) {
        int count = 0;
        for (ParkingSpot spot : spots) {
            if (spotType == null || spot.getSpotType() == spotType) {
                count++;
            }
        }
        return count;
    }

    public int getFloorNumber() { return floorNumber; }
}

// ============================================================
// PARKING TICKET — issued at entry, closed at exit
// Links a vehicle to a spot and tracks entry/exit times.
// The ticket does NOT calculate fees — that is the FeeStrategy's job.
// ============================================================

class ParkingTicket {
    private static int ticketCounter = 0; // Class variable for generating unique ticket IDs

    private String ticketId;
    private Vehicle vehicle;
    private ParkingSpot spot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private boolean isPaid;
    private double amountPaid;

    public ParkingTicket(Vehicle vehicle, ParkingSpot spot) {
        ticketCounter++;
        this.ticketId = String.format("TKT-%04d", ticketCounter);
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
        this.isPaid = false;
        this.amountPaid = 0.0;
    }

    /** Mark the ticket as closed (vehicle is leaving). */
    public void close(LocalDateTime exitTime) {
        this.exitTime = (exitTime != null) ? exitTime : LocalDateTime.now();
    }

    /** Calculate how many hours the vehicle was parked. */
    public double getDurationHours() {
        LocalDateTime exit = (this.exitTime != null) ? this.exitTime : LocalDateTime.now();
        double duration = Duration.between(this.entryTime, exit).getSeconds() / 3600.0;
        return Math.max(duration, 0.5); // Minimum 30 minutes (half hour)
    }

    public String getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public boolean isPaid() { return isPaid; }
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public void setPaid(boolean paid) { this.isPaid = paid; }

    @Override
    public String toString() {
        String duration = (exitTime != null)
                ? String.format("%.1f hours", getDurationHours())
                : "ACTIVE";
        return "Ticket " + ticketId + ": " + vehicle + " at " + spot.getSpotId()
                + " (" + duration + ")";
    }
}

// ============================================================
// FEE STRATEGY — Strategy pattern for different pricing models
// The parking lot can switch pricing strategies at runtime.
// For example: normal pricing on weekdays, premium on weekends,
// flat rate during festivals, etc.
// ============================================================

/** Base class for all fee calculation strategies. */
abstract class FeeStrategy {
    public abstract double calculateFee(ParkingTicket ticket);
    public abstract String strategyName();
}

/**
 * Standard hourly pricing — different rates per vehicle type.
 * This is what most mall parking lots in India use.
 */
class HourlyFeeStrategy extends FeeStrategy {
    // Rate per hour for each spot type (in Rupees)
    private Map<SpotType, Integer> rates;

    public HourlyFeeStrategy() {
        rates = new HashMap<>();
        rates.put(SpotType.COMPACT, 20);   // Rs.20/hr for bikes
        rates.put(SpotType.REGULAR, 40);   // Rs.40/hr for cars
        rates.put(SpotType.LARGE, 80);     // Rs.80/hr for buses
    }

    @Override
    public double calculateFee(ParkingTicket ticket) {
        double hours = ticket.getDurationHours();
        int rate = rates.getOrDefault(ticket.getSpot().getSpotType(), 40);
        // Round up to the next hour (no partial hour discount)
        int billableHours = (int) Math.ceil(hours);
        return billableHours * rate;
    }

    @Override
    public String strategyName() {
        return "Hourly Rate";
    }
}

/**
 * Flat rate — pay once, park all day.
 * Used by airport parking lots (e.g., Mumbai airport parking).
 */
class FlatRateFeeStrategy extends FeeStrategy {
    private Map<SpotType, Integer> rates;

    public FlatRateFeeStrategy() {
        rates = new HashMap<>();
        rates.put(SpotType.COMPACT, 50);    // Rs.50 flat for bikes
        rates.put(SpotType.REGULAR, 200);   // Rs.200 flat for cars
        rates.put(SpotType.LARGE, 500);     // Rs.500 flat for buses
    }

    @Override
    public double calculateFee(ParkingTicket ticket) {
        return rates.getOrDefault(ticket.getSpot().getSpotType(), 200);
    }

    @Override
    public String strategyName() {
        return "Flat Rate (All Day)";
    }
}

/**
 * Weekend/holiday pricing — 1.5x the normal hourly rate.
 * Used during Big Billion Days sale at malls, weekends, festivals.
 */
class WeekendSurgeFeeStrategy extends FeeStrategy {
    private HourlyFeeStrategy baseStrategy;
    private double multiplier;

    public WeekendSurgeFeeStrategy(HourlyFeeStrategy baseStrategy, double multiplier) {
        this.baseStrategy = baseStrategy;
        this.multiplier = multiplier;
    }

    public WeekendSurgeFeeStrategy(HourlyFeeStrategy baseStrategy) {
        this(baseStrategy, 1.5);
    }

    @Override
    public double calculateFee(ParkingTicket ticket) {
        double baseFee = baseStrategy.calculateFee(ticket);
        return baseFee * multiplier;
    }

    @Override
    public String strategyName() {
        return "Weekend Surge (" + multiplier + "x)";
    }
}

// ============================================================
// PARKING LOT — the main class (Singleton in a real system)
// Manages floors, spots, tickets, and fee calculation.
// This is the "god class" for the parking system — the entry point
// that external code interacts with.
// ============================================================

class ParkingLot {
    // Mapping: which vehicle type needs which spot type
    private static final Map<VehicleType, SpotType> VEHICLE_TO_SPOT = new HashMap<>();
    static {
        VEHICLE_TO_SPOT.put(VehicleType.BIKE, SpotType.COMPACT);
        VEHICLE_TO_SPOT.put(VehicleType.CAR, SpotType.REGULAR);
        VEHICLE_TO_SPOT.put(VehicleType.BUS, SpotType.LARGE);
    }

    private String name;
    private List<ParkingFloor> floors;              // List of ParkingFloor objects
    private Map<String, ParkingTicket> activeTickets; // licensePlate -> ParkingTicket
    private FeeStrategy feeStrategy;

    public ParkingLot(String name, FeeStrategy feeStrategy) {
        this.name = name;
        this.floors = new ArrayList<>();
        this.activeTickets = new HashMap<>();
        this.feeStrategy = (feeStrategy != null) ? feeStrategy : new HourlyFeeStrategy();
    }

    public ParkingLot(String name) {
        this(name, new HourlyFeeStrategy());
    }

    /** Add a floor to the parking lot. */
    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    /**
     * Change the pricing strategy at runtime (Strategy pattern).
     * Example: switch to weekend pricing on Saturday morning.
     */
    public void setFeeStrategy(FeeStrategy strategy) {
        System.out.println("[ParkingLot] Pricing changed to: " + strategy.strategyName());
        this.feeStrategy = strategy;
    }

    /**
     * Search all floors for an available spot matching the vehicle type.
     * Returns the first available spot (closest to entrance = lowest floor).
     */
    public ParkingSpot findAvailableSpot(VehicleType vehicleType) {
        SpotType neededSpotType = VEHICLE_TO_SPOT.get(vehicleType);
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(neededSpotType);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }

    /**
     * Main entry point: vehicle arrives, find a spot, issue a ticket.
     */
    public ParkingTicket parkVehicle(Vehicle vehicle) {
        // Check if vehicle is already parked
        if (activeTickets.containsKey(vehicle.getLicensePlate())) {
            System.out.println("Vehicle " + vehicle.getLicensePlate() + " is already parked!");
            return null;
        }

        // Find an available spot
        ParkingSpot spot = findAvailableSpot(vehicle.getVehicleType());
        if (spot == null) {
            System.out.println("Sorry! No " + VEHICLE_TO_SPOT.get(vehicle.getVehicleType()).getValue()
                    + " spot available for " + vehicle);
            return null;
        }

        // Park the vehicle and issue a ticket
        spot.park(vehicle);
        ParkingTicket ticket = new ParkingTicket(vehicle, spot);
        activeTickets.put(vehicle.getLicensePlate(), ticket);

        System.out.println("Parked " + vehicle + " at Spot " + spot.getSpotId()
                + " (Floor " + spot.getFloorNumber() + ")");
        System.out.println("  Ticket: " + ticket.getTicketId());
        return ticket;
    }

    /**
     * Main exit point: vehicle is leaving, calculate fee, free the spot.
     */
    public double exitVehicle(String licensePlate, LocalDateTime exitTime) {
        ParkingTicket ticket = activeTickets.get(licensePlate);
        if (ticket == null) {
            System.out.println("No active ticket found for " + licensePlate);
            return 0;
        }

        // Close the ticket (set exit time)
        ticket.close(exitTime);

        // Calculate fee using the current strategy
        double fee = feeStrategy.calculateFee(ticket);
        ticket.setAmountPaid(fee);
        ticket.setPaid(true);

        // Free the spot
        ticket.getSpot().release();

        // Remove from active tickets
        activeTickets.remove(licensePlate);

        System.out.println("Vehicle " + licensePlate + " exited from Spot " + ticket.getSpot().getSpotId());
        System.out.printf("  Duration: %.1f hours%n", ticket.getDurationHours());
        System.out.println("  Strategy: " + feeStrategy.strategyName());
        System.out.printf("  Fee: Rs.%.0f%n", fee);
        return fee;
    }

    public double exitVehicle(String licensePlate) {
        return exitVehicle(licensePlate, null);
    }

    /** Show available spots per floor and type. */
    public Map<String, Map<String, String>> getAvailability() {
        Map<String, Map<String, String>> report = new LinkedHashMap<>();
        for (ParkingFloor floor : floors) {
            String floorKey = "Floor " + floor.getFloorNumber();
            Map<String, String> floorReport = new LinkedHashMap<>();
            for (SpotType spotType : SpotType.values()) {
                int available = floor.getAvailableCount(spotType);
                int total = floor.getTotalCount(spotType);
                if (total > 0) {
                    floorReport.put(spotType.getValue(), available + "/" + total);
                }
            }
            report.put(floorKey, floorReport);
        }
        return report;
    }

    /** Pretty-print the availability board (like the LED display at the mall entrance). */
    public void displayAvailability() {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("  " + name + " — Parking Availability");
        System.out.println("==================================================");
        Map<String, Map<String, String>> report = getAvailability();
        for (Map.Entry<String, Map<String, String>> floorEntry : report.entrySet()) {
            System.out.println("  " + floorEntry.getKey() + ":");
            for (Map.Entry<String, String> spotEntry : floorEntry.getValue().entrySet()) {
                String typeName = spotEntry.getKey().substring(0, 1).toUpperCase()
                        + spotEntry.getKey().substring(1);
                System.out.println("    " + typeName + ": " + spotEntry.getValue() + " available");
            }
        }
        System.out.println("==================================================");
        System.out.println();
    }
}

// ============================================================
// BUILD AND TEST THE SYSTEM
// ============================================================

class ParkingLotDemo {
    public static void main(String[] args) {
        // Create the parking lot
        ParkingLot lot = new ParkingLot("Phoenix Marketcity Parking", new HourlyFeeStrategy());

        // Create floors and spots
        for (int floorNum = 1; floorNum <= 3; floorNum++) { // 3 floors
            ParkingFloor floor = new ParkingFloor(floorNum);

            // Each floor has: 10 compact, 20 regular, 5 large spots
            for (int i = 1; i <= 10; i++) {
                floor.addSpot(new ParkingSpot(
                        String.format("F%d-C%02d", floorNum, i), SpotType.COMPACT, floorNum));
            }
            for (int i = 1; i <= 20; i++) {
                floor.addSpot(new ParkingSpot(
                        String.format("F%d-R%02d", floorNum, i), SpotType.REGULAR, floorNum));
            }
            for (int i = 1; i <= 5; i++) {
                floor.addSpot(new ParkingSpot(
                        String.format("F%d-L%02d", floorNum, i), SpotType.LARGE, floorNum));
            }

            lot.addFloor(floor);
        }

        // Show initial availability
        lot.displayAvailability();

        // Park some vehicles
        Vehicle car1 = new Vehicle("MH-02-AB-1234", VehicleType.CAR);
        Vehicle car2 = new Vehicle("MH-04-CD-5678", VehicleType.CAR);
        Vehicle bike1 = new Vehicle("MH-12-EF-9012", VehicleType.BIKE);
        Vehicle bus1 = new Vehicle("MH-01-GH-3456", VehicleType.BUS);

        lot.parkVehicle(car1);
        lot.parkVehicle(car2);
        lot.parkVehicle(bike1);
        lot.parkVehicle(bus1);

        // Show availability after parking
        lot.displayAvailability();

        // Simulate exit after some hours
        // (We fake the exit time to simulate 3 hours of parking)
        LocalDateTime exitTime = LocalDateTime.now().plusHours(3);
        lot.exitVehicle("MH-02-AB-1234", exitTime);
        // Fee: Rs.120 (3 hours x Rs.40/hr for regular spot)

        // Switch to weekend pricing and exit another vehicle
        lot.setFeeStrategy(new WeekendSurgeFeeStrategy(new HourlyFeeStrategy(), 1.5));
        lot.exitVehicle("MH-04-CD-5678", exitTime);
        // Fee: Rs.180 (3 hours x Rs.40/hr x 1.5 surge = Rs.180)
    }
}
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

```java
import java.util.HashMap;
import java.util.Map;

/**
 * A node in the doubly linked list.
 * Each node stores a key-value pair and pointers to the previous and next nodes.
 *
 * Why doubly linked? Because we need to remove nodes from the MIDDLE of the list
 * in O(1) time. With a singly linked list, removing a node requires traversing
 * from the head to find the previous node — that is O(n).
 * With a doubly linked list, each node knows its previous and next, so removal
 * is just updating 4 pointers — O(1).
 */
class Node {
    int key;
    int value;
    Node prev; // Pointer to the previous node
    Node next; // Pointer to the next node

    public Node(int key, int value) {
        this.key = key;
        this.value = value;
        this.prev = null;
        this.next = null;
    }

    public Node() {
        this(0, 0);
    }

    @Override
    public String toString() {
        return "(" + key + ": " + value + ")";
    }
}

/**
 * LRU Cache implementation using a HashMap + Doubly Linked List.
 *
 * The linked list maintains the ORDER of access:
 * - HEAD (front) = Most Recently Used (MRU)
 * - TAIL (back) = Least Recently Used (LRU)
 *
 * The HashMap provides O(1) lookup:
 * - key -> Node (so we can jump directly to any node in the list)
 *
 * Visual representation:
 *     HashMap: {1: Node1, 2: Node2, 3: Node3}
 *
 *     Doubly Linked List (most recent on left):
 *     [DUMMY_HEAD] <-> [Node3] <-> [Node1] <-> [Node2] <-> [DUMMY_TAIL]
 *                       (MRU)                    (LRU)
 *
 * Why dummy head and tail?
 * They simplify the code by eliminating edge cases. Without dummies,
 * every insert/remove must check "is this the head?" and "is this the tail?"
 * With dummies, there is ALWAYS a node before and after any real node.
 */
class LRUCache {
    private int capacity;
    private Map<Integer, Node> cache; // HashMap: key -> Node

    // Dummy head and tail nodes
    // These are sentinel nodes — they never hold real data.
    // They exist only to simplify insertion/removal logic.
    private Node head; // Dummy head (left boundary)
    private Node tail; // Dummy tail (right boundary)

    /**
     * Initialize the cache with a fixed capacity.
     * Once the cache has this many items, adding a new item
     * evicts the least recently used item.
     */
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();

        // Create dummy head and tail nodes
        this.head = new Node();
        this.tail = new Node();
        this.head.next = this.tail;
        this.tail.prev = this.head;
        // Initial state: head <-> tail (empty list)
    }

    /**
     * Remove a node from its current position in the linked list.
     * This is O(1) because we have direct access to prev and next.
     *
     * Before: ... <-> [A] <-> [node] <-> [B] <-> ...
     * After:  ... <-> [A] <-> [B] <-> ...
     */
    private void remove(Node node) {
        Node prevNode = node.prev;
        Node nextNode = node.next;
        prevNode.next = nextNode; // A's next now points to B
        nextNode.prev = prevNode; // B's prev now points to A
        // 'node' is now disconnected from the list
    }

    /**
     * Add a node right after the dummy head (making it the most recently used).
     * This is O(1) — just update 4 pointers.
     *
     * Before: head <-> [X] <-> ...
     * After:  head <-> [node] <-> [X] <-> ...
     */
    private void addToFront(Node node) {
        node.prev = this.head;         // node's prev = head
        node.next = this.head.next;    // node's next = whatever was after head
        this.head.next.prev = node;    // old first real node's prev = node
        this.head.next = node;         // head's next = node
    }

    /**
     * Get the value for a key.
     * If the key exists: return its value AND move it to the front (most recently used).
     * If the key does not exist: return -1.
     *
     * Time complexity: O(1)
     */
    public int get(int key) {
        if (!cache.containsKey(key)) {
            return -1;
        }

        // Key exists — find the node via HashMap (O(1))
        Node node = cache.get(key);

        // Move to front: remove from current position, add to front
        // This marks it as "most recently used"
        remove(node);
        addToFront(node);

        return node.value;
    }

    /**
     * Insert or update a key-value pair.
     * If key already exists: update the value and move to front.
     * If key does not exist:
     *     - If cache is full: evict the least recently used (node before tail)
     *     - Add the new key-value pair to the front
     *
     * Time complexity: O(1)
     */
    public void put(int key, int value) {
        if (cache.containsKey(key)) {
            // Key already exists — update value and move to front
            Node node = cache.get(key);
            node.value = value;
            remove(node);
            addToFront(node);
        } else {
            // Key does not exist — might need to evict
            if (cache.size() >= capacity) {
                // Cache is full! Evict the least recently used item.
                // The LRU item is the node just before the dummy tail.
                Node lruNode = tail.prev;
                remove(lruNode);
                cache.remove(lruNode.key); // Remove from HashMap too!
            }

            // Create a new node and add it to the front
            Node newNode = new Node(key, value);
            cache.put(key, newNode);
            addToFront(newNode);
        }
    }

    /** Print the cache contents from most recently used to least recently used. */
    public void display() {
        StringBuilder sb = new StringBuilder();
        Node current = head.next;
        boolean first = true;
        while (current != tail) {
            if (!first) sb.append(" -> ");
            sb.append(current.key + ":" + current.value);
            first = false;
            current = current.next;
        }
        System.out.println("Cache (" + cache.size() + "/" + capacity + "): [" + sb + "]");
        System.out.println("  (Left = Most Recent, Right = Least Recent)");
    }
}

// ============================================================
// WALKTHROUGH — step by step
// ============================================================

class LRUCacheDemo {
    public static void main(String[] args) {
        LRUCache cache = new LRUCache(3); // Capacity of 3

        // Put 3 items
        cache.put(1, 100);
        cache.display(); // [1:100]

        cache.put(2, 200);
        cache.display(); // [2:200 -> 1:100]

        cache.put(3, 300);
        cache.display(); // [3:300 -> 2:200 -> 1:100]
        // Cache is now FULL (3/3)

        // Access key 1 — moves it to the front (most recent)
        int value = cache.get(1);
        System.out.println("\nget(1) = " + value);
        cache.display(); // [1:100 -> 3:300 -> 2:200]
        // Notice: 1 moved to front, 2 is now the LRU

        // Put a new item (4) — cache is full, so LRU (key 2) gets evicted
        cache.put(4, 400);
        System.out.println("\nput(4, 400) — evicts key 2 (least recently used)");
        cache.display(); // [4:400 -> 1:100 -> 3:300]
        // Key 2 is GONE — it was the least recently used

        // Try to get evicted key
        value = cache.get(2);
        System.out.println("\nget(2) = " + value); // -1 (not found — it was evicted!)

        // Update existing key
        cache.put(3, 999);
        System.out.println("\nput(3, 999) — updates value and moves to front");
        cache.display(); // [3:999 -> 4:400 -> 1:100]
    }
}
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

```java
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.*;

enum BookStatus {
    AVAILABLE("available"),
    BORROWED("borrowed"),
    RESERVED("reserved"),
    LOST("lost");

    private final String value;
    BookStatus(String value) { this.value = value; }
    public String getValue() { return value; }
}

enum BookCategory {
    FICTION("Fiction"),
    NON_FICTION("Non-Fiction"),
    SCIENCE("Science"),
    TECHNOLOGY("Technology"),
    HISTORY("History");

    private final String value;
    BookCategory(String value) { this.value = value; }
    public String getValue() { return value; }
}

// ============================================================
// OBSERVER PATTERN — for waitlist notifications
// When a popular book is returned, all members waiting for it
// are automatically notified. Without Observer, the librarian
// would have to manually check the waitlist and call each member.
// ============================================================

/** Any class that wants to be notified when a book becomes available. */
interface BookObserver {
    void onBookAvailable(Book book);
    String getName();
}

/**
 * Represents a single copy of a book in the library.
 * Note: A library might have multiple COPIES of the same TITLE.
 * Each copy is a separate Book object with its own status.
 */
class Book {
    private String isbn;
    private String title;
    private String author;
    private BookCategory category;
    private int copyNumber;
    private BookStatus status;
    private List<BookObserver> waitlist; // Observer pattern — list of members waiting for this book

    public Book(String isbn, String title, String author, BookCategory category, int copyNumber) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.copyNumber = copyNumber;
        this.status = BookStatus.AVAILABLE;
        this.waitlist = new ArrayList<>();
    }

    public Book(String isbn, String title, String author, BookCategory category) {
        this(isbn, title, author, category, 1);
    }

    /** Subscribe a member to be notified when this book is available. */
    public void addToWaitlist(BookObserver observer) {
        if (!waitlist.contains(observer)) {
            waitlist.add(observer);
            System.out.println("  " + observer.getName() + " added to waitlist for '" + title + "'");
        }
    }

    /** Notify all waiting members that this book is now available. */
    public void notifyWaitlist() {
        for (BookObserver observer : waitlist) {
            observer.onBookAvailable(this);
        }
        waitlist.clear();
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public BookCategory getCategory() { return category; }
    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "'" + title + "' by " + author + " [" + status.getValue() + "]";
    }
}

/**
 * Tracks a single borrow transaction — who borrowed which book and when.
 * This is like the slip the librarian stamps with the due date.
 */
class BorrowRecord {
    private Book book;
    private Member member;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private double fine;

    public BorrowRecord(Book book, Member member, LocalDateTime borrowDate) {
        this.book = book;
        this.member = member;
        this.borrowDate = (borrowDate != null) ? borrowDate : LocalDateTime.now();
        this.dueDate = this.borrowDate.plusDays(14); // 2-week borrowing period
        this.returnDate = null;
        this.fine = 0.0;
    }

    public BorrowRecord(Book book, Member member) {
        this(book, member, null);
    }

    /** Calculate late return fine: Rs.5 per day after due date. */
    public double calculateFine(LocalDateTime returnDate) {
        LocalDateTime actualReturn = (returnDate != null) ? returnDate : LocalDateTime.now();
        if (actualReturn.isAfter(dueDate)) {
            long daysLate = ChronoUnit.DAYS.between(dueDate, actualReturn);
            this.fine = daysLate * 5; // Rs.5 per day
        }
        return this.fine;
    }

    public Book getBook() { return book; }
    public Member getMember() { return member; }
    public LocalDateTime getDueDate() { return dueDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }
    public double getFine() { return fine; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        String status = (returnDate != null) ? "RETURNED" : "DUE: " + dueDate.format(fmt);
        return "'" + book.getTitle() + "' -> " + member.getName() + " (" + status + ")";
    }
}

/**
 * A library member who can borrow and return books.
 * Also acts as an Observer — gets notified when waitlisted books become available.
 */
class Member implements BookObserver {
    public static final int MAX_BOOKS = 5; // Maximum books a member can borrow at once

    private String memberId;
    private String name;
    private String email;
    private List<BorrowRecord> borrowedBooks;    // List of BorrowRecords
    private List<BorrowRecord> borrowHistory;    // All past borrow records
    private double totalFines;

    public Member(String memberId, String name, String email) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.borrowedBooks = new ArrayList<>();
        this.borrowHistory = new ArrayList<>();
        this.totalFines = 0.0;
    }

    /** Check if member can borrow more books. */
    public boolean canBorrow() {
        long activeBorrows = borrowedBooks.stream()
                .filter(r -> r.getReturnDate() == null)
                .count();
        return activeBorrows < MAX_BOOKS;
    }

    /** Observer callback — called when a waitlisted book is returned. */
    @Override
    public void onBookAvailable(Book book) {
        System.out.println("  [NOTIFICATION] " + name + ": '" + book.getTitle()
                + "' is now available! Hurry to the library!");
    }

    @Override
    public String getName() { return name; }
    public String getMemberId() { return memberId; }
    public List<BorrowRecord> getBorrowedBooks() { return borrowedBooks; }
    public List<BorrowRecord> getBorrowHistory() { return borrowHistory; }
    public double getTotalFines() { return totalFines; }
    public void addFine(double fine) { this.totalFines += fine; }

    @Override
    public String toString() {
        long active = borrowedBooks.stream().filter(r -> r.getReturnDate() == null).count();
        return "Member " + name + " (" + active + "/" + MAX_BOOKS + " books borrowed)";
    }
}

/**
 * The main library system — manages books, members, and borrowing.
 * This is the Facade that external code interacts with.
 */
class Library {
    private String name;
    private List<Book> books;          // All book copies in the library
    private Map<String, Member> members; // memberId -> Member

    public Library(String name) {
        this.name = name;
        this.books = new ArrayList<>();
        this.members = new HashMap<>();
    }

    /** Add a book copy to the library's collection. */
    public void addBook(Book book) {
        books.add(book);
    }

    /** Register a new library member. */
    public void registerMember(Member member) {
        members.put(member.getMemberId(), member);
        System.out.println("Registered: " + member.getName() + " (" + member.getMemberId() + ")");
    }

    /** Search for books by title (case-insensitive partial match). */
    public List<Book> searchByTitle(String title) {
        List<Book> results = new ArrayList<>();
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(title.toLowerCase())) {
                results.add(b);
            }
        }
        return results;
    }

    /** Search for books by author name. */
    public List<Book> searchByAuthor(String author) {
        List<Book> results = new ArrayList<>();
        for (Book b : books) {
            if (b.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                results.add(b);
            }
        }
        return results;
    }

    /** Search for books by category. */
    public List<Book> searchByCategory(BookCategory category) {
        List<Book> results = new ArrayList<>();
        for (Book b : books) {
            if (b.getCategory() == category) {
                results.add(b);
            }
        }
        return results;
    }

    /** Find an available copy of a book by title. */
    public Book searchAvailable(String title) {
        List<Book> matches = searchByTitle(title);
        for (Book book : matches) {
            if (book.getStatus() == BookStatus.AVAILABLE) {
                return book;
            }
        }
        return null;
    }

    /**
     * A member borrows a book. The core transaction of the library.
     */
    public BorrowRecord borrowBook(String memberId, String title, LocalDateTime borrowDate) {
        // Validate member
        Member member = members.get(memberId);
        if (member == null) {
            System.out.println("Member " + memberId + " not found!");
            return null;
        }

        // Check if member can borrow
        if (!member.canBorrow()) {
            System.out.println(member.getName() + " has reached the maximum borrowing limit ("
                    + Member.MAX_BOOKS + " books)!");
            return null;
        }

        // Find an available copy
        Book book = searchAvailable(title);
        if (book == null) {
            System.out.println("No available copy of '" + title + "' found.");
            // Offer to add to waitlist
            List<Book> allCopies = searchByTitle(title);
            if (!allCopies.isEmpty()) {
                System.out.println("  All " + allCopies.size() + " copies are currently borrowed.");
                allCopies.get(0).addToWaitlist(member);
            }
            return null;
        }

        // Create the borrow record
        BorrowRecord record = new BorrowRecord(book, member, borrowDate);
        book.setStatus(BookStatus.BORROWED);
        member.getBorrowedBooks().add(record);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        System.out.println(member.getName() + " borrowed '" + book.getTitle() + "'");
        System.out.println("  Due date: " + record.getDueDate().format(fmt));
        return record;
    }

    public BorrowRecord borrowBook(String memberId, String title) {
        return borrowBook(memberId, title, null);
    }

    /**
     * A member returns a book. Calculates fine if late.
     * Notifies waitlisted members if any.
     */
    public double returnBook(String memberId, String title, LocalDateTime returnDate) {
        Member member = members.get(memberId);
        if (member == null) {
            System.out.println("Member " + memberId + " not found!");
            return 0;
        }

        // Find the active borrow record for this book
        BorrowRecord record = null;
        for (BorrowRecord r : member.getBorrowedBooks()) {
            if (r.getBook().getTitle().equalsIgnoreCase(title) && r.getReturnDate() == null) {
                record = r;
                break;
            }
        }

        if (record == null) {
            System.out.println(member.getName() + " does not have '" + title + "' borrowed!");
            return 0;
        }

        // Process the return
        LocalDateTime actualReturn = (returnDate != null) ? returnDate : LocalDateTime.now();
        record.setReturnDate(actualReturn);
        double fine = record.calculateFine(actualReturn);
        member.addFine(fine);

        // Make the book available again
        record.getBook().setStatus(BookStatus.AVAILABLE);

        System.out.println(member.getName() + " returned '" + record.getBook().getTitle() + "'");
        if (fine > 0) {
            long daysLate = ChronoUnit.DAYS.between(record.getDueDate(), actualReturn);
            System.out.printf("  Late by %d days. Fine: Rs.%.0f%n", daysLate, fine);
        } else {
            System.out.println("  Returned on time. No fine.");
        }

        // Notify waitlisted members (Observer pattern in action!)
        record.getBook().notifyWaitlist();

        // Move to history
        member.getBorrowHistory().add(record);

        return fine;
    }

    public double returnBook(String memberId, String title) {
        return returnBook(memberId, title, null);
    }
}

// ============================================================
// TEST THE SYSTEM
// ============================================================

class LibraryDemo {
    public static void main(String[] args) {
        // Create the library
        Library library = new Library("Central Library, IIT Bombay");

        // Add books
        library.addBook(new Book("978-0-13-468599-1", "Clean Code", "Robert C. Martin",
                BookCategory.TECHNOLOGY));
        library.addBook(new Book("978-0-13-468599-1", "Clean Code", "Robert C. Martin",
                BookCategory.TECHNOLOGY, 2));
        library.addBook(new Book("978-0-06-112008-4", "To Kill a Mockingbird", "Harper Lee",
                BookCategory.FICTION));
        library.addBook(new Book("978-0-07-013151-4", "The C Programming Language", "Kernighan & Ritchie",
                BookCategory.TECHNOLOGY));

        // Register members
        Member sheetal = new Member("M001", "Sheetal", "sheetal@example.com");
        Member rahul = new Member("M002", "Rahul", "rahul@example.com");
        library.registerMember(sheetal);
        library.registerMember(rahul);

        // Sheetal borrows Clean Code
        library.borrowBook("M001", "Clean Code");
        // Sheetal borrowed 'Clean Code'. Due: 17-Jun-2026

        // Rahul also wants Clean Code — borrows the second copy
        library.borrowBook("M002", "Clean Code");
        // Rahul borrowed 'Clean Code'. Due: 17-Jun-2026

        // A third person wants Clean Code — but both copies are borrowed!
        Member priya = new Member("M003", "Priya", "priya@example.com");
        library.registerMember(priya);
        library.borrowBook("M003", "Clean Code");
        // No available copy. Priya added to waitlist.

        // Sheetal returns Clean Code (on time)
        library.returnBook("M001", "Clean Code");
        // Sheetal returned 'Clean Code'. No fine.
        // [NOTIFICATION] Priya: 'Clean Code' is now available! Hurry to the library!

        // Rahul returns Clean Code LATE (5 days late)
        LocalDateTime lateReturn = LocalDateTime.now().plusDays(19); // 19 days (5 days late)
        library.returnBook("M002", "Clean Code", lateReturn);
        // Rahul returned 'Clean Code'. Late by 5 days. Fine: Rs.25
    }
}
```

### Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Observer** | `Book.waitlist` and `Member.onBookAvailable()` | When a popular book is returned, all waitlisted members are automatically notified without the library having to manually track who wants which book. |
| **Facade** | `Library` class | Provides a simple interface (`borrowBook`, `returnBook`, `search`) hiding the complexity of managing books, members, records, and notifications. |
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

```java
import java.time.LocalDateTime;
import java.util.*;

enum SplitType {
    EQUAL("equal"),
    EXACT("exact"),
    PERCENTAGE("percentage");

    private final String value;
    SplitType(String value) { this.value = value; }
    public String getValue() { return value; }
}

// ============================================================
// STRATEGY PATTERN — Different ways to split an expense
// Why Strategy? Because the splitting algorithm varies by type,
// and we might add new types later (e.g., by shares, by weight)
// ============================================================

abstract class SplitStrategy {
    /**
     * Returns a map of {userId: amountOwed} for each participant.
     * The amounts should sum to totalAmount.
     */
    public abstract Map<String, Double> calculateSplits(double totalAmount,
            List<String> participants, Map<String, Double> splitDetails);
}

/** Split equally among all participants. */
class EqualSplitStrategy extends SplitStrategy {
    @Override
    public Map<String, Double> calculateSplits(double totalAmount,
            List<String> participants, Map<String, Double> splitDetails) {
        double perPerson = Math.round(totalAmount / participants.size() * 100.0) / 100.0;
        Map<String, Double> splits = new LinkedHashMap<>();
        for (String p : participants) {
            splits.put(p, perPerson);
        }
        // Handle rounding — give the remainder to the first person
        double remainder = Math.round((totalAmount - (perPerson * participants.size())) * 100.0) / 100.0;
        if (remainder != 0) {
            splits.put(participants.get(0),
                    Math.round((splits.get(participants.get(0)) + remainder) * 100.0) / 100.0);
        }
        return splits;
    }
}

/** Each participant pays an exact specified amount. */
class ExactSplitStrategy extends SplitStrategy {
    @Override
    public Map<String, Double> calculateSplits(double totalAmount,
            List<String> participants, Map<String, Double> splitDetails) {
        if (splitDetails == null || splitDetails.isEmpty()) {
            throw new IllegalArgumentException("Exact split requires splitDetails {userId: amount}");
        }
        // Validate that amounts sum to total
        double totalSpecified = splitDetails.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalSpecified - totalAmount) > 0.01) {
            throw new IllegalArgumentException(
                    "Specified amounts (" + totalSpecified + ") do not sum to total (" + totalAmount + ")");
        }
        return splitDetails;
    }
}

/** Each participant pays a specified percentage. */
class PercentageSplitStrategy extends SplitStrategy {
    @Override
    public Map<String, Double> calculateSplits(double totalAmount,
            List<String> participants, Map<String, Double> splitDetails) {
        if (splitDetails == null || splitDetails.isEmpty()) {
            throw new IllegalArgumentException("Percentage split requires splitDetails {userId: percentage}");
        }
        // Validate percentages sum to 100
        double totalPct = splitDetails.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalPct - 100) > 0.01) {
            throw new IllegalArgumentException("Percentages must sum to 100, got " + totalPct);
        }
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : splitDetails.entrySet()) {
            result.put(entry.getKey(), Math.round(totalAmount * entry.getValue() / 100.0 * 100.0) / 100.0);
        }
        return result;
    }
}

// ============================================================
// CORE CLASSES
// ============================================================

class User {
    private String userId;
    private String name;
    private String phone;

    public User(String userId, String name, String phone) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}

/**
 * A single expense — someone paid for something, and it is split among members.
 * Example: Rahul paid Rs.3000 for the cab, split equally among 4 friends.
 */
class Expense {
    private static int counter = 0;

    // Factory for creating the right strategy
    private static final Map<SplitType, SplitStrategy> SPLIT_STRATEGIES = new HashMap<>();
    static {
        SPLIT_STRATEGIES.put(SplitType.EQUAL, new EqualSplitStrategy());
        SPLIT_STRATEGIES.put(SplitType.EXACT, new ExactSplitStrategy());
        SPLIT_STRATEGIES.put(SplitType.PERCENTAGE, new PercentageSplitStrategy());
    }

    private String expenseId;
    private String description;
    private double totalAmount;
    private User paidBy;
    private SplitType splitType;
    private LocalDateTime createdAt;
    private Map<String, Double> splits;
    private Map<String, User> participants;

    public Expense(String description, double totalAmount, User paidBy,
                   SplitType splitType, List<User> participantList,
                   Map<String, Double> splitDetails) {
        counter++;
        this.expenseId = String.format("EXP-%04d", counter);
        this.description = description;
        this.totalAmount = totalAmount;
        this.paidBy = paidBy;
        this.splitType = splitType;
        this.createdAt = LocalDateTime.now();

        // Use Strategy pattern to calculate how much each person owes
        SplitStrategy strategy = SPLIT_STRATEGIES.get(splitType);
        List<String> participantIds = new ArrayList<>();
        this.participants = new LinkedHashMap<>();
        for (User p : participantList) {
            participantIds.add(p.getUserId());
            this.participants.put(p.getUserId(), p);
        }
        this.splits = strategy.calculateSplits(totalAmount, participantIds, splitDetails);
    }

    public void display() {
        System.out.println("\n  " + expenseId + ": " + description);
        System.out.printf("  Total: Rs.%.2f | Paid by: %s%n", totalAmount, paidBy.getName());
        System.out.println("  Split (" + splitType.getValue() + "):");
        for (Map.Entry<String, Double> entry : splits.entrySet()) {
            String name = participants.containsKey(entry.getKey())
                    ? participants.get(entry.getKey()).getName() : entry.getKey();
            System.out.printf("    %s: Rs.%.2f%n", name, entry.getValue());
        }
    }

    public double getTotalAmount() { return totalAmount; }
    public User getPaidBy() { return paidBy; }
    public Map<String, Double> getSplits() { return splits; }
}

/**
 * A group of people who share expenses.
 * Example: "Goa Trip 2026" with 4 friends.
 */
class Group {
    private String groupId;
    private String name;
    private User createdBy;
    private Map<String, User> members;       // userId -> User
    private List<Expense> expenses;          // List of Expense objects
    private List<double[]> settlements;      // List of [fromIndex, toIndex, amount] — using String arrays instead
    private List<String[]> settlementData;   // List of {fromUserId, toUserId, amount}

    public Group(String groupId, String name, User createdBy) {
        this.groupId = groupId;
        this.name = name;
        this.createdBy = createdBy;
        this.members = new LinkedHashMap<>();
        this.expenses = new ArrayList<>();
        this.settlementData = new ArrayList<>();

        // Add creator as first member
        addMember(createdBy);
    }

    public void addMember(User user) {
        members.put(user.getUserId(), user);
        System.out.println("  " + user.getName() + " joined group '" + name + "'");
    }

    /**
     * Add an expense to the group.
     * If participants not specified, splits among ALL group members.
     */
    public Expense addExpense(String description, double totalAmount, User paidBy,
                              SplitType splitType, List<User> participants,
                              Map<String, Double> splitDetails) {
        if (participants == null) {
            participants = new ArrayList<>(members.values());
        }
        Expense expense = new Expense(description, totalAmount, paidBy,
                splitType, participants, splitDetails);
        expenses.add(expense);
        expense.display();
        return expense;
    }

    public Expense addExpense(String description, double totalAmount, User paidBy) {
        return addExpense(description, totalAmount, paidBy, SplitType.EQUAL, null, null);
    }

    /**
     * Calculate net balance for each member.
     * Positive = others owe you money (you overpaid)
     * Negative = you owe money (you underpaid)
     *
     * Example:
     *   Rahul paid Rs.3000 for 4 people (Rs.750 each)
     *   Rahul's balance: +2250 (he paid 3000 but his share was only 750)
     *   Each other person's balance: -750 (they owe their share)
     */
    public Map<String, Double> getBalances() {
        Map<String, Double> balances = new LinkedHashMap<>();
        for (String uid : members.keySet()) {
            balances.put(uid, 0.0);
        }

        for (Expense expense : expenses) {
            // The payer PAID the full amount (positive)
            String payerId = expense.getPaidBy().getUserId();
            balances.put(payerId, balances.getOrDefault(payerId, 0.0) + expense.getTotalAmount());

            // Each participant OWES their split amount (negative)
            for (Map.Entry<String, Double> entry : expense.getSplits().entrySet()) {
                balances.put(entry.getKey(),
                        balances.getOrDefault(entry.getKey(), 0.0) - entry.getValue());
            }
        }

        // Account for settlements
        for (String[] s : settlementData) {
            String fromId = s[0];
            String toId = s[1];
            double amount = Double.parseDouble(s[2]);
            balances.put(fromId, balances.get(fromId) + amount);  // Payer reduces their debt
            balances.put(toId, balances.get(toId) - amount);      // Receiver's credit decreases
        }

        // Round to avoid floating point artifacts
        Map<String, Double> rounded = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            rounded.put(entry.getKey(), Math.round(entry.getValue() * 100.0) / 100.0);
        }
        return rounded;
    }

    /**
     * Calculate the MINIMUM number of transactions needed to settle all debts.
     *
     * Algorithm (greedy):
     * 1. Calculate net balance for each person
     * 2. Separate into creditors (positive balance) and debtors (negative balance)
     * 3. Match the biggest debtor with the biggest creditor
     * 4. Transfer the minimum of the two amounts
     * 5. Repeat until all balances are zero
     *
     * This minimizes the number of transactions.
     * Example: If A owes B Rs.100 and B owes C Rs.100,
     * instead of two transactions, just A pays C Rs.100 directly.
     */
    public List<String[]> getSimplifiedDebts() {
        Map<String, Double> balances = getBalances();

        // Separate into creditors (owed money) and debtors (owe money)
        List<Object[]> creditors = new ArrayList<>(); // {userId, amount} — positive balances
        List<Object[]> debtors = new ArrayList<>();   // {userId, amount} — negative balances

        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            if (entry.getValue() > 0.01) {
                creditors.add(new Object[]{entry.getKey(), entry.getValue()});
            } else if (entry.getValue() < -0.01) {
                debtors.add(new Object[]{entry.getKey(), -entry.getValue()}); // Store as positive for easier math
            }
        }

        // Sort both by amount (descending) for greedy matching
        creditors.sort((a, b) -> Double.compare((Double) b[1], (Double) a[1]));
        debtors.sort((a, b) -> Double.compare((Double) b[1], (Double) a[1]));

        List<String[]> transactions = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            String debtorId = (String) debtors.get(i)[0];
            double debt = (Double) debtors.get(i)[1];
            String creditorId = (String) creditors.get(j)[0];
            double credit = (Double) creditors.get(j)[1];

            // Transfer the smaller of the two amounts
            double transfer = Math.min(debt, credit);
            transactions.add(new String[]{debtorId, creditorId,
                    String.valueOf(Math.round(transfer * 100.0) / 100.0)});

            // Update remaining balances
            debtors.get(i)[1] = (Double) debtors.get(i)[1] - transfer;
            creditors.get(j)[1] = (Double) creditors.get(j)[1] - transfer;

            // Move to next debtor/creditor if settled
            if ((Double) debtors.get(i)[1] < 0.01) i++;
            if ((Double) creditors.get(j)[1] < 0.01) j++;
        }

        return transactions;
    }

    /** Record a settlement (payment) between two members. */
    public void settle(String fromUserId, String toUserId, double amount) {
        settlementData.add(new String[]{fromUserId, toUserId, String.valueOf(amount)});
        String fromName = members.get(fromUserId).getName();
        String toName = members.get(toUserId).getName();
        System.out.printf("  Settlement: %s paid Rs.%.2f to %s%n", fromName, amount, toName);
    }

    /** Display who owes what. */
    public void showBalances() {
        Map<String, Double> balances = getBalances();
        System.out.println();
        System.out.println("==================================================");
        System.out.println("  Balances for '" + name + "'");
        System.out.println("==================================================");
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            String memberName = members.get(entry.getKey()).getName();
            double balance = entry.getValue();
            if (balance > 0.01) {
                System.out.printf("  %s: +Rs.%.2f (is owed)%n", memberName, balance);
            } else if (balance < -0.01) {
                System.out.printf("  %s: -Rs.%.2f (owes)%n", memberName, Math.abs(balance));
            } else {
                System.out.println("  " + memberName + ": settled up!");
            }
        }
    }

    /** Show the minimum transactions needed to settle all debts. */
    public void showSimplifiedDebts() {
        List<String[]> transactions = getSimplifiedDebts();
        System.out.println("\n  Simplified settlements:");
        if (transactions.isEmpty()) {
            System.out.println("  Everyone is settled up!");
            return;
        }
        for (String[] t : transactions) {
            String fromName = members.get(t[0]).getName();
            String toName = members.get(t[1]).getName();
            System.out.printf("  %s -> pays Rs.%s -> %s%n", fromName, t[2], toName);
        }
    }
}

// ============================================================
// TEST THE SYSTEM — Goa Trip Example
// ============================================================

class SplitwiseDemo {
    public static void main(String[] args) {
        // Create users
        User sheetal = new User("U1", "Sheetal", "9876543210");
        User rahul = new User("U2", "Rahul", "9876543211");
        User priya = new User("U3", "Priya", "9876543212");
        User amit = new User("U4", "Amit", "9876543213");

        // Create a group
        System.out.println("Creating group: Goa Trip 2026");
        Group goaTrip = new Group("G1", "Goa Trip 2026", sheetal);
        goaTrip.addMember(rahul);
        goaTrip.addMember(priya);
        goaTrip.addMember(amit);

        // Add expenses
        System.out.println("\n--- Adding Expenses ---");

        // Sheetal paid for the hotel (split equally among all 4)
        goaTrip.addExpense("Hotel (2 nights)", 12000, sheetal);
        // Each person owes Rs.3000

        // Rahul paid for the cab (split equally)
        goaTrip.addExpense("Cab (Airport + Sightseeing)", 3200, rahul);
        // Each person owes Rs.800

        // Priya paid for dinner, but Amit ate more, so split by percentage
        Map<String, Double> dinnerSplit = new LinkedHashMap<>();
        dinnerSplit.put("U1", 20.0);
        dinnerSplit.put("U2", 20.0);
        dinnerSplit.put("U3", 25.0);
        dinnerSplit.put("U4", 35.0);
        goaTrip.addExpense("Dinner at Thalassa", 4000, priya,
                SplitType.PERCENTAGE, null, dinnerSplit);
        // Sheetal: Rs.800, Rahul: Rs.800, Priya: Rs.1000, Amit: Rs.1400

        // Amit paid for water sports, but only 3 participated (not Priya)
        List<User> waterSportsParticipants = Arrays.asList(sheetal, rahul, amit);
        goaTrip.addExpense("Water Sports", 6000, amit,
                SplitType.EQUAL, waterSportsParticipants, null);
        // Sheetal: Rs.2000, Rahul: Rs.2000, Amit: Rs.2000

        // Show balances
        goaTrip.showBalances();

        // Show simplified settlements
        goaTrip.showSimplifiedDebts();
    }
}
```

### Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `SplitStrategy` and subclasses | Different ways to split expenses (equal, exact, percentage). New split types can be added without changing existing code. |
| **Factory** (implicit) | `SPLIT_STRATEGIES` map | Maps SplitType enum to the right strategy object. |
| **Facade** | `Group` class | Provides simple methods like `addExpense()` and `showBalances()` that hide the complexity of balance calculation and debt simplification. |

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

```java
enum Symbol {
    X("X"),
    O("O"),
    EMPTY(" ");

    private final String value;
    Symbol(String value) { this.value = value; }
    public String getValue() { return value; }
}

class Player {
    /** Represents a player in the game. */
    private String name;
    private Symbol symbol;
    private int wins;

    public Player(String name, Symbol symbol) {
        this.name = name;
        this.symbol = symbol;
        this.wins = 0;
    }

    public String getName() { return name; }
    public Symbol getSymbol() { return symbol; }
    public int getWins() { return wins; }
    public void incrementWins() { wins++; }

    @Override
    public String toString() {
        return name + " (" + symbol.getValue() + ")";
    }
}

/**
 * The game board — an NxN grid.
 * Handles placing symbols, checking validity, and detecting wins.
 */
class Board {
    private int size;
    private Symbol[][] grid;
    private int movesMade;

    public Board(int size) {
        this.size = size;
        // Create a 2D grid filled with EMPTY
        this.grid = new Symbol[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = Symbol.EMPTY;
            }
        }
        this.movesMade = 0;
    }

    public Board() {
        this(3);
    }

    /**
     * Place a symbol on the board.
     * Returns true if successful, false if invalid move.
     */
    public boolean place(int row, int col, Symbol symbol) {
        // Validate bounds
        if (row < 0 || row >= size || col < 0 || col >= size) {
            System.out.println("  Invalid! Row and column must be between 0 and " + (size - 1));
            return false;
        }

        // Validate cell is empty
        if (grid[row][col] != Symbol.EMPTY) {
            System.out.println("  Invalid! Cell (" + row + "," + col + ") is already occupied by "
                    + grid[row][col].getValue());
            return false;
        }

        grid[row][col] = symbol;
        movesMade++;
        return true;
    }

    /**
     * Check if the given symbol has won.
     * Must check: all rows, all columns, both diagonals.
     */
    public boolean checkWinner(Symbol symbol) {
        int n = size;

        // Check each row — does any row have all cells matching the symbol?
        for (int row = 0; row < n; row++) {
            boolean allMatch = true;
            for (int col = 0; col < n; col++) {
                if (grid[row][col] != symbol) { allMatch = false; break; }
            }
            if (allMatch) return true;
        }

        // Check each column
        for (int col = 0; col < n; col++) {
            boolean allMatch = true;
            for (int row = 0; row < n; row++) {
                if (grid[row][col] != symbol) { allMatch = false; break; }
            }
            if (allMatch) return true;
        }

        // Check main diagonal (top-left to bottom-right)
        boolean allMatch = true;
        for (int i = 0; i < n; i++) {
            if (grid[i][i] != symbol) { allMatch = false; break; }
        }
        if (allMatch) return true;

        // Check anti-diagonal (top-right to bottom-left)
        allMatch = true;
        for (int i = 0; i < n; i++) {
            if (grid[i][n - 1 - i] != symbol) { allMatch = false; break; }
        }
        if (allMatch) return true;

        return false;
    }

    /** Check if the board is completely filled (draw condition). */
    public boolean isFull() {
        return movesMade == size * size;
    }

    /** Clear the board for a new game. */
    public void reset() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = Symbol.EMPTY;
            }
        }
        movesMade = 0;
    }

    /** Pretty-print the board. */
    public void display() {
        int n = size;
        System.out.println();
        for (int row = 0; row < n; row++) {
            StringBuilder sb = new StringBuilder();
            for (int col = 0; col < n; col++) {
                if (col > 0) sb.append("|");
                sb.append(" " + grid[row][col].getValue() + " ");
            }
            System.out.println(sb);
            if (row < n - 1) {
                StringBuilder sep = new StringBuilder();
                for (int i = 0; i < 4 * n - 1; i++) sep.append("-");
                System.out.println(sep);
            }
        }
        System.out.println();
    }
}

/**
 * The game controller — manages turns, validates moves, and determines the outcome.
 * Uses the State pattern concept: the game has states (IN_PROGRESS, WON, DRAW).
 */
class Game {
    private Board board;
    private Player[] players;
    private int currentTurn; // Index into players (0 or 1)
    private Player winner;
    private boolean isOver;

    public Game(String player1Name, String player2Name, int boardSize) {
        this.board = new Board(boardSize);
        this.players = new Player[] {
            new Player(player1Name, Symbol.X),
            new Player(player2Name, Symbol.O),
        };
        this.currentTurn = 0;
        this.winner = null;
        this.isOver = false;
    }

    public Game(String player1Name, String player2Name) {
        this(player1Name, player2Name, 3);
    }

    /** Return the player whose turn it is. */
    public Player currentPlayer() {
        return players[currentTurn];
    }

    /**
     * The current player makes a move at (row, col).
     * Returns true if the move was valid and processed.
     */
    public boolean makeMove(int row, int col) {
        if (isOver) {
            System.out.println("Game is already over!");
            return false;
        }

        Player player = currentPlayer();
        System.out.println(player.getName() + "'s turn (" + player.getSymbol().getValue()
                + "): placing at (" + row + ", " + col + ")");

        // Try to place the symbol
        if (!board.place(row, col, player.getSymbol())) {
            return false;
        }

        // Display the board after the move
        board.display();

        // Check if this move wins the game
        if (board.checkWinner(player.getSymbol())) {
            this.winner = player;
            this.isOver = true;
            player.incrementWins();
            System.out.println("==============================");
            System.out.println("  " + player.getName() + " WINS!");
            System.out.println("==============================");
            return true;
        }

        // Check for draw
        if (board.isFull()) {
            this.isOver = true;
            System.out.println("==============================");
            System.out.println("  It's a DRAW!");
            System.out.println("==============================");
            return true;
        }

        // Switch turns
        currentTurn = 1 - currentTurn; // Toggle between 0 and 1
        return true;
    }

    /** Reset for a new game, keeping the same players. */
    public void resetGame() {
        board.reset();
        currentTurn = 0;
        winner = null;
        isOver = false;
    }

    public Board getBoard() { return board; }
    public Player[] getPlayers() { return players; }
}

// ============================================================
// PLAY A GAME
// ============================================================

class TicTacToeDemo {
    public static void main(String[] args) {
        Game game = new Game("Sheetal", "Rahul");

        System.out.println("Let's play Tic Tac Toe!");
        System.out.println(game.getPlayers()[0] + " vs " + game.getPlayers()[1]);
        game.getBoard().display();

        // Simulate a game where Sheetal (X) wins with a diagonal
        game.makeMove(0, 0); // Sheetal: X at top-left
        game.makeMove(0, 1); // Rahul:  O at top-center
        game.makeMove(1, 1); // Sheetal: X at center (diagonal building)
        game.makeMove(0, 2); // Rahul:  O at top-right
        game.makeMove(2, 2); // Sheetal: X at bottom-right -> WINS! (diagonal)
    }
}
```

### Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **State** (conceptual) | Game has states: IN_PROGRESS, WON, DRAW | The game behaves differently based on whether it is in progress or over. `makeMove` rejects moves when the game is over. |

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
