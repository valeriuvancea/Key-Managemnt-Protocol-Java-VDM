package org.mockup.controller.protocol;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;
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
import org.vdm.annotations.VDMOperation;

public class ControllerProtocolContext extends ProtocolContext {
    public static final String CERT_M_FILE_PATH = "store/cert_m";
    public static final String ID_FILE_PATH = "store/id";
    public static final String SK_M_FILE_PATH = "store/sk_m";
    public static final String SK_CT_FILE_PATH = "store/sk_ct";
    public static final String PK_CT_FILE_PATH = "store/pk_ct";
    public static final String SK_EFF_PENDING_FILE_PATH = "store/sk_eff_pending";
    public static final String PK_EFF_PENDING_FILE_PATH = "store/pk_eff_pending";
    public static final String SK_EFF_FILE_PATH = "store/sk_eff";
    public static final String PK_EFF_FILE_PATH = "store/pk_eff";
    public static final String CERT_EFF_FILE_PATH = "store/cert_eff";
    public static final String CERT_CT_FILE_PATH = "store/cert_ct";

    private final byte[] certController;
    private final Logger logger = LoggerFactory.getLogger(ControllerProtocolContext.class);

    private String challengeString;
    private String keyVaultIpAddress;
    private byte[] keyVaultCertificate;
    private byte[] otherControllerEffectiveCertificate;
    private String otherControllerIpAddress;
    private String otherControllerIdString;
    private boolean otherControllerFound;
    private AtomicReference<String> effectiveCertificateString;
    private AtomicBoolean hasJoined;

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
        this.hasJoined = new AtomicBoolean(false);
        this.effectiveCertificateString = new AtomicReference<>();

