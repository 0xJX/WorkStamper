package com.app.workstamper;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Document;

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

        for(int i = 0; i <= (Stamper.Database.docs.size() - 1); i ++)
        {
            if(Stamper.Database.docs.isEmpty())
                break;

            DocumentSnapshot document = Stamper.Database.docs.get(i);

            if(document == null || !document.contains("EndTime"))
                continue;

            String
                hadFoodBreak = document.getString("HadFoodBreak"),
                startTime = document.getString("StartTime"),
                startDate = document.getString("StartDate"),
                endTime = document.getString("EndTime"),
                endDate = document.getString("EndDate");

            if(startTime == null || hadFoodBreak == null)
                continue;

            stampData.add(new Stamper.StampData(startDate, endDate, startTime, endTime, hadFoodBreak.contains("true")));
            stampData.get(i).id = document.getId();
        }
        historyView = findViewById(R.id.rView);

        layoutManager = new LinearLayoutManager(this);
        historyView.setLayoutManager(layoutManager);
        historyViewAdapter = new HistoryViewAdapter(stampData);
        historyView.setAdapter(historyViewAdapter);
    }
}
