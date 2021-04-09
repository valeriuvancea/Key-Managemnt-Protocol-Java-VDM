package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class SendSigningRequestState extends ControllerProtocolState {
    private Boolean first;

    public SendSigningRequestState(Boolean first) {
        super(9, MessageType.SIGNING_REPLY);
        this.first = first;
    }

    public SendSigningRequestState() {
        super(9, MessageType.SIGNING_REPLY);
        this.first = false;
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {

    }

    @Override
    public void OnStart() {
        this.GetContext().GenerateAndSendSigningRequest(this.first);
    }

    @Override
    public void OnTimeout() {
        this.GetContext().Terminate();
    }
}
