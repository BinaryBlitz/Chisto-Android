package ru.binaryblitz.Chisto.Server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiEndpoints {

    @GET("cities")
    Call<JsonArray> getCitiesList();

    @GET("categories")
    Call<JsonArray> getCategories();

    @GET("cities/{id}/laundries?api_token=foobar")
    Call<JsonArray> getLaundries(@Path("id") int id);

    @GET("categories/{id}/items")
    Call<JsonArray> getItems(@Path("id") int id);

    @GET("items/{id}/treatments")
    Call<JsonArray> getTreatments(@Path("id") int id);

    @GET("laundries/{id}?api_token=foobar")
    Call<JsonObject> getLaundry(@Path("id") int id);
}
