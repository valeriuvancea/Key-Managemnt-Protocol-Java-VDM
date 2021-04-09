package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageType;

public class SendChallengeAnswerState extends ControllerProtocolState {
    private final String challenge;

    public SendChallengeAnswerState(String challenge) {
        super(9, MessageType.KEY_VAULT_CERTIFICATE);
        this.challenge = challenge;
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        System.out.println("received key vault certificate");
    }

    @Override
    public void OnStart() {
        this.GetContext().DecryptAndSendChallengeAnswer(this.challenge);
    }

    @Override
    public void OnTimeout() {
        this.GetContext().Terminate();
    }
}
