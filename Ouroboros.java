import java.util.ArrayList;
import java.util.Scanner;

public class Ouroboros{

    private static GameMap map;
    private static ArrayList<Entity> entities;
    private static PlayerAI player;
    private static boolean gameRunning;
    private static String chosenSpeed;
    private static String chosenAlgo;
    private static Scanner scanner;
    
    public static void main(String[] args) {
            
        initializeGame();
        gameLoop();

        scanner.close();

    }

    public static void initializeGame(){

        //Sets gameRunning to True;
        gameRunning = true;

        scanner = new Scanner(System.in);

        //Prompts for which search algo to use;
        System.out.println("Please select your simulation mode:");
        System.out.println("[1] Fast");
        System.out.println("[2 / Any Other Number] Slow");
        System.out.print("Please enter the number of your choice: ");
        int x = scanner.nextInt();

        if(x == 1){
            chosenSpeed = "fast";
        }else{
            chosenSpeed = "slow";
        }
        
        System.out.println("Please select your preffered search algorithm:");
        System.out.println("[1] DFS");
        System.out.println("[2 / Any Other Number] BFS");
        System.out.print("Please enter the number of your choice: ");
        x = scanner.nextInt();
        scanner.nextLine();

        if(x == 1){
            chosenAlgo = "dfs";
        }else{
            chosenAlgo = "bfs";
        }

        //Generates Map;
        map = new GameMap(7, 5, 7, 7);
        System.out.println("MAP GENERATED");
        entities = map.getAllEntities();
        System.out.println("ENTITIES GENERATED");
        player = ((PlayerAI) map.getExitRoom().checkTile(new Position(4, 4)).spawnEntity("player"));
        System.out.println("PLAYER SPAWNED");

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
            
            if(chosenSpeed == "fast"){

                // Small delay to make turns readable
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }else{

                System.out.println();
                System.out.print("Press enter to continue...");
                scanner.nextLine();

            }

        }
        
        System.out.println("Game ended.");
    }

    public static void processTurn(){
        System.out.println("\n--- New Turn ---");
        
        // Display current map state
        map.displayMap();
        player.getCurrentRoom().displayRoom();

        // Player takes turn
        player.takeTurn();
        player.replenishActionPoints();
        
        checkEnemyDeath();

        checkDoors();
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

    public static void checkDoors(){
        if(player.getCurrentRoom().checkTile(player.getPosition()).getType() == "door"){
            player.changeRoom();
        }
    }

    public static boolean checkWinCondition(){
        return player.getCurrentRoom().checkTile(player.getPosition()).getType() == "XitDoor";
    }
 
    public static boolean checkLoseCondition(){
        return !player.isAlive();
    }

}