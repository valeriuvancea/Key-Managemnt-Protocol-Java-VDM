package org.mockup.controller.protocol;

import org.mockup.common.protocol.MessageType;
import org.mockup.common.protocol.ProtocolState;

public abstract class ControllerProtocolState extends ProtocolState {

    public ControllerProtocolState(int timeoutS, MessageType... expectedTypes) {
        super(timeoutS, expectedTypes);
    }

    public ControllerProtocolContext GetContext() {
        return (ControllerProtocolContext) super.GetContext();
    }
}
