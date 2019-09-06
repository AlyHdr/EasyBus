package com.example.alihaidar.phoneverification;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String ph = "";
    Intent intent;
    public static Activity finish;
    public School school;
    boolean entered_on_create=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        finish = this;
        intent = getIntent();
       pref = getSharedPreferences("authenticated", Context.MODE_PRIVATE);
        editor = pref.edit();
        editor.putBoolean("profile_registered",true).apply();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(pref.getBoolean("first_launch",true))
        {
            Intent intent=new Intent(this,SliderActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (auth.getCurrentUser() != null && pref.getBoolean("profile_registered",false)) {
            String phone = auth.getCurrentUser().getPhoneNumber();
            char[] elements = phone.toCharArray();
            for (int i = 4; i < elements.length; i++)
                ph += elements[i];
            runActivity();
        }
        else if(auth.getCurrentUser()==null)
        {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build())).build(), 123);
            editor = pref.edit();
            editor.putInt("auth", 1);
            editor.apply();
        }
        else if(!pref.getBoolean("profile_registered",false)) {

        }
    }
    public void runActivity()
    {
        if(isNetworkAvailable())
        {
            checkUserTypeTask check = new checkUserTypeTask();
            check.execute(ph);
            return;
        }
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setMessage("Mobile data is not avialable you need internet connection!");
        alert.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                runActivity();
            }
        });
        alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    dialogInterface.dismiss();
                    finish();
                }
                return false;
            }
        });
        alert.create().show();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null && pref.getInt("auth", 0) == 1) {
            editor = pref.edit();
            editor.putInt("auth", 0);
            editor.apply();
            finish();
        }
    }

    private static final int PERMISSIONS_REQUEST = 1;
    static boolean student_registered = false;
    static boolean isDriver = false;
    public Driver driver_user;
    String answer = "";
    private class checkUserTypeTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("https://easybusmanagment.000webhostapp.com/loginUser.php?phoneNb=" + strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    answer += line;
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if ((answer.split(" ")[0]).equals("Driver")) {
                // Check location permission is granted - if it is, start
                // the service, otherwise request the permission
                isDriver = true;
                int permission = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);
                if (permission == PackageManager.PERMISSION_GRANTED)
                {

                    String[] array=answer.split(" ");
                    int id=Integer.valueOf(array[1]);
                    String name=array[2]+" "+array[3];
                    String phone=array[4];
                    LatLng houseLatLng=new LatLng(Double.parseDouble(array[5]),Double.parseDouble(array[6]));
                    driver_user=new Driver(id,name,phone,houseLatLng);
                    LatLng schoolLocation=new LatLng(Double.parseDouble(array[7]),Double.parseDouble(array[8]));
                    school=new School("School",schoolLocation);
                    startTrackerService();
                }
                else
                {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST);
                }
              }
              else if(answer.equals("not found"))
              {
                    editor.putBoolean("profile_registered",false).apply();
                    Intent intent=new Intent(getApplication(),RegisterActivity.class);
                    startActivity(intent);
                    finish();
              }
              else {
                if ((answer.split(" ")).length==6)
                {
                    student_registered=true;
                    setAlarmResetAbsence();
                    Intent intent = new Intent(MainActivity.this,NavigationActivity.class);
                    intent.putExtra("user","student");
                    Student student=parseAsStudent(answer);
                    student_user=student;
                    GetDriversTask getDriversTask=new GetDriversTask();
                    getDriversTask.execute(student_user.id);
                    try {
                        student_user.myDrivers=getDriversTask.get();
                    } catch (Exception e) {
                    }
                    if(pref.getBoolean("first_login",true))
                    {
                        RegisterTokenTask taskRegister=new RegisterTokenTask();
                        taskRegister.execute(FirebaseInstanceId.getInstance().getToken(),String.valueOf(student_user.id));
                        editor.putInt("userId",student_user.id).apply();
                        //setAlarmResetAbsence();
                        editor.putBoolean("first_login",false);
                        editor.apply();
                    }
                    startActivity(intent);
                }
                else
                {
                    AlertDialog.Builder alert=new AlertDialog.Builder(finish);
                    alert.setMessage("Network Error..Something went wrong please try again or come back later");
                    alert.setPositiveButton("try again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            runActivity();
                        }
                    });
                    alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                            if (i == KeyEvent.KEYCODE_BACK) {
                                dialogInterface.dismiss();
                                finish();
                            }
                            return false;
                        }
                    });
                    alert.setCancelable(false);
                    alert.create().show();

                }
            }

        }
    }
    public void setAlarmResetAbsence()
    {
        Intent myIntent = new Intent(this, ResetAbsenceReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),1000*60*60*24 ,pendingIntent);
    }
    public Student parseAsStudent(String line) {
        String values[] = line.split(" ");
        String name = values[1] + values[2];
        LatLng latLng=new LatLng(Double.parseDouble(values[4]),Double.parseDouble(values[5]));
        return new Student(Integer.parseInt(values[0]),name,values[3],latLng);
    }
    public static Student student_user;

    public static ArrayList<Student> orderedStudents = new ArrayList<>();


    DataBaseHandler handler = new DataBaseHandler(this);
    public static ArrayList<Student> myStudents = new ArrayList<>();
    public static ArrayList<LatLng> savedDriverTrack = new ArrayList<>();
    public static ArrayList<Student> allOrderedStudents;
    public static ArrayList<Student> orderedStudentsMorning = new ArrayList<>();
    public static ArrayList<Student> orderedStudentsAfternoon = new ArrayList<>();

    private void startTrackerService() {
        Intent intent1 = new Intent(MainActivity.this, NavigationActivity.class);
        intent1.putExtra("user", "driver");
        intent1.putExtra("phone", driver_user.getPhoneNb());
        intent1.putExtra("id",driver_user.getId());
        intent1.putExtra("name",driver_user.getName());
        intent1.putExtra("houseLat",driver_user.getLatLng().latitude);
        intent1.putExtra("houseLng",driver_user.getLatLng().longitude);
        intent1.putExtra("schoolLat",school.getLatLng().latitude);
        intent1.putExtra("schoolLng",school.getLatLng().longitude);
        intent1.putExtra("schoolName",school.getName());


        CollectStudentsTask task = new CollectStudentsTask();
        task.execute(ph);
        GetStudentsOrderedTask getStudentsOrderedTask = new GetStudentsOrderedTask();
        getStudentsOrderedTask.execute(ph);

        try
        {
            myStudents = task.get();
            handler.addStudents(myStudents);
            allOrderedStudents = getStudentsOrderedTask.get();
            handler.addOrderedStudents(allOrderedStudents);
            orderedStudentsMorning.clear();
            orderedStudentsAfternoon.clear();
            for (int i = 0; i < allOrderedStudents.size(); i++)
            {
                Student s = allOrderedStudents.get(i);
                if (s.getMorning() == 1)
                    orderedStudentsMorning.add(s);
                if(s.getAfternoon()==1)
                    orderedStudentsAfternoon.add(0,s);
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
            Toast.makeText(this, "Error in getting arrayList", Toast.LENGTH_SHORT).show();
        }
        startActivity(intent1);
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==123)
        {
            checkUserTypeTask task=new checkUserTypeTask();
            FirebaseAuth auth=FirebaseAuth.getInstance();
            String phone = auth.getCurrentUser().getPhoneNumber();
            String phNbr="";
            char[] elements = phone.toCharArray();
            for (int i = 4; i < elements.length; i++)
                phNbr += elements[i];
            task.execute(phNbr);
        }
    }

}
