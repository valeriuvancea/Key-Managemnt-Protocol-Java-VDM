package org.mockup.controller.protocol;

import java.io.IOException;

import org.json.JSONObject;
import org.apache.commons.codec.digest.Crypt;
import org.javatuples.Pair;
import org.mockup.common.Common;
import org.mockup.common.communication.Sender;
import org.mockup.common.crypto.Crypto;
import org.mockup.common.protocol.IContextTerminatedCallback;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;
import org.mockup.common.protocol.ProtocolContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerProtocolContext extends ProtocolContext {
    public static final String CERT_M_FILE_PATH = "store/cert_m";
    public static final String ID_FILE_PATH = "store/id";
    public static final String SK_M_FILE_PATH = "store/sk_m";
    public static final String SK_CT_FILE_PATH = "store/sk_ct";
    public static final String PK_CT_FILE_PATH = "store/pk_ct";
    public static final String CERT_CT_FILE_PATH = "store/cert_ct";

    private final byte[] certController;

    private String keyVaultIpAddress;

    private final Logger logger = LoggerFactory.getLogger(ControllerProtocolContext.class);

    public ControllerProtocolContext(Sender sender, IContextTerminatedCallback terminatedCallback)
            throws IOException, InterruptedException {
        super(Common.ReadFromFile(ControllerProtocolContext.ID_FILE_PATH), sender, terminatedCallback);

        if (Common.FileExists(ControllerProtocolContext.SK_M_FILE_PATH)) {
            /* Hackish way for now */
            Pair<byte[], byte[]> controllerKeys = Crypto.GenerateKeyPairTPM(ControllerProtocolContext.PK_CT_FILE_PATH,
                    ControllerProtocolContext.SK_CT_FILE_PATH);
            byte[] certControllerProtocolContext = Crypto.GenerateCertificate(
                    ControllerProtocolContext.CERT_M_FILE_PATH, ControllerProtocolContext.SK_M_FILE_PATH,
                    controllerKeys.getValue0(), this.associatedIdString);
            Common.WriteToFile(certControllerProtocolContext, ControllerProtocolContext.CERT_CT_FILE_PATH);

            Common.RemoveFile(ControllerProtocolContext.SK_M_FILE_PATH);
        }

        this.certController = Common.ReadFromFile(ControllerProtocolContext.CERT_CT_FILE_PATH);
    }

    public void DecryptAndSendChallengeAnswer(String encryptedChallenge) {
        String answer = this.DecryptChallenge(encryptedChallenge);

        if (answer != null) {
            this.SendDecryptedChallenge(answer);
        }
    }

    public void SendDecryptedChallenge(String decryptedChallenge) {
        JSONObject contents = new JSONObject();
        contents.put(MessageField.DECRYPTED_CHALLENGE.Value(), decryptedChallenge);
        this.SendMessageToKeyVault(MessageType.CHALLENGE_ANSWER, contents);
    }

    public String DecryptChallenge(String encryptedChallenge) {
        try {
            byte[] cipher = Common.StringToByteArray(encryptedChallenge);
            byte[] text = Crypto.DecryptTPM(ControllerProtocolContext.SK_CT_FILE_PATH, cipher);
            return Common.ByteArrayToString(text);
        } catch (Exception e) {
            logger.error("Failed to decrypt challenge");
            return null;
        }
    }

    public void SendJoinRequest() {
        JSONObject contents = new JSONObject();
        contents.put(MessageField.CERT_CT.Value(), this.GetCertControllerAsString());
        this.SendMessageToKeyVault(MessageType.JOIN_REQUEST, contents);
    }

    public String GetCertControllerAsString() {
        return Common.ByteArrayToString(this.certController);
    }

    public void SetKeyVaultIpAddress(String address) {
        this.keyVaultIpAddress = address;
    }

    public void SendMessageToKeyVault(MessageType type, JSONObject contents) {
        this.SendMessage(this.keyVaultIpAddress, type, contents);
    }

    public void SendMessageToKeyVault(MessageType type) {
        this.SendMessageToKeyVault(type, new JSONObject());
    }
}