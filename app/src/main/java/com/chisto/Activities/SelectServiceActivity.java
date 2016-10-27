package com.chisto.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.chisto.Adapters.TreatmentsAdapter;
import com.chisto.Base.BaseActivity;
import com.chisto.Custom.RecyclerListView;
import com.chisto.Model.Treatment;
import com.chisto.R;
import com.chisto.Server.ServerApi;
import com.chisto.Utils.AndroidUtilities;
import com.chisto.Utils.LogUtil;
import com.chisto.Utils.OrderList;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectServiceActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private TreatmentsAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_select_service);

        findViewById(R.id.appbar).setBackgroundColor(getIntent().getIntExtra("color", 0));
        AndroidUtilities.INSTANCE.colorAndroidBar(this, getIntent().getIntExtra("color", 0));

        findViewById(R.id.drawer_indicator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.cont_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(adapter.getSelected().size() != 0) {
                    OrderList.addTreatments(adapter.getSelected());
                    Intent intent = new Intent(SelectServiceActivity.this, OrdersActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Snackbar.make(findViewById(R.id.main), R.string.nothing_selected_code_str, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        adapter = new TreatmentsAdapter(this);
        view.setAdapter(adapter);
        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(this);
        layout.setColorSchemeResources(R.color.colorAccent);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                load();
            }
        }, 200);
    }

    @Override
    public void onRefresh() {
        load();
    }

    private void load() {
        ServerApi.get(this).api().getTreatments(getIntent().getIntExtra("id", 0)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                LogUtil.logError(response.body().toString());
                layout.setRefreshing(false);
                if(response.isSuccessful()) {
                    ArrayList<Treatment> collection = new ArrayList<>();
                    JsonArray array = response.body();

                    collection.add(new Treatment(
                            1,
                            "Декор",
                            "Описание",
                            false));

                    for (int i = 0; i < array.size(); i++) {
                        JsonObject object = array.get(i).getAsJsonObject();
                        collection.add(new Treatment(
                                object.get("id").getAsInt(),
                                object.get("name").getAsString(),
                                object.get("description").getAsString(),
                                false));
                    }

                    adapter.setCollection(collection);
                    adapter.notifyDataSetChanged();
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                layout.setRefreshing(false);
                onInternetConnectionError();
            }
        });
    }
}
