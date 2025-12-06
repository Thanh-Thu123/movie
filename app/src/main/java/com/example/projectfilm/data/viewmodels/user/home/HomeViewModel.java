package com.example.projectfilm.data.viewmodels.user.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Log;

import com.example.projectfilm.data.model.Movie;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Movie>> moviesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> bannerBase64s = new MutableLiveData<>();

    public LiveData<List<Movie>> getMovies() {
        if (moviesLiveData.getValue() == null) {
            loadMoviesFromFirestore();
        }
        return moviesLiveData;
    }

    public LiveData<List<String>> getBannerBase64s() {
        return bannerBase64s;
    }

    private void loadMoviesFromFirestore() {
        FirebaseFirestore.getInstance().collection("movies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Movie> movies = new ArrayList<>();
                    List<String> banners = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Movie movie = doc.toObject(Movie.class);
                        if (movie != null) {
                            movies.add(movie);

                            if (movie.getPosterBase64() != null && !movie.getPosterBase64().isEmpty()) {
                                banners.add(movie.getPosterBase64());
                            }
                        }
                    }

                    moviesLiveData.setValue(movies);
                    bannerBase64s.setValue(banners);
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeVM", "Firestore error: ", e);
                });
    }
}
