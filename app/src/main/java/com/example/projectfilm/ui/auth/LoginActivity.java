package com.example.projectfilm.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projectfilm.MainActivity;
import com.example.projectfilm.ui.auth.RegisterActivity;
import com.example.projectfilm.ui.auth.AdminLoginActivity;
import com.example.projectfilm.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các thành phần giao diện
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerText = findViewById(R.id.registerText);
        TextView adminLoginText = findViewById(R.id.adminLoginText);

        // Xử lý sự kiện nút đăng nhập
        loginButton.setOnClickListener(v -> loginUser());

        // Chuyển sang RegisterActivity
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Chuyển sang AdminLoginActivity
        adminLoginText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        // Chuyển đến MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Đóng LoginActivity để không quay lại
                    } else {
                        Toast.makeText(this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}