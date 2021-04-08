package org.mockup.key_vault;

import java.io.IOException;
import org.json.JSONObject;
import org.mockup.common.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyVault {
    private static final String SK_KV_FILE_PATH = "store/sk_kv";
    private static final String PK_KV_FILE_PATH = "store/pk_kv";
    private static final String CERT_KV_FILE_PATH = "store/cert_kv";
    private static final String CERT_M_FILE_PATH = "store/cert_m";

    private final Logger logger = LoggerFactory.getLogger(Main.class);

    private final byte[] certKeyVault;

    public KeyVault() throws IOException, InterruptedException {
        this.certKeyVault = Common.ReadFromFile(KeyVault.CERT_KV_FILE_PATH);
    }

    public void Start() {

    }

    public void Stop() {

    }
}
