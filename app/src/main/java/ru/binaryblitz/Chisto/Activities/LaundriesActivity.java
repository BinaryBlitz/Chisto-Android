package ru.binaryblitz.Chisto.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import ru.binaryblitz.Chisto.Adapters.LaundriesAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.Model.Laundry;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.DeviceInfoStore;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Server.ServerConfig;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LaundriesActivity extends BaseActivity
        implements AppBarLayout.OnOffsetChangedListener, SwipeRefreshLayout.OnRefreshListener {

    private LaundriesAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_laundries);

        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        initList();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                load();
            }
        }, 150);
    }

    @Override
    public void onRefresh() {
        load();
    }

    private void initList() {
        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        view.setEmptyView(null);

        adapter = new LaundriesAdapter(this);
        view.setAdapter(adapter);

        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(this);
        layout.setColorSchemeResources(R.color.colorAccent);
    }

    private void showDialog() {
        ArrayList<String> items = new ArrayList<>();
        items.add(getString(R.string.cost_filter_str));
        items.add(getString(R.string.speed_filter_str));
        items.add(getString(R.string.rate_filter_str));

        new MaterialDialog.Builder(this)
                .title(R.string.title)
                .items(items)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        // TODO
                        return true;
                    }
                })
                .positiveText(R.string.choose)
                .show();
    }

    private void load() {
        ServerApi.get(this).api().getLaundries(DeviceInfoStore.getCityObject(this).getId()).enqueue(new Callback<JsonArray>() {
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

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            layout.setEnabled(true);
        } else {
            layout.setEnabled(false);
        }
    }

    private void parseAnswer(JsonArray array) {
        ArrayList<Laundry> collection = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            collection.add(new Laundry(
                    object.get("id").getAsInt(),
                    ServerConfig.INSTANCE.getImageUrl() + object.get("logo_url").getAsString(),
                    object.get("name").getAsString(),
                    object.get("description").getAsString(),
                    object.get("category").getAsString()
            ));
        }

        adapter.setCollection(collection);
        adapter.notifyDataSetChanged();
    }
}
