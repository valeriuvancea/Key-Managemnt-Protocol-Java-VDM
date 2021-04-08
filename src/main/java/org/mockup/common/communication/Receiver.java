package org.mockup.common.communication;

import static spark.Spark.post;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Receiver implements Route {
    private final Logger logger = LoggerFactory.getLogger(Receiver.class);
    private final IReceiverCallback callback;

    public Receiver(String interfaceIpAddress, IReceiverCallback callback) {
        Spark.ipAddress(interfaceIpAddress);
        this.callback = callback;
    }

    public void Start() {
        post("/message", "application/json", this);
    }

    public void Stop() {
        Spark.stop();
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String contentsString = request.queryParams("contents");

        try {
            String senderAddress = request.ip();
            JSONObject contents = new JSONObject(contentsString);
            this.callback.HandleMessage(senderAddress, contents);
        } catch (JSONException e) {
        }

        return "";
    }

}
