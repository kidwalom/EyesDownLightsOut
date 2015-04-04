package com.hitheshreddivari.lahacks;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;

/**
 * Created by harshsingh on 4/4/15.
 */
public class PostActivity {
    private Context mContext;

    public PostActivity(Context mContext) {
        this.mContext=mContext;
    }

    public void postData(final String param, Response.Listener<String> mResponseListener) {


//        HttpStack stack;
//        RequestQueue rq = Volley.newRequestQueue(this);

        RequestQueue rq;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(mContext.getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        rq = new RequestQueue(cache, network);

        rq.start();

        String url = "http://requestb.in/1my5dv61";

        StringRequest postReq = new StringRequest(Request.Method.POST, url, mResponseListener, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error [" + error + "]");

            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("param1", param);
                return params;
            }

        };

        rq.add(postReq);

    }

}
