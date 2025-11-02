import java.util.ArrayList;

public class Ouroboros{

    private static GameMap map;
    private static ArrayList<Entity> entities;
    private static PlayerAI player;
    private static boolean gameRunning;
    
    public static void main(String[] args) {
            
        initializeGame();

    }

    public static void initializeGame(){

        //Sets gameRunning to True;
        gameRunning = true;

        //Generates Map;
        map = new GameMap(7, 5, 7, 7);
        entities = map.getAllEntities();
        map.displayMap();

    }

    public static void gameLoop(){

    }

    public static void processTurn(){

    }

    public static boolean checkWinCondition(){
        return player.getCurrentRoom().checkTile(player.getPosition()).getType() == "XitDoor";
    }

    public static boolean checkLoseCondition(){
        return !player.isAlive();
    }

}