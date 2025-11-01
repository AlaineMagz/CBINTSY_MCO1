public class Tile {
    
    private Position pos;
    private String type;
    private String entityType;
    private Entity entity;

    public Tile(int x, int y, String type){
        this.pos = new Position(x, y);
        this.type = type;
        this.entityType = "empty";
    }

    public boolean isWalkable(){
        
        if(entityType != "empty"){
            return false;
        }
        
        switch(type){
            case "floor": return true;
        }
        return false;
    }

    //TODO
    public Entity spawnEntity(String type){
        this.entityType = type;
        Entity e;

        switch(type){

            default: e = new Consumable(null, this.pos, type, type, 0, 0);

        }

        return e;
    }

    public Position getPos(){
        return this.pos;
    }

    public String getType(){
        return this.type;
    }

    public String getEntityType(){
        return this.entityType;
    }

    public Entity getEntity(){
        return this.entity;
    }

}
