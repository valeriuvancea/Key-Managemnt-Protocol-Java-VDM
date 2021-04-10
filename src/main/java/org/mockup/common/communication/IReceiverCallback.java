package org.mockup.common.communication;

import org.json.JSONObject;

public interface IReceiverCallback {
    public void HandleMessage(String senderIpAddress, JSONObject contents);
}