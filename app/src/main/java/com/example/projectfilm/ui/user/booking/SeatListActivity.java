package com.example.projectfilm.ui.user.booking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectfilm.R;
import com.example.projectfilm.data.model.Movie;
import com.example.projectfilm.utils.PriceCalculator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatListActivity extends AppCompatActivity {

    private List<String> bookedSeats = new ArrayList<>();
    private List<String> selectedSeats = new ArrayList<>();

    private GridLayout gridSeats;
    private TextView textSelectedSeats, textTotalPrice;
    private Button btnConfirm;

    private String cinemaName = "", timeSlot = "", selectedDate = "";

    private Map<String, Long> weekendPricing;
    private List<String> peakHours;
    private int peakHourIncrease = 0, hotMovieThreshold = 0, hotMovieIncrease = 0;

    private boolean isPriceDataReady = false;
    private boolean isMovieDataReady = false;

    private int latestTotalPrice = 0;

    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_list);

        // Ánh xạ View
        gridSeats = findViewById(R.id.gridSeats);
        textSelectedSeats = findViewById(R.id.textSelectedSeats);
        textTotalPrice = findViewById(R.id.textTotalPrice);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setEnabled(false);

        // Nhận dữ liệu intent
        Intent intent = getIntent();
        selectedDate = intent.getStringExtra("date").trim();
        cinemaName = intent.getStringExtra("cinema").trim();
        timeSlot = intent.getStringExtra("time").trim();
        movie = (Movie) intent.getSerializableExtra("movie");

        Log.d("SeatListActivity", "Nhận: date=" + selectedDate + ", cinema=" + cinemaName + ", time=" + timeSlot);

        // Tải dữ liệu quy tắc giá
        fetchPriceRules();

        // Load ghế đã đặt từ seats_status
        loadBookedSeats();

        // Cập nhật lại thông tin phim từ Firestore
        if (movie != null && movie.getTitle() != null) {
            FirebaseFirestore.getInstance().collection("movies")
                    .whereEqualTo("title", movie.getTitle())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            DocumentSnapshot doc = snapshot.getDocuments().get(0);
                            Object priceObj = doc.get("price");
                            if (priceObj instanceof Number) {
                                movie.setPrice(((Number) priceObj).intValue());
                            } else {
                                Log.w("MovieFix", "price không phải dạng số: " + priceObj);
                            }

                            Object viewsObj = doc.get("viewCount");
                            if (viewsObj instanceof Number) {
                                movie.setViewCount(((Number) viewsObj).intValue());
                            } else {
                                Log.w("MovieFix", "viewCount không phải dạng số: " + viewsObj);
                            }

                            isMovieDataReady = true;
                            Log.d("MovieFix", "Cập nhật từ Firestore: price=" + movie.getPrice() + ", views=" + movie.getViewCount());

                            if (isPriceDataReady) {
                                updateSeatInfo();
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("MovieFire", "Không thể lấy thông tin phim: " + e.getMessage()));
        }

        btnConfirm.setOnClickListener(v -> {
            if (selectedSeats.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ghế", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isPriceDataReady) {
                Toast.makeText(this, "Đang tải dữ liệu giá vé...", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent payIntent = new Intent(this, ThanhToanActivity.class);
            payIntent.putExtra("cinema", cinemaName);
            payIntent.putExtra("time", timeSlot);
            payIntent.putExtra("date", selectedDate);
            payIntent.putExtra("seats", TextUtils.join(", ", selectedSeats));
            payIntent.putExtra("price", String.valueOf(latestTotalPrice));
            payIntent.putExtra("movie", movie);
            payIntent.putExtra("movieName", movie.getTitle());
            startActivity(payIntent);
        });
    }

    private void fetchPriceRules() {
        FirebaseFirestore.getInstance().collection("price_rules").document("default")
                .get()
                .addOnSuccessListener(this::parsePriceRules)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không thể tải dữ liệu giá vé", Toast.LENGTH_SHORT).show());
    }

    private void parsePriceRules(DocumentSnapshot snapshot) {
        try {
            weekendPricing = (Map<String, Long>) snapshot.get("weekendPricing");
            peakHours = (List<String>) snapshot.get("peakHours");

            Long peakInc = snapshot.getLong("peakHourIncrease");
            Long hotThreshold = snapshot.getLong("hotMovieThreshold");
            Long hotInc = snapshot.getLong("hotMovieIncrease");

            if (peakInc != null) peakHourIncrease = peakInc.intValue();
            if (hotThreshold != null) hotMovieThreshold = hotThreshold.intValue();
            if (hotInc != null) hotMovieIncrease = hotInc.intValue();

            isPriceDataReady = true;

            if (isMovieDataReady) {
                updateSeatInfo();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xử lý dữ liệu giá vé", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBookedSeats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String statusDocId = ThanhToanThanhCongActivity.getSeatStatusDocId(cinemaName, timeSlot, selectedDate);

        Log.d("SeatListActivity", "Đang tải ghế từ: " + statusDocId);

        db.collection("seats_status")
                .document(statusDocId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("SeatListActivity", "Lỗi lắng nghe seats_status: " + e.getMessage());
                        Toast.makeText(this, "Lỗi tải trạng thái ghế, tạo mặc định", Toast.LENGTH_SHORT).show();
                        bookedSeats.clear();
                        generateSeatButtons(); // Tạo mặc định nếu lỗi
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        bookedSeats.clear();
                        Map<String, Object> data = snapshot.getData();
                        if (data != null) {
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                Object value = entry.getValue();
                                if (value instanceof Map) {
                                    Map<String, Object> seatData = (Map<String, Object>) value;
                                    String status = (String) seatData.get("status");
                                    if ("booked".equals(status)) {
                                        bookedSeats.add(entry.getKey());
                                    }
                                }
                            }
                            Log.d("SeatListActivity", "Đã tải bookedSeats: " + bookedSeats);
                        }
                    } else {
                        Log.d("SeatListActivity", "Document không tồn tại, không có ghế nào bị khóa.");
                        bookedSeats.clear();
                    }
                    generateSeatButtons(); // Luôn gọi lại để cập nhật giao diện
                });
    }



    private void generateSeatButtons() {
        String[] rows = {"A", "B", "C", "D", "E"};
        int cols = 5;
        gridSeats.removeAllViews(); // Xóa các button cũ
        gridSeats.setColumnCount(cols);

        float scale = getResources().getDisplayMetrics().density;
        int seatSize = (int) (53 * scale);
        int seatMargin = (int) (6 * scale);

        for (String row : rows) {
            for (int col = 1; col <= cols; col++) {
                String seatId = row + col;
                Button seatButton = new Button(this);
                seatButton.setText(seatId);
                seatButton.setTextSize(12);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = seatSize;
                params.height = seatSize;
                params.setMargins(seatMargin, seatMargin, seatMargin, seatMargin);
                seatButton.setLayoutParams(params);

                if (bookedSeats.contains(seatId)) {
                    seatButton.setBackgroundResource(R.drawable.bg_seat_unavailable);
                    seatButton.setEnabled(false);
                } else {
                    seatButton.setBackgroundResource(R.drawable.bg_seat_available);
                    int finalCol = col;
                    seatButton.setOnClickListener(v -> {
                        if (selectedSeats.contains(seatId)) {
                            selectedSeats.remove(seatId);
                            seatButton.setBackgroundResource(R.drawable.bg_seat_available);
                        } else {
                            selectedSeats.add(seatId);
                            seatButton.setBackgroundResource(R.drawable.bg_seat_selected);
                        }
                        updateSeatInfo();
                    });
                }

                gridSeats.addView(seatButton);
            }
        }
    }

    private void updateSeatInfo() {
        textSelectedSeats.setText(selectedSeats.size() + " Ghế: " + TextUtils.join(", ", selectedSeats));

        if (isPriceDataReady && movie != null) {
            int viewCount = movie.getViewCount();
            int basePrice = (movie.getPrice() > 0) ? movie.getPrice() : 100000;

            latestTotalPrice = PriceCalculator.calculateDynamicPrice(
                    selectedDate,
                    timeSlot,
                    selectedSeats.size(),
                    basePrice,
                    weekendPricing,
                    peakHours,
                    peakHourIncrease,
                    hotMovieThreshold,
                    hotMovieIncrease,
                    viewCount
            );

            textTotalPrice.setText(latestTotalPrice + " VND");
        } else {
            textTotalPrice.setText((selectedSeats.size() * 100000) + " VND");
        }

        btnConfirm.setEnabled(isPriceDataReady && !selectedSeats.isEmpty());
    }
}