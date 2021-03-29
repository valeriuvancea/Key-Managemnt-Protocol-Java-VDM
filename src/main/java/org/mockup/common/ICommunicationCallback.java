package org.mockup.common;

import org.json.JSONObject;

public interface ICommunicationCallback {
    public void HandleMessage(String senderIpAddress, JSONObject contents);
}