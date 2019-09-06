package com.example.alihaidar.phoneverification;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import static com.example.alihaidar.phoneverification.MainActivity.student_user;

/**
 * Created by Ali Haidar on 5/29/2018.
 */

public class RegisterTokenTask extends AsyncTask<String,Void,Void> {
    String app_server_url = "https://easybusmanagment.000webhostapp.com/insert_fcm.php";
    @Override
    protected Void doInBackground(final String... strings) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, app_server_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("fcm_token", strings[0]);
                params.put("id", strings[1]);
                return params;
            }
        };
        MySingleton.getInstance(MainActivity.finish).addToRequestQueue(stringRequest);
        return null;
    }
}
