package com.example.projectfilm.ui.admin.dashboardFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;

import com.example.projectfilm.R;
import com.example.projectfilm.ui.admin.booking.OrderManage;
import com.example.projectfilm.ui.admin.movie.MovieManage;
import com.example.projectfilm.ui.auth.AdminLoginActivity;
import com.example.projectfilm.ui.admin.movie.PriceManageFragment;
import com.example.projectfilm.ui.admin.user.UserManageFragment;



public class DashboardFragment extends Fragment {
    private Button btnLogout;

    private Button btnManageMovies, btnManageUsers, btnManageBooking, btnManagePrice;
    ;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize buttons
        btnManageMovies = view.findViewById(R.id.btn_manage_movies);
        btnManageUsers = view.findViewById(R.id.btn_manage_users);
        btnManageBooking = view.findViewById(R.id.btn_manage_booking);
        btnManagePrice = view.findViewById(R.id.btn_manage_price);


        if (btnManageMovies == null) {
            Log.e("DashboardFragment", "btn_manage_movies is NULL! Check admin_dashboard.xml ID.");
        }
        if (btnManageUsers == null) {
            Log.e("DashboardFragment", "btn_manage_users is NULL! Check admin_dashboard.xml ID.");
        } else {
            Log.d("DashboardFragment", "btn_manage_users found successfully.");
        }

        btnManageBooking.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderManage.class);
            startActivity(intent);
        });

        // Navigate to Movie Management screen
        btnManageMovies.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MovieManage.class);
            startActivity(intent);
        });

        // Navigate to User Management screen
        btnManageUsers.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.admin_fragment_container, new UserManageFragment());
            transaction.addToBackStack(null);
            transaction.commit();

        });
        btnManagePrice = view.findViewById(R.id.btn_manage_price); // nhớ thêm dòng này nếu chưa có

        btnManagePrice.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.admin_fragment_container, new PriceManageFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });




        btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            // Xoá phiên người dùng nếu cần (FirebaseAuth.getInstance().signOut();)
            Intent intent = new Intent(getActivity(), AdminLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xoá ngăn xếp để không quay lại bằng nút Back
            startActivity(intent);
        });

    }
}
