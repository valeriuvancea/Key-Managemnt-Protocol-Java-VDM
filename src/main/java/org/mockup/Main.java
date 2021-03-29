package org.mockup;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.json.JSONObject;
import org.mockup.common.*;

/**
 * For trying things out.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException, SocketException {
        Communication communication = new Communication("192.168.1.136", new ICommunicationCallback() {
            @Override
            public void HandleMessage(String senderIpAddress, JSONObject contents) {
                System.out.println(contents.toString());
            };
        });

        communication.Start();

        while (true) {
            Thread.sleep(3000);
            System.out.println("Sending");
            communication.SendMessage("192.168.1.99", new JSONObject().append("type", "dummy"));
        }
    }
}
