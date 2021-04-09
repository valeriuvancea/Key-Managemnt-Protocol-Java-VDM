package org.mockup.key_vault.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageType;

public class ReceiveJoinRequestState extends KeyVaultProtocolState {

    public ReceiveJoinRequestState() {
        super(5, MessageType.JOIN_REQUEST);
    }

    @Override
    public void OnMessageReceived(JSONObject message) {

    }

    @Override
    public void OnStart() {
        this.GetContext().SendMessageToController(MessageType.KEY_VAULT_DISCOVERY_REPLY);
    }

    @Override
    public void OnTimeout() {
        this.GetContext().Terminate();
    }
}
