package com.example.alihaidar.phoneverification;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;

/**
 * Created by Ahmad Alibrahim on 5/19/2018.
 */

public class ChangeTrackTypeTask extends AsyncTask <Void,Void,Void> {
    private String phoneNb;
    private String trackType;

    public ChangeTrackTypeTask(String phoneNb, String trackType) {
        this.phoneNb = phoneNb;
        this.trackType = trackType;
    }
    @Override
    protected Void doInBackground(Void... voids) {
        try
        {
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("https://easybusmanagment.000webhostapp.com/changeTrackType.php?driverPhone="+phoneNb+"&trackType="+trackType);

            //httpPost.setEntity(new UrlEncodedFormEntity());

            HttpResponse httpResponse = httpClient.execute(httpPost);
            httpResponse.getEntity();

            publishProgress();
        }
        catch (Exception e) {
            System.out.println("AHMADDDDDDDDDDDDDDDDd");
            System.out.println(e.toString());
            System.out.println("AHMADDDDDDDDDDDDDDDDd");

        }
        return null;
    }
}
