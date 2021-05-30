package org.mockup.key_vault.protocol;

import org.json.JSONObject;
import org.mockup.common.Common;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class ReceiveChallengeAnswerState extends KeyVaultProtocolState {
    public ReceiveChallengeAnswerState() {
        super(15, MessageType.CHALLENGE_ANSWER);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        byte[] answer = Common.StringToByteArray(message.getString(MessageField.DECRYPTED_CHALLENGE.Value()));

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
