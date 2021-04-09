package org.mockup.key_vault.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class ReceiveJoinRequestState extends KeyVaultProtocolState {

    public ReceiveJoinRequestState() {
        super(15, MessageType.JOIN_REQUEST);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        String controllerCertString = message.getString(MessageField.CERT_CT.Value());

        if (this.GetContext().CheckControllerCertificate(controllerCertString)) {
            this.GetContext().SaveControllerCertificate(controllerCertString);
            this.GetContext().GoToNext(new ReceiveChallengeAnswerState());
        } else {
            this.GetContext().Terminate();
        }
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
