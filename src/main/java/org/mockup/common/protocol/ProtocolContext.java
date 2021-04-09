package org.mockup.common.protocol;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.mockup.common.Common;
import org.mockup.common.communication.IReceiverCallback;
import org.mockup.common.communication.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolContext implements IReceiverCallback {
    private final Logger logger = LoggerFactory.getLogger(ProtocolContext.class);
    private final Timer timeoutTimer;
    private final IContextTerminatedCallback terminatedCallback;

    protected final byte[] associatedId;
    protected final String associatedIdString;
    protected final Sender sender;

    private ProtocolState currentState;
    private TimeoutTask timeoutTask;

    public ProtocolContext(byte[] associatedId, Sender sender, IContextTerminatedCallback terminatedCallback) {
        this.currentState = null;
        this.timeoutTimer = new Timer();
        this.terminatedCallback = terminatedCallback;
        this.sender = sender;
        this.associatedId = associatedId;
        this.associatedIdString = Common.ByteArrayToString(associatedId);
    }

    public String GetAssociateIdString() {
        return this.associatedIdString;
    }

    public void Start(ProtocolState startState) {
        if (currentState != null) {
            return;
        }

        this.StartNewState(startState);
    }

    public void Stop() {
        if (currentState == null) {
            return;
        }

        this.StopCurrentState();
    }

    public void Terminate() {
        this.Stop();
        this.terminatedCallback.HandleContextTerminated(this.associatedIdString);
    }

    public void GoToNext(ProtocolState state) {
        if (currentState == null) {
            return;
        }

        this.StopCurrentState();
        this.StartNewState(state);
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
                logger.error("Received message {} could not be handled by state {}", type,
                        this.currentState.getClass());
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
            this.currentState.OnMessageReceived(senderIpAddress, contents);
        }
    }

    private void StopCurrentState() {
        synchronized (this) {
            logger.debug("{} associated state {} stopped.", this.associatedIdString,
                    this.currentState.getClass().getName());
            this.timeoutTask.cancel();
            this.currentState = null;
        }
    }

    private void StartNewState(ProtocolState state) {
        synchronized (this) {
            this.currentState = state;
            logger.debug("{} associated state {} starting.", this.associatedIdString,
                    this.currentState.getClass().getName());

            this.currentState.SetContext(this);
            this.currentState.OnStart();
            long timeoutS = this.currentState.GetTimeoutS();
            if (timeoutS > 0) {
                this.timeoutTask = new TimeoutTask();
                this.timeoutTimer.schedule(this.timeoutTask, timeoutS * 1000);
            }
        }
    }

    protected void SendMessage(String ipAddress, MessageType messageType, JSONObject message) {
        message.put(MessageField.CONTROLLER_ID.Value(), this.associatedIdString);
        message.put(MessageField.TYPE.Value(), messageType.Value());
        this.sender.SendMessage(ipAddress, message);
    }

    private class TimeoutTask extends TimerTask {
        private final AtomicBoolean cancelledFlag;

        public TimeoutTask() {
            this.cancelledFlag = new AtomicBoolean(false);
        }

        @Override
        public boolean cancel() {
            this.cancelledFlag.set(true);
            return super.cancel();
        }

        @Override
        public void run() {
            synchronized (ProtocolContext.this) {
                if (this.cancelledFlag.get()) {
                    return;
                }

                logger.debug("{} associated state {} has timed out.", ProtocolContext.this.associatedIdString,
                        ProtocolContext.this.currentState.getClass().getName());

                ProtocolContext.this.currentState.OnTimeout();
            }
        }
    }
}
