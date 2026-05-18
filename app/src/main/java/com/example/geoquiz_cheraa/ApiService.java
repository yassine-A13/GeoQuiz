package com.example.geoquiz_cheraa;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/scores")
    Call<Score> submitScore(@Body Score score);

    @GET("api/leaderboard")
    Call<List<Score>> getLeaderboard();
}