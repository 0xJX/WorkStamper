package com.app.workstamper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    private Button
            timeBtn,
            dateBtn,
            stampBtn;

    private CheckBox
            foodBreakBox;
    private TextView
            hoursLbl;
    private boolean
            isWorking = false;
    private Calendar
            selectedDateTime,
            storedDateTime;

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
           Bundle extras = getIntent().getExtras();
           String companyID = extras.getString("CompanyID");
        */

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        timeBtn = findViewById(R.id.timeButton);
        dateBtn = findViewById(R.id.dateButton);
        stampBtn = findViewById(R.id.stampButton);
        hoursLbl = findViewById(R.id.hoursLabel);
        foodBreakBox = findViewById(R.id.fbreakCheckbox);
        UpdateView();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        // User was null, return back to login. (unless debugSkipLogin is set to true)
        if (user == null && !LoginActivity.debugSkipLogin)
        {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    void UpdateView()
    {
        // Get Current Time
        selectedDateTime = Calendar.getInstance();

        timeBtn.setText(timeToStringFormat(selectedDateTime));
        dateBtn.setText(dateToStringFormat(selectedDateTime));
        stampBtn.setText(this.isWorking ? "Stop work" : "Start work");
        hoursLbl.setVisibility(this.isWorking ? View.VISIBLE : View.INVISIBLE);
        foodBreakBox.setVisibility(this.isWorking ? View.VISIBLE : View.INVISIBLE);
        UpdateWorkingHours();
    }

    void UpdateWorkingHours()
    {
        if(!isWorking)
            return;

        String hoursString = (selectedDateTime.get(Calendar.HOUR_OF_DAY) - storedDateTime.get(Calendar.HOUR_OF_DAY)) +
                "h " + (selectedDateTime.get(Calendar.MINUTE) - storedDateTime.get(Calendar.MINUTE)) + "min";

        hoursLbl.setText(hoursString);
    }

    public String timeToStringFormat(Calendar c)
    {
        /*
            Return time in "0:00" format and add extra zero in front of minutes,
            when minutes are 1 digit.
        */
        return c.get(Calendar.HOUR_OF_DAY) + ((c.get(Calendar.MINUTE) < 10) ? ":0" : ":") + c.get(Calendar.MINUTE);
    }

    public String dateToStringFormat(Calendar c)
    {
        return c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR);
    }

    public void onClickTime(View view)
    {
        Calendar c = Calendar.getInstance();
        int iHours = c.get(Calendar.HOUR_OF_DAY), iMinutes = c.get(Calendar.MINUTE);

        // Launch TimePicker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker view, int hours, int minutes)
            {
                if(hours >= iHours && minutes > iMinutes || hours > iHours)
                {
                    Toast.makeText(getApplicationContext(),"Can't set time to future.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //TODO: Add prevention for minus hours when isWorking is set to true.

                selectedDateTime.set(Calendar.HOUR_OF_DAY, hours);
                selectedDateTime.set(Calendar.MINUTE, minutes);
                timeBtn.setText(timeToStringFormat(selectedDateTime));
                UpdateWorkingHours();
            }
        }, iHours, iMinutes, true);
        timePickerDialog.show();
    }

    public void onClickDate(View view)
    {
        Calendar c = Calendar.getInstance();
        int iYear = c.get(Calendar.YEAR), iMonth = c.get(Calendar.MONTH), iDay = c.get(Calendar.DAY_OF_MONTH);

        // Launch DatePicker Dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day)
            {
                if(year > iYear || year >= iYear && month > iMonth || year >= iYear && month >= iMonth && day > iDay)
                {
                    Toast.makeText(getApplicationContext(),"Can't set date to future.", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, day);
                dateBtn.setText(dateToStringFormat(selectedDateTime));
            }
        }, iYear, iMonth, iDay);
        datePickerDialog.show();
    }

    public void onClickWork(View view)
    {
        isWorking = !isWorking;

        // Copy "selected datetime data" to "start datetime data".
        storedDateTime = (Calendar)selectedDateTime.clone();

        Map<String, Object> stamp = new HashMap<>();
        if (isWorking)
        {
            stamp.put("started", timeToStringFormat(storedDateTime));
            stamp.put("ended", "Still working...");


            if(mAuth.getUid() != null)
            {
                db.collection(mAuth.getUid()).document(dateToStringFormat(storedDateTime))
                        .set(stamp).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error writing document", e);
                    }
                });
            }else
            {
                // This can happen as well if LoginActivity.debugSkipLogin is set to true.
                Log.e(TAG, "Database collection failed: Authentication was null.");
            }
        }
        else
        {
            stamp.put("ended", timeToStringFormat(storedDateTime));

            if(mAuth.getUid() != null)
            {
                db.collection(mAuth.getUid()).document(dateToStringFormat(storedDateTime)).set(stamp);
            }else
            {
                Log.e(TAG, "Database collection failed: Authentication was null.");
            }
        }
        UpdateView();
    }

    public void onClickHistory(View view)
    {
        startActivity(new Intent(MainActivity.this, HistoryActivity.class));
    }

    public void onClickLogout(View view)
    {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }
}