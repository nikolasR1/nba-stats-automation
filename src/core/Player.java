package core;

public class Player {
    private int id;
    private String firstName;
    private String lastName;
    private String position;
    private String teamAbbr;

    public Player(int id, String firstName, String lastName, String position, String teamAbbr) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.teamAbbr = teamAbbr;
    }

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPosition() { return position; }
    public String getTeamAbbr() { return teamAbbr; }
}
