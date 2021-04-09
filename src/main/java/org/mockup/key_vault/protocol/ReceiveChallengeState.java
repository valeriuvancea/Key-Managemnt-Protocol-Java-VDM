package org.mockup.key_vault.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class ReceiveChallengeState extends KeyVaultProtocolState {
    public ReceiveChallengeState() {
        super(9, MessageType.CHALLENGE_SUBMISSION);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {

    }

    @Override
    public void OnStart() {

    }

    @Override
    public void OnTimeout() {
        this.GetContext().Terminate();
    }
}
