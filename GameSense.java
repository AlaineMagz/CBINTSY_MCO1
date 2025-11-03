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
     * LEVEL SENSE: Detects whether adjacent rooms contain enemies or items
     */
    public Map<String, String> getLevelSense() {
        Map<String, String> roomHints = new HashMap<>();
        Room currentRoom = player.getCurrentRoom();
        Position roomPos = currentRoom.getRoomPos();
        
        // Check all four directions for adjacent rooms
        String[] directions = {"up", "down", "left", "right"};
        
        for (String dir : directions) {
            Position adjacentRoomPos = getAdjacentRoomPosition(roomPos, dir);
            
            if (isValidRoomPosition(adjacentRoomPos)) {
                Room adjacentRoom = gameMap.getAllRooms()[adjacentRoomPos.getY()][adjacentRoomPos.getX()];
                String hint = getRoomHint(adjacentRoom);
                roomHints.put(dir, hint);
            } else {
                roomHints.put(dir, "no room");
            }
        }
        
        return roomHints;
    }
    
    /**
     * ROOM SENSE: Detects enemies or items in the current room
     * Returns suggestion based on room content
     */
    public String getRoomSense() {
        Room currentRoom = player.getCurrentRoom();
        
        // Check for enemies in current room
        SenseData nearestEnemy = findNearestEnemy();
        boolean hasEnemiesInRoom = nearestEnemy.hasEnemy;
        
        // Check for items in current room  
        SenseData nearestItem = findNearestItem();
        boolean hasItemsInRoom = nearestItem.hasItem;
        
        StringBuilder suggestion = new StringBuilder();
        
        if (hasEnemiesInRoom) {
            suggestion.append("Enemies detected in this room. ");
            if (player.getHealth() > player.getMaxHealth() * 0.6) {
                suggestion.append("Consider engaging. ");
            } else {
                suggestion.append("Consider fleeing. ");
            }
        }
        
        if (hasItemsInRoom) {
            suggestion.append("Items available. ");
            if (player.getHealth() < player.getMaxHealth() * 0.4) {
                suggestion.append("Prioritize health items. ");
            }
        }
        
        // If room is empty, suggest exploring other rooms
        if (!hasEnemiesInRoom && !hasItemsInRoom) {
            suggestion.append("Room is empty. Explore other rooms through doors. ");
            
            // Find available doors
            List<String> availableDoors = getAvailableDoors();
            if (!availableDoors.isEmpty()) {
                suggestion.append("Available exits: ").append(String.join(", ", availableDoors));
            }
        }
        
        return suggestion.toString().trim();
    }
    
    /**
     * Enhanced decision maker that uses both Level Sense and Room Sense
     */
    public String whatShouldIDo() {
        Position playerPos = player.getPosition();
        
        // 1. Check for immediate danger (enemy in front)
        SenseData front = checkFront(); 
        if (front.hasEnemy) {
            return "Attack or run! Enemy in front!";
        }
        
        // 2. Use Room Sense for current room assessment
        String roomAssessment = getRoomSense();
        
        // 3. Check for nearby enemies in current room
        SenseData nearestEnemy = findNearestEnemy();
        if (nearestEnemy.hasEnemy) {
            int distance = Math.abs(playerPos.getX() - nearestEnemy.position.getX()) + 
                          Math.abs(playerPos.getY() - nearestEnemy.position.getY());
            
            if (distance <= 2) {
                return "Enemy nearby! " + roomAssessment;
            }
        }
        
        // 4. Check for low health and find health potions
        if (player.getHealth() < player.getMaxHealth() * 0.4) {
            SenseData healthPotion = findNearestItem();
            if (healthPotion.hasItem) {
                return "Low health! Find health potion. " + roomAssessment;
            }
        }
        
        // 5. Check for items to pick up
        SenseData nearestItem = findNearestItem();
        if (nearestItem.hasItem) {
            return "Item nearby. " + roomAssessment;
        }
        
        // 6. If current room is empty, use Level Sense to decide where to go
        if (roomAssessment.contains("Room is empty")) {
            Map<String, String> levelSense = getLevelSense();
            String bestDirection = findBestDirectionFromLevelSense(levelSense);
            if (!bestDirection.equals("none")) {
                return "Room empty. Move " + bestDirection + " for better opportunities. " + 
                       getLevelSenseHint(levelSense);
            }
        }
        
        // 7. Default: explore with room sense info
        return "Explore. " + roomAssessment;
    }
    
    /**
     * Check if there's a door in the specified direction from current position
     */
    public boolean isDoorInDirection(String direction) {
        Position checkPos = player.getPosition().getAdjacent(direction);
        Room currentRoom = player.getCurrentRoom();
        
        if (!isValidPosition(checkPos, currentRoom)) {
            return false;
        }
        
        Tile tile = currentRoom.checkTile(checkPos);
        return tile.getType().equals("door") || tile.getType().equals("XitDoor");
    }
    
    /**
     * Get the position of the door in the specified direction
     */
    public Position getDoorPositionInDirection(String direction) {
        return player.getPosition().getAdjacent(direction);
    }
    
    /**
     * Find path to the nearest door in the specified direction
     */
    public ArrayList<Position> findPathToDoor(String direction) {
        Position doorPos = getDoorPositionInDirection(direction);
        if (isDoorInDirection(direction)) {
            return findPathTo(doorPos);
        }
        return new ArrayList<>();
    }
    
    // Helper methods for Level Sense and Room Sense
    
    private Position getAdjacentRoomPosition(Position roomPos, String direction) {
        switch(direction) {
            case "up": return new Position(roomPos.getX(), roomPos.getY() - 1);
            case "down": return new Position(roomPos.getX(), roomPos.getY() + 1);
            case "left": return new Position(roomPos.getX() - 1, roomPos.getY());
            case "right": return new Position(roomPos.getX() + 1, roomPos.getY());
            default: return roomPos;
        }
    }
    
    private boolean isValidRoomPosition(Position roomPos) {
        return roomPos.getX() >= 0 && roomPos.getX() < gameMap.getWidthByRoom() &&
               roomPos.getY() >= 0 && roomPos.getY() < gameMap.getHeightByRoom();
    }
    
    private String getRoomHint(Room room) {
        boolean hasEnemies = room.hasEnemy();
        boolean hasItems = room.hasItem();
        
        if (hasEnemies && hasItems) return "enemies and items";
        if (hasEnemies) return "enemies";
        if (hasItems) return "items";
        return "empty";
    }
    
    public List<String> getAvailableDoors() {
        List<String> availableDoors = new ArrayList<>();
        String[] directions = {"up", "down", "left", "right"};
        
        for (String dir : directions) {
            if (isDoorInDirection(dir)) {
                availableDoors.add(dir);
            }
        }
        
        return availableDoors;
    }
    
    private String findBestDirectionFromLevelSense(Map<String, String> levelSense) {
        // Only consider directions that actually have doors
        List<String> availableDoors = getAvailableDoors();
        
        // Priority: rooms with items > rooms with enemies > empty rooms
        for (String dir : availableDoors) {
            String hint = levelSense.get(dir);
            if (hint != null && hint.contains("items")) return dir;
        }
        for (String dir : availableDoors) {
            String hint = levelSense.get(dir);
            if (hint != null && hint.contains("enemies")) return dir;
        }
        for (String dir : availableDoors) {
            String hint = levelSense.get(dir);
            if (hint != null && hint.equals("empty")) return dir;
        }
        
        // If no specific hints, just use first available door
        return availableDoors.isEmpty() ? "none" : availableDoors.get(0);
    }
    
    private String getLevelSenseHint(Map<String, String> levelSense) {
        StringBuilder hint = new StringBuilder("Adjacent rooms: ");
        for (Map.Entry<String, String> entry : levelSense.entrySet()) {
            if (!entry.getValue().equals("no room")) {
                hint.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
            }
        }
        return hint.toString().trim();
    }

    
    
    private boolean isValidPosition(Position pos, Room room) {
        return pos.getX() >= 0 && pos.getX() < room.getWidth() &&
               pos.getY() >= 0 && pos.getY() < room.getHeight();
    }
    
    // Existing methods remain the same...
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
        } else if (!frontTile.isWalkable()) {
            result.whatIsIt = "wall";
        }
        
        return result;
    }
    
    public SenseData findNearestEnemy() {
        return findNearestThing("enemy");
    }
    
    public SenseData findNearestItem() {
        return findNearestThing("item");
    }
    
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
        
        return result;
    }
    
    public ArrayList<Position> findPathTo(Position target) {
        Room currentRoom = player.getCurrentRoom();
        Position start = player.getPosition();
        
        Queue<Position> queue = new LinkedList<>();
        boolean[][] visited = new boolean[currentRoom.getHeight()][currentRoom.getWidth()];
        Map<Position, Position> cameFrom = new HashMap<>();
        
        queue.add(start);
        visited[start.getY()][start.getX()] = true;
        cameFrom.put(start, null);
        
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            
            if (current.equals(target)) {
                return buildPath(cameFrom, target);
            }
            
            for (Position neighbor : getWalkableNeighbors(current, currentRoom)) {
                if (!visited[neighbor.getY()][neighbor.getX()]) {
                    visited[neighbor.getY()][neighbor.getX()] = true;
                    cameFrom.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        
        return new ArrayList<>();
    }
    
    public String getDirectionTo(Position target) {
        Position current = player.getPosition();
        
        int dx = target.getX() - current.getX();
        int dy = target.getY() - current.getY();
        
        if (dx > 0) return "right";
        if (dx < 0) return "left";
        if (dy > 0) return "down";
        if (dy < 0) return "up";
        
        return "up";
    }
    
    private void addNeighborsToQueue(Position pos, Queue<Position> queue, boolean[][] visited, Room room) {
        String[] directions = {"up", "down", "left", "right"};
        
        for (String dir : directions) {
            Position neighbor = pos.getAdjacent(dir);
            
            if (neighbor.getX() < 0 || neighbor.getX() >= room.getWidth() ||
                neighbor.getY() < 0 || neighbor.getY() >= room.getHeight()) {
                continue;
            }
            
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
            
            if (neighbor.getX() < 0 || neighbor.getX() >= room.getWidth() ||
                neighbor.getY() < 0 || neighbor.getY() >= room.getHeight()) {
                continue;
            }
            
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
        
        while (current != null) {
            path.add(0, current);
            current = cameFrom.get(current);
        }
        
        return path;
    }
    
    public void printGameInfo() {
        System.out.println("=== GAME INFO ===");
        System.out.println("Health: " + player.getHealth() + "/" + player.getMaxHealth());
        System.out.println("Position: (" + player.getPosition().getX() + "," + player.getPosition().getY() + ")");
        System.out.println("Facing: " + player.getDirection() + " -> " + this.player.getPosition().getAdjacent(this.player.getDirection()).getCoordinates());
        
        SenseData front = checkFront();
        System.out.println("In front: " + front.whatIsIt);
        
        // Room Sense information
        System.out.println("Room Sense: " + getRoomSense());
        
        // Level Sense information
        Map<String, String> levelSense = getLevelSense();
        System.out.println("Level Sense: " + getLevelSenseHint(levelSense));
        
        // Available doors
        List<String> availableDoors = getAvailableDoors();
        System.out.println("Available doors: " + availableDoors);
        
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