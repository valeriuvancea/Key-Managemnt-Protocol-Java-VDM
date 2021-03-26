import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Start");

        while (true) {
            Thread.sleep(3000);
            byte[] dummyBytes = Common.ReadFromFile("store/pk_kv");
            System.out.println(dummyBytes.toString());
        }
    }
}
