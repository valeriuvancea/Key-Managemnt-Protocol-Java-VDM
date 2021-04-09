package org.mockup.key_vault.protocol;

import java.io.IOException;

import org.json.JSONObject;
import org.mockup.common.Common;
import org.mockup.common.communication.Sender;
import org.mockup.common.protocol.IContextTerminatedCallback;
import org.mockup.common.protocol.MessageType;
import org.mockup.common.protocol.ProtocolContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyVaultProtocolContext extends ProtocolContext {
    public static final String SK_KV_FILE_PATH = "store/sk_kv";
    public static final String PK_KV_FILE_PATH = "store/pk_kv";
    public static final String CERT_KV_FILE_PATH = "store/cert_kv";
    public static final String CERT_M_FILE_PATH = "store/cert_m";

    private final String controllerAddress;
    private final byte[] certKeyVault;

    private final Logger logger = LoggerFactory.getLogger(KeyVaultProtocolContext.class);

    public KeyVaultProtocolContext(String controllerAddress, byte[] associatedId, Sender sender,
            IContextTerminatedCallback terminatedCallback) throws IOException {
        super(associatedId, sender, terminatedCallback);
        this.controllerAddress = controllerAddress;
        this.certKeyVault = Common.ReadFromFile(KeyVaultProtocolContext.CERT_KV_FILE_PATH);
    }

    public void SendMessageToController(MessageType type, JSONObject contents) {
        this.SendMessage(this.controllerAddress, type, contents);
    }

    public void SendMessageToController(MessageType type) {
        this.SendMessage(this.controllerAddress, type, new JSONObject());
    }
}
