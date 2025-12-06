package com.example.projectfilm.ui.admin.movie;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.projectfilm.R;

public class MovieManageFragment extends Fragment {

    public MovieManageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_manage_movies, container, false);
    }
}

