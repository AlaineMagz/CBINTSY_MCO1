public class Position {
    
    private int x;
    private int y;

    public Position(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    public Position getAdjacent(String dir){

        switch(dir){

            case "up": return new Position(this.x, this.y - 1);
            case "down": return new Position(this.x, this.y + 1);
            case "left": return new Position(this.x - 1, this.y);
            case "right": return new Position(this.x + 1, this.y);

        }

        return this;

    }

    public double distanceTo(Position point){

        double a = (double) Math.abs(this.x - point.getX());
        double b = (double) Math.abs(this.y - point.getY());

        double c = Math.sqrt((a*a) + (b*b));

        return c;

    }

    public String getCoordinates(){
        String s = "(" + this.x + ", " + this.y + ")";
        return s;
    }

}
