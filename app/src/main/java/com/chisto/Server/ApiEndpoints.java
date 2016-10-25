package com.chisto.Server;

import com.google.gson.JsonArray;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiEndpoints {

    @GET("cities")
    Call<JsonArray> getCitiesList();

    @GET("categories")
    Call<JsonArray> getCategories();
}
