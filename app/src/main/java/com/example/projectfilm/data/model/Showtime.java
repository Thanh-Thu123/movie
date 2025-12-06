package com.example.projectfilm.data.model;

public class Showtime {
        private String theater;
        private String time;
        private String title;
        private String status;
        public Showtime() {}

        public String getTheater() { return theater; }
        public void setTheater(String theater) { this.theater = theater; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public String getTitle() { return title; } // ✅ getter
        public void setTitle(String title) { this.title = title; }
}
