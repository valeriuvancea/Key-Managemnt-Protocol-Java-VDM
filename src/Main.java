import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Start");

        while (true) {
            byte[] bytes = new byte[] { (byte) 0xAB, (byte) 0xF0, (byte) 0x0F, (byte) 0xAA, (byte) 0x00, (byte) 0x11,
                    (byte) 0xFF };
            String text = Common.ByteArrayToString(bytes);
            System.out.println(text);
            byte[] bytesAgain = Common.StringToByteArray(text);
            System.out.println(Common.ByteArrayToString(bytesAgain));
            Thread.sleep(1000);
        }
    }
}
