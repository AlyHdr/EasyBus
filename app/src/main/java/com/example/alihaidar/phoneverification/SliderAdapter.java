package com.example.alihaidar.phoneverification;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Ali Haidar on 7/15/2018.
 */

public class SliderAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context) {
        this.context = context;
    }
    public int[] slider_images={R.drawable.ic_bus_on_map , R.drawable.ic_find_bus,R.drawable.notifications};
    public String[] slider_headers={"Bus Tracking","Find Your Bus","Notifications"};
    public String[] slider_descriptions={"You can easily track you bus location on the map"
            ,"Find the bus that most fit your location and the institution you want ot go to"
            ,"Get notified whenever the bus reaches your home and for dynamic interactions"};
    @Override
    public int getCount() {
        return slider_headers.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==(RelativeLayout)object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.layout_slide,container,false);

        ImageView slider_image=view.findViewById(R.id.slider_image_view);
        TextView slider_header=view.findViewById(R.id.slider_header);
        TextView slider_desc=view.findViewById(R.id.slider_description);

        slider_image.setImageResource(slider_images[position]);
        slider_header.setText(slider_headers[position]);
        slider_desc.setText(slider_descriptions[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }
}
