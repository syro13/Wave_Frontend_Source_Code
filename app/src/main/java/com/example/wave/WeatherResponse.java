package com.example.wave;

public class WeatherResponse {
    public Weather[] weather;

    public static class Weather {
        public String icon; // Weather icon code (e.g., "01d", "02n")
    }
}

