import java.util.ArrayList;
import java.util.Random;

public class GameMap {
    
    private int widthByRoom; 
    private int heightByRoom;
    private Room[][] roomList;
    private Room exitRoom;
    private Random random = new Random();

    public GameMap(int width, int height, int roomWidth, int roomHeight){

        this.widthByRoom = width;
        this.heightByRoom = height;

        //Pick exit room;
        int x;
        int y;
        int s = random.nextInt(4);

        if(s == 0){
            x = random.nextInt(width);
            y = 0;
        }else if(s == 1){
            x = random.nextInt(width);
            y = height - 1;
        }else if(s == 2){
            x = 0;
            y = random.nextInt(height);
        }else{
            x = width - 1;
            y = random.nextInt(height);
        }

        this.generateRooms(roomWidth, roomHeight, x, y, s);
        this.spawnKey(x, y, roomWidth, roomHeight);

    }

    public void generateRooms(int roomWidth, int roomHeight, int xExitPos, int yExitPos, int exitSide){

        int enemyCount;
        int itemCount;
        ArrayList<String> doorDirs = new ArrayList<>();

        this.roomList = new Room[this.heightByRoom][this.widthByRoom];

        for(int y = 0; y < this.heightByRoom; y++){

            for(int x = 0; x < this.widthByRoom; x++){

                if(y != 0){
                    doorDirs.add("up");
                }

                if(y < heightByRoom - 1){
                    doorDirs.add("down");
                }

                if(x != 0){
                    doorDirs.add("left");
                }

                if(x < widthByRoom - 1){
                    doorDirs.add("right");
                }

                enemyCount = random.nextInt(2);
                itemCount = random.nextInt(4);
                this.roomList[y][x] = new Room(this, x, y, roomWidth + 2, roomHeight + 2, enemyCount, itemCount, doorDirs, -1);
                doorDirs.clear();

            }

        }

        if(yExitPos != 0){
            doorDirs.add("up");
        }

        if(yExitPos < heightByRoom - 1){
            doorDirs.add("down");
        }

        if(xExitPos != 0){
            doorDirs.add("left");
        }

        if(xExitPos < widthByRoom - 1){
            doorDirs.add("right");
        }

        this.roomList[yExitPos][xExitPos] = new Room(this, xExitPos, yExitPos, roomWidth + 2, roomHeight + 2, 0, 0, doorDirs, exitSide);
        this.exitRoom = this.roomList[yExitPos][xExitPos];

    }

    public void displayMap(){

        for(int y = 0; y < this.heightByRoom; y++){

            for(int x = 0; x < this.widthByRoom; x++){

                boolean es = this.roomList[y][x].hasEnemy();
                boolean is = this.roomList[y][x].hasItem();

                if(es && is){
                    System.out.print("F");
                }else if(es){
                    System.out.print("E");
                }else if(is){
                    System.out.print("I");
                }else{
                    System.out.print("R");
                }

            }

            System.out.println("");

        }

    }

    public void spawnKey(int xExitPos, int yExitPos, int roomWidth, int roomHeight) {
    int roomX, roomY;
    int tileX, tileY;
    boolean running = true;
    int attempts = 0;
    final int MAX_ATTEMPTS = 100; // Prevent infinite loop

    while (running && attempts < MAX_ATTEMPTS) {
        attempts++;
        
        // Step 1: Pick a random room (excluding exit room)
        roomX = random.nextInt(this.widthByRoom);
        roomY = random.nextInt(this.heightByRoom);

        if (roomX != xExitPos || roomY != yExitPos) {
            
            // Step 2: Pick a random tile position within that room
            // Use roomWidth and roomHeight for bounds (subtract 2 for walls)
            tileX = random.nextInt(roomWidth - 4) + 2;  // Avoid walls
            tileY = random.nextInt(roomHeight - 4) + 2; // Avoid walls
            
            Room targetRoom = this.roomList[roomY][roomX];
            Tile targetTile = targetRoom.checkTile(new Position(tileX, tileY));
            
            // Step 3: Check if the tile is empty and walkable
            if (targetTile.getEntityType().equals("empty") && targetTile.isWalkable()) {
                targetRoom.getEntityList().add(targetTile.spawnEntity("key"));
                System.out.println("Key spawned in room (" + roomX + "," + roomY + ") at position (" + tileX + "," + tileY + ")");
                running = false;
                return;
            }
        }
    }
    
    // Fallback: if we can't find a spot after many attempts, place in a predetermined location
    System.out.println("Warning: Could not find optimal key location after " + MAX_ATTEMPTS + " attempts. Using fallback.");
    
    // Find any non-exit room and place key in center
    for (roomY = 0; roomY < this.heightByRoom; roomY++) {
        for (roomX = 0; roomX < this.widthByRoom; roomX++) {
            if (roomX != xExitPos || roomY != yExitPos) {
                tileX = roomWidth / 2;
                tileY = roomHeight / 2;
                Room targetRoom = this.roomList[roomY][roomX];
                Tile targetTile = targetRoom.checkTile(new Position(tileX, tileY));
                
                if (targetTile.isWalkable()) {
                    targetTile.spawnEntity("key");
                    System.out.println("Key placed in fallback location: room (" + roomX + "," + roomY + ")");
                    return;
                }
            }
        }
    }
    
    System.out.println("Error: Could not place key anywhere!");
}

    public Room getExitRoom(){
        return this.exitRoom;
    }

    public ArrayList<Entity> getAllEntities(){

        ArrayList<Entity> entities = new ArrayList<>();

        for(int y = 0; y < this.heightByRoom; y++){

            for(int x = 0; x < this.widthByRoom; x++){

                entities.addAll(this.roomList[y][x].getEntityList());

            }

        }

        return entities;

    }

    public Room[][] getRoomList(){
        return this.roomList;
    }

    public int getWidthByRoom(){
        return this.widthByRoom;
    }

    public int getHeightByRoom(){
        return this.heightByRoom;
    }

    public Room[][] getAllRooms() {
        return this.roomList;
    }

}
