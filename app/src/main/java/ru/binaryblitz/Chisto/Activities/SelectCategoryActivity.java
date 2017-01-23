package ru.binaryblitz.Chisto.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Adapters.CategoriesAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.Model.Category;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Server.ServerConfig;
import ru.binaryblitz.Chisto.Utils.AndroidUtilities;
import ru.binaryblitz.Chisto.Utils.ColorsList;
import ru.binaryblitz.Chisto.Utils.OrderList;

public class SelectCategoryActivity extends BaseActivity {
    private CategoriesAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(ru.binaryblitz.Chisto.R.layout.activity_select_category);

        findViewById(ru.binaryblitz.Chisto.R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity();
            }
        });

        initList();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                layout.setRefreshing(true);
                load();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity() {
        OrderList.removeCurrent();
        finish();
    }

    private void initList() {
        RecyclerListView view = (RecyclerListView) findViewById(ru.binaryblitz.Chisto.R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);

        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(null);
        layout.setEnabled(false);
        layout.setColorSchemeResources(R.color.colorAccent);

        adapter = new CategoriesAdapter(this);
        view.setAdapter(adapter);
    }

    private void load() {
        ServerApi.get(this).api().getCategories().enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                layout.setRefreshing(false);
                if (response.isSuccessful()) {
                    parseAnswer(response.body());
                } else {
                    onServerError(response);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                layout.setRefreshing(false);
                onInternetConnectionError();
            }
        });
    }

    private void parseAnswer(JsonArray array) {
        ArrayList<Category> collection = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            if (AndroidUtilities.INSTANCE.getBooleanFieldFromJson(object.get("featured"))) {
                collection.add(0, parseCategory(object));
            } else {
                collection.add(parseCategory(object));
            }
        }

        sort(collection);
        save(collection);

        adapter.setCategories(collection);
        adapter.notifyDataSetChanged();
    }

    private void save(ArrayList<Category> collection) {
        for (int i = 0; i < collection.size(); i++) {
            Category category = collection.get(i);
            ColorsList.add(new Pair<>(category.getId(), category.getColor()));
        }

        ColorsList.saveColors(this);
    }

    private Category parseCategory(JsonObject object) {
        return new Category(
                AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")),
                ServerConfig.INSTANCE.getImageUrl() + AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("icon_url")),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("name")),
                generateDesсription(object),
                Color.parseColor(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("color"))),
                AndroidUtilities.INSTANCE.getBooleanFieldFromJson(object.get("featured"))
        );
    }

    private Spannable generateDesсription(JsonObject object) {
        int itemsCount = AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("items_count"));
        String description;
        Set<String> preview = new HashSet<>();
        JsonArray array = object.get("items_preview").getAsJsonArray();

        description = generateStartOfDescription(array, preview);
        return generateEndOfDescription(itemsCount, description.length(), description, preview);
    }

    private String generateStartOfDescription(JsonArray array, Set<String> preview) {
        for (int i = 0; i < array.size(); i++) {
            String item = array.get(i).getAsString();
            item = item.trim();
            preview.add(item.split(" или ")[0]);
        }
        return TextUtils.join(" \u2022 ", preview);
    }

    private Spannable generateEndOfDescription(int itemsCount, int start, String description, Set<String> preview) {
        if (itemsCount > preview.size()) {
            String pluralText = getResources().getQuantityString(R.plurals.items,
                    itemsCount - preview.size(), itemsCount - preview.size());
            description += getString(R.string.splitter_code) + pluralText;
        }

        Spannable span = new SpannableString(description);
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#b6b6b6")), start, description.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    private void sort(ArrayList<Category> collection) {
        Collections.sort(collection, new Comparator<Category>() {
            @Override
            public int compare(Category category, Category t) {
                if (category.getFeatured() && !t.getFeatured()) {
                    return -1;
                } else if (!category.getFeatured() && t.getFeatured()) {
                    return 1;
                } else {
                    return category.getName().compareTo(t.getName());
                }
            }
        });
    }
}
