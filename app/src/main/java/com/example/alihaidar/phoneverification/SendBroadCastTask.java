package com.example.alihaidar.phoneverification;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ali Haidar on 5/31/2018.
 */

public class SendBroadCastTask extends AsyncTask<String,Void,Void> {
    Context context;
    AlertDialog alert;
    public SendBroadCastTask(Context context,AlertDialog alert) {
        this.context = context;
        this.alert=alert;
    }

    public SendBroadCastTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(final String... strings) {

        String app_server_url2 = "https://easybusmanagment.000webhostapp.com/send_broadcast.php?morning="+MapsFragmentDriver.morning;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, app_server_url2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                    if(alert!=null)
                        alert.dismiss();
                    Snackbar snackbar=Snackbar.make(NavigationActivity.finish.findViewById(R.id.app_bar),"Broadcast sent", BaseTransientBottomBar.LENGTH_LONG);
                    View snack_view=snackbar.getView();
                    snack_view.setBackgroundColor(Color.GREEN);
                    snackbar.show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("message",strings[0]);
                params.put("title", "EasyBus");
                params.put("driverId",strings[1]);
                return params;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(stringRequest);
        return null;
    }
}
