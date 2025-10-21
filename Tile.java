public class Tile {
    
    private Position pos;
    private String type;
    private String entityType;
    private Entity entity;

    public Tile(int x, int y, String type){
        this.pos = new Position(x, y);
        this.type = type;
    }

    public boolean isWalkable(){
        
        if(entityType != "empty"){
            return false;
        }
        
        switch(entityType){
            case "floor": return true;
        }
        return false;
    }

    //TODO
    public Entity spawnEntity(){
        Entity e = new Entity();
        return e;
    }

    public Position getPos(){
        return this.pos;
    }

    public String getType(){
        return this.type;
    }

}
