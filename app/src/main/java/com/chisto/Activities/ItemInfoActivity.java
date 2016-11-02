package com.chisto.Activities;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chisto.Adapters.EditTreatmentsAdapter;
import com.chisto.Base.BaseActivity;
import com.chisto.Custom.RecyclerListView;
import com.chisto.Model.Order;
import com.chisto.R;
import com.chisto.Utils.AndroidUtilities;
import com.chisto.Utils.OrderList;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class ItemInfoActivity extends BaseActivity {

    private EditTreatmentsAdapter adapter;
    private RecyclerListView view;

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

        setInfo(order);
        setOnClickListeners(order);
    }

    private void initRecyclerView() {
        view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        view.setEmptyView(null);

        adapter = new EditTreatmentsAdapter(this);
        view.setAdapter(adapter);
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
    }

    private void setInfo(final Order order) {
        findViewById(R.id.appbar).setBackgroundColor(getIntent().getIntExtra(EXTRA_COLOR, DEFAULT_COLOR));
        AndroidUtilities.INSTANCE.colorAndroidBar(this, getIntent().getIntExtra(EXTRA_COLOR, DEFAULT_COLOR));

        if (order != null) {
            ((TextView) findViewById(R.id.title)).setText(order.getCategory().getName());
            ((TextView) findViewById(R.id.textView12)).setText(order.getCategory().getDesc());
            ((TextView) findViewById(R.id.textView)).setText(Integer.toString(order.getCount()));
        }
    }

    private void setOnClickListeners(final Order order) {
        findViewById(R.id.drawer_indicator).setOnClickListener(new View.OnClickListener() {
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
                }
                count.setText(Integer.toString(Integer.parseInt(count.getText().toString()) - 1));
            }
        });

        findViewById(R.id.cont_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        startActivity(intent);
    }

    private void showDialog() {
        new MaterialDialog.Builder(ItemInfoActivity.this)
                .title(R.string.app_name)
                .content(R.string.delete_from_order_str)
                .positiveText(R.string.yes_code_str)
                .negativeText(R.string.no_code_str)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        OrderList.removeCurrent();
                        finish();
                    }
                })
                .show();
    }

    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                xMark = ContextCompat.getDrawable(ItemInfoActivity.this, R.drawable.ic_clear_24dp);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) ItemInfoActivity.this.getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                EditTreatmentsAdapter testAdapter = (EditTreatmentsAdapter) recyclerView.getAdapter();
                if (testAdapter.getUndoOn() && testAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                EditTreatmentsAdapter adapter = (EditTreatmentsAdapter) view.getAdapter();
                boolean undoOn = adapter.getUndoOn();
                if (undoOn) {
                    adapter.pendingRemoval(swipedPosition);
                } else {
                    adapter.remove(swipedPosition);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                if (viewHolder.getAdapterPosition() == -1) {
                    return;
                }

                if (!initiated) {
                    init();
                }

                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(view);
    }

    private void setUpAnimationDecoratorHelper() {
        view.addItemDecoration(new RecyclerView.ItemDecoration() {

            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                if (parent.getItemAnimator().isRunning()) {
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    int left = 0;
                    int right = parent.getWidth();

                    int top = 0;
                    int bottom = 0;

                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onResume() {
        super.onResume();
        if (OrderList.getTreatments() != null) {
            adapter.setCollection(OrderList.getTreatments());
            adapter.notifyDataSetChanged();
        }
    }
}
