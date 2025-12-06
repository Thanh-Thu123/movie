package com.example.projectfilm.ui.user.booking;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectfilm.MainActivity;
import com.example.projectfilm.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ThanhToanThanhCongActivity extends AppCompatActivity {

    TextView textCinema, textTime, textSeats, textPrice, textName, textEmail, textPaymentMethod, textMovie, textBookingTime;
    private static final String VNP_HASH_SECRET = "COBYG8XL0JETUWBU0LV7KMHM9WNS5EL8";
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan_thanh_cong);

        textCinema = findViewById(R.id.textCinema);
        textTime = findViewById(R.id.textTime);
        textSeats = findViewById(R.id.textSeats);
        textPrice = findViewById(R.id.textPrice);
        textName = findViewById(R.id.textName);
        textEmail = findViewById(R.id.textEmail);
        textPaymentMethod = findViewById(R.id.textPaymentMethod);
        textMovie = findViewById(R.id.textMovie);
        textBookingTime = findViewById(R.id.textBookingTime);

        handleIncomingIntent(getIntent());

        findViewById(R.id.btnBackToHome).setOnClickListener(view -> {
            Intent homeIntent = new Intent(ThanhToanThanhCongActivity.this, MainActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        Uri data = intent.getData();
        String paymentStatusMessage = "Chưa xác định trạng thái thanh toán";
        String actualPaymentMethod = "Không xác định";

        String receivedMethod = intent.getStringExtra("paymentMethod");
        if (receivedMethod != null && !receivedMethod.isEmpty()) {
            actualPaymentMethod = receivedMethod;
        } else {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            actualPaymentMethod = sharedPrefs.getString("current_booking_paymentMethod", "Không xác định");
        }

        // Lấy selectedDate
        selectedDate = intent.getStringExtra("date");
        if (selectedDate == null || selectedDate.isEmpty()) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            selectedDate = sharedPrefs.getString("current_booking_date", "Không xác định");
        }

        displayBookingInfoFromSharedPreferences();

        if (data != null && "projectfilm".equals(data.getScheme()) && "payment-success".equals(data.getHost())) {
            String vnp_ResponseCode = data.getQueryParameter("vnp_ResponseCode");
            String vnp_TransactionStatus = data.getQueryParameter("vnp_TransactionStatus");
            String vnp_SecureHash = data.getQueryParameter("vnp_SecureHash");

            Map<String, String> vnp_Params = new HashMap<>();
            for (String key : data.getQueryParameterNames()) {
                if (!key.equals("vnp_SecureHash")) {
                    String value = data.getQueryParameter(key);
                    if (value != null) {
                        vnp_Params.put(key, value);
                    }
                }
            }

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = vnp_Params.get(fieldName);
                try {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (i < fieldNames.size() - 1) hashData.append('&');
                } catch (Exception e) {
                    Log.e("VNPayReturn", "Lỗi mã hóa: " + fieldName, e);
                }
            }

            String calculatedSecureHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());
            if (vnp_SecureHash != null && calculatedSecureHash.equals(vnp_SecureHash)) {
                if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
                    paymentStatusMessage = "Thanh toán VNPay thành công!";
                    actualPaymentMethod = "VNPay";
                    Toast.makeText(this, paymentStatusMessage, Toast.LENGTH_LONG).show();
                    saveBookingToFirestore(actualPaymentMethod);
                } else {
                    Toast.makeText(this, "Thanh toán VNPay thất bại.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Chữ ký không hợp lệ!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return;
            }
        } else {
            saveBookingToFirestore(actualPaymentMethod);
        }

        textPaymentMethod.setText("Thanh toán bằng: " + actualPaymentMethod);
    }

    private void displayBookingInfoFromSharedPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String cinema = sharedPrefs.getString("current_booking_cinema", "Không xác định");
        String time = sharedPrefs.getString("current_booking_time", "Không xác định");
        String seats = sharedPrefs.getString("current_booking_seats", "Không xác định");
        String price = sharedPrefs.getString("current_booking_price", "0");
        String name = sharedPrefs.getString("current_booking_name", "Không xác định");
        String email = sharedPrefs.getString("current_booking_email", "Không xác định");
        String movieName = sharedPrefs.getString("current_booking_movieName", "Không xác định");

        textCinema.setText("Rạp: " + cinema);
        textTime.setText("Giờ chiếu: " + time);
        textSeats.setText("Ghế: " + seats);
        textPrice.setText("Tổng tiền: " + price + " VND");
        textName.setText("Tên: " + name);
        textEmail.setText("Email: " + email);
        textMovie.setText("Phim: " + movieName);

        String bookingTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        textBookingTime.setText("Ngày đặt: " + bookingTime);
    }

    private void saveBookingToFirestore(String paymentMethod) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String cinema = sharedPrefs.getString("current_booking_cinema", null);
        String time = sharedPrefs.getString("current_booking_time", null);
        String seats = sharedPrefs.getString("current_booking_seats", null);
        String price = sharedPrefs.getString("current_booking_price", null);
        String name = sharedPrefs.getString("current_booking_name", null);
        String email = sharedPrefs.getString("current_booking_email", null);
        String movieName = sharedPrefs.getString("current_booking_movieName", null);
        boolean usePoints = sharedPrefs.getBoolean("current_booking_use_points", false);

        if (selectedDate == null || selectedDate.isEmpty() || selectedDate.equals("Không xác định")) {
            selectedDate = sharedPrefs.getString("current_booking_date", null);
        }

        if (cinema == null || time == null || seats == null || price == null || name == null ||
                email == null || movieName == null || selectedDate == null ||
                cinema.equals("Không xác định") || time.equals("Không xác định") ||
                selectedDate.equals("Không xác định") || seats.trim().isEmpty()) {
            Toast.makeText(this, "Không đủ dữ liệu để lưu vé!", Toast.LENGTH_LONG).show();
            Log.e("ThanhToanThanhCong", "Dữ liệu không hợp lệ để khóa ghế.");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> booking = new HashMap<>();
        booking.put("cinema", cinema);
        booking.put("time", time);
        booking.put("seats", seats);
        booking.put("price", price);
        booking.put("name", name);
        booking.put("email", email);
        booking.put("paymentMethod", paymentMethod);
        booking.put("movieName", movieName);
        booking.put("timestamp", System.currentTimeMillis());
        booking.put("userId", user.getUid());
        booking.put("status", "booked");

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(docRef -> {
                    String statusDocId = getSeatStatusDocId(cinema, time, this.selectedDate);

                    Map<String, Object> seatUpdates = new HashMap<>();
                    for (String seat : seats.split(",\\s*")) {
                        seatUpdates.put(seat.trim(), Map.of(
                                "status", "booked",
                                "bookingId", docRef.getId(),
                                "timestamp", System.currentTimeMillis()
                        ));
                    }

                    Log.d("DEBUG_GHE", "Khóa ghế với ID: " + statusDocId);
                    Log.d("DEBUG_GHE", "Ghế cập nhật: " + seatUpdates.keySet());

                    db.collection("seats_status")
                            .document(statusDocId)
                            .set(seatUpdates, SetOptions.merge())
                            .addOnSuccessListener(unused -> Log.d("ThanhToan", "Ghế đã được khóa thành công."))
                            .addOnFailureListener(e -> Log.e("ThanhToan", "Lỗi khi khóa ghế: " + e.getMessage()));

                    updateUserPoints(user.getUid(), usePoints);

                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.clear();
                    editor.apply();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu vé: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("ThanhToanThanhCong", "Lỗi khi lưu booking: " + e.getMessage());
                });
    }


    private void updateUserPoints(String userId, boolean usePoints) {
        DocumentReference userDoc = FirebaseFirestore.getInstance().collection("users").document(userId);
        userDoc.get().addOnSuccessListener(snapshot -> {
            Long currentPoints = snapshot.getLong("points");
            if (currentPoints == null) currentPoints = 0L;

            long newPoints = currentPoints + 1;
            if (usePoints && currentPoints >= 10) {
                newPoints -= 10;
                Toast.makeText(this, "Đã sử dụng 10 điểm để giảm giá!", Toast.LENGTH_SHORT).show();
            }

            userDoc.update("points", newPoints)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Bạn đã nhận được 1 điểm!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Log.e("FIREBASE_ERROR", "Lỗi cập nhật điểm: " + e.getMessage()));
        }).addOnFailureListener(e -> Log.e("FIREBASE_ERROR", "Lỗi lấy điểm: " + e.getMessage()));
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            final SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : result) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception ex) {
            Log.e("ThanhToanThanhCong", "Lỗi tạo HMAC: " + ex.getMessage());
            return "";
        }
    }
    public static String getSeatStatusDocId(String cinema, String time, String date) {
        return cinema.trim().replace(" ", "_") + "_" + time.trim().replace(":", "") + "_" + date.trim().replace("/", "-");
    }

}
