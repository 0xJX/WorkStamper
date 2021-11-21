package com.app.workstamper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

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
            startDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
           Bundle extras = getIntent().getExtras();
           String userID = extras.getString("UserID");
           String companyID = extras.getString("CompanyID");
        */

        timeBtn = findViewById(R.id.timeButton);
        dateBtn = findViewById(R.id.dateButton);
        stampBtn = findViewById(R.id.stampButton);
        hoursLbl = findViewById(R.id.hoursLabel);
        foodBreakBox = findViewById(R.id.fbreakCheckbox);
        UpdateView();
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

        String hoursString = (selectedDateTime.get(Calendar.HOUR_OF_DAY) - startDateTime.get(Calendar.HOUR_OF_DAY)) +
                "h " + (selectedDateTime.get(Calendar.MINUTE) - startDateTime.get(Calendar.MINUTE)) + "min";

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
        startDateTime = (Calendar)selectedDateTime.clone();

        UpdateView();
    }
}