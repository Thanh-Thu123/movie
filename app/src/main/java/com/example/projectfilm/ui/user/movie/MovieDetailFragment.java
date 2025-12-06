package com.example.projectfilm.ui.user.movie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.projectfilm.R;
import com.example.projectfilm.ui.user.booking.Ticketbooking;
import com.example.projectfilm.data.model.Movie;

public class MovieDetailFragment extends Fragment {

    private static final String ARG_MOVIE = "movie";
    private Movie movie;

    // Tạo fragment với movie truyền vào
    public static MovieDetailFragment newInstance(Movie movie) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movie = (Movie) getArguments().getSerializable(ARG_MOVIE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView imageBanner = view.findViewById(R.id.imageBanner);
        TextView textTitle = view.findViewById(R.id.textTitle);
        TextView textDescription = view.findViewById(R.id.textDescription);
        TextView textGener = view.findViewById(R.id.textGener);
        TextView textStatus = view.findViewById(R.id.textStatus);
        TextView textActors = view.findViewById(R.id.textActors);
        TextView textCountry = view.findViewById(R.id.textCountry);
        TextView textDirector = view.findViewById(R.id.textDirector);
        TextView textProducer = view.findViewById(R.id.textProducer);
        TextView textYear = view.findViewById(R.id.textYear);
        Button btnBookTicket = view.findViewById(R.id.btnBookTicket);

        if (movie != null) {
            textTitle.setText(movie.getTitle());
            textDescription.setText(movie.getDescription());
            textGener.setText("Thể loại: " + movie.getGenre());
            textStatus.setText("Trạng thái: " + movie.getStatus());

            textActors.setText("Diễn viên: " + movie.getActors());
            textCountry.setText("Quốc gia: " + movie.getCountry());
            textDirector.setText("Đạo diễn: " + movie.getDirector());
            textProducer.setText("Nhà sản xuất: " + movie.getProducer());
            textYear.setText("Năm: " + movie.getYear());

            // ✅ Load ảnh từ Base64 thay vì Glide
            String posterBase64 = movie.getPosterBase64();
            if (posterBase64 != null && !posterBase64.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(posterBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    imageBanner.setImageBitmap(bitmap);
                } catch (Exception e) {
                    imageBanner.setImageResource(R.drawable.placeholder);
                }
            } else {
                imageBanner.setImageResource(R.drawable.placeholder);
            }
        }
        Button btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Sự kiện khi bấm "Đặt vé"
        btnBookTicket.setOnClickListener(v -> {
            if (movie != null) {
                Intent intent = new Intent(requireContext(), Ticketbooking.class);
                intent.putExtra("movie", movie);
                startActivity(intent);
            }
        });
    }
}
