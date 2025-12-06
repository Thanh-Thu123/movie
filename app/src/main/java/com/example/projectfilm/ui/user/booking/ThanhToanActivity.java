package com.example.projectfilm.ui.user.booking;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectfilm.R;
import com.example.projectfilm.ui.user.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import com.example.projectfilm.data.model.Movie;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ThanhToanActivity extends AppCompatActivity {

    private static final String VNP_TMN_CODE = "7SW34HP6";
    private static final String VNP_HASH_SECRET = "COBYG8XL0JETUWBU0LV7KMHM9WNS5EL8";
    private static final String VNP_URL_TEST = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_RETURN_URL_APP = "projectfilm://thanhtoanthanhcong";

    private TextView textCinema, textTime, textSeats, textPrice, textMovieName;
    private EditText editName, editEmail;
    private RadioGroup radioGroup;
    private RadioButton radioMomo, radioBank, radioVnpay;
    private CheckBox cbUsePoints;
    private Button btnConfirmPayment;

    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    private String cinema, time, seats, price, selectedDate; // Thêm selectedDate
    private double totalAmount;
    private double displayAmount;
    private String selectedPaymentMethod;
    private String movieName;
    private Movie movie;
    private int currentPoints;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);

        // Ánh xạ View
        textCinema = findViewById(R.id.textCinema);
        textTime = findViewById(R.id.textTime);
        textSeats = findViewById(R.id.textSeats);
        textPrice = findViewById(R.id.textPrice);
        textMovieName = findViewById(R.id.textMovieName);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        radioGroup = findViewById(R.id.radioGroup);
        radioMomo = findViewById(R.id.radioMomo);
        radioBank = findViewById(R.id.radioBank);
        radioVnpay = findViewById(R.id.radioVnpay);
        cbUsePoints = findViewById(R.id.cb_use_points);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        // Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        // Nhận dữ liệu từ Intent
        Intent initialIntent = getIntent();
        cinema = initialIntent.getStringExtra("cinema");
        time = initialIntent.getStringExtra("time");
        seats = initialIntent.getStringExtra("seats");
        selectedDate = initialIntent.getStringExtra("date"); // Nhận selectedDate
        String priceStr = initialIntent.getStringExtra("price");
        movie = (Movie) initialIntent.getSerializableExtra("movie");
        if (movie != null) {
            movieName = movie.getTitle();
        }
        if (movieName != null && !movieName.isEmpty()) {
            textMovieName.setText("🎞️ Phim: " + movieName);
        }

        try {
            assert priceStr != null;
            totalAmount = Double.parseDouble(priceStr);
            displayAmount = totalAmount;
            price = priceStr;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Lỗi định dạng giá tiền.", Toast.LENGTH_SHORT).show();
            Log.e("ThanhToanActivity", "Lỗi chuyển đổi giá tiền: " + e.getMessage());
            finish();
            return;
        }

        // Hiển thị dữ liệu lên màn hình
        updateOrderSummaryUI();

        // Lấy thông tin người dùng và điểm từ Firestore
        if (currentUser != null) {
            String uid = currentUser.getUid();
            firestore.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String name = snapshot.getString("name");
                            String email = snapshot.getString("email");
                            Long points = snapshot.getLong("points");
                            currentPoints = points != null ? points.intValue() : 0;
                            if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
                                Toast.makeText(this, "Vui lòng hoàn thiện thông tin cá nhân trước khi thanh toán", Toast.LENGTH_LONG).show();
                                Intent profileIntent = new Intent(this, ProfileActivity.class);
                                profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(profileIntent);
                                finish();
                            } else {
                                editName.setText(name);
                                editEmail.setText(email);
                                editName.setEnabled(false);
                                editEmail.setEnabled(false);
                                cbUsePoints.setEnabled(currentPoints >= 10);
                                if (currentPoints < 10) {
                                    cbUsePoints.setText("Cần 10 điểm để giảm 10,000 VNĐ (Bạn có: " + currentPoints + ")");
                                }
                            }
                        } else {
                            Toast.makeText(this, "Không tìm thấy thông tin người dùng, vui lòng cập nhật hồ sơ.", Toast.LENGTH_LONG).show();
                            Intent profileIntent = new Intent(this, ProfileActivity.class);
                            profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(profileIntent);
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ThanhToanActivity", "Lỗi tải thông tin người dùng: " + e.getMessage());
                        Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Xử lý sự kiện CheckBox
        cbUsePoints.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && currentPoints >= 10) {
                displayAmount = totalAmount - 10000;
            } else {
                displayAmount = totalAmount;
            }
            updateOrderSummaryUI();
        });

        // Đặt VNPay làm mặc định
        if (radioVnpay != null) {
            radioVnpay.setChecked(true);
            selectedPaymentMethod = "VNPay";
        }
        if (radioMomo != null) {
            radioMomo.setEnabled(true);
            radioMomo.setAlpha(1f);
        }
        if (radioBank != null) {
            radioBank.setEnabled(true);
            radioBank.setAlpha(1f);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = findViewById(checkedId);
            if (checkedRadioButton != null) {
                selectedPaymentMethod = checkedRadioButton.getText().toString().replace(" ", "");
            }
        });

        btnConfirmPayment.setOnClickListener(v -> {
            if (totalAmount <= 0 || seats == null || seats.isEmpty()) {
                Toast.makeText(this, "Không có đơn hàng để thanh toán.", Toast.LENGTH_SHORT).show();
                return;
            }

            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId == -1) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu thông tin vào SharedPreferences, bao gồm selectedDate
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("current_booking_cinema", cinema);
            editor.putString("current_booking_time", time);
            editor.putString("current_booking_seats", seats);
            editor.putString("current_booking_price", String.valueOf(displayAmount));
            editor.putString("current_booking_name", editName.getText().toString().trim());
            editor.putString("current_booking_email", editEmail.getText().toString().trim());
            editor.putString("current_booking_movieName", movieName);
            editor.putString("current_booking_paymentMethod", selectedPaymentMethod);
            editor.putBoolean("current_booking_use_points", cbUsePoints.isChecked());
            editor.putString("current_booking_date", selectedDate); // Lưu selectedDate
            editor.apply();

            if (checkedId == R.id.radioVnpay) {
                initiateVnpayPayment();
            } else if (checkedId == R.id.radioMomo) {
                int userId = currentUser != null ? currentUser.getUid().hashCode() : 0;
                String momoAmount = String.valueOf((long) displayAmount);
                Log.d("ThanhToanActivity", "MoMo Amount: " + momoAmount);
                new MomoPaymentTask(this, userId, momoAmount).execute();
                Toast.makeText(this, "Đang chuyển sang cổng thanh toán MoMo...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Phương thức " + selectedPaymentMethod + " không được hỗ trợ.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateOrderSummaryUI() {
        textCinema.setText("🎬 Rạp: " + cinema);
        textTime.setText("🕒 Giờ chiếu: " + time);
        textSeats.setText("💺 Ghế: " + seats);
        textPrice.setText(String.format(Locale.getDefault(), "💰 Tổng tiền: %,.0f đ", displayAmount));
    }

    private void initiateVnpayPayment() {
        Toast.makeText(this, "Đang chuẩn bị thanh toán qua VNPay...", Toast.LENGTH_LONG).show();
        String vnpayUrl = generateVNPAYUrl(displayAmount, seats);
        if (vnpayUrl != null) {
            openPaymentGateway(vnpayUrl);
        } else {
            Toast.makeText(this, "Không thể tạo URL thanh toán VNPay.", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateVNPAYUrl(double amount, String orderInfoSuffix) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_Amount = String.valueOf((long) (amount * 100));
        String vnp_CurrCode = "VND";
        String vnp_TxnRef = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()) + "_" + System.currentTimeMillis() % 10000;
        String vnp_OrderInfo = "ThanhToanVePhim_" + orderInfoSuffix.replace(",", "_") + "_" + vnp_TxnRef;
        String vnp_OrderType = "other";
        String vnp_Locale = "vn";
        String vnp_IpAddr = "127.0.0.1";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String vnp_CreateDate = formatter.format(new Date());

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", VNP_TMN_CODE);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", VNP_RETURN_URL_APP);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                try {
                    hashData.append(fieldName);
                    hashData.append('=');
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                    }
                    query.append('=');
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    }
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                } catch (Exception e) {
                    Log.e("ThanhToanActivity", "Lỗi mã hóa tham số: " + fieldName);
                    return null;
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());
        if (vnp_SecureHash.isEmpty()) {
            return null;
        }
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return VNP_URL_TEST + "?" + queryUrl;
    }

    private void openPaymentGateway(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Không thể mở cổng thanh toán.", Toast.LENGTH_LONG).show();
                Log.e("ThanhToanActivity", "Lỗi mở URL thanh toán: " + e.getMessage());
            }
        } else {
            Toast.makeText(this, "URL thanh toán trống.", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchSuccessActivity(String cinema, String time, String seats, String price,
                                       String name, String email, String movieName) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("current_booking_cinema", cinema);
        editor.putString("current_booking_time", time);
        editor.putString("current_booking_seats", seats);
        editor.putString("current_booking_price", price);
        editor.putString("current_booking_name", name);
        editor.putString("current_booking_email", email);
        editor.putString("current_booking_movieName", movieName);
        editor.putString("current_booking_paymentMethod","vnpay" );
        editor.putBoolean("current_booking_use_points", cbUsePoints.isChecked());
        editor.putString("current_booking_date", selectedDate); // Lưu selectedDate
        editor.apply();

        Intent confirmIntent = new Intent(ThanhToanActivity.this, ThanhToanThanhCongActivity.class);
        confirmIntent.putExtra("cinema", cinema);
        confirmIntent.putExtra("time", time);
        confirmIntent.putExtra("seats", seats);
        confirmIntent.putExtra("price", price);
        confirmIntent.putExtra("name", name);
        confirmIntent.putExtra("email", email);
        confirmIntent.putExtra("paymentMethod", "vnpay");
        confirmIntent.putExtra("movieName", movieName);
        confirmIntent.putExtra("date", selectedDate); // Truyền selectedDate qua Intent
        startActivity(confirmIntent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null && data.getScheme() != null && data.getScheme().equals("projectfilm") && data.getHost() != null && data.getHost().equals("payment-result")) {
            Log.d("VNPayReturn", "Received deep link: " + data.toString());
            Toast.makeText(this, "Đã quay lại từ VNPay. Đang kiểm tra kết quả...", Toast.LENGTH_LONG).show();

            String vnp_ResponseCode = data.getQueryParameter("vnp_ResponseCode");
            String vnp_TransactionStatus = data.getQueryParameter("vnp_TransactionStatus");
            String vnp_SecureHash = data.getQueryParameter("vnp_SecureHash");
            String vnp_TxnRef = data.getQueryParameter("vnp_TxnRef");

            Map<String, String> vnp_Params = new HashMap<>();
            for (String key : data.getQueryParameterNames()) {
                if (!key.equals("vnp_SecureHash")) {
                    vnp_Params.put(key, data.getQueryParameter(key));
                }
            }

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = vnp_Params.get(fieldName);
                try {
                    hashData.append(fieldName);
                    hashData.append('=');
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    }
                    if (i < fieldNames.size() - 1) {
                        hashData.append('&');
                    }
                } catch (Exception e) {
                    Log.e("VNPayReturn", "Lỗi mã hóa tham số: " + fieldName);
                }
            }

            String calculatedSecureHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());
            if (calculatedSecureHash.equals(vnp_SecureHash)) {
                if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
                    Toast.makeText(this, "Thanh toán VNPay thành công!", Toast.LENGTH_LONG).show();
                    Log.d("VNPayReturn", "Thanh toán thành công cho TxnRef: " + vnp_TxnRef);
                    launchSuccessActivity(
                            cinema, time, seats, String.valueOf(displayAmount),
                            editName.getText().toString().trim(),
                            editEmail.getText().toString().trim(),
                            movieName
                    );
                } else {
                    String message = "Thanh toán VNPay thất bại hoặc đang chờ xử lý. Mã: " + vnp_ResponseCode + ", Trạng thái: " + vnp_TransactionStatus;
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    Log.e("VNPayReturn", "Thanh toán thất bại: " + message);
                    Intent mainIntent = new Intent(ThanhToanActivity.this, com.example.projectfilm.MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                    finish();
                }
            } else {
                Toast.makeText(this, "Cảnh báo: Chữ ký VNPay không hợp lệ.", Toast.LENGTH_LONG).show();
                Log.e("VNPayReturn", "SecureHash không hợp lệ!");
                Intent mainIntent = new Intent(ThanhToanActivity.this, com.example.projectfilm.MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                finish();
            }
        }
        // Xử lý MoMo
        if (data.getScheme().equals("projectfilm") && data.getHost().equals("payment-result-momo")) {
            Log.d("MomoReturn", "Received MoMo deep link: " + data.toString());
            Toast.makeText(this, "Đã quay lại từ MoMo. Đang kiểm tra kết quả...", Toast.LENGTH_LONG).show();

            String status = data.getQueryParameter("status");  // Ví dụ MoMo trả về
            String message = data.getQueryParameter("message");
            Log.d("MomoReturn", "Status: " + status + ", Message: " + message);

            if ("0".equals(status)) {
                Toast.makeText(this, "Thanh toán MoMo thành công!", Toast.LENGTH_LONG).show();
                launchSuccessActivity(
                        cinema, time, seats, String.valueOf(displayAmount),
                        editName.getText().toString().trim(),
                        editEmail.getText().toString().trim(),
                        movieName
                );
            } else {
                Toast.makeText(this, "Thanh toán MoMo thất bại hoặc bị hủy.", Toast.LENGTH_LONG).show();
                Intent mainIntent = new Intent(ThanhToanActivity.this, com.example.projectfilm.MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                finish();
            }
        }
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("Khóa hoặc dữ liệu không được null.");
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            Log.e("ThanhToanActivity", "Lỗi tạo HMAC SHA512: " + ex.getMessage());
            return "";
        }
    }
}