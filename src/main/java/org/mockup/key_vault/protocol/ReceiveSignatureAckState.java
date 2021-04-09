package org.mockup.key_vault.protocol;

import org.json.JSONObject;
import org.mockup.common.protocol.MessageType;

public class ReceiveSignatureAckState extends KeyVaultProtocolState {
    private final String effectiveKeyString;
    private String effectiveCertificate;

    public ReceiveSignatureAckState(String effectiveKeyString) {
        super(9, MessageType.SIGNING_ACK);
        this.effectiveKeyString = effectiveKeyString;
    }

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        this.GetContext().SaveEffectiveCertificate(this.effectiveCertificate);
        this.GetContext().GoToNext(new RekeyPendingState());
    }

    @Override
    public void OnStart() {
        this.effectiveCertificate = this.GetContext().GenerateAndSendEffectiveCertificate(this.effectiveKeyString);
    }

    @Override
    public void OnTimeout() {
        this.GetContext().Terminate();
    }
}
