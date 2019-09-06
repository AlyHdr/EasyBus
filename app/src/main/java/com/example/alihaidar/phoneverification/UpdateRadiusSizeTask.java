package com.example.alihaidar.phoneverification;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ali Haidar on 5/27/2018.
 */
public class UpdateRadiusSizeTask extends AsyncTask<String,Void,String> {
    String url="https://easybusmanagment.000webhostapp.com/updateRadius.php";
    Context context;
    AlertDialog load;
    public UpdateRadiusSizeTask(Context c, AlertDialog alertDialog)
    {
        this.load=alertDialog;
        this.context=c;
    }
    String result="Ok";
    @Override
    protected String doInBackground(final String... strings) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.trim().equals("ok"))
                {
                    load.dismiss();
                    Snackbar snackbar=Snackbar.make(NavigationActivity.finish.findViewById(R.id.app_bar),"Radius Changed", BaseTransientBottomBar.LENGTH_LONG);
                    View snack_view=snackbar.getView();
                    snack_view.setBackgroundColor(Color.GREEN);
                    snackbar.show();
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
                params.put("userId", strings[0]);
                params.put("radius", strings[1]);
                return params;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(stringRequest);
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
