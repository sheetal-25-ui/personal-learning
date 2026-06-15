# Week 06 — More LLD Problems (6 Full Problems with Java Code)

Each problem covers: system description, requirements, entities, full Java code with comments, and design patterns used. Examples use Indian context where relevant.

---

## Problem 1: Chess Game (Microsoft / Google)

### System Description

Design a Chess game for two players on a standard 8x8 board. Each player has 16 pieces — King, Queen, 2 Rooks, 2 Bishops, 2 Knights, 8 Pawns. Players alternate turns. The game detects check, checkmate, and stalemate.

Think of it like playing chess on your phone — you tap a piece, see valid moves, tap destination, and the game validates everything.

### Requirements

1. Standard 8x8 board with proper piece placement
2. Two players take alternating turns (White goes first)
3. Each piece type has its own movement rules
4. Validate that a move is legal before executing
5. Detect check, checkmate, and stalemate
6. Track killed (captured) pieces
7. Game ends on checkmate, stalemate, or resignation

### Design Patterns Used

| Pattern | Where Used |
|---------|-----------|
| **Polymorphism** | Abstract `Piece` class with `canMove()` overridden by each piece type |
| **Encapsulation** | Board manages its own grid, validates moves internally |
| **Enum** | PieceColor, GameStatus |

### Entities

- **Piece** (abstract) — color, killed flag, abstract `canMove(board, start, end)`
- **King, Queen, Rook, Bishop, Knight, Pawn** — each extends Piece with own movement logic
- **Cell** — row, col, piece reference
- **Board** — 8x8 grid of Cells, resetBoard(), movePiece()
- **Player** — name, color, isWhiteSide
- **Move** — start cell, end cell, piece moved, piece killed
- **Game** — players, board, status, current turn, move list

### Full Java Code

```java
// ===================== ENUMS =====================

enum PieceColor {
    WHITE, BLACK
}

enum GameStatus {
    ACTIVE,       // Game is ongoing
    CHECK,        // A king is in check
    CHECKMATE,    // Game over — king cannot escape check
    STALEMATE,    // Game over — no legal moves but not in check
    RESIGNED      // A player gave up
}

// ===================== CELL =====================

class Cell {
    private int row;
    private int col;
    private Piece piece; // null if empty

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.piece = null;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public Piece getPiece() { return piece; }
    public void setPiece(Piece piece) { this.piece = piece; }
    public boolean isEmpty() { return piece == null; }
}

// ===================== ABSTRACT PIECE =====================

// POLYMORPHISM: Each piece type overrides canMove() with its own movement logic.
// This lets us call piece.canMove() without knowing the concrete type.
abstract class Piece {
    private PieceColor color;
    private boolean killed;

    public Piece(PieceColor color) {
        this.color = color;
        this.killed = false;
    }

    public PieceColor getColor() { return color; }
    public boolean isKilled() { return killed; }
    public void setKilled(boolean killed) { this.killed = killed; }

    // Each subclass defines its own movement rules
    public abstract boolean canMove(Board board, Cell start, Cell end);

    // Helper: check if path between two cells is clear (used by Rook, Bishop, Queen)
    protected boolean isPathClear(Board board, Cell start, Cell end) {
        int rowDir = Integer.signum(end.getRow() - start.getRow());
        int colDir = Integer.signum(end.getCol() - start.getCol());

        int currentRow = start.getRow() + rowDir;
        int currentCol = start.getCol() + colDir;

        // Walk along the path — every cell in between must be empty
        while (currentRow != end.getRow() || currentCol != end.getCol()) {
            if (!board.getCell(currentRow, currentCol).isEmpty()) {
                return false; // Something is blocking
            }
            currentRow += rowDir;
            currentCol += colDir;
        }
        return true;
    }
}

// ===================== CONCRETE PIECES =====================

class King extends Piece {
    public King(PieceColor color) { super(color); }

    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        // King moves exactly 1 square in any direction
        int rowDiff = Math.abs(start.getRow() - end.getRow());
        int colDiff = Math.abs(start.getCol() - end.getCol());

        if (rowDiff > 1 || colDiff > 1) return false;
        if (rowDiff == 0 && colDiff == 0) return false;

        // Cannot capture own piece
        if (!end.isEmpty() && end.getPiece().getColor() == this.getColor()) {
            return false;
        }
        return true;
    }
}

class Queen extends Piece {
    public Queen(PieceColor color) { super(color); }

    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        // Queen = Rook + Bishop (straight lines + diagonals)
        int rowDiff = Math.abs(start.getRow() - end.getRow());
        int colDiff = Math.abs(start.getCol() - end.getCol());

        boolean straightLine = (start.getRow() == end.getRow() || start.getCol() == end.getCol());
        boolean diagonal = (rowDiff == colDiff);

        if (!straightLine && !diagonal) return false;
        if (!isPathClear(board, start, end)) return false;
        if (!end.isEmpty() && end.getPiece().getColor() == this.getColor()) return false;

        return true;
    }
}

class Rook extends Piece {
    public Rook(PieceColor color) { super(color); }

    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        // Rook moves in straight lines — same row OR same column
        if (start.getRow() != end.getRow() && start.getCol() != end.getCol()) {
            return false;
        }
        if (!isPathClear(board, start, end)) return false;
        if (!end.isEmpty() && end.getPiece().getColor() == this.getColor()) return false;

        return true;
    }
}

class Bishop extends Piece {
    public Bishop(PieceColor color) { super(color); }

    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        // Bishop moves diagonally — row distance must equal column distance
        int rowDiff = Math.abs(start.getRow() - end.getRow());
        int colDiff = Math.abs(start.getCol() - end.getCol());

        if (rowDiff != colDiff || rowDiff == 0) return false;
        if (!isPathClear(board, start, end)) return false;
        if (!end.isEmpty() && end.getPiece().getColor() == this.getColor()) return false;

        return true;
    }
}

class Knight extends Piece {
    public Knight(PieceColor color) { super(color); }

    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        // Knight moves in L-shape: 2+1 or 1+2
        // Knight is the ONLY piece that can jump over others (no path check needed)
        int rowDiff = Math.abs(start.getRow() - end.getRow());
        int colDiff = Math.abs(start.getCol() - end.getCol());

        if (!((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2))) {
            return false;
        }
        if (!end.isEmpty() && end.getPiece().getColor() == this.getColor()) return false;

        return true;
    }
}

class Pawn extends Piece {
    public Pawn(PieceColor color) { super(color); }

    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        int rowDiff = end.getRow() - start.getRow();
        int colDiff = Math.abs(start.getCol() - end.getCol());

        // White pawns move UP (row decreases), Black pawns move DOWN (row increases)
        // Row 0 is top of board (Black's back rank), Row 7 is bottom (White's back rank)
        int direction = (this.getColor() == PieceColor.WHITE) ? -1 : 1;

        // Normal move: 1 step forward, column unchanged, destination must be empty
        if (colDiff == 0 && rowDiff == direction && end.isEmpty()) {
            return true;
        }

        // First move: can go 2 steps forward if both cells are empty
        boolean isFirstMove = (this.getColor() == PieceColor.WHITE && start.getRow() == 6)
                           || (this.getColor() == PieceColor.BLACK && start.getRow() == 1);
        if (colDiff == 0 && rowDiff == 2 * direction && isFirstMove) {
            // Check that both the intermediate and destination cells are empty
            Cell intermediate = board.getCell(start.getRow() + direction, start.getCol());
            if (intermediate.isEmpty() && end.isEmpty()) {
                return true;
            }
        }

        // Diagonal capture: 1 step forward + 1 step sideways, destination must have opponent piece
        if (colDiff == 1 && rowDiff == direction && !end.isEmpty()
                && end.getPiece().getColor() != this.getColor()) {
            return true;
        }

        return false;
    }
}

// ===================== BOARD =====================

class Board {
    private Cell[][] grid;

    public Board() {
        grid = new Cell[8][8];
        // Create all 64 cells
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                grid[i][j] = new Cell(i, j);
            }
        }
        resetBoard();
    }

    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    // Place all 32 pieces in their starting positions
    public void resetBoard() {
        // Black pieces on rows 0-1, White pieces on rows 6-7
        // Row 0: Black back rank
        grid[0][0].setPiece(new Rook(PieceColor.BLACK));
        grid[0][1].setPiece(new Knight(PieceColor.BLACK));
        grid[0][2].setPiece(new Bishop(PieceColor.BLACK));
        grid[0][3].setPiece(new Queen(PieceColor.BLACK));
        grid[0][4].setPiece(new King(PieceColor.BLACK));
        grid[0][5].setPiece(new Bishop(PieceColor.BLACK));
        grid[0][6].setPiece(new Knight(PieceColor.BLACK));
        grid[0][7].setPiece(new Rook(PieceColor.BLACK));
        // Row 1: Black pawns
        for (int j = 0; j < 8; j++) {
            grid[1][j].setPiece(new Pawn(PieceColor.BLACK));
        }

        // Row 7: White back rank
        grid[7][0].setPiece(new Rook(PieceColor.WHITE));
        grid[7][1].setPiece(new Knight(PieceColor.WHITE));
        grid[7][2].setPiece(new Bishop(PieceColor.WHITE));
        grid[7][3].setPiece(new Queen(PieceColor.WHITE));
        grid[7][4].setPiece(new King(PieceColor.WHITE));
        grid[7][5].setPiece(new Bishop(PieceColor.WHITE));
        grid[7][6].setPiece(new Knight(PieceColor.WHITE));
        grid[7][7].setPiece(new Rook(PieceColor.WHITE));
        // Row 6: White pawns
        for (int j = 0; j < 8; j++) {
            grid[6][j].setPiece(new Pawn(PieceColor.WHITE));
        }

        // Rows 2-5 are empty (already null from Cell constructor)
    }

    // Find the cell containing the king of a given color
    public Cell findKing(PieceColor color) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = grid[i][j].getPiece();
                if (p != null && p instanceof King && p.getColor() == color) {
                    return grid[i][j];
                }
            }
        }
        return null; // Should never happen in a valid game
    }

    // Check if the king of given color is currently in check
    public boolean isKingInCheck(PieceColor kingColor) {
        Cell kingCell = findKing(kingColor);
        PieceColor opponentColor = (kingColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        // See if any opponent piece can move to king's position
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = grid[i][j].getPiece();
                if (p != null && p.getColor() == opponentColor) {
                    if (p.canMove(this, grid[i][j], kingCell)) {
                        return true; // King is under attack
                    }
                }
            }
        }
        return false;
    }

    // Check if the player of given color has any legal move
    public boolean hasAnyLegalMove(PieceColor color) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = grid[i][j].getPiece();
                if (p != null && p.getColor() == color) {
                    // Try every destination cell
                    for (int r = 0; r < 8; r++) {
                        for (int c = 0; c < 8; c++) {
                            if (p.canMove(this, grid[i][j], grid[r][c])) {
                                // Simulate the move and check if king is still safe
                                if (isMoveSafe(grid[i][j], grid[r][c], color)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // Simulate a move and check if own king is NOT in check after it
    public boolean isMoveSafe(Cell start, Cell end, PieceColor color) {
        Piece movingPiece = start.getPiece();
        Piece capturedPiece = end.getPiece();

        // Temporarily make the move
        end.setPiece(movingPiece);
        start.setPiece(null);

        boolean kingInCheck = isKingInCheck(color);

        // Undo the move
        start.setPiece(movingPiece);
        end.setPiece(capturedPiece);

        return !kingInCheck; // Safe if king is NOT in check
    }
}

// ===================== PLAYER =====================

class Player {
    private String name;
    private PieceColor color;

    public Player(String name, PieceColor color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public PieceColor getColor() { return color; }
}

// ===================== MOVE =====================

class Move {
    private Cell start;
    private Cell end;
    private Piece pieceMoved;
    private Piece pieceKilled; // null if no capture

    public Move(Cell start, Cell end, Piece pieceMoved, Piece pieceKilled) {
        this.start = start;
        this.end = end;
        this.pieceMoved = pieceMoved;
        this.pieceKilled = pieceKilled;
    }

    public Cell getStart() { return start; }
    public Cell getEnd() { return end; }
    public Piece getPieceMoved() { return pieceMoved; }
    public Piece getPieceKilled() { return pieceKilled; }
}

// ===================== GAME =====================

class ChessGame {
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Player currentTurn;
    private GameStatus status;
    private List<Move> moveHistory;

    public ChessGame(String whiteName, String blackName) {
        this.board = new Board();
        this.whitePlayer = new Player(whiteName, PieceColor.WHITE);
        this.blackPlayer = new Player(blackName, PieceColor.BLACK);
        this.currentTurn = whitePlayer; // White always goes first
        this.status = GameStatus.ACTIVE;
        this.moveHistory = new ArrayList<>();
    }

    public GameStatus getStatus() { return status; }

    // Main method to make a move — returns true if move was successful
    public boolean makeMove(int startRow, int startCol, int endRow, int endCol) {
        if (status == GameStatus.CHECKMATE || status == GameStatus.STALEMATE
                || status == GameStatus.RESIGNED) {
            System.out.println("Game is already over!");
            return false;
        }

        Cell start = board.getCell(startRow, startCol);
        Cell end = board.getCell(endRow, endCol);

        // Must pick a cell that has a piece
        if (start.isEmpty()) {
            System.out.println("No piece at the start position.");
            return false;
        }

        Piece piece = start.getPiece();

        // Must move your own piece
        if (piece.getColor() != currentTurn.getColor()) {
            System.out.println("Not your piece! It's " + currentTurn.getName() + "'s turn.");
            return false;
        }

        // Piece must be able to make this move by its own rules
        if (!piece.canMove(board, start, end)) {
            System.out.println("Illegal move for this piece.");
            return false;
        }

        // Move must not leave your own king in check
        if (!board.isMoveSafe(start, end, currentTurn.getColor())) {
            System.out.println("Move puts your king in check!");
            return false;
        }

        // Execute the move
        Piece captured = end.getPiece();
        if (captured != null) {
            captured.setKilled(true);
            System.out.println(currentTurn.getName() + " captured a piece!");
        }

        end.setPiece(piece);
        start.setPiece(null);
        moveHistory.add(new Move(start, end, piece, captured));

        // After move, evaluate the opponent's state
        PieceColor opponentColor = (currentTurn.getColor() == PieceColor.WHITE)
                ? PieceColor.BLACK : PieceColor.WHITE;

        boolean opponentInCheck = board.isKingInCheck(opponentColor);
        boolean opponentHasMoves = board.hasAnyLegalMove(opponentColor);

        if (opponentInCheck && !opponentHasMoves) {
            status = GameStatus.CHECKMATE;
            System.out.println("CHECKMATE! " + currentTurn.getName() + " wins!");
        } else if (!opponentInCheck && !opponentHasMoves) {
            status = GameStatus.STALEMATE;
            System.out.println("STALEMATE! It's a draw.");
        } else if (opponentInCheck) {
            status = GameStatus.CHECK;
            System.out.println("CHECK!");
        } else {
            status = GameStatus.ACTIVE;
        }

        // Switch turns
        currentTurn = (currentTurn == whitePlayer) ? blackPlayer : whitePlayer;
        return true;
    }

    public void resign() {
        status = GameStatus.RESIGNED;
        Player winner = (currentTurn == whitePlayer) ? blackPlayer : whitePlayer;
        System.out.println(currentTurn.getName() + " resigned. " + winner.getName() + " wins!");
    }
}

// ===================== MAIN =====================

public class ChessMain {
    public static void main(String[] args) {
        ChessGame game = new ChessGame("Vishy Anand", "Magnus Carlsen");

        // White pawn e2 to e4 (row 6, col 4 -> row 4, col 4)
        game.makeMove(6, 4, 4, 4);
        // Black pawn e7 to e5 (row 1, col 4 -> row 3, col 4)
        game.makeMove(1, 4, 3, 4);

        System.out.println("Game status: " + game.getStatus());
    }
}
```

