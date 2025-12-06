package com.example.projectfilm.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectfilm.R;
import com.example.projectfilm.ui.admin.booking.OrderManage;
import com.example.projectfilm.ui.admin.movie.MovieManage;

public class admin extends AppCompatActivity {
    ImageView btn_manage_movies, btn_manage_order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btn_manage_movies = findViewById(R.id.btn_manage_movies);
        btn_manage_movies.setOnClickListener(v -> {
            Intent intent = new Intent(admin.this, MovieManage.class);
            startActivity(intent);
        });
        btn_manage_order = findViewById(R.id.btn_manage_order);
        btn_manage_order.setOnClickListener(v -> {
            Intent intent = new Intent(admin.this, OrderManage.class);
            startActivity(intent);
        });


    }
}