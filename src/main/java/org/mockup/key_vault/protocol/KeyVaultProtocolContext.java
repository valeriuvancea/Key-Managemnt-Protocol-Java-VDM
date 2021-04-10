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
    public static final String SK_CA_FILE_PATH = "store/sk_ca";
    public static final String PK_CA_FILE_PATH = "store/pk_ca";
    public static final String CERT_KV_FILE_PATH = "store/cert_kv";
    public static final String CERT_M_FILE_PATH = "store/cert_m";

    private final String controllerAddress;
    private final String keyVaultCertificateString;
    private final String caCertificateString;

    private final Logger logger = LoggerFactory.getLogger(KeyVaultProtocolContext.class);

    private byte[] controllerCertificate;
    private byte[] controllerEffectiveCertificate;
    private String challengeString;
    private Boolean hasJoined;

    public KeyVaultProtocolContext(String controllerAddress, byte[] associatedId, Sender sender,
            IContextTerminatedCallback terminatedCallback) throws IOException {
        super(associatedId, sender, terminatedCallback);
        this.controllerAddress = controllerAddress;
        this.keyVaultCertificateString = Common
                .ByteArrayToString(Common.ReadFromFile(KeyVaultProtocolContext.CERT_KV_FILE_PATH));
        this.caCertificateString = Common
                .ByteArrayToString(Common.ReadFromFile(KeyVaultProtocolContext.CERT_CA_FILE_PATH));
        this.hasJoined = false;
    }

    public Boolean HasJoined() {
        return this.hasJoined;
    }

    public void SendRekeyRequest() {
        this.SendMessageToController(MessageType.RE_KEY_REQUEST);
    }

    public void SaveEffectiveCertificate(String effectiveCertificate) {
        this.controllerEffectiveCertificate = Common.StringToByteArray(effectiveCertificate);
        this.hasJoined = true;
    }

    public String GenerateAndSendEffectiveCertificate(String effectiveKeyString) {
        String certificate = this.GenerateEffectiveCertificate(effectiveKeyString);

        if (certificate == null) {
            return "";
        }

        String hash = this.GetEffectiveCertificateSignature(this.GetAssociateIdString(), certificate,
                this.caCertificateString);

        if (hash == null) {
            return "";
        }

        this.SendEffectiveCertificate(certificate, this.caCertificateString, hash);

        return certificate;
    }

    public void SendEffectiveCertificate(String effectiveCertificateString, String caCertificateString, String hash) {
        JSONObject contents = new JSONObject();
        contents.put(MessageField.CERT_EFF.Value(), effectiveCertificateString);
        contents.put(MessageField.CERT_CA.Value(), caCertificateString);
        contents.put(MessageField.HASH.Value(), hash);
        this.SendMessageToController(MessageType.SIGNING_REPLY, contents);
    }

    public String GetEffectiveCertificateSignature(String controllerIdString, String effectiveCertificateString,
            String caCertificateString) {
        String dataString = controllerIdString.concat(effectiveCertificateString).concat(caCertificateString);
        byte[] data = Common.StringToByteArray(dataString);
        try {
            byte[] signature = Crypto.Sign(KeyVaultProtocolContext.SK_KV_FILE_PATH, data);
            return Common.ByteArrayToString(signature);
        } catch (Exception e) {
            logger.error("Failed to generate signature for signing reply.");
            return null;
        }
    }

    public String GenerateEffectiveCertificate(String effectiveKeyString) {
        byte[] effectiveKey = Common.StringToByteArray(effectiveKeyString);
        try {
            byte[] certificate = Crypto.GenerateCertificate(KeyVaultProtocolContext.CERT_CA_FILE_PATH,
                    KeyVaultProtocolContext.SK_CA_FILE_PATH, effectiveKey, this.GetAssociateIdString());
            return Common.ByteArrayToString(certificate);
        } catch (Exception e) {
            logger.error("Failed to generate controller effective key certificate");
            return null;
        }
    }

    public Boolean CheckSigningRequestSignature(String controllerIdString, String keyString, String expectedSignature) {
        byte[] certificate = this.controllerEffectiveCertificate;
        if (!this.HasJoined()) {
            certificate = this.controllerCertificate;
        }

        return this.CheckSigningRequestSignature(controllerIdString, keyString, certificate, expectedSignature);
    }

    public Boolean CheckSigningRequestSignature(String controllerIdString, String keyString, byte[] signingCertificate,
            String expectedSignature) {
        String dataString = controllerIdString.concat(keyString);
        byte[] data = Common.StringToByteArray(dataString);
        byte[] signature = Common.StringToByteArray(expectedSignature);

        try {
            return Crypto.IsSignatureValid(signingCertificate, data, signature);
        } catch (Exception e) {
            logger.error("Failed to verify signing request signature.");
            return false;
        }
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
        this.SendMessageToController(MessageType.CHALLENGE_ANSWER, contents);
    }

    public String DecryptChallenge(String encryptedChallenge) {
        try {
            byte[] cipher = Common.StringToByteArray(encryptedChallenge);
            byte[] text = Crypto.Decrypt(KeyVaultProtocolContext.SK_KV_FILE_PATH, cipher);
            return Common.ByteArrayToString(text);
        } catch (Exception e) {
            logger.error("Failed to decrypt challenge");
            return null;
        }
    }

    public Boolean CheckChallengeAnswer(String answer) {
        return answer.equals(this.challengeString);
    }

    public void SendKeyVaultCertificate() {
        JSONObject contents = new JSONObject();
        contents.put(MessageField.CERT_KV.Value(), this.keyVaultCertificateString);
        this.SendMessageToController(MessageType.KEY_VAULT_CERTIFICATE, contents);
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
