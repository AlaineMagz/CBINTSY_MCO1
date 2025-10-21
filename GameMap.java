public class GameMap {
    
    private int widthByRoom;
    private int heightByRoom;
    private Room[][] roomList;
    private Room exitRoom;

    public GameMap(int width, int height){

        this.widthByRoom = width;
        this.heightByRoom = height;

        this.generateRooms();

    }

    public void generateRooms(){

        this.roomList = new Room[this.heightByRoom][this.widthByRoom];

        for(int y = 0; y < this.heightByRoom; y++){

            for(int x = 0; x < this.widthByRoom; x++){

                this.roomList[y][x] = new Room(5, 5, 0, 0, null);

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
