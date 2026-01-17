package core;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Game {
    private String homeTeamAbbr;
    private String visitorTeamAbbr;
    private LocalDate date;

    public Game(String homeTeamAbbr, String visitorTeamAbbr, String dateStr) {
        this.homeTeamAbbr = homeTeamAbbr;
        this.visitorTeamAbbr = visitorTeamAbbr;

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        this.date = LocalDate.parse(dateStr.substring(0, 10), formatter);
    }

    public String getHomeTeamAbbr() {
        return homeTeamAbbr;
    }

    public String getVisitorTeamAbbr() {
        return visitorTeamAbbr;
    }

    public LocalDate getDate() {
        return date;
    }
}
