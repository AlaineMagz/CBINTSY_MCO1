import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlayerAI extends AI {

    private GameSense gameSense;
    private ArrayList<Item> inventory;
    private Weapon equippedWeapon;
    private boolean hasKey;

    public PlayerAI(Room currentRoom, Position pos, int hp, int maxHP, int aS, int ap, int maxAP, Weapon startingWeapon, GameMap gameMap) {
        super(currentRoom, pos, hp, maxHP, aS, ap, maxAP, "up");
        this.inventory = new ArrayList<>();
        this.equippedWeapon = startingWeapon;
        this.gameSense = new GameSense(gameMap, this);
        this.hasKey = false;
    }
    
    public void takeTurn() {
        gameSense.printGameInfo();
        
        String action = decideAction();
        System.out.println("AI Decision: " + action);
        
        executeAction(action);
        
        pickupItem();
    }
    
    private String decideAction() {
        GameSense.SenseData front = gameSense.checkFront();
        GameSense.SenseData nearestEnemy = gameSense.findNearestEnemy();
        GameSense.SenseData nearestItem = gameSense.findNearestItem();
        Map<String, String> levelSense = gameSense.getLevelSense();
        
        boolean enemiesInRoom = hasEnemiesInCurrentRoom();
        
        // DEBUG: Force check enemies
        System.out.println("=== ENEMY CHECK ===");
        System.out.println("GameSense enemy detection: " + nearestEnemy.hasEnemy);
        System.out.println("Room entity list check: " + checkRoomEntitiesForEnemies());
        System.out.println("Final enemiesInRoom: " + enemiesInRoom);
        System.out.println("Has Key: " + hasKey); // Debug key status
        System.out.println("===================");
        
        // NEVER leave room if enemies are present - this is the highest priority
        if (enemiesInRoom) {
            System.out.println(">>> ENEMIES IN ROOM - CANNOT LEAVE <<<");
            
            if (shouldFleeFromDanger(nearestEnemy)) {
                return "FLEE_FROM_DANGER";
            }
            
            if (shouldFleeFromLosingBattle(nearestEnemy)) {
                return "FLEE_FROM_LOSING_BATTLE";
            }
            
            if (front.hasEnemy) {
                return "ATTACK_ENEMY";
            }
            
            if (shouldEngageEnemy(nearestEnemy)) {
                return "ENGAGE_NEAREST_ENEMY";
            } else {
                return "FLEE_FROM_ENEMIES";
            }
        }
        
        // Only consider these actions if NO enemies in room
        if (front.hasEnemy) {
            return "ATTACK_ENEMY";
        }
        
        // Check if front is exit door and we don't have key
        if (front.whatIsIt.equals("XitDoor") && !hasKey) {
            System.out.println("Exit door detected but no key - avoiding exit");
            return "AVOID_EXIT_DOOR";
        }
        
        if (front.hasItem) {
            return "MOVE_TOWARD_FRONT_ITEM";
        }
        
        boolean itemsInRoom = hasItemsInCurrentRoom();
        
        if (itemsInRoom) {
            return "COLLECT_ITEMS_IN_ROOM";
        }
        
        if (nearestItem.hasItem) {
            String itemDirection = gameSense.getDirectionTo(nearestItem.position);
            if (!directionHasEnemy(itemDirection, levelSense)) {
                return "TURN_TOWARD_ITEM:" + itemDirection;
            }
        }
        
        // Room movement decisions (only if no enemies)
        if (getHealth() < getMaxHealth()) {
            String healthRoomDir = findRoomWithHealthPotions(levelSense);
            if (!healthRoomDir.equals("none")) {
                return "MOVE_TO_HEALTH_ROOM:" + healthRoomDir;
            }
        }
        
        // Only consider exit if we have the key
        if (hasKey) {
            String exitDirection = findExitDoor(levelSense);
            if (!exitDirection.equals("none")) {
                return "MOVE_TO_EXIT:" + exitDirection;
            }
        } else {
            System.out.println("No key yet - searching for key instead of exit");
        }
        
        String safeRoomDir = findRoomWithoutEnemies(levelSense);
        if (!safeRoomDir.equals("none")) {
            return "MOVE_TO_SAFE_ROOM:" + safeRoomDir;
        }
        
        String randomRoomDir = findRandomAvailableRoom(levelSense);
        if (!randomRoomDir.equals("none")) {
            return "MOVE_TO_RANDOM_ROOM:" + randomRoomDir;
        }
        
        return "EXPLORE_CURRENT_ROOM";
    }
    
    private void executeAction(String action) {
        System.out.println("Executing: " + action);
        
        // BLOCK ALL ROOM MOVEMENT IF ENEMIES ARE PRESENT
        if (action.startsWith("MOVE_TO_") && hasEnemiesInCurrentRoom()) {
            System.out.println("!!! BLOCKED: Tried to move to another room but enemies are present!");
            System.out.println("!!! Staying in current room to handle enemies.");
            explore();
            return;
        }
        
        switch (action) {
            case "FLEE_FROM_DANGER":
            case "FLEE_FROM_LOSING_BATTLE":
            case "FLEE_FROM_ENEMIES":
                fleeFromEnemies();
                break;
                
            case "ATTACK_ENEMY":
                attack(getTotalAttackStat());
                System.out.println("Attacking enemy!");
                break;
                
            case "MOVE_TOWARD_FRONT_ITEM":
                moveForward();
                break;
                
            case "ENGAGE_NEAREST_ENEMY":
                GameSense.SenseData enemy = gameSense.findNearestEnemy();
                if (enemy.hasEnemy) {
                    moveToward(enemy.position);
                }
                break;
                
            case "COLLECT_ITEMS_IN_ROOM":
                GameSense.SenseData item = gameSense.findNearestItem();
                if (item.hasItem) {
                    moveToward(item.position);
                } else {
                    explore();
                }
                break;
                
            case "AVOID_EXIT_DOOR":
                // Turn away from exit door if we don't have key
                avoidExitDoor();
                break;
                
            case "EXPLORE_CURRENT_ROOM":
                explore();
                break;
                
            default:
                if (action.startsWith("TURN_TOWARD_ITEM:")) {
                    String direction = action.split(":")[1];
                    setDirection(direction);
                    System.out.println("Turned toward item");
                } else if (action.startsWith("MOVE_TO_")) {
                    String direction = action.split(":")[1];
                    moveThroughDoor(direction);
                } else {
                    explore();
                }
                break;
        }
    }
    
    @Override
    public void moveForward() {
        GameSense.SenseData front = gameSense.checkFront();
        
        // Prevent moving through exit door without key
        if (front.whatIsIt.equals("XitDoor") && !hasKey) {
            System.out.println("Cannot move through exit door without key!");
            return;
        }
        
        // Call parent moveForward if allowed
        super.moveForward();
    }
    
    private void avoidExitDoor() {
        // Turn in a random direction that's not toward the exit
        String[] directions = {"up", "down", "left", "right"};
        String currentDirection = getDirection();
        
        // Try to find a direction that doesn't face the exit
        for (int i = 0; i < 4; i++) {
            String testDir = directions[new Random().nextInt(4)];
            setDirection(testDir);
            GameSense.SenseData front = gameSense.checkFront();
            if (!front.whatIsIt.equals("XitDoor")) {
                System.out.println("Turned away from exit door to: " + testDir);
                return;
            }
        }
        
        // If all directions have exit doors (unlikely), just pick one randomly
        String newDir = directions[new Random().nextInt(4)];
        setDirection(newDir);
        System.out.println("Forced turn to: " + newDir);
    }
    
    private String findExitDoor(Map<String, String> levelSense) {
        List<String> availableDoors = gameSense.getAvailableDoors();
        
        for (String dir : availableDoors) {
            String hint = levelSense.get(dir);
            if (hint != null && hint.contains("exit")) {
                return dir;
            }
            
            // Also check if there's an exit door in this direction
            String currentDir = getDirection();
            setDirection(dir);
            GameSense.SenseData front = gameSense.checkFront();
            setDirection(currentDir);
            
            if (front.whatIsIt.equals("XitDoor")) {
                return dir;
            }
        }
        
        return "none";
    }
    
    private boolean hasEnemiesInCurrentRoom() {
        // Method 1: Check room entity list directly
        boolean roomHasEnemies = checkRoomEntitiesForEnemies();
        
        // Method 2: Check GameSense as backup
        boolean senseHasEnemies = gameSense.findNearestEnemy().hasEnemy;
        
        // Method 3: Scan all tiles in the room
        boolean scanHasEnemies = scanRoomTilesForEnemies();
        
        System.out.println("Enemy detection - Room: " + roomHasEnemies + ", Sense: " + senseHasEnemies + ", Scan: " + scanHasEnemies);
        
        // If ANY method detects enemies, return true
        return roomHasEnemies || senseHasEnemies || scanHasEnemies;
    }
    
    private boolean checkRoomEntitiesForEnemies() {
        try {
            Room currentRoom = getCurrentRoom();
            ArrayList<Entity> entities = currentRoom.getEntityList();
            
            for (Entity entity : entities) {
                if (entity instanceof EnemyAI) {
                    EnemyAI enemy = (EnemyAI) entity;
                    if (enemy.isAlive()) {
                        System.out.println("Found living enemy in entity list: " + enemy.getEnemyType());
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error checking room entities: " + e.getMessage());
        }
        return false;
    }
    
    private boolean scanRoomTilesForEnemies() {
        try {
            Room currentRoom = getCurrentRoom();
            int width = currentRoom.getWidth();
            int height = currentRoom.getHeight();
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Tile tile = currentRoom.checkTile(new Position(x, y));
                    if (tile.getEntityType().equals("enemy")) {
                        Entity entity = tile.getEntity();
                        if (entity instanceof EnemyAI) {
                            EnemyAI enemy = (EnemyAI) entity;
                            if (enemy.isAlive()) {
                                System.out.println("Found enemy on tile: (" + x + "," + y + ")");
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error scanning room tiles: " + e.getMessage());
        }
        return false;
    }
    
    private boolean shouldFleeFromDanger(GameSense.SenseData nearestEnemy) {
        if (!nearestEnemy.hasEnemy) return false;
        
        int enemyATK = estimateEnemyAttack(nearestEnemy);
        int dangerThreshold = 2 * enemyATK;
        
        return getHealth() < dangerThreshold && isEnemyNear(nearestEnemy);
    }
    
    private boolean shouldFleeFromLosingBattle(GameSense.SenseData nearestEnemy) {
        if (!nearestEnemy.hasEnemy) return false;
        
        Entity enemyEntity = getCurrentRoom().checkTile(nearestEnemy.position).getEntity();
        if (!(enemyEntity instanceof EnemyAI)) return false;
        
        EnemyAI enemy = (EnemyAI) enemyEntity;
        int enemyHP = enemy.getHealth();
        int playerATK = getTotalAttackStat();
        int playerHP = getHealth();
        int enemyATK = estimateEnemyAttack(nearestEnemy);
        
        if (playerATK == 0) playerATK = 1;
        if (enemyATK == 0) enemyATK = 1;
        
        double enemyTurnsToKillPlayer = (double) playerHP / enemyATK;
        double playerTurnsToKillEnemy = (double) enemyHP / playerATK;
        
        return playerTurnsToKillEnemy > enemyTurnsToKillPlayer;
    }
    
    private boolean shouldEngageEnemy(GameSense.SenseData nearestEnemy) {
        if (!nearestEnemy.hasEnemy) return false;
        
        Entity enemyEntity = getCurrentRoom().checkTile(nearestEnemy.position).getEntity();
        if (!(enemyEntity instanceof EnemyAI)) return false;
        
        EnemyAI enemy = (EnemyAI) enemyEntity;
        int enemyHP = enemy.getHealth();
        int playerATK = getTotalAttackStat();
        int playerHP = getHealth();
        int enemyATK = estimateEnemyAttack(nearestEnemy);
        
        if (playerATK == 0) playerATK = 1;
        if (enemyATK == 0) enemyATK = 1;
        
        double enemyTurnsToKillPlayer = (double) playerHP / enemyATK;
        double playerTurnsToKillEnemy = (double) enemyHP / playerATK;
        
        return playerTurnsToKillEnemy <= enemyTurnsToKillPlayer;
    }
    
    private int estimateEnemyAttack(GameSense.SenseData enemyData) {
        Entity enemyEntity = getCurrentRoom().checkTile(enemyData.position).getEntity();
        if (enemyEntity instanceof EnemyAI) {
            EnemyAI enemy = (EnemyAI) enemyEntity;
            String enemyType = enemy.getEnemyType();
            
            switch(enemyType) {
                case "Walker": return 10;
                case "Anchored": return 12;
                case "Brawler": return 18;
                default: return 10;
            }
        }
        return 10;
    }
    
    private boolean isEnemyNear(GameSense.SenseData enemyData) {
        if (!enemyData.hasEnemy) return false;
        
        Position playerPos = getPosition();
        Position enemyPos = enemyData.position;
        int distance = Math.abs(playerPos.getX() - enemyPos.getX()) + 
                      Math.abs(playerPos.getY() - enemyPos.getY());
        
        return distance <= 3;
    }
    
    private boolean hasItemsInCurrentRoom() {
        GameSense.SenseData item = gameSense.findNearestItem();
        return item.hasItem;
    }
    
    private boolean directionHasEnemy(String direction, Map<String, String> levelSense) {
        String hint = levelSense.get(direction);
        return hint != null && hint.contains("enemies");
    }
    
    private String findRoomWithHealthPotions(Map<String, String> levelSense) {
        List<String> availableDoors = gameSense.getAvailableDoors();
        
        for (String dir : availableDoors) {
            String hint = levelSense.get(dir);
            if (hint != null && hint.contains("items") && !hint.contains("enemies")) {
                return dir;
            }
        }
        
        for (String dir : availableDoors) {
            String hint = levelSense.get(dir);
            if (hint != null && hint.contains("items")) {
                return dir;
            }
        }
        
        return "none";
    }
    
    private String findRoomWithoutEnemies(Map<String, String> levelSense) {
        List<String> availableDoors = gameSense.getAvailableDoors();
        
        for (String dir : availableDoors) {
            String hint = levelSense.get(dir);
            if (hint != null && !hint.contains("enemies")) {
                return dir;
            }
        }
        
        return "none";
    }
    
    private String findRandomAvailableRoom(Map<String, String> levelSense) {
        List<String> availableDoors = gameSense.getAvailableDoors();
        
        if (!availableDoors.isEmpty()) {
            Random random = new Random();
            return availableDoors.get(random.nextInt(availableDoors.size()));
        }
        
        return "none";
    }
    
    private void moveThroughDoor(String direction) {
        // FINAL CHECK: Absolutely prevent leaving if enemies present
        if (hasEnemiesInCurrentRoom()) {
            System.out.println("!!! ABSOLUTELY BLOCKED: Enemies detected, cannot leave room!");
            System.out.println("!!! Forcing enemy engagement instead.");
            
            GameSense.SenseData enemy = gameSense.findNearestEnemy();
            if (enemy.hasEnemy) {
                moveToward(enemy.position);
            } else {
                explore();
            }
            return;
        }
        
        if (gameSense.isDoorInDirection(direction)) {
            setDirection(direction);
            
            GameSense.SenseData front = gameSense.checkFront();
            if (front.whatIsIt.equals("door") || front.whatIsIt.equals("XitDoor")) {
                // Additional check for exit door without key
                if (front.whatIsIt.equals("XitDoor") && !hasKey) {
                    System.out.println("Cannot move through exit door without key!");
                    return;
                }
                moveForward();
                System.out.println("Moving through door to " + direction);
            } else {
                ArrayList<Position> pathToDoor = gameSense.findPathToDoor(direction);
                if (!pathToDoor.isEmpty() && pathToDoor.size() > 1) {
                    Position nextStep = pathToDoor.get(1);
                    String moveDirection = gameSense.getDirectionTo(nextStep);
                    setDirection(moveDirection);
                    moveForward();
                }
            }
        }
    }
    
    private void moveToward(Position target) {
        ArrayList<Position> path = gameSense.findPathTo(target);
        if (path.size() > 1) {
            Position nextStep = path.get(1);
            String direction = gameSense.getDirectionTo(nextStep);
            setDirection(direction);
            
            GameSense.SenseData front = gameSense.checkFront();
            if (!front.whatIsIt.equals("enemy") && !front.whatIsIt.equals("wall")) {
                moveForward();
            }
        }
    }
    
    private void explore() {
        GameSense.SenseData front = gameSense.checkFront();
        
        if (front.whatIsIt.equals("wall") || front.whatIsIt.equals("enemy") || 
            (front.whatIsIt.equals("XitDoor") && !hasKey)) {
            String[] directions = {"up", "down", "left", "right"};
            String newDir = directions[new Random().nextInt(4)];
            rotate(newDir);
        } else {
            moveForward();
        }
    }
    
    private void fleeFromEnemies() {
        GameSense.SenseData enemy = gameSense.findNearestEnemy();
        if (enemy.hasEnemy) {
            Position enemyPos = enemy.position;
            Position myPos = getPosition();
            
            if (enemyPos.getX() > myPos.getX() && canMoveSafely("left")) {
                setDirection("left");
                moveForward();
            } else if (enemyPos.getX() < myPos.getX() && canMoveSafely("right")) {
                setDirection("right");
                moveForward();
            } else if (enemyPos.getY() > myPos.getY() && canMoveSafely("up")) {
                setDirection("up");
                moveForward();
            } else if (enemyPos.getY() < myPos.getY() && canMoveSafely("down")) {
                setDirection("down");
                moveForward();
            } else {
                explore();
            }
        } else {
            explore();
        }
    }
    
    private boolean canMoveSafely(String direction) {
        String currentDir = getDirection();
        setDirection(direction);
        GameSense.SenseData front = gameSense.checkFront();
        setDirection(currentDir);
        return !front.whatIsIt.equals("wall") && !front.whatIsIt.equals("enemy") &&
               !(front.whatIsIt.equals("XitDoor") && !hasKey);
    }

    private void pickupItem() {
        Tile currentTile = this.getCurrentRoom().checkTile(this.getPosition());
        if (currentTile.getEntityType() == "item") {
            Entity e = currentTile.getEntity();
            if (e instanceof Consumable) {
                this.inventory.add((Consumable) e);
                currentTile.clearTile();
                System.out.println("Picked up consumable");
            } else if (e instanceof Weapon) {
                Weapon weapon = (Weapon) e;
                if (equippedWeapon == null || weapon.isBetterThan(equippedWeapon)) {
                    equippedWeapon = weapon;
                    System.out.println("Equipped better weapon");
                } else {
                    System.out.println("Weapon not better than current");
                }
                currentTile.clearTile();
            } else if (e instanceof Key) {
                // Found the key (not implemented yet)
                hasKey = true;
                currentTile.clearTile();
                System.out.println("*** PICKED UP THE KEY! Now can exit the level. ***");
            }
        }
    }

    public int getTotalAttackStat(){
        
        if(this.equippedWeapon != null){
            return this.getBaseAttackStat() + this.equippedWeapon.getDamage();
        }else{
            return this.getBaseAttackStat();
        }

    }

    public void setEquippedWeapon(Weapon w){
        this.equippedWeapon = w;
    }

    public ArrayList<Item> getInventory(){
        return this.inventory;
    }

    public void pickupKey(){

        Tile t = this.getCurrentRoom().checkTile(this.getPosition());

        if(t.getEntityType() == "key"){
            t.clearTile();
            this.hasKey = true;
        }

    }

    public boolean hasKey(){
        return this.hasKey;
    }

    public void changeRoom(){

        Position originalPos = this.getPosition();
        Room originalRoom = this.getCurrentRoom();

        Position newPosition;
        Room newRoom;

        if(originalPos.getX() == 0){
            newPosition = new Position(originalRoom.getWidth() - 1, originalPos.getY());
            newRoom = originalRoom.getMap().getRoomList()[originalRoom.getRoomPos().getY()][originalRoom.getRoomPos().getX() - 1];
        }else if(originalPos.getX() == originalRoom.getWidth() - 1){
            newPosition = new Position(0, originalPos.getY());
            newRoom = originalRoom.getMap().getRoomList()[originalRoom.getRoomPos().getY()][originalRoom.getRoomPos().getX() + 1];
        }else if(originalPos.getY() == 0){
            newPosition = new Position(originalPos.getX(), originalRoom.getHeight() - 1);
            newRoom = originalRoom.getMap().getRoomList()[originalRoom.getRoomPos().getY() - 1][originalRoom.getRoomPos().getX()];
        }else{
            newPosition = new Position(originalPos.getX(), 0);
            newRoom = originalRoom.getMap().getRoomList()[originalRoom.getRoomPos().getY() + 1][originalRoom.getRoomPos().getX()];
        }

        //Set Departing Room to Peaceful;
        if(originalRoom.findLastDoor() != null){
            originalRoom.findLastDoor().setType("door");
        }
        originalRoom.makeAllEnemiesSleep();
        originalRoom.checkTile(originalPos).clearTile();

        //Place player in new room;
        newRoom.checkTile(newPosition).setEntity(this);
        newRoom.checkTile(newPosition).setType("lastDoor");
        this.setCurrentRoom(newRoom);
        this.setPosition(newPosition);

    }

}
