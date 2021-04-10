package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class SendJoinRequestState extends ControllerProtocolState {
    public SendJoinRequestState() {
        super(15, MessageType.CHALLENGE_SUBMISSION);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        String challenge = message.getString(MessageField.ENCRYPTED_CHALLENGE.Value());
        this.GetContext().GoToNext(new SendChallengeAnswerState(challenge));
    }

    @Override
    public void OnStart() {
        this.GetContext().SendJoinRequest();
    }

    @Override
    public void OnTimeout() {
        this.GetContext().Terminate();
    }
}
