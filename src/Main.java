import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Start");

        while (true) {
            Thread.sleep(3000);
            String dummy = "X";
            byte[] dummyBytes = dummy.getBytes();
            Common.WriteToFile(dummyBytes, "store/pk_kv");
        }
    }
}
