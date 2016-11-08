package com.chisto.Activities;

import com.google.gson.JsonObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chisto.Adapters.OrdersAdapter;
import com.chisto.Base.BaseActivity;
import com.chisto.Custom.RecyclerListView;
import com.chisto.R;
import com.chisto.Server.ServerApi;
import com.chisto.Server.ServerConfig;
import com.chisto.Utils.Animations.Animations;
import com.chisto.Utils.Image;
import com.chisto.Utils.OrderList;
import com.chisto.Utils.SwipeItemDecoration;
import com.chisto.Utils.TouchHelper;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends BaseActivity {

    private OrdersAdapter adapter;

    private TextView contBtn;
    private static boolean dialogOpened = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_orders);

        contBtn = (TextView) findViewById(R.id.textView2);

        initRecyclerView();
        setOnClickListeners();
    }

    private void initRecyclerView() {
        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        view.setEmptyView(findViewById(R.id.empty_orders));

        adapter = new OrdersAdapter(this);
        view.setAdapter(adapter);

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new TouchHelper(0, ItemTouchHelper.LEFT, this, view));
        mItemTouchHelper.attachToRecyclerView(view);
        view.addItemDecoration(new SwipeItemDecoration());
    }

    private void setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrdersActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrdersActivity.this, SelectCategoryActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrdersActivity.this, SelectCategoryActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        dialogOpened = false;
                        Animations.animateRevealHide(findViewById(R.id.dialog));
                    }
                });
            }
        });

        findViewById(R.id.main_dialog_part).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (dialogOpened) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    dialogOpened = false;
                    Animations.animateRevealHide(findViewById(R.id.dialog));
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    @SuppressWarnings("ConstantConditions")
    private void update() {
        if (OrderList.get() != null) {
            adapter.setCollection(OrderList.get());
            adapter.notifyDataSetChanged();

            if (adapter.getItemCount() != 0) {
                contBtn.setText(R.string.cont_code_str);
                contBtn.setBackgroundColor(ContextCompat.getColor(OrdersActivity.this, R.color.colorPrimary));
                contBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadLastOrder();
                    }
                });
            } else {
                contBtn.setText(R.string.nothing_selected_code_str);
                contBtn.setBackgroundColor(ContextCompat.getColor(OrdersActivity.this, R.color.greyColor));
                contBtn.setOnClickListener(null);
            }
        }
    }

    private void loadLastOrder() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.show();

        ServerApi.get(OrdersActivity.this).api().getLaundry(1).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    parseAnswer(response.body());
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            dialogOpened = true;
                            Animations.animateRevealShow(findViewById(R.id.dialog), OrdersActivity.this);
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
        ((TextView) findViewById(R.id.name_text)).setText(getString(R.string.laundary_code_str) + object.get("name").getAsString());
        ((TextView) findViewById(R.id.desc_text)).setText(object.get("description").getAsString());
        ((TextView) findViewById(R.id.order_current_btn)).setText(getString(R.string.order_code_str) + object.get("name").getAsString());

        Image.loadPhoto(ServerConfig.INSTANCE.getImageUrl() +
                object.get("background_image_url").getAsString(), (ImageView) findViewById(R.id.back_image));
        Image.loadPhoto(ServerConfig.INSTANCE.getImageUrl() +
                object.get("logo_url").getAsString(), (ImageView) findViewById(R.id.logo_image));

        findViewById(R.id.cont_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrdersActivity.this, LaundriesActivity.class);
                startActivity(intent);
            }
        });
    }
}
