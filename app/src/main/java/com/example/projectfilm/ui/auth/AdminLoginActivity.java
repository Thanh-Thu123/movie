package com.example.projectfilm.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.example.projectfilm.R;
import com.example.projectfilm.ui.admin.dashboardFragment.DashboardFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText editTextAdminEmail;
    private EditText editTextAdminPassword;
    private Button buttonAdminLogin;
    private TextView textViewBackToMainLogin;
    private LinearLayout adminLoginFormContainer;
    private FrameLayout adminFragmentContainer;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login); // Sử dụng layout riêng cho AdminLoginActivity

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các View từ activity_admin_login.xml
        adminLoginFormContainer = findViewById(R.id.admin_login_form_container);
        editTextAdminEmail = findViewById(R.id.editTextAdminEmail);
        editTextAdminPassword = findViewById(R.id.editTextAdminPassword);
        buttonAdminLogin = findViewById(R.id.buttonAdminLogin);
        textViewBackToMainLogin = findViewById(R.id.textViewBackToMainLogin);
        adminFragmentContainer = findViewById(R.id.admin_fragment_container);

        // Đảm bảo chỉ hiển thị form admin
        if (adminLoginFormContainer != null) {
            adminLoginFormContainer.setVisibility(View.VISIBLE);
        }
        if (adminFragmentContainer != null) {
            adminFragmentContainer.setVisibility(View.GONE);
        }

        // Xử lý sự kiện đăng nhập Admin
        buttonAdminLogin.setOnClickListener(v -> {
            String email = editTextAdminEmail.getText().toString().trim();
            String password = editTextAdminPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(AdminLoginActivity.this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thực hiện đăng nhập với Firebase Auth
            adminLogin(email, password);
        });

        // Xử lý sự kiện quay lại màn hình đăng nhập chính
        if (textViewBackToMainLogin != null) {
            textViewBackToMainLogin.setOnClickListener(v -> finish());
        }
    }

    private void adminLogin(String email, String password) {
        // Kiểm tra email admin (có thể thay bằng kiểm tra từ Firestore như phiên bản trước)
        if (!email.equals("admin@moviebookingapp.com")) { // Sử dụng email cố định cho demo
            Toast.makeText(AdminLoginActivity.this, "Bạn không có quyền admin.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Toast.makeText(AdminLoginActivity.this, "Đăng nhập Admin thành công!", Toast.LENGTH_SHORT).show();

                                // Ẩn form admin và hiển thị DashboardFragment
                                if (adminLoginFormContainer != null) {
                                    adminLoginFormContainer.setVisibility(View.GONE);
                                }
                                if (adminFragmentContainer != null) {
                                    adminFragmentContainer.setVisibility(View.VISIBLE);
                                }
                                showDashboardFragment();
                            }
                        } else {
                            // Đăng nhập thất bại
                            Toast.makeText(AdminLoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showDashboardFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // R.id.admin_fragment_container là ID của FrameLayout trong activity_admin_login.xml
        transaction.replace(R.id.admin_fragment_container, new DashboardFragment());
        transaction.addToBackStack(null); // Cho phép quay lại form đăng nhập
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            if (adminLoginFormContainer != null) {
                adminLoginFormContainer.setVisibility(View.VISIBLE);
            }
            if (adminFragmentContainer != null) {
                adminFragmentContainer.setVisibility(View.GONE);
            }
        } else {
            super.onBackPressed();
        }
    }
}