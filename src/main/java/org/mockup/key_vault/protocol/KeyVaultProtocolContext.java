package org.mockup.key_vault.protocol;

import org.mockup.common.communication.Sender;
import org.mockup.common.protocol.IContextTerminatedCallback;
import org.mockup.common.protocol.ProtocolContext;

public class KeyVaultProtocolContext extends ProtocolContext {

    public KeyVaultProtocolContext(String associatedIdString, Sender sender,
            IContextTerminatedCallback terminatedCallback) {
        super(associatedIdString, sender, terminatedCallback);
        // TODO Auto-generated constructor stub
    }

}
