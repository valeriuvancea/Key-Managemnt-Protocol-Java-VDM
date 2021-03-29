package org.mockup.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discovery implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(Discovery.class);

    private final static int DISCOVERY_PORT = 2378;
    private final MessageType discoveryMessageType;
    private final IDiscoveryCallback discoveryCallback;

    private Thread processThread = null;
    private AtomicBoolean run = new AtomicBoolean(false);

    public Discovery(MessageType discoveryMessageType, IDiscoveryCallback discoveryCallback) {
        this.discoveryMessageType = discoveryMessageType;
        this.discoveryCallback = discoveryCallback;
    }

    public void Start() {
        if (this.processThread != null) {
            return;
        }

        this.run.set(true);
        this.processThread = new Thread(this);
        this.processThread.start();
    }

    public void Stop() {
        if (this.processThread == null) {
            return;
        }

        this.run.set(false);
        this.processThread = null;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        byte[] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        try {
            socket = new DatagramSocket(Discovery.DISCOVERY_PORT, InetAddress.getByName("0.0.0.0"));
            socket.setSoTimeout(1000);

            Discovery.logger.debug("Background thread running");

            while (this.run.get()) {
                try {
                    socket.receive(packet);

                    String address = packet.getAddress().getHostAddress();
                    String contentsString = new String(packet.getData(), 0, packet.getLength());
                    JSONObject contents = new JSONObject(contentsString);

                    if (!contents.has(MessageField.TYPE.toString())) {
                        continue;
                    }

                    String receivedType = contents.getString(MessageField.TYPE.toString());
                    if (receivedType.compareTo(this.discoveryMessageType.toString()) != 0) {
                        continue;
                    }

                    if (!contents.has(MessageField.CONTROLLER_ID.toString())) {
                        continue;
                    }

                    String controllerId = contents.getString(MessageField.CONTROLLER_ID.toString());
                    this.discoveryCallback.BroadcastReceived(address, controllerId);
                } catch (SocketTimeoutException | JSONException e) {

                }
            }
        } catch (IOException e) {
            logger.error("Background thread failed with exception {}", e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
            Discovery.logger.debug("Background thread terminated");
        }
    }

    protected static void Broadcast(MessageType messageType, String controllerId) {
        JSONObject message = new JSONObject();
        message.put(MessageField.TYPE.toString(), messageType.toString());
        message.put(MessageField.CONTROLLER_ID.toString(), controllerId);

        DatagramSocket socket = null;
        try {
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            byte[] buffer = message.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, DISCOVERY_PORT);
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.send(packet);
        } catch (IOException e) {
            Discovery.logger.error("Failed to send out a broadcast message with exception {}", e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
