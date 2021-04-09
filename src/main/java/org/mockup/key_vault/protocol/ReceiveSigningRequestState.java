package org.mockup.key_vault.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class ReceiveSigningRequestState extends KeyVaultProtocolState {
    private String challenge;

    public ReceiveSigningRequestState(String challenge) {
        super(9, MessageType.SIGNING_REQUEST);
        this.challenge = challenge;
    }

    public ReceiveSigningRequestState() {
        super(9, MessageType.SIGNING_REQUEST);
        this.challenge = null;
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        String controllerId = message.getString(MessageField.CONTROLLER_ID.Value());
        String key = message.getString(MessageField.PK_EFF.Value());
        String hash = message.getString(MessageField.HASH.Value());

        if (this.GetContext().CheckSigningRequestSignature(controllerId, key, hash)) {
            this.GetContext().GoToNext(new ReceiveSignatureAckState(key));
        } else {
            this.GetContext().Terminate();
        }
    }

    @Override
    public void OnStart() {
        if (this.GetContext().HasJoined()) {
            this.GetContext().SendRekeyRequest();
        } else {
            this.GetContext().DecryptAndSendChallengeAnswer(this.challenge);
        }
    }

    @Override
    public void OnTimeout() {
        if (this.GetContext().HasJoined()) {
            /* Controller has joined the network, try again */
            this.GetContext().GoToNext(this);
        } else {
            this.GetContext().Terminate();
        }
    }
}
