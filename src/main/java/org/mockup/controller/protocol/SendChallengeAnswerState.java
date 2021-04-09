package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class SendChallengeAnswerState extends ControllerProtocolState {
    private final String challenge;

    public SendChallengeAnswerState(String challenge) {
        super(15, MessageType.KEY_VAULT_CERTIFICATE);
        this.challenge = challenge;
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        String keyVaultCertificateString = message.getString(MessageField.CERT_KV.Value());
        if (this.GetContext().CheckKeyVaultCertificate(keyVaultCertificateString)) {
            this.GetContext().SaveKeyVaultCertificate(keyVaultCertificateString);
            this.GetContext().GoToNext(new SendChallengeState());
        } else {
            this.GetContext().Terminate();
        }
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
