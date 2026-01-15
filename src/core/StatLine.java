package core;

public class StatLine {
    private int points;
    private int rebounds;
    private int assists;
    private double minutes;

    public StatLine(int points, int rebounds, int assists, double minutes) {
        this.points = points;
        this.rebounds = rebounds;
        this.assists = assists;
        this.minutes = minutes;
    }

    public int getPoints() { return points; }
    public int getRebounds() { return rebounds; }
    public int getAssists() { return assists; }
    public double getMinutes() { return minutes; }
}
