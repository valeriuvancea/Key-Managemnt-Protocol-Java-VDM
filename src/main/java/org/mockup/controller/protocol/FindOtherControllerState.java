package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.discovery.ControllerDiscovery;
import org.mockup.common.protocol.MessageField;
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
        this.GetContext().GoToNext(this);
    }

    @Override
    public void OnOperationalMessageReceived(String senderIpAddress, JSONObject message) {
        String idString = message.getString(MessageField.SENDER_ID.Value());
        String certificateString = message.getString(MessageField.CERT_EFF.Value());
        this.GetContext().SaveOtherControllerInformation(senderIpAddress, idString, certificateString);
        this.GetContext().GoToNext(new DummyCommunicationState());
    }
}
