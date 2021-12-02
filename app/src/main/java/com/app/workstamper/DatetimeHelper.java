package com.app.workstamper;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class DatetimeHelper
{
    public static class Time
    {
        public static String toStringFormat(Calendar c)
        {
            /*
                Return time in "0:00" format and add extra zero in front of minutes,
                when minutes are 1 digit.
            */
            return c.get(Calendar.HOUR_OF_DAY) + ((c.get(Calendar.MINUTE) < 10) ? ":0" : ":") + c.get(Calendar.MINUTE);
        }

        public static void timePickerDialog(Context context, Button button, Calendar c, boolean getLatestTime)
        {
            Calendar latestCalendar = Calendar.getInstance();
            Calendar temp = getLatestTime ? (Calendar)latestCalendar.clone(): (Calendar)c.clone();
            int iHours = temp.get(Calendar.HOUR_OF_DAY), iMinutes = temp.get(Calendar.MINUTE);
            int lHours = latestCalendar.get(Calendar.HOUR_OF_DAY), lMinutes = latestCalendar.get(Calendar.MINUTE);

            // Launch TimePicker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hours, int minutes) {
                    if (hours >= lHours && minutes > lMinutes || hours > lHours) {
                        Toast.makeText(context, "Can't set time to future.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    c.set(Calendar.HOUR_OF_DAY, hours);
                    c.set(Calendar.MINUTE, minutes);
                    button.setText(toStringFormat(c));
                }
            }, iHours, iMinutes, true);
            timePickerDialog.show();
        }
    }

    public static class Date
    {
        public static String toStringFormat(Calendar c)
        {
            return c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR);
        }
    }
}
