package ru.binaryblitz.Chisto.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Adapters.MyOrdersAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.Model.MyOrder;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Utils.AndroidUtilities;

public class MyOrdersActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private MyOrdersAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(ru.binaryblitz.Chisto.R.layout.activity_my_orders);

        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initList();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               load();
            }
        }, 50);
    }

    private void initList() {
        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        view.setEmptyView(null);

        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(this);
        layout.setColorSchemeResources(R.color.colorAccent);

        adapter = new MyOrdersAdapter(this);
        view.setAdapter(adapter);
    }

    private void load() {
        ServerApi.get(this).api().getOrders().enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                layout.setRefreshing(false);
                if (response.isSuccessful())  parseAnswer(response.body());
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                layout.setRefreshing(false);
                onInternetConnectionError();
            }
        });
    }

    private void parseAnswer(JsonArray array) {
        Log.e("qwerty", array.toString());
        ArrayList<MyOrder> collection = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();

            collection.add(new MyOrder(
                    AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")),
                    AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("laundry_id")),
                    AndroidUtilities.INSTANCE.getBooleanFieldFromJson(object.get("paid")),
                    getStatusFromJson(object),
                    getDateFromJson(object),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("house")),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("street")),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("flat")),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("contact_number")),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("notes"))
            ));
        }

        adapter.setCollection(collection);
        adapter.notifyDataSetChanged();
    }

    private MyOrder.Status getStatusFromJson(JsonObject object) {
        return object.get("status").getAsString().equals("processing") ? MyOrder.Status.PROCESS :
                (object.get("status").getAsString().equals("completed") ? MyOrder.Status.COMPLETED : MyOrder.Status.CANCELED);
    }


    private Date getDateFromJson(JsonObject object) {
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = format.parse(object.get("created_at").getAsString());
        } catch (Exception ignored) {}

        return date;
    }

    @Override
    public void onRefresh() {
        load();
    }
}
