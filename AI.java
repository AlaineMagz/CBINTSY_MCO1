public abstract class AI extends Entity{
    
    private int health;
    private int maxHealth;
    private int attackStat;
    private int actionPoints;
    private int maxAP;

    public AI(Room currentRoom, Position pos, int hp, int maxHP, int aS, int ap, int maxAP){

        super(currentRoom, pos);
        this.health = hp;
        this.maxHealth = maxHP;
        this.attackStat = aS;
        this.actionPoints = ap;
        this.maxAP = maxAP;

    }

    public void takeDamage(int damage){
        this.health -= damage;
    }

    public boolean isAlive(){
        return (this.health > 0) ? true : false;
    }

    public void heal(int healAmount){

        this.health += healAmount;

        if(this.health > this.maxHealth){
            this.health = this.maxHealth;
        }

    }

    public int getBaseAttackStat(){
        return this.attackStat;
    }

    public void increaseBaseAttackStat(int incAmount){
        this.attackStat += incAmount;
    }

    public int getActionPoints(){
        return this.actionPoints;
    }

    public void useActionPoint(){
        this.actionPoints -= 1;
    }

    public void replenishActionPoints(){
        this.actionPoints = this.maxAP;
    }

}
