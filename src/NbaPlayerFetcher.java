import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

public class NbaPlayerFetcher {

    private final String API_KEY;

    public NbaPlayerFetcher(String apiKey) {
        this.API_KEY = apiKey;
    }

    // Classes to match JSON structure
    class PlayerWrapper {
        List<PlayerApi> data;
    }

    class PlayerApi {
        int id;
        String first_name;
        String last_name;
        TeamApi team;
    }

    class TeamApi {
        int id;
        String abbreviation;
    }

    public List<Integer> fetchPlayerIDs(int numberOfPlayers) {
        List<Integer> playerIDs = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = "https://api.balldontlie.io/v1/players?per_page=" + numberOfPlayers;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();

            System.out.println("Raw JSON from players endpoint:\n" + json);

            Gson gson = new Gson();
            PlayerWrapper wrapper = gson.fromJson(json, PlayerWrapper.class);

            for (PlayerApi p : wrapper.data) {
                playerIDs.add(p.id);
            }

        } catch (Exception e) {
            System.out.println("Error fetching player IDs: " + e.getMessage());
        }
        return playerIDs;
    }
}
