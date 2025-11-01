import java.util.ArrayList;
import java.util.Random;

public class Room {
    
    private Position roomPos;
    private int width;
    private int height;
    private Tile[][] tileMap;
    private ArrayList<String> doorDirection;
    private boolean hasEnemies;
    private boolean hasItems;
    private Random random = new Random();

    public Room(int x, int y, int width, int height, int enemyCount, int itemCount, ArrayList<String> doorDir){

        this.roomPos = new Position(x, y);
        this.width = width;
        this.height = height;

        this.generateTileMap();
        this.generateEntities(enemyCount, itemCount);

        this.hasEnemies = (enemyCount > 0) ? true : false;
        this.hasItems = (itemCount > 0) ? true : false;

        this.doorDirection = doorDir;
        this.setDoors();

    }

    public void generateTileMap(){

        this.tileMap = new Tile[this.height][this.width];

        for(int y = 0; y < this.height; y++){

            for(int x = 0; x < this.width; x++){

                if(x == 0 || x == width - 1 || y == 0 || y == height - 1){
                    this.tileMap[y][x] = new Tile(x, y, "wall");
                }else{
                    this.tileMap[y][x] = new Tile(x, y, "floor");
                }

            }

        }

    }

    public void setDoors(){

        if(doorDirection.contains("up")){
            int x = width/2;
            this.tileMap[0][x] = new Tile(x, 0, "door");
        }

        if(doorDirection.contains("down")){
            int x = width/2;
            this.tileMap[height - 1][x] = new Tile(x, height - 1, "door");
        }

        if(doorDirection.contains("left")){
            int y = height/2;
            this.tileMap[y][0] = new Tile(0, y, "door");
        }

        if(doorDirection.contains("right")){
            int y = height/2;
            this.tileMap[y][width - 1] = new Tile(width - 1, y, "door");
        }

    }

    //TODO
    public void generateEntities(int enemyCount, int itemCount){

        for(int i = 0; i < enemyCount; i++){

            int x = random.nextInt(width - 4) + 2;
            int y = random.nextInt(height - 4) + 2;

            if(this.tileMap[y][x].isWalkable()){
                this.tileMap[y][x].spawnEntity("enemy");
            }else{
                i--;
            }

        }

        for(int i = 0; i < itemCount; i++){

            int x = random.nextInt(width - 4) + 2;
            int y = random.nextInt(height - 4) + 2;

            if(this.tileMap[y][x].isWalkable()){
                this.tileMap[y][x].spawnEntity("item");
            }else{
                i--;
            }

        }

    }

    public void displayRoom(){

        char c;
        System.out.println("");

        for(int y = 0; y < this.height; y++){

            for(int x = 0; x < this.width; x++){

                if(this.tileMap[y][x].getEntityType() == "empty"){
                    c = this.tileMap[y][x].getType().toCharArray()[0];
                }else{
                    c = this.tileMap[y][x].getEntityType().toCharArray()[0];
                }
                
                System.out.print(c);

            }

            System.out.println("");

        }

    }

    public boolean hasEnemy(){
        return this.hasEnemies;
    }

    public boolean hasItem(){
        return this.hasItems;
    }

    public Position getRoomPos(){
        return this.roomPos;
    }

}
