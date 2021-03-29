package org.mockup.common;

import static spark.Spark.post;

import java.io.IOException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.*;

public class Communication implements Route {
    private final ICommunicationCallback callback;

    public Communication(String interfaceIpAddress, ICommunicationCallback callback) {
        Spark.ipAddress(interfaceIpAddress);
        this.callback = callback;
    }

    public void Start() {
        post("/message", "application/json", this);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String contentsString = request.queryParams("contents");

        try {
            String senderAddress = request.ip();
            JSONObject contents = new JSONObject(contentsString);
            this.callback.HandleMessage(senderAddress, contents);
        } catch (JSONException ex) {

        }

        return "";
    }

    /**
     * Best effort. Does not guarantee delivery.
     * 
     * @param destinationIpAddress
     * @param contents
     */
    public void SendMessage(String destinationIpAddress, JSONObject contents) {
        String messageContents = String.format("contents=%s", contents.toString());
        StringEntity entity = new StringEntity(messageContents, ContentType.APPLICATION_FORM_URLENCODED);
        HttpPost request = new HttpPost(String.format("http://%s:4567/message", destinationIpAddress));
        request.setEntity(entity);

        RequestConfig config = RequestConfig.custom().setConnectTimeout(3000).setConnectionRequestTimeout(3000)
                .setSocketTimeout(3000).build();
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        try {
            client.execute(request);
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        } finally {
            try {
                client.close();
            } catch (IOException e) {
            }
        }

    }
}
