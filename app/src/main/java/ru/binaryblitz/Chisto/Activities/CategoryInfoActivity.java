package ru.binaryblitz.Chisto.Activities;

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

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Adapters.CategoryItemsAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.Model.CategoryItem;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Server.ServerConfig;
import ru.binaryblitz.Chisto.Utils.AndroidUtilities;

public class CategoryInfoActivity extends BaseActivity{
    private CategoryItemsAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_category_info);

        findViewById(ru.binaryblitz.Chisto.R.id.toolbar).setBackgroundColor(getIntent().getIntExtra("color", Color.parseColor("#212121")));
        AndroidUtilities.INSTANCE.colorAndroidBar(this, getIntent().getIntExtra("color", Color.parseColor("#212121")));

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
        RecyclerListView view = (RecyclerListView) findViewById(ru.binaryblitz.Chisto.R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        adapter = new CategoryItemsAdapter(this);
        view.setAdapter(adapter);

        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(null);
        layout.setEnabled(false);
        layout.setColorSchemeResources(R.color.colorAccent);

        adapter.setColor(getIntent().getIntExtra("color", Color.parseColor("#212121")));
    }

    private void load() {
        ServerApi.get(this).api().getItems(getIntent().getIntExtra("id", 0)).enqueue(new Callback<JsonArray>() {
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
        ArrayList<CategoryItem> collection = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            collection.add(new CategoryItem(
                    AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")),
                    ServerConfig.INSTANCE.getImageUrl() + AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("icon_url")),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("name")),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("description"))
            ));
        }

        adapter.setCategories(collection);
        adapter.notifyDataSetChanged();
    }
}
