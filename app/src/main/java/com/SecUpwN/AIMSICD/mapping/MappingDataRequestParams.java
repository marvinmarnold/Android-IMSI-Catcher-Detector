package com.SecUpwN.AIMSICD.mapping;

import com.android.volley.Response;

import org.json.JSONObject;

/**
 * Created by Marvin Arnold on 22/08/15.
 */
class MappingDataRequestParams {
    public JSONObject getJsonObject() {
        return mJsonObject;
    }

    public String getEndpoint() {
        return mEndpoint;
    }

    public int getMethod() {
        return mMethod;
    }

    public Response.Listener getSuccessListener() {
        return mSuccessListener;
    }

    public Response.ErrorListener getErrorListener() {
        return mErrorListener;
    }

    private final JSONObject mJsonObject;
    private final String mEndpoint;
    private final int mMethod;
    private final Response.Listener mSuccessListener;
    private final Response.ErrorListener mErrorListener;

    public MappingDataRequestParams(JSONObject jsonObject,
                                    String endpoint,
                                    int method,
                                    Response.Listener successListener,
                                    Response.ErrorListener errorListener) {

        this.mJsonObject = jsonObject;
        this.mEndpoint = endpoint;
        this.mMethod = method;
        this.mSuccessListener = successListener;
        this.mErrorListener = errorListener;
    }
}