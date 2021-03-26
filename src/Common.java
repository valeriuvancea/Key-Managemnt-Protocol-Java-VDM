import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public static void WriteToFile(byte[] buffer, String filePath) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        FileOutputStream output = new FileOutputStream(file);
        output.write(buffer);
        output.close();
    }

    public static byte[] ReadFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream input = new FileInputStream(file);
        byte[] output = new byte[(int) file.length()];
        input.read(output);
        input.close();
        return output;
    }
}