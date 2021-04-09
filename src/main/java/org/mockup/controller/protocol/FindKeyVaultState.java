package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.discovery.KeyVaultDiscovery;
import org.mockup.common.protocol.MessageType;

public class FindKeyVaultState extends ControllerProtocolState {
    public FindKeyVaultState() {
        super(5, MessageType.KEY_VAULT_DISCOVERY_REPLY);
    }

    @Override
    public void OnMessageReceived(JSONObject message) {
        System.out.println("something");
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
