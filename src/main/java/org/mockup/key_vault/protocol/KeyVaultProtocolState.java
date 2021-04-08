package org.mockup.key_vault.protocol;

import org.mockup.common.protocol.MessageType;
import org.mockup.common.protocol.ProtocolState;

public abstract class KeyVaultProtocolState extends ProtocolState {
    public KeyVaultProtocolState(int timeoutS, MessageType... expectedTypes) {
        super(timeoutS, expectedTypes);
        // TODO Auto-generated constructor stub
    }

    public KeyVaultProtocolContext GetContext() {
        return (KeyVaultProtocolContext) super.GetContext();
    }
}
