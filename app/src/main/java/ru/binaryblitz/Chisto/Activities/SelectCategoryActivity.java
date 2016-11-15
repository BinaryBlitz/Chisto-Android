package ru.binaryblitz.Chisto.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Adapters.CategoriesAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.Model.Category;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Server.ServerConfig;
import ru.binaryblitz.Chisto.Utils.LogUtil;
import ru.binaryblitz.Chisto.Utils.OrderList;

public class SelectCategoryActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private CategoriesAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(ru.binaryblitz.Chisto.R.layout.activity_select_category);

        findViewById(ru.binaryblitz.Chisto.R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderList.removeCurrent();
                finish();
            }
        });

        initList();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                load();
            }
        }, 200);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        OrderList.removeCurrent();
    }

    @Override
    public void onRefresh() {
        load();
    }

    private void initList() {
        RecyclerListView view = (RecyclerListView) findViewById(ru.binaryblitz.Chisto.R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);

        adapter = new CategoriesAdapter(this);
        view.setAdapter(adapter);

        layout = (SwipeRefreshLayout) findViewById(ru.binaryblitz.Chisto.R.id.refresh);
        layout.setOnRefreshListener(this);
        layout.setColorSchemeResources(ru.binaryblitz.Chisto.R.color.colorAccent);
    }

    private void load() {
        ServerApi.get(this).api().getCategories().enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                LogUtil.logError(response.body().toString());
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
        ArrayList<Category> collection = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            collection.add(new Category(
                    object.get("id").getAsInt(),
                    ServerConfig.INSTANCE.getImageUrl() + object.get("icon_url").getAsString(),
                    object.get("name").getAsString(),
                    object.get("description").getAsString(),
                    ContextCompat.getColor(this, ru.binaryblitz.Chisto.R.color.greyColor)
            ));
        }

        adapter.setCategories(collection);
        adapter.notifyDataSetChanged();
    }
}
