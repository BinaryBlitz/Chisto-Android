package ru.binaryblitz.Chisto.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import ru.binaryblitz.Chisto.Adapters.EditTreatmentsAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.Model.Order;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Utils.AndroidUtilities;
import ru.binaryblitz.Chisto.Utils.OrderList;
import ru.binaryblitz.Chisto.Utils.SwipeItemDecoration;
import ru.binaryblitz.Chisto.Utils.TouchHelper;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class ItemInfoActivity extends BaseActivity {

    private EditTreatmentsAdapter adapter;

    private TextView count;

    public static final String EXTRA_COLOR = "color";
    public static final String EXTRA_INDEX = "index";
    public static final String EXTRA_EDIT = "edit";
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_NAME = "name";

    public static final int DEFAULT_COLOR = Color.parseColor("#212121");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_item_info);

        count = (TextView) findViewById(R.id.textView);

        initRecyclerView();

        final Order order = OrderList.get(getIntent().getIntExtra(EXTRA_INDEX, 0));
        OrderList.copyToBuffer(OrderList.getTreatments());
        setInfo(order);
        setOnClickListeners(order);
    }

    public void onRemovalError() {
        Snackbar.make(findViewById(R.id.main), R.string.removal, Snackbar.LENGTH_SHORT).show();
    }

    private void initRecyclerView() {
        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setEmptyView(null);

        adapter = new EditTreatmentsAdapter(this);
        view.setAdapter(adapter);

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new TouchHelper(0, ItemTouchHelper.LEFT, this, view));
        mItemTouchHelper.attachToRecyclerView(view);
        view.addItemDecoration(new SwipeItemDecoration());
    }

    private void setInfo(final Order order) {
        findViewById(R.id.appbar).setBackgroundColor(getIntent().getIntExtra(EXTRA_COLOR, DEFAULT_COLOR));
        AndroidUtilities.INSTANCE.colorAndroidBar(this, getIntent().getIntExtra(EXTRA_COLOR, DEFAULT_COLOR));

        if (order != null) {
            ((TextView) findViewById(R.id.title)).setText(order.getCategory().getName());
            ((TextView) findViewById(R.id.textView12)).setText(order.getCategory().getDescription());
            ((TextView) findViewById(R.id.textView)).setText(Integer.toString(order.getCount()));
        }
    }

    private void setOnClickListeners(final Order order) {
        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(order);
            }
        });

        findViewById(R.id.plus_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count.setText(Integer.toString(Integer.parseInt(count.getText().toString()) + 1));
            }
        });

        findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Integer.parseInt(count.getText().toString()) == 1) {
                    showDialog();
                    return;
                }
                count.setText(Integer.toString(Integer.parseInt(count.getText().toString()) - 1));
            }
        });

        findViewById(R.id.cont_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter.getItemCount() == 0) return;
                OrderList.saveTreatments(adapter.getCollection());
                OrderList.changeCount(Integer.parseInt(count.getText().toString()));
                finish();
            }
        });
    }

    private void openActivity(final Order order) {
        Intent intent = new Intent(ItemInfoActivity.this, SelectServiceActivity.class);
        intent.putExtra(EXTRA_EDIT, true);
        intent.putExtra(EXTRA_ID, order != null ? order.getCategory().getId() : 0);
        intent.putExtra(EXTRA_NAME, order != null ? order.getCategory().getName() : "");
        intent.putExtra(EXTRA_COLOR, order != null ? order.getColor() : DEFAULT_COLOR);
        startActivity(intent);
    }

    private void showDialog() {
        new MaterialDialog.Builder(ItemInfoActivity.this)
                .title(R.string.app_name)
                .content(R.string.delete_from_order)
                .positiveText(R.string.yes_code)
                .negativeText(R.string.no_code)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        count.setText(Integer.toString(1));
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        OrderList.removeCurrent();
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        count.setText(Integer.toString(1));
                    }
                })
                .show();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onResume() {
        super.onResume();
        if (OrderList.getTreatments() != null) {
            adapter.setCollection(OrderList.getBufferTreatments());
            adapter.notifyDataSetChanged();
        }
    }
}
