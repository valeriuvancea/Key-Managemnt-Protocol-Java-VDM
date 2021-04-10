package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public class SendSigningRequestState extends ControllerProtocolState {
    public SendSigningRequestState() {
        super(15, MessageType.SIGNING_REPLY);
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
            this.HandleStateFailure();
        }
    }

    @Override
    public void OnStart() {
        this.GetContext().GenerateAndSendSigningRequest();
    }

    @Override
    public void OnTimeout() {
        this.HandleStateFailure();
    }

    private void HandleStateFailure() {
        if (this.GetContext().HasJoined()) {
            this.GetContext().GoToNext(new FindOtherControllerState());
        } else {
            this.GetContext().Terminate();
        }
    }
}
