package org.mockup.controller.protocol;

import org.json.JSONObject;

public class SendSignatureAckState extends ControllerProtocolState {
    public SendSignatureAckState() {
        super(0);
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {

    }

    @Override
    public void OnStart() {
        this.GetContext().SendSignatureAck();

        if (this.GetContext().HasFoundOtherController()) {
            this.GetContext().UpdateEffectiveCertificateToOtherController();
        }

        this.GetContext().GoToNext(new FindOtherControllerState());
    }

    @Override
    public void OnTimeout() {

    }
}
