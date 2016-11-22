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

    @GET("cities/{id}/laundries?api_token=foobar")
    Call<JsonArray> getLaundries(@Path("id") int id);

    @GET("categories/{id}/items")
    Call<JsonArray> getItems(@Path("id") int id);

    @GET("items/{id}/treatments")
    Call<JsonArray> getTreatments(@Path("id") int id);

    @GET("laundries/{id}?api_token=foobar")
    Call<JsonObject> getLaundry(@Path("id") int id);

    @GET("laundries/{id}/ratings?api_token=foobar")
    Call<JsonArray> getReviews(@Path("id") int id);

    @GET("orders/{id}?api_token=foobar")
    Call<JsonObject> getOrder(@Path("id") int id);

    @GET("orders?api_token=foobar")
    Call<JsonArray> getOrders();

    @PATCH("verification_tokens/{id}")
    Call<JsonObject> verifyPhoneNumber(@Path("id") String id,
                                       @Query("code") String code,
                                       @Query("phone_number") String number);

    @POST("verification_tokens")
    Call<JsonObject> authWithPhoneNumber(@Query("phone_number") String number);

    @POST("laundries/{id}/orders?api_token=foobar")
    Call<JsonObject> sendOrder(@Path("id") int id, @Body JsonObject object);
}
