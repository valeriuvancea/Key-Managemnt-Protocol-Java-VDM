package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.discovery.ControllerDiscovery;
import org.mockup.common.protocol.MessageType;

public class FindOtherControllerState extends OperationalState {
    public FindOtherControllerState() {
        super(3, MessageType.CONTROLLER_DISCOVERY_REPLY);
    }

    @Override
    public void OnStart() {
        ControllerDiscovery.BroadcastDiscoveryRequest(this.GetContext().GetAssociateIdString());
    }

    @Override
    public void OnTimeout() {

    }

    @Override
    public void OnOperationalMessageReceived(String senderIpAddress, JSONObject message) {
        System.out.println("Controller found.");
    }
}
