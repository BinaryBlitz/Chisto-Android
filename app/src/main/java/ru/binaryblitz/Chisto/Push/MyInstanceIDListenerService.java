package ru.binaryblitz.Chisto.Push;

import com.google.android.gms.iid.InstanceIDListenerService;

import android.content.Intent;

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
