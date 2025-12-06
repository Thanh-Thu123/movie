package com.example.projectfilm.ui.admin.movie;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.projectfilm.R;
import com.example.projectfilm.data.model.Movie;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;


public class DetailActivity extends AppCompatActivity {
    EditText editTextTitle, editTextDescription, editTextActors, editTextProducer;
    TextView editTextYear;
    Spinner spinnerCountries, spinnerGenre;
    ImageView imageView, btnBack;
    AppCompatButton btnEdit, btnDelete;
    Uri imageUri;
    FirebaseFirestore db;
    String movieId;

    ArrayList<String> countryList = new ArrayList<>();
    ArrayAdapter<String> countryAdapter;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextTitle = findViewById(R.id.editText);
        editTextDescription = findViewById(R.id.editText2);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        imageView = findViewById(R.id.imageView);
        editTextActors = findViewById(R.id.editText0);
        editTextProducer = findViewById(R.id.editText6);
        editTextYear = findViewById(R.id.year);
        spinnerCountries = findViewById(R.id.spinnerCountries);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        // Spinner genre
        String[] genres = {"Kinh dị", "Hoạt hình", "Hài", "Hành động", "Khoa học viễn tưởng", "Phiêu lưu", "Tâm lý", "Trinh thám", "Tình cảm"};
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(genres));
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);

        // Lấy quốc gia, sau đó load chi tiết phim
        getCountries(() -> {
            Intent intent = getIntent();
            if (intent != null) {
                movieId = intent.getStringExtra("movieId");
                if (movieId != null) {
                    loadMovieDetails(movieId);
                } else {
                    Toast.makeText(this, "Không có movieId!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Lỗi khi lấy dữ liệu!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnEdit.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String genre = spinnerGenre.getSelectedItem().toString();
            String actors = editTextActors.getText().toString().trim();
            String producer = editTextProducer.getText().toString().trim();
            String year = editTextYear.getText().toString().trim();
            String country = spinnerCountries.getSelectedItem().toString();

            if (title.isEmpty() || description.isEmpty() || genre.isEmpty() || actors.isEmpty() || producer.isEmpty() || year.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("title", title);
            updatedData.put("description", description);
            updatedData.put("genre", genre);
            updatedData.put("actors", actors);
            updatedData.put("producer", producer);
            updatedData.put("year", year);
            updatedData.put("country", country);

            db.collection("movies").document(movieId)
                    .update(updatedData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnDelete.setOnClickListener(v -> {
            db.collection("movies").document(movieId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void loadMovieDetails(String movieId) {
        db.collection("movies").document(movieId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Movie movie = documentSnapshot.toObject(Movie.class);
                        if (movie != null) {
                            editTextTitle.setText(movie.getTitle());
                            editTextDescription.setText(movie.getDescription());
                            editTextActors.setText(movie.getActors());
                            editTextProducer.setText(movie.getProducer());
                            editTextYear.setText(movie.getYear());

                            int genrePosition = ((ArrayAdapter<String>) spinnerGenre.getAdapter()).getPosition(movie.getGenre());
                            spinnerGenre.setSelection(genrePosition);

                            if (countryAdapter != null) {
                                int countryPosition = countryAdapter.getPosition(movie.getCountry());
                                spinnerCountries.setSelection(countryPosition);
                            }

                            String posterBase64 = movie.getPosterBase64();
                            if (posterBase64 != null && !posterBase64.isEmpty()) {
                                try {
                                    byte[] decodedBytes = Base64.decode(posterBase64, Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                    imageView.setImageBitmap(bitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "Lỗi giải mã ảnh!", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy phim!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DetailActivity", "Lỗi Firestore: ", e);
                    Toast.makeText(this, "Lỗi khi tải dữ liệu!", Toast.LENGTH_SHORT).show();
                });
    }

    private void getCountries(Runnable callback) {
        Request request = new Request.Builder()
                .url("https://restcountries.com/v3.1/all?fields=name")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(DetailActivity.this, "Lỗi tải danh sách quốc gia: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if (callback != null) callback.run();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(res);

                        countryList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            JSONObject nameObj = obj.getJSONObject("name");
                            String commonName = nameObj.getString("common");
                            countryList.add(commonName);
                        }

                        runOnUiThread(() -> {
                            countryAdapter = new ArrayAdapter<>(
                                    DetailActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    countryList
                            );
                            countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCountries.setAdapter(countryAdapter);
                            if (callback != null) callback.run();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(DetailActivity.this, "Lỗi xử lý JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            if (callback != null) callback.run();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(DetailActivity.this, "API trả lỗi: " + response.code(), Toast.LENGTH_LONG).show();
                        if (callback != null) callback.run();
                    });
                }
            }
        });
    }
}