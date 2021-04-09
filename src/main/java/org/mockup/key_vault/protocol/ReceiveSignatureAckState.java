package org.mockup.key_vault.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class ReceiveSignatureAckState extends KeyVaultProtocolState {
    private String controllerEffectiveKeyString;

    public ReceiveSignatureAckState(String controllerEffectiveKeyString) {
        super(9, MessageType.SIGNING_ACK);
        this.controllerEffectiveKeyString = controllerEffectiveKeyString;
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
