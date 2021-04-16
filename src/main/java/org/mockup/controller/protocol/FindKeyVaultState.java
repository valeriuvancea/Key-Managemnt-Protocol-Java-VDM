package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.discovery.KeyVaultDiscovery;
import org.mockup.common.protocol.MessageType;

public class FindKeyVaultState extends ControllerProtocolState {
    public FindKeyVaultState() {
        super(10, MessageType.KEY_VAULT_DISCOVERY_REPLY);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        this.GetContext().SetKeyVaultIpAddress(senderIpAddress);
        this.GetContext().GoToNext(new SendJoinRequestState());
    }

    @Override
    public void OnStart() {
        KeyVaultDiscovery.BroadcastDiscoveryRequest(this.GetContext().GetAssociateIdString());
    }

    @Override
    public void OnTimeout() {
        this.GetContext().GoToNext(this);
    }
}
