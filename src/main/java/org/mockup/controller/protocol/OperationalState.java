package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageType;

public class OperationalState extends ControllerProtocolState {
    public OperationalState() {
        super(0, MessageType.RE_KEY_REQUEST);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        this.GetContext().GoToNext(new SendSigningRequestState());
    }

    @Override
    public void OnStart() {

    }

    @Override
    public void OnTimeout() {

    }
}
