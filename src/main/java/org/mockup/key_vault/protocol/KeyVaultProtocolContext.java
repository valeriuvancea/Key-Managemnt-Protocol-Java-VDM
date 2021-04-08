package org.mockup.key_vault.protocol;

import org.mockup.common.communication.Sender;
import org.mockup.common.protocol.IContextTerminatedCallback;
import org.mockup.common.protocol.ProtocolContext;

public class KeyVaultProtocolContext extends ProtocolContext {

    public KeyVaultProtocolContext(byte[] associatedId, Sender sender, IContextTerminatedCallback terminatedCallback) {
        super(associatedId, sender, terminatedCallback);
    }
}
