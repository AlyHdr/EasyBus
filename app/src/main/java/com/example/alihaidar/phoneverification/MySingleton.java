package com.example.alihaidar.phoneverification;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Ali Haidar on 5/10/2018.
 */

public class MySingleton {
    private static MySingleton instance;
    private static Context context;
    private RequestQueue requestQueue;
    public MySingleton(Context c)
    {
        context=c;
        requestQueue=getRequestQueue();
    }
    private RequestQueue getRequestQueue()
    {
        if(requestQueue==null)
            requestQueue= Volley.newRequestQueue(context.getApplicationContext());
        return requestQueue;
    }
    public static synchronized MySingleton getInstance(Context context)
    {
        if(instance==null)
            instance=new MySingleton(context);
        return instance;
    }
    public <T> void addToRequestQueue(Request<T> request)
    {
        getRequestQueue().add(request);
    }
}
