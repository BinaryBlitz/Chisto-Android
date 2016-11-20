package ru.binaryblitz.Chisto.Activities;

import com.google.gson.JsonObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Adapters.OrdersAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Server.ServerConfig;
import ru.binaryblitz.Chisto.Utils.Animations.Animations;
import ru.binaryblitz.Chisto.Utils.Image;
import ru.binaryblitz.Chisto.Utils.OrderList;
import ru.binaryblitz.Chisto.Utils.SwipeItemDecoration;
import ru.binaryblitz.Chisto.Utils.TouchHelper;

public class OrdersActivity extends BaseActivity {

    private OrdersAdapter adapter;

    private TextView contBtn;
    private static boolean dialogOpened = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(ru.binaryblitz.Chisto.R.layout.activity_orders);

        contBtn = (TextView) findViewById(ru.binaryblitz.Chisto.R.id.textView2);

        initRecyclerView();
        setOnClickListeners();
    }

    private void initRecyclerView() {
        RecyclerListView view = (RecyclerListView) findViewById(ru.binaryblitz.Chisto.R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        view.setEmptyView(findViewById(ru.binaryblitz.Chisto.R.id.empty_orders));

        adapter = new OrdersAdapter(this);
        view.setAdapter(adapter);

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new TouchHelper(0, ItemTouchHelper.LEFT, this, view));
        mItemTouchHelper.attachToRecyclerView(view);
        view.addItemDecoration(new SwipeItemDecoration());
    }

    private void setOnClickListeners() {
        findViewById(ru.binaryblitz.Chisto.R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrdersActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        findViewById(ru.binaryblitz.Chisto.R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrdersActivity.this, SelectCategoryActivity.class);
                startActivity(intent);
            }
        });

        findViewById(ru.binaryblitz.Chisto.R.id.add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrdersActivity.this, SelectCategoryActivity.class);
                startActivity(intent);
            }
        });

        findViewById(ru.binaryblitz.Chisto.R.id.dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        dialogOpened = false;
                        Animations.animateRevealHide(findViewById(ru.binaryblitz.Chisto.R.id.dialog));
                    }
                });
            }
        });

        findViewById(ru.binaryblitz.Chisto.R.id.main_dialog_part).setOnClickListener(new View.OnClickListener() {
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
                    Animations.animateRevealHide(findViewById(ru.binaryblitz.Chisto.R.id.dialog));
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
                contBtn.setText(ru.binaryblitz.Chisto.R.string.cont_code_str);
                contBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadLastOrder();
                    }
                });
            } else {
                contBtn.setText(ru.binaryblitz.Chisto.R.string.nothing_selected_code_str);
                contBtn.setEnabled(false);
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
                            Animations.animateRevealShow(findViewById(ru.binaryblitz.Chisto.R.id.dialog), OrdersActivity.this);
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
                Intent intent = new Intent(OrdersActivity.this, LaundriesActivity.class);
                startActivity(intent);
            }
        });
    }
}
