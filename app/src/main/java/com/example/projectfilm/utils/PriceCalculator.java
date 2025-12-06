package com.example.projectfilm.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PriceCalculator {

    private static final String TAG = "PriceCalculator";

    public static int calculateDynamicPrice(String dateStr, String time, int seats,
                                            int basePrice, Map<String, Long> weekendPricing,
                                            List<String> peakHours, int peakHourIncrease,
                                            int hotMovieThreshold, int hotMovieIncrease,
                                            int viewCount) {
        if (dateStr == null || time == null || weekendPricing == null || peakHours == null) {
            Log.w(TAG, "Missing data, fallback to basePrice only");
            return basePrice * seats;
        }

        int totalPercent = 0;

        String dayOfWeek = getDayOfWeekFromDate(dateStr);
        boolean isWeekend = isWeekend(dayOfWeek);
        boolean isPeak = isPeakHour(time, peakHours);

// 🔹 Tăng giá theo ngày cuối tuần
        int weekendIncrease = 0;
        if (isWeekend) {
            weekendIncrease = getWeekendIncrease(dayOfWeek, weekendPricing);
            totalPercent += weekendIncrease;
        }

// 🔹 Tăng giá theo giờ cao điểm (chỉ nếu KHÔNG phải cuối tuần)
        int peakIncrease = 0;
        if (!isWeekend && isPeak) {
            peakIncrease = peakHourIncrease;
            totalPercent += peakIncrease;
        }

// 🔹 Tăng giá nếu phim hot
        int hotIncrease = 0;
        if (viewCount >= hotMovieThreshold) {
            hotIncrease = hotMovieIncrease;
            totalPercent += hotIncrease;
        }


        int pricePerSeat = basePrice + (basePrice * totalPercent) / 100;
        int totalPrice = pricePerSeat * seats;

        // 🔸 DEBUG log
        Log.d(TAG, "----- DYNAMIC PRICE DEBUG -----");
        Log.d(TAG, "Base price: " + basePrice);
        Log.d(TAG, "Seats: " + seats);
        Log.d(TAG, "Day: " + dayOfWeek);
        Log.d(TAG, "Weekend Increase: " + weekendIncrease + "%");
        Log.d(TAG, "Is Peak Hour: " + isPeakHour(time, peakHours) + " → +" + peakIncrease + "%");
        Log.d(TAG, "Is Hot Movie (views=" + viewCount + "): " + (viewCount >= hotMovieThreshold) + " → +" + hotIncrease + "%");
        Log.d(TAG, "Total percent increase: " + totalPercent + "%");
        Log.d(TAG, "Final pricePerSeat: " + pricePerSeat);
        Log.d(TAG, "Total price: " + totalPrice);
        Log.d(TAG, "------------------------------");

        return totalPrice;
    }

    public static String getDayOfWeekFromDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            Date date = sdf.parse(dateStr);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
            return dayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isWeekend(String dayOfWeek) {
        if (dayOfWeek == null) return false;
        return dayOfWeek.equalsIgnoreCase("Friday") ||
                dayOfWeek.equalsIgnoreCase("Saturday") ||
                dayOfWeek.equalsIgnoreCase("Sunday");
    }

    public static int getWeekendIncrease(String dayOfWeek, Map<String, Long> weekendPricing) {
        if (weekendPricing == null || dayOfWeek == null) return 0;
        String formattedDay = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1).toLowerCase();
        if (weekendPricing.containsKey(formattedDay)) {
            return weekendPricing.get(formattedDay).intValue();
        }
        return 0;
    }

    public static boolean isPeakHour(String time, List<String> peakHours) {
        if (time == null || peakHours == null) return false;
        return peakHours.contains(time);
    }
}
