package org.mockup.controller;

import java.io.IOException;

import org.mockup.common.protocol.*;
import org.mockup.controller.protocol.ControllerProtocolContext;
import org.mockup.controller.protocol.FindKeyVaultState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.mockup.common.Common;
import org.mockup.common.communication.Sender;
import org.mockup.common.discovery.ControllerDiscovery;
import org.mockup.common.discovery.IDiscoveryCallback;
import org.mockup.common.communication.Receiver;

public class Controller implements IContextTerminatedCallback, IDiscoveryCallback {
    private final Logger logger = LoggerFactory.getLogger(Controller.class);
    private final String controllerAddress;
    private final Sender sender;
    private final Receiver receiver;
    private final ControllerProtocolContext context;
    private final ControllerDiscovery discovery;

    public Controller() throws IOException, InterruptedException {
        this.controllerAddress = Common.GetIpAddress();
        this.sender = new Sender();
        this.context = new ControllerProtocolContext(sender, this);
        this.receiver = new Receiver(this.controllerAddress, this.context);
        this.discovery = new ControllerDiscovery(this);
    }

    public void Start() throws InterruptedException {
        this.logger.info("Starting controller.");
        this.receiver.Start();
        this.discovery.Start();

        /*
         * Give some time for receiver and discovery to spin up, before starting the
         * state machine.
         */
        Thread.sleep(3000);

        this.context.Start(new FindKeyVaultState());

    }

    public void Stop() {
        this.logger.info("Stopping controller.");
        this.context.Stop();
        this.receiver.Stop();
        this.discovery.Stop();
    }

    @Override
    public void HandleContextTerminated(String associatedControllerIdString) {
        this.logger.info("Protocol has terminated. Restarting.");
        this.context.Start(new FindKeyVaultState());
    }

    @Override
    public void BroadcastReceived(String sourceIpAddress, String senderIdString) {
        if (!this.context.HasJoined()) {
            return;
        }

        String controllerIdString = this.context.GetAssociateIdString();

        if (controllerIdString.equals(senderIdString)) {
            return;
        }

        String effectiveCertificateString = this.context.GetEffectiveCertificateString();
        JSONObject contents = new JSONObject();
        contents.put(MessageField.TYPE.Value(), MessageType.CONTROLLER_DISCOVERY_REPLY.Value());
        contents.put(MessageField.CONTROLLER_ID.Value(), senderIdString);
        contents.put(MessageField.SENDER_ID.Value(), controllerIdString);
        contents.put(MessageField.CERT_EFF.Value(), effectiveCertificateString);
        this.sender.SendMessage(sourceIpAddress, contents);
    }
}
