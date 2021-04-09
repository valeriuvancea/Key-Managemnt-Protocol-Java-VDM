package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageType;

public class SendJoinRequestState extends ControllerProtocolState {
    public SendJoinRequestState() {
        super(9, MessageType.CHALLENGE_SUBMISSION);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        System.out.println("Received challenge");
    }

    @Override
    public void OnStart() {
        this.GetContext().SendJoinRequest();
    }

    @Override
    public void OnTimeout() {
        this.GetContext().Terminate();
    }
}
