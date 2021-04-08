package org.mockup.key_vault;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.mockup.common.Common;
import org.mockup.common.communication.IReceiverCallback;
import org.mockup.common.communication.Receiver;
import org.mockup.common.communication.Sender;
import org.mockup.common.discovery.IDiscoveryCallback;
import org.mockup.common.discovery.KeyVaultDiscovery;
import org.mockup.common.protocol.IContextTerminatedCallback;
import org.mockup.common.protocol.MessageField;
import org.mockup.key_vault.protocol.KeyVaultProtocolContext;
import org.mockup.key_vault.protocol.ReceiveJoinRequestState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyVault implements IDiscoveryCallback, IReceiverCallback, IContextTerminatedCallback {
    private final Logger logger = LoggerFactory.getLogger(KeyVault.class);
    private final Map<String, KeyVaultProtocolContext> contextsMap;
    private final Receiver receiver;
    private final KeyVaultDiscovery discovery;

    public KeyVault() throws IOException, InterruptedException {
        this.contextsMap = new ConcurrentHashMap<String, KeyVaultProtocolContext>();
        this.receiver = new Receiver(Common.GetIpAddress(), this);
        this.discovery = new KeyVaultDiscovery(this);
    }

    public void Start() {
        this.logger.info("Starting key vault.");
        this.discovery.Start();
        this.receiver.Start();
    }

    public void Stop() {
        this.logger.info("Stopping key vault.");
        this.discovery.Stop();
        this.receiver.Stop();

        for (Entry<String, KeyVaultProtocolContext> entry : this.contextsMap.entrySet()) {
            entry.getValue().Stop();
        }
    }

    @Override
    public void HandleMessage(String senderIpAddress, JSONObject contents) {
        String controllerIdString = contents.optString(MessageField.CONTROLLER_ID.Value());
        KeyVaultProtocolContext context = this.contextsMap.get(controllerIdString);

        if (context == null) {
            return;
        }

        /* This is thread safe */
        context.HandleMessage(senderIpAddress, contents);
    }

    @Override
    public void BroadcastReceived(String sourceIpAddress, String controllerIdString) {
        if (this.contextsMap.containsKey(controllerIdString)) {
            return;
        }

        try {
            KeyVaultProtocolContext context = new KeyVaultProtocolContext(sourceIpAddress,
                    Common.StringToByteArray(controllerIdString), new Sender(), this);
            this.contextsMap.put(controllerIdString, context);
            context.Start(new ReceiveJoinRequestState());
            this.logger.info("Key vault context associated with {} was created and started.", controllerIdString);
        } catch (IOException e) {
            logger.error("Failed to create new key vault protocol context.");
        }
    }

    @Override
    public void HandleContextTerminated(String associatedControllerIdString) {
        this.contextsMap.remove(associatedControllerIdString);
        this.logger.info("Key vault context associated with {} was terminated.", associatedControllerIdString);
    }
}
