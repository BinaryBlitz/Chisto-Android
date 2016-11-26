package ru.binaryblitz.Chisto.Activities;

import com.google.gson.JsonObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.DeviceInfoStore;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Server.ServerConfig;
import ru.binaryblitz.Chisto.Utils.Image;

public class LaundryAndOrderActivity extends BaseActivity {

    private static final String EXTRA_ID = "id";
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_laundry_and_order);

        findViewById(R.id.cont_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean userNotLogged = DeviceInfoStore.getUserObject(LaundryAndOrderActivity.this) == null ||
                        DeviceInfoStore.getUserObject(LaundryAndOrderActivity.this).getPhone().equals("null");
                if (userNotLogged) openActivity(RegistrationActivity.class);
                else openActivity(PersonalInfoActivity.class);
            }
        });

        findViewById(R.id.reviews_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaundryAndOrderActivity.this, ReviewsActivity.class);
                intent.putExtra(EXTRA_ID, getIntent().getIntExtra(EXTRA_ID, 1));
                startActivity(intent);
            }
        });

        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(null);
        layout.setEnabled(false);
        layout.setColorSchemeResources(R.color.colorAccent);

        load();
    }

    private void openActivity(Class<? extends Activity> activity) {
        Intent intent = new Intent(LaundryAndOrderActivity.this, activity);
        startActivity(intent);
    }

    private void load() {
        layout.setRefreshing(true);
        ServerApi.get(LaundryAndOrderActivity.this).api().getLaundry(getIntent().getIntExtra(EXTRA_ID, 1)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                layout.setRefreshing(false);
                if (response.isSuccessful()) parseAnswer(response.body());
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                layout.setRefreshing(false);
                onInternetConnectionError();
            }
        });
    }

    private void parseAnswer(JsonObject object) {
        ((TextView) findViewById(ru.binaryblitz.Chisto.R.id.name_text)).setText(object.get("name").getAsString());
        ((TextView) findViewById(ru.binaryblitz.Chisto.R.id.desc_text)).setText(object.get("description").getAsString());

        Image.loadPhoto(ServerConfig.INSTANCE.getImageUrl() +
                object.get("background_image_url").getAsString(), (ImageView) findViewById(ru.binaryblitz.Chisto.R.id.back_image));
        Image.loadPhoto(ServerConfig.INSTANCE.getImageUrl() +
                object.get("logo_url").getAsString(), (ImageView) findViewById(ru.binaryblitz.Chisto.R.id.logo_image));

        int count = object.get("ratings_count").getAsInt();
        String pluralText = getResources().getQuantityString(R.plurals.review, count, count);
        ((TextView) findViewById(R.id.reviews_btn)).setText(pluralText);

        ((SimpleRatingBar) findViewById(R.id.ratingBar)).setRating(object.get("rating").getAsFloat());
    }
}
