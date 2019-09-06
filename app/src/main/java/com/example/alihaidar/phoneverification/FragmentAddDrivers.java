package com.example.alihaidar.phoneverification;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class FragmentAddDrivers extends android.app.Fragment {

    public FragmentAddDrivers() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    String destination;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        destination=getArguments().getString("destination");
        return inflater.inflate(R.layout.fragment_fragment_add_drivers, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView=getActivity().findViewById(R.id.list_view_drivers);
        ArrayList<String> names=new ArrayList<>();
        names.add("Ali Haidar");
        names.add("Bilal Rammal");
        names.add("France taj rasak");
        DriversArrayAdapter adapter=new DriversArrayAdapter(getActivity(),names);
        listView.setAdapter(adapter);
    }
}
