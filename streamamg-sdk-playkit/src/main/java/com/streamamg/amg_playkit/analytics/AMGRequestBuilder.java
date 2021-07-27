package com.streamamg.amg_playkit.analytics;

import android.util.Log;

import com.google.gson.JsonObject;
import com.kaltura.netkit.connect.request.RequestBuilder;

import org.json.JSONObject;

import java.util.Map;

import static com.kaltura.playkit.utils.Consts.HTTP_METHOD_POST;

public class AMGRequestBuilder {
    public static RequestBuilder getRequest(String baseUrl, String userAgent, AMGAnalyticsRequest request) {
        try {
        RequestBuilder requestBuilder = new RequestBuilder()
                .method(HTTP_METHOD_POST)
                .url(baseUrl)
                .addParams(request.toJsonObject());
        requestBuilder.build().getHeaders().put("User-Agent", userAgent);
        return requestBuilder;
        } catch (Exception e){
            Log.d("AMG", "Error creating body: " + e.getLocalizedMessage());
        }
        return null;
    }
}
