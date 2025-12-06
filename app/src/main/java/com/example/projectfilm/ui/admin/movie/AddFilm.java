package com.example.projectfilm.ui.admin.movie;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectfilm.R;
import com.google.firebase.auth.FirebaseAuth;
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

public class AddFilm extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    EditText editTextTitle, editTextDescription, editTextPrices, editTextActors, editTextProducer ;
    TextView editTextYear;
    ImageView imageView, btnBack;
    AppCompatButton btnAdd;
    Spinner spinnerCountries, spinnerGenre;
    Uri imageUri;

    ArrayList<String> countryList = new ArrayList<>();
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_film);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextTitle = findViewById(R.id.editText);
        editTextDescription = findViewById(R.id.editText2);
        editTextProducer = findViewById(R.id.editText6);
        editTextYear = findViewById(R.id.year);
        spinnerGenre = findViewById(R.id.editText3);
        imageView = findViewById(R.id.imageView);
        editTextActors = findViewById(R.id.editText0);
        spinnerCountries = findViewById(R.id.spinnerCountries);
        btnAdd = findViewById(R.id.btn_add);
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        editTextYear.setOnClickListener(v -> {
            final NumberPicker numberPicker = new NumberPicker(AddFilm.this);
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

            numberPicker.setMinValue(1900);
            numberPicker.setMaxValue(currentYear);
            numberPicker.setValue(currentYear);
            new android.app.AlertDialog.Builder(AddFilm.this)
                    .setTitle("Chọn năm")
                    .setView(numberPicker)
                    .setPositiveButton("OK", (dialog, which) -> {
                        editTextYear.setText(String.valueOf(numberPicker.getValue()));
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });




        imageView.setOnClickListener(v -> openGallery());

        // Gọi hàm lấy danh sách quốc gia
        getCountries();
        ArrayList<String> genreList = new ArrayList<>(Arrays.asList("Kinh dị", "Hoạt hình", "Hài", "Hành động", "Khoa học viễn tưởng", "Phiêu lưu", "Tâm lý", "Trinh thám", "Tình cảm"));
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genreList);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);
        btnAdd.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String producer = editTextProducer.getText().toString().trim();
            String genre = spinnerGenre.getSelectedItem().toString();
            String actors = editTextActors.getText().toString().trim();
            String year = editTextYear.getText().toString().trim();
            String prices = editTextPrices.getText().toString().trim();
            String selectedCountry = spinnerCountries.getSelectedItem().toString();

            if (title.isEmpty() || description.isEmpty() || genre.isEmpty() || prices.isEmpty()|| actors.isEmpty()|| year.isEmpty()|| producer.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> movieData = new HashMap<>();
            movieData.put("actors", actors);
            movieData.put("title", title);
            movieData.put("description", description);
            movieData.put("producer", producer);
            movieData.put("genre", genre);
            movieData.put("posterUrl", imageUri != null ? imageUri.toString() : "");
            movieData.put("prices", prices);
            movieData.put("year", year);
            movieData.put("country", selectedCountry);
            movieData.put("status", "Đang chiếu");

            db.collection("movies")
                    .add(movieData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Đã lưu thành công!", Toast.LENGTH_SHORT).show();
                        editTextTitle.setText("");
                        editTextActors.setText("");
                        editTextDescription.setText("");
                        editTextProducer.setText("");
                        editTextPrices.setText("");
                        spinnerGenre.setSelection(0);
                        spinnerCountries.setSelection(0);
                        editTextYear.setText("");
                        imageView.setImageResource(R.drawable.ic_launcher_background);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void getCountries() {
        Request request = new Request.Builder()
                .url("https://restcountries.com/v3.1/all?fields=name")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace(); // log lỗi chi tiết
                runOnUiThread(() ->
                        Toast.makeText(AddFilm.this, "Lỗi tải danh sách quốc gia: " + e.getMessage(), Toast.LENGTH_LONG).show());
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
                            if (obj.has("name")) {
                                JSONObject nameObj = obj.getJSONObject("name");
                                if (nameObj.has("common")) {
                                    String commonName = nameObj.getString("common");
                                    countryList.add(commonName);
                                }
                            }
                        }

                        runOnUiThread(() -> {
                            if (countryList.isEmpty()) {
                                Toast.makeText(AddFilm.this, "Không có quốc gia nào được tải.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    AddFilm.this,
                                    android.R.layout.simple_spinner_item,
                                    countryList
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCountries.setAdapter(adapter);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(AddFilm.this, "Lỗi xử lý JSON: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(AddFilm.this, "API trả lỗi: " + response.code(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }
}
