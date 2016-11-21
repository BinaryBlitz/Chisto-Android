package ru.binaryblitz.Chisto.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Adapters.LaundriesAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.Model.Laundry;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.DeviceInfoStore;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Server.ServerConfig;
import ru.binaryblitz.Chisto.Utils.AndroidUtilities;
import ru.binaryblitz.Chisto.Utils.Animations.Animations;
import ru.binaryblitz.Chisto.Utils.Image;
import ru.binaryblitz.Chisto.Utils.LogUtil;

public class LaundriesActivity extends BaseActivity {

    private LaundriesAdapter adapter;
    private static boolean dialogOpened = false;

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

        findViewById(ru.binaryblitz.Chisto.R.id.main_dialog_part).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadLastOrder();
            }
        }, 150);
    }

    private void initList() {
        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        view.setEmptyView(null);

        adapter = new LaundriesAdapter(this);
        view.setAdapter(adapter);
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
                if (response.isSuccessful()) {
                    parseAnswer(response.body());
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                onInternetConnectionError();
            }
        });
    }

    private void parseAnswer(JsonArray array) {
        LogUtil.logError(array.toString());
        ArrayList<Laundry> collection = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            collection.add(new Laundry(
                    AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")),
                    ServerConfig.INSTANCE.getImageUrl() + AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("logo_url")),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("name")),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("description")),
                    getTypeFromJson(object)
            ));
        }

        adapter.setCollection(collection);
        adapter.notifyDataSetChanged();
    }

    private Laundry.Type getTypeFromJson(JsonObject object) {
        Laundry.Type type = Laundry.Type.EMPTY;

        String text = AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("category"));
        switch (text) {
            case "premium":
                type = Laundry.Type.PREMIUM;
                break;
            case "economy":
                type = Laundry.Type.ECONOMY;
                break;
            case "fast":
                type = Laundry.Type.FAST;
                break;
        }

        return type;
    }

    private void loadLastOrder() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.show();

        ServerApi.get(LaundriesActivity.this).api().getLaundry(1).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    parseAnswer(response.body());
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            dialogOpened = true;
                            Animations.animateRevealShow(findViewById(ru.binaryblitz.Chisto.R.id.dialog), LaundriesActivity.this);
                        }
                    });
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dialog.dismiss();
                onInternetConnectionError();
            }
        });
    }

    private void parseAnswer(JsonObject object) {
        ((TextView) findViewById(ru.binaryblitz.Chisto.R.id.name_text)).setText(getString(ru.binaryblitz.Chisto.R.string.laundary_code_str) + object.get("name").getAsString());
        ((TextView) findViewById(ru.binaryblitz.Chisto.R.id.desc_text)).setText(object.get("description").getAsString());
        ((TextView) findViewById(ru.binaryblitz.Chisto.R.id.order_current_btn)).setText(R.string.ordering_code_str);

        Image.loadPhoto(ServerConfig.INSTANCE.getImageUrl() +
                object.get("background_image_url").getAsString(), (ImageView) findViewById(ru.binaryblitz.Chisto.R.id.back_image));
        Image.loadPhoto(ServerConfig.INSTANCE.getImageUrl() +
                object.get("logo_url").getAsString(), (ImageView) findViewById(ru.binaryblitz.Chisto.R.id.logo_image));

        findViewById(ru.binaryblitz.Chisto.R.id.cont_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogOpened) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            dialogOpened = false;
                            Animations.animateRevealHide(findViewById(ru.binaryblitz.Chisto.R.id.dialog));
                            load();
                        }
                    });
                }
            }
        });
    }
}
