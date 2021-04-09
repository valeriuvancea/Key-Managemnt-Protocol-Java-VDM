package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class SendSigningRequestState extends ControllerProtocolState {
    private Boolean first;

    public SendSigningRequestState(Boolean first) {
        super(9, MessageType.SIGNING_REPLY);
        this.first = first;
    }

    public SendSigningRequestState() {
        super(9, MessageType.SIGNING_REPLY);
        this.first = false;
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        String controllerIdString = message.getString(MessageField.CONTROLLER_ID.Value());
        String effectiveCertificateString = message.getString(MessageField.CERT_EFF.Value());
        String caCertificateString = message.getString(MessageField.CERT_CA.Value());
        String hashString = message.getString(MessageField.HASH.Value());

        if (this.GetContext().CheckSigningReplySignature(controllerIdString, effectiveCertificateString,
                caCertificateString, hashString)) {
            this.GetContext().SaveEffectiveKeys(effectiveCertificateString);
            this.GetContext().GoToNext(new SendSignatureAckState());
        } else {
            this.GetContext().Terminate();
        }
    }

    @Override
    public void OnStart() {
        this.GetContext().GenerateAndSendSigningRequest(this.first);
    }

    @Override
    public void OnTimeout() {
        this.GetContext().Terminate();
    }
}
