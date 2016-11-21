package ru.binaryblitz.Chisto.Activities;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.MyMapFragment;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Utils.AndroidUtilities;

public class MapActivity extends BaseActivity
        implements MyMapFragment.TouchableWrapper.UpdateMapAfterUserInteraction, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap googleMap;

    static public LatLng selected_lat_lng;
    static public String selected = "";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        final SupportMapFragment mMap = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.scroll);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMap.getMapAsync(MapActivity.this);
            }
        }, 100);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                } else {
                    setUpMap();
                }
            } else {
                setUpMap();
            }
        } catch (Exception ignored) {
        }
    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        selected = strAdd;
        return strAdd;
    }

    @Override
    public void onUpdateMapAfterUserInteraction() {
        try {
            String res = getCompleteAddressString(googleMap.getCameraPosition().target.latitude,
                    googleMap.getCameraPosition().target.longitude);
            ((TextView) findViewById(R.id.date_text_view)).setText(res);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mGoogleApiClient.isConnected()) getLocation();
                else mGoogleApiClient.connect();
            }
        }, 50);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                } else {
                    getLocation();
                }
            } catch (Exception ignored) {
            }
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onLocationError();
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            selected_lat_lng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            moveCamera();
        } else {
            onLocationError();
        }
    }

    private void moveCamera() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(selected_lat_lng)
                .zoom(17)
                .bearing(90)
                .tilt(40)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getCompleteAddressString(googleMap.getCameraPosition().target.latitude,
                                googleMap.getCameraPosition().target.longitude);
                        ((TextView) findViewById(R.id.date_text_view)).setText(selected);
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
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
