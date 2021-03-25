import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Common {
    public static String RunCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        process.waitFor();

        String line = "";
        StringBuilder outputBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            outputBuilder.append(line);
            outputBuilder.append(System.lineSeparator());
        }

        return outputBuilder.toString();
    }
}