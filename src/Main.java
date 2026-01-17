import java.util.List;
import java.util.Scanner;

import Api.NbaApiClient;
import Automation.AutomatedStatsRunner;
import core.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your API key: ");
        String apiKey = scanner.nextLine();

        NbaApiClient apiClient = new NbaApiClient(apiKey);
        AutomatedStatsRunner runner = new AutomatedStatsRunner(apiClient);
        runner.runAutomatedSimulation();


        // Step 1: Fetch a batch of players
        List<Player> players = apiClient.fetchPlayers(20);
        if (players.isEmpty()) {
            System.out.println("No players found. Exiting.");
            return;
        }

        // Step 2: Let the user select a player from the list
        Player selectedPlayer = PlayerSelector.choosePlayer(players);

        // Step 3: Fetch some games
        List<Game> games = apiClient.fetchGames(5); // fetch 5 games for demo
        if (games.isEmpty()) {
            System.out.println("No games found. Exiting.");
            return;
        }

        // Step 4: Generate performances only for the selected player
        List<Player> singlePlayerList = List.of(selectedPlayer);
        List<PlayerGamePerformance> performances = apiClient.generatePerformances(singlePlayerList, games);

        // Step 5: Print out the performances
        System.out.println("\nGenerated Performances:");
        for (PlayerGamePerformance perf : performances) {
            Player p = perf.getPlayer();
            Game g = perf.getGame();
            StatLine s = perf.getStats();

            System.out.printf("%s %s | %s | %s | Date: %s | PTS: %d, REB: %d, AST: %d, MIN: %.1f%n",
                    p.getFirstName(),
                    p.getLastName(),
                    g.getOpponent(),
                    g.getisHomeOrAway() ? "Home" : "Away",
                    g.getDate(),
                    s.getPoints(),
                    s.getRebounds(),
                    s.getAssists(),
                    s.getMinutes());
        }
    }
}
