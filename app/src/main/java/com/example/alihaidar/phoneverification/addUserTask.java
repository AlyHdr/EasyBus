package com.example.alihaidar.phoneverification;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Ali Haidar on 3/17/2018.
 */

public class addUserTask extends AsyncTask<ArrayList<NameValuePair>,Void,Void> {
    @Override
    protected Void doInBackground(ArrayList<NameValuePair>[] arrayLists) {
        try
        {
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("https://easybusmanagment.000webhostapp.com/signUp.php");

            httpPost.setEntity(new UrlEncodedFormEntity(arrayLists[0]));

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
