package org.mockup.controller.protocol;

import org.mockup.common.communication.Sender;
import org.mockup.common.protocol.IContextTerminatedCallback;
import org.mockup.common.protocol.ProtocolContext;

public class ControllerProtocolContext extends ProtocolContext {

    public ControllerProtocolContext(String associatedIdString, Sender sender,
            IContextTerminatedCallback terminatedCallback) {
        super(associatedIdString, sender, terminatedCallback);
        // TODO Auto-generated constructor stub
    }

}
