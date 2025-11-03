import java.util.ArrayList;

public class Ouroboros{

    private static GameMap map;
    private static ArrayList<Entity> entities;
    private static PlayerAI player;
    private static boolean gameRunning;
    
    public static void main(String[] args) {
            
        initializeGame();
        gameLoop();

    }

    public static void initializeGame(){

        //Sets gameRunning to True;
        gameRunning = true;

        //Generates Map;
        map = new GameMap(7, 5, 7, 7);
        entities = map.getAllEntities();
        player = ((PlayerAI) map.getExitRoom().checkTile(new Position(1, 1)).spawnEntity("player"));
        entities.add(map.getExitRoom().checkTile(new Position(1, 3)).spawnEntity("enemy"));
        entities.add(map.getExitRoom().checkTile(new Position(3, 1)).spawnEntity("enemy"));
        map.displayMap();

    }

    public static void gameLoop(){
        System.out.println("Game started! Entering main game loop...");
        
        while (gameRunning) {
            processTurn();
            
            // Check win/lose conditions
            if (checkWinCondition()) {
                System.out.println("Congratulations! You found the exit! You win!!");
                gameRunning = false;
                break;
            }
            
            if (checkLoseCondition()) {
                System.out.println("Game Over!");
                gameRunning = false;
                break;
            }
            
            // Small delay to make turns readable
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("Game ended.");
    }

    public static void processTurn(){
        System.out.println("\n--- New Turn ---");
        
        // Player takes turn
        player.takeTurn();
        player.replenishActionPoints();

        checkEnemyDeath();
        
        // Display current map state
        map.displayMap();
    }

    public static void checkEnemyDeath(){
        ArrayList<Entity> deadEntities = new ArrayList<>();

        // make new array list called dead enemies
        for (Entity entity : entities) {
            if (entity instanceof EnemyAI) {
                EnemyAI enemy = (EnemyAI) entity;
                
                // Check if enemy is dead
                if (!enemy.isAlive()) {
                    System.out.println("Enemy defeated at position (" + enemy.getPosition().getCoordinates());
                    
                    // Get the tile where the enemy is located
                    Tile enemyTile = enemy.getCurrentRoom().checkTile(enemy.getPosition());
                    
                    // Clear the tile (remove the enemy entity)
                    enemyTile.clearTile();
                    deadEntities.add(enemy);
                }
            }
        }
        for(Entity entity : deadEntities){
            entities.remove(entity);
        }
    }
    /*for each entity, If entity.getEntityType is equal to enemy then If entity.isalive = false uhh type uhh entity.getroom.checkTile(enemy.getPosistion). and your supposed to remove
     * the entity tile by deleting the entity. 
    */

    public static boolean checkWinCondition(){
        return player.getCurrentRoom().checkTile(player.getPosition()).getType() == "XitDoor";
    }
 
    public static boolean checkLoseCondition(){
        return !player.isAlive();
    }

}