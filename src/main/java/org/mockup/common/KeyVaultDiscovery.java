package org.mockup.common;

public class KeyVaultDiscovery extends Discovery {
    private static final MessageType discoveryMessageType = MessageType.KEY_VAULT_DISCOVERY_REQUEST;

    public KeyVaultDiscovery(IDiscoveryCallback callback) {
        super(discoveryMessageType, callback);
    }

    public static void BroadcastDiscoveryRequest(String controllerId) {
        Discovery.Broadcast(discoveryMessageType, controllerId);
    }
}
