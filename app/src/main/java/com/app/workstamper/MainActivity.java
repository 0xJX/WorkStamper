package com.app.workstamper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;

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
    FirebaseFirestore db;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

            case (int)R.id.settings:
                Toast.makeText(this, "Settings clicked, no actions configured.", Toast.LENGTH_LONG).show();
                break;

            case (int)R.id.logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Logout");
                builder.setMessage("Press OK to logout");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        mAuth.signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                });

                builder.setNegativeButton("Cancel", (dialogInterface, i) -> { /* Do nothing :) */ });

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
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
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

    void UpdateWorkingHours()
    {
        if (!isWorking)
            return;

        String hoursString = (selectedDateTime.get(Calendar.HOUR_OF_DAY) - storedDateTime.get(Calendar.HOUR_OF_DAY)) +
                "h " + (selectedDateTime.get(Calendar.MINUTE) - storedDateTime.get(Calendar.MINUTE)) + "min";

        hoursLbl.setText(hoursString);
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
        isWorking = !isWorking;

        // Copy "selected datetime data" to "start datetime data".
        storedDateTime = (Calendar) selectedDateTime.clone();

        // Update UI
        UpdateView();

        // Writes data to database, sets "StartDateTime" if isWorking is true.
        Stamper.Database.WriteStamp(storedDateTime, foodBreakBox.isChecked(), isWorking);
    }
}