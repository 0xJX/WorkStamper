package com.app.workstamper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button
            timeBtn,
            dateBtn,
            stampBtn;

    private CheckBox
            foodBreakBox;
    private TextView
            hoursLbl,
            loggedUserLbl,
            loggedOrgLbl;
    private boolean
            isWorking = false;
    private Calendar
            selectedDateTime,
            storedDateTime;
    private String
            storedUsername,
            storedLastname,
            storedOrganization;

    static FirebaseAuth mAuth;
    static FirebaseFirestore db;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        timeBtn = findViewById(R.id.timeButton);
        dateBtn = findViewById(R.id.dateButton);
        stampBtn = findViewById(R.id.stampButton);
        hoursLbl = findViewById(R.id.hoursLabel);
        foodBreakBox = findViewById(R.id.fbreakCheckbox);

        loggedUserLbl = findViewById(R.id.LoggedUser);
        loggedOrgLbl = findViewById(R.id.Organization);

        // Fetch user related strings from database.
        UpdateStoredUserData();

        // Update UI.
        UpdateView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.history:
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                break;

            case R.id.settings:
                Toast.makeText(this, "Settings clicked, no actions configured.", Toast.LENGTH_LONG).show();
                break;

            case R.id.logout:
                //Toast.makeText(this, "Logout clicked", Toast.LENGTH_LONG).show(); //For debugging purposes

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Logout");
                builder.setMessage("Press OK to logout");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                break;
        }

        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();

        // User was null, return back to login. (unless debugSkipLogin is set to true)
        if (mAuth.getCurrentUser() == null && !LoginActivity.debugSkipLogin) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    void UpdateStoredUserData() {
        if (mAuth.getUid() == null) {
            Log.e(TAG, "Unable to get stored userdata, authentication was null.");
            return;
        }

        // Get work status
        if(Stamper.Database.IsCurrentlyWorking())
        {
            DocumentSnapshot document = Stamper.Database.GetLatestDocument();
            if (document != null && document.exists())
            {
                String hadFoodBreak = document.getString("HadFoodBreak");
                String startTime = document.getString("StartTime");
                String startDate = document.getString("StartDate");

                if(hadFoodBreak != null && startTime != null && startDate != null)
                {
                    Stamper.StampData stampData = new Stamper.StampData(startDate, startTime, hadFoodBreak.contains("true"));
                    storedDateTime = stampData.startDateTime;
                    isWorking = true;
                    UpdateView();
                }
            }
        }


        // Get names and organization.
        DocumentReference docRef = db.collection("users").document(mAuth.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document != null && document.exists()) {
                        storedUsername = document.getString("firstname");
                        storedLastname = document.getString("lastname");
                        storedOrganization = document.getString("organization");

                        loggedUserLbl.setText(storedUsername + " " + storedLastname);
                        loggedOrgLbl.setText(storedOrganization);

                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.e(TAG, "No such document");
                    }
                } else {
                    Log.e(TAG, "Get failed: ", task.getException());
                }
            }
        });
    }

    void UpdateView() {
        // Get Current Time
        selectedDateTime = Calendar.getInstance();

        timeBtn.setText(DatetimeHelper.Time.toStringFormat(selectedDateTime));
        dateBtn.setText(DatetimeHelper.Date.toStringFormat(selectedDateTime));
        stampBtn.setText(this.isWorking ? "Stop work" : "Start work");
        hoursLbl.setVisibility(this.isWorking ? View.VISIBLE : View.INVISIBLE);
        foodBreakBox.setVisibility(this.isWorking ? View.VISIBLE : View.INVISIBLE);

        UpdateWorkingHours();
    }

    void UpdateWorkingHours() {
        if (!isWorking)
            return;

        String hoursString = (selectedDateTime.get(Calendar.HOUR_OF_DAY) - storedDateTime.get(Calendar.HOUR_OF_DAY)) +
                "h " + (selectedDateTime.get(Calendar.MINUTE) - storedDateTime.get(Calendar.MINUTE)) + "min";

        hoursLbl.setText(hoursString);
    }

    public void onClickTime(View view) {
        Calendar c = Calendar.getInstance();
        int iHours = c.get(Calendar.HOUR_OF_DAY), iMinutes = c.get(Calendar.MINUTE);

        // Launch TimePicker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hours, int minutes) {
                if (hours >= iHours && minutes > iMinutes || hours > iHours) {
                    Toast.makeText(getApplicationContext(), "Can't set time to future.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //TODO: Add prevention for minus hours when isWorking is set to true.

                selectedDateTime.set(Calendar.HOUR_OF_DAY, hours);
                selectedDateTime.set(Calendar.MINUTE, minutes);
                timeBtn.setText(DatetimeHelper.Time.toStringFormat(selectedDateTime));
                UpdateWorkingHours();
            }
        }, iHours, iMinutes, true);
        timePickerDialog.show();
    }

    public void onClickDate(View view) {
        Calendar c = Calendar.getInstance();
        int iYear = c.get(Calendar.YEAR), iMonth = c.get(Calendar.MONTH), iDay = c.get(Calendar.DAY_OF_MONTH);

        // Launch DatePicker Dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                if (year > iYear || year >= iYear && month > iMonth || year >= iYear && month >= iMonth && day > iDay) {
                    Toast.makeText(getApplicationContext(), "Can't set date to future.", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, day);
                dateBtn.setText(DatetimeHelper.Date.toStringFormat(selectedDateTime));
            }
        }, iYear, iMonth, iDay);
        datePickerDialog.show();
    }

    public void onClickWork(View view)
    {
        isWorking = !isWorking;

        // Copy "selected datetime data" to "start datetime data".
        storedDateTime = (Calendar) selectedDateTime.clone();

        // Update UI
        UpdateView();

        if (mAuth.getUid() == null) {
            // This can happen as well if LoginActivity.debugSkipLogin is set to true.
            Log.e(TAG, "Database collection failed: Authentication was null.");
            return;
        }

        // Writes data to database, sets "StartDateTime" if isWorking is true.
        Stamper.Database.WriteStamp(storedDateTime, foodBreakBox.isChecked(), isWorking);
    }
}