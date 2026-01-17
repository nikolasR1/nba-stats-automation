import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Api.NbaApiClient;
import Automation.AutomatedGameTracker;

public class Main {
    public static void main(String[] args) {
        System.out.println("+==========================================================+");
        System.out.println("|     NBA AUTOMATED GAME TRACKING SYSTEM v1.0              |");
        System.out.println("+==========================================================+\n");

        // Load API key from config file
        Properties config = new Properties();
        try {
            config.load(new FileInputStream("config.properties"));
            String apiKey = config.getProperty("api.key");

            if (apiKey == null || apiKey.trim().isEmpty()) {
                System.out.println("Error: API key not found in config.properties");
                System.out.println("Please create config.properties with: api.key=YOUR_KEY");
                return;
            }

            NbaApiClient apiClient = new NbaApiClient(apiKey);
            AutomatedGameTracker tracker = new AutomatedGameTracker(apiClient);

            // Run immediately on startup
            System.out.println("Running initial report...\n");
            tracker.runDailyReport();

            // Schedule daily runs
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            System.out.println("\nScheduler activated!");
            System.out.println("Will run daily at this time.");
            System.out.println("Reports saved to ./reports/");
            System.out.println("\nPress Ctrl+C to stop.\n");

            scheduler.scheduleAtFixedRate(
                    () -> tracker.runDailyReport(),
                    24,          // Initial delay (24 hours from now)
                    24,          // Run every 24 hours
                    TimeUnit.HOURS
            );

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}