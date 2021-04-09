package org.mockup.key_vault.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class ReceiveSigningRequest extends KeyVaultProtocolState {
    private String challenge;
    private Boolean first;

    public ReceiveSigningRequest(String challenge) {
        super(9, MessageType.SIGNING_REQUEST);
        this.challenge = challenge;
        this.first = true;
    }

    public ReceiveSigningRequest() {
        super(9, MessageType.SIGNING_REQUEST);
        this.challenge = null;
        this.first = false;
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        String controllerId = message.getString(MessageField.CONTROLLER_ID.Value());
        String key = message.getString(MessageField.PK_EFF.Value());
        String hash = message.getString(MessageField.HASH.Value());

        if (this.GetContext().CheckSigningRequestSignature(controllerId, key, hash, this.first)) {
            this.GetContext().GoToNext(new ReceiveSignatureAckState(key));
        } else {
            this.GetContext().Terminate();
        }
    }

    @Override
    public void OnStart() {
        if (this.challenge != null) {
            this.GetContext().DecryptAndSendChallengeAnswer(this.challenge);
        } else {
            /* Send re-key request */
        }
    }

    @Override
    public void OnTimeout() {
        this.GetContext().Terminate();
    }
}
