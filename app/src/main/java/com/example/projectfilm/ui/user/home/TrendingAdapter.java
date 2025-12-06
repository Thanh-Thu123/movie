package com.example.projectfilm.ui.user.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfilm.R;
import com.example.projectfilm.data.model.Movie;
import com.example.projectfilm.ui.user.movie.MovieDetailFragment;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
public class TrendingAdapter extends RecyclerView.Adapter<TrendingAdapter.MovieViewHolder> {

    private List<Movie> movieList = new ArrayList<>();
    private List<Movie> originalList = new ArrayList<>();
    private Context context;

    public TrendingAdapter(Context context) {
        this.context = context;
    }

    public void setMovieList(List<Movie> list) {
        this.originalList = list;
        this.movieList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void filter(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            movieList = new ArrayList<>(originalList);
        } else {
            String keywordNormalized = normalizeText(keyword);
            List<Movie> filtered = new ArrayList<>();
            for (Movie movie : originalList) {
                String title = movie.getTitle();
                if (title != null) {
                    String titleNormalized = normalizeText(title);
                    if (titleNormalized.contains(keywordNormalized)) {
                        filtered.add(movie);
                    }
                }
            }
            movieList = filtered;
        }
        notifyDataSetChanged();
    }

    public static String normalizeText(String str) {
        if (str == null) return "";
        String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        temp = temp.replaceAll("đ", "d").replaceAll("Đ", "D");
        return temp.toLowerCase();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView moviePoster;
        TextView movieTitle;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            moviePoster = itemView.findViewById(R.id.moviePoster);
            movieTitle = itemView.findViewById(R.id.movieTitle);
        }
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_card, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        // Load ảnh từ base64
        String posterBase64 = movie.getPosterBase64();
        if (posterBase64 != null && !posterBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(posterBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.moviePoster.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.moviePoster.setImageResource(R.drawable.ic_menu_gallery);
            }
        } else {
            holder.moviePoster.setImageResource(R.drawable.ic_menu_gallery);
        }

        // Set title
        if (holder.movieTitle != null && movie.getTitle() != null) {
            holder.movieTitle.setText(movie.getTitle());
        }

        // Sự kiện click: mở MovieDetailFragment với movie
        holder.itemView.setOnClickListener(v -> {
            MovieDetailFragment fragment = MovieDetailFragment.newInstance(movie); // ✅ truyền Movie

            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }
}
