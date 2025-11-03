import java.util.*;

public class GameSense {
    private GameMap gameMap;
    private PlayerAI player;
    
    public GameSense(GameMap gameMap, PlayerAI player) {
        this.gameMap = gameMap;
        this.player = player;
    }
    
    /**
     * sense data container
     */
    public static class SenseData {
        public boolean hasEnemy = false;
        public boolean hasItem = false;
        public Position position = null;
        public String whatIsIt = "nothing";
        
        public SenseData() {}
    }
    
    /**
     * Check what's directly in front of player
     */
    public SenseData checkFront() {
        SenseData result = new SenseData();
        Position playerPos = player.getPosition();
        String facing = player.getDirection();
        
        Position frontPos = playerPos.getAdjacent(facing);
        Room currentRoom = player.getCurrentRoom();
        
        // Check if position is valid
        if (frontPos.getX() < 0 || frontPos.getX() >= currentRoom.getWidth() ||
            frontPos.getY() < 0 || frontPos.getY() >= currentRoom.getHeight()) {
            result.whatIsIt = "wall";
            return result;
        }
        
        Tile frontTile = currentRoom.checkTile(frontPos);
        
        result.whatIsIt = "empty";

        if (frontTile.getEntityType().equals("enemy")) {
            result.hasEnemy = true;
            result.whatIsIt = "enemy";
            result.position = frontPos;
        } else if (frontTile.getEntityType().equals("item")) {
            result.hasItem = true;
            result.whatIsIt = "item";
            result.position = frontPos;
        } else {
            
        }
        
        return result;
    }
    
    /**
     * Find nearest enemy using BFS
     */
    public SenseData findNearestEnemy() {
        return findNearestThing("enemy");
    }
    
    /**
     * Find nearest item using BFS
     */
    public SenseData findNearestItem() {
        return findNearestThing("item");
    }
    
    /**
     * BFS search for nearest entity of specified type
     */
    private SenseData findNearestThing(String targetType) {
        SenseData result = new SenseData();
        Room currentRoom = player.getCurrentRoom();
        Position start = player.getPosition();
        
        // BFS setup
        Queue<Position> queue = new LinkedList<>();
        boolean[][] visited = new boolean[currentRoom.getHeight()][currentRoom.getWidth()];
        
        queue.add(start);
        visited[start.getY()][start.getX()] = true;
        
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            
            // Check if current tile has what we're looking for
            Tile tile = currentRoom.checkTile(current);
            if (tile.getEntityType().equals(targetType)) {
                result.position = current;
                result.whatIsIt = targetType; 
                if (targetType.equals("enemy")) result.hasEnemy = true;
                if (targetType.equals("item")) result.hasItem = true;
                return result;
            }
            
            // Add neighbors to queue
            addNeighborsToQueue(current, queue, visited, currentRoom);
        }
        
