package com.app.workstamper;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity
{
    RecyclerView historyView;
    LinearLayoutManager layoutManager;
    HistoryViewAdapter historyViewAdapter;
    ArrayList<Stamper.StampData> stampData;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        stampData = new ArrayList<>();

        // Get data from retrieved docs to stampData classes.
        for(int i = 0; i <= (Stamper.Database.docs.size() - 1); i++)
        {
            if(Stamper.Database.docs.isEmpty())
                break;

            DocumentSnapshot document = Stamper.Database.docs.get(i);

            // Document is invalid or is currently on work state, skip.
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

            stampData.add(new Stamper.StampData(startDate, endDate, startTime, endTime, hadFoodBreak.contains("true"), document.getId()));
        }

        historyView = findViewById(R.id.rView);

        layoutManager = new LinearLayoutManager(this);
        historyView.setLayoutManager(layoutManager);
        historyViewAdapter = new HistoryViewAdapter(stampData);
        historyView.setAdapter(historyViewAdapter);
    }
}
