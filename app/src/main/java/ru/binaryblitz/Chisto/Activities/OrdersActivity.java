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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import ru.binaryblitz.Chisto.Server.DeviceInfoStore;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Utils.LogUtil;
import ru.binaryblitz.Chisto.Utils.OrderList;
import ru.binaryblitz.Chisto.Utils.SwipeItemDecoration;
import ru.binaryblitz.Chisto.Utils.TouchHelper;

public class OrdersActivity extends BaseActivity {

    private OrdersAdapter adapter;
    private TextView contBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(ru.binaryblitz.Chisto.R.layout.activity_orders);

        contBtn = (TextView) findViewById(ru.binaryblitz.Chisto.R.id.textView2);
        initRecyclerView();
        setOnClickListeners();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getUser();
            }
        });
    }

    private void getUser() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.show();

        ServerApi.get(this).api().getUser(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                LogUtil.logError(response.body().toString());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
            }
        });
    }

    private void initRecyclerView() {
        RecyclerListView view = (RecyclerListView) findViewById(ru.binaryblitz.Chisto.R.id.recyclerView);
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
                contBtn.setText(ru.binaryblitz.Chisto.R.string.cont_code);
                contBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(OrdersActivity.this, LaundriesActivity.class);
                        startActivity(intent);
                    }
                });
            } else {
                contBtn.setText(ru.binaryblitz.Chisto.R.string.nothing_selected_code);
                contBtn.setEnabled(false);
                contBtn.setOnClickListener(null);
            }
        }
    }
}
