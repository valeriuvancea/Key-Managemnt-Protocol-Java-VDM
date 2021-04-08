package org.mockup;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONObject;
import org.mockup.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockup.common.state.*;

/**
 * For trying things out.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException {
        ProtocolState state = new ProtocolState(MessageType.KEY_VAULT_DISCOVERY_REPLY) {
            @Override
            public void OnMessageReceived(JSONObject message) {
                // TODO Auto-generated method stub

            }

            @Override
            public void OnStart() {
                // TODO Auto-generated method stub

            }

            @Override
            public void OnTimeout() {
                // TODO Auto-generated method stub

            }
        };

        ProtocolContext context = new ProtocolContext();
        context.Start(state);
        Communication communication = new Communication("192.168.1.136", context);
        communication.Start();

        while (true) {
            Thread.sleep(3000);
            logger.info("Running");
        }
    }
}
