package com.example.projectfilm.ui.admin.movie;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;


import com.example.projectfilm.R;
import com.example.projectfilm.data.model.Movie;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.projectfilm.ui.admin.movie.MoviePriceAdapter;

import java.util.*;

public class PriceManageFragment extends Fragment {

    private LinearLayout layoutSurcharge;
    private RecyclerView recyclerMoviePrice;
    private Button btnSurcharge, btnMoviePrice;

    private TextView tvFriday, tvSaturday, tvSunday;
    private Button btnEditFriday, btnEditSaturday, btnEditSunday;

    private TextView tvHotPricing, tvPeakPricing;
    private Button btnEditHot, btnEditPeak, btnEditPeakPercent;


    private GridLayout layoutPeakHours;

    private final String DOC_ID = "default"; // Hoặc "nam5" nếu bạn dùng document đó

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_price_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        layoutSurcharge = view.findViewById(R.id.layout_surcharge);
        recyclerMoviePrice = view.findViewById(R.id.recycler_movie_price);
        recyclerMoviePrice.setLayoutManager(new LinearLayoutManager(getContext()));

        btnSurcharge = view.findViewById(R.id.btn_tab_surcharge);
        btnMoviePrice = view.findViewById(R.id.btn_tab_movie_price);

        tvFriday = view.findViewById(R.id.tvFriday);
        tvSaturday = view.findViewById(R.id.tvSaturday);
        tvSunday = view.findViewById(R.id.tvSunday);
        btnEditFriday = view.findViewById(R.id.btnEditFriday);
        btnEditSaturday = view.findViewById(R.id.btnEditSaturday);
        btnEditSunday = view.findViewById(R.id.btnEditSunday);

        tvHotPricing = view.findViewById(R.id.tvHotMovie);
        tvPeakPricing = view.findViewById(R.id.tvPeakHour);
        btnEditHot = view.findViewById(R.id.btnEditHot);
        btnEditPeak = view.findViewById(R.id.btnEditPeak);

        layoutPeakHours = view.findViewById(R.id.layout_peak_hours);

        btnSurcharge.setOnClickListener(v -> {
            layoutSurcharge.setVisibility(View.VISIBLE);
            recyclerMoviePrice.setVisibility(View.GONE);
            highlightTab(true);
        });

        btnMoviePrice.setOnClickListener(v -> {
            layoutSurcharge.setVisibility(View.GONE);
            recyclerMoviePrice.setVisibility(View.VISIBLE);
            highlightTab(false);
        });

        btnEditFriday.setOnClickListener(v -> showEditDialog("Phụ thu Thứ 6", tvFriday, "weekendPricing.Friday"));
        btnEditSaturday.setOnClickListener(v -> showEditDialog("Phụ thu Thứ 7", tvSaturday, "weekendPricing.Saturday"));
        btnEditSunday.setOnClickListener(v -> showEditDialog("Phụ thu Chủ nhật", tvSunday, "weekendPricing.Sunday"));

        btnEditHot.setOnClickListener(v -> showEditDialog("Phụ thu phim hot", tvHotPricing, "hotMovieIncrease"));
        btnEditPeak.setOnClickListener(v -> showPeakHourEditDialog());

        btnEditPeakPercent = view.findViewById(R.id.btnEditPeakPercent);
        btnEditPeakPercent.setOnClickListener(v ->
                showEditDialog("Phụ thu giờ cao điểm", tvPeakPricing, "peakHourIncrease")
        );

        layoutSurcharge.setVisibility(View.VISIBLE);
        recyclerMoviePrice.setVisibility(View.GONE);
        highlightTab(true);

        loadPriceRules();
        loadMoviePrices();
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();

        });

    }

    private void highlightTab(boolean isSurcharge) {
        btnSurcharge.setBackgroundTintList(getResources().getColorStateList(isSurcharge ? R.color.orange : R.color.gray, null));
        btnSurcharge.setTextColor(getResources().getColor(isSurcharge ? android.R.color.white : android.R.color.black, null));

        btnMoviePrice.setBackgroundTintList(getResources().getColorStateList(!isSurcharge ? R.color.orange : R.color.gray, null));
        btnMoviePrice.setTextColor(getResources().getColor(!isSurcharge ? android.R.color.white : android.R.color.black, null));
    }

    private void loadPriceRules() {
        FirebaseFirestore.getInstance()
                .collection("price_rules")
                .document(DOC_ID)
                .get()
                .addOnSuccessListener(this::displaySurchargeRules);
    }

    private void displaySurchargeRules(DocumentSnapshot doc) {
        Map<String, Long> weekendMap = (Map<String, Long>) doc.get("weekendPricing");
        if (weekendMap != null) {
            if (weekendMap.get("Friday") != null)
                tvFriday.setText("• Friday: +" + weekendMap.get("Friday") + "%");
            if (weekendMap.get("Saturday") != null)
                tvSaturday.setText("• Saturday: +" + weekendMap.get("Saturday") + "%");
            if (weekendMap.get("Sunday") != null)
                tvSunday.setText("• Sunday: +" + weekendMap.get("Sunday") + "%");
        }

        Long hot = doc.getLong("hotMovieIncrease");
        if (hot != null) tvHotPricing.setText("+" + hot + "%");

        Long peak = doc.getLong("peakHourIncrease");
        if (peak != null) tvPeakPricing.setText("+" + peak + "%");

        List<String> peakHours = (List<String>) doc.get("peakHours");
        if (peakHours != null) {
            layoutPeakHours.removeAllViews();
            for (String hour : peakHours) {
                TextView tv = new TextView(getContext());
                tv.setText(hour);
                tv.setPadding(24, 16, 24, 16);
                tv.setTextSize(14f);
                tv.setBackgroundResource(R.drawable.custom_edittext); // bạn có thể tạo 1 drawable bo góc
                layoutPeakHours.addView(tv);
            }
        }
    }

    private void showEditDialog(String title, TextView targetTextView, String firestoreField) {
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(targetTextView.getText().toString().replaceAll("[^\\d]", ""));

        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) return;
                    FirebaseFirestore.getInstance()
                            .collection("price_rules")
                            .document(DOC_ID)
                            .update(firestoreField, Long.parseLong(value))
                            .addOnSuccessListener(unused -> {
                                targetTextView.setText("+" + value + "%");
                                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showPeakHourEditDialog() {
        EditText input = new EditText(getContext());
        input.setHint("Nhập giờ cách nhau bằng dấu phẩy (VD: 18:30,19:00)");
        input.setTextColor(getResources().getColor(android.R.color.black));
        input.setTextSize(14f);

        new AlertDialog.Builder(getContext())
                .setTitle("Sửa giờ cao điểm")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String raw = input.getText().toString().trim();
                    if (!raw.isEmpty()) {
                        List<String> list = new ArrayList<>();
                        for (String s : raw.split(",")) {
                            list.add(s.trim());
                        }
                        FirebaseFirestore.getInstance()
                                .collection("price_rules")
                                .document(DOC_ID)
                                .update("peakHours", list)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(), "Đã cập nhật giờ cao điểm", Toast.LENGTH_SHORT).show();
                                    loadPriceRules(); // reload UI
                                });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadMoviePrices() {
        FirebaseFirestore.getInstance()
                .collection("movies")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Movie> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        Movie m = doc.toObject(Movie.class);
                        if (m != null) {
                            m.setMovieId(doc.getId()); // ✅ Gán movieId từ Firestore document ID
                            list.add(m);
                        }
                    }
                    MoviePriceAdapter adapter = new MoviePriceAdapter(getContext(), list);
                    recyclerMoviePrice.setAdapter(adapter);
                });
    }

}
