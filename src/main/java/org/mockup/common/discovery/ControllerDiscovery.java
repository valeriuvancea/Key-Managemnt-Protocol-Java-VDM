package org.mockup.common.discovery;

import org.mockup.common.protocol.MessageType;

public class ControllerDiscovery extends Discovery {
    private static final MessageType discoveryMessageType = MessageType.CONTROLLER_DISCOVERY_REQUEST;

    public ControllerDiscovery(IDiscoveryCallback callback) {
        super(discoveryMessageType, callback);
    }

    public static void BroadcastDiscoveryRequest(String controllerId) {
        Discovery.Broadcast(discoveryMessageType, controllerId);
    }
}
