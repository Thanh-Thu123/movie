package com.example.projectfilm.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;


import androidx.appcompat.app.AppCompatActivity;

import com.example.projectfilm.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase Auth và Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Ánh xạ giao diện
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button registerButton = findViewById(R.id.registerButton);

        // Xử lý nút đăng ký
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Gửi thông tin user lên Firestore
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("email", user.getEmail());
                            userMap.put("name", ""); // Có thể cho nhập thêm nếu muốn
                            userMap.put("address", "");
                            userMap.put("isAdmin", false);

                            firestore.collection("users").document(user.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void goToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Optional: nếu không muốn người dùng quay lại màn hình đăng ký bằng nút back
    }
}
