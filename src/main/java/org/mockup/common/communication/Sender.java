package org.mockup.common.communication;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

public class Sender {
    private final Logger logger = LoggerFactory.getLogger(Sender.class);

    public void SendMessage(String destinationIpAddress, JSONObject contents) {
        HttpGet request = new HttpGet(String.format("http://%s:4567/message", destinationIpAddress));

        URI uri = null;
        try {
            uri = new URIBuilder(request.getURI()).addParameter("contents", contents.toString()).build();
        } catch (URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        ((HttpRequestBase) request).setURI(uri);

        RequestConfig config = RequestConfig.custom().setConnectTimeout(3000).setConnectionRequestTimeout(3000)
                .setSocketTimeout(3000).build();
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        try {
            client.execute(request);
        } catch (Exception e) {
            logger.error("Failed to send message to {}", destinationIpAddress);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
            }
        }
    }
}
