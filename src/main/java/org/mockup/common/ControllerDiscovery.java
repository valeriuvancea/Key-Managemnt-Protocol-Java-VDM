package org.mockup.common;

import java.net.UnknownHostException;

public class ControllerDiscovery extends Discovery {
    private static final MessageType discoveryMessageType = MessageType.CONTROLLER_DISCOVERY_REQUEST;

    public ControllerDiscovery(IDiscoveryCallback callback) {
        super(discoveryMessageType, callback);
    }

    public static void BroadcastDiscoveryRequest(String controllerId) {
        Discovery.Broadcast(discoveryMessageType, controllerId);
    }
}