### Key Points for Interview

- **Polymorphism** is the star — `canMove()` in each piece makes adding new piece types trivial.
- `isMoveSafe()` does a simulate-and-undo approach to check if a move exposes the king.
- The Board is the authority — pieces only say "can I theoretically reach this cell?", the Board confirms it is safe.
- `hasAnyLegalMove()` is O(n^4) in worst case but that is fine for an 8x8 board (at most 64x64 = 4096 checks).

---

## Problem 2: Cab Booking System (Ola / Uber)

### System Description

Design a cab booking system like Ola or Uber. A rider opens the app in Mumbai, enters pickup and drop location, the system finds the nearest available driver, assigns the ride, and tracks it through its lifecycle. Fare is calculated based on distance, vehicle type, and surge pricing.

### Requirements

1. Rider can request a ride with pickup/drop locations and vehicle type
2. System finds the nearest available driver with matching vehicle type
3. Driver can accept or is auto-assigned
4. Ride goes through states: REQUESTED -> ASSIGNED -> ARRIVED -> IN_PROGRESS -> COMPLETED / CANCELLED
5. Fare calculated using base fare + per km charge + surge multiplier
6. Notifications sent to rider and driver at each state change
7. Driver status: AVAILABLE, ON_RIDE, OFFLINE

### Design Patterns Used

| Pattern | Where Used |
|---------|-----------|
| **Strategy** | FareCalculator — different fare strategies for different vehicle types and surge |
| **Observer** | NotificationService — rider and driver notified on ride state changes |
| **State** | Ride lifecycle management |
| **Singleton** | RideManager (single instance managing all rides) |

### Entities

- **Location** — latitude, longitude
- **Rider** — id, name, phone, location
- **Vehicle** — type (AUTO/MINI/SEDAN/SUV), registration number
- **Driver** — id, name, phone, location, vehicle, status (AVAILABLE/ON_RIDE/OFFLINE)
- **Ride** — id, rider, driver, pickup, drop, status, fare
- **FareStrategy** (interface) — calculateFare(distance, vehicleType)
- **NotificationObserver** (interface) — notify(message)
- **RideManager** — manages ride lifecycle

### Full Java Code

