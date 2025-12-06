package com.example.projectfilm.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectfilm.MainActivity;
import com.example.projectfilm.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Kiểm tra trạng thái đăng nhập bằng Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Sử dụng Handler để tạo delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (currentUser != null) {
                // Người dùng đã đăng nhập, chuyển đến MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                // Người dùng chưa đăng nhập, chuyển đến RegisterActivity
                Intent intent = new Intent(SplashActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
            finish(); // Đóng SplashActivity để không quay lại
        },2000);
    }
}