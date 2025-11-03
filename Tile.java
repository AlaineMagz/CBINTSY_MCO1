import java.util.Random;

public class Tile {
    
    private Room room;
    private Position pos;
    private String type;
    private String entityType;
    private Entity entity;
    private Random random;

    public Tile(Room room, int x, int y, String type){
        this.room = room;
        this.pos = new Position(x, y);
        this.type = type;
        this.entityType = "empty";
        this.random = new Random();
    }

    public boolean isWalkable(){
        
        if(this.entityType != "empty" && this.entityType != "item"){
            return false;
        }
        
        switch(type){
            case "floor": return true;
            case "item": return true;
        }
        return false;
    }

    public Entity spawnEntity(String type){
        this.entityType = type;
        int r;
        Entity e = new EnemyAI(room, pos, 1, 1, 0, 1, 1, "Error", "immobile");

        if(type == "enemy"){

            r = random.nextInt(3);

            if(r == 0){
                e = new EnemyAI(room, pos, 15, 15, 10, 1, 1, "Walker", "mobile");
            }else if(r == 1){
                e = new EnemyAI(room, pos, 25, 25, 12, 1, 1, "Anchored", "immobile");
            }else if(r == 2){
                e = new EnemyAI(room, pos, 20, 20, 18, 1, 1, "Brawler", "mobile");
            }

        }else if(type == "item"){

            r = random.nextInt(5);

            if(r <= 3){

                r = random.nextInt(3);

                if(r < 2){
                    e = new Consumable(room, pos, "Health Potion", "Restores 15 HP.", 15, 0);
                }else if(r == 2){
                    e = new Consumable(room, pos, "Strength Potion", "Permanently increases attack stat by 1.", 0, 1);
                }

            }else{

                r = random.nextInt(6);

                if(r == 0){
                    e = new Weapon(room, pos, "Dagger", "A short blade.", 4);
                }else if(r == 1){
                    e = new Weapon(room, pos, "Short Sword", "An uninspiring sword.", 6);
                }else if(r == 2){
                    e = new Weapon(room, pos, "Whip", "Ancient version of the slipper.", 2);
                }else if(r == 3){
                    e = new Weapon(room, pos, "Spear", "Pointy.", 5);
                }else if(r == 4){
                    e = new Weapon(room, pos, "Mace", "Spiky ball on a stick.", 8);
                }else if(r == 5){
                    e = new Weapon(room, pos, "Battle Axe", "High tier weapon.", 14);
                }

            }

        }else if(type == "player"){

            e = new PlayerAI(room, pos, 80, 80, 4, 2, 2, null, this.room.getMap());

        }

        this.entity = e;
        return e;
    }

    public Position getPos(){
        return this.pos;
    }

    public String getType(){
        return this.type;
    }

    public void setEntity(Entity e){

        if(e instanceof Item){
            this.entityType = "item";
        }else if(e instanceof EnemyAI){
            this.entityType = "enemy";
        }else if(e instanceof PlayerAI){
            this.entityType = "player";
        }

        this.entity = e;

    }

    public void clearTile(){
        this.entityType = "empty";
        this.entity = null;
    }

    public String getEntityType(){
        return this.entityType;
    }

    public Entity getEntity(){
        return this.entity;
    }

}
