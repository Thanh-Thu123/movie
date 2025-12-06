package com.example.projectfilm.ui.user.booking;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    // Trả về thứ trong tuần: Monday, Tuesday, ...
    public static String getDayOfWeek(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        return sdf.format(date);
    }

    // Trả về thứ viết thường để đối chiếu với Firestore: "friday", "saturday"
    public static String getDayOfWeekLower(Date date) {
        return getDayOfWeek(date).toLowerCase(Locale.ENGLISH);
    }

    // Lấy Date từ DatePicker (year, month, day)
    public static Date getDateFromPicker(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }
}
