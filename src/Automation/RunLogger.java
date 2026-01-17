package Automation;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RunLogger {
    public static FileWriter createRunFile() throws IOException {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String filename = "runs/run_" + timestamp + ".txt";
        return new FileWriter(filename);
    }
}
