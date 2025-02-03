package com.example.wave;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WeatherResponseTests {

    // Tests weather icon getter
    @Test
    public void testWeatherIconGetter() {
        // Arrange
        WeatherResponse.Weather weather = new WeatherResponse.Weather();
        weather.icon = "02n";

        // Act
        String resultIcon = weather.icon;

        // Assert
        assertEquals("02n", resultIcon);
    }

    // Tests empty weather array
    @Test
    public void testEmptyWeatherArray() {
        // Arrange
        WeatherResponse weatherResponse = new WeatherResponse();
        weatherResponse.weather = new WeatherResponse.Weather[0];

        // Assert
        assertNotNull(weatherResponse.weather);
        assertEquals(0, weatherResponse.weather.length);
    }
}
