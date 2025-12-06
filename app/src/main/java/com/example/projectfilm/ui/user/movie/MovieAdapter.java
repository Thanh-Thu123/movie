package com.example.projectfilm.ui.user.movie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfilm.R;
import com.example.projectfilm.data.model.Movie;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context context;
    private List<Movie> movieList;
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public MovieAdapter(Context context, List<Movie> movieList, OnMovieClickListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.listener = listener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_card, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.titleText.setText(movie.getTitle());
        holder.genreText.setText(movie.getGenre());

        // Load base64 instead of Glide
        String posterBase64 = movie.getPosterBase64();
        if (posterBase64 != null && !posterBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(posterBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.posterImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.posterImage.setImageResource(R.drawable.placeholder);
            }
        } else {
            holder.posterImage.setImageResource(R.drawable.placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;
        TextView titleText, genreText;

        public MovieViewHolder(View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.moviePoster);
            titleText = itemView.findViewById(R.id.movieTitle);
            genreText = itemView.findViewById(R.id.textStatus);
        }
    }
}