```java
import java.util.*;

// ===================== ENUMS =====================

enum VehicleType {
    AUTO,   // Cheapest, like auto-rickshaw
    MINI,   // Hatchback (WagonR, i10)
    SEDAN,  // Sedan (Dzire, Etios)
    SUV     // Innova, Ertiga
}

enum DriverStatus {
    AVAILABLE,  // Ready to accept rides
    ON_RIDE,    // Currently on a ride
    OFFLINE     // Not working
}

enum RideStatus {
    REQUESTED,    // Rider has requested
    ASSIGNED,     // Driver assigned
    ARRIVED,      // Driver reached pickup point
    IN_PROGRESS,  // Ride ongoing
    COMPLETED,    // Ride finished
    CANCELLED     // Ride cancelled by rider or driver
}

// ===================== LOCATION =====================

class Location {
    private double latitude;
    private double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Simple distance calculation (Euclidean for simplicity; real systems use Haversine)
    public double distanceTo(Location other) {
        double dLat = this.latitude - other.latitude;
        double dLon = this.longitude - other.longitude;
        // Rough conversion: 1 degree ~ 111 km
        return Math.sqrt(dLat * dLat + dLon * dLon) * 111;
    }
}

// ===================== RIDER =====================

class Rider {
    private String id;
    private String name;
    private String phone;
    private Location currentLocation;

    public Rider(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Location getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(Location loc) { this.currentLocation = loc; }
}

// ===================== VEHICLE =====================

class Vehicle {
    private VehicleType type;
    private String registrationNumber;

    public Vehicle(VehicleType type, String registrationNumber) {
        this.type = type;
        this.registrationNumber = registrationNumber;
    }

    public VehicleType getType() { return type; }
    public String getRegistrationNumber() { return registrationNumber; }
}

// ===================== DRIVER =====================

class Driver {
    private String id;
    private String name;
    private String phone;
    private Location currentLocation;
    private Vehicle vehicle;
    private DriverStatus status;
    private double rating;

    public Driver(String id, String name, String phone, Vehicle vehicle) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.vehicle = vehicle;
        this.status = DriverStatus.AVAILABLE;
        this.rating = 5.0; // Start with perfect rating
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Vehicle getVehicle() { return vehicle; }
    public DriverStatus getStatus() { return status; }
    public void setStatus(DriverStatus status) { this.status = status; }
    public Location getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(Location loc) { this.currentLocation = loc; }
    public double getRating() { return rating; }
}

// ===================== STRATEGY PATTERN — FARE CALCULATION =====================

// Strategy interface — different algorithms for fare calculation
interface FareStrategy {
    double calculateFare(double distanceKm, VehicleType vehicleType);
}

// Normal pricing: base fare + per km charge based on vehicle type
class NormalFareStrategy implements FareStrategy {
    @Override
    public double calculateFare(double distanceKm, VehicleType vehicleType) {
        double baseFare;
        double perKmRate;

        switch (vehicleType) {
            case AUTO:
                baseFare = 25;    // Rs 25 base (like Mumbai auto meter)
                perKmRate = 12;   // Rs 12 per km
                break;
            case MINI:
                baseFare = 40;    // Rs 40 base
                perKmRate = 10;   // Rs 10 per km
                break;
            case SEDAN:
                baseFare = 60;    // Rs 60 base
                perKmRate = 14;   // Rs 14 per km
                break;
            case SUV:
                baseFare = 80;    // Rs 80 base
                perKmRate = 18;   // Rs 18 per km
                break;
            default:
                baseFare = 50;
                perKmRate = 12;
        }

        return baseFare + (perKmRate * distanceKm);
    }
}

// Surge pricing: normal fare multiplied by surge factor
// Used during peak hours (Monday 9am, Friday evening, New Year's Eve, etc.)
class SurgeFareStrategy implements FareStrategy {
    private double surgeMultiplier; // e.g., 1.5x, 2.0x during heavy rain in Mumbai

    public SurgeFareStrategy(double surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }

    @Override
    public double calculateFare(double distanceKm, VehicleType vehicleType) {
        // First calculate normal fare, then apply surge
        FareStrategy normalStrategy = new NormalFareStrategy();
        double normalFare = normalStrategy.calculateFare(distanceKm, vehicleType);
        return normalFare * surgeMultiplier;
    }
}

// ===================== OBSERVER PATTERN — NOTIFICATIONS =====================

// Observer interface
interface NotificationObserver {
    void notify(String message);
}

// Concrete observer for rider
class RiderNotification implements NotificationObserver {
    private Rider rider;

    public RiderNotification(Rider rider) {
        this.rider = rider;
    }

    @Override
    public void notify(String message) {
        System.out.println("[SMS to Rider " + rider.getName() + "]: " + message);
    }
}

// Concrete observer for driver
class DriverNotification implements NotificationObserver {
    private Driver driver;

    public DriverNotification(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void notify(String message) {
        System.out.println("[Push to Driver " + driver.getName() + "]: " + message);
    }
}

// Subject that manages observers
class NotificationService {
    private List<NotificationObserver> observers = new ArrayList<>();

    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyAll(String message) {
        for (NotificationObserver observer : observers) {
            observer.notify(message);
        }
    }
}

// ===================== RIDE =====================

class Ride {
    private String id;
    private Rider rider;
    private Driver driver;
    private Location pickup;
    private Location drop;
    private RideStatus status;
    private double fare;
    private NotificationService notificationService;

    public Ride(String id, Rider rider, Location pickup, Location drop) {
        this.id = id;
        this.rider = rider;
        this.pickup = pickup;
        this.drop = drop;
        this.status = RideStatus.REQUESTED;
        this.notificationService = new NotificationService();

        // Rider always gets notifications for their ride
        notificationService.addObserver(new RiderNotification(rider));
    }

    public String getId() { return id; }
    public Rider getRider() { return rider; }
    public Driver getDriver() { return driver; }
    public RideStatus getStatus() { return status; }
    public double getFare() { return fare; }
    public Location getPickup() { return pickup; }
    public Location getDrop() { return drop; }

    // Assign a driver to this ride
    public void assignDriver(Driver driver) {
        this.driver = driver;
        this.status = RideStatus.ASSIGNED;
        driver.setStatus(DriverStatus.ON_RIDE);

        // Add driver to notification list
        notificationService.addObserver(new DriverNotification(driver));
        notificationService.notifyAll("Ride assigned! Driver " + driver.getName()
                + " (" + driver.getVehicle().getRegistrationNumber() + ") is on the way.");
    }

    public void driverArrived() {
        this.status = RideStatus.ARRIVED;
        notificationService.notifyAll("Driver has arrived at pickup point.");
    }

    public void startRide() {
        this.status = RideStatus.IN_PROGRESS;
        notificationService.notifyAll("Ride started. Enjoy your trip!");
    }

    public void completeRide(FareStrategy fareStrategy, VehicleType vehicleType) {
        this.status = RideStatus.COMPLETED;
        double distance = pickup.distanceTo(drop);
        this.fare = fareStrategy.calculateFare(distance, vehicleType);
        driver.setStatus(DriverStatus.AVAILABLE);
        notificationService.notifyAll("Ride completed! Fare: Rs " + String.format("%.2f", fare));
    }

    public void cancelRide() {
        this.status = RideStatus.CANCELLED;
        if (driver != null) {
            driver.setStatus(DriverStatus.AVAILABLE);
        }
        notificationService.notifyAll("Ride cancelled.");
    }
}

// ===================== RIDE MANAGER (Singleton) =====================

class RideManager {
    private static RideManager instance;
    private List<Driver> drivers;
    private List<Ride> rides;
    private int rideCounter;

    private RideManager() {
        this.drivers = new ArrayList<>();
        this.rides = new ArrayList<>();
        this.rideCounter = 0;
    }

    public static RideManager getInstance() {
        if (instance == null) {
            instance = new RideManager();
        }
        return instance;
    }

    public void registerDriver(Driver driver) {
        drivers.add(driver);
    }

    // Find the nearest available driver with the requested vehicle type
    public Driver findNearestDriver(Location pickup, VehicleType vehicleType) {
        Driver nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Driver driver : drivers) {
            if (driver.getStatus() == DriverStatus.AVAILABLE
                    && driver.getVehicle().getType() == vehicleType
                    && driver.getCurrentLocation() != null) {
                double dist = driver.getCurrentLocation().distanceTo(pickup);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = driver;
                }
            }
        }
        return nearest;
    }

    // Rider requests a ride
    public Ride requestRide(Rider rider, Location pickup, Location drop, VehicleType vehicleType) {
        rideCounter++;
        Ride ride = new Ride("RIDE-" + rideCounter, rider, pickup, drop);
        rides.add(ride);

        System.out.println(rider.getName() + " requested a " + vehicleType + " ride.");

        // Find nearest driver
        Driver driver = findNearestDriver(pickup, vehicleType);
        if (driver != null) {
            ride.assignDriver(driver);
        } else {
            System.out.println("No drivers available nearby. Please wait...");
        }

        return ride;
    }
}

// ===================== MAIN =====================

public class CabBookingMain {
    public static void main(String[] args) {
        RideManager manager = RideManager.getInstance();

        // Create drivers in Mumbai
        Driver raju = new Driver("D1", "Raju Sharma", "9876543210",
                new Vehicle(VehicleType.SEDAN, "MH-02-AB-1234"));
        raju.setCurrentLocation(new Location(19.0760, 72.8777)); // Near CST station

        Driver amit = new Driver("D2", "Amit Patil", "9876543211",
                new Vehicle(VehicleType.AUTO, "MH-01-CD-5678"));
        amit.setCurrentLocation(new Location(19.0800, 72.8800)); // Near Churchgate

        manager.registerDriver(raju);
        manager.registerDriver(amit);

        // Rider requests a Sedan ride
        Rider priya = new Rider("R1", "Priya Desai", "9988776655");
        Location pickup = new Location(19.0750, 72.8780); // Colaba
        Location drop = new Location(19.1136, 72.8697);   // Bandra

        Ride ride = manager.requestRide(priya, pickup, drop, VehicleType.SEDAN);

        // Simulate ride lifecycle
        ride.driverArrived();
        ride.startRide();

        // Complete with surge pricing (Mumbai rain season = 1.5x)
        FareStrategy surgeStrategy = new SurgeFareStrategy(1.5);
        ride.completeRide(surgeStrategy, VehicleType.SEDAN);
    }
}
```

### Key Points for Interview

- **Strategy Pattern** shines for fare calculation — easily swap between normal and surge pricing without changing Ride class.
- **Observer Pattern** keeps notifications decoupled — add email, push, SMS observers without touching Ride logic.
- **Singleton** for RideManager ensures one central coordinator.
- `findNearestDriver()` uses simple Euclidean distance — mention that real systems use road-network distance (Google Maps API).
- The state transitions in Ride are linear and well-guarded — each method only makes sense when called in order.

---

## Problem 3: Shopping Cart (Amazon / Flipkart)

### System Description

Design the shopping cart system for an e-commerce platform like Amazon or Flipkart. Users browse products organized in categories (Electronics > Mobiles > Samsung), add items to cart, apply discount coupons, and checkout. Orders go through states from placement to delivery.

### Requirements

1. Products organized in categories (tree structure — parent/child categories)
2. Users can browse products and add to cart
3. Cart tracks items with quantities
4. Coupons: percentage-based (10% off) and flat-amount (Rs 200 off)
5. Order created at checkout with status tracking
6. Order states: PLACED -> CONFIRMED -> SHIPPED -> DELIVERED / CANCELLED
7. Payment integration (simplified)

### Design Patterns Used

| Pattern | Where Used |
|---------|-----------|
| **Strategy** | Discount coupons — different coupon types use different discount algorithms |
| **Builder** | Order creation — complex object built step by step |
| **Composite** | Category tree — categories can contain sub-categories |

### Entities

- **Category** — id, name, parent category (tree structure)
- **Product** — id, name, description, price, category, stock
- **User** — id, name, email, address, cart
- **CartItem** — product, quantity
- **Cart** — list of cart items, add/remove/total
- **Coupon** (interface) — applyCoupon(totalAmount)
- **Order** — id, items, total, status, shipping address, payment
- **Payment** — id, amount, method, status

### Full Java Code

