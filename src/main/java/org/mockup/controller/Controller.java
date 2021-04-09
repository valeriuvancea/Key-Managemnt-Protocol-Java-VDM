package org.mockup.controller;

import java.io.IOException;

import org.mockup.common.protocol.*;
import org.mockup.controller.protocol.ControllerProtocolContext;
import org.mockup.controller.protocol.FindKeyVaultState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockup.common.Common;
import org.mockup.common.communication.Sender;
import org.mockup.common.communication.Receiver;

public class Controller implements IContextTerminatedCallback {
    private final Logger logger = LoggerFactory.getLogger(Controller.class);
    private final String controllerAddress;
    private final Sender sender;
    private final Receiver receiver;
    private final ControllerProtocolContext context;

    public Controller() throws IOException, InterruptedException {
        this.controllerAddress = Common.GetIpAddress();
        this.sender = new Sender();
        this.context = new ControllerProtocolContext(sender, this);
        this.receiver = new Receiver(this.controllerAddress, this.context);
    }

    public void Start() {
        this.logger.info("Starting controller.");
        this.context.Start(new FindKeyVaultState());
        this.receiver.Start();
    }

    public void Stop() {
        this.logger.info("Stopping controller.");
        this.context.Stop();
        this.receiver.Stop();
    }

    @Override
    public void HandleContextTerminated(String associatedControllerIdString) {
        this.logger.info("Protocol has terminated. Restarting.");
        this.context.Start(new FindKeyVaultState());
    }
}
