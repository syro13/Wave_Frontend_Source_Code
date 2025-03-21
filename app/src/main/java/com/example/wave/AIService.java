package com.example.wave;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AIService {
    @POST("run-prompt")
    Call<AIResponse> getAIResponse(@Body AIPromptRequest request);
}
