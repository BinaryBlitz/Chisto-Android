package ru.binaryblitz.Chisto.Activities;

import com.google.gson.JsonObject;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Model.MyOrder;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Utils.AndroidUtilities;
import ru.binaryblitz.Chisto.Utils.LogUtil;

public class MyOrderActivity extends BaseActivity {

    private static final String EXTRA_ID = "id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_my_order);

        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                load();
            }
        }, 50);
    }

    private void load() {
        ServerApi.get(this).api().getOrder(getIntent().getIntExtra(EXTRA_ID, 1)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) parseAnswer(response.body());
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                onInternetConnectionError();
            }
        });
    }

    private void parseAnswer(JsonObject object) {
        if (AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("status")).equals("processing")) {
            ((ImageView) findViewById(R.id.order_icon)).setImageResource(R.drawable.process_indicator);
            ((TextView) findViewById(R.id.status_text)).setTextColor(ContextCompat.getColor(this, R.color.processColor));
            ((TextView) findViewById(R.id.status_text)).setText(R.string.ready_code_str);
        } else if (AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("status")).equals("completed")) {
            ((ImageView) findViewById(R.id.order_icon)).setImageResource(R.drawable.completed_indicator);
            ((TextView) findViewById(R.id.status_text)).setTextColor(ContextCompat.getColor(this, R.color.completedColor));
            ((TextView) findViewById(R.id.status_text)).setText(R.string.process_code_str);
        } else {
            ((ImageView) findViewById(R.id.order_icon)).setImageResource(R.drawable.canceled_indicator);
            ((TextView) findViewById(R.id.status_text)).setTextColor(ContextCompat.getColor(this, R.color.canceledColor));
            ((TextView) findViewById(R.id.status_text)).setText(R.string.canceled_code_str);
        }

        ((TextView) findViewById(R.id.date_text_view)).setText(getString(R.string.my_order_code_str) + AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")));
        ((TextView) findViewById(R.id.number)).setText("№ " + AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")));
        ((TextView) findViewById(R.id.date_text)).setText(getDateFromJson(object));
    }

    private String getDateFromJson(JsonObject object) {
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = format.parse(object.get("created_at").getAsString());
        } catch (Exception ignored) {
        }

        SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }
}
