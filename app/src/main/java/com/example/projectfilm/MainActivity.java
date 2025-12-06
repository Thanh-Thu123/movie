package com.example.projectfilm;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.projectfilm.ui.user.home.HomeFragment;
import com.example.projectfilm.ui.user.movie.MovieListFragment;
import com.example.projectfilm.ui.user.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.example.projectfilm.ui.auth.LoginActivity;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // đảm bảo activity_main có DrawerLayout

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view); // từ layout
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Gắn toggle để mở/đóng drawer (nếu dùng icon menu)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Gắn listener cho NavigationView (drawer menu)
        navigationView.setNavigationItemSelectedListener(this);

        // Mở HomeFragment mặc định
        if (savedInstanceState == null) {
            openFragment(new HomeFragment());
        }

        // Xử lý BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                openFragment(new HomeFragment());
                return true;
            } else if (id == R.id.menu_favorite) {
                openFragment(new MovieListFragment()); // có thể là danh sách tất cả phim
                return true;
            } else if (id == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            startActivity(new Intent(this, LoginActivity.class)); // Hoặc FirebaseAuth.getInstance().signOut();
        } else if (id == R.id.nav_now_showing) {
            openFragment(MovieListFragment.newInstance("status", "Đang chiếu"));
        } else if (id == R.id.nav_coming_soon) {
            openFragment(MovieListFragment.newInstance("status", "Sắp chiếu"));
        } else if (id == R.id.nav_horror) {
            openFragment(MovieListFragment.newInstance("genre", "Kinh dị"));
        } else if (id == R.id.nav_action) {
            openFragment(MovieListFragment.newInstance("genre", "Hành động"));
        } else if (id == R.id.nav_animation) {
            openFragment(MovieListFragment.newInstance("genre", "Hoạt hình"));
        }
        Fragment selectedFragment = null;


        if (selectedFragment != null) {
            openFragment(selectedFragment);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
