package com.streamamg.amg_playkit.analytics;

import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonObject;
import com.kaltura.netkit.connect.request.RequestBuilder;

import org.json.JSONObject;

import java.util.Map;

import static com.kaltura.playkit.utils.Consts.HTTP_METHOD_POST;
import static com.kaltura.playkit.utils.Consts.HTTP_METHOD_GET;

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

    public static RequestBuilder sendAnalyticsEvent(String baseUrl, String userAgent, Map<String, String> params) {
        RequestBuilder requestBuilder = new RequestBuilder()
                .method(HTTP_METHOD_GET)
                .url(buildUrlWithParams(baseUrl, params));
        requestBuilder.build().getHeaders().put("User-Agent", userAgent);
        return requestBuilder;
    }

    private static String buildUrlWithParams(String baserUrl, Map<String, String> params) {

        Uri.Builder builder = Uri.parse(baserUrl).buildUpon();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build().toString();
    }

}
