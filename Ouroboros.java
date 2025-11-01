import java.util.ArrayList;

public class Ouroboros{

    private static GameMap map;
    private static ArrayList<Entity> entities;
    private static boolean gameRunning;
    
    public static void main(String[] args) {
            
        initializeGame();

    }

    public static void initializeGame(){

        //Sets gameRunning to True;
        gameRunning = true;

        //Generates Map;
        map = new GameMap(7, 5, 7, 7);
        map.displayMap();

    }

    public static void gameLoop(){

    }

    public static void processTurn(){

    }

    //TODO
    public static boolean checkWinCondition(){
        return false;
    }

    //TODO
    public static boolean checkLoseCondition(){
        return false;
    }

}