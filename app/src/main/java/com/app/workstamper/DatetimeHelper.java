package com.app.workstamper;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.Button;
import android.widget.Toast;
import java.util.Calendar;

public class DatetimeHelper
{
    public static int foodBreakPenaltyMinutes = 30; // Organization controls this but default is 30.

    public static String getCountedHours(Stamper.StampData stampData)
    {
        int
            years = stampData.endDateTime.get(Calendar.YEAR) - stampData.startDateTime.get(Calendar.YEAR),
            months = stampData.endDateTime.get(Calendar.MONTH) - stampData.startDateTime.get(Calendar.MONTH),
            days = stampData.endDateTime.get(Calendar.DAY_OF_MONTH) - stampData.startDateTime.get(Calendar.DAY_OF_MONTH),
            hours = stampData.endDateTime.get(Calendar.HOUR_OF_DAY) - stampData.startDateTime.get(Calendar.HOUR_OF_DAY),
            minutes = stampData.endDateTime.get(Calendar.MINUTE) - stampData.startDateTime.get(Calendar.MINUTE);

        if(stampData.hadFoodBreak)
            minutes -= foodBreakPenaltyMinutes;

        while(true) // Loop until we have converted the datetime.
        {
            if (months < 0 && years > 0)
            {
                years--;
                months = 12 + months;
            }else if (days < 0 && (months > 0 || years > 0))
            {
                months--;
                days = stampData.startDateTime.getActualMaximum(Calendar.DATE) + days;
            }else if (hours < 0 && (days > 0 || months > 0 || years > 0))
            {
                days--;
                hours = stampData.startDateTime.getActualMaximum(Calendar.HOUR_OF_DAY) + hours;
            }else if (minutes < 0 && (hours > 0 || days > 0 || months > 0 || years > 0))
            {
                hours--;
                minutes = 60 + minutes;
            }else
            {
                break;
            }
        }

        String countedHours = "";

        if(years > 0)
            countedHours += years + "y ";

        if(months > 0)
            countedHours += months + "mo ";

        if(days > 0)
            countedHours += days + "d ";

        if(hours > 0)
            countedHours += hours + "h ";

        if(minutes > 0)
            countedHours += minutes + "min ";

        if(countedHours.equals(""))
            countedHours = "No hours.";

        return countedHours;
    }

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
            TimePickerDialog timePickerDialog = new TimePickerDialog(button.getContext(), (view, hours, minutes) ->
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
            }, temp.get(Calendar.HOUR_OF_DAY), temp.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }
    }

    public static class Date
    {
        public static String toStringFormat(Calendar c)
        {
            return c.get(Calendar.DAY_OF_MONTH) + "." + (c.get(Calendar.MONTH) + 1) + "." + c.get(Calendar.YEAR);
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
            DatePickerDialog datePickerDialog = new DatePickerDialog(button.getContext(), (view, year, month, day) ->
            {
                // Check that current date isn't set to future and prevent setting it.
                if (year > latestYear || year >= latestYear && month > latestMonth || year >= latestYear && month >= latestMonth && day > latestDay)
                {
                    Toast.makeText(button.getContext(), "Can't set date to future.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Also check if date is same but the set hours are in the future.
                if((year >= latestYear && month >= latestMonth && day >= latestDay)
                        && (c.get(Calendar.HOUR_OF_DAY) >= latestHours && c.get(Calendar.MINUTE) > latestMinutes || (c.get(Calendar.HOUR_OF_DAY) > latestHours)))
                {
                    Toast.makeText(button.getContext(), "Can't set date: Time is set too high with this date.", Toast.LENGTH_LONG).show();
                    return;
                }
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, day);
                button.setText(toStringFormat(c));
            }, temp.get(Calendar.YEAR), temp.get(Calendar.MONTH), temp.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
    }
}
