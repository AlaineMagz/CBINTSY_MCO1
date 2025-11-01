public class Consumable extends Item{
    
    private int restoreHealth;
    private int attackBoost;

    public Consumable(Room currentRoom, Position pos, String name, String description, int rH, int aB){

        super(currentRoom, pos, name, description, "consumable");
        this.restoreHealth = rH;
        this.attackBoost = aB;

    }

    public void useConsumable(PlayerAI player){
        
        player.heal(restoreHealth);
        player.increaseBaseAttackStat(attackBoost);

    }

}
