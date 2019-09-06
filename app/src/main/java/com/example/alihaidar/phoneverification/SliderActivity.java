package com.example.alihaidar.phoneverification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SliderActivity extends AppCompatActivity {

    private ViewPager slider_pager;
    private LinearLayout dots_layout;
    private SliderAdapter sliderAdapter;
    private TextView mdots[];
    int currentPos=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);

        btn_next=findViewById(R.id.button_next);
        btn_skip=findViewById(R.id.button_skip);
        slider_pager=findViewById(R.id.slider_view_pager);
        dots_layout=findViewById(R.id.dots_layout);

        sliderAdapter=new SliderAdapter(this);
        slider_pager.setAdapter(sliderAdapter);
        addDotsIndicators(0);
        slider_pager.addOnPageChangeListener(viewListener);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentPos<2)
                    slider_pager.setCurrentItem(currentPos + 1);
                else
                {
                    SharedPreferences pref=getSharedPreferences("authenticated", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor=pref.edit();
                    editor.putBoolean("first_launch",false);
                    editor.apply();
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });
    }
    Button btn_skip;
    Button btn_next;
    public void addDotsIndicators(int position)
    {
        mdots=new TextView[3];
        dots_layout.removeAllViews();
        for(int i=0;i<mdots.length;i++)
        {
            mdots[i]=new TextView(this);
            mdots[i].setText(Html.fromHtml("&#8226;"));
            mdots[i].setTextSize(50);
            mdots[i].setTextColor(getResources().getColor(R.color.color_white));

            dots_layout.addView(mdots[i]);
        }
        if(mdots.length>0)
            mdots[position].setTextColor(getResources().getColor(R.color.colorAccent));
    }
    ViewPager.OnPageChangeListener viewListener=new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicators(position);
            currentPos=position;
            if(position==2)
            {
                btn_next.setText("Finish");
                btn_skip.setVisibility(View.INVISIBLE);
            }
            else
            {
                btn_next.setText("next");
                btn_skip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
