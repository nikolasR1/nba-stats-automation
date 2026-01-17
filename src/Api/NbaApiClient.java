package Api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.google.gson.Gson;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;

import core.Player;
import core.PlayerGamePerformance;
import core.Game;
import core.StatLine;

public class NbaApiClient {
    private final String apiKey;
    private long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL = 13000; // 13 seconds between requests

    public NbaApiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    // Rate limit helper prevents hitting the 5 requests/minute limit
    private void rateLimitDelay() {
        try {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastRequest = currentTime - lastRequestTime;

            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
                long sleepTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest;
                int secondsToWait = (int) (sleepTime / 1000);

                System.out.print("Rate limiting: waiting " + secondsToWait + " seconds");

                // Countdown display
                for (int i = secondsToWait; i > 0; i--) {
                    Thread.sleep(1000);
                    System.out.print(".");
                }
                System.out.println(" done!");
            }

            lastRequestTime = System.currentTimeMillis();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Fetch a list of players
    public List<Player> fetchPlayers(int count) {
        List<Player> players = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            Gson gson = new Gson();
            int remaining = count;
            int page = 1;

            while (remaining > 0) {
                int perPage = Math.min(remaining, 25);
                String url = "https://api.balldontlie.io/v1/players?per_page=" + perPage + "&page=" + page;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", apiKey)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                rateLimitDelay(); // Add delay after API call

                String body = response.body();

                if (!body.startsWith("{")) break;

                PlayerApiWrapper wrapper = gson.fromJson(body, PlayerApiWrapper.class);
                if (wrapper == null || wrapper.data == null || wrapper.data.isEmpty()) break;

                for (PlayerApi p : wrapper.data) {
                    String position = (p.position != null) ? p.position : "G";
                    players.add(new Player(p.id, p.first_name, p.last_name, position, p.team.abbreviation));
                }

                remaining -= wrapper.data.size();
                page++;
            }

        } catch (Exception e) {
            System.out.println("Error fetching players: " + e.getMessage());
        }
        return players;
    }

    /**
     * Fetches players from teams that have played recently (most accurate for current players)
     */
    public List<Player> fetchRecentPlayers(int count) {
        // Step 1: Get recent games to identify active teams (ONE API CALL)
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(14);

        System.out.println("Fetching games from " + startDate + " to today...");
        List<Game> recentGames = fetchGames(100, startDate.toString());

        if (recentGames.isEmpty()) {
            System.out.println("No recent games found. Fetching all players instead.");
            return fetchPlayers(count);
        }

        // Step 2: Get all active team IDs from recent games
        Set<String> activeTeams = recentGames.stream()
                .flatMap(g -> java.util.stream.Stream.of(
                        g.getHomeTeamAbbr(),
                        g.getVisitorTeamAbbr()
                ))
                .collect(Collectors.toSet());

        System.out.println("Found " + activeTeams.size() + " active teams from recent games");

        // Step 3: Fetch players and filter by active teams (ONE API CALL)
        List<Player> allPlayers = fetchPlayers(count * 3);

        List<Player> recentPlayers = allPlayers.stream()
                .filter(p -> activeTeams.contains(p.getTeamAbbr()))
                .filter(p -> p.getTeamAbbr() != null && !p.getTeamAbbr().isEmpty())
                .limit(count)
                .collect(Collectors.toList());

        System.out.println("Filtered to " + recentPlayers.size() + " players from active teams");

        return recentPlayers;
    }

    // Fetch games from API
    public List<Game> fetchGames(int count) {
        return fetchGames(count, "2026-01-01");
    }

    public List<Game> fetchGames(int count, String startDate) {
        List<Game> games = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = "https://api.balldontlie.io/v1/games?per_page=" + count + "&start_date=" + startDate;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            rateLimitDelay(); // Add delay after API call

            String body = response.body();

            // Check if we got rate limited or error response
            if (!body.startsWith("{")) {
                System.out.println("API Error: " + body);
                return games;
            }

            Gson gson = new Gson();
            GameApiWrapper wrapper = gson.fromJson(body, GameApiWrapper.class);

            if (wrapper != null && wrapper.data != null) {
                for (GameApi g : wrapper.data) {
                    String homeAbbr = g.home_team.abbreviation;
                    String visitorAbbr = g.visitor_team.abbreviation;

                    games.add(new Game(homeAbbr, visitorAbbr, g.date));
                }
            }

        } catch (Exception e) {
            System.out.println("Error fetching games: " + e.getMessage());
        }
        return games;
    }


    // Generate random performances
    public List<PlayerGamePerformance> generatePerformances(List<Player> players, List<Game> games) {
        List<PlayerGamePerformance> list = new ArrayList<>();
        Random rand = new Random();

        for (Player player : players) {
            String position = (player.getPosition() != null) ? player.getPosition() : "G";

            for (Game game : games) {
                double min = 20 + rand.nextDouble() * 20;
                int pts, reb, ast;

                switch (position) {
                    case "G":
                        pts = 10 + rand.nextInt(21);
                        ast = 3 + rand.nextInt(8);
                        reb = rand.nextInt(6);
                        break;
                    case "F":
                        pts = 8 + rand.nextInt(18);
                        ast = 1 + rand.nextInt(5);
                        reb = 3 + rand.nextInt(8);
                        break;
                    case "C":
                        pts = 6 + rand.nextInt(15);
                        ast = rand.nextInt(3);
                        reb = 5 + rand.nextInt(11);
                        break;
                    default:
                        pts = 8 + rand.nextInt(20);
                        ast = 2 + rand.nextInt(5);
                        reb = 2 + rand.nextInt(6);
                        break;
                }

                StatLine stats = new StatLine(pts, reb, ast, min);
                list.add(new PlayerGamePerformance(player, game, stats));
            }
        }

        return list;
    }

    public List<Player> filterCurrentPlayers(List<Player> players, List<Game> recentGames) {
        // Collect all team abbreviations in recent games
        Set<String> activeTeams = recentGames.stream()
                .flatMap(g -> java.util.stream.Stream.of(
                        g.getHomeTeamAbbr(),
                        g.getVisitorTeamAbbr()
                ))
                .collect(Collectors.toSet());

        return players.stream()
                .filter(p -> activeTeams.contains(p.getTeamAbbr()))
                .filter(p -> p.getTeamAbbr() != null && !p.getTeamAbbr().isEmpty())
                .collect(Collectors.toList());
    }

    // ---- Gson helper classes ----
    private static class PlayerApiWrapper { List<PlayerApi> data; }
    private static class PlayerApi { int id; String first_name; String last_name; String position; TeamApi team; }
    private static class TeamApi { String abbreviation; }
    private static class GameApiWrapper { List<GameApi> data; }
    private static class GameApi { String date; TeamApi home_team; TeamApi visitor_team; }
}