package com.example.projectfilm.ui.admin.booking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfilm.R;
import com.example.projectfilm.ui.user.booking.Booking;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class OrderManage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etSearch;
    private ImageButton btnSearch;
    private ImageView btnBack;

    private final List<Booking> bookingList = new ArrayList<>();
    private final List<Booking> filteredList = new ArrayList<>();
    private final Map<String, String> userMap = new HashMap<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BookingAdapterInline adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_manage);

        recyclerView = findViewById(R.id.rvBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        etSearch = findViewById(R.id.etSearchBooking);
        btnSearch = findViewById(R.id.btnSearchBooking);
        btnBack = findViewById(R.id.btn_back);

        // Xử lý nút quay lại
        btnBack.setOnClickListener(v -> finish());

        btnSearch.setOnClickListener(v -> filterBookings(etSearch.getText().toString()));

        loadBookings();
    }

    private void loadBookings() {
        db.collection("bookings").get().addOnSuccessListener(querySnapshots -> {
            bookingList.clear();
            for (DocumentSnapshot doc : querySnapshots) {
                Booking booking = doc.toObject(Booking.class);
                if (booking != null) {
                    booking.setBookingId(doc.getId());
                    bookingList.add(booking);
                }
            }
            loadUsers();
        });
    }

    private void loadUsers() {
        db.collection("users").get().addOnSuccessListener(querySnapshots -> {
            userMap.clear();
            for (DocumentSnapshot doc : querySnapshots) {
                String userId = doc.getId();
                String name = doc.getString("name"); // lấy field "name"
                if (name != null) {
                    userMap.put(userId, name);
                }
            }

            filteredList.clear();
            filteredList.addAll(bookingList);

            adapter = new BookingAdapterInline(this, filteredList, userMap);
            recyclerView.setAdapter(adapter);
        });
    }

    private void filterBookings(String keyword) {
        filteredList.clear();

        if (TextUtils.isEmpty(keyword)) {
            filteredList.addAll(bookingList);
        } else {
            String search = normalize(keyword.toLowerCase());

            for (Booking booking : bookingList) {
                String bookingId = normalize(booking.getBookingId().toLowerCase());
                String name = normalize((userMap.get(booking.getUserId()) != null ? userMap.get(booking.getUserId()) : "").toLowerCase());

                if (bookingId.contains(search) || name.contains(search)) {
                    filteredList.add(booking);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private String normalize(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    // ✅ Adapter hiển thị danh sách đơn đặt
    public static class BookingAdapterInline extends RecyclerView.Adapter<BookingAdapterInline.ViewHolder> {
        private final Context context;
        private final List<Booking> bookings;
        private final Map<String, String> userMap;

        public BookingAdapterInline(Context context, List<Booking> bookings, Map<String, String> userMap) {
            this.context = context;
            this.bookings = bookings;
            this.userMap = userMap;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvBookingId, tvUserName;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvBookingId = itemView.findViewById(R.id.ID_booking);
                tvUserName = itemView.findViewById(R.id.tv_username);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking booking = bookings.get(position);

            holder.tvBookingId.setText("Mã đơn: " + booking.getBookingId());

            String username = userMap.get(booking.getUserId());
            holder.tvUserName.setText("Người đặt: " + (username != null ? username : "Không rõ"));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailBooking.class);
                intent.putExtra("bookingId", booking.getBookingId());
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return bookings.size();
        }
    }
}
