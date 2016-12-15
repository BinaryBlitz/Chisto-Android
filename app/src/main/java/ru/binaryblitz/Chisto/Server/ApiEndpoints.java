package ru.binaryblitz.Chisto.Server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiEndpoints {


    @GET("cities")
    Call<JsonArray> getCitiesList();

    @GET("categories")
    Call<JsonArray> getCategories();

    @GET("cities/{id}/laundries")
    Call<JsonArray> getLaundries(@Path("id") int id);

    @GET("categories/{id}/items")
    Call<JsonArray> getItems(@Path("id") int id);

    @GET("items/{id}/treatments")
    Call<JsonArray> getTreatments(@Path("id") int id);

    @GET("laundries/{id}?api_token=foobar")
    Call<JsonObject> getLaundry(@Path("id") int id);

    @GET("laundries/{id}/ratings")
    Call<JsonArray> getReviews(@Path("id") int id, @Query("api_token") String token);

    @GET("orders/{id}")
    Call<JsonObject> getOrder(@Path("id") int id, @Query("api_token") String token);

    @GET("orders")
    Call<JsonArray> getOrders(@Query("api_token") String token);

    @PATCH("verification_token")
    Call<JsonObject> verifyPhoneNumber(@Body JsonObject token);

    @POST("verification_token")
    Call<JsonObject> authWithPhoneNumber(@Body JsonObject number);

    @POST("user")
    Call<JsonObject> createUser(@Body JsonObject number);

    @PATCH("user")
    Call<JsonObject> updateUser(@Body JsonObject number, @Query("api_token") String token);

    @POST("laundries/{id}/orders")
    Call<JsonObject> sendOrder(@Path("id") int id, @Body JsonObject object, @Query("api_token") String token);
}
