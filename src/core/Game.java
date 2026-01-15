package core;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Game {
    private String opponent;
    private LocalDate date;
    private boolean homeOrAway; // true = home, false = away

    public Game(String opponent, String dateStr, boolean homeOrAway) {
        this.opponent = opponent;
        this.homeOrAway = homeOrAway;

        // Parse API string to LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        this.date = LocalDate.parse(dateStr.substring(0, 10), formatter);
    }

    public String getOpponent() { return opponent; }
    public LocalDate getDate() { return date; }
    public boolean getisHomeOrAway() { return homeOrAway; }

    public void setOpponent(String opponent) { this.opponent = opponent; }
    public void setDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        this.date = LocalDate.parse(dateStr.substring(0, 10), formatter);
    }
    public void setHomeOrAway(boolean homeOrAway) { this.homeOrAway = homeOrAway; }
}
