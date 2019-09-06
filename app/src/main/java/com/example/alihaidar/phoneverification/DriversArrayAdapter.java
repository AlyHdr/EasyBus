package com.example.alihaidar.phoneverification;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ali Haidar on 8/2/2018.
 */

public class DriversArrayAdapter extends ArrayAdapter<String> {
    public DriversArrayAdapter(@NonNull Context context, @NonNull ArrayList<String> objects) {
        super(context, 0, objects);
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String driverName= getItem(position);
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_driver_item, null, false);
        }
        TextView textView=convertView.findViewById(R.id.text_driver_name_add_drivers);
        textView.setText(driverName);
        return convertView; }
}