        this.otherControllerIdString = null;
        this.otherControllerIpAddress = null;
        this.otherControllerEffectiveCertificate = null;
        this.otherControllerFound = false;
    }

    public void UpdateEffectiveCertificateToOtherController() {
        this.SendEffectiveCertificateToOtherController(this.GetEffectiveCertificateString());
    }

    public void SendEffectiveCertificateToOtherController(String effectiveCertificateString) {
        JSONObject contents = new JSONObject();
        contents.put(MessageField.CERT_EFF.Value(), effectiveCertificateString);
        this.SendMessageToOtherController(MessageType.CONTROLLER_CERTIFICATE_UPDATE, contents);
    }

    public void SaveOtherControllerInformation(String ipAddress, String idString, String certificateString) {
        this.otherControllerFound = true;
        this.otherControllerIdString = idString;
        this.otherControllerIpAddress = ipAddress;
        this.otherControllerEffectiveCertificate = Common.StringToByteArray(certificateString);
    }

    public void EncryptAndSendToOtherController(String messageString) {
        String cipher = this.EncryptMessageToOtherController(messageString);

        if (cipher == null) {
            return;
        }

        JSONObject contents = new JSONObject();
        contents.put(MessageField.ENCRYPTED_DATA.Value(), cipher);
        this.SendMessageToOtherController(MessageType.DUMMY_MESSAGE, contents);
    }

    public Boolean HasFoundOtherController() {
        return this.otherControllerFound;
    }

    public String DecryptMessageFromOtherController(String messageString) {
        byte[] cipher = Common.StringToByteArray(messageString);
        byte[] text;
        try {
            text = Crypto.DecryptTPM(ControllerProtocolContext.SK_EFF_FILE_PATH, cipher);
            return Common.ByteArrayToString(text);
        } catch (Exception e) {
            logger.error("Failed to decrypt message form other controller.");
            return null;
        }
    }

    public String EncryptMessageToOtherController(String messageString) {
        byte[] messageBytes = Common.StringToByteArray(messageString);

        try {
            byte[] cipherBytes = Crypto.Encrypt(this.otherControllerEffectiveCertificate, messageBytes);
            return Common.ByteArrayToString(cipherBytes);
        } catch (Exception e) {
            logger.error("Failed to encrypt message to other controller.");
            return null;
        }
    }

    public boolean HasJoined() {
        return this.hasJoined.get();
    }

    public String GetEffectiveCertificateString() {
        return this.effectiveCertificateString.get();
    }

    public void SendSignatureAck() {
        this.SendMessageToKeyVault(MessageType.SIGNING_ACK);
    }

    public void SaveEffectiveKeys(String effectiveCertificateString) {
        try {
            this.effectiveCertificateString.set(effectiveCertificateString);
            byte[] effectiveCertificate = Common.StringToByteArray(effectiveCertificateString);
            Common.RenameFile(ControllerProtocolContext.PK_EFF_PENDING_FILE_PATH,
                    ControllerProtocolContext.PK_EFF_FILE_PATH);
            Common.RenameFile(ControllerProtocolContext.SK_EFF_PENDING_FILE_PATH,
                    ControllerProtocolContext.SK_EFF_FILE_PATH);
            Common.WriteToFile(effectiveCertificate, ControllerProtocolContext.CERT_EFF_FILE_PATH);
            this.hasJoined.set(true);
        } catch (Exception e) {
            logger.error("Failed to save new effective keys and certificate.");
        }
    }

    public Boolean CheckSigningReplySignature(String controllerIdString, String effectiveCertificateString,
            String caCertificateString, String expectedSignatureString) {
        byte[] expectedSignature = Common.StringToByteArray(expectedSignatureString);
        String dataString = controllerIdString.concat(effectiveCertificateString).concat(caCertificateString);
        byte[] data = Common.StringToByteArray(dataString);

        try {
            return Crypto.IsSignatureValid(this.keyVaultCertificate, data, expectedSignature);
        } catch (Exception e) {
            logger.error("Failed to verify signing request reply.");
            return false;
        }
    }

    public void GenerateAndSendSigningRequest() {
        String key = this.GenerateEffectivePendingKeys();

        if (key == null) {
            return;
        }

        String signingKey = ControllerProtocolContext.SK_EFF_FILE_PATH;
        if (!this.HasJoined()) {
            signingKey = ControllerProtocolContext.SK_CT_FILE_PATH;
        }

        String hash = this.GetSigningRequestSignature(this.GetAssociateIdString(), key, signingKey);

        if (hash == null) {
            return;
        }

        this.SendSigningRequest(key, hash);
    }

    public void SendSigningRequest(String key, String hash) {
        JSONObject contents = new JSONObject();
        contents.put(MessageField.PK_EFF.Value(), key);
        contents.put(MessageField.HASH.Value(), hash);
        this.SendMessageToKeyVault(MessageType.SIGNING_REQUEST, contents);
    }

    public String GetSigningRequestSignature(String controllerIdString, String keyString, String signingKeyPath) {
        String dataString = controllerIdString.concat(keyString);
        byte[] data = Common.StringToByteArray(dataString);
        try {
            byte[] sign = Crypto.SignTPM(signingKeyPath, data);
            return Common.ByteArrayToString(sign);
        } catch (Exception e) {
            logger.error("Failed to generate signature for signing request TPM.");
            return null;
        }
    }

    public String GenerateEffectivePendingKeys() {
        try {
            Pair<byte[], byte[]> keys = Crypto.GenerateKeyPairTPM(ControllerProtocolContext.PK_EFF_PENDING_FILE_PATH,
                    ControllerProtocolContext.SK_EFF_PENDING_FILE_PATH);
            return Common.ByteArrayToString(keys.getValue0());
        } catch (Exception e) {
            logger.error("Failed to generate controller effective pending keys TPM");
            return null;
        }
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
        this.SendMessageToKeyVault(MessageType.CHALLENGE_SUBMISSION, contents);
    }

    public byte[] EncryptChallenge(byte[] challenge) {
        try {
            return Crypto.Encrypt(this.GetKeyVaultCertificate(), challenge);
        } catch (Exception e) {
            logger.error("Failed to encrypt challenge.");
            return null;
        }
    }

    @VDMOperation(postCondition = "len RESULT=128")
    public byte[] GenerateChallenge() {
        try {
            return Crypto.GenerateRandomByteArrayTPM();
        } catch (Exception e) {
            logger.error("Failed to generate challenge TPM.");
            return null;
        }
    }

    @VDMOperation(postCondition = "RESULT=true")
    public Boolean CheckKeyVaultCertificate(String certificateString) {
        byte[] certificate = Common.StringToByteArray(certificateString);

        try {
            return Crypto.IsCertificateValid(certificate, ControllerProtocolContext.CERT_M_FILE_PATH);
        } catch (Exception e) {
            logger.error("Failed to validate key vault certificate");
            return false;
        }
    }

    public byte[] GetKeyVaultCertificate() {
        return this.keyVaultCertificate;
    }

    public void SaveKeyVaultCertificate(String certificateString) {
        this.keyVaultCertificate = Common.StringToByteArray(certificateString);
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
            logger.error("Failed to decrypt challenge TPM");
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

    public void SendMessageToOtherController(MessageType type, JSONObject contents) {
        contents.put(MessageField.CONTROLLER_ID.Value(), this.otherControllerIdString);
        contents.put(MessageField.SENDER_ID.Value(), this.GetAssociateIdString());
        contents.put(MessageField.TYPE.Value(), type.Value());
        this.sender.SendMessage(this.otherControllerIpAddress, contents);
    }

    public void SendMessageToKeyVault(MessageType type, JSONObject contents) {
        this.SendMessage(this.keyVaultIpAddress, type, contents);
    }

    public void SendMessageToKeyVault(MessageType type) {
        this.SendMessageToKeyVault(type, new JSONObject());
    }
}
