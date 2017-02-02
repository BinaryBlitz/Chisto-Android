package ru.binaryblitz.Chisto.Activities;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.binaryblitz.Chisto.Adapters.CitiesAdapter;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.RecyclerListView;
import ru.binaryblitz.Chisto.Model.City;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.ServerApi;
import ru.binaryblitz.Chisto.Utils.AndroidUtilities;
import ru.binaryblitz.Chisto.Utils.CustomPhoneNumberTextWatcher;
import ru.binaryblitz.Chisto.Utils.LogUtil;

public class SelectCityActivity extends BaseActivity
        implements SwipeRefreshLayout.OnRefreshListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private CitiesAdapter adapter;
    private SwipeRefreshLayout layout;
    private static final int LOCATION_PERMISSION = 2;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    private MaterialEditText phone;
    private MaterialEditText city;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ru.binaryblitz.Chisto.R.layout.activity_select_city);
        Fabric.with(this, new Crashlytics());

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initList();


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                layout.setEnabled(true);
                load();
            }
        });

        setOnClickListeners();
    }

    private void initDialog(MaterialDialog dialog) {
        View view = dialog.getCustomView();
        if (view == null) return;
        phone = (MaterialEditText) view.findViewById(R.id.editText);
        city = (MaterialEditText) view.findViewById(R.id.editText2);
        phone.addTextChangedListener(new CustomPhoneNumberTextWatcher());
    }

    @Override
    public void onRefresh() {
        load();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION);
                } else {
                    getLocation();
                }
            } catch (Exception ignored) {
                layout.setRefreshing(false);
            }
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    onLocationError();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        layout.setRefreshing(false);
        onLocationError();
    }

    public void cityError() {
        Snackbar.make(findViewById(ru.binaryblitz.Chisto.R.id.main), ru.binaryblitz.Chisto.R.string.city_error, Snackbar.LENGTH_SHORT).show();
    }

    private void setOnClickListeners() {
        findViewById(R.id.my_loc_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

        findViewById(R.id.city_not_found_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void initList() {
        RecyclerListView view = (RecyclerListView) findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setItemAnimator(new DefaultItemAnimator());
        view.setHasFixedSize(true);

        adapter = new CitiesAdapter(this);
        view.setAdapter(adapter);

        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(this);
        layout.setColorSchemeResources(R.color.colorAccent);
    }

    private void load() {
        ServerApi.get(this).api().getCitiesList().enqueue(new Callback<JsonArray>() {
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
        ArrayList<CitiesAdapter.City> collection = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            City city = new City(object.get("id").getAsInt(),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("name")),
                    AndroidUtilities.INSTANCE.getDoubleFieldFromJson(object.get("latitude")),
                    AndroidUtilities.INSTANCE.getDoubleFieldFromJson(object.get("longitude")));

            collection.add(new CitiesAdapter.City(city, false));
        }

        adapter.setCollection(collection);
        adapter.notifyDataSetChanged();
    }

    private void showDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(SelectCityActivity.this)
                .title(ru.binaryblitz.Chisto.R.string.app_name)
                .customView(R.layout.city_not_found_dialog, true)
                .positiveText(ru.binaryblitz.Chisto.R.string.send_code)
                .negativeText(ru.binaryblitz.Chisto.R.string.back_code)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (checkDialogInput()) {
                            sendSubscription(dialog);
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();

        initDialog(dialog);
    }

    private JsonObject generateJson() {
        JsonObject object = new JsonObject();
        JsonObject toSend = new JsonObject();

        object.addProperty("phone_number", phone.getText().toString());
        object.addProperty("content", city.getText().toString());

        toSend.add("subscription", object);

        return toSend;
    }

    private void sendSubscription(final MaterialDialog dialog) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();

        ServerApi.get(this).api().sendSubscription(generateJson()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressDialog.dismiss();
                dialog.dismiss();
                if (!response.isSuccessful()) {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressDialog.dismiss();
                dialog.dismiss();
                onInternetConnectionError();
            }
        });
    }

    private boolean checkDialogInput() {
        boolean res = true;
        if (!AndroidUtilities.INSTANCE.validatePhone(phone.getText().toString())) {
            phone.setError(getString(R.string.wrong_phone_code));
            res = false;
        }

        if (city.getText().toString().isEmpty()) {
            city.setError(getString(R.string.wrong_city_code));
            res = false;
        }

        return res;
    }

    private void load(double latitude, double longitude) {
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                if (adapter.getItemCount() == 0) {
                    cityError();
                } else {
                    adapter.selectCity(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                }
            } else {
                onLocationError();
            }
        } catch (IOException e) {
            LogUtil.logException(e);
        }
    }

    private void getLocation() {
        layout.setRefreshing(false);
        if (mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                onLocationError();
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                load(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            } else {
                onLocationError();
            }
        } else {
            mGoogleApiClient.connect();
        }
    }
}
