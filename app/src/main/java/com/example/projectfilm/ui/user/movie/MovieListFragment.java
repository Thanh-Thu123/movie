package com.example.projectfilm.ui.user.movie;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;


import com.example.projectfilm.data.model.Movie;
import com.example.projectfilm.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MovieListFragment extends Fragment {

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieList;
    private TextView emptyText;

    private static final String ARG_FILTER_TYPE = "filterType";
    private static final String ARG_FILTER_VALUE = "filterValue";

    public static MovieListFragment newInstance(String filterType, String filterValue) {
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER_TYPE, filterType);
        args.putString(ARG_FILTER_VALUE, filterValue);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewMovies);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        emptyText = view.findViewById(R.id.emptyText);
        movieList = new ArrayList<>();
        adapter = new MovieAdapter(getContext(), movieList, movie -> {
            // Chuyển sang MovieDetailFragment khi bấm vào item
            MovieDetailFragment detailFragment = MovieDetailFragment.newInstance(movie);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment) // Sửa đúng ID container
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            String filterType = getArguments().getString(ARG_FILTER_TYPE);
            String filterValue = getArguments().getString(ARG_FILTER_VALUE);
            if (filterType != null && filterValue != null) {
                loadMovies(filterType, filterValue);
            } else {
                loadAllMovies();
            }
        } else {
            loadAllMovies();
        }
    }

    private void loadMovies(String filterType, String filterValue) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moviesRef = db.collection("movies");

        Query query = moviesRef.whereEqualTo(filterType, filterValue);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                movieList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Movie movie = doc.toObject(Movie.class);
                    movieList.add(movie);
                }
                adapter.notifyDataSetChanged();
                emptyText.setVisibility(movieList.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                Log.e("Firestore", "Error: ", task.getException());
            }
        });
    }

    private void loadAllMovies() {
        FirebaseFirestore.getInstance().collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        movieList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Movie movie = doc.toObject(Movie.class);
                            movieList.add(movie);
                        }
                        adapter.notifyDataSetChanged();
                        emptyText.setVisibility(movieList.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        Log.e("Firestore", "Failed to fetch movies", task.getException());
                    }
                });
    }
}
