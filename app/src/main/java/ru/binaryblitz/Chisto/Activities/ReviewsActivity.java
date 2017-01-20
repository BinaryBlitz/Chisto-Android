package ru.binaryblitz.Chisto.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Adapters.ReviewsAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.Model.Review;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.DeviceInfoStore;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Server.ServerConfig;
import ru.binaryblitz.Chisto.Utils.AndroidUtilities;
import ru.binaryblitz.Chisto.Utils.DateUtils;
import ru.binaryblitz.Chisto.Utils.Image;

public class ReviewsActivity extends BaseActivity {
    private static final String EXTRA_ID = "id";

    private ReviewsAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_reviews);

        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initList();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                load();
                layout.setRefreshing(true);
                loadReviews();
            }
        });
    }

    private void initList() {
        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        view.setEmptyView(findViewById(R.id.empty));

        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(null);
        layout.setEnabled(false);
        layout.setColorSchemeResources(R.color.colorAccent);

        adapter = new ReviewsAdapter(this);
        view.setAdapter(adapter);
    }

    private void loadReviews() {
        ServerApi.get(this).api().getReviews(getIntent().getIntExtra(EXTRA_ID, 1), DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                layout.setRefreshing(false);
                if (response.isSuccessful()) parseAnswer(response.body());
                else onServerError(response);
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                layout.setRefreshing(false);
                onInternetConnectionError();
            }
        });
    }

    private void parseAnswer(JsonArray array) {
        ArrayList<Review> collection = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();

            collection.add(new Review(
                    AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")),
                    DateUtils.INSTANCE.parse(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("created_at"))),
                    getUserName(object.get("user").getAsJsonObject()),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("content")),
                    (float) AndroidUtilities.INSTANCE.getDoubleFieldFromJson(object.get("value"))
            ));
        }

        adapter.setCollection(collection);
        adapter.notifyDataSetChanged();
    }

    private String getUserName(JsonObject object) {
        String firstName = AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("first_name"));
        String lastName = AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("last_name"));
        return firstName + " " + lastName;
    }

    private void load() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.show();

        ServerApi.get(ReviewsActivity.this).api().getLaundry(getIntent().getIntExtra(EXTRA_ID, 1)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                dialog.dismiss();
                if (response.isSuccessful()) parseAnswer(response.body());
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dialog.dismiss();
                onInternetConnectionError();
            }
        });
    }

    private void parseAnswer(JsonObject object) {
        ((TextView) findViewById(ru.binaryblitz.Chisto.R.id.name_text)).setText(object.get("name").getAsString());

        Image.loadPhoto(ServerConfig.INSTANCE.getImageUrl() +
                object.get("background_image_url").getAsString(), (ImageView) findViewById(ru.binaryblitz.Chisto.R.id.back_image));
        Image.loadPhoto(ServerConfig.INSTANCE.getImageUrl() +
                object.get("logo_url").getAsString(), (ImageView) findViewById(ru.binaryblitz.Chisto.R.id.logo_image));
        int count =  object.get("ratings_count").getAsInt();

        String pluralText = getResources().getQuantityString(R.plurals.review, count, count);
        ((TextView) findViewById(R.id.count)).setText(pluralText);

        ((SimpleRatingBar) findViewById(R.id.ratingBar)).setRating(object.get("rating").getAsFloat());
    }
}