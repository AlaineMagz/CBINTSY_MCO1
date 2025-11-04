import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    }
    
   private String decideAction() {
    GameSense.SenseData front = gameSense.checkFront();
    GameSense.SenseData nearestEnemy = gameSense.findNearestEnemy();
    boolean enemiesInRoom = hasEnemiesInCurrentRoom();

    System.out.println("=== DECISION INFO ===");
    System.out.println("Enemies in room: " + enemiesInRoom);
    System.out.println("Front: " + front.whatIsIt);
    System.out.println("Health: " + getHealth() + "/" + getMaxHealth());
    System.out.println("Has Key: " + hasKey);
    System.out.println("====================");

    // NEVER leave room if enemies are present
    if (enemiesInRoom) {
        System.out.println(">>> ENEMIES IN ROOM - CANNOT LEAVE <<<");
        return handleEnemyCombat(front, nearestEnemy);
    }

    return handleSafeMovement(front, nearestEnemy);

}


    private String handleEnemyCombat(GameSense.SenseData front, GameSense.SenseData nearestEnemy) {
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

    private String handleSafeMovement(GameSense.SenseData front, GameSense.SenseData nearestEnemy) {
        Map<String, String> levelSense = gameSense.getLevelSense();
        
        if (front.hasEnemy) {
            return "ATTACK_ENEMY";
        }
        
        // Check if front is exit door and we don't have key
        if ("XitDoor".equals(front.whatIsIt) && !hasKey) {
            System.out.println("Exit door detected but no key - avoiding exit");
            return "AVOID_EXIT_DOOR";
        }
        
        if (front.hasItem) {
            return "MOVE_TOWARD_FRONT_ITEM";
        }
        
        if (hasItemsInCurrentRoom()) {
            return "COLLECT_ITEMS_IN_ROOM";
        }
        
        // Room movement decisions (only if no enemies)
        if (getHealth() < getMaxHealth() * 0.4) {
            String healthRoomDir = findRoomWithHealthPotions(levelSense);
            if (!"none".equals(healthRoomDir)) {
                return "MOVE_TO_HEALTH_ROOM:" + healthRoomDir;
            }
        }
        
        // Only consider exit if we have the key
        if (hasKey) {
            String exitDirection = findExitDoor(levelSense);
            if (!"none".equals(exitDirection)) {
                return "MOVE_TO_EXIT:" + exitDirection;
            }
        }
        
        if (!hasKey && this.getCurrentRoom().hasItem()) {
            GameSense.SenseData nearestKey = gameSense.findNearestItemOfType("Key");
            if (nearestKey != null && nearestKey.hasItem && nearestKey.position != null) {
                return "MOVE_TO_KEY";
            } else {
                System.out.println("No visible key, exploring to search for one...");
                return "SEARCH_FOR_KEY";
            }
        }

        return "MOVE_TO_NEW_ROOM";
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

            case "MOVE_TO_KEY":
            GameSense.SenseData keyData = gameSense.findNearestItemOfType("Key");
                if (keyData != null && keyData.hasItem && keyData.position != null) {
                    moveToward(keyData.position);
                    System.out.println("Moving toward the key...");
         }   else {
            explore();
             }
            break;

            case "SEARCH_FOR_KEY":
                explore();
                break;
                
            case "MOVE_TOWARD_FRONT_ITEM":
                moveForward();
                break;
                
            case "ENGAGE_NEAREST_ENEMY":
                GameSense.SenseData enemy = gameSense.findNearestEnemy();
                if (enemy != null && enemy.hasEnemy && enemy.position != null) {
                    moveToward(enemy.position);
                }
                break;
                
            case "COLLECT_ITEMS_IN_ROOM":
                GameSense.SenseData item = gameSense.findNearestItem();
                System.out.println(item.position.getCoordinates());
                if (item != null && item.hasItem && item.position != null) {
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
                    String[] parts = action.split(":", 2);
                    if (parts.length == 2) {
                        String direction = parts[1];
                        setDirection(direction);
                        System.out.println("Turned toward item");
                    }
                } else if (action.startsWith("MOVE_TO_")) {
                    String[] parts = action.split(":", 2);
                    if (parts.length == 2) {
                        String direction = parts[1];
                        moveThroughDoor(direction);
                    } else {
                        // fallback: pick a random available door
                        String dir = findRandomAvailableRoom(gameSense.getLevelSense());
                        if (!"none".equals(dir)) moveThroughDoor(dir);
                        else explore();
                    }
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
        if ("XitDoor".equals(front.whatIsIt) && !hasKey) {
            System.out.println("Cannot move through exit door without key!");
            return;
        }

        if(this.getCurrentRoom().checkTile(this.getPosition().getAdjacent(this.getDirection())).getEntityType() == "item"){
            this.pickupItem(this.getPosition().getAdjacent(this.getDirection()));
        }else if(this.getCurrentRoom().checkTile(this.getPosition().getAdjacent(this.getDirection())).getEntityType() == "key"){
            this.pickupKey(this.getPosition().getAdjacent(this.getDirection()));
        }
        
        // Call parent moveForward if allowed
        super.moveForward();
    }
    
    private void avoidExitDoor() {
        // Turn in a random direction that's not toward the exit
        String[] directions = {"up", "down", "left", "right"};
        
        // Try to find a direction that doesn't face the exit
        for (int i = 0; i < 6; i++) { // try a few times
            String testDir = directions[new Random().nextInt(directions.length)];
            setDirection(testDir);
            GameSense.SenseData front = gameSense.checkFront();
            if (!"XitDoor".equals(front.whatIsIt)) {
                System.out.println("Turned away from exit door to: " + testDir);
                moveForward();
                return;
            }
        }
        
        // If all directions have exit doors (very unlikely), just pick one randomly
        String newDir = directions[new Random().nextInt(directions.length)];
        setDirection(newDir);
        System.out.println("Forced turn to: " + newDir);

        moveForward();
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
            
            if ("XitDoor".equals(front.whatIsIt)) {
                return dir;
            }
        }
        
        return "none";
    }
    
    private boolean hasEnemiesInCurrentRoom() {
        Room currentRoom = getCurrentRoom();
        
        // Method 1: Direct tile scanning - most reliable
        if (scanRoomTilesForEnemies()) {
            return true;
        }
        
        // Method 2: Check entity list as backup
        return checkRoomEntitiesForEnemies();
    }

    private boolean scanRoomTilesForEnemies() {
        try {
            Room currentRoom = getCurrentRoom();
            int width = currentRoom.getWidth();
            int height = currentRoom.getHeight();
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Tile tile = currentRoom.checkTile(new Position(x, y));
                    if ("enemy".equals(tile.getEntityType())) {
                        Entity entity = tile.getEntity();
                        if (entity instanceof EnemyAI) {
                            EnemyAI enemy = (EnemyAI) entity;
                            if (enemy.isAlive() && enemy.isHostile()) {
                                System.out.println("Enemy detected on tile: (" + x + "," + y + ") - " + enemy.getEnemyType());
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

    private boolean checkRoomEntitiesForEnemies() {
        try {
            Room currentRoom = getCurrentRoom();
            ArrayList<Entity> entities = currentRoom.getEntityList();
            
            for (Entity entity : entities) {
                if (entity instanceof EnemyAI) {
                    EnemyAI enemy = (EnemyAI) entity;
                    if (enemy.isAlive() && enemy.isHostile()) {
                        System.out.println("Enemy found in entity list: " + enemy.getEnemyType());
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error checking room entities: " + e.getMessage());
        }
        return false;
    }
    
    private boolean shouldFleeFromDanger(GameSense.SenseData nearestEnemy) {
        if (nearestEnemy == null || !nearestEnemy.hasEnemy) return false;
        
        int enemyATK = estimateEnemyAttack(nearestEnemy);
        int dangerThreshold = 2 * enemyATK;
        
        return getHealth() < dangerThreshold && isEnemyNear(nearestEnemy);
    }
    
    private boolean shouldFleeFromLosingBattle(GameSense.SenseData nearestEnemy) {
        if (nearestEnemy == null || !nearestEnemy.hasEnemy) return false;
        
        if (nearestEnemy.position == null) return false;
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
        if (nearestEnemy == null || !nearestEnemy.hasEnemy) return false;
        if (nearestEnemy.position == null) return false;
        
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
        if (enemyData == null || enemyData.position == null) return 10;
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
        if (enemyData == null || !enemyData.hasEnemy || enemyData.position == null) return false;
        
        Position playerPos = getPosition();
        Position enemyPos = enemyData.position;
        int distance = Math.abs(playerPos.getX() - enemyPos.getX()) + 
                      Math.abs(playerPos.getY() - enemyPos.getY());
        
        return distance <= 3;
    }
    
    private boolean hasItemsInCurrentRoom() {
        GameSense.SenseData item = gameSense.findNearestItem();
        return item != null && item.hasItem;
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
            if (enemy != null && enemy.hasEnemy && enemy.position != null) {
                moveToward(enemy.position);
            } else {
                explore();
            }
            return;
        }
        
        if (gameSense.isDoorInDirection(direction)) {
            setDirection(direction);
            
            GameSense.SenseData front = gameSense.checkFront();
            if ("door".equals(front.whatIsIt) || "XitDoor".equals(front.whatIsIt)) {
                // Additional check for exit door without key
                if ("XitDoor".equals(front.whatIsIt) && !hasKey) {
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
        if (target == null) return;
        ArrayList<Position> path = gameSense.findPathTo(target);
        if (path.size() > 1) {
            Position nextStep = path.get(1);
            String direction = gameSense.getDirectionTo(nextStep);
            setDirection(direction);
            System.out.println("GOING TO " + nextStep.getCoordinates() + "! TURNING TO " + direction);
            
            GameSense.SenseData front = gameSense.checkFront();
            if (!"enemy".equals(front.whatIsIt) && !"wall".equals(front.whatIsIt)) {
                moveForward();
            }
        }else{
            System.out.println("PATH SIZE IS 1");
        }
    }
    
    private void explore() {
    GameSense.SenseData front = gameSense.checkFront();
    
    // If front is clear, move forward
    if (!"wall".equals(front.whatIsIt) && !"enemy".equals(front.whatIsIt) && 
        !("XitDoor".equals(front.whatIsIt) && !hasKey)) {
        moveForward();
        return;
    }
    
    // If blocked, find a walkable direction and move there immediately
    String[] directions = {"up", "down", "left", "right"};
    List<String> shuffledDirs = Arrays.asList(directions);
    Collections.shuffle(shuffledDirs); // Randomize direction selection
    
    for (String dir : shuffledDirs) {
        String currentDir = getDirection();
        setDirection(dir);
        GameSense.SenseData checkFront = gameSense.checkFront();
        
        if (!"wall".equals(checkFront.whatIsIt) && !"enemy".equals(checkFront.whatIsIt) &&
            !("XitDoor".equals(checkFront.whatIsIt) && !hasKey)) {
            moveForward(); // Move in the first valid direction found
            return;
        }
        setDirection(currentDir); // Restore original direction if this one doesn't work
    }
    
    // If completely stuck, just rotate randomly
    rotate(directions[new Random().nextInt(directions.length)]);
}
    
    private void fleeFromEnemies() {
        GameSense.SenseData enemy = gameSense.findNearestEnemy();
        if (enemy != null && enemy.hasEnemy && enemy.position != null) {
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
        return !"wall".equals(front.whatIsIt) && !"enemy".equals(front.whatIsIt) &&
               !("XitDoor".equals(front.whatIsIt) && !hasKey);
    }

    private void pickupItem(Position pos) {
    Tile currentTile = this.getCurrentRoom().checkTile(pos);
    if ("item".equals(currentTile.getEntityType())) {
        Entity e = currentTile.getEntity();
        if (e instanceof Consumable) {
            Consumable consumable = (Consumable) e;
            // Check if this is the key by name
            if ("Key".equals(consumable.getItemName())) {
                hasKey = true;
                currentTile.clearTile();
                System.out.println("*** PICKED UP THE KEY! Now can exit the level. ***");
                System.out.println("*** INVENTORY: Key added - can now open exit doors ***");
            } else {
                // Apply consumable effects and show detailed info
                int oldHealth = getHealth();
                int oldAttack = getBaseAttackStat();
                
                consumable.useConsumable(this);
                this.inventory.add(consumable);
                currentTile.clearTile();
                
                // Show detailed pickup information
                System.out.println("==========================================");
                System.out.println("PICKED UP: " + consumable.getItemName());
                System.out.println("DESCRIPTION: " + consumable.getItemDescription());
                
                // Show health change if any
                if (oldHealth != getHealth()) {
                    System.out.println("HEALTH: " + oldHealth + " → " + getHealth() + " (+" + (getHealth() - oldHealth) + ")");
                }
                
                // Show attack change if any  
                if (oldAttack != getBaseAttackStat()) {
                    System.out.println("ATTACK: " + oldAttack + " → " + getBaseAttackStat() + " (+" + (getBaseAttackStat() - oldAttack) + ")");
                }
                
                System.out.println("INVENTORY: " + inventory.size() + " items total");
                System.out.println("==========================================");
            }
        } else if (e instanceof Weapon) {
            Weapon weapon = (Weapon) e;
            int oldDamage = (equippedWeapon != null) ? equippedWeapon.getDamage() : 0;
            int newDamage = weapon.getDamage();
            
            if (equippedWeapon == null || weapon.isBetterThan(equippedWeapon)) {
                equippedWeapon = weapon;
                currentTile.clearTile();
                
                System.out.println("==========================================");
                System.out.println("EQUIPPED NEW WEAPON: " + weapon.getItemName());
                System.out.println("DESCRIPTION: " + weapon.getItemDescription());
                System.out.println("DAMAGE: " + oldDamage + " → " + newDamage + " (+" + (newDamage - oldDamage) + ")");
                System.out.println("TOTAL ATTACK: " + getTotalAttackStat() + " (Base: " + getBaseAttackStat() + " + Weapon: " + newDamage + ")");
                System.out.println("==========================================");
            } else {
                System.out.println("==========================================");
                System.out.println("FOUND WEAPON: " + weapon.getItemName() + " (Damage: " + newDamage + ")");
                System.out.println("CURRENT WEAPON: " + equippedWeapon.getItemName() + " (Damage: " + oldDamage + ")");
                System.out.println("DECISION: Not better than current weapon - leaving it");
                System.out.println("==========================================");
                // Don't clear tile - leave weapon for potential future use
            }
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

    public void pickupKey(Position pos){
        Tile t = this.getCurrentRoom().checkTile(pos);
        if ("key".equals(t.getEntityType())) {
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
