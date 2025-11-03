import java.util.ArrayList;
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
        // Print game info every turn
        gameSense.printGameInfo();
        
        // Get suggestion from game sense
        String action = gameSense.whatShouldIDo();
        System.out.println("AI Decision: " + action);
        
        // Simple action logic
        if (action.contains("Enemy in front")) {
            // Enemy directly in front = attack
            attack(getTotalAttackStat());
            System.out.println("ATTAKING THE ENEMY HIYAH");
        } 
        else if (action.contains("Enemy nearby")) {
            // Enemy nearby - decide whether to fight or flee
            if (this.getHealth() > this.getMaxHealth() * 0.6) {
                //  Healthy enough to fight - find and attack enemy
                GameSense.SenseData enemy = gameSense.findNearestEnemy();
                if (enemy.hasEnemy) {
                    moveToward(enemy.position);
                }
            } else {
                // Low health - run away
                fleeFromEnemies();
            }
        }
        else if (action.contains("health potion") || action.contains("Item nearby")) {
            // Find and move toward items
            GameSense.SenseData item = gameSense.findNearestItem();
            if (item.hasItem) {
                moveToward(item.position);
            }
        }
        else {
            // Default: explore
            explore();
        }
    }
    
    // move toward a specific position
     
    private void moveToward(Position target) {
        ArrayList<Position> path = gameSense.findPathTo(target);
        if (path.size() > 1) {
            Position nextStep = path.get(1);
            String direction = gameSense.getDirectionTo(nextStep);
            setDirection(direction);
            moveForward();
        }
    }
    
    //  exploration - move forward or turn if blocked
    private void explore() {
        GameSense.SenseData front = gameSense.checkFront();
        
        if (front.whatIsIt.equals("wall") || front.whatIsIt.equals("enemy")) {
            // Turn randomly if blocked
            String[] directions = {"up", "down", "left", "right"};
            String newDir = directions[new Random().nextInt(4)];
            rotate(newDir);
        } else {
            // Move forward
            moveForward();
        }
    }
    
    // flee behavior - move away from enemies

    private void fleeFromEnemies() {
        GameSense.SenseData enemy = gameSense.findNearestEnemy();
        if (enemy.hasEnemy) {
            Position enemyPos = enemy.position;
            Position myPos = getPosition();
            
            // Move in opposite direction of enemy
            if (enemyPos.getX() > myPos.getX()) {
                setDirection("left");
            } else if (enemyPos.getX() < myPos.getX()) {
                setDirection("right");
            } else if (enemyPos.getY() > myPos.getY()) {
                setDirection("up");
            } else {
                setDirection("down");
            }
            
            moveForward();
        } else {
            explore();
        }
    }

    private void pickupItem(){

        Tile currentTile = this.getCurrentRoom().checkTile(this.getPosition());

        if(currentTile.getEntityType() == "item"){

            Entity e = this.getCurrentRoom().checkTile(this.getPosition()).getEntity();

            if(e instanceof Consumable){

                this.inventory.add((Consumable) e);
                currentTile.clearTile();
                
            }else if(e instanceof Weapon){

                

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
