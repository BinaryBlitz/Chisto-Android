package ru.binaryblitz.Chisto.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.entities.User;
import ru.binaryblitz.Chisto.network.DeviceInfoStore;
import ru.binaryblitz.Chisto.ui.base.BaseActivity;
import ru.binaryblitz.Chisto.utils.AndroidUtilities;
import ru.binaryblitz.Chisto.utils.LogUtil;
import ru.binaryblitz.Chisto.views.MyMapFragment;

public class MapActivity extends BaseActivity
        implements MyMapFragment.TouchableWrapper.UpdateMapAfterUserInteraction, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int LOCATION_PERMISSION = 1;
    private GoogleMap googleMap;

    static public LatLng selected_lat_lng;
    static public String selected = "";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    private AutoCompleteTextView searchBox;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyC48bjEMes05K8RgTG6PrwVSKicZqXZ7WY";

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList<String> resultList;

        GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        resultList = autocomplete(constraint.toString());
                        filterResults.values = resultList;
                        filterResults.count = resultList == null ? 0 : resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initAutocomplete();
        initMap();
        initGoogleApiClient();
        setOnClickListeners();
    }

    private void initAutocomplete() {
        searchBox = findViewById(R.id.search_box);

        searchBox.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        searchBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Geocoder geocoder = new Geocoder(MapActivity.this);
                List<Address> addresses;
                try {
                    String address = (String) searchBox.getAdapter().getItem(i);
                    addresses = geocoder.getFromLocationName(address, 1);
                    if (addresses.size() > 0) autocompleteClick(addresses, address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void autocompleteClick(List<Address> addresses, String address) {
        double latitude= addresses.get(0).getLatitude();
        double longitude= addresses.get(0).getLongitude();

        selected_lat_lng = new LatLng(latitude, longitude);
        selected = address;
        moveCamera(false);

        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private ArrayList<String> autocomplete(String input) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            String sb = PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON + "?key=" + API_KEY + "&components=country:ru" +
                    "&input=" + URLEncoder.encode(input, "utf8");

            conn = (HttpURLConnection) new URL(sb).openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }

        return parseAnswer(jsonResults);
    }

    private ArrayList<String> parseAnswer(StringBuilder jsonResults) {
        ArrayList<String> resultList = null;
        try {
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray prevJsonArray = jsonObj.getJSONArray("predictions");

            resultList = new ArrayList<>(prevJsonArray.length());
            for (int i = 0; i < prevJsonArray.length(); i++) {
                resultList.add(prevJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            LogUtil.logException(e);
        }

        return resultList;
    }

    private void initMap() {
        final SupportMapFragment mMap = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.scroll);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mMap.getMapAsync(MapActivity.this);
            }
        });
    }

    private void initGoogleApiClient() {
        if (mGoogleApiClient != null) return;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void setOnClickListeners() {
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.my_loc_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGoogleApiClient.isConnected()) getLocation();
                else mGoogleApiClient.connect();
            }
        });

        findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Geocoder geocoder;
                geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                saveUser(geocoder);
                finish();
            }
        });

        final View activityRootView = findViewById(R.id.main);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                activityRootView.getWindowVisibleDisplayFrame(r);
                int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);

                if (searchBox != null) {
                    boolean isKeyboard = heightDiff > 100;
                    searchBox.setCursorVisible(isKeyboard);
                }
            }
        });
    }

    private void saveUser(Geocoder geocoder) {
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(selected_lat_lng.latitude, selected_lat_lng.longitude, 1);
            String street = addresses.get(0).getThoroughfare();
            String house = addresses.get(0).getSubThoroughfare();

            User user = DeviceInfoStore.getUserObject(MapActivity.this);
            if (user == null) return;

            user.setHouseNumber(house);
            user.setStreetName(street);

            DeviceInfoStore.saveUser(MapActivity.this, user);
        } catch (IOException e) {
            LogUtil.logException(e);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermission()) {
                ActivityCompat.requestPermissions(MapActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION);
            } else {
                setUpMap();
            }
        } catch (Exception e) {
            LogUtil.logException(e);
        }
    }

    @SuppressLint("NewApi")
    private boolean checkPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setPadding(0, (int) AndroidUtilities.INSTANCE.convertDpToPixel(66f, this), 0, 0);
    }

    private String getCompleteAddressString(double latitude, double lognitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, lognitude, 1);
            if (addresses != null) {
                if (addresses.size() > 0) {
                    strAdd = addresses.get(0).getAddressLine(0);
                }
            }
        } catch (Exception ignored) {
        }

        selected_lat_lng = new LatLng(latitude, lognitude);

        selected = strAdd;
        return strAdd;
    }

    @Override
    public void onUpdateMapAfterUserInteraction() {
        try {
            String res = getCompleteAddressString(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude);
            searchBox.setText(res);
            searchBox.dismissDropDown();
        } catch (Exception e) {
            LogUtil.logException(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mGoogleApiClient.isConnected()) {
                    getLocation();
                } else {
                    mGoogleApiClient.connect();
                }
            }
        }, 50);
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
            } catch (Exception e) {
                LogUtil.logException(e);
            }
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onLocationError();
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            selected_lat_lng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            moveCamera(true);
        } else {
            onLocationError();
        }
    }

    private void moveCamera(final boolean setText) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(selected_lat_lng)
                .zoom(17)
                .bearing(0)
                .tilt(0)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (setText) {
                            getCompleteAddressString(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude);
                            searchBox.setText(selected);
                        }
                        searchBox.dismissDropDown();
                    }
                }, 50);
            }

            @Override
            public void onCancel() {
            }
        });
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
        onLocationError();
    }
}
