package com.example.alihaidar.phoneverification;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ali Haidar on 7/17/2018.
 */

public class NotificationsArrayAdapter extends ArrayAdapter<Message> {

    public NotificationsArrayAdapter(@NonNull Context context, @NonNull ArrayList<Message> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
         Message message= getItem(position);
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.message_item, null, false);
        }

        TextView title = convertView.findViewById(R.id.message_title);
        TextView body = convertView.findViewById(R.id.message_body);
        TextView date = convertView.findViewById(R.id.message_date);

        // Populate the data into the template view using the data object
        title.setText(message.title);

        body.setText(message.body);

        date.setText(message.date);

        // Return the completed view to render on screen
        return convertView;

    }
}
