package ru.binaryblitz.Chisto.Activities;

import com.google.gson.JsonObject;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.DeviceInfoStore;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Utils.AndroidUtilities;
import ru.binaryblitz.Chisto.Utils.Image;
import ru.binaryblitz.Chisto.Utils.LogUtil;

public class MyOrderActivity extends BaseActivity {

    private static final String EXTRA_ID = "id";
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_my_order);
        Image.init(this);

        initSwipeRefresh();
        setOnClickListeners();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                layout.setRefreshing(true);
                load();
            }
        });
    }

    private void initSwipeRefresh() {
        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(null);
        layout.setEnabled(false);
        layout.setColorSchemeResources(R.color.colorAccent);
    }

    private void setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.phone_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndroidUtilities.INSTANCE.call(MyOrderActivity.this, "+74957667849");
            }
        });
    }

    private void load() {
        ServerApi.get(this).api().getOrder(getIntent().getIntExtra(EXTRA_ID, 1), DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                layout.setRefreshing(false);
                if (response.isSuccessful()) parseAnswer(response.body());
                else onServerError(response);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                layout.setRefreshing(false);
                onInternetConnectionError();
            }
        });
    }

    private void parseAnswer(JsonObject object) {
        LogUtil.logError(object.toString());
        String status = AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("status"));
        processStatus(status);
        setLaundryInfo(object.get("laundry").getAsJsonObject());
        ((TextView) findViewById(R.id.date_text_view)).setText(getString(R.string.my_order_code) + AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")));
        ((TextView) findViewById(R.id.number)).setText("№ " + AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")));
        ((TextView) findViewById(R.id.date_text)).setText(getDateFromJson(object));
    }

    private void setLaundryInfo(JsonObject object) {
        ((TextView) findViewById(R.id.name)).setText(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("name")));
        ((TextView) findViewById(R.id.description)).setText(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("description")));
        Image.loadPhoto(
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("logo_url")),
                (ImageView) findViewById(R.id.category_icon)
        );
    }

    private void processStatus(String status) {
        int icon;
        int text;
        int textColor;

        switch (status) {
            case "processing":
                icon = R.drawable.process_indicator;
                textColor = ContextCompat.getColor(this, R.color.processColor);
                text = R.string.ready_code;
                break;
            case "completed":
                icon = R.drawable.completed_indicator;
                textColor = ContextCompat.getColor(this, R.color.completedColor);
                text = R.string.process_code;
                break;
            default:
                icon = R.drawable.canceled_indicator;
                textColor = ContextCompat.getColor(this, R.color.canceledColor);
                text = R.string.canceled_code;
                break;
        }

        ((ImageView) findViewById(R.id.order_icon)).setImageResource(icon);
        ((TextView) findViewById(R.id.status_text)).setTextColor(textColor);
        ((TextView) findViewById(R.id.status_text)).setText(text);
    }

    private String getDateFromJson(JsonObject object) {
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = format.parse(object.get("created_at").getAsString());
        } catch (Exception e) {
            LogUtil.logException(e);
        }

        SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }
}
