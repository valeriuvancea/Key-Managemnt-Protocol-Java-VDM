package org.mockup.controller.protocol;

import org.json.JSONObject;
import org.mockup.common.Common;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyCommunicationState extends OperationalState {
    private final Logger logger = LoggerFactory.getLogger(DummyCommunicationState.class);

    public DummyCommunicationState() {
        super(10, MessageType.DUMMY_MESSAGE);
    }

    @Override
    public void OnStart() {
        String message = String.format("Hello from %s", this.GetContext().GetAssociateIdString());
        byte[] messageBytes = message.getBytes();
        this.GetContext().EncryptAndSendToOtherController(Common.ByteArrayToString(messageBytes));
    }

    @Override
    public void OnTimeout() {
        this.GetContext().GoToNext(this);
    }

    @Override
    public void OnOperationalMessageReceived(String senderIpAddress, JSONObject message) {
        String encryptedMessageString = message.getString(MessageField.ENCRYPTED_DATA.Value());
        String decryptedMessageString = this.GetContext().DecryptMessageFromOtherController(encryptedMessageString);

        if (decryptedMessageString == null) {
            return;
        }

        String messageFromOtherController = new String(Common.StringToByteArray(decryptedMessageString));
        logger.info(String.format("Received encrypted message: %s", messageFromOtherController));
    }
}
