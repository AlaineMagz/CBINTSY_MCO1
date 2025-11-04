public class EnemyAI extends AI {
    
    private String type;
    private String behavior;
    private boolean isHostile;
    private PlayerAI targetPlayer; 

    public EnemyAI(Room currentRoom, Position pos, int hp, int maxHP, int aS, int ap, int maxAP, String type, String behavior) {
        super(currentRoom, pos, hp, maxHP, aS, ap, maxAP, "up");
        this.type = type;
        this.behavior = behavior;
        this.isHostile = false;
        this.targetPlayer = null;
    }

    public void takeTurn() {
        // Check if player is in the same room
        checkForPlayerInRoom();
        
        // Only act if hostile (player detected in room)
        if (this.isHostile) {
            System.out.println(this.type + " is hostile! Taking action...");
            
            if (this.targetPlayer != null && this.targetPlayer.isAlive()) {
                chasePlayer();
            } else {
                // Player left or died, become dormant again
                this.isHostile = false;
                this.targetPlayer = null;
            }
        }
    }

    private void checkForPlayerInRoom() {
        Room currentRoom = this.getCurrentRoom();
        
        // Scan all entities in the room for PlayerAI
        for (Entity entity : currentRoom.getEntityList()) {
            if (entity instanceof PlayerAI) {
                PlayerAI player = (PlayerAI) entity;
                if (player.isAlive()) {
                    this.targetPlayer = player;
                    this.isHostile = true;
                    System.out.println(this.type + " detected player in room! Becoming hostile.");
                    return;
                }
            }
        }
        
        // Also check tiles for player (in case entity list doesn't work)
        int width = currentRoom.getWidth();
        int height = currentRoom.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Tile tile = currentRoom.checkTile(new Position(x, y));
                if (tile.getEntityType().equals("player")) {
                    Entity entity = tile.getEntity();
                    if (entity instanceof PlayerAI) {
                        PlayerAI player = (PlayerAI) entity;
                        if (player.isAlive()) {
                            this.targetPlayer = player;
                            this.isHostile = true;
                            System.out.println(this.type + " detected player on tile! Becoming hostile.");
                            return;
                        }
                    }
                }
            }
        }
        
        // No player found, remain dormant
        this.isHostile = false;
        this.targetPlayer = null;
    }

    private void chasePlayer() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) {
            this.isHostile = false;
            return;
        }
        
        switch(this.behavior) {
            case "mobile":
                mobileChaseBehavior();
                break;

            case "immobile":
                immobileBehavior();
                break;

            default: 
                defaultBehavior();
                break;
        }
    }

    private void mobileChaseBehavior() {
        Position playerPos = this.targetPlayer.getPosition();
        Position enemyPos = this.getPosition();
        
        System.out.println(this.type + " (mobile) chasing player from " + 
                         enemyPos.getCoordinates() + " to " + playerPos.getCoordinates());
        
        // Calculate direction to player
        int dx = playerPos.getX() - enemyPos.getX();
        int dy = playerPos.getY() - enemyPos.getY();
        
        // Prefer horizontal movement first
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) {
                setDirection("right");
            } else {
                setDirection("left");
            }
        } else {
            if (dy > 0) {
                setDirection("down");
            } else {
                setDirection("up");
            }
        }
        
        // Check if we can move toward player
        Tile frontTile = this.getCurrentRoom().checkTile(this.getPosition().getAdjacent(getDirection()));
        
        if (frontTile.isWalkable() && !frontTile.getEntityType().equals("enemy")) {
            // Attack if player is in front
            if (frontTile.getEntityType().equals("player")) {
                attack(getBaseAttackStat());
                System.out.println(this.type + " attacks player!");
            } else {
                // Move toward player
                moveForward();
                System.out.println(this.type + " moves toward player");
            }
        } else if (frontTile.getEntityType().equals("player")){
            attack(getBaseAttackStat());
            System.out.println(this.type + " attacks player!");
        } else {
            // Can't move in preferred direction, try alternatives
            tryAlternativeDirections();
        }
    }

    private void immobileBehavior() {
        Position playerPos = this.targetPlayer.getPosition();
        Position enemyPos = this.getPosition();
        
        System.out.println(this.type + " (immobile) tracking player at " + playerPos.getCoordinates());
        
        // Face the player but don't move
        int dx = playerPos.getX() - enemyPos.getX();
        int dy = playerPos.getY() - enemyPos.getY();
        
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) {
                setDirection("right");
            } else {
                setDirection("left");
            }
        } else {
            if (dy > 0) {
                setDirection("down");
            } else {
                setDirection("up");
            }
        }
        
        // Attack if player is adjacent
        Tile frontTile = this.getCurrentRoom().checkTile(this.getPosition().getAdjacent(getDirection()));
        if (frontTile.getEntityType().equals("player")) {
            attack(getBaseAttackStat());
            System.out.println(this.type + " attacks adjacent player!");
        }
    }

    private void defaultBehavior() {
        // Default mobile behavior
        mobileChaseBehavior();
    }

    private void tryAlternativeDirections() {
        String[] directions = {"up", "down", "left", "right"};
        
        for (String dir : directions) {
            setDirection(dir);
            Tile frontTile = this.getCurrentRoom().checkTile(this.getPosition().getAdjacent(dir));
            
            if (frontTile.isWalkable() && !frontTile.getEntityType().equals("enemy")) {
                moveForward();
                System.out.println(this.type + " moves in alternative direction: " + dir);
                return;
            }
        }
        
        // Can't move in any direction
        System.out.println(this.type + " is stuck and cannot move");
    }

    public String getEnemyType() {
        return this.type;
    }
    
    public boolean isHostile() {
        return this.isHostile;
    }
    
    public PlayerAI getTargetPlayer() {
        return this.targetPlayer;
    }

    public void setIsHostile(boolean b){
        this.isHostile = b;
    }

}