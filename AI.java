public abstract class AI extends Entity{
    
    private int health;
    private int maxHealth;
    private int attackStat;
    private int actionPoints;
    private int maxAP;
    private String facingDirection;

    public AI(Room currentRoom, Position pos, int hp, int maxHP, int aS, int ap, int maxAP, String facingDir){

        super(currentRoom, pos);
        this.health = hp;
        this.maxHealth = maxHP;
        this.attackStat = aS;
        this.actionPoints = ap;
        this.maxAP = maxAP;
        this.facingDirection = facingDir;

    }

    public void takeDamage(int damage){
        this.health -= damage;
    }

    public boolean isAlive(){
        return (this.health > 0);
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

    public String getDirection(){
        return this.facingDirection;
    }

    public void setDirection(String dir){
        this.facingDirection = dir;
    }

    public void moveForward(){

        this.useActionPoint();

        Tile currentTile = this.getCurrentRoom().checkTile(this.getPosition());
        Tile newTile = this.getCurrentRoom().checkTile(this.getPosition().getAdjacent(facingDirection));

        currentTile.clearTile();
        newTile.setEntity(this);

        this.setPosition(newTile.getPos());

    }

    public void rotate(String newDir){
        this.useActionPoint();
        this.facingDirection = newDir;
    }

    public void attack(int damage){

        Tile attackTile = this.getCurrentRoom().checkTile(this.getPosition().getAdjacent(facingDirection));

        if(attackTile.getEntityType() == "enemy"){
            ((AI) attackTile.getEntity()).takeDamage(damage);
            System.out.println("ATTACKED ENEMY WITH " + damage + " DAMAGE!");
            System.out.println("ENEMY HAS " + ((AI) attackTile.getEntity()).getHealth() + " HP LEFT!");
            System.out.println("ENEMY IS DEAD = " + !((AI) attackTile.getEntity()).isAlive());
        }

    }

    public int getHealth(){
        return this.health;
    }

    public int getMaxHealth(){
        return this.maxHealth;
    }

}
