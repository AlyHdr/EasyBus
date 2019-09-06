package com.example.alihaidar.phoneverification;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;
    public static Activity finish;
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        FragmentManager fm = getFragmentManager();
        android.app.FragmentTransaction transaction = fm.beginTransaction();
        SharedPreferences preferences=getPreferences(MODE_PRIVATE);
        if (getIntent().getExtras().getString("user").equals("driver"))
        {
            if (getPreferences(MODE_PRIVATE).getBoolean("firstLaunch", true))
            {
                DrawerLayout dr = findViewById(R.id.drawer_layout);
                dr.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                android.app.FragmentManager manager=getFragmentManager();
                android.app.FragmentTransaction tr=manager.beginTransaction();
                OrderStudentsFragment orderStudentsFragment = new OrderStudentsFragment();
                AppBarLayout layout=findViewById(R.id.app_bar);
                layout.setVisibility(View.GONE);
                tr.replace(R.id.content_navigation, orderStudentsFragment, "OrderStudentsFragment");
                tr.commit();
            }
            else
            {
                map_launched=true;
                onNavigationItemSelected(navigationView.getMenu().getItem(0).setChecked(true));
                fragment = new MapsFragmentDriver();
                transaction.replace(R.id.content_navigation, fragment, "MapsFragmentDriver");
                transaction.commit();
            }
        }
        else if (getIntent().getExtras().getString("user").equals("student"))
        {
            map_launched=true;
            onNavigationItemSelected(navigationView.getMenu().getItem(4).setChecked(true));
            fragment = new MapsFragmentStudent();
            transaction.replace(R.id.content_navigation, fragment, "MapsFragmentStudent");
            transaction.commit();

        }
        finish = this;
        init();
    }

    public void init() {
        if (getIntent().getExtras().get("user").equals("driver"))
        {
            navigationView.getMenu().setGroupVisible(R.id.group_navig_driver, true);
        }
        else if (getIntent().getExtras().get("user").equals("student")) {
            navigationView.getMenu().setGroupVisible(R.id.group_navig_student, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        FragmentManager manager=getFragmentManager();
//        MapsFragmentDriver fragmentDriver=(MapsFragmentDriver)manager.findFragmentById(R.id.fragment_maps_driver);
//        System.out.println("-----------------------"+fragmentDriver.getActivity());
        if (requestCode == 1 || requestCode==12)
        {
            Fragment fragment1=getFragmentManager().findFragmentByTag("MapsFragmentDriver");
            fragment1.onActivityResult(requestCode, resultCode, data);
        }
        else if(requestCode==2)
        {
            Fragment fragment=getFragmentManager().findFragmentByTag("StudentSettings");
            fragment.onActivityResult(requestCode,resultCode,data);
        }

    }
    public static boolean map_launched;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            if(getPreferences(MODE_PRIVATE).getBoolean("firstLaunch",true))
            {
                finish();
                MainActivity.finish.finish();
            }
            else
            {
                if(map_launched) {
                    MainActivity.finish.finish();
                    this.finish();
                }
                else
                {
                    map_launched=true;
                    navigationView.getMenu().getItem(0).setChecked(true);
                    FragmentManager fm = getFragmentManager();
                    if(getIntent().getExtras().getString("user").equals("driver"))
                    {
                        navigationView.getMenu().getItem(0).setChecked(true);
                        fm.beginTransaction().replace(R.id.content_navigation,new MapsFragmentDriver()).commit();
                    }
                    else {
                        navigationView.getMenu().getItem(4).setChecked(true);
                        fm.beginTransaction().replace(R.id.content_navigation, new MapsFragmentStudent()).commit();
                    }
                }
            }
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }
    public void make_zero(View view)
    {
        SharedPreferences pref=getSharedPreferences("next_student", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=pref.edit();
        editor.putInt("next",0);
        editor.commit();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FragmentManager fm = getFragmentManager();
        int id = item.getItemId();
        android.app.FragmentTransaction transaction = fm.beginTransaction();

        if (getIntent().getExtras().getString("user").equals("driver"))
        {

            if (id == R.id.nav_mapDriver)
            {
                map_launched=true;
                MapsFragmentDriver fragment = new MapsFragmentDriver();
                transaction.replace(R.id.content_navigation, fragment, "MapsFragmentDriver");
                transaction.commit();
            }
            else if (id == R.id.nav_orderStudents)
            {
                map_launched=false;
                OrderStudentsFragment fragment = new OrderStudentsFragment();
                transaction.replace(R.id.content_navigation, fragment, "StudentOrderFragment");
                transaction.commit();
            }
            else if (id == R.id.nav_manage_driver)
            {
                map_launched=false;
                ManageFragment fragment = new ManageFragment();
                transaction.replace(R.id.content_navigation, fragment, "ManageFragment");
                transaction.commit();
            }
            else if (id == R.id.nav_notif_driver)
            {
                map_launched=false;
                NotificationsFragment fragment = new NotificationsFragment();
                transaction.replace(R.id.content_navigation, fragment, "NotificaitonsFragment");
                transaction.commit();
            }
            else if (id == R.id.nav_share_driver)
            {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Download Easy Bus for easy bussing :)";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Easy Bus");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
            else if (id == R.id.nav_send_driver)
            {

            }
        }
        else if (getIntent().getExtras().getString("user").equals("student"))
        {
            switch (id)
            {
                case R.id.nav_map_student:
                    map_launched=true;
                    MapsFragmentStudent fragment = new MapsFragmentStudent();
                    transaction.replace(R.id.content_navigation, fragment, "MapsFragmentStudent");
                    transaction.commit();
                    break;
                case R.id.nav_drivers_student:
                    map_launched=false;
                    MyDriversFragment fragment0 = new MyDriversFragment();
                    transaction.replace(R.id.content_navigation, fragment0, "MyDriversFragment");
                    transaction.commit();
                    break;
                case R.id.nav_notif_student:
                    map_launched=false;
                    NotificationsFragment fragment_notif = new NotificationsFragment();
                    transaction.replace(R.id.content_navigation, fragment_notif, "NotificationsFragment");
                    transaction.commit();
                    break;
                case R.id.nav_abscence_student:
                    map_launched=false;
                    StudentAbsenceFragment fragment1 = new StudentAbsenceFragment();
                    transaction.replace(R.id.content_navigation, fragment1, "StudentAbsenceFragment");
                    transaction.commit();
                    break;
                case R.id.nav_settings_student:
                    map_launched=false;
                    StudentSettings fragment2=new StudentSettings();
                    transaction.replace(R.id.content_navigation, fragment2, "StudentSettings");
                    transaction.commit();
                    break;
                case R.id.nav_share_student:
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = "Download Easy Bus for easy bussing :)";
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Easy Bus");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    break;
            }
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}