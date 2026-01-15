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

import core.Player;
import core.PlayerGamePerformance;
import core.Game;
import core.StatLine;

public class NbaApiClient {
    private final String apiKey;

    public NbaApiClient(String apiKey) {
        this.apiKey = apiKey;
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
                        .header("Authorization", "Bearer " + apiKey)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
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

    // Fetch a single player by ID
    public Player fetchPlayerById(int playerId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            Gson gson = new Gson();
            String url = "https://api.balldontlie.io/v1/players/" + playerId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            if (!body.startsWith("{")) {
                System.out.println("API returned non-JSON: " + body);
                return null;
            }

            // Deserialize directly to PlayerSingleApi
            PlayerSingleApi p = gson.fromJson(body, PlayerSingleApi.class);

            if (p == null) {
                System.out.println("Player not found!");
                return null;
            }

            String position = (p.position != null) ? p.position : "G";
            String teamAbbr = (p.team != null && p.team.abbreviation != null) ? p.team.abbreviation : "N/A";

            return new Player(p.id, p.first_name, p.last_name, position, teamAbbr);

        } catch (Exception e) {
            System.out.println("Error fetching player by ID: " + e.getMessage());
            return null;
        }
    }


    // ------------------ Helper classes ------------------
    private static class PlayerSingleApi {
        int id;
        String first_name;
        String last_name;
        String position;
        Team team;

        static class Team {
            int id;
            String abbreviation;
        }
    }

    // Fetch games from API
    public List<Game> fetchGames(int count) {
        return fetchGames(count, "2026-01-01"); // or just use empty startDate
    }
    public List<Game> fetchGames(int count, String startDate) {
        List<Game> games = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = "https://api.balldontlie.io/v1/games?per_page=" + count + "&start_date=" + startDate;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();
            GameApiWrapper wrapper = gson.fromJson(response.body(), GameApiWrapper.class);

            for (GameApi g : wrapper.data) {
                String homeAbbr = g.home_team.abbreviation;
                String visitorAbbr = g.visitor_team.abbreviation;

                boolean isHome = true;
                String opponent = isHome ? visitorAbbr : homeAbbr;

                games.add(new Game(opponent + " @ " + (isHome ? homeAbbr : visitorAbbr),
                        g.date,
                        isHome));
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
                        g.getOpponent().split(" @ ")[0],
                        g.getOpponent().split(" @ ")[1]))
                .collect(Collectors.toSet());

        // Filter players who are on active teams
        return players.stream()
                .filter(p -> activeTeams.contains(p.getTeamAbbr()))
                .collect(Collectors.toList());
    }

    // ---- Gson helper classes ----
    private static class PlayerApiWrapper { List<PlayerApi> data; }
    private static class PlayerApi { int id; String first_name; String last_name; String position; TeamApi team; }
    private static class TeamApi { String abbreviation; }
    private static class GameApiWrapper { List<GameApi> data; }
    private static class GameApi { String date; TeamApi home_team; TeamApi visitor_team; }
}
