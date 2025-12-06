package com.example.projectfilm.data.model;

import java.util.List;
import java.io.Serializable;

public class Movie implements Serializable {

    private String movieId;         // giữ lại
    private String title;
    private String description;
    private String genre;
    private String status;
    private String posterUrl;       // dùng cho ảnh URL
    private String posterBase64;    // dùng nếu ảnh base64
    private String actors;
    private String country;
    private String director;
    private String producer;
    private String year;
    private String ticketPrice;     // có thể bỏ nếu dùng price int
    private int price;
    private int viewCount;// thêm để map field Firestore
    private List<Showtime> showtimes; // lịch chiếu

    public Movie() {} // Constructor rỗng bắt buộc cho Firestore

    // --- Getter/Setter mới thêm ---

    public String getPosterBase64() {
        return posterBase64;
    }

    public void setPosterBase64(String posterBase64) {
        this.posterBase64 = posterBase64;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    // --- Getter/Setter cũ giữ nguyên ---
    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(String ticketPrice) {
        this.ticketPrice = ticketPrice;
    }
    public int getViewCount() {  // 👈 Getter bạn cần
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }
}
