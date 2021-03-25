import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Start");

        while (true) {
            Thread.sleep(3000);
            String result = Common.RunCommand("ping -c 4 www.stackabuse.com");
            System.out.println(result);
        }
    }
}