        return result; // Nothing found
    }
    
    /**
     * Find path to a target position using BFS
     */
    public ArrayList<Position> findPathTo(Position target) {
        Room currentRoom = player.getCurrentRoom();
        Position start = player.getPosition();
        
        // BFS for pathfinding
        Queue<Position> queue = new LinkedList<>();
        boolean[][] visited = new boolean[currentRoom.getHeight()][currentRoom.getWidth()];
        Map<Position, Position> cameFrom = new HashMap<>(); // Tracks path
        
        queue.add(start);
        visited[start.getY()][start.getX()] = true;
        cameFrom.put(start, null);
        
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            
            // Found target
            if (current.equals(target)) {
                return buildPath(cameFrom, target);
            }
            
            // Explore neighbors
            for (Position neighbor : getWalkableNeighbors(current, currentRoom)) {
                if (!visited[neighbor.getY()][neighbor.getX()]) {
                    visited[neighbor.getY()][neighbor.getX()] = true;
                    cameFrom.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        
        return new ArrayList<>(); // No path found
    }
    
    /**
     * Simple decision maker - tells player what to do next
     */
    public String whatShouldIDo() {
        Position playerPos = player.getPosition();
        
        // 1. Check for immediate danger
        SenseData front = checkFront(); 
        if (front.hasEnemy) {
            return "Attack or run! Enemy in front!";
        }
        
        // 2. Check for nearby enemies
        SenseData nearestEnemy = findNearestEnemy();
        if (nearestEnemy.hasEnemy) {
            int distance = Math.abs(playerPos.getX() - nearestEnemy.position.getX()) + 
                          Math.abs(playerPos.getY() - nearestEnemy.position.getY());
            
            if (distance <= 2) {
                return "Enemy nearby! Be careful!";
            }
        }
        
        // 3. Check for low health and find health potions
        if (player.getHealth() < player.getMaxHealth() * 0.4) {
            SenseData healthPotion = findNearestItem();
            if (healthPotion.hasItem) {
                return "Low health! Find health potion at (" + 
                       healthPotion.position.getX() + "," + healthPotion.position.getY() + ")";
            }
        }
        
        // 4. Check for items to pick up
        SenseData nearestItem = findNearestItem();
        if (nearestItem.hasItem) {
            return "Item nearby at (" + nearestItem.position.getX() + "," + nearestItem.position.getY() + ")";
        }
        
        // 5. Default action
        return "Explore the room";
    }
    
    /**
     * Get direction to move toward a position
     */
    public String getDirectionTo(Position target) {
        Position current = player.getPosition();
        
        int dx = target.getX() - current.getX();
        int dy = target.getY() - current.getY();
        
        // Prefer horizontal movement first
        if (dx > 0) return "right";
        if (dx < 0) return "left";
        if (dy > 0) return "down";
        if (dy < 0) return "up";
        
        return "up"; // Default
    }
    
    // Helper methods
    private void addNeighborsToQueue(Position pos, Queue<Position> queue, boolean[][] visited, Room room) {
        String[] directions = {"up", "down", "left", "right"};
        
        for (String dir : directions) {
            Position neighbor = pos.getAdjacent(dir);
            
            // Check bounds
            if (neighbor.getX() < 0 || neighbor.getX() >= room.getWidth() ||
                neighbor.getY() < 0 || neighbor.getY() >= room.getHeight()) {
                continue;
            }
            
            // Check if visited and walkable
            if (!visited[neighbor.getY()][neighbor.getX()]) {
                Tile tile = room.checkTile(neighbor);
                if (tile.isWalkable()) {
                    visited[neighbor.getY()][neighbor.getX()] = true;
                    queue.add(neighbor);
                }
            }
        }
    }
    
    private List<Position> getWalkableNeighbors(Position pos, Room room) {
        List<Position> neighbors = new ArrayList<>();
        String[] directions = {"up", "down", "left", "right"};
        
        for (String dir : directions) {
            Position neighbor = pos.getAdjacent(dir);
            
            // Check bounds
            if (neighbor.getX() < 0 || neighbor.getX() >= room.getWidth() ||
                neighbor.getY() < 0 || neighbor.getY() >= room.getHeight()) {
                continue;
            }
            
            // Check if walkable
            Tile tile = room.checkTile(neighbor);
            if (tile.isWalkable()) {
                neighbors.add(neighbor);
            }
        }
        
        return neighbors;
    }
    
    private ArrayList<Position> buildPath(Map<Position, Position> cameFrom, Position target) {
        ArrayList<Position> path = new ArrayList<>();
        Position current = target;
        
        // Work backwards from target to start
        while (current != null) {
            path.add(0, current); // Add to front to reverse
            current = cameFrom.get(current);
        }
        
        return path;
    }
    
    /**
     * Print current game state information
     */
    public void printGameInfo() {
        System.out.println("=== GAME INFO ===");
        System.out.println("Health: " + player.getHealth() + "/" + player.getMaxHealth());
        System.out.println("Position: (" + player.getPosition().getX() + "," + player.getPosition().getY() + ")");
        System.out.println("Facing: " + player.getDirection() + " -> " + this.player.getPosition().getAdjacent(this.player.getDirection()).getCoordinates());
        
        SenseData front = checkFront();
        System.out.println("In front: " + front.whatIsIt);
        
        SenseData enemy = findNearestEnemy();
        if (enemy.hasEnemy) {
            System.out.println("Nearest enemy: (" + enemy.position.getX() + "," + enemy.position.getY() + ")");
        }
        
        SenseData item = findNearestItem();
        if (item.hasItem) {
            System.out.println("Nearest item: (" + item.position.getX() + "," + item.position.getY() + ")");
        }
        
        System.out.println("Suggestion: " + whatShouldIDo());
        System.out.println("=================");
    }
} 