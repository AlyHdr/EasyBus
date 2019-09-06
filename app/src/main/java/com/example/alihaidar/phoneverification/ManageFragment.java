package com.example.alihaidar.phoneverification;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ManageFragment extends PreferenceFragment {
    String phoneNb;
    ListPreference preferenceListMapType;
    SwitchPreference preferenceSwitchShowTrack;
    ListPreference getPreferenceListTrackType;
    ListPreference preferenceListAlert;
    SharedPreferences preferences;
    SharedPreferences.Editor preferenceEditor;
    SharedPreferences defaultPreferences;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.driver_settings_xml);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        phoneNb = getActivity().getIntent().getExtras().getString("phone");
        preferences = getActivity().getPreferences(getActivity().MODE_PRIVATE);
        preferenceEditor = preferences.edit();
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        preferenceListMapType = (ListPreference) getPreferenceManager().findPreference("preference_list_mapType");
        preferenceSwitchShowTrack = (SwitchPreference) getPreferenceManager().findPreference("preference_switch_showTrack");
        getPreferenceListTrackType = (ListPreference) getPreferenceManager().findPreference("preference_list_trackType");
        preferenceListAlert = (ListPreference) getPreferenceManager().findPreference("preference_list_sendAlert");

        preferenceListMapType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                preferenceListMapType.setSummary((String) o);
                return true;
            }
        });
        preferenceSwitchShowTrack.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                return true;
            }
        });
        getPreferenceListTrackType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Toast.makeText(getActivity(), "" + o, Toast.LENGTH_SHORT).show();
                getPreferenceListTrackType.setSummary("" + o);
                ChangeTrackTypeTask changeTrackTypeTask = new ChangeTrackTypeTask(phoneNb, String.valueOf(o));
                changeTrackTypeTask.execute();
                return true;
            }
        });
        final String driverId=String.valueOf(getActivity().getIntent().getExtras().getInt("id",2));
        preferenceListAlert.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object o) {
                final String notif = "" + o;
                if (notif.equals("Custom")) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setMessage("Write custom message: ");
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final View alertLayout = inflater.inflate(R.layout.layout_alert_custom_notif, null);
                    alert.setView(alertLayout);
                    alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String notif = ((EditText) alertLayout.findViewById(R.id.editText_custom_notif)).getText().toString();
                            if (!notif.equals("")) {
                                sendBroadcast(notif,driverId);
                                Toast.makeText(getActivity(), "sent", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(getActivity(), "Empty Message", Toast.LENGTH_SHORT).show();
                        }
                    });
                    alert.show();
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Send?");
                    alert.setMessage("Send '" + notif + "' to all students?");
                    alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendBroadcast(notif, driverId);
                        }
                    });
                    alert.show();
                }
                return false;
            }
        });

        return inflater.inflate(R.layout.fragment_manage, container, false);
    }
    boolean morning=MapsFragmentDriver.morning;

    public void sendBroadcast(String message,String id) {
        AlertDialog alert_load = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_loading, null);
        TextView text_load=alertLayout.findViewById(R.id.text_loading);
        text_load.setText("Sending your broadcast...");
        alert_load.setView(alertLayout);
        alert_load.setCancelable(false);
        alert_load.show();
        SendBroadCastTask task=new SendBroadCastTask(getActivity(),alert_load);
        task.execute(message,id);
    }
}
