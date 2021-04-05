package org.mockup.common;

public enum MessageField {
    TYPE("type"), CONTROLLER_ID("controller_id"), CERT_CT("cert_ct"), CERT_KV("cert_kv"),
    ENCRYPTED_CHALLENGE("encrypted_challenge"), DECRYPTED_CHALLENGE("decrypted_challenge"), PK_EFF("pk_eff"),
    HASH("hash"), CERT_EFF("cert_eff"), CERT_CA("cert_ca"), SENDER_ID("sender_id"), ENCRYPTED_DATA("encrypted_data");

    private final String text;

    MessageField(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }

    public MessageField[] GetExpected(MessageType messageType) {
        switch (messageType) {
        case CONTROLLER_CERTIFICATE_UPDATE: {
            return new MessageField[] { TYPE, CONTROLLER_ID, SENDER_ID, CERT_EFF };
        }
        case DUMMY_MESSAGE: {
            return new MessageField[] { TYPE, CONTROLLER_ID, SENDER_ID, ENCRYPTED_DATA };
        }
        case CONTROLLER_DISCOVERY_REPLY: {
            return new MessageField[] { TYPE, CONTROLLER_ID, SENDER_ID, CERT_EFF };
        }
        case SIGNING_ACK: {
            return new MessageField[] { TYPE, CONTROLLER_ID };
        }
        case SIGNING_REPLY: {
            return new MessageField[] { TYPE, CONTROLLER_ID, CERT_EFF, CERT_CA };
        }
        case SIGNING_REQUEST: {
            return new MessageField[] { TYPE, CONTROLLER_ID, PK_EFF, HASH };
        }
        case KEY_VAULT_CERTIFICATE: {
            return new MessageField[] { TYPE, CONTROLLER_ID, CERT_KV };
        }
        case CHALLENGE_ANSWER: {
            return new MessageField[] { TYPE, CONTROLLER_ID, DECRYPTED_CHALLENGE };
        }
        case CHALLENGE_SUBMISSION: {
            return new MessageField[] { TYPE, CONTROLLER_ID, ENCRYPTED_CHALLENGE };
        }
        case JOIN_REQUEST: {
            return new MessageField[] { TYPE, CONTROLLER_ID, CERT_CT };
        }
        case KEY_VAULT_DISCOVERY_REQUEST: {
            /* Fall through: */
        }
        case KEY_VAULT_DISCOVERY_REPLY: {
            /* Fall through: */
        }
        case RE_KEY_REQUEST: {
            /* Fall through: */
        }
        case CONTROLLER_DISCOVERY_REQUEST: {
            /* Fall through: */
        }
        default: {
            return new MessageField[] { TYPE, CONTROLLER_ID };
        }

        }
    }
}
