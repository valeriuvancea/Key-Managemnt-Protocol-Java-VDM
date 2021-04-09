package org.mockup.common.communication;

import static spark.Spark.post;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import org.javatuples.Pair;
import org.json.*;
import org.mockup.common.protocol.MessageField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Receiver implements Route, Runnable {
    private final Logger logger = LoggerFactory.getLogger(Receiver.class);
    private final IReceiverCallback callback;
    private final ConcurrentLinkedQueue<Pair<String, JSONObject>> messages;

    private Thread processThread;
    private AtomicBoolean run = new AtomicBoolean(false);

    public Receiver(String interfaceIpAddress, IReceiverCallback callback) {
        Spark.ipAddress(interfaceIpAddress);
        this.callback = callback;
        this.messages = new ConcurrentLinkedQueue<>();
    }

    public void Start() {
        this.run.set(true);
        this.processThread = new Thread(this);
        this.processThread.start();
        post("/message", "application/json", this);
    }

    public void Stop() {
        Spark.stop();
        this.run.set(false);
        this.processThread = null;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String contentsString = request.queryParams("contents");

        try {
            String senderAddress = request.ip();
            JSONObject contents = new JSONObject(contentsString);
            // System.out.println(contents.getString(MessageField.TYPE.Value()) + " "
            // + contents.getString(MessageField.CONTROLLER_ID.Value()));

            synchronized (this.messages) {
                this.messages.add(new Pair<String, JSONObject>(senderAddress, contents));
                this.messages.notifyAll();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public void run() {
        while (this.run.get()) {
            try {
                Pair<String, JSONObject> pair = null;

                if (this.messages.isEmpty()) {
                    synchronized (this.messages) {
                        this.messages.wait(3000);
                    }
                }

                pair = this.messages.poll();

                if (pair != null) {
                    this.callback.HandleMessage(pair.getValue0(), pair.getValue1());
                }
            } catch (InterruptedException e) {
                logger.error("Error while processing received messages.");
            }
        }
    }

}
