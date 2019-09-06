package com.example.alihaidar.phoneverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;

import java.util.ArrayList;

public class MyDriversFragment extends android.app.Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public void init(String query)
    {
        objects = new ArrayList<>();
        ArrayList<String> filtered=new ArrayList<>();
        for(int i=0;i<12;i++)
        {
            objects.add((i+1)+" Hello");
        }
        if(searchView !=null)
        {
            for (int i=0;i<12;i++)
            {
                if(objects.get(i).toLowerCase().startsWith(query.toLowerCase()))
                    filtered.add(objects.get(i));
            }
            adapter=new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,filtered);
            listView.setAdapter(adapter);
        }
        else
        {
            adapter=new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,objects);
            listView.setAdapter(adapter);
        }


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_drivers, container, false);
    }
    ArrayList<String> objects;
    ListView listView;
    ArrayAdapter<String> adapter;
    FloatingSearchView searchView;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchView=getActivity().findViewById(R.id.floating_search_view);
        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                init(newQuery);
            }

        });
        listView=getActivity().findViewById(R.id.list_view_institutions);
        init("");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle=new Bundle();
                bundle.putString("destination",adapterView.getItemAtPosition(i).toString());
                FragmentAddDrivers frag=new FragmentAddDrivers();
                frag.setArguments(bundle);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction=fm.beginTransaction();
                transaction.replace(R.id.content_navigation,frag);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });
    }
}
