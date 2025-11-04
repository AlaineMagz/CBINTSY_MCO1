public abstract class Item extends Entity{
    
    private String name;
    private String description;
    private String type;

    public Item(Room currentRoom, Position pos, String name, String description, String type){

        super(currentRoom, pos);
        this.name = name;
        this.description = description;
        this.type = type;
 
    }

    public String getItemName(){
        return this.name;
    }

    public String getItemDescription(){
        return this.description;
    }

    public String getItemType(){
        return this.type;
    }

}
