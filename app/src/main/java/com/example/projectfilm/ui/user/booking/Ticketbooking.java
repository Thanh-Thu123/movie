package com.example.projectfilm.ui.user.booking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectfilm.R;
import com.example.projectfilm.data.model.Movie;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class Ticketbooking extends AppCompatActivity {

    private String selectedCinema = null;
    private String selectedDate = null;
    private String selectedTime = null;

    private Button btnChooseSeat;

    private Button selectedDayButton = null;
    private Button selectedTimeButton = null;

    private FirebaseFirestore db;
    private int basePrice = 0; // giả định base giá vé

    private Movie selectedMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtime);

        db = FirebaseFirestore.getInstance();
        btnChooseSeat = findViewById(R.id.btnChooseSeat);
        btnChooseSeat.setVisibility(View.GONE);

        // Nhận dữ liệu movie từ MovieDetailFragment
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("movie")) {
            selectedMovie = (Movie) intent.getSerializableExtra("movie");
            basePrice = selectedMovie.getPrice(); // ✅ Lấy giá đúng từ Firestore
            Log.d("Ticketbooking", "Giá gốc của phim: " + basePrice); // để debug nếu cần
        } else {
            Toast.makeText(this, "Không có thông tin phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        setupDynamicDayButtons();
        setupTimeButtons();
        setupCinemaSelection();

        ImageView calendarIcon = findViewById(R.id.calendar);
        calendarIcon.setOnClickListener(v -> showDatePicker());

        btnChooseSeat.setOnClickListener(v -> {
            if (selectedCinema != null && selectedDate != null && selectedTime != null) {
                Intent seatIntent = new Intent(Ticketbooking.this, SeatListActivity.class);
                seatIntent.putExtra("cinema", selectedCinema);
                seatIntent.putExtra("date", selectedDate);
                seatIntent.putExtra("time", selectedTime);
                seatIntent.putExtra("movie", selectedMovie); // Truyền movie qua tiếp
                startActivity(seatIntent);
            } else {
                Toast.makeText(this, "Vui lòng chọn đầy đủ ngày, giờ và rạp", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Ticketbooking.this,
                (view, year1, month1, dayOfMonth) -> {
                    String selected = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    selectedDate = selected;
                    if (selectedDayButton != null) {
                        selectedDayButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
                        selectedDayButton.setTextColor(getResources().getColor(android.R.color.black));
                        selectedDayButton = null;
                    }
                    checkShowChooseSeat();
                },
                year, month, day
        );

        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 3);

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void setupDynamicDayButtons() {
        int[] dayButtonIds = {
                R.id.btnDay1, R.id.btnDay2, R.id.btnDay3, R.id.btnDay4,
                R.id.btnDay5, R.id.btnDay6, R.id.btnDay7
        };

        SimpleDateFormat displayFormat = new SimpleDateFormat("dd\nEEE", Locale.ENGLISH);
        SimpleDateFormat valueFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < dayButtonIds.length; i++) {
            Button button = findViewById(dayButtonIds[i]);
            if (button != null) {
                Date date = calendar.getTime();
                String displayText = displayFormat.format(date);
                String valueText = valueFormat.format(date);

                button.setText(displayText);
                String finalValueText = valueText;

                button.setOnClickListener(v -> {
                    if (selectedDayButton != null) {
                        selectedDayButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
                        selectedDayButton.setTextColor(getResources().getColor(android.R.color.black));
                    }
                    button.setBackgroundTintList(getResources().getColorStateList(R.color.purple_200));
                    button.setTextColor(getResources().getColor(android.R.color.white));
                    selectedDayButton = button;

                    selectedDate = finalValueText;
                    checkShowChooseSeat();
                });

                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
    }

    private void setupTimeButtons() {
        int[] timeButtonIds = {
                R.id.btnTime1700, R.id.btnTime1730, R.id.btnTime1800, R.id.btnTime1830,
                R.id.btnTime1900, R.id.btnTime1930, R.id.btnTime1945, R.id.btnTime2000,
                R.id.btnTime2030, R.id.btnTime2045, R.id.btnTime2100, R.id.btnTime2130,
                R.id.btnTime2145, R.id.btnTime2200, R.id.btnTime2230, R.id.btnTime2300
        };

        String[] timeValues = {
                "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "19:45", "20:00",
                "20:30", "20:45", "21:00", "21:30", "21:45", "22:00", "22:30", "23:00"
        };

        for (int i = 0; i < timeButtonIds.length; i++) {
            Button btn = findViewById(timeButtonIds[i]);
            String timeValue = timeValues[i];
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    if (selectedTimeButton != null) {
                        selectedTimeButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
                        selectedTimeButton.setTextColor(getResources().getColor(android.R.color.black));
                    }
                    btn.setBackgroundTintList(getResources().getColorStateList(R.color.teal_700));
                    btn.setTextColor(getResources().getColor(android.R.color.white));
                    selectedTimeButton = btn;

                    selectedTime = timeValue;
                    checkShowChooseSeat();
                });
            }
        }
    }

    private void setupCinemaSelection() {
        String[] cinemaNames = {
                "Cinestar Quốc Thanh",
                "Mega GS Cao Thắng",
                "Galaxy Tân Bình",
                "Lotte Thủ Đức",
                "Lotte Moonlight Thủ Đức",
                "Cinestar Hai Bà Trưng"
        };

        int[] buttonIds = {
                R.id.theater_Cinestar_Quoc_Thanh,
                R.id.theater_Mega_GS_Cao_Thang,
                R.id.theater_Galaxy_Tan_Binh,
                R.id.theater_Lotte_Thu_Duc,
                R.id.theater_Lotte_Moonlight_Thu_Duc,
                R.id.theater_Cinestar_Hai_Ba_Trung
        };

        List<Button> cinemaButtons = new ArrayList<>();

        for (int i = 0; i < cinemaNames.length; i++) {
            String cinema = cinemaNames[i];
            Button btn = findViewById(buttonIds[i]);

            if (btn != null) {
                cinemaButtons.add(btn);
                btn.setOnClickListener(v -> {
                    selectedCinema = cinema;

                    for (Button b : cinemaButtons) {
                        b.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
                        b.setTextColor(getResources().getColor(android.R.color.black));
                    }

                    btn.setBackgroundTintList(getResources().getColorStateList(R.color.light_blue));
                    btn.setTextColor(getResources().getColor(android.R.color.white));

                    checkShowChooseSeat();
                });
            }
        }
    }

    private void checkShowChooseSeat() {
        if (selectedCinema != null && selectedDate != null && selectedTime != null) {
            btnChooseSeat.setVisibility(View.VISIBLE);
        } else {
            btnChooseSeat.setVisibility(View.GONE);
        }
    }
}
