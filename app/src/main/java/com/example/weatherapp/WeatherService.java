package com.example.weatherapp;
import  retrofit2.http.Query;
public class WeatherService {
    @Headers("Content-Type: application/json")
    @GET("/v1/public/yql?q=select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"pabna, ati\")&format=json")
    Call<WeatherModel> getWeather(@Query("q") String q);


    @Headers("Content-Type: application/json")
    @GET("/v1/public/yql")
    Call<WeatherModel> getWeatherQuery(@Query("q") String q, @Query("format") String format);


}
