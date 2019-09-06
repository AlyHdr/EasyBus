package com.example.alihaidar.phoneverification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ali Haidar on 5/25/2018.
 */

public class GroupNotification {

    public GroupNotification() {

    }
    public void sendBroadcastNotificaton(String messageBody,String notification_key)
    {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        String to = notification_key; // the notification key
        AtomicInteger atomicInteger=new AtomicInteger();
        fm.send(new RemoteMessage.Builder(to)
                .setMessageId(atomicInteger.toString())
                .addData("EasyBus",messageBody)
                .build());
    }
    public String addNotificationKey(String senderId, String driverPhone, String registrationId, String idToken) throws IOException, JSONException {
        URL url = new URL("https://fcm.googleapis.com/fcm/googlenotification");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);

        // HTTP request header
        con.setRequestProperty("project_id", senderId);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");
        con.connect();

        // HTTP request
        JSONObject data = new JSONObject();
        data.put("operation", "add");
        data.put("notification_key_name", driverPhone);
        data.put("registration_ids", new JSONArray(Arrays.asList(registrationId)));
        data.put("id_token", idToken);

        OutputStream os = con.getOutputStream();
        os.write(data.toString().getBytes("UTF-8"));
        os.close();

        // Read the response into a string
        InputStream is = con.getInputStream();
        String responseString = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        is.close();

        // Parse the JSON string and return the notification key
        JSONObject response = new JSONObject(responseString);
        return response.getString("notification_key");

    }
}
