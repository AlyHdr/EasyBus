package com.example.alihaidar.phoneverification;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class NotificationsFragment extends android.app.Fragment {

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView list_notif=getActivity().findViewById(R.id.list_view_notifications);
        DataBaseHandler handler =new DataBaseHandler(getActivity());
        final ArrayList<Message> notifications=handler.getNotifications();
        if(notifications.size()==0)
        {
            LinearLayout layout=getActivity().findViewById(R.id.layout_no_notif);
            layout.setVisibility(View.VISIBLE);
        }
        NotificationsArrayAdapter adapter=new NotificationsArrayAdapter(getActivity(),notifications);
        list_notif.setAdapter(adapter);


        list_notif.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.list_view_notifications), notifications.get(i).body, BaseTransientBottomBar.LENGTH_LONG);
                snackbar.setAction("DELETE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alert=new AlertDialog.Builder(getActivity());
                        alert.setMessage("Are you sure you want to delete this message ?");
                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //do the math
                            }
                        });
                        alert.setNegativeButton("No",null);
                        alert.create().show();
                    }
                });
                snackbar.show();
                return false;
            }
        });
    }
}
