package ru.binaryblitz.Chisto.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ru.binaryblitz.Chisto.ui.order.MyOrderActivity;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.utils.AppConfig;
import ru.binaryblitz.Chisto.utils.LogUtil;

public class MyGcmListenerService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(AppConfig.LogTag, "From: " + remoteMessage.getFrom());
        processPush(remoteMessage);
    }

    private void processPush(RemoteMessage remoteMessage) {
        String body = remoteMessage.getNotification() == null || remoteMessage.getNotification().getBody() == null ?
                getString(R.string.default_notification_text) : remoteMessage.getNotification().getBody();
        int id = 0;
        try {
            id = Integer.parseInt(remoteMessage.getData().get("order_id"));
        } catch (Exception e) {
            LogUtil.logException(e);
        }
        sendNotification(id, body);
    }

    private void sendNotification(int orderId, String messageBody) {
        MyOrderActivity.Companion.setOpenedFromPush(true);
        Intent intent = new Intent(this, MyOrderActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("id", orderId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.status_changed))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
