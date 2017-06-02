package ru.binaryblitz.Chisto.push;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import ru.binaryblitz.Chisto.utils.AppConfig;

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(AppConfig.LogTag, "Refreshed token: " + refreshedToken);
    }
}