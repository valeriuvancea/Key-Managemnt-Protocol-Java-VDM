package org.mockup.key_vault.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageType;

public class ReceiveChallengeAnswerState extends KeyVaultProtocolState {

    private byte[] challengeBytes;

    public ReceiveChallengeAnswerState() {
        super(9, MessageType.CHALLENGE_ANSWER);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {

    }

    @Override
    public void OnStart() {
        System.out.println("Send challenge");
        this.challengeBytes = this.GetContext().SendChallenge();
    }

    @Override
    public void OnTimeout() {
        this.GetContext().Terminate();
    }
}