```java
import java.util.*;
import java.time.LocalDateTime;

// ===================== ENUMS =====================

enum OrderStatus {
    PLACED,     // Order just created
    CONFIRMED,  // Payment verified
    SHIPPED,    // Out for delivery
    DELIVERED,  // Customer received it
    CANCELLED   // Cancelled before delivery
}

enum PaymentMethod {
    UPI,          // Google Pay, PhonePe, Paytm
    CREDIT_CARD,
    DEBIT_CARD,
    NET_BANKING,
    COD           // Cash on Delivery — still very popular in India
}

enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}

// ===================== CATEGORY (Composite-like tree) =====================

// Categories form a tree: Electronics -> Mobiles -> Samsung
class Category {
    private String id;
    private String name;
    private Category parent; // null for root categories
    private List<Category> subCategories;

    public Category(String id, String name, Category parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.subCategories = new ArrayList<>();
        if (parent != null) {
            parent.addSubCategory(this);
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Category getParent() { return parent; }
    public List<Category> getSubCategories() { return subCategories; }

    public void addSubCategory(Category sub) {
        subCategories.add(sub);
    }

    // Get full category path like "Electronics > Mobiles > Samsung"
    public String getFullPath() {
        if (parent == null) return name;
        return parent.getFullPath() + " > " + name;
    }
}

// ===================== PRODUCT =====================

class Product {
    private String id;
    private String name;
    private String description;
    private double price;    // In rupees
    private Category category;
    private int stockQuantity;

    public Product(String id, String name, String description,
                   double price, Category category, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stockQuantity = stock;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public Category getCategory() { return category; }
    public int getStockQuantity() { return stockQuantity; }

    public boolean isInStock() { return stockQuantity > 0; }

    public void reduceStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new RuntimeException("Not enough stock for " + name);
        }
        this.stockQuantity -= quantity;
    }
}

// ===================== CART ITEM =====================

class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Total price for this item = unit price * quantity
    public double getSubtotal() {
        return product.getPrice() * quantity;
    }
}

// ===================== CART =====================

class Cart {
    private List<CartItem> items;

    public Cart() {
        this.items = new ArrayList<>();
    }

    public List<CartItem> getItems() { return items; }

    public void addItem(Product product, int quantity) {
        // If product already in cart, increase quantity
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                System.out.println("Updated " + product.getName() + " quantity to " + item.getQuantity());
                return;
            }
        }
        // New product — add fresh item
        items.add(new CartItem(product, quantity));
        System.out.println("Added " + product.getName() + " x" + quantity + " to cart.");
    }

    public void removeItem(String productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public boolean isEmpty() { return items.isEmpty(); }

    public void clear() { items.clear(); }
}

// ===================== STRATEGY PATTERN — COUPONS =====================

// Strategy interface for different coupon types
interface CouponStrategy {
    double applyDiscount(double totalAmount);
    String getDescription();
}

// Percentage coupon — "DIWALI10" gives 10% off
class PercentageCoupon implements CouponStrategy {
    private String code;
    private double percentage;    // e.g., 10 for 10%
    private double maxDiscount;   // Cap the discount (Flipkart style — "up to Rs 500")

    public PercentageCoupon(String code, double percentage, double maxDiscount) {
        this.code = code;
        this.percentage = percentage;
        this.maxDiscount = maxDiscount;
    }

    @Override
    public double applyDiscount(double totalAmount) {
        double discount = totalAmount * (percentage / 100);
        // Cap at max discount — "10% off up to Rs 500"
        return Math.min(discount, maxDiscount);
    }

    @Override
    public String getDescription() {
        return code + ": " + percentage + "% off (max Rs " + maxDiscount + ")";
    }
}

// Flat coupon — "FLAT200" gives Rs 200 off
class FlatCoupon implements CouponStrategy {
    private String code;
    private double flatAmount;     // Rs 200 off
    private double minOrderValue;  // Minimum order to apply (e.g., Rs 999)

    public FlatCoupon(String code, double flatAmount, double minOrderValue) {
        this.code = code;
        this.flatAmount = flatAmount;
        this.minOrderValue = minOrderValue;
    }

    @Override
    public double applyDiscount(double totalAmount) {
        if (totalAmount < minOrderValue) {
            System.out.println("Minimum order Rs " + minOrderValue + " required for this coupon.");
            return 0;
        }
        // Discount cannot exceed total
        return Math.min(flatAmount, totalAmount);
    }

    @Override
    public String getDescription() {
        return code + ": Rs " + flatAmount + " off (min order Rs " + minOrderValue + ")";
    }
}

// ===================== PAYMENT =====================

class Payment {
    private String id;
    private double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime timestamp;

    public Payment(String id, double amount, PaymentMethod method) {
        this.id = id;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }

    public void markCompleted() { this.status = PaymentStatus.COMPLETED; }
    public void markFailed() { this.status = PaymentStatus.FAILED; }
    public void markRefunded() { this.status = PaymentStatus.REFUNDED; }
}

// ===================== ORDER (Built using Builder pattern) =====================

class Order {
    private String id;
    private List<CartItem> items;
    private double subtotal;
    private double discount;
    private double total;
    private String shippingAddress;
    private OrderStatus status;
    private Payment payment;
    private LocalDateTime orderDate;

    // Private constructor — use Builder
    private Order(OrderBuilder builder) {
        this.id = builder.id;
        this.items = builder.items;
        this.subtotal = builder.subtotal;
        this.discount = builder.discount;
        this.total = builder.total;
        this.shippingAddress = builder.shippingAddress;
        this.status = OrderStatus.PLACED;
        this.payment = builder.payment;
        this.orderDate = LocalDateTime.now();
    }

    public String getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public double getTotal() { return total; }

    public void confirmOrder() {
        if (payment != null && payment.getStatus() == PaymentStatus.COMPLETED) {
            this.status = OrderStatus.CONFIRMED;
            System.out.println("Order " + id + " confirmed!");
        } else {
            System.out.println("Payment not completed. Cannot confirm order.");
        }
    }

    public void shipOrder() {
        this.status = OrderStatus.SHIPPED;
        System.out.println("Order " + id + " shipped! Track on Delhivery/BlueDart.");
    }

    public void deliverOrder() {
        this.status = OrderStatus.DELIVERED;
        System.out.println("Order " + id + " delivered! Rate your experience.");
    }

    public void cancelOrder() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            System.out.println("Cannot cancel — order already " + status);
            return;
        }
        this.status = OrderStatus.CANCELLED;
        if (payment != null) {
            payment.markRefunded();
        }
        System.out.println("Order " + id + " cancelled. Refund initiated.");
    }

    public void printOrderSummary() {
        System.out.println("===== Order " + id + " =====");
        for (CartItem item : items) {
            System.out.println("  " + item.getProduct().getName()
                    + " x" + item.getQuantity()
                    + " = Rs " + String.format("%.2f", item.getSubtotal()));
        }
        System.out.println("  Subtotal: Rs " + String.format("%.2f", subtotal));
        System.out.println("  Discount: -Rs " + String.format("%.2f", discount));
        System.out.println("  TOTAL:    Rs " + String.format("%.2f", total));
        System.out.println("  Status:   " + status);
        System.out.println("  Ship to:  " + shippingAddress);
    }

    // ===================== BUILDER =====================

    static class OrderBuilder {
        private String id;
        private List<CartItem> items;
        private double subtotal;
        private double discount;
        private double total;
        private String shippingAddress;
        private Payment payment;

        public OrderBuilder(String id) {
            this.id = id;
            this.items = new ArrayList<>();
            this.discount = 0;
        }

        public OrderBuilder withItems(List<CartItem> items) {
            this.items = new ArrayList<>(items); // Defensive copy
            this.subtotal = 0;
            for (CartItem item : items) {
                this.subtotal += item.getSubtotal();
            }
            this.total = this.subtotal;
            return this;
        }

        public OrderBuilder withCoupon(CouponStrategy coupon) {
            this.discount = coupon.applyDiscount(subtotal);
            this.total = subtotal - discount;
            System.out.println("Coupon applied: " + coupon.getDescription()
                    + " -> Saved Rs " + String.format("%.2f", discount));
            return this;
        }

        public OrderBuilder withShippingAddress(String address) {
            this.shippingAddress = address;
            return this;
        }

        public OrderBuilder withPayment(Payment payment) {
            this.payment = payment;
            return this;
        }

        public Order build() {
            if (items.isEmpty()) throw new RuntimeException("Cannot create order with empty cart");
            if (shippingAddress == null) throw new RuntimeException("Shipping address required");
            return new Order(this);
        }
    }
}

// ===================== USER =====================

class User {
    private String id;
    private String name;
    private String email;
    private String address;
    private Cart cart;

    public User(String id, String name, String email, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.cart = new Cart();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Cart getCart() { return cart; }
}

// ===================== MAIN =====================

public class ShoppingCartMain {
    public static void main(String[] args) {
        // Create category tree: Electronics > Mobiles > Samsung
        Category electronics = new Category("C1", "Electronics", null);
        Category mobiles = new Category("C2", "Mobiles", electronics);
        Category samsung = new Category("C3", "Samsung", mobiles);
        Category clothing = new Category("C4", "Clothing", null);

        System.out.println("Category path: " + samsung.getFullPath());
        // Output: Electronics > Mobiles > Samsung

        // Create products
        Product galaxyS24 = new Product("P1", "Samsung Galaxy S24",
                "Flagship phone with AI features", 79999, samsung, 50);
        Product tshirt = new Product("P2", "Allen Solly Tshirt",
                "Cotton round neck", 799, clothing, 200);
        Product earbuds = new Product("P3", "boAt Airdopes 141",
                "TWS earbuds with 42hr battery", 1299, electronics, 500);

        // Create user and add items to cart
        User rahul = new User("U1", "Rahul Kumar", "rahul@gmail.com",
                "Flat 302, Lodha Palava, Dombivli East, Mumbai - 421204");

        rahul.getCart().addItem(galaxyS24, 1);
        rahul.getCart().addItem(tshirt, 2);
        rahul.getCart().addItem(earbuds, 1);

        System.out.println("Cart total: Rs " + rahul.getCart().getTotal());

        // Apply Diwali coupon — 10% off, max Rs 5000
        CouponStrategy diwaliCoupon = new PercentageCoupon("DIWALI10", 10, 5000);

        // Create payment via UPI
        Payment payment = new Payment("PAY-001", 0, PaymentMethod.UPI);

        // Build the order using Builder pattern
        Order order = new Order.OrderBuilder("ORD-1001")
                .withItems(rahul.getCart().getItems())
                .withCoupon(diwaliCoupon)
                .withShippingAddress(rahul.getAddress())
                .withPayment(payment)
                .build();

        // Simulate payment success
        payment.markCompleted();

        order.printOrderSummary();
        order.confirmOrder();
        order.shipOrder();
        order.deliverOrder();

        // Clear cart after successful order
        rahul.getCart().clear();
    }
}
```

### Key Points for Interview

- **Strategy Pattern** for coupons — adding a new coupon type (BuyOneGetOne, Cashback) means just adding a new class.
- **Builder Pattern** for Order — avoids a constructor with 10 parameters. Each `with()` method returns `this` for chaining.
- Category uses a simple tree structure (Composite idea) with `getFullPath()` for breadcrumbs.
- Stock is managed at the Product level — `reduceStock()` would be called during checkout in a real system.
- Indian context: UPI payment, Diwali coupons, COD option, Delhivery/BlueDart shipping.

---

## Problem 4: Cricket Scoring System (CricBuzz)

### System Description

Design a live cricket scoring system like CricBuzz or ESPNCricinfo. The system tracks a match ball-by-ball, maintains batting and bowling scorecards, and updates the scoreboard in real time. Supports different match formats — Test, ODI, T20.

Think of following an India vs Australia match on CricBuzz — every ball shows runs, wickets, extras, and the scorecard updates live.

### Requirements

1. Create and manage a match between two teams
2. Track ball-by-ball scoring in each over
3. Record runs (0-6), wickets, and extras (wide, no ball, bye, leg bye)
4. Maintain batting scorecard (runs, balls faced, 4s, 6s, strike rate)
5. Maintain bowling scorecard (overs, maidens, runs given, wickets, economy)
6. Different rules per format — T20 (20 overs), ODI (50 overs), Test (unlimited)
7. Live scoreboard updates via Observer
8. Detect innings completion (all out, overs done, declaration)

### Design Patterns Used

| Pattern | Where Used |
|---------|-----------|
| **Observer** | Scoreboard display updated on every ball |
| **Strategy** | Match rules differ by format (T20/ODI/Test) |
| **Enum** | WicketType, MatchFormat, ExtrasType |

### Entities

- **Player** — name, role (BATSMAN/BOWLER/ALL_ROUNDER/WICKET_KEEPER)
- **Team** — name, list of players
- **Ball** — runs scored, isWicket, extras, batsman, bowler
- **Over** — list of balls, over number
- **BattingScorecard** — per batsman stats
- **BowlingScorecard** — per bowler stats
- **Innings** — batting team, bowling team, overs, scorecard, total
- **Match** — two teams, innings list, format, result
- **ScoreboardObserver** — live display

### Full Java Code

