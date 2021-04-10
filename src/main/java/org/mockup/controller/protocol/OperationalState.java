package org.mockup.controller.protocol;

import org.eclipse.jetty.util.ArrayUtil;
import org.json.JSONObject;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;

public abstract class OperationalState extends ControllerProtocolState {
    public OperationalState(int timeout, MessageType... operationalMessages) {
        super(timeout,
                ArrayUtil.addToArray(
                        ArrayUtil.addToArray(operationalMessages, MessageType.RE_KEY_REQUEST, MessageType.class),
                        MessageType.CONTROLLER_CERTIFICATE_UPDATE, MessageType.class));
    }

    public abstract void OnOperationalMessageReceived(String senderIpAddress, JSONObject message);

    @Override
    public void OnMessageReceived(String senderIpAddress, JSONObject message) {
        switch (MessageType.ByValue(message.getString(MessageField.TYPE.Value()))) {
        case RE_KEY_REQUEST: {
            /* Drop everything and rekey */
            this.GetContext().GoToNext(new SendSigningRequestState());
            break;
        }
        case CONTROLLER_CERTIFICATE_UPDATE: {
            String idString = message.getString(MessageField.SENDER_ID.Value());
            String certificateString = message.getString(MessageField.CERT_EFF.Value());
            this.GetContext().SaveOtherControllerInformation(senderIpAddress, idString, certificateString);
            break;
        }
        default: {
            this.OnOperationalMessageReceived(senderIpAddress, message);
            break;
        }
        }
    }
}
