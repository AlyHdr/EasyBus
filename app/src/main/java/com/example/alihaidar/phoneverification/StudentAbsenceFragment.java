package com.example.alihaidar.phoneverification;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.Context.ALARM_SERVICE;


public class StudentAbsenceFragment extends Fragment {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    static Fragment thisFragment;
    Student student;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_absence, container, false);
    }
    CheckBox morning_checkBox;
    CheckBox after_noon_checkBox;
    RadioButton radio_tmrw;
    RadioButton radio_today;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        thisFragment=this;
        morning_checkBox=getActivity().findViewById(R.id.checkbox_morning_absence);
        after_noon_checkBox=getActivity().findViewById(R.id.checkbox_afternoon_absence);

        radio_tmrw=getActivity().findViewById(R.id.radio_tmrw);
        radio_today=getActivity().findViewById(R.id.radio_today);

        student=MainActivity.student_user;
        final Button btn_register_absence=getActivity().findViewById(R.id.btn_register_absence);
        final Button btn_cancel_absence=getActivity().findViewById(R.id.btn_cancel_absence);
        final TextView text_absence=getActivity().findViewById(R.id.text_absence);

        int morning=MainActivity.student_user.morning;
        final int afternoon=MainActivity.student_user.afternoon;

        if(morning==0 || afternoon==0)
        {
            if(morning==0)
                morning_checkBox.setChecked(true);
            else
                morning_checkBox.setEnabled(false);
            if(afternoon==0)
                after_noon_checkBox.setChecked(true);
            else
                after_noon_checkBox.setEnabled(false);

            btn_register_absence.setVisibility(View.GONE);
            btn_cancel_absence.setVisibility(View.VISIBLE);
            text_absence.setText("Absence is reported");
            radio_today.setEnabled(false);
            radio_tmrw.setEnabled(false);
        }
        btn_register_absence.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if(hour>18)
                {
                    Snackbar snackbar=Snackbar.make(getActivity().findViewById(R.id.app_bar),"You can't report for today right now!", BaseTransientBottomBar.LENGTH_INDEFINITE);
                    View snack_view=snackbar.getView();
                    snack_view.setBackgroundColor(Color.RED);
                    snackbar.show();
                }
                else {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setMessage("Are you sure you want to report absence?");
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (radio_tmrw.isChecked()) {
                                if (!morning_checkBox.isChecked() && !after_noon_checkBox.isChecked())
                                    Snackbar.make(getActivity().findViewById(R.id.content_navigation), "Please check a path", Snackbar.LENGTH_LONG).show();
                                else {
                                    SharedPreferences preferences = getActivity().getSharedPreferences("absence", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();

                                    if (morning_checkBox.isChecked()) {
                                        morning_checkBox.setEnabled(true);
                                        editor.putInt("morning", 0);
                                        MainActivity.student_user.morning = 0;
                                    } else {
                                        morning_checkBox.setEnabled(false);
                                        editor.putInt("morning", 1);
                                        MainActivity.student_user.morning = 1;
                                    }
                                    if (after_noon_checkBox.isChecked()) {
                                        after_noon_checkBox.setEnabled(true);
                                        editor.putInt("afternoon", 0);
                                        MainActivity.student_user.afternoon = 0;
                                    } else {
                                        after_noon_checkBox.setEnabled(false);
                                        editor.putInt("afternoon", 1);
                                        MainActivity.student_user.afternoon = 1;
                                    }
                                    editor.apply();

                                    Intent myIntent = new Intent(getContext(), AbsenceTomorrowReciever.class);
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, myIntent, 0);
                                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);

                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(System.currentTimeMillis());
                                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                                    calendar.set(Calendar.MINUTE, 1);
                                    calendar.set(Calendar.SECOND, 0);
                                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                                    Snackbar.make(getActivity().findViewById(R.id.content_navigation), "Reported for tomorrow", Snackbar.LENGTH_LONG).show();
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.detach(thisFragment).attach(thisFragment).commit();
                                    text_absence.setText("Cancel Absence");
                                    radio_today.setEnabled(false);
                                    radio_tmrw.setEnabled(false);
                                    btn_register_absence.setVisibility(View.GONE);
                                    btn_cancel_absence.setVisibility(View.VISIBLE);
                                }
                            } else if (radio_today.isChecked()) {

                                if (!morning_checkBox.isChecked() && !after_noon_checkBox.isChecked())
                                    Snackbar.make(getActivity().findViewById(R.id.content_navigation), "Please check a path", Snackbar.LENGTH_LONG).show();
                                else {
                                    int flag_morning = 1;
                                    int flag_afternoon = 1;
                                    MainActivity.student_user.afternoon = 1;
                                    MainActivity.student_user.morning = 1;
                                    morning_checkBox.setEnabled(false);
                                    after_noon_checkBox.setEnabled(false);
                                    if (morning_checkBox.isChecked()) {
                                        morning_checkBox.setEnabled(true);
                                        flag_morning = 0;
                                        MainActivity.student_user.morning = 0;
                                    }
                                    if (after_noon_checkBox.isChecked()) {
                                        after_noon_checkBox.setEnabled(true);
                                        flag_afternoon = 0;
                                        MainActivity.student_user.afternoon = 0;
                                    }
                                    android.support.v7.app.AlertDialog alert = new android.support.v7.app.AlertDialog.Builder(getActivity()).create();
                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                    View alertLayout = inflater.inflate(R.layout.layout_loading, null);
                                    TextView text_load = alertLayout.findViewById(R.id.text_loading);
                                    text_load.setText("Registering absence please wait..");
                                    alert.setView(alertLayout);
                                    alert.setCancelable(false);
                                    alert.show();
                                    UpdateAbsenceTask task = new UpdateAbsenceTask(getActivity(), alert);
                                    task.execute(student.id, flag_morning, flag_afternoon);
                                    text_absence.setText("Cancel Absence");
                                    radio_today.setEnabled(false);
                                    radio_tmrw.setEnabled(false);
                                    btn_register_absence.setVisibility(View.GONE);
                                    btn_cancel_absence.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });
                    alert.setNegativeButton("No", null);
                    alert.create().show();
                }
            }
        });
        btn_cancel_absence.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert=new AlertDialog.Builder(getContext());
                alert.setMessage("Are you sure you want to cancel absence?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(morning_checkBox.isChecked() && after_noon_checkBox.isChecked())
                        {
                            Snackbar.make(getActivity().findViewById(R.id.content_navigation),"Please uncheck a path",Snackbar.LENGTH_LONG).show();
                        }
                        else {
                            if(radio_today.isChecked()) {
                                int flag_morning = 1;
                                int flag_afternoon = 1;
                                MainActivity.student_user.afternoon = 1;
                                MainActivity.student_user.morning = 1;
                                if (morning_checkBox.isChecked()) {
                                    flag_morning = 0;
                                    MainActivity.student_user.morning = 0;
                                }
                                if (after_noon_checkBox.isChecked()) {
                                    flag_afternoon = 0;
                                    MainActivity.student_user.afternoon = 0;
                                }
                                android.support.v7.app.AlertDialog alert = new android.support.v7.app.AlertDialog.Builder(getActivity()).create();
                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                View alertLayout = inflater.inflate(R.layout.layout_loading, null);
                                TextView text_load=alertLayout.findViewById(R.id.text_loading);
                                text_load.setText("Canceling absence please wait..");
                                alert.setView(alertLayout);
                                alert.setCancelable(false);
                                alert.show();
                                UpdateAbsenceTask task = new UpdateAbsenceTask(getContext(),alert);
                                task.execute(student.id, flag_morning, flag_afternoon);
                                text_absence.setText("Report Absence");
                                radio_today.setEnabled(true);
                                radio_tmrw.setEnabled(true);
                                morning_checkBox.setEnabled(true);
                                after_noon_checkBox.setEnabled(true);
                                btn_cancel_absence.setVisibility(View.GONE);
                                btn_register_absence.setVisibility(View.VISIBLE);
                            }
                            else if(radio_tmrw.isChecked())
                            {
                                Intent myIntent = new Intent(getContext(), AbsenceTomorrowReciever.class);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, myIntent, 0);
                                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                                alarmManager.cancel(pendingIntent);
                            }
                        }
                    }
                });
                alert.setNegativeButton("No",null);
                alert.create().show();
            }
        });
    }
}
