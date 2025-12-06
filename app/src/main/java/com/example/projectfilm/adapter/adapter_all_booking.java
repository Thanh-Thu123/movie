package com.example.projectfilm.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfilm.R;
import com.example.projectfilm.ui.admin.booking.DetailBooking;
import com.example.projectfilm.ui.user.booking.Booking;

import java.util.List;
import java.util.Map;

public class adapter_all_booking extends RecyclerView.Adapter<adapter_all_booking.ViewHolder> {

    private final List<Booking> bookingList;
    private final Map<String, String> bookingUserMap;

    public adapter_all_booking(List<Booking> bookingList, Map<String, String> bookingUserMap) {
        this.bookingList = bookingList;
        this.bookingUserMap = bookingUserMap;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView Id_booking, user_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Id_booking = itemView.findViewById(R.id.ID_booking);      // Mã đơn
            user_name = itemView.findViewById(R.id.tv_username);      // Tên người đặt
        }
    }

    @NonNull
    @Override
    public adapter_all_booking.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false); // Layout bạn đang dùng cho mỗi đơn đặt
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull adapter_all_booking.ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // ✅ Hiển thị mã đơn (bookingId)
        holder.Id_booking.setText("Mã đơn: " + booking.getBookingId());

        // ✅ Lấy và hiển thị tên người đặt từ userMap
        String userName = bookingUserMap.get(booking.getUserId());
        holder.user_name.setText("Người đặt: " + (userName != null ? userName : "Không rõ"));

        // ✅ Chuyển sang màn chi tiết khi click
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), DetailBooking.class);
            intent.putExtra("bookingId", booking.getBookingId());
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }
}
