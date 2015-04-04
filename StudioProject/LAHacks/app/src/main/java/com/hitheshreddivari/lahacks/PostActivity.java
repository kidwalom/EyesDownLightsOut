package com.hitheshreddivari.lahacks;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

        String url = "https://graph.api.smartthings.com/api/smartapps/installations/2eb26a77-078c-44d8-924f-b120b5ff5ec1/switch";
//        String url = "http://requestb.in/18ltbmq1";
        StringRequest postReq = new StringRequest(Request.Method.PUT, url, mResponseListener, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error [" + error + "]");

            }
        }) {

//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                JSONObject data = new JSONObject();
//                params.put("value", param);
//                Iterator iter = params.entrySet().iterator();
//                while (iter.hasNext())
//                {
//                    Map.Entry pairs = (Map.Entry)iter.next();
//                    try {
//                        data.put((String)pairs.getKey(), (String)pairs.getValue());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                return params;
//            }

            @Override
            public byte[] getBody() throws AuthFailureError{
                JSONObject data = new JSONObject();
                try {
                    data.put("value",param );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("DATA ", data.toString());
                return data.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", "Bearer f2dac983-4f2f-4719-855f-c7a822ba11d2");
//                params.put("Host", "graph.api.smartthings.com");
//                params.put("Content-Length", "15");
//                params.put("X-Target-URI", "https://graph.api.smartthings.com");
//                params.put("Content-Type","text/plain; charset=UTF-8");
//                params.put("Connection","Keep-Alive");
                return params;
            }

        };

        rq.add(postReq);

    }

}
