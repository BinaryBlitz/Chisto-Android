package com.chisto.Server;

import com.google.gson.JsonArray;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiEndpoints {

    @GET("cities")
    Call<JsonArray> getCitiesList();

    @GET("categories")
    Call<JsonArray> getCategories();

    @GET("categories/{id}/items")
    Call<JsonArray> getItems(@Path("id") int id);
}
