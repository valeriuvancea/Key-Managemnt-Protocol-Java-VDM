package org.mockup.common;

public enum MessageType {
    KEY_VAULT_DISCOVERY_REQUEST("BroadcastToSearchKeyVault"),
    KEY_VAULT_DISCOVERY_REPLY("KeyVaultSearchBroadcastAcknowledgement"), JOIN_REQUEST("JoinRequest"),
    CHALLENGE_SUBMISSION("SendChallenge"), CHALLENGE_ANSWER("SendChallengeAnswer"),
    KEY_VAULT_CERTIFICATE("SendKeyVaultCertificate"), SIGNING_REQUEST("SendNewEffectivePublicKey"),
    SIGNING_REPLY("SendNewEffectiveCertificate"), SIGNING_ACK("SendNewEffectiveCertificateAcknoledgement"),
    RE_KEY_REQUEST("SendReKeyRequest"), CONTROLLER_DISCOVERY_REQUEST("BroadcastToSearchControllers"),
    CONTROLLER_DISCOVERY_REPLY("ControllerSearchBroadcastAcknowledgement"),
    CONTROLLER_CERTIFICATE_UPDATE("ControllerCertificateUpdate"), DUMMY_MESSAGE("DummyMessage");

    private final String text;

    MessageType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
