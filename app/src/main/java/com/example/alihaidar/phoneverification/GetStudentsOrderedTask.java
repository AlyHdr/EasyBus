package com.example.alihaidar.phoneverification;

import android.os.AsyncTask;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Ali Haidar on 5/19/2018.
 */

public class GetStudentsOrderedTask extends AsyncTask<String,Void,ArrayList<Student>>{
    @Override
    protected ArrayList<Student> doInBackground(String... strings) {
        URL url = null;
        ArrayList<Student> orderedStudents=new ArrayList<>();
        try {
            url = new URL("https://easybusmanagment.000webhostapp.com/collectOrdered.php?driverPhone=" + strings[0]);
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
                    String[] studentInfo = str.split(" ");
                    String name=studentInfo[0]+" "+studentInfo[1];
                    String driverPh=strings[0];
                    LatLng latlng=new LatLng(Double.parseDouble(studentInfo[2]),Double.parseDouble(studentInfo[3]));
                    String ph=studentInfo[4];
                    int id=Integer.parseInt(studentInfo[5]);
                    int morning=Integer.parseInt(studentInfo[6]);
                    int afternoon=Integer.parseInt(studentInfo[7]);
                    int fenceRaduis=Integer.parseInt(studentInfo[8]);
                    Student student=new Student(id,name,driverPh,latlng,ph,morning,afternoon,fenceRaduis);

                    orderedStudents.add(student);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return orderedStudents;
    }

    @Override
    protected void onPostExecute(ArrayList<Student> orderedStudents) {
        super.onPostExecute(orderedStudents);
    }
}