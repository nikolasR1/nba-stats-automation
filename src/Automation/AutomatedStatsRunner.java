package Automation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import Api.NbaApiClient;
import core.Player;
import core.Game;
import core.PlayerGamePerformance;
import core.StatLine;

public class AutomatedStatsRunner {

    private final NbaApiClient apiClient;


    public AutomatedStatsRunner(NbaApiClient apiClient) {
        this.apiClient = apiClient;
        List<Game> games = apiClient.fetchGames(10, "2026-01-01"); // new method with start_date
        List<Player> players = apiClient.fetchPlayers(50);
        players = apiClient.filterCurrentPlayers(players, games);

        // Keep small batch
        players = players.stream().limit(5).toList();
        games = games.stream().limit(3).toList();


    }

    // Safe small batch automation
    public void runAutomatedSimulation() {
        FileWriter writer;
        try {
            writer = RunLogger.createRunFile();
        } catch (Exception e) {
            System.out.println("Failed to create run log");
            return;
        }

        System.out.println("=== Automated Stats Runner ===");

        // Step 1: Fetch a small batch of players (5 players)
        List<Player> players = apiClient.fetchPlayers(5);
        if (players.isEmpty()) {
            System.out.println("No players found. Exiting automated run.");
            return;
        }

        // Step 2: Fetch a small batch of games (3 games)
        List<Game> games = apiClient.fetchGames(3);
        if (games.isEmpty()) {
            System.out.println("No games found. Exiting automated run.");
            return;
        }

        // Step 3: Generate performances for all players across all games
        List<PlayerGamePerformance> performances = apiClient.generatePerformances(players, games);

        // Step 4: Print the results
        System.out.println("\nGenerated Performances:");
        try {
            for (PlayerGamePerformance perf : performances) {
                Player p = perf.getPlayer();
                Game g = perf.getGame();
                StatLine s = perf.getStats();

                String line = String.format(
                        "%s %s | %s | %s | Date: %s | PTS: %d, REB: %d, AST: %d, MIN: %.1f%n",
                        p.getFirstName(),
                        p.getLastName(),
                        g.getOpponent(),
                        g.getisHomeOrAway() ? "Home" : "Away",
                        g.getDate(),
                        s.getPoints(),
                        s.getRebounds(),
                        s.getAssists(),
                        s.getMinutes()
                );

                System.out.print(line);
                writer.write(line);
            }

            writer.close();

        } catch (IOException e) {
            System.out.println("Error writing run log: " + e.getMessage());
        }
    }
}
