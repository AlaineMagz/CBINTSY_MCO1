import java.util.ArrayList;
import java.util.Random;

public class Room {
    
    private GameMap parentMap;
    private Position roomPos;
    private int width;
    private int height;
    private Tile[][] tileMap;
    private ArrayList<String> doorDirection;
    private ArrayList<Entity> entityList;
    private boolean hasEnemies;
    private boolean hasItems;
    private int exitSide;
    private Random random = new Random();

    public Room(GameMap map, int x, int y, int width, int height, int enemyCount, int itemCount, ArrayList<String> doorDir, int exitSide){

        this.parentMap = map;
        this.roomPos = new Position(x, y);
        this.width = width;
        this.height = height;

        this.generateTileMap();

        this.entityList = new ArrayList<>();
        this.generateEntities(enemyCount, itemCount);

        this.hasEnemies = (enemyCount > 0) ? true : false;
        this.hasItems = (itemCount > 0) ? true : false;

        this.doorDirection = doorDir;
        this.exitSide = exitSide;
        this.setDoors();

    }

    public void generateTileMap(){

        this.tileMap = new Tile[this.height][this.width];

        for(int y = 0; y < this.height; y++){

            for(int x = 0; x < this.width; x++){

                if(x == 0 || x == width - 1 || y == 0 || y == height - 1){
                    this.tileMap[y][x] = new Tile(this, x, y, "wall");
                }else{
                    this.tileMap[y][x] = new Tile(this, x, y, "floor");
                }

            }

        }

    }

    public void setDoors(){

        if(doorDirection.contains("up")){
            int x = width/2;
            this.tileMap[0][x] = new Tile(this, x, 0, "door");
        }

        if(doorDirection.contains("down")){
            int x = width/2;
            this.tileMap[height - 1][x] = new Tile(this, x, height - 1, "door");
        }

        if(doorDirection.contains("left")){
            int y = height/2;
            this.tileMap[y][0] = new Tile(this, 0, y, "door");
        }

        if(doorDirection.contains("right")){
            int y = height/2;
            this.tileMap[y][width - 1] = new Tile(this, width - 1, y, "door");
        }

        if(this.exitSide == 0){
            int x = width/2;
            this.tileMap[0][x] = new Tile(this, x, 0, "XitDoor");
        }

        if(this.exitSide == 1){
            int x = width/2;
            this.tileMap[height - 1][x] = new Tile(this, x, height - 1, "XitDoor");
        }

        if(this.exitSide == 2){
            int y = height/2;
            this.tileMap[y][0] = new Tile(this, 0, y, "XitDoor");
        }

        if(this.exitSide == 3){
            int y = height/2;
            this.tileMap[y][width - 1] = new Tile(this, width - 1, y, "XitDoor");
        }

    }

    public void generateEntities(int enemyCount, int itemCount){

        for(int i = 0; i < enemyCount; i++){

            int x = random.nextInt(width - 4) + 2;
            int y = random.nextInt(height - 4) + 2;

            if(this.tileMap[y][x].isWalkable()){
                this.entityList.add(this.tileMap[y][x].spawnEntity("enemy"));
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

    public Tile findLastDoor(){

        for(int y = 0; y < this.height; y++){

            for(int x = 0; x < this.width; x++){

                if(this.tileMap[y][x].getType() == "lastDoor"){
                    return this.tileMap[y][x];
                }

            }

        }

        return null;

    }

    public void makeAllEnemiesSleep(){

        for(Entity e : this.entityList){

            if(e instanceof EnemyAI){
                ((EnemyAI)e).setIsHostile(false);
            }

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

    public ArrayList<Entity> getEntityList(){
        return this.entityList;
    }

    public Tile checkTile(Position pos){

        return this.tileMap[pos.getY()][pos.getX()];

    }

    public int getWidth(){
        return this.width;
    }

    public int getHeight(){
        return this.height;
    }

    public GameMap getMap(){
        return this.parentMap;
    }

    public void refreshHasEntities(){

        this.hasEnemies = false;
        this.hasItems = false;

        for(Entity e : this.entityList){

            if(e instanceof EnemyAI){
                this.hasEnemies = true;
            }

            if(e instanceof Item){
                this.hasItems = true;
            }

        }

    }

}
