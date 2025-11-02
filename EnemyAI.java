public class EnemyAI extends AI{
    
    private String type;
    private String behavior;
    private boolean isHostile;

    public EnemyAI(Room currentRoom, Position pos, int hp, int maxHP, int aS, int ap, int maxAP, String type, String behavior){

        super(currentRoom, pos, hp, maxHP, aS, ap, maxAP, "up");
        this.type = type;
        this.behavior = behavior;
        this.isHostile = false;

    }

    public void takeTurn(PlayerAI player){
        
        if(this.isHostile){
            chasePlayer(player);
        }

    }

    public void chasePlayer(PlayerAI player){
        
        switch(this.behavior){

            case "mobile":

            //TODO

            break;

            case "immobile":



            break;

            default: 



            break;

        }

    }

    public String getEnemyType(){
        return this.type;
    }

}
