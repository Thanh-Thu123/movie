package com.example.projectfilm.data.model;

import java.util.List;

public class MovieResponse {
    private int page;
    private List<Movie> results;
    private int total_pages;
    private int total_results;

    // Getter & Setter
    public int getPage() {
        return page;
    }

    public List<Movie> getResults() {
        return results;
    }

    public int getTotalPages() {
        return total_pages;
    }

    public int getTotalResults() {
        return total_results;
    }
}
