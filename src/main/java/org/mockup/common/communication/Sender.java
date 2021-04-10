package org.mockup.common.communication;

import java.io.IOException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sender {
    private final Logger logger = LoggerFactory.getLogger(Sender.class);

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
        } catch (Exception e) {
            logger.error("Failed to send message to {}. Contents: {}", destinationIpAddress, messageContents);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
            }
        }
    }
}
