package org.mockup.common.protocol;

import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.mockup.common.communication.IReceiverCallback;
import org.mockup.common.communication.Sender;

public class ProtocolContext extends TimerTask implements IReceiverCallback {
    private final Timer timeoutTimer;
    private final IContextTerminatedCallback terminatedCallback;
    private final String associatedIdString;
    private final Sender sender;

    private ProtocolState currentState;

    public ProtocolContext(String associatedIdString, Sender sender, IContextTerminatedCallback terminatedCallback) {
        this.currentState = null;
        this.timeoutTimer = new Timer();
        this.terminatedCallback = terminatedCallback;
        this.associatedIdString = associatedIdString;
        this.sender = sender;
    }

    public void Start(ProtocolState state) {
        this.StopCurrentState();

        this.currentState = state;
        this.currentState.SetContext(this);
        this.currentState.OnStart();

        long timeoutS = this.currentState.GetTimeoutS();
        if (timeoutS > 0) {
            this.timeoutTimer.schedule(this, timeoutS * 1000);
        }
    }

    public void Stop() {
        this.StopCurrentState();
        this.terminatedCallback.HandleContextTerminated();
    }

    @Override
    public synchronized void HandleMessage(String senderIpAddress, JSONObject contents) {
        synchronized (this) {
            if (this.currentState == null) {
                return;
            }

            /* Make sure message type can be handled by the current state. */
            MessageType type = MessageType.ByValue(contents.optString(MessageField.TYPE.Value()));
            if (!Stream.of(this.currentState.GetExpectedTypes()).anyMatch(x -> x == type)) {
                return;
            }

            /* Make sure the message contains all expected fields. */
            MessageField[] expectedFields = MessageField.GetExpected(type);
            for (MessageField field : expectedFields) {
                if (!contents.has(field.Value())) {
                    return;
                }
            }

            /* Make sure the message is associated with the correct id. */
            String idString = contents.getString(MessageField.CONTROLLER_ID.Value());
            if (!this.associatedIdString.equals(idString)) {
                return;
            }

            /* Pass the message to the current state to be handled. */
            this.currentState.OnMessageReceived(contents);
        }
    }

    @Override
    public void run() {
        ProtocolState timedOutState = this.currentState;

        synchronized (this) {
            if (this.currentState != timedOutState) {
                return;
            }

            this.currentState.OnTimeout();
        }
    }

    private void StopCurrentState() {
        if (this.currentState == null) {
            return;
        }

        this.currentState = null;
        this.timeoutTimer.cancel();
    }

    private void SendMessage(String ipAddress, MessageType messageType, JSONObject message) {
        message.put(MessageField.CONTROLLER_ID.Value(), this.associatedIdString);
        message.put(MessageField.TYPE.Value(), messageType.Value());
        this.sender.SendMessage(ipAddress, message);
    }
}
