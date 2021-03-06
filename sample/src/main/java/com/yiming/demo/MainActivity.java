package com.yiming.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String[] items = {"RecyclerView", "ListView", "GridView", "WebView", "ScrollView", "Non-ScrollView"};
    private Class[] acts = {RecyclerViewDemo.class, ListViewDemo.class,  GridViewDemo.class,
            WebViewDemo.class, ScrollViewDemo.class, NonScrollViewDemo.class};
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = new ListView(this);
        setContentView(listView);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(this, acts[position]));
    }
}
