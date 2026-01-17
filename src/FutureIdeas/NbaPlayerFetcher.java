package FutureIdeas;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import core.Player;

public class NbaPlayerFetcher {
    //Sets API key
    private final String API_KEY;
    public NbaPlayerFetcher(String apiKey) {
        this.API_KEY = apiKey;
    }
    //Helper classes for fetching player/team ID
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
    //Fetches for player
    public Player fetchPlayerById(int playerId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            Gson gson = new Gson();
            String url = "https://api.balldontlie.io/v1/players/" + playerId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            if (!body.startsWith("{")) {
                System.out.println("API returned non-JSON: " + body);
                return null;
            }

            // Deserialize directly to PlayerSingleApi
            NbaPlayerFetcher.PlayerSingleApi p = gson.fromJson(body, NbaPlayerFetcher.PlayerSingleApi.class);

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
}