```java
import java.util.*;

// ===================== ENUMS =====================

enum PlayerRole {
    BATSMAN, BOWLER, ALL_ROUNDER, WICKET_KEEPER
}

enum MatchFormat {
    TEST,   // No over limit (but 90 overs per day)
    ODI,    // 50 overs per side
    T20     // 20 overs per side
}

enum WicketType {
    BOWLED,     // Ball hits stumps directly
    CAUGHT,     // Fielder catches the ball
    LBW,        // Leg Before Wicket — ball hits pad, would've hit stumps
    RUN_OUT,    // Batsman short of crease during a run
    STUMPED,    // Keeper removes bails while batsman is out of crease
    HIT_WICKET, // Batsman's own body hits the stumps
    RETIRED     // Batsman retires (rare — like Ashwin in a Test)
}

enum ExtrasType {
    NONE,
    WIDE,       // Ball too wide for batsman to reach
    NO_BALL,    // Bowler oversteps the crease
    BYE,        // Ball goes past everyone, batsmen take a run
    LEG_BYE     // Ball hits batsman's body (not bat), run taken
}

// ===================== PLAYER =====================

class Player {
    private String name;
    private PlayerRole role;

    public Player(String name, PlayerRole role) {
        this.name = name;
        this.role = role;
    }

    public String getName() { return name; }
    public PlayerRole getRole() { return role; }
}

// ===================== TEAM =====================

class Team {
    private String name;
    private List<Player> players;

    public Team(String name) {
        this.name = name;
        this.players = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<Player> getPlayers() { return players; }

    public void addPlayer(Player player) {
        if (players.size() >= 11) {
            throw new RuntimeException("Team already has 11 players!");
        }
        players.add(player);
    }
}

// ===================== BALL =====================

class Ball {
    private int ballNumber;       // Ball number within the over (1-6)
    private Player batsman;
    private Player bowler;
    private int runsScored;       // Runs off the bat (0, 1, 2, 3, 4, 6)
    private boolean isWicket;
    private WicketType wicketType;
    private ExtrasType extrasType;
    private int extraRuns;        // Extra runs (wide=1, no ball=1, etc.)

    public Ball(int ballNumber, Player batsman, Player bowler) {
        this.ballNumber = ballNumber;
        this.batsman = batsman;
        this.bowler = bowler;
        this.extrasType = ExtrasType.NONE;
        this.extraRuns = 0;
    }

    // Setters for recording what happened
    public void setRuns(int runs) { this.runsScored = runs; }
    public void setWicket(WicketType type) {
        this.isWicket = true;
        this.wicketType = type;
    }
    public void setExtras(ExtrasType type, int runs) {
        this.extrasType = type;
        this.extraRuns = runs;
    }

    public int getRunsScored() { return runsScored; }
    public int getExtraRuns() { return extraRuns; }
    public int getTotalRuns() { return runsScored + extraRuns; }
    public boolean isWicket() { return isWicket; }
    public WicketType getWicketType() { return wicketType; }
    public ExtrasType getExtrasType() { return extrasType; }
    public Player getBatsman() { return batsman; }
    public Player getBowler() { return bowler; }

    // A wide or no-ball does NOT count as a legal delivery
    public boolean isLegalDelivery() {
        return extrasType != ExtrasType.WIDE && extrasType != ExtrasType.NO_BALL;
    }
}

// ===================== OVER =====================

class Over {
    private int overNumber;
    private Player bowler;
    private List<Ball> balls;

    public Over(int overNumber, Player bowler) {
        this.overNumber = overNumber;
        this.bowler = bowler;
        this.balls = new ArrayList<>();
    }

    public int getOverNumber() { return overNumber; }
    public Player getBowler() { return bowler; }
    public List<Ball> getBalls() { return balls; }

    public void addBall(Ball ball) {
        balls.add(ball);
    }

    // Count only legal deliveries (wides and no-balls don't count)
    public int getLegalDeliveries() {
        int count = 0;
        for (Ball ball : balls) {
            if (ball.isLegalDelivery()) count++;
        }
        return count;
    }

    // An over is complete when 6 legal deliveries have been bowled
    public boolean isComplete() {
        return getLegalDeliveries() >= 6;
    }

    // Total runs in this over
    public int getOverRuns() {
        int total = 0;
        for (Ball ball : balls) {
            total += ball.getTotalRuns();
        }
        return total;
    }

    // A maiden over = complete over with 0 runs scored off the bat
    public boolean isMaiden() {
        if (!isComplete()) return false;
        for (Ball ball : balls) {
            if (ball.getRunsScored() > 0) return false;
        }
        return true;
    }
}

// ===================== BATTING SCORECARD =====================

class BattingScorecard {
    private Player batsman;
    private int runs;
    private int ballsFaced;
    private int fours;      // Number of 4s hit
    private int sixes;      // Number of 6s hit
    private boolean isOut;
    private WicketType dismissalType;

    public BattingScorecard(Player batsman) {
        this.batsman = batsman;
        this.runs = 0;
        this.ballsFaced = 0;
        this.fours = 0;
        this.sixes = 0;
        this.isOut = false;
    }

    public Player getBatsman() { return batsman; }
    public int getRuns() { return runs; }
    public int getBallsFaced() { return ballsFaced; }
    public int getFours() { return fours; }
    public int getSixes() { return sixes; }
    public boolean isOut() { return isOut; }

    public void addRuns(int runs) {
        this.runs += runs;
        if (runs == 4) this.fours++;
        if (runs == 6) this.sixes++;
    }

    public void incrementBallsFaced() {
        this.ballsFaced++;
    }

    public void setOut(WicketType type) {
        this.isOut = true;
        this.dismissalType = type;
    }

    // Strike rate = (runs / balls faced) * 100
    public double getStrikeRate() {
        if (ballsFaced == 0) return 0;
        return (runs * 100.0) / ballsFaced;
    }

    @Override
    public String toString() {
        String status = isOut ? "out (" + dismissalType + ")" : "not out";
        return String.format("%-20s %4d (%d) [4s:%d, 6s:%d] SR:%.1f %s",
                batsman.getName(), runs, ballsFaced, fours, sixes,
                getStrikeRate(), status);
    }
}

// ===================== BOWLING SCORECARD =====================

class BowlingScorecard {
    private Player bowler;
    private int oversBowled;        // Complete overs
    private int ballsInCurrentOver; // Balls in incomplete over (0-5)
    private int maidens;
    private int runsConceded;
    private int wicketsTaken;

    public BowlingScorecard(Player bowler) {
        this.bowler = bowler;
    }

    public Player getBowler() { return bowler; }
    public int getWicketsTaken() { return wicketsTaken; }

    public void addLegalDelivery() {
        ballsInCurrentOver++;
        if (ballsInCurrentOver == 6) {
            oversBowled++;
            ballsInCurrentOver = 0;
        }
    }

    public void addRuns(int runs) { this.runsConceded += runs; }
    public void addWicket() { this.wicketsTaken++; }
    public void addMaiden() { this.maidens++; }

    // Overs displayed as "4.3" meaning 4 overs and 3 balls
    public String getOversString() {
        if (ballsInCurrentOver == 0) return String.valueOf(oversBowled);
        return oversBowled + "." + ballsInCurrentOver;
    }

    // Economy = runs conceded per over
    public double getEconomy() {
        double totalOvers = oversBowled + (ballsInCurrentOver / 6.0);
        if (totalOvers == 0) return 0;
        return runsConceded / totalOvers;
    }

    @Override
    public String toString() {
        return String.format("%-20s %5s  %2d  %4d  %2d  Econ:%.1f",
                bowler.getName(), getOversString(), maidens,
                runsConceded, wicketsTaken, getEconomy());
    }
}

// ===================== OBSERVER PATTERN — SCOREBOARD =====================

interface ScoreboardObserver {
    void onScoreUpdate(String updateMessage);
}

class LiveScoreboard implements ScoreboardObserver {
    @Override
    public void onScoreUpdate(String updateMessage) {
        System.out.println("[LIVE] " + updateMessage);
    }
}

class CommentaryPanel implements ScoreboardObserver {
    @Override
    public void onScoreUpdate(String updateMessage) {
        System.out.println("[COMMENTARY] " + updateMessage);
    }
}

// ===================== STRATEGY — MATCH FORMAT RULES =====================

interface MatchRulesStrategy {
    int getMaxOversPerInnings();
    int getMaxInningsPerTeam();
    String getFormatName();
}

class T20Rules implements MatchRulesStrategy {
    @Override
    public int getMaxOversPerInnings() { return 20; }
    @Override
    public int getMaxInningsPerTeam() { return 1; }
    @Override
    public String getFormatName() { return "T20"; }
}

class ODIRules implements MatchRulesStrategy {
    @Override
    public int getMaxOversPerInnings() { return 50; }
    @Override
    public int getMaxInningsPerTeam() { return 1; }
    @Override
    public String getFormatName() { return "ODI"; }
}

class TestRules implements MatchRulesStrategy {
    @Override
    public int getMaxOversPerInnings() { return Integer.MAX_VALUE; } // No limit
    @Override
    public int getMaxInningsPerTeam() { return 2; } // Each team bats twice
    @Override
    public String getFormatName() { return "Test"; }
}

// ===================== INNINGS =====================

class Innings {
    private Team battingTeam;
    private Team bowlingTeam;
    private int inningsNumber;
    private List<Over> overs;
    private Map<String, BattingScorecard> battingCards; // keyed by player name
    private Map<String, BowlingScorecard> bowlingCards;
    private int totalRuns;
    private int totalWickets;
    private int totalExtras;
    private List<ScoreboardObserver> observers;
    private MatchRulesStrategy rules;

    public Innings(Team battingTeam, Team bowlingTeam, int inningsNumber,
                   MatchRulesStrategy rules) {
        this.battingTeam = battingTeam;
        this.bowlingTeam = bowlingTeam;
        this.inningsNumber = inningsNumber;
        this.overs = new ArrayList<>();
        this.battingCards = new LinkedHashMap<>();
        this.bowlingCards = new LinkedHashMap<>();
        this.observers = new ArrayList<>();
        this.rules = rules;
    }

    public int getTotalRuns() { return totalRuns; }
    public int getTotalWickets() { return totalWickets; }
    public List<Over> getOvers() { return overs; }

    public void addObserver(ScoreboardObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(String message) {
        for (ScoreboardObserver obs : observers) {
            obs.onScoreUpdate(message);
        }
    }

    // Get or create batting scorecard for a player
    private BattingScorecard getBattingCard(Player batsman) {
        return battingCards.computeIfAbsent(batsman.getName(),
                k -> new BattingScorecard(batsman));
    }

    // Get or create bowling scorecard for a player
    private BowlingScorecard getBowlingCard(Player bowler) {
        return bowlingCards.computeIfAbsent(bowler.getName(),
                k -> new BowlingScorecard(bowler));
    }

    // Start a new over with a given bowler
    public Over startNewOver(Player bowler) {
        int overNumber = overs.size() + 1;
        Over over = new Over(overNumber, bowler);
        overs.add(over);
        return over;
    }

    // Record a ball — the core scoring method
    public void recordBall(Over over, Player batsman, Player bowler,
                           int runs, WicketType wicketType,
                           ExtrasType extrasType, int extraRuns) {
        int ballNumber = over.getLegalDeliveries() + 1;
        Ball ball = new Ball(ballNumber, batsman, bowler);

        ball.setRuns(runs);
        if (wicketType != null) {
            ball.setWicket(wicketType);
        }
        if (extrasType != ExtrasType.NONE) {
            ball.setExtras(extrasType, extraRuns);
        }

        over.addBall(ball);

        // Update batting scorecard
        BattingScorecard batCard = getBattingCard(batsman);
        batCard.addRuns(runs);
        if (ball.isLegalDelivery()) {
            batCard.incrementBallsFaced();
        }
        if (ball.isWicket()) {
            batCard.setOut(wicketType);
            totalWickets++;
        }

        // Update bowling scorecard
        BowlingScorecard bowlCard = getBowlingCard(bowler);
        bowlCard.addRuns(ball.getTotalRuns());
        if (ball.isLegalDelivery()) {
            bowlCard.addLegalDelivery();
        }
        if (ball.isWicket()) {
            bowlCard.addWicket();
        }

        // Update innings totals
        totalRuns += ball.getTotalRuns();
        totalExtras += extraRuns;

        // Notify observers
        String msg = battingTeam.getName() + " " + totalRuns + "/" + totalWickets
                + " (" + getOversString() + " ov)";
        if (runs == 4) msg += " — FOUR by " + batsman.getName() + "!";
        if (runs == 6) msg += " — SIX by " + batsman.getName() + "!";
        if (ball.isWicket()) msg += " — WICKET! " + batsman.getName()
                + " " + wicketType + "!";
        notifyObservers(msg);

        // Check if over is complete — record maiden if applicable
        if (over.isComplete() && over.isMaiden()) {
            bowlCard.addMaiden();
        }
    }

    // Current overs as string like "12.4"
    public String getOversString() {
        if (overs.isEmpty()) return "0";
        Over lastOver = overs.get(overs.size() - 1);
        int completedOvers = overs.size() - 1;
        int ballsInLast = lastOver.getLegalDeliveries();
        if (lastOver.isComplete()) {
            return String.valueOf(overs.size());
        }
        return completedOvers + "." + ballsInLast;
    }

    // Check if innings is over (all out or overs exhausted)
    public boolean isInningsComplete() {
        if (totalWickets >= 10) return true; // All out (10 wickets = 11 batsmen, last one not out)
        if (!overs.isEmpty()) {
            int completedOvers = 0;
            for (Over o : overs) {
                if (o.isComplete()) completedOvers++;
            }
            if (completedOvers >= rules.getMaxOversPerInnings()) return true;
        }
        return false;
    }

    // Print the full scorecard
    public void printScorecard() {
        System.out.println("\n===== " + battingTeam.getName() + " INNINGS =====");
        System.out.println("Total: " + totalRuns + "/" + totalWickets
                + " (" + getOversString() + " overs)");
        System.out.println("\n--- Batting ---");
        for (BattingScorecard card : battingCards.values()) {
            System.out.println(card);
        }
        System.out.println("\n--- Bowling ---");
        for (BowlingScorecard card : bowlingCards.values()) {
            System.out.println(card);
        }
    }
}

// ===================== MATCH =====================

class CricketMatch {
    private Team team1;
    private Team team2;
    private MatchRulesStrategy rules;
    private List<Innings> inningsList;

    public CricketMatch(Team team1, Team team2, MatchRulesStrategy rules) {
        this.team1 = team1;
        this.team2 = team2;
        this.rules = rules;
        this.inningsList = new ArrayList<>();
    }

    public Innings startInnings(Team battingTeam, Team bowlingTeam) {
        int inningsNumber = inningsList.size() + 1;
        Innings innings = new Innings(battingTeam, bowlingTeam, inningsNumber, rules);
        inningsList.add(innings);
        System.out.println("\n*** " + rules.getFormatName() + " Match — Innings "
                + inningsNumber + ": " + battingTeam.getName() + " batting ***");
        return innings;
    }

    public void printMatchSummary() {
        System.out.println("\n========== MATCH SUMMARY ==========");
        System.out.println(rules.getFormatName() + ": " + team1.getName()
                + " vs " + team2.getName());
        for (Innings innings : inningsList) {
            innings.printScorecard();
        }
    }
}

// ===================== MAIN =====================

public class CricketScoringMain {
    public static void main(String[] args) {
        // Create teams
        Team india = new Team("India");
        Player virat = new Player("Virat Kohli", PlayerRole.BATSMAN);
        Player rohit = new Player("Rohit Sharma", PlayerRole.BATSMAN);
        Player bumrah = new Player("Jasprit Bumrah", PlayerRole.BOWLER);
        india.addPlayer(virat);
        india.addPlayer(rohit);
        india.addPlayer(bumrah);
        // ... add remaining players (simplified for code brevity)

        Team australia = new Team("Australia");
        Player smith = new Player("Steve Smith", PlayerRole.BATSMAN);
        Player starc = new Player("Mitchell Starc", PlayerRole.BOWLER);
        Player cummins = new Player("Pat Cummins", PlayerRole.ALL_ROUNDER);
        australia.addPlayer(smith);
        australia.addPlayer(starc);
        australia.addPlayer(cummins);

        // Create a T20 match
        CricketMatch match = new CricketMatch(india, australia, new T20Rules());

        // India batting first
        Innings firstInnings = match.startInnings(india, australia);

        // Add live scoreboard observers
        firstInnings.addObserver(new LiveScoreboard());
        firstInnings.addObserver(new CommentaryPanel());

        // Simulate Over 1: Starc bowling to Rohit
        Over over1 = firstInnings.startNewOver(starc);
        firstInnings.recordBall(over1, rohit, starc, 0, null, ExtrasType.NONE, 0);  // Dot ball
        firstInnings.recordBall(over1, rohit, starc, 4, null, ExtrasType.NONE, 0);  // FOUR!
        firstInnings.recordBall(over1, rohit, starc, 1, null, ExtrasType.NONE, 0);  // Single
        firstInnings.recordBall(over1, virat, starc, 6, null, ExtrasType.NONE, 0);  // SIX by Kohli!
        firstInnings.recordBall(over1, virat, starc, 0, null, ExtrasType.WIDE, 1);  // Wide
        firstInnings.recordBall(over1, virat, starc, 2, null, ExtrasType.NONE, 0);  // Two runs
        firstInnings.recordBall(over1, virat, starc, 0, WicketType.BOWLED, ExtrasType.NONE, 0); // WICKET!

        // Simulate Over 2: Cummins bowling
        Over over2 = firstInnings.startNewOver(cummins);
        firstInnings.recordBall(over2, rohit, cummins, 4, null, ExtrasType.NONE, 0);
        firstInnings.recordBall(over2, rohit, cummins, 0, null, ExtrasType.NONE, 0);
        firstInnings.recordBall(over2, rohit, cummins, 1, null, ExtrasType.NONE, 0);
        // ... more balls ...

        // Print scorecard
        firstInnings.printScorecard();
        match.printMatchSummary();
    }
}
```

