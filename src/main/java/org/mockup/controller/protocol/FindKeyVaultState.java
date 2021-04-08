package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.discovery.ControllerDiscovery;
import org.mockup.common.protocol.MessageType;

public class FindKeyVaultState extends ControllerProtocolState {
    public FindKeyVaultState() {
        super(5, MessageType.KEY_VAULT_DISCOVERY_REPLY);
    }

    @Override
    public void OnMessageReceived(JSONObject message) {
    }

    @Override
    public void OnStart() {
        ControllerDiscovery.BroadcastDiscoveryRequest(this.GetContext().GetAssociatedIdString());
    }

    @Override
    public void OnTimeout() {
        this.GetContext().GoToNext(this);
    }
}
