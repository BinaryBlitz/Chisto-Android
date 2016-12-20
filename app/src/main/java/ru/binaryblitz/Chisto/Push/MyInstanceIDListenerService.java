package ru.binaryblitz.Chisto.Push;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import android.util.Log;

import ru.binaryblitz.Chisto.Utils.AppConfig;

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(AppConfig.LogTag, "Refreshed token: " + refreshedToken);
    }
}