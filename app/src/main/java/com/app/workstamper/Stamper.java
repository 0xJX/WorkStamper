package com.app.workstamper;

import static com.app.workstamper.LoginActivity.mAuth;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
                startDateTime,
                endDateTime;
        public boolean
                hadFoodBreak = false;
        public String
                id = "ID_NOT_SET";

        public Calendar parseDateTime(String date, String time)
        {
            String[] dateSplit = date.split("\\."); // 0=day, 1=month, 2=year
            String[] timeSplit = time.split(":"); // 0=hour, 1=minutes

            // Copy parsed string data to calendar format for easier editing.
            Calendar c = (Calendar)Calendar.getInstance().clone();
            c.set(Calendar.YEAR, Integer.parseInt(dateSplit[2]));
            c.set(Calendar.MONTH, Integer.parseInt(dateSplit[1]) - 1); // Months are 0 - 11 in Calendar
            c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateSplit[0]));
            c.set(Calendar.HOUR_OF_DAY,Integer.parseInt(timeSplit[0]));
            c.set(Calendar.MINUTE,Integer.parseInt(timeSplit[1]));

            return c;
        }

        public StampData()
        {
            startDateTime = (Calendar)Calendar.getInstance().clone();
            endDateTime = (Calendar)Calendar.getInstance().clone();
        }

        public StampData(String startDate, String endDate, String startTime, String endTime, boolean hadFoodBreak)
        {
            startDateTime = parseDateTime(startDate, startTime);
            endDateTime = parseDateTime(endDate, endTime);
            this.hadFoodBreak = hadFoodBreak;
        }
    }

    public static class Database
    {
        private static final String TAG = "Stamper";
        public static final ArrayList<DocumentSnapshot> docs = new ArrayList<>();

        public static void UpdateDocumentArray()
        {
            if (mAuth.getUid() == null)
            {
                Log.e(TAG, "Stamping failed: Authentication was null.");
                return;
            }

            FirebaseFirestore.getInstance().collection(mAuth.getUid()).get().addOnSuccessListener(documentSnapshots ->
            {
                docs.clear();
                if (documentSnapshots.isEmpty())
                {
                    Log.e(TAG, "Unable to get documents: Empty list.");
                } else {
                    List<DocumentSnapshot> types = documentSnapshots.getDocuments();
                    docs.addAll(types);
                    Log.d(TAG, "Added docs to arraylist.");
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Unable to get documents: " + e));
        }

        public static void DeleteStamp(StampData stampData)
        {
            if (mAuth.getUid() == null)
            {
                Log.e(TAG, "Stamp delete failed: Authentication was null.");
                return;
            }

            DocumentReference docRef = FirebaseFirestore.getInstance().collection(mAuth.getUid()).document(stampData.id);
            docRef.get().addOnCompleteListener(task ->
            {
                if (task.isSuccessful())
                {
                    docRef.delete();
                }
            });
        }

        public static void UpdateStamp(StampData stampData)
        {
            if (mAuth.getUid() == null)
            {
                Log.e(TAG, "Stamp update failed: Authentication was null.");
                return;
            }

            DocumentReference docRef = FirebaseFirestore.getInstance().collection(mAuth.getUid()).document(stampData.id);
            docRef.get().addOnCompleteListener(task ->
            {
                if (task.isSuccessful())
                {
                    Map<String, String> stamp = new HashMap<>();
                    stamp.put("StartDate", DatetimeHelper.Date.toStringFormat(stampData.startDateTime));
                    stamp.put("StartTime", DatetimeHelper.Time.toStringFormat(stampData.startDateTime));
                    stamp.put("EndDate", DatetimeHelper.Date.toStringFormat(stampData.endDateTime));
                    stamp.put("EndTime", DatetimeHelper.Time.toStringFormat(stampData.endDateTime));
                    stamp.put("HadFoodBreak", stampData.hadFoodBreak ? "true" : "false");

                    docRef.set(stamp, SetOptions.merge()).addOnFailureListener(e ->
                            Log.e(TAG, "Error updating stamp.", e));
                }
            });
        }

        public static DocumentSnapshot GetLatestDocument()
        {
            UpdateDocumentArray();

            Log.e(TAG, "Docs size: " + docs.size());

            if(docs.isEmpty())
                return null;

            DocumentSnapshot latestDocument = null;
            long latestTimestamp = 0;
            for (int i = 0; i <= (docs.size() - 1); i++)
            {
                DocumentSnapshot document = docs.get(i);

                if (document == null || !document.exists())
                    continue;

                String data = document.getString("Timestamp");

                if(data == null)
                    continue;

                long timestamp = Long.parseLong(data);
                if(timestamp > latestTimestamp)
                {
                    latestTimestamp = timestamp;
                    latestDocument = document;
                }
            }

            if (latestDocument == null || !latestDocument.exists())
                return null;

            return latestDocument;
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

            boolean isWorking = document.contains("StartTime") && !document.contains("EndTime");
            Log.d(TAG, "isWorking = " + isWorking);

            return isWorking;
        }

        public static String RandomString(int len)
        {
            final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            SecureRandom rnd = new SecureRandom();
            return IntStream.range(0, len).mapToObj(i -> String.valueOf(AB.charAt(rnd.nextInt(AB.length())))).collect(Collectors.joining());
        }

        public static void WriteStamp(Stamper.StampData stampData, boolean isStartDateTime)
        {
            if (mAuth.getUid() == null)
            {
                Log.e(TAG, "Stamping failed: Authentication was null.");
                return;
            }

            // Get latest document.
            DocumentSnapshot document = GetLatestDocument();

            final Calendar c = isStartDateTime ? stampData.startDateTime : stampData.endDateTime;
            final String timeSpecifierString = isStartDateTime ? "StartTime" : "EndTime";

            Map<String, String> stamp = new HashMap<>();
            stamp.put(timeSpecifierString, DatetimeHelper.Time.toStringFormat(c));
            stamp.put(isStartDateTime ? "StartDate" : "EndDate", DatetimeHelper.Date.toStringFormat(c));
            stamp.put("HadFoodBreak", stampData.hadFoodBreak ? "true" : "false");
            stamp.put("Timestamp", Long.toString(System.currentTimeMillis() / 1000L));

            if(document == null || document.exists() && document.contains(timeSpecifierString))
            {
                FirebaseFirestore.getInstance().collection(mAuth.getUid()).document("Stamp_" + RandomString(10))
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
