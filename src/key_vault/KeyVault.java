package key_vault;

import java.io.IOException;

import common.*;

public class KeyVault {
    public KeyVault() throws IOException, InterruptedException {

    }

    private byte[] GenerateRandomByteArray(int numberOfBytes) throws IOException, InterruptedException {
        try {
            Common.RunCommand(String.format("openssl rand -out %s -hex %s", Common.TEMP_DATA_FILE, numberOfBytes));
            String output = new String(Common.ReadFromFile(Common.TEMP_DATA_FILE));
            byte[] bytes = Common.StringToByteArray(output);
            return bytes;
        } finally {
            Common.RemoveFile(Common.TEMP_DATA_FILE, false);
        }

    }

    private byte[] GenerateRandomByteArray() throws IOException, InterruptedException {
        return this.GenerateRandomByteArray(128);
    }

}
