package org.mockup.common;

import static spark.Spark.post;
import spark.Request;
import spark.Response;
import spark.Route;
import org.json.*;

public class Communication implements Route {
    public void Start() {
        post("/message", "application/json", this);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String contentsString = request.queryParams("contents");
        JSONObject contents = new JSONObject(contentsString);
        System.out.println(contentsString);
        return null;
    }
}
