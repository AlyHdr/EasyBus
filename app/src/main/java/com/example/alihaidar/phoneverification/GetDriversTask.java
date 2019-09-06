package com.example.alihaidar.phoneverification;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Ali Haidar on 7/16/2018.
 */

public class GetDriversTask extends AsyncTask<Integer,Void,ArrayList<Driver>> {

    @Override
    protected ArrayList<Driver> doInBackground(Integer... ints) {
        URL url = null;
        ArrayList<Driver> Drivers=new ArrayList<>();
        try {
            url = new URL("https://easybusmanagment.000webhostapp.com/getDrivers.php?userId=" + ints[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = rd.readLine()) != null)
            {
                String[] array = line.split("<br/>");
                for (String str : array)
                {
                    String[] driverInfo = str.split(" ");
                    String name=driverInfo[0]+" "+driverInfo[1];
                    String driverPh=driverInfo[2];
                    LatLng houselatlng=new LatLng(Double.parseDouble(driverInfo[3]),Double.parseDouble(driverInfo[4]));
                    String trackType=driverInfo[5];
                    int id=Integer.parseInt(driverInfo[6]);
                    Driver driver=new Driver(id,name,driverPh,houselatlng,trackType);
                    Drivers.add(driver);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return Drivers;
    }

    @Override
    protected void onPostExecute(ArrayList<Driver> drivers) {
        super.onPostExecute(drivers);
    }
}
