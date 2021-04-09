package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class SendChallengeState extends ControllerProtocolState {
    public SendChallengeState() {
        super(15, MessageType.CHALLENGE_ANSWER);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        String challengeAnswer = message.getString(MessageField.DECRYPTED_CHALLENGE.Value());

        if (this.GetContext().CheckChallengeAnswer(challengeAnswer)) {
            this.GetContext().GoToNext(new SendSigningRequestState());
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
