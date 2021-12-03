package com.app.workstamper;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.DatePicker;
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

        public static void pickerDialog(Button button, Calendar c, boolean getLatestTime)
        {
            Calendar temp = getLatestTime ? Calendar.getInstance() : (Calendar)c.clone();

            int latestHours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                latestMinutes = Calendar.getInstance().get(Calendar.MINUTE),
                latestYear = Calendar.getInstance().get(Calendar.YEAR),
                latestMonth = Calendar.getInstance().get(Calendar.MONTH),
                latestDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

            // Launch TimePicker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(button.getContext(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hours, int minutes)
                {
                    // Check if time is set too high.
                    if (hours >= latestHours && minutes > latestMinutes || hours > latestHours)
                    {
                        // Check the date as well.
                        if(c.get(Calendar.YEAR) >= latestYear && (c.get(Calendar.YEAR) >= latestYear && c.get(Calendar.MONTH) >= latestMonth)
                                && (c.get(Calendar.YEAR) >= latestYear && c.get(Calendar.MONTH) >= latestMonth && c.get(Calendar.DAY_OF_MONTH) >= latestDay))
                        {
                            Toast.makeText(button.getContext(), "Can't set time to future.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    c.set(Calendar.HOUR_OF_DAY, hours);
                    c.set(Calendar.MINUTE, minutes);
                    button.setText(toStringFormat(c));
                }
            }, temp.get(Calendar.HOUR_OF_DAY), temp.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }
    }

    public static class Date
    {
        public static String toStringFormat(Calendar c)
        {
            return c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR);
        }

        public static void pickerDialog(Button button, Calendar c, boolean getLatestDate)
        {
            Calendar temp = getLatestDate ? Calendar.getInstance() : (Calendar)c.clone();

            int latestYear = Calendar.getInstance().get(Calendar.YEAR),
                latestMonth = Calendar.getInstance().get(Calendar.MONTH),
                latestDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                latestHours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                latestMinutes = Calendar.getInstance().get(Calendar.MINUTE);

            // Launch DatePicker Dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(button.getContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day)
                {
                    // Check that current date isn't set to future and prevent setting it.
                    if (year > latestYear || year >= latestYear && month > latestMonth || year >= latestYear && month >= latestMonth && day > latestDay)
                    {
                        Toast.makeText(button.getContext(), "Can't set date to future.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Also check if date is same but the set hours are in the future.
                    if(c.get(Calendar.YEAR) >= latestYear && (c.get(Calendar.YEAR) >= latestYear && c.get(Calendar.MONTH) >= latestMonth)
                            && (c.get(Calendar.YEAR) >= latestYear && c.get(Calendar.MONTH) >= latestMonth && c.get(Calendar.DAY_OF_MONTH) >= latestDay)
                            && (c.get(Calendar.HOUR_OF_DAY) >= latestHours && c.get(Calendar.MINUTE) > latestMinutes) || (c.get(Calendar.HOUR_OF_DAY) > latestHours))
                    {
                        Toast.makeText(button.getContext(), "Can't set date: Time is set too high with this date.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.MONTH, month);
                    c.set(Calendar.DAY_OF_MONTH, day);
                    button.setText(toStringFormat(c));
                }
            }, temp.get(Calendar.YEAR), temp.get(Calendar.MONTH), temp.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
    }
}
