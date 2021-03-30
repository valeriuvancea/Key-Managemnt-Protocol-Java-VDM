package org.mockup;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.json.JSONObject;
import org.mockup.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For trying things out.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException {
        while (true) {
            Thread.sleep(3000);
            KeyVaultDiscovery.BroadcastDiscoveryRequest("DUMMY");
        }
    }
}
