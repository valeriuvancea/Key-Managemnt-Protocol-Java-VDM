package org.mockup.common.protocol;

import org.json.JSONObject;

public abstract class ProtocolState {
    private final int timeoutS;
    private final MessageType[] expectedTypes;
    private ProtocolContext context;

    public ProtocolState(int timeoutS, MessageType... expectedTypes) {
        this.timeoutS = timeoutS;
        this.expectedTypes = expectedTypes;
    }

    public MessageType[] GetExpectedTypes() {
        return expectedTypes;
    }

    public int GetTimeoutS() {
        return this.timeoutS;
    }

    public void SetContext(ProtocolContext context) {
        this.context = context;
    }

    public abstract void OnMessageReceived(String senderIpAddress, JSONObject message);

    public abstract void OnStart();

    public abstract void OnTimeout();

    protected ProtocolContext GetContext() {
        return this.context;
    }
}
