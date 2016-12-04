package ru.binaryblitz.Chisto.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.util.Pair;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Adapters.LaundriesAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.Model.Laundry;
import ru.binaryblitz.Chisto.Model.Order;
import ru.binaryblitz.Chisto.Model.Treatment;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.DeviceInfoStore;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Server.ServerConfig;
import ru.binaryblitz.Chisto.Utils.AndroidUtilities;
import ru.binaryblitz.Chisto.Utils.Animations.Animations;
import ru.binaryblitz.Chisto.Utils.Image;
import ru.binaryblitz.Chisto.Utils.LogUtil;
import ru.binaryblitz.Chisto.Utils.OrderList;

public class LaundriesActivity extends BaseActivity {

    private LaundriesAdapter adapter;
    private static boolean dialogOpened = false;
    private SwipeRefreshLayout layout;
    private static JsonArray array;

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

        findViewById(R.id.order_current_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                loadLastOrder();
            }
        });
    }

    private void initList() {
        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);
        view.setEmptyView(null);

        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(null);
        layout.setEnabled(false);
        layout.setColorSchemeResources(R.color.colorAccent);

        adapter = new LaundriesAdapter(this);
        view.setAdapter(adapter);
    }

    private void showDialog() {
        ArrayList<String> items = new ArrayList<>();
        items.add(getString(R.string.cost_filter));
        items.add(getString(R.string.speed_filter));
        items.add(getString(R.string.rate_filter));

        new MaterialDialog.Builder(this)
                .title(R.string.title)
                .items(items)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        sort(which);
                        return true;
                    }
                })
                .positiveText(R.string.choose)
                .show();
    }

    private void sort(int which) {
        switch (which) {
            case 0:
                adapter.sortByCost();
                break;
            case 1:
                adapter.sortBySpeed();
                break;
            case 2:
                adapter.sortByRating();
                break;
            default:
                adapter.sortByCost();
                break;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void load() {
        ServerApi.get(this).api().getLaundries(DeviceInfoStore.getCityObject(this).getId()).enqueue(new Callback<JsonArray>() {
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
        LogUtil.logError(array.toString());
        LaundriesActivity.array = array;
        ArrayList<Laundry> collection = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            if (!checkTreatments(object)) continue;
            countSums(i);
            collection.add(new Laundry(
                    AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")),
                    ServerConfig.INSTANCE.getImageUrl() + AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("logo_url")),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("name")),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("description")),
                    getTypeFromJson(object),
                    (float) AndroidUtilities.INSTANCE.getDoubleFieldFromJson(object.get("rating")),
                    parseDate(object, "collection_date", "yyyy-MM-dd"),
                    parseDate(object, "delivery_date", "yyyy-MM-dd"),
                    parseDate(object, "delivery_date_opens_at", "HH:mm"),
                    parseDate(object, "delivery_date_closes_at", "HH:mm"),
                    0,
                    getAllOrdersCost()
            ));
        }

        adapter.setCollection(collection);
        adapter.notifyDataSetChanged();
    }

    private int getFillSum(Order order) {
        int sum = 0;

        if (order.getTreatments() == null) return 0;

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

    public static Date parseDate(JsonObject object, String elementName, String pattern) {
        if (object.isJsonNull()) return null;

        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = format.parse(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get(elementName)));
        } catch (Exception e) {
            LogUtil.logException(e);
        }

        return date;
    }

    private boolean checkTreatments(JsonObject object) {
        JsonArray treatments = object.get("laundry_treatments").getAsJsonArray();
        if (treatments.size() == 0) return false;

        ArrayList<Integer> laundryTreatments = fillLaundryTreatments(treatments);
        ArrayList<Treatment> orderTreatments = OrderList.getTreatments();

        return checkTreatmentsAvailability(orderTreatments, laundryTreatments);
    }

    private ArrayList<Integer> fillLaundryTreatments(JsonArray treatments) {
        ArrayList<Integer> laundryTreatments = new ArrayList<>();
        for (int j = 0; j < treatments.size(); j++) {
            JsonObject treatment = treatments.get(j).getAsJsonObject();
            laundryTreatments.add(AndroidUtilities.INSTANCE.getIntFieldFromJson(treatment.get("treatment").getAsJsonObject().get("id")));
        }

        return laundryTreatments;
    }

    private boolean checkTreatmentsAvailability(ArrayList<Treatment> orderTreatments, ArrayList<Integer> laundryTreatments) {
        for (int i = 0; i < orderTreatments.size(); i++) {
            if (orderTreatments.get(i).getId() == 1) continue;
            if (!checkTreatmentAvailability(orderTreatments.get(i), laundryTreatments)) return false;
        }

        return true;
    }

    private boolean checkTreatmentAvailability(Treatment treatment, ArrayList<Integer> laundryTreatments) {
        for (int j = 0; j < laundryTreatments.size(); j++) {
            if (treatment.getId() == laundryTreatments.get(j)) return true;
            if (j == laundryTreatments.size() - 1) return false;
        }

        return true;
    }

    public void countSums(int index) {
        JsonArray treatments = array.get(index).getAsJsonObject().get("laundry_treatments").getAsJsonArray();
        if (treatments.size() == 0) return;

        ArrayList<Pair<Integer, Integer>> laundryTreatments = fillPrices(treatments);
        ArrayList<Treatment> orderTreatments = OrderList.getTreatments();

        fillOrderList(orderTreatments, laundryTreatments);
    }

    private void fillOrderList(ArrayList<Treatment> orderTreatments, ArrayList<Pair<Integer, Integer>> laundryTreatments) {
        for (int i = 0; i < orderTreatments.size(); i++) {
            if (orderTreatments.get(i).getId() == 1) continue;
            setPriceForTreatment(orderTreatments.get(i), laundryTreatments);
        }
    }

    private void setPriceForTreatment(Treatment treatment, ArrayList<Pair<Integer, Integer>> laundryTreatments) {
        for (int j = 0; j < laundryTreatments.size(); j++) {
            if (treatment.getId() == laundryTreatments.get(j).first) {
                OrderList.setCost(treatment.getId(), laundryTreatments.get(j).second);
            }
        }
    }

    private ArrayList<Pair<Integer, Integer>> fillPrices(JsonArray treatments) {
        ArrayList<Pair<Integer, Integer>> laundryTreatments = new ArrayList<>();
        for (int j = 0; j < treatments.size(); j++) {
            JsonObject treatment = treatments.get(j).getAsJsonObject();
            laundryTreatments.add(new Pair<>(
                    AndroidUtilities.INSTANCE.getIntFieldFromJson(treatment.get("treatment").getAsJsonObject().get("id")),
                    AndroidUtilities.INSTANCE.getIntFieldFromJson(treatment.get("price"))));
        }

        return laundryTreatments;
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

        ServerApi.get(LaundriesActivity.this).api().getOrders().enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                dialog.dismiss();
                if (response.isSuccessful()) parseAnswerForPopup(response.body());
                else onServerError(response);
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                dialog.dismiss();
                onInternetConnectionError();
            }
        });
    }

    private void loadLaundry(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.show();

        ServerApi.get(LaundriesActivity.this).api().getLaundry(id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                dialog.dismiss();
                if (response.isSuccessful()) parseAnswer(response.body());
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dialog.dismiss();
                onInternetConnectionError();
            }
        });
    }

    private void parseAnswer(JsonObject object) {
        ((TextView) findViewById(R.id.name_text)).setText(getString(R.string.laundary_code) + object.get("name").getAsString());
        ((TextView) findViewById(R.id.desc_text)).setText(object.get("description").getAsString());
        ((TextView) findViewById(R.id.order_current_btn)).setText(R.string.ordering_code);

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
                            layout.setRefreshing(true);
                            Animations.animateRevealHide(findViewById(ru.binaryblitz.Chisto.R.id.dialog));
                            load();
                        }
                    });
                }
            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                dialogOpened = true;
                Animations.animateRevealShow(findViewById(ru.binaryblitz.Chisto.R.id.dialog), LaundriesActivity.this);
            }
        });
    }

    private void parseAnswerForPopup(JsonArray array) {
        int id = array.get(array.size() - 1).getAsJsonObject().get("laundry_id").getAsInt();
        loadLaundry(id);
    }
}
