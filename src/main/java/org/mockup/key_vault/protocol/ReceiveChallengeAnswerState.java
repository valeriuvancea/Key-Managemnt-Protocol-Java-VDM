package org.mockup.key_vault.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class ReceiveChallengeAnswerState extends KeyVaultProtocolState {
    public ReceiveChallengeAnswerState() {
        super(9, MessageType.CHALLENGE_ANSWER);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        String answer = message.getString(MessageField.DECRYPTED_CHALLENGE.Value());

        if (this.GetContext().CheckChallengeAnswer(answer)) {
            this.GetContext().GoToNext(new ReceiveChallengeState());
        } else {
            this.GetContext().Terminate();
        }
    }

    @Override
    public void OnStart() {
        this.GetContext().GenerateStashEncryptAndSendChallenge();
    }

    @Override
    public void OnTimeout() {
        this.GetContext().Terminate();
    }
}
