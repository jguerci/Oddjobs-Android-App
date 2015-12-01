package com.jguerci.oddjobs;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestHandler {
    private static RequestHandler sInstance = null;
    private RequestQueue requestQueue_;
    private static Context context_;

    private RequestHandler(Context context) {
        context_ = context;
        requestQueue_ = getRequestQueue();
    }

    public static synchronized RequestHandler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RequestHandler(context);
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue_ == null) {
            requestQueue_ = Volley.newRequestQueue(context_.getApplicationContext());
        }
        return requestQueue_;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
