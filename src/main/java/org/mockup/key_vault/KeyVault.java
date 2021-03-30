package org.mockup.key_vault;

import java.io.IOException;
import org.json.JSONObject;
import org.mockup.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyVault implements ICommunicationCallback, IDiscoveryCallback {
    private static final String SK_KV_FILE_PATH = "store/sk_kv";
    private static final String PK_KV_FILE_PATH = "store/pk_kv";
    private static final String CERT_KV_FILE_PATH = "store/cert_kv";
    private static final String CERT_M_FILE_PATH = "store/cert_m";

    private final Logger logger = LoggerFactory.getLogger(Main.class);

    private final byte[] certKeyVault;
    private final Communication communication;
    private final KeyVaultDiscovery discovery;

    public KeyVault() throws IOException, InterruptedException {
        this.certKeyVault = Common.ReadFromFile(KeyVault.CERT_KV_FILE_PATH);
        this.communication = new Communication(Common.GetIpAddress(), this);
        this.discovery = new KeyVaultDiscovery(this);
    }

    public void Start() {
        this.communication.Start();
        this.discovery.Start();
    }

    public void Stop() {
        this.communication.Stop();
        this.discovery.Stop();
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

    private byte[] Decrypt(String skFilePath, byte[] cipher) throws IOException, InterruptedException {
        try {
            Common.WriteToFile(cipher, Common.TEMP_CIPHER_FILE);
            Common.RunCommand(String.format("openssl rsautl -decrypt -inkey %s -in %s -out %s", skFilePath,
                    Common.TEMP_CIPHER_FILE, Common.TEMP_DATA_FILE));
            return Common.ReadFromFile(Common.TEMP_DATA_FILE);
        } finally {
            Common.RemoveFile(Common.TEMP_CIPHER_FILE, false);
            Common.RemoveFile(Common.TEMP_DATA_FILE, false);
        }
    }

    @Override
    public void HandleMessage(String senderIpAddress, JSONObject contents) {
        System.out.println(String.format("Key vault received message from %s with contents of %s", senderIpAddress,
                contents.toString()));
    }

    @Override
    public void BroadcastReceived(String sourceIpAddress, String controllerId) {
        logger.debug("Received broadcast from {}", sourceIpAddress);
    }
}
