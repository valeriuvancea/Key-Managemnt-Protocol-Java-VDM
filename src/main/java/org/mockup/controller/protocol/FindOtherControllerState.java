package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageType;

public class FindOtherControllerState extends OperationalState {
    public FindOtherControllerState() {
        super(0, MessageType.CONTROLLER_DISCOVERY_REPLY);
    }

    @Override
    public void OnStart() {

    }

    @Override
    public void OnTimeout() {

    }

    @Override
    public void OnOperationalMessageReceived(String senderIpAddress, JSONObject message) {

    }
}
