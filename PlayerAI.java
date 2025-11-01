import java.util.ArrayList;

public class PlayerAI extends AI{
    
    private ArrayList<Item> inventory;
    private Weapon equippedWeapon;
    private String facingDirection;

    public PlayerAI(Room currentRoom, Position pos, int hp, int maxHP, int aS, int dS, int ap, int maxAP, Weapon startingWeapon){
        
        super(currentRoom, pos, hp, maxHP, aS, dS, ap, maxAP);
        this.inventory = new ArrayList<>();
        this.equippedWeapon = startingWeapon;
        this.facingDirection = "Up";

    }

    public void takeTurn(){
        //TODO
    }

    public String getDirection(){
        return this.facingDirection;
    }

    public void setDirection(String dir){
        this.facingDirection = dir;
    }

    public int getTotalAttackStat(){
        return this.getBaseAttackStat() + this.equippedWeapon.getDamage();
    }

}
