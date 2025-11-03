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

                enemyCount = random.nextInt(3);
                itemCount = random.nextInt(3);
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

        this.roomList[yExitPos][xExitPos] = new Room(this, xExitPos, yExitPos, roomWidth, roomHeight, 0, 0, doorDirs, exitSide);
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

    public void spawnKey(int xExitPos, int yExitPos, int roomWidth, int roomHeight){

        int x;
        int y;
        boolean running = true;

        while(running){

            x = random.nextInt(this.widthByRoom);
            y = random.nextInt(this.heightByRoom);

            if(x != xExitPos && y != yExitPos){

                x = random.nextInt(roomWidth - 4) + 2;
                y = random.nextInt(roomHeight - 4) + 2;
                
                if(this.roomList[y][x].checkTile(new Position(x, y)).getEntityType() == "empty"){
                    this.roomList[y][x].checkTile(new Position(x, y)).spawnEntity("key");
                    running = false;
                }
                
            }

        }

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

}
