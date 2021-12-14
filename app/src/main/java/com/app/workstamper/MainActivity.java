package com.app.workstamper;

import static com.app.workstamper.LoginActivity.mAuth;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
{
    private Button
            timeBtn,
            dateBtn,
            stampBtn;

    private CheckBox
            foodBreakBox;
    private RadioGroup
            radioGroup;
    private RadioButton
            normalHoursBtn;
    private TextView
            hoursLbl,
            loggedUserLbl,
            loggedOrgLbl;
    private boolean
            isWorking = false;
    private Stamper.StampData
            mainStampData;
    private Calendar
            selectedDateTime;
    private String
            storedUsername,
            storedLastname,
            storedOrganization;

    FirebaseFirestore db;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedDateTime = Calendar.getInstance();
        mainStampData = new Stamper.StampData();
        db = FirebaseFirestore.getInstance();

        TextWatcher timeDateWatcher = new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(isWorking)
                    mainStampData.endDateTime = selectedDateTime;
                hoursLbl.setText(DatetimeHelper.getCountedHours(mainStampData));
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
        };

        timeBtn = findViewById(R.id.timeButton);
        dateBtn = findViewById(R.id.dateButton);
        stampBtn = findViewById(R.id.stampButton);
        timeBtn.addTextChangedListener(timeDateWatcher);
        dateBtn.addTextChangedListener(timeDateWatcher);
        hoursLbl = findViewById(R.id.hoursLabel);
        foodBreakBox = findViewById(R.id.fbreakCheckbox);
        normalHoursBtn = findViewById(R.id.normalRbtn);
        radioGroup = findViewById(R.id.radGroup);

        loggedUserLbl = findViewById(R.id.LoggedUser);
        loggedOrgLbl = findViewById(R.id.Organization);

        foodBreakBox.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            mainStampData.hadFoodBreak = isChecked;
            hoursLbl.setText(DatetimeHelper.getCountedHours(mainStampData));
        });

        // Fetch user related strings from database.
        UpdateStoredUserData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case (int)R.id.history:
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                break;

            case (int)R.id.logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Logout");
                builder.setMessage("Press OK to logout");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", (dialogInterface, i) ->
                {
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                });

                builder.setNegativeButton("Cancel", (dialogInterface, i) -> { /* Do nothing :) */ });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
        return true;
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // User was null, return back to login. (unless debugSkipLogin is set to true)
        if (mAuth.getCurrentUser() == null && !LoginActivity.debugSkipLogin) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    @SuppressLint("SetTextI18n")
    void UpdateStoredUserData()
    {
        if (mAuth.getUid() == null)
        {
            Log.e(TAG, "Unable to get stored userdata, authentication was null.");
            return;
        }

        // Get work status
        if(Stamper.Database.IsCurrentlyWorking())
        {
            Log.e(TAG, "Working state returned.");
            DocumentSnapshot document = Stamper.Database.GetLatestDocument();
            if (document != null && document.exists())
            {
                String hadFoodBreak = document.getString("HadFoodBreak");
                String startTime = document.getString("StartTime");
                String startDate = document.getString("StartDate");

                if(hadFoodBreak != null && startTime != null && startDate != null)
                {
                    mainStampData.startDateTime = mainStampData.parseDateTime(startDate, startTime);
                    mainStampData.hadFoodBreak = hadFoodBreak.contains("true");
                    mainStampData.id = document.getId();
                    this.isWorking = true;
                }
            }
        }

        // Get names and organization.
        DocumentReference docRef = db.collection("users").document(mAuth.getUid());
        docRef.get().addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                DocumentSnapshot document = task.getResult();

                if (document != null && document.exists())
                {
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
        });

        // Get organization related rules/settings.
        if(storedOrganization != null)
        {
            DocumentReference doc = db.collection("organization").document(storedOrganization);
            doc.get().addOnCompleteListener(task ->
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot document = task.getResult();

                    if (document != null && document.exists())
                    {
                        String storedPenalty = document.getString("FoodBreakPenaltyMinutes");
                        if(storedPenalty != null)
                        {
                            DatetimeHelper.foodBreakPenaltyMinutes = Integer.parseInt(storedPenalty);
                            Log.d(TAG, "FoodBreakPenaltyMinutes: " + storedPenalty);
                        }
                    }
                } else {
                    Log.e(TAG, "Get failed: ", task.getException());
                }
            });
        }

        // Update UI.
        UpdateView();
    }

    void UpdateView()
    {
        selectedDateTime = Calendar.getInstance();
        timeBtn.setText(DatetimeHelper.Time.toStringFormat(selectedDateTime));
        dateBtn.setText(DatetimeHelper.Date.toStringFormat(selectedDateTime));
        stampBtn.setText(this.isWorking ? "Stop work" : "Start work");
        radioGroup.setVisibility(this.isWorking ? View.INVISIBLE : View.VISIBLE);
        hoursLbl.setVisibility(this.isWorking ? View.VISIBLE : View.INVISIBLE);
        foodBreakBox.setVisibility(this.isWorking ? View.VISIBLE : View.INVISIBLE);
    }

    public void onClickTime(View view)
    {
        DatetimeHelper.Time.pickerDialog(timeBtn, selectedDateTime, true);
    }

    public void onClickDate(View view)
    {
        DatetimeHelper.Date.pickerDialog(dateBtn, selectedDateTime, true);
    }

    public void onClickWork(View view)
    {
        if(!isWorking)
        {
            mainStampData.startDateTime = selectedDateTime;
            mainStampData.type = (radioGroup.getCheckedRadioButtonId() == R.id.overtimeRbtn) ? "Overtime" : "Normal";
        }

        isWorking = !isWorking;

        Stamper.Database.WriteStamp(mainStampData, isWorking);

        // Update UI
        if(!isWorking)
        {
            normalHoursBtn.setChecked(true);
            foodBreakBox.setChecked(false);
        }

        UpdateView();
    }
}