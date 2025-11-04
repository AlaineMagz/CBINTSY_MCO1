public abstract class Entity {
    
    private Room currentRoom; 
    private Position position;

    public Entity(Room currentRoom, Position pos){

        this.currentRoom = currentRoom;
        this.position = pos;

    }

    public Room getCurrentRoom(){
        return this.currentRoom;
    }

    public void setCurrentRoom(Room newRoom){
        this.currentRoom = newRoom;
    }

    public Position getPosition(){
        return this.position;
    }

    public void setPosition(Position newPos){
        this.position = newPos;
    }

}
