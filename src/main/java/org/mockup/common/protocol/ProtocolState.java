package org.mockup.common.protocol;

import org.json.JSONObject;
import org.mockup.common.MessageType;

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

    public abstract void OnMessageReceived(JSONObject message);

    public abstract void OnStart();

    public abstract void OnTimeout();

    protected void Terminate() {
        if (this.context == null) {
            return;
        }

        this.context.Stop();
    }

    protected void GoToNext(ProtocolState nextState) {
        if (this.context == null) {
            return;
        }

        this.context.Start(nextState);
    }
}
