package org.mockup.key_vault.protocol;

import org.json.JSONObject;

public class RekeyPendingState extends KeyVaultProtocolState {

    public RekeyPendingState() {
        super(30);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
    }

    @Override
    public void OnStart() {
    }

    @Override
    public void OnTimeout() {
        this.GetContext().GoToNext(new ReceiveSigningRequestState());
    }
}
