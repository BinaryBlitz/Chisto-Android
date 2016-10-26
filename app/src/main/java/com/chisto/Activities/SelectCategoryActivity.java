package com.chisto.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.chisto.Adapters.CategoriesAdapter;
import com.chisto.Adapters.CitiesAdapter;
import com.chisto.Base.BaseActivity;
import com.chisto.Custom.RecyclerListView;
import com.chisto.Model.Category;
import com.chisto.R;
import com.chisto.Server.ServerApi;
import com.chisto.Utils.LogUtil;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectCategoryActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private CategoriesAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_select_category);

        findViewById(R.id.drawer_indicator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        adapter = new CategoriesAdapter(this);
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
        ServerApi.get(this).api().getCategories().enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                LogUtil.logError(response.body().toString());
                layout.setRefreshing(false);
                if(response.isSuccessful()) {
                    ArrayList<Category> collection = new ArrayList<>();
                    JsonArray array = response.body();

                    for (int i = 0; i < array.size(); i++) {
                        JsonObject object = array.get(i).getAsJsonObject();
                        collection.add(new Category(
                                object.get("id").getAsInt(),
                                object.get("name").getAsString(),
                                object.get("description").getAsString(),
                                object.get("icon").getAsString(),
                                Color.parseColor("#212121")
                        ));
                    }

                    adapter.setCategories(collection);
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
