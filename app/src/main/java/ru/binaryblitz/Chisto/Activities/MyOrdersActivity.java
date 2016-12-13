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
import java.util.Collections;
import java.util.Comparator;
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
import ru.binaryblitz.Chisto.Server.DeviceInfoStore;
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

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                layout.setRefreshing(true);
                load();
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
        layout.setOnRefreshListener(this);
        layout.setColorSchemeResources(R.color.colorAccent);

        adapter = new MyOrdersAdapter(this);
        view.setAdapter(adapter);
    }

    private void load() {
        ServerApi.get(this).api().getOrders(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonArray>() {
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
        ArrayList<MyOrder> collection = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();

            collection.add(new MyOrder(object));
        }

        sort(collection);

        adapter.setCollection(collection);
        adapter.notifyDataSetChanged();
    }

    private void sort(ArrayList<MyOrder> collection) {
        Collections.sort(collection, new Comparator<MyOrder>() {
            @Override
            public int compare(MyOrder myOrder, MyOrder t1) {
                return -myOrder.getCreateAt().compareTo(t1.getCreateAt());
            }
        });
    }

    @Override
    public void onRefresh() {
        load();
    }
}
