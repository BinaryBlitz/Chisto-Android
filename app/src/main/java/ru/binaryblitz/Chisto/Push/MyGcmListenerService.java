package ru.binaryblitz.Chisto.Push;

import com.google.android.gms.gcm.GcmListenerService;

import android.os.Bundle;

public class MyGcmListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        sendNotification(message);
    }

    private void sendNotification(String message) {
        // TODO
    }
}
