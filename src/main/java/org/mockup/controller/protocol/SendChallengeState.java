package org.mockup.controller.protocol;

import java.rmi.activation.ActivationGroupDesc.CommandEnvironment;

import org.json.JSONObject;
import org.mockup.common.Common;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class SendChallengeState extends ControllerProtocolState {
    public SendChallengeState() {
        super(15, MessageType.CHALLENGE_ANSWER);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        String challengeAnswerString = message.getString(MessageField.DECRYPTED_CHALLENGE.Value());
        byte[] challengeAnswer = Common.StringToByteArray(challengeAnswerString);

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
