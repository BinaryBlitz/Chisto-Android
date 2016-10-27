package com.chisto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.chisto.Adapters.OrdersAdapter;
import com.chisto.Base.BaseActivity;
import com.chisto.Custom.RecyclerListView;
import com.chisto.R;
import com.chisto.Utils.OrderList;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class OrdersActivity extends BaseActivity {

    private OrdersAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_orders);

        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setEmptyView(findViewById(R.id.empty_orders));

        adapter = new OrdersAdapter(this);
        view.setAdapter(adapter);

        findViewById(R.id.drawer_indicator2).setOnClickListener(new View.OnClickListener() {
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
        adapter.setCollection(OrderList.get());
        adapter.notifyDataSetChanged();
    }


}
