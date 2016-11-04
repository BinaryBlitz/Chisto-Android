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
import android.view.View;

import com.chisto.Adapters.CategoryItemsAdapter;
import com.chisto.Base.BaseActivity;
import com.chisto.Custom.RecyclerListView;
import com.chisto.Model.CategoryItem;
import com.chisto.R;
import com.chisto.Server.ServerApi;
import com.chisto.Utils.AndroidUtilities;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryInfoActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private CategoryItemsAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_category_info);

        findViewById(R.id.toolbar).setBackgroundColor(getIntent().getIntExtra("color", Color.parseColor("#212121")));
        AndroidUtilities.INSTANCE.colorAndroidBar(this, getIntent().getIntExtra("color", Color.parseColor("#212121")));

        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        adapter = new CategoryItemsAdapter(this);
        view.setAdapter(adapter);
        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(this);
        layout.setColorSchemeResources(R.color.colorAccent);

        adapter.setColor(getIntent().getIntExtra("color", Color.parseColor("#212121")));

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
        ServerApi.get(this).api().getItems(getIntent().getIntExtra("id", 0)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                layout.setRefreshing(false);
                if (response.isSuccessful()) {
                    parseAnswer(response.body());
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

    private void parseAnswer(JsonArray array) {
        ArrayList<CategoryItem> collection = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            collection.add(new CategoryItem(
                    object.get("id").getAsInt(),
                    object.get("icon").getAsString(),
                    object.get("name").getAsString(),
                    object.get("description").isJsonNull() ? "" : object.get("description").getAsString()
            ));
        }

        adapter.setCategories(collection);
        adapter.notifyDataSetChanged();
    }
}
