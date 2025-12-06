package com.example.projectfilm.ui.admin.movie;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfilm.R;
import com.example.projectfilm.data.model.Movie;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MoviePriceAdapter extends RecyclerView.Adapter<MoviePriceAdapter.MovieViewHolder> {

    private List<Movie> movies;
    private Context context;

    public MoviePriceAdapter(Context context, List<Movie> movies) {
        this.movies = movies;
        this.context = context;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvViewCount;
        Button btnEditPrice;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvtitle);
            tvPrice = itemView.findViewById(R.id.tvprice);
            tvViewCount = itemView.findViewById(R.id.tvviewcount);
            btnEditPrice = itemView.findViewById(R.id.btn_edit_price); // bạn cần thêm button này vào layout
        }
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_price, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);

        holder.tvTitle.setText(movie.getTitle());
        holder.tvPrice.setText(movie.getPrice() + " đ");
        holder.tvViewCount.setText("👁 " + movie.getViewCount() + " lượt xem");

        holder.btnEditPrice.setOnClickListener(v -> {
            EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setText(String.valueOf(movie.getPrice()));

            new AlertDialog.Builder(context)
                    .setTitle("Sửa giá cho phim: " + movie.getTitle())
                    .setView(input)
                    .setPositiveButton("Lưu", (dialog, which) -> {
                        String newVal = input.getText().toString().trim();
                        if (!newVal.isEmpty()) {
                            int newPrice = Integer.parseInt(newVal);
                            FirebaseFirestore.getInstance()
                                    .collection("movies")
                                    .document(movie.getMovieId()) // nhớ phải set đúng movieId
                                    .update("price", newPrice)
                                    .addOnSuccessListener(unused -> {
                                        movie.setPrice(newPrice);
                                        notifyItemChanged(position);
                                        Toast.makeText(context, "Đã cập nhật giá", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }
}
