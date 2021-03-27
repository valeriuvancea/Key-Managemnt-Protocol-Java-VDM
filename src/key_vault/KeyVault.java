package key_vault;

import java.io.IOException;

import common.*;

public class KeyVault {

    private static final String SK_KV_FILE_PATH = "store/sk_kv";
    private static final String PK_KV_FILE_PATH = "store/pk_kv";
    private static final String CERT_KV_FILE_PATH = "store/cert_kv";
    private static final String CERT_M_FILE_PATH = "store/cert_m";

    private byte[] certKeyVault;

    public KeyVault() throws IOException, InterruptedException {
        this.certKeyVault = Common.ReadFromFile(KeyVault.CERT_KV_FILE_PATH);
        System.out.println(Common.IsCertificateValid(this.certKeyVault, KeyVault.CERT_M_FILE_PATH));
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
