package manufacturer;

import java.io.IOException;
import common.*;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        String keyVaultIdString = "key_vault";
        byte[] controller_1_id = "controller_1".getBytes();
        byte[] controller_2_id = "controller_2".getBytes();

        Pair<byte[], byte[]> manufacturerKeys = Common.GenerateKeyPair("manufacturer_store/pk_m",
                "manufacturer_store/sk_m");

        /* Controller 1 */
        Common.WriteToFile(controller_1_id, "controller_1_store/id");

        /* Controller 2 */
        Common.WriteToFile(controller_1_id, "controller_2_store/id");
    }
}
