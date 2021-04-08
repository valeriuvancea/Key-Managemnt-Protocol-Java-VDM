package org.mockup.key_vault;

import java.io.IOException;
import org.json.JSONObject;
import org.mockup.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyVault {
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

    public void Kick() {
        JSONObject contents = new JSONObject();
        contents.put(MessageField.TYPE.Value(), MessageType.KEY_VAULT_DISCOVERY_REQUEST.Value());
        contents.put(MessageField.CONTROLLER_ID.Value(), "dummy");
        this.communication.SendMessage("192.168.1.136", contents);
    }

    public void Start() {
        this.communication.Start();
        this.discovery.Start();
    }

    public void Stop() {
        this.communication.Stop();
        this.discovery.Stop();
    }
}
