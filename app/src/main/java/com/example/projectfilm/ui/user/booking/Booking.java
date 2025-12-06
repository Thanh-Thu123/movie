package com.example.projectfilm.ui.user.booking;

public class Booking {
    private String bookingId;
    private String cinema, email, name, paymentMethod, seats, time;
    private Object price;
    private Object userId;
    private long timestamp;
    private String movieTitle;
    private String movieId;
    private String date;

    public Booking() {}

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getCinema() { return cinema; }
    public void setCinema(String cinema) { this.cinema = cinema; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Object getPrice() {
        return price;
    }

    public void setPrice(Object price) {
        this.price = price;
    }

    public String getSeats() { return seats; }
    public void setSeats(String seats) { this.seats = seats; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getUserId() {
        if (userId instanceof Long) {
            return String.valueOf(userId);
        } else if (userId instanceof String) {
            return (String) userId;
        } else {
            return null;
        }
    }

    public void setUserId(Object userId) {
        this.userId = userId;
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getPriceAsString() {
        if (price instanceof Long || price instanceof Double || price instanceof Integer) {
            return String.valueOf(price);
        } else if (price instanceof String) {
            return (String) price;
        } else {
            return "";
        }
    }
}