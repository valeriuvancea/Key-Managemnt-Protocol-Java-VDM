package org.mockup.common;

import java.util.HashMap;
import java.util.Map;

public enum MessageType {
    KEY_VAULT_DISCOVERY_REQUEST("BroadcastToSearchKeyVault"),
    KEY_VAULT_DISCOVERY_REPLY("KeyVaultSearchBroadcastAcknowledgement"), JOIN_REQUEST("JoinRequest"),
    CHALLENGE_SUBMISSION("SendChallenge"), CHALLENGE_ANSWER("SendChallengeAnswer"),
    KEY_VAULT_CERTIFICATE("SendKeyVaultCertificate"), SIGNING_REQUEST("SendNewEffectivePublicKey"),
    SIGNING_REPLY("SendNewEffectiveCertificate"), SIGNING_ACK("SendNewEffectiveCertificateAcknoledgement"),
    RE_KEY_REQUEST("SendReKeyRequest"), CONTROLLER_DISCOVERY_REQUEST("BroadcastToSearchControllers"),
    CONTROLLER_DISCOVERY_REPLY("ControllerSearchBroadcastAcknowledgement"),
    CONTROLLER_CERTIFICATE_UPDATE("ControllerCertificateUpdate"), DUMMY_MESSAGE("DummyMessage"), UNKNOWN("Unknown");

    private static final Map<String, MessageType> BY_VALUE = new HashMap<String, MessageType>();

    static {
        for (MessageType e : values()) {
            BY_VALUE.put(e.Value(), e);
        }
    }

    private final String text;

    MessageType(final String text) {
        this.text = text;
    }

    public static MessageType ByValue(String value) {
        if (BY_VALUE.containsKey(value)) {
            return BY_VALUE.get(value);
        }

        return MessageType.UNKNOWN;
    }

    public String Value() {
        return this.text;
    }
}
