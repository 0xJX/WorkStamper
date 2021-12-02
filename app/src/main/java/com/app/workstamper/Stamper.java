package com.app.workstamper;

import static com.app.workstamper.MainActivity.db;
import static com.app.workstamper.MainActivity.mAuth;

import android.util.Log;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Stamper
{
    public static class StampData
    {
        public Calendar
                startDateTime = Calendar.getInstance(),
                endDateTime = Calendar.getInstance();
        public boolean
                hadFoodBreak = false;

        public Calendar ParseDateTime(String date, String time)
        {
            String[] dateSplit = date.split("\\."); // 0=day, 1=month, 2=year
            String[] timeSplit = time.split(":"); // 0=hour, 1=minutes

            // Copy parsed string data to calendar format for easier editing.
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, Integer.parseInt(dateSplit[2]));
            c.set(Calendar.MONTH, Integer.parseInt(dateSplit[1]));
            c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateSplit[0]));
            c.set(Calendar.HOUR_OF_DAY,Integer.parseInt(timeSplit[0]));
            c.set(Calendar.MINUTE,Integer.parseInt(timeSplit[1]));

            return c;
        }

        public StampData(String startDate, String startTime, boolean hadFoodBreak)
        {
            startDateTime = ParseDateTime(startDate, startTime);
            this.hadFoodBreak = hadFoodBreak;
        }

        public StampData(String startDate, String endDate, String startTime, String endTime, boolean hadFoodBreak)
        {
            startDateTime = ParseDateTime(startDate, startTime);
            endDateTime = ParseDateTime(endDate, endTime);
            this.hadFoodBreak = hadFoodBreak;
        }
    }

    public static class Database
    {
        private static final String TAG = "Stamper";
        private static ArrayList<DocumentSnapshot> docs = new ArrayList<>();

        public static void GetDocuments()
        {
            if (mAuth.getUid() == null)
            {
                Log.e(TAG, "Stamping failed: Authentication was null.");
                return;
            }

            db.collection(mAuth.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            if (documentSnapshots.isEmpty())
                            {
                                docs = new ArrayList<>();
                                Log.e(TAG, "Unable to get documents: Empty list.");
                            } else {
                                List<DocumentSnapshot> types = documentSnapshots.getDocuments();
                                docs = new ArrayList<>();
                                docs.addAll(types);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Unable to get documents: " + e);
                }
            });
        }

        public static DocumentSnapshot GetLatestDocument()
        {
            // Update documents.
            GetDocuments();

            if(docs.size() == 0)
                return null;

            // Get latest document.
            DocumentSnapshot document = docs.get(docs.size() - 1);

            if (!document.exists())
                return null;

            return document;
        }

        public static boolean IsCurrentlyWorking()
        {
            if (mAuth.getUid() == null)
            {
                Log.e(TAG, "Stamping failed: Authentication was null.");
                return false;
            }

            // Get latest document.
            DocumentSnapshot document = GetLatestDocument();

            // No latest documents, user is not working.
            if(document == null)
                return false;

           return (document.contains("StartTime") && !document.contains("EndTime"));
        }

        public static String RandomString(int len)
        {
            final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            SecureRandom rnd = new SecureRandom();
            return IntStream.range(0, len).mapToObj(i -> String.valueOf(AB.charAt(rnd.nextInt(AB.length())))).collect(Collectors.joining());
        }

        public static void WriteStamp(Calendar storedDateTime, boolean hadFoodBreak, boolean isStartDateTime)
        {
            if (mAuth.getUid() == null)
            {
                Log.e(TAG, "Stamping failed: Authentication was null.");
                return;
            }

            // Get latest document.
            DocumentSnapshot document = GetLatestDocument();

            final String timeSpecifierString = isStartDateTime ? "StartTime" : "EndTime";

            Map<String, String> stamp = new HashMap<>();
            stamp.put(timeSpecifierString, DatetimeHelper.Time.toStringFormat(storedDateTime));
            stamp.put(isStartDateTime ? "StartDate" : "EndDate", DatetimeHelper.Date.toStringFormat(storedDateTime));
            stamp.put("HadFoodBreak", hadFoodBreak ? "true" : "false");

            if(document == null || document.exists() && document.contains(timeSpecifierString))
            {
                db.collection(mAuth.getUid()).document("Stamp_" + RandomString(10))
                        .set(stamp).addOnFailureListener(e -> Log.e(TAG, "Error writing " + timeSpecifierString + ": ", e));
            }
            else if(document.exists() && !document.contains(timeSpecifierString)) // Document exists, but does not have data.
            {
                document.getReference().set(stamp, SetOptions.merge())
                        .addOnFailureListener(e -> Log.e(TAG, "Error writing " + timeSpecifierString + ": ", e));
            }
        }
    }
}
