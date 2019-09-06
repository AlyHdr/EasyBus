package com.example.alihaidar.phoneverification;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Ali Haidar on 5/19/2018.
 */

public class UpdateAbsenceTask extends AsyncTask<Integer,Void,String> {
    String url="https://easybusmanagment.000webhostapp.com/updateAbsence.php";
    Context context;
    android.support.v7.app.AlertDialog load;
    public UpdateAbsenceTask(Context c, android.support.v7.app.AlertDialog alertDialog)
    {
        this.context=c;
        this.load=alertDialog;
    }


    @Override
    protected String doInBackground(final Integer... integers) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("Ok")) {
                    if(load!=null)
                        load.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userId", String.valueOf(integers[0]));
                params.put("morning", String.valueOf(integers[1]));
                params.put("afternoon", String.valueOf(integers[2]));
                return params;

            }
        };
        MySingleton.getInstance(context).addToRequestQueue(stringRequest);
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
