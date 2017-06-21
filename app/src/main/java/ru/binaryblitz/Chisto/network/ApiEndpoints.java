package ru.binaryblitz.Chisto.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.binaryblitz.Chisto.entities.Category;
import ru.binaryblitz.Chisto.entities.CategoryItem;
import ru.binaryblitz.Chisto.entities.Treatment;

public interface ApiEndpoints {
    @GET("cities")
    Call<JsonArray> getCitiesList();

    @GET("categories")
    Observable<List<Category>> getCategories();

    @GET("cities/{id}/laundries")
    Call<JsonArray> getLaundries(@Path("id") int id, @Query("long_treatment") Boolean... longTreatment);

    @GET("categories/{id}/items")
    Observable<List<CategoryItem>> getItems(@Path("id") int id);

    @GET("items")
    Observable<List<CategoryItem>> getAllItems();

    @GET("items/{id}/treatments")
    Observable<List<Treatment>> getTreatments(@Path("id") int id);

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

    @GET("user")
    Call<JsonObject> getUser(@Query("api_token") String token);

    @PATCH("user")
    Call<JsonObject> updateUser(@Body JsonObject number, @Query("api_token") String token);

    @POST("laundries/{id}/orders")
    Call<JsonObject> sendOrder(@Path("id") int id, @Body JsonObject object, @Query("api_token") String token);

    @POST("laundries/{id}/ratings")
    Call<JsonObject> sendReview(@Path("id") int id, @Body JsonObject object, @Query("api_token") String token);

    @PATCH("ratings/{id}")
    Call<JsonObject> updateReview(@Path("id") int id, @Body JsonObject object, @Query("api_token") String token);

    @POST("subscriptions")
    Call<JsonObject> sendSubscription(@Body JsonObject object);

    @GET("promo_codes/{code}")
    Call<JsonObject> getPromoCode(@Path("code") String code, @Query("api_token") String token);
}
