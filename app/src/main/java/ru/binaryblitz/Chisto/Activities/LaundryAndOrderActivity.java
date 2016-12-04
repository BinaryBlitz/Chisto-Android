package ru.binaryblitz.Chisto.Activities;

import com.google.gson.JsonObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.util.Pair;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Adapters.OrderContentAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.Model.Order;
import ru.binaryblitz.Chisto.Model.Treatment;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.DeviceInfoStore;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Server.ServerConfig;
import ru.binaryblitz.Chisto.Utils.Image;
import ru.binaryblitz.Chisto.Utils.OrderList;

public class LaundryAndOrderActivity extends BaseActivity {

    private static final String EXTRA_ID = "id";
    private SwipeRefreshLayout layout;

    private OrderContentAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_laundry_and_order);

        initElements();
        setOnClickListeners();

        load();
    }

    @SuppressWarnings("ConstantConditions")
    private void setOnClickListeners() {

        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.cont_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean userNotLogged = DeviceInfoStore.getUserObject(LaundryAndOrderActivity.this) == null ||
                        DeviceInfoStore.getUserObject(LaundryAndOrderActivity.this).getPhone().equals("null");
                if (userNotLogged) openActivity(RegistrationActivity.class);
                else openActivity(PersonalInfoActivity.class);
            }
        });

        findViewById(R.id.reviews_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaundryAndOrderActivity.this, ReviewsActivity.class);
                intent.putExtra(EXTRA_ID, getIntent().getIntExtra(EXTRA_ID, 1));
                startActivity(intent);
            }
        });
    }

    private void initElements() {
        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(null);
        layout.setEnabled(false);
        layout.setColorSchemeResources(R.color.colorAccent);

        load();
        initList();
        createOrderListView();
    }

    private void initList() {
        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        view.setEmptyView(null);

        adapter = new OrderContentAdapter(this);
        view.setAdapter(adapter);
    }

    private void createOrderListView() {
        ArrayList<Order> orderList = OrderList.get();
        ArrayList<Pair<String, Object>> listToShow = new ArrayList<>();

        for (int i = 0; i < orderList.size(); i++) {
            Order order = orderList.get(i);

            addHeader(order, listToShow);
            addBasic(order, listToShow);
        }

        adapter.setCollection(listToShow);
        adapter.notifyDataSetChanged();

        setSums();
    }

    private void setSums() {
        ((TextView) findViewById(R.id.cost)).setText(Integer.toString(getAllOrdersCost()) + " \u20bd");
        ((Button) findViewById(R.id.cont_btn)).setText(getString(R.string.create_order_code) +
                Integer.toString(getAllOrdersCost()) + " \u20bd");
    }

    private void addHeader(Order order, ArrayList<Pair<String, Object>> listToShow) {
        int sum = getFillSum(order);

        OrderContentAdapter.Header header = new OrderContentAdapter.Header(
                order.getCategory().getName(),
                sum,
                order.getCount(),
                order.getCategory().getIcon(),
                order.getColor()
        );

        listToShow.add(new Pair<String, Object>("H", header));
    }

    private void addBasic(Order order, ArrayList<Pair<String, Object>> listToShow) {
        for (int j = 0; j < order.getTreatments().size(); j++) {
            Treatment treatment = order.getTreatments().get(j);
            OrderContentAdapter.Basic basic = new OrderContentAdapter.Basic(
                    treatment.getName(),
                    treatment.getCost()
            );

            listToShow.add(new Pair<String, Object>("B", basic));
        }
    }

    private int getFillSum(Order order) {
        int sum = 0;

        if(order.getTreatments() == null) return 0;

        for (int i = 0; i < order.getTreatments().size(); i++) {
            sum += order.getTreatments().get(i).getCost();
        }

        sum *= order.getCount();

        return sum;
    }

    private int getAllOrdersCost() {
        int cost = 0;

        for (int i = 0; i < OrderList.get().size(); i++) {
            cost += getFillSum(OrderList.get(i));
        }

        return cost;
    }

    private void openActivity(Class<? extends Activity> activity) {
        Intent intent = new Intent(LaundryAndOrderActivity.this, activity);
        startActivity(intent);
    }

    private void load() {
        layout.setRefreshing(true);
        ServerApi.get(LaundryAndOrderActivity.this).api().getLaundry(getIntent().getIntExtra(EXTRA_ID, 1)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                layout.setRefreshing(false);
                if (response.isSuccessful()) parseAnswer(response.body());
                else onServerError(response);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                layout.setRefreshing(false);
                onInternetConnectionError();
            }
        });
    }

    private void parseAnswer(JsonObject object) {
        ((TextView) findViewById(ru.binaryblitz.Chisto.R.id.name_text)).setText(object.get("name").getAsString());
        ((TextView) findViewById(ru.binaryblitz.Chisto.R.id.desc_text)).setText(object.get("description").getAsString());

        Image.loadPhoto(ServerConfig.INSTANCE.getImageUrl() +
                object.get("background_image_url").getAsString(), (ImageView) findViewById(ru.binaryblitz.Chisto.R.id.back_image));
        Image.loadPhoto(ServerConfig.INSTANCE.getImageUrl() +
                object.get("logo_url").getAsString(), (ImageView) findViewById(ru.binaryblitz.Chisto.R.id.logo_image));

        int count = object.get("ratings_count").getAsInt();
        String pluralText = getResources().getQuantityString(R.plurals.review, count, count);
        ((TextView) findViewById(R.id.reviews_btn)).setText(pluralText);

        ((SimpleRatingBar) findViewById(R.id.ratingBar)).setRating(object.get("rating").getAsFloat());
    }
}
