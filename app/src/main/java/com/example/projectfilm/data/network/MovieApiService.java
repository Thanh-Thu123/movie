package com.example.projectfilm.data.network;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.example.projectfilm.data.model.MovieResponse;

public interface  MovieApiService {

        @GET("movie/now_playing")
        Call<MovieResponse> getNowPlayingMovies(
                @Query("api_key") String apiKey,
                @Query("language") String language,
                @Query("page") int page
        );
}
