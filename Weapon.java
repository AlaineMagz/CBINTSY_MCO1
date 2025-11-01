public class Weapon extends Item{
    
    private int damage;

    public Weapon(Room currentRoom, Position pos, String name, String description, int damage){

        super(currentRoom, pos, name, description, "weapon");
        this.damage = damage;

    }

    public void equipWeapon(PlayerAI player){
        player.setEquippedWeapon(this);
    }

    public int getDamage(){
        return this.damage;
    }

    public boolean isBetterThan(Weapon other){
        return (this.damage > other.getDamage()) ? true : false;
    }

}
