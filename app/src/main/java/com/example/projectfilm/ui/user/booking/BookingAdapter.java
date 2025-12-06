package com.example.projectfilm.ui.user.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfilm.R;
import com.example.projectfilm.ui.user.booking.Booking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private List<Booking> bookingList;

    public BookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.tvCinema.setText("Rạp: " + booking.getCinema());
        holder.tvSeats.setText("Ghế: " + booking.getSeats());
        holder.tvPrice.setText("Giá: " + booking.getPrice() + "đ");
        holder.tvTime.setText("Giờ: " + booking.getTime());
        holder.tvPayment.setText("Thanh toán: " + booking.getPaymentMethod());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String date = sdf.format(new Date(booking.getTimestamp()));
        holder.tvDate.setText("Ngày: " + date);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCinema, tvSeats, tvPrice, tvTime, tvPayment, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCinema = itemView.findViewById(R.id.tvCinema);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPayment = itemView.findViewById(R.id.tvPaymentMethod);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
