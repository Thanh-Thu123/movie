package com.example.projectfilm.ui.user.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectfilm.MainActivity;
import com.example.projectfilm.R;
import com.example.projectfilm.ui.user.booking.BookingHistoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etAddress;
    private TextView tvPoints; // Thêm TextView cho số điểm
    private Button btnOrderHistory, btnHotline, btnInviteFriends, btnLogout;
    private ProgressBar progressBar;

    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private DocumentReference userDoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ view
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        tvPoints = findViewById(R.id.tvPoints); // Ánh xạ TextView số điểm
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnHotline = findViewById(R.id.btnHotline);
        btnInviteFriends = findViewById(R.id.btnInviteFriends);
        Button btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.profileLoading);

        setEditingEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            userDoc = firestore.collection("users").document(uid);

            userDoc.get()
                    .addOnSuccessListener(snapshot -> {
                        progressBar.setVisibility(View.GONE);
                        if (snapshot.exists()) {
                            etFullName.setText(snapshot.getString("name") != null ? snapshot.getString("name") : "");
                            etEmail.setText(snapshot.getString("email") != null ? snapshot.getString("email") : "");
                            etAddress.setText(snapshot.getString("address") != null ? snapshot.getString("address") : "");
                            // Lấy và hiển thị số điểm
                            Long points = snapshot.getLong("points");
                            tvPoints.setText("Số điểm: " + (points != null ? points : 0));
                        } else {
                            // Nếu tài liệu không tồn tại, tạo mới với points = 0
                            Map<String, Object> initialData = new HashMap<>();
                            initialData.put("points", 0);
                            userDoc.set(initialData, SetOptions.merge());
                            tvPoints.setText("Số điểm: 0");
                            Toast.makeText(this, "Tạo mới dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                        }
                        setEditingEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        setEditingEnabled(true);
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Sự kiện các nút khác
        btnOrderHistory.setOnClickListener(v -> {
            Log.d("DEBUG_BUTTON", "Bấm nút Lịch sử đơn hàng");
            startActivity(new Intent(this, BookingHistoryActivity.class));
        });

        btnHotline.setOnClickListener(v ->
                Toast.makeText(this, "Hotline: 19001900", Toast.LENGTH_SHORT).show());

        btnInviteFriends.setOnClickListener(v ->
                Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show());

        btnSave.setOnClickListener(v -> saveUserData());

        btnLogout.setOnClickListener(v -> saveAndReturnHome());
    }

    private void saveUserData() {
        if (currentUser != null && userDoc != null) {
            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ họ tên, email và địa chỉ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy số điểm hiện tại từ TextView
            String pointsText = tvPoints.getText().toString().replace("Số điểm: ", "");
            int currentPoints = Integer.parseInt(pointsText);

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("email", email);
            data.put("address", address);
            data.put("points", currentPoints); // Lưu số điểm hiện tại

            Log.d("FIREBASE_DEBUG", "Lưu dữ liệu: " + data);

            progressBar.setVisibility(View.VISIBLE);
            userDoc.set(data, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Đã lưu thành công", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e("FIREBASE_ERROR", "Lỗi lưu dữ liệu", e);
                        Toast.makeText(this, "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Log.e("FIREBASE_ERROR", "currentUser hoặc userDoc null");
        }
    }

    private void saveAndReturnHome() {
        if (currentUser != null && userDoc != null) {
            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ họ tên, email và địa chỉ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy số điểm hiện tại từ TextView
            String pointsText = tvPoints.getText().toString().replace("Số điểm: ", "");
            int currentPoints = Integer.parseInt(pointsText);

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("email", email);
            data.put("address", address);
            data.put("points", currentPoints); // Lưu số điểm hiện tại

            progressBar.setVisibility(View.VISIBLE);
            userDoc.set(data, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Đã lưu và quay lại trang chủ", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("openHome", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e("FIREBASE_ERROR", "Lỗi lưu dữ liệu khi quay về", e);
                        Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(this, "Không xác định được người dùng", Toast.LENGTH_SHORT).show();
        }
    }

    private void setEditingEnabled(boolean enabled) {
        etFullName.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etAddress.setEnabled(enabled);
        // tvPoints không cho phép chỉnh sửa
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Không tự lưu khi rời màn hình
    }
}