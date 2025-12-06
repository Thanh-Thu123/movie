package com.example.projectfilm.ui.admin.movie;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfilm.R;
import com.example.projectfilm.adapter.adapter_all_film;
import com.example.projectfilm.data.model.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MovieManage extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppCompatButton btnAdd;
    private ImageView btnBack;
    private adapter_all_film adapter;
    private List<Movie> filmList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_manage);

        // Áp padding hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // Ánh xạ View
        recyclerView = findViewById(R.id.recyclerView);
        btnAdd = findViewById(R.id.btn_add);
        btnBack = findViewById(R.id.btn_back);

        // Danh sách dữ liệu
        filmList = new ArrayList<>();
        adapter = new adapter_all_film(this, filmList);

        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Bắt sự kiện nút
        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MovieManage.this, AddFilm.class);
            startActivity(intent);
        });

        // Tải dữ liệu từ Firestore
        listenToMovieUpdates();
    }

    private void listenToMovieUpdates() {
        db.collection("movies").addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;

            boolean changed = false;

            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                Movie movie = dc.getDocument().toObject(Movie.class);
                if (movie == null) continue;

                movie.setMovieId(dc.getDocument().getId());

                switch (dc.getType()) {
                    case ADDED:
                        filmList.add(movie);
                        changed = true;
                        break;
                    case MODIFIED:
                        for (int i = 0; i < filmList.size(); i++) {
                            if (filmList.get(i).getMovieId().equals(movie.getMovieId())) {
                                filmList.set(i, movie);
                                changed = true;
                                break;
                            }
                        }
                        break;
                    case REMOVED:
                        for (int i = 0; i < filmList.size(); i++) {
                            if (filmList.get(i).getMovieId().equals(movie.getMovieId())) {
                                filmList.remove(i);
                                changed = true;
                                break;
                            }
                        }
                        break;
                }
            }

            if (changed) adapter.notifyDataSetChanged();
        });
    }
}
