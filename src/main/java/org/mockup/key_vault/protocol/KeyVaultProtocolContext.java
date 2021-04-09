package org.mockup.key_vault.protocol;

import java.io.IOException;

import javax.swing.event.ChangeListener;
import javax.xml.soap.MessageFactory;

import org.json.JSONObject;
import org.mockup.common.Common;
import org.mockup.common.communication.Sender;
import org.mockup.common.crypto.Crypto;
import org.mockup.common.protocol.IContextTerminatedCallback;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;
import org.mockup.common.protocol.ProtocolContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyVaultProtocolContext extends ProtocolContext {
    public static final String CERT_CA_FILE_PATH = "store/cert_ca";
    public static final String SK_KV_FILE_PATH = "store/sk_kv";
    public static final String PK_KV_FILE_PATH = "store/pk_kv";
    public static final String CERT_KV_FILE_PATH = "store/cert_kv";
    public static final String CERT_M_FILE_PATH = "store/cert_m";

    private final String controllerAddress;
    private final byte[] certKeyVault;

    private final Logger logger = LoggerFactory.getLogger(KeyVaultProtocolContext.class);

    private byte[] controllerCertificate;
    private String challengeString;

    public KeyVaultProtocolContext(String controllerAddress, byte[] associatedId, Sender sender,
            IContextTerminatedCallback terminatedCallback) throws IOException {
        super(associatedId, sender, terminatedCallback);
        this.controllerAddress = controllerAddress;
        this.certKeyVault = Common.ReadFromFile(KeyVaultProtocolContext.CERT_KV_FILE_PATH);
    }

    public Boolean CheckChallengeAnswer(String answer) {
        return answer.equals(this.challengeString);
    }

    public void GenerateStashEncryptAndSendChallenge() {
        byte[] challenge = this.GenerateChallenge();

        if (challenge == null) {
            return;
        }

        this.challengeString = Common.ByteArrayToString(challenge);
        byte[] encryptedChallenge = this.EncryptChallenge(challenge);

        if (encryptedChallenge == null) {
            return;
        }

        this.SendEncryptedChallenge(encryptedChallenge);
    }

    public void SendEncryptedChallenge(byte[] encryptedChallenge) {
        String encryptedChallengeString = Common.ByteArrayToString(encryptedChallenge);
        JSONObject contents = new JSONObject();
        contents.put(MessageField.ENCRYPTED_CHALLENGE.Value(), encryptedChallengeString);
        this.SendMessageToController(MessageType.CHALLENGE_SUBMISSION, contents);
    }

    public byte[] EncryptChallenge(byte[] challenge) {
        try {
            return Crypto.Encrypt(this.GetControllerCertificate(), challenge);
        } catch (Exception e) {
            logger.error("Failed to encrypt challenge.");
            return null;
        }
    }

    public byte[] GenerateChallenge() {
        try {
            return Crypto.GenerateRandomByteArray();
        } catch (Exception e) {
            logger.error("Failed to generate challenge.");
            return null;
        }
    }

    public Boolean CheckControllerCertificate(String certificateString) {
        /*
         * TODO: does it make sense to check that controller id matches the one
         * presented in the certificate?
         */
        byte[] certificate = Common.StringToByteArray(certificateString);
        try {
            return Crypto.IsCertificateValid(certificate, KeyVaultProtocolContext.CERT_M_FILE_PATH);
        } catch (Exception e) {
            logger.error("Failed to validate controller certificate");
            return false;
        }
    }

    public void SaveControllerCertificate(String certificateString) {
        this.controllerCertificate = Common.StringToByteArray(certificateString);
    }

    public byte[] GetControllerCertificate() {
        return this.controllerCertificate;
    }

    public void SendMessageToController(MessageType type, JSONObject contents) {
        this.SendMessage(this.controllerAddress, type, contents);
    }

    public void SendMessageToController(MessageType type) {
        this.SendMessage(this.controllerAddress, type, new JSONObject());
    }
}