### Key Points for Interview

- **Observer Pattern** updates the live scoreboard and commentary panel every ball — very natural fit.
- **Strategy Pattern** for match rules — T20, ODI, Test each have different over limits and innings counts.
- Ball-by-ball tracking allows exact replay of the match (like CricBuzz's ball-by-ball section).
- Legal deliveries vs total deliveries is crucial — wides and no-balls do not count toward the over.
- Strike rate and economy are real cricket metrics — shows domain knowledge.

---

## Problem 5: Social Media Platform (Instagram / Twitter)

### System Description

Design a social media platform like Instagram or Twitter. Users create profiles, follow each other, create posts, like and comment on posts, and see a feed. When someone likes or comments on your post, or follows you, you get a notification. The feed can be sorted chronologically or by an algorithm.

Think of scrolling through Instagram in India — you follow Virat Kohli, see his latest Reel, like it, and he gets a notification (well, among millions).

### Requirements

1. Users can create profiles with name, bio, profile picture
2. Users can follow/unfollow other users
3. Users can create text/image posts
4. Users can like and comment on posts
5. Feed shows posts from followed users
6. Notifications for likes, comments, and new followers
7. Feed can be sorted chronologically or algorithmically (by engagement)

### Design Patterns Used

| Pattern | Where Used |
|---------|-----------|
| **Observer** | Notifications — followers notified when user posts; author notified on like/comment |
| **Strategy** | Feed generation — chronological vs algorithmic sorting |
| **Singleton** | SocialMediaService (central service) |

### Entities

- **User** — id, name, bio, followers, following, posts
- **Post** — id, author, content, imageUrl, likes, comments, timestamp
- **Comment** — id, author, content, timestamp
- **Like** — user, timestamp
- **Follow** — follower, following, timestamp
- **Notification** — type (LIKE/COMMENT/FOLLOW/POST), message, timestamp
- **FeedStrategy** — interface for generating a user's feed
- **SocialMediaService** — manages users, posts, follows

### Full Java Code

```java
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

// ===================== ENUMS =====================

enum NotificationType {
    LIKE,       // "Virat Kohli liked your post"
    COMMENT,    // "Rohit Sharma commented on your post"
    FOLLOW,     // "Sachin Tendulkar started following you"
    POST        // "Deepika Padukone shared a new post"
}

// ===================== NOTIFICATION =====================

class Notification {
    private String id;
    private NotificationType type;
    private String message;
    private LocalDateTime timestamp;
    private boolean isRead;

    public Notification(String id, NotificationType type, String message) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    public NotificationType getType() { return type; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public void markRead() { this.isRead = true; }

    @Override
    public String toString() {
        return "[" + type + "] " + message;
    }
}

// ===================== COMMENT =====================

class Comment {
    private String id;
    private User author;
    private String content;
    private LocalDateTime timestamp;

    public Comment(String id, User author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public User getAuthor() { return author; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

// ===================== LIKE =====================

class Like {
    private User user;
    private LocalDateTime timestamp;

    public Like(User user) {
        this.user = user;
        this.timestamp = LocalDateTime.now();
    }

    public User getUser() { return user; }
}

// ===================== POST =====================

class Post {
    private String id;
    private User author;
    private String content;
    private String imageUrl;       // null for text-only posts
    private List<Like> likes;
    private List<Comment> comments;
    private LocalDateTime timestamp;

    public Post(String id, User author, String content, String imageUrl) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.imageUrl = imageUrl;
        this.likes = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public User getAuthor() { return author; }
    public String getContent() { return content; }
    public List<Like> getLikes() { return likes; }
    public List<Comment> getComments() { return comments; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getLikeCount() { return likes.size(); }
    public int getCommentCount() { return comments.size(); }

    // Engagement score = likes + (comments * 2) — comments are more valuable
    public int getEngagementScore() {
        return likes.size() + (comments.size() * 2);
    }

    public void addLike(Like like) {
        // Prevent double-liking
        boolean alreadyLiked = likes.stream()
                .anyMatch(l -> l.getUser().getId().equals(like.getUser().getId()));
        if (!alreadyLiked) {
            likes.add(like);
        }
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    @Override
    public String toString() {
        return author.getName() + ": " + content
                + " [Likes:" + likes.size() + " Comments:" + comments.size() + "]";
    }
}

// ===================== USER =====================

class User {
    private String id;
    private String name;
    private String bio;
    private String profilePicUrl;
    private Set<String> followers;     // Set of user IDs who follow this user
    private Set<String> following;     // Set of user IDs this user follows
    private List<Post> posts;
    private List<Notification> notifications;

    public User(String id, String name, String bio) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.followers = new HashSet<>();
        this.following = new HashSet<>();
        this.posts = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getBio() { return bio; }
    public Set<String> getFollowers() { return followers; }
    public Set<String> getFollowing() { return following; }
    public List<Post> getPosts() { return posts; }
    public List<Notification> getNotifications() { return notifications; }

    public void addPost(Post post) { posts.add(post); }

    public void addFollower(String userId) { followers.add(userId); }
    public void removeFollower(String userId) { followers.remove(userId); }
    public void addFollowing(String userId) { following.add(userId); }
    public void removeFollowing(String userId) { following.remove(userId); }

    public void addNotification(Notification notification) {
        notifications.add(notification);
        System.out.println("  -> " + name + " received: " + notification);
    }

    public int getFollowerCount() { return followers.size(); }
    public int getFollowingCount() { return following.size(); }
}

// ===================== STRATEGY PATTERN — FEED GENERATION =====================

// Strategy interface for different feed algorithms
interface FeedStrategy {
    List<Post> generateFeed(User user, Map<String, User> allUsers);
}

// Simple chronological feed — newest posts first (like old Twitter)
class ChronologicalFeedStrategy implements FeedStrategy {
    @Override
    public List<Post> generateFeed(User user, Map<String, User> allUsers) {
        List<Post> feed = new ArrayList<>();

        // Collect all posts from users this person follows
        for (String followingId : user.getFollowing()) {
            User followedUser = allUsers.get(followingId);
            if (followedUser != null) {
                feed.addAll(followedUser.getPosts());
            }
        }

        // Sort by timestamp — newest first
        feed.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return feed;
    }
}

// Algorithmic feed — sorted by engagement (like Instagram's "top posts")
class AlgorithmicFeedStrategy implements FeedStrategy {
    @Override
    public List<Post> generateFeed(User user, Map<String, User> allUsers) {
        List<Post> feed = new ArrayList<>();

        for (String followingId : user.getFollowing()) {
            User followedUser = allUsers.get(followingId);
            if (followedUser != null) {
                feed.addAll(followedUser.getPosts());
            }
        }

        // Sort by engagement score — highest engagement first
        // This is a simplified version of what Instagram does (they also factor in
        // recency, your past interactions, post type, etc.)
        feed.sort((a, b) -> {
            int engageDiff = b.getEngagementScore() - a.getEngagementScore();
            if (engageDiff != 0) return engageDiff;
            // Tie-break by recency
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
        return feed;
    }
}

// ===================== OBSERVER PATTERN — NOTIFICATION SERVICE =====================

interface SocialEventObserver {
    void onNewPost(User author, Post post);
    void onLike(User liker, Post post);
    void onComment(User commenter, Post post, Comment comment);
    void onFollow(User follower, User followed);
}

class NotificationManager implements SocialEventObserver {
    private Map<String, User> users;
    private int notificationCounter = 0;

    public NotificationManager(Map<String, User> users) {
        this.users = users;
    }

    @Override
    public void onNewPost(User author, Post post) {
        // Notify all followers that this user posted something new
        for (String followerId : author.getFollowers()) {
            User follower = users.get(followerId);
            if (follower != null) {
                notificationCounter++;
                follower.addNotification(new Notification(
                        "N-" + notificationCounter,
                        NotificationType.POST,
                        author.getName() + " shared a new post: \""
                                + post.getContent().substring(0, Math.min(30, post.getContent().length()))
                                + "...\""
                ));
            }
        }
    }

    @Override
    public void onLike(User liker, Post post) {
        // Notify the post author (don't notify if you liked your own post)
        if (!liker.getId().equals(post.getAuthor().getId())) {
            notificationCounter++;
            post.getAuthor().addNotification(new Notification(
                    "N-" + notificationCounter,
                    NotificationType.LIKE,
                    liker.getName() + " liked your post"
            ));
        }
    }

    @Override
    public void onComment(User commenter, Post post, Comment comment) {
        // Notify the post author
        if (!commenter.getId().equals(post.getAuthor().getId())) {
            notificationCounter++;
            post.getAuthor().addNotification(new Notification(
                    "N-" + notificationCounter,
                    NotificationType.COMMENT,
                    commenter.getName() + " commented: \"" + comment.getContent() + "\""
            ));
        }
    }

    @Override
    public void onFollow(User follower, User followed) {
        notificationCounter++;
        followed.addNotification(new Notification(
                "N-" + notificationCounter,
                NotificationType.FOLLOW,
                follower.getName() + " started following you"
        ));
    }
}

// ===================== SOCIAL MEDIA SERVICE (Singleton) =====================

class SocialMediaService {
    private static SocialMediaService instance;
    private Map<String, User> users;
    private Map<String, Post> posts;
    private NotificationManager notificationManager;
    private FeedStrategy feedStrategy;
    private int postCounter = 0;
    private int commentCounter = 0;

    private SocialMediaService() {
        this.users = new HashMap<>();
        this.posts = new HashMap<>();
        this.notificationManager = new NotificationManager(users);
        this.feedStrategy = new ChronologicalFeedStrategy(); // Default
    }

    public static SocialMediaService getInstance() {
        if (instance == null) {
            instance = new SocialMediaService();
        }
        return instance;
    }

    // Switch feed algorithm at runtime — Strategy pattern
    public void setFeedStrategy(FeedStrategy strategy) {
        this.feedStrategy = strategy;
    }

    public void registerUser(User user) {
        users.put(user.getId(), user);
        System.out.println(user.getName() + " joined the platform!");
    }

    // Follow another user
    public void follow(User follower, User followed) {
        if (follower.getId().equals(followed.getId())) {
            System.out.println("Cannot follow yourself!");
            return;
        }
        follower.addFollowing(followed.getId());
        followed.addFollower(follower.getId());
        System.out.println(follower.getName() + " followed " + followed.getName());
        notificationManager.onFollow(follower, followed);
    }

    // Unfollow a user
    public void unfollow(User follower, User followed) {
        follower.removeFollowing(followed.getId());
        followed.removeFollower(follower.getId());
        System.out.println(follower.getName() + " unfollowed " + followed.getName());
    }

    // Create a new post
    public Post createPost(User author, String content, String imageUrl) {
        postCounter++;
        Post post = new Post("POST-" + postCounter, author, content, imageUrl);
        author.addPost(post);
        posts.put(post.getId(), post);
        System.out.println(author.getName() + " posted: \"" + content + "\"");
        notificationManager.onNewPost(author, post);
        return post;
    }

    // Like a post
    public void likePost(User user, Post post) {
        Like like = new Like(user);
        post.addLike(like);
        System.out.println(user.getName() + " liked " + post.getAuthor().getName() + "'s post");
        notificationManager.onLike(user, post);
    }

    // Comment on a post
    public Comment commentOnPost(User user, Post post, String content) {
        commentCounter++;
        Comment comment = new Comment("CMT-" + commentCounter, user, content);
        post.addComment(comment);
        System.out.println(user.getName() + " commented on " + post.getAuthor().getName()
                + "'s post: \"" + content + "\"");
        notificationManager.onComment(user, post, comment);
        return comment;
    }

    // Generate feed for a user using the current strategy
    public List<Post> getFeed(User user) {
        return feedStrategy.generateFeed(user, users);
    }
}

// ===================== MAIN =====================

public class SocialMediaMain {
    public static void main(String[] args) {
        SocialMediaService service = SocialMediaService.getInstance();

        // Create users
        User virat = new User("U1", "Virat Kohli", "Cricketer | Blue tick verified");
        User deepika = new User("U2", "Deepika Padukone", "Actor | Mental health advocate");
        User rahul = new User("U3", "Rahul Kumar", "Just a regular guy from Pune");
        User priya = new User("U4", "Priya Sharma", "Foodie | Traveller | Mumbai");

        service.registerUser(virat);
        service.registerUser(deepika);
        service.registerUser(rahul);
        service.registerUser(priya);

        // Follow relationships
        service.follow(rahul, virat);     // Rahul follows Virat
        service.follow(rahul, deepika);   // Rahul follows Deepika
        service.follow(priya, virat);     // Priya follows Virat
        service.follow(priya, rahul);     // Priya follows Rahul

        System.out.println("\n--- Creating Posts ---");

        // Virat posts
        Post viratPost = service.createPost(virat,
                "What a win at Wankhede! The crowd was unreal. #INDvAUS",
                "wankhede_celebration.jpg");

        // Deepika posts
        Post deepikaPost = service.createPost(deepika,
                "New film announcement coming soon! Stay tuned.",
                "teaser_poster.jpg");

        // Rahul posts
        Post rahulPost = service.createPost(rahul,
                "Had the best vada pav at Anand Stall, Dadar. 10/10.",
                "vada_pav.jpg");

        System.out.println("\n--- Interactions ---");

        // Rahul likes Virat's post
        service.likePost(rahul, viratPost);

        // Priya comments on Virat's post
        service.commentOnPost(priya, viratPost, "King Kohli! What an innings!");

        // Priya likes Rahul's vada pav post
        service.likePost(priya, rahulPost);

        // Rahul comments on Deepika's post
        service.commentOnPost(rahul, deepikaPost, "Can't wait! Is it with SRK?");

        System.out.println("\n--- Rahul's Feed (Chronological) ---");
        List<Post> feed = service.getFeed(rahul);
        for (Post post : feed) {
            System.out.println("  " + post);
        }

        // Switch to algorithmic feed
        System.out.println("\n--- Rahul's Feed (Algorithmic — by engagement) ---");
        service.setFeedStrategy(new AlgorithmicFeedStrategy());
        feed = service.getFeed(rahul);
        for (Post post : feed) {
            System.out.println("  " + post);
        }

        // Print Virat's notifications
        System.out.println("\n--- Virat's Notifications ---");
        for (Notification n : virat.getNotifications()) {
            System.out.println("  " + n);
        }
    }
}
```

### Key Points for Interview

- **Observer Pattern** for notifications — cleanly separates "what happened" from "who needs to know."
- **Strategy Pattern** for feed — swap between chronological and algorithmic at runtime without changing any other code.
- Engagement score is a simplified version of what platforms actually do (real algorithms use ML models with hundreds of features).
- Followers stored as `Set<String>` (user IDs) for O(1) lookup — important for users with millions of followers like Virat Kohli.
- Double-like prevention in `addLike()` — a real-world requirement often missed in interviews.

---

## Problem 6: File System (Linux)

### System Description

Design an in-memory file system similar to Linux. It has a hierarchical structure where directories can contain files AND other directories. You can perform standard operations like `mkdir`, `touch` (create file), `rm`, `ls`, `find`, `cd`, and `pwd`.

This is a classic Composite Pattern problem — a directory "contains" both files and directories, and you treat them uniformly through a common interface.

### Requirements

1. Hierarchical structure — directories contain files and sub-directories
2. Operations: mkdir, touch (create file), rm (remove), ls (list contents)
3. Navigation: cd (change directory), pwd (print working directory)
4. Search: find (search by name)
5. File has a size; directory's size is the sum of all contents
6. Support absolute paths (/home/user/docs) and relative paths

### Design Patterns Used

| Pattern | Where Used |
|---------|-----------|
| **Composite** | FileSystemEntry is the component; File is leaf; Directory is composite (contains children) |
| **Iterator** | Traversing directory tree for find and recursive ls |

### Entities

- **FileSystemEntry** (abstract) — name, parent, creation time, abstract `getSize()`, abstract `isDirectory()`
- **File** — extends FileSystemEntry, has content and size
- **Directory** — extends FileSystemEntry, contains list of FileSystemEntry children
- **FileSystem** — root directory, current directory, all operations (mkdir, touch, cd, etc.)

### Full Java Code

```java
import java.util.*;
import java.time.LocalDateTime;

// ===================== COMPOSITE PATTERN — FILE SYSTEM ENTRY =====================

// Component — the abstract base that both File and Directory share
abstract class FileSystemEntry {
    protected String name;
    protected Directory parent; // null for root
    protected LocalDateTime createdAt;

    public FileSystemEntry(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
        this.createdAt = LocalDateTime.now();
    }

    public String getName() { return name; }
    public Directory getParent() { return parent; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Get full path like /home/sheetal/documents/resume.txt
    public String getFullPath() {
        if (parent == null) return "/"; // Root
        String parentPath = parent.getFullPath();
        if (parentPath.equals("/")) return "/" + name;
        return parentPath + "/" + name;
    }

    // Each subclass defines its own size calculation
    public abstract long getSize();
    public abstract boolean isDirectory();
}

// ===================== LEAF — FILE =====================

// File is a leaf node — it cannot contain other entries
class File extends FileSystemEntry {
    private String content;
    private long size; // Size in bytes

    public File(String name, Directory parent) {
        super(name, parent);
        this.content = "";
        this.size = 0;
    }

    public String getContent() { return content; }

    public void setContent(String content) {
        this.content = content;
        this.size = content.length(); // Simplified: 1 char = 1 byte
    }

    @Override
    public long getSize() { return size; }

    @Override
    public boolean isDirectory() { return false; }

    @Override
    public String toString() {
        return name + " (" + size + " bytes)";
    }
}

// ===================== COMPOSITE — DIRECTORY =====================

// Directory is a composite — it contains both Files and other Directories
class Directory extends FileSystemEntry {
    private List<FileSystemEntry> children;

    public Directory(String name, Directory parent) {
        super(name, parent);
        this.children = new ArrayList<>();
    }

    public List<FileSystemEntry> getChildren() { return children; }

    public void addEntry(FileSystemEntry entry) {
        // Check for duplicate names
        for (FileSystemEntry child : children) {
            if (child.getName().equals(entry.getName())) {
                throw new RuntimeException("'" + entry.getName() + "' already exists in "
                        + this.getFullPath());
            }
        }
        children.add(entry);
    }

    public void removeEntry(FileSystemEntry entry) {
        children.remove(entry);
    }

    // Find a child by name (direct children only, not recursive)
    public FileSystemEntry getChild(String name) {
        for (FileSystemEntry child : children) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    // COMPOSITE: Directory size = sum of all children sizes (recursive)
    @Override
    public long getSize() {
        long totalSize = 0;
        for (FileSystemEntry child : children) {
            totalSize += child.getSize(); // Polymorphic call — works for both files and directories
        }
        return totalSize;
    }

    @Override
    public boolean isDirectory() { return true; }

    @Override
    public String toString() {
        return name + "/ (" + children.size() + " items, " + getSize() + " bytes)";
    }
}

// ===================== FILE SYSTEM =====================

class FileSystem {
    private Directory root;
    private Directory currentDirectory;

    public FileSystem() {
        this.root = new Directory("", null); // Root directory has empty name
        this.currentDirectory = root;
    }

    // ---- pwd: Print Working Directory ----
    public String pwd() {
        return currentDirectory.getFullPath();
    }

    // ---- cd: Change Directory ----
    public void cd(String path) {
        Directory target = navigateToDirectory(path);
        if (target != null) {
            currentDirectory = target;
            System.out.println("Changed to: " + pwd());
        } else {
            System.out.println("Directory not found: " + path);
        }
    }

    // ---- mkdir: Create Directory ----
    public Directory mkdir(String name) {
        // Check if it is a path like "a/b/c" or just a simple name
        if (name.contains("/")) {
            return mkdirPath(name);
        }

        Directory newDir = new Directory(name, currentDirectory);
        currentDirectory.addEntry(newDir);
        System.out.println("Created directory: " + newDir.getFullPath());
        return newDir;
    }

    // mkdir -p equivalent — create intermediate directories
    private Directory mkdirPath(String path) {
        String[] parts = path.split("/");
        Directory current = path.startsWith("/") ? root : currentDirectory;

        for (String part : parts) {
            if (part.isEmpty()) continue;
            FileSystemEntry existing = current.getChild(part);
            if (existing != null && existing.isDirectory()) {
                current = (Directory) existing;
            } else if (existing != null) {
                System.out.println(part + " exists but is not a directory!");
                return null;
            } else {
                Directory newDir = new Directory(part, current);
                current.addEntry(newDir);
                current = newDir;
            }
        }
        System.out.println("Created directory path: " + current.getFullPath());
        return current;
    }

    // ---- touch: Create File ----
    public File touch(String name) {
        // If file already exists, just return it (like real touch)
        FileSystemEntry existing = currentDirectory.getChild(name);
        if (existing != null && !existing.isDirectory()) {
            return (File) existing;
        }

        File newFile = new File(name, currentDirectory);
        currentDirectory.addEntry(newFile);
        System.out.println("Created file: " + newFile.getFullPath());
        return newFile;
    }

    // ---- rm: Remove file or directory ----
    public void rm(String name) {
        FileSystemEntry entry = currentDirectory.getChild(name);
        if (entry == null) {
            System.out.println("Not found: " + name);
            return;
        }
        currentDirectory.removeEntry(entry);
        System.out.println("Removed: " + entry.getFullPath());
    }

    // ---- ls: List contents of current directory ----
    public void ls() {
        ls(currentDirectory);
    }

    public void ls(Directory dir) {
        System.out.println("Contents of " + dir.getFullPath() + ":");
        if (dir.getChildren().isEmpty()) {
            System.out.println("  (empty)");
            return;
        }
        for (FileSystemEntry entry : dir.getChildren()) {
            String type = entry.isDirectory() ? "[DIR] " : "[FILE]";
            System.out.println("  " + type + " " + entry.getName()
                    + " (" + entry.getSize() + " bytes)");
        }
    }

    // ---- ls -R: Recursive listing ----
    public void lsRecursive() {
        lsRecursive(currentDirectory, 0);
    }

    private void lsRecursive(Directory dir, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + dir.getName() + "/");
        for (FileSystemEntry entry : dir.getChildren()) {
            if (entry.isDirectory()) {
                lsRecursive((Directory) entry, depth + 1);
            } else {
                System.out.println(indent + "  " + entry.getName()
                        + " (" + entry.getSize() + " bytes)");
            }
        }
    }

    // ---- find: Search for a file/directory by name (recursive) ----
    public List<FileSystemEntry> find(String name) {
        List<FileSystemEntry> results = new ArrayList<>();
        findRecursive(root, name, results);
        return results;
    }

    private void findRecursive(Directory dir, String name, List<FileSystemEntry> results) {
        for (FileSystemEntry entry : dir.getChildren()) {
            if (entry.getName().equals(name)) {
                results.add(entry);
            }
            if (entry.isDirectory()) {
                findRecursive((Directory) entry, name, results);
            }
        }
    }

    // ---- find by extension (e.g., find all .java files) ----
    public List<FileSystemEntry> findByExtension(String extension) {
        List<FileSystemEntry> results = new ArrayList<>();
        findByExtensionRecursive(root, extension, results);
        return results;
    }

    private void findByExtensionRecursive(Directory dir, String extension,
                                           List<FileSystemEntry> results) {
        for (FileSystemEntry entry : dir.getChildren()) {
            if (!entry.isDirectory() && entry.getName().endsWith(extension)) {
                results.add(entry);
            }
            if (entry.isDirectory()) {
                findByExtensionRecursive((Directory) entry, extension, results);
            }
        }
    }

    // ---- Helper: Navigate to a directory by path ----
    private Directory navigateToDirectory(String path) {
        if (path.equals("/")) return root;
        if (path.equals("..")) return currentDirectory.getParent() != null
                ? currentDirectory.getParent() : root;
        if (path.equals(".")) return currentDirectory;

        // Split path and traverse
        String[] parts = path.split("/");
        Directory current = path.startsWith("/") ? root : currentDirectory;

        for (String part : parts) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                current = current.getParent() != null ? current.getParent() : root;
                continue;
            }
            FileSystemEntry child = current.getChild(part);
            if (child != null && child.isDirectory()) {
                current = (Directory) child;
            } else {
                return null; // Path does not exist or is not a directory
            }
        }
        return current;
    }

    // ---- Utility: Get total size of entire file system ----
    public long getTotalSize() {
        return root.getSize();
    }
}

// ===================== ITERATOR — DEPTH-FIRST TRAVERSAL =====================

// Iterator that visits every entry in the file system tree (depth-first)
class FileSystemIterator implements Iterator<FileSystemEntry> {
    private Stack<FileSystemEntry> stack;

    public FileSystemIterator(Directory root) {
        this.stack = new Stack<>();
        // Push root's children in reverse order so first child is processed first
        List<FileSystemEntry> children = root.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            stack.push(children.get(i));
        }
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public FileSystemEntry next() {
        if (!hasNext()) throw new NoSuchElementException();

        FileSystemEntry entry = stack.pop();

        // If it is a directory, push its children onto the stack
        if (entry.isDirectory()) {
            Directory dir = (Directory) entry;
            List<FileSystemEntry> children = dir.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
        }

        return entry;
    }
}

// ===================== MAIN =====================

public class FileSystemMain {
    public static void main(String[] args) {
        FileSystem fs = new FileSystem();

        System.out.println("Current directory: " + fs.pwd());

        // Create directory structure like a typical Indian developer's machine
        fs.mkdir("home");
        fs.cd("home");
        fs.mkdir("sheetal");
        fs.cd("sheetal");

        // Create subdirectories
        fs.mkdir("documents");
        fs.mkdir("code");
        fs.mkdir("downloads");

        // Create some files in documents
        fs.cd("documents");
        File resume = fs.touch("resume.pdf");
        resume.setContent("Sheetal's resume - 5 pages of awesomeness");
        File aadhar = fs.touch("aadhar_scan.jpg");
        aadhar.setContent("Aadhar card scan for KYC verification");
        fs.cd("..");

        // Create files in code directory
        fs.cd("code");
        fs.mkdir("lld-practice");
        fs.cd("lld-practice");
        File chessJava = fs.touch("Chess.java");
        chessJava.setContent("public class Chess { /* full chess game implementation */ }");
        File cabJava = fs.touch("CabBooking.java");
        cabJava.setContent("public class CabBooking { /* Ola/Uber clone */ }");
        fs.cd(".."); // Back to code
        fs.cd(".."); // Back to sheetal

        // Create a file in downloads
        fs.cd("downloads");
        File movie = fs.touch("Jawan_2023.mkv");
        movie.setContent("A".repeat(1000)); // Simulate a large file
        fs.cd("..");

        // List everything recursively
        System.out.println("\n--- Recursive Listing ---");
        fs.lsRecursive();

        // Show directory sizes (Composite pattern in action)
        System.out.println("\nTotal file system size: " + fs.getTotalSize() + " bytes");

        // Find files
        System.out.println("\n--- Find 'resume.pdf' ---");
        List<FileSystemEntry> results = fs.find("resume.pdf");
        for (FileSystemEntry entry : results) {
            System.out.println("  Found: " + entry.getFullPath());
        }

        // Find all .java files
        System.out.println("\n--- Find all .java files ---");
        results = fs.findByExtension(".java");
        for (FileSystemEntry entry : results) {
            System.out.println("  Found: " + entry.getFullPath());
        }

        // Use the iterator to visit all entries
        System.out.println("\n--- Iterator traversal ---");
        // Navigate to root first
        fs.cd("/");
        FileSystemIterator iterator = new FileSystemIterator(
                (Directory) new FileSystem() {{ /* Using a separate instance for demo */ }}
                        .pwd().equals("/") ? null : null
        );

        // Simpler demo of iterator:
        Directory homeDir = (Directory) fs.find("home").stream()
                .filter(FileSystemEntry::isDirectory)
                .findFirst()
                .orElse(null);

        if (homeDir != null) {
            FileSystemIterator iter = new FileSystemIterator(homeDir);
            while (iter.hasNext()) {
                FileSystemEntry entry = iter.next();
                String type = entry.isDirectory() ? "[DIR]" : "[FILE]";
                System.out.println("  " + type + " " + entry.getFullPath());
            }
        }

        // Demonstrate path navigation
        System.out.println("\n--- Navigation ---");
        fs.cd("/home/sheetal/code/lld-practice");
        System.out.println("pwd: " + fs.pwd());
        fs.ls();

        fs.cd("../..");
        System.out.println("After cd ../..: " + fs.pwd());

        // Remove a file
        fs.cd("downloads");
        fs.rm("Jawan_2023.mkv");
        fs.ls();
    }
}
```

### Key Points for Interview

- **Composite Pattern** is the core — `getSize()` on a Directory recursively sums all children. You call the same method on both File and Directory without knowing which one you have.
- `FileSystemEntry` is the **component**, `File` is the **leaf**, `Directory` is the **composite**.
- The Iterator provides depth-first traversal of the tree — useful for operations like `find` and `du` (disk usage).
- Path navigation (`cd`, `pwd`) handles absolute paths (`/home/sheetal`), relative paths (`../code`), and special paths (`.`, `..`).
- `mkdir` with path support (like `mkdir -p`) creates intermediate directories automatically.

---

## Summary Table — All 6 Problems

| # | Problem | Core Pattern | Other Patterns | Key Entity Count |
|---|---------|-------------|----------------|-----------------|
| 1 | Chess Game | **Polymorphism** | Encapsulation, Enum | 10 classes |
| 2 | Cab Booking | **Strategy** (Fare) | Observer, Singleton, State | 12 classes |
| 3 | Shopping Cart | **Strategy** (Coupon) + **Builder** (Order) | Composite (Category) | 12 classes |
| 4 | Cricket Scoring | **Observer** (Scoreboard) | Strategy (Rules), Enum | 13 classes |
| 5 | Social Media | **Observer** (Notifications) + **Strategy** (Feed) | Singleton | 10 classes |
| 6 | File System | **Composite** | Iterator | 6 classes |

---

## Interview Tips

1. **Start with requirements** — Before jumping into code, list 5-7 requirements. Shows you think before you code.

2. **Identify the core pattern first** — Each problem has one or two key patterns. Name them upfront: "This is a classic Composite pattern problem."

3. **Draw entities before coding** — A quick sketch of classes and their relationships saves time later.

4. **Use enums for fixed sets** — GameStatus, VehicleType, WicketType. Shows clean design thinking.

5. **Keep classes small** — Each class should have one responsibility. King knows how to move; Board validates the move; Game manages turns.

6. **Think about edge cases** — Can a user like their own post? Can a pawn move backward? Can you cd to a file? These details matter.

7. **Code should compile** — Interviewers notice if your code would actually run. Practice writing full compilable code, not pseudocode.

8. **Indian examples help build rapport** — If your interviewer is from India, using Ola, CricBuzz, Flipkart examples shows real-world thinking. For international interviews, use Uber, ESPN, Amazon instead — same design, different branding.
