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
 * Created by Ali Haidar on 5/18/2018.
 */

public class CollectStudentsTask extends AsyncTask<String,Void,ArrayList<Student>> {
    @Override
    protected ArrayList<Student> doInBackground(String... strings) {
        URL url = null;
        ArrayList<Student> students=new ArrayList<>();
        try {
            url = new URL("https://easybusmanagment.000webhostapp.com/collectStudents.php?driverPhone=" + strings[0]);
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
                    int fenceRaduis=Integer.parseInt(studentInfo[6]);
                    Student student=new Student(id,name,driverPh,latlng,ph,fenceRaduis);
                    students.add(student);
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return students;
    }

    @Override
    protected void onPostExecute(ArrayList<Student> students) {
        super.onPostExecute(students);
    }
}