/*
  Copyright 2014 Magnus Woxblom
  <p/>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p/>
  http://www.apache.org/licenses/LICENSE-2.0
  <p/>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.example.alihaidar.phoneverification;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class OrderStudentsFragment extends Fragment {

    private ArrayList<Student> mItemArray;
    private DragListView mDragListView;
    private Activity myActivity;
    Button chooseTimeButton;
    boolean morning;
    SharedPreferences defaultPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        defaultPreferences=PreferenceManager.getDefaultSharedPreferences(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_students_list_layout1, container, false);

        mDragListView = view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(false);

        mItemArray = new ArrayList<>();
        Calendar calendar=Calendar.getInstance();
        int currentHour=calendar.get(Calendar.HOUR_OF_DAY);
        if(currentHour>=6 && currentHour<=9)
            morning=true;
        else
            morning=false;
        //Toast.makeText(myActivity, ""+mItemArray.size(), Toast.LENGTH_SHORT).show();
        addItemsToArray(MainActivity.allOrderedStudents);
        setupListRecyclerView();
        myActivity=this.getActivity();

        return view;
    }
    public void addItemsToArray(ArrayList<Student> arrayList)
    {
        for (int i = 0; i < arrayList.size(); i++)
        {
            Student s=arrayList.get(i);
            mItemArray.add(new Student(s.getId(),s.getName(),s.getPhNumber(),s.getLatLng(),s.getDriverPhone(),s.getMorning(),s.getAfternoon(),s.getFenceRaduis()));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menuItem_abscentStudents:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Absent Students");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
                for(int i=0;i<mItemArray.size();i++)
                {
                    if(morning)
                    {
                        if (mItemArray.get(i).getMorning() == 0)
                            arrayAdapter.add(mItemArray.get(i).getName());
                    }
                    else
                    {
                        if (mItemArray.get(i).getAfternoon() == 0)
                            arrayAdapter.add(mItemArray.get(i).getName());
                    }
                }
                builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupListRecyclerView() {
        mDragListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.order_students_list_item, R.id.image, false);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(new MyDragItem(getActivity(), R.layout.order_students_list_item));
    }

    private static class MyDragItem extends DragItem {

        MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text = ((TextView) clickedView.findViewById(R.id.text)).getText();
            ((TextView) dragView.findViewById(R.id.text)).setText(text);
            dragView.findViewById(R.id.item_layout).setBackgroundColor(dragView.getResources().getColor(R.color.color_trans));
        }
    }

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    AlertDialog alert_load;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferences=getActivity().getPreferences(getActivity().MODE_PRIVATE);


        if(preferences.getBoolean("firstLaunch", true))
        {
            Snackbar.make(getActivity().findViewById(R.id.saveOrderedStudents),"This is your first launch, order your students. ",Snackbar.LENGTH_LONG);
        }
        editor=preferences.edit();
        Button saveButton=getActivity().findViewById(R.id.saveOrderedStudents);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                alert_load = new AlertDialog.Builder(getActivity()).create();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.layout_loading, null);
                TextView text_load=alertLayout.findViewById(R.id.text_loading);
                text_load.setText("Saving order please wait..");
                alert_load.setView(alertLayout);
                alert_load.setCancelable(false);
                alert_load.show();

                String [] idsArray=new String[mItemArray.size()];

                MainActivity.allOrderedStudents.clear();
                MainActivity.orderedStudentsMorning.clear();
                MainActivity.orderedStudentsAfternoon.clear();
                for(int i=0;i<idsArray.length;i++)
                {
                    Student s=mItemArray.get(i);
                    idsArray[i]=String.valueOf(mItemArray.get(i).getId());
                    MainActivity.allOrderedStudents.add(new Student(s.getId(),s.getName(),s.getPhNumber(),s.getLatLng(),s.getDriverPhone(),s.getMorning(),s.getAfternoon(),s.getFenceRaduis()));
                    if(s.getMorning()==1)
                        MainActivity.orderedStudentsMorning.add(new Student(s.getId(),s.getName(),s.getPhNumber(),s.getLatLng(),s.getDriverPhone(),s.getMorning(),s.getAfternoon(),s.getFenceRaduis()));
                    if(s.getAfternoon()==1) {
                        MainActivity.orderedStudentsAfternoon.add(0,new Student(s.getId(),s.getName(),s.getPhNumber(),s.getLatLng(),s.getDriverPhone(),s.getMorning(),s.getAfternoon(),s.getFenceRaduis()));
                    }
                }

                sendMessage(idsArray);
                if(preferences.getBoolean("firstLaunch",true))
                {
                    Fragment fragment=getActivity().getFragmentManager().findFragmentByTag("OrderStudentsFragment");
                    android.app.FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                    transaction.remove(fragment);
                    transaction.commit();

                    NavigationActivity.map_launched=true;
                    AppBarLayout layout=getActivity().findViewById(R.id.app_bar);
                    layout.setVisibility(View.VISIBLE);
                    android.app.FragmentTransaction transaction1=getActivity().getFragmentManager().beginTransaction();
                    MapsFragmentDriver fragmentDriver = new MapsFragmentDriver();
                    transaction1.replace(R.id.content_navigation, fragmentDriver, "MapsFragmentDriver");
                    transaction1.commit();

                }
            }
        });

        chooseTimeButton=getActivity().findViewById(R.id.choose_time_button);
        chooseTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.layout_alert_time_picker1, null);
                alert.setView(alertLayout);
                final TimePicker timePicker = alertLayout.findViewById(R.id.timePicker);
                final RadioButton radioGoing=alertLayout.findViewById(R.id.radioGoing);
                RadioButton radioComing=alertLayout.findViewById(R.id.radioComing);

                String s1 = preferences.getString("timePickedForGoingAlarm", "06:00");
                String[] array = s1.split(":");
                int hour = Integer.parseInt(array[0]);
                int minute = Integer.parseInt(array[1]);
                timePicker.setCurrentHour(hour);
                timePicker.setCurrentMinute(minute);

                radioGoing.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view) {
                        String s1 = preferences.getString("timePickedForGoingAlarm", "06:00");
                        String[] array = s1.split(":");
                        int hour = Integer.parseInt(array[0]);
                        int minute = Integer.parseInt(array[1]);
                        timePicker.setCurrentHour(hour);
                        timePicker.setCurrentMinute(minute);
                    }
                });
                radioComing.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view) {
                        String s1 = preferences.getString("timePickedForComingAlarm", "02:00");
                        String[] array = s1.split(":");
                        int hour = Integer.parseInt(array[0]);
                        int minute = Integer.parseInt(array[1]);
                        timePicker.setCurrentHour(hour);
                        timePicker.setCurrentMinute(minute);
                    }
                });

                alert.setView(alertLayout);
                alert.setPositiveButton("Set", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        int hour=timePicker.getCurrentHour();
                        int minutes=timePicker.getCurrentMinute();
                        if(radioGoing.isChecked())
                        {
                            //this step in order to prevent 1:5 and be 01:05
                            String minutesStr=""+minutes;
                            String hourStr=""+hour;
                            if(hour<10)
                                hourStr="0"+hour;
                            if(minutes<10)
                                minutesStr="0"+minutes;
                            editor.putString("timePickedForGoingAlarm",""+hourStr+":"+minutesStr);
                            editor.apply();

                            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                            Intent notificationIntent = new Intent(getActivity(), AlarmReceiverGoing.class);
                            notificationIntent.putExtra("tracking",preferences.getBoolean("tracking",false));
                            PendingIntent broadcast = PendingIntent.getBroadcast(getActivity(), 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            Calendar customCalendar = Calendar.getInstance();
                            customCalendar.set(Calendar.HOUR_OF_DAY, hour);
                            customCalendar.set(Calendar.MINUTE,minutes);
                            customCalendar.set(Calendar.SECOND,0);
                            customCalendar.set(Calendar.MILLISECOND, 0);
                            if(customCalendar.before(Calendar.getInstance()))
                            {
                                customCalendar.add(Calendar.DATE, 1);
                            }
                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, customCalendar.getTimeInMillis(),1000*60*60*24 ,broadcast);
                        }
                        else
                        {
                            //this step in order to prevent 1:5 and be 01:05
                            String minutesStr=""+minutes;
                            String hourStr=""+hour;
                            if(hour<10)
                                hourStr="0"+hour;
                            if(minutes<10)
                                minutesStr="0"+minutes;
                            editor.putString("timePickedForComingAlarm",""+hourStr+":"+minutesStr);
                            editor.apply();

                            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                            Intent notificationIntent = new Intent(getActivity(), AlarmReceiverComing.class);
                            notificationIntent.putExtra("tracking",preferences.getBoolean("tracking",false));
                            PendingIntent broadcast = PendingIntent.getBroadcast(getActivity(), 101, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            Calendar customCalendar = Calendar.getInstance();
                            customCalendar.set(Calendar.HOUR_OF_DAY, hour);
                            customCalendar.set(Calendar.MINUTE,minutes);
                            customCalendar.set(Calendar.SECOND,0);
                            customCalendar.set(Calendar.MILLISECOND, 0);
                            if(customCalendar.before(Calendar.getInstance()))
                            {
                                customCalendar.add(Calendar.DATE, 1);
                            }
                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, customCalendar.getTimeInMillis(),1000*60*60*24 , broadcast);

                        }
                    }
                });
                alert.show();
            }
        });
    }

    public void sendMessage(final String [] arrayOfIds) {
        String driverPhone=getActivity().getIntent().getExtras().getString("phone");
        String app_server_url2 = "https://easybusmanagment.000webhostapp.com/orderStudents.php?driverPhone="+driverPhone;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, app_server_url2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(myActivity, "Saved", Toast.LENGTH_SHORT).show();
                alert_load.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                String str="";
                for(int i=0;i<arrayOfIds.length;i++)
                {
                    if(i==arrayOfIds.length-1)
                        str+=arrayOfIds[i];
                    else
                        str+=arrayOfIds[i]+",";
                }
                params.put("studentsIds",str);
                return params;
            }
        };
        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }
}
