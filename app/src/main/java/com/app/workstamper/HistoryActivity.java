package com.app.workstamper;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class HistoryActivity extends AppCompatActivity
{
    RecyclerView historyView;
    LinearLayoutManager layoutManager;
    HistoryViewAdapter historyViewAdapter;
    ArrayList<Stamper.StampData> stampData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        // TODO: Fetch real database data, this stuff here is just manual testing for dynamic view.
        stampData.add(new Stamper.StampData("20.10.2021", "20.10.2021", "10:00", "18:00", false));
        stampData.add(new Stamper.StampData("10.10.2021", "10.10.2021", "12:00", "15:00", true));
        historyView = findViewById(R.id.rView);

        layoutManager = new LinearLayoutManager(this);
        historyView.setLayoutManager(layoutManager);
        historyViewAdapter = new HistoryViewAdapter(stampData);
        historyView.setAdapter(historyViewAdapter);
    }
}
