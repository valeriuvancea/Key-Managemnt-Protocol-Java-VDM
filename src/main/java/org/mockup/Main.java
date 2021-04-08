package org.mockup;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONObject;
import org.mockup.common.*;
import org.mockup.common.communication.Sender;
import org.mockup.common.protocol.MessageField;
import org.mockup.common.protocol.MessageType;
import org.mockup.controller.protocol.ControllerProtocolContext;
import org.mockup.controller.protocol.FindKeyVaultState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For trying things out.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException {
        FindKeyVaultState startState = new FindKeyVaultState();
        Sender sender = new Sender();
        ControllerProtocolContext context = new ControllerProtocolContext("dummy", sender, null);

        context.StartContext(startState);
        JSONObject message = new JSONObject();
        message.put(MessageField.CONTROLLER_ID.Value(), "dummy");
        message.put(MessageField.TYPE.Value(), MessageType.KEY_VAULT_DISCOVERY_REPLY.Value());
        context.HandleMessage("", message);

        while (true) {
            Thread.sleep(3000);
            logger.info("Sketchbook running");
        }
    }
}
