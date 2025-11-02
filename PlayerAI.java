import java.util.ArrayList;

public class PlayerAI extends AI{
    
    private ArrayList<Item> inventory;
    private Weapon equippedWeapon;

    public PlayerAI(Room currentRoom, Position pos, int hp, int maxHP, int aS, int ap, int maxAP, Weapon startingWeapon){
        
        super(currentRoom, pos, hp, maxHP, aS, ap, maxAP, "up");
        this.inventory = new ArrayList<>();
        this.equippedWeapon = startingWeapon;

    }

    public void takeTurn(){
        //TODO
    }

    public int getTotalAttackStat(){
        return this.getBaseAttackStat() + this.equippedWeapon.getDamage();
    }

    public void setEquippedWeapon(Weapon w){
        this.equippedWeapon = w;
    }

    public ArrayList<Item> getInventory(){
        return this.inventory;
    }

}
