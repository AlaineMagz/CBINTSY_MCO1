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
        int x = random.nextInt(roomHeight);

        this.generateRooms(roomWidth, roomHeight);

    }

    public void generateRooms(int roomWidth, int roomHeight){

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
                this.roomList[y][x] = new Room(x, y, roomWidth + 2, roomHeight + 2, enemyCount, itemCount, doorDirs);

            }

        }

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

        this.roomList[0][0].displayRoom();

    }

}
