public class Room {
    
    private int width;
    private int height;
    private Tile[][] tileMap;
    private String[] doorDirection;
    private boolean hasEnemies;
    private boolean hasItems;

    public Room(int width, int height, int enemyCount, int itemCount, String[] doorDir){

        this.width = width;
        this.height = height;

        this.generateTileMap();

        this.hasEnemies = false;
        this.hasItems = false;

    }

    public void generateTileMap(){

        this.tileMap = new Tile[this.height][this.width];

        for(int y = 0; y < this.height; y++){

            for(int x = 0; x < this.width; x++){

                this.tileMap[y][x] = new Tile(x, y, "floor");

            }

        }

    }

    //TODO
    public void generateEntities(){

    }

    public void displayRoom(){

        for(int y = 0; y < this.height; y++){

            for(int x = 0; x < this.width; x++){

                char c = this.tileMap[y][x].getType().toCharArray()[0];
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

}
