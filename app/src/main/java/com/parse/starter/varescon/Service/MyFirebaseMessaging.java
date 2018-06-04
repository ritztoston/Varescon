package com.parse.starter.varescon.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.R;
import com.parse.starter.varescon.RiderActivity;
import com.parse.starter.varescon.RiderCall;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    private LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if (remoteMessage.getNotification().getTitle().equals("Cancel")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("MyData");
                    intent.putExtra("Title", "Cancel");
                    intent.putExtra("Message", remoteMessage.getNotification().getBody());
                    broadcastManager.sendBroadcast(intent);
                }
            });
        } else if (remoteMessage.getNotification().getTitle().equals("PaidPayPal")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    Intent intent = new Intent("MyData");
                    intent.putExtra("Title", "PaidPayPal");
                    intent.putExtra("Message", remoteMessage.getNotification().getBody());
                    broadcastManager.sendBroadcast(intent);
                }
            });
        }else if (remoteMessage.getNotification().getTitle().equals("Accept")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("MyData");
                    intent.putExtra("Title", "Accept");
                    intent.putExtra("Message", remoteMessage.getNotification().getBody());
                    broadcastManager.sendBroadcast(intent);
                }
            });
        } else if (remoteMessage.getNotification().getTitle().equals("Arrived")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("MyData");
                    intent.putExtra("Title", "Arrived");
                    intent.putExtra("Message", remoteMessage.getNotification().getBody());
                    broadcastManager.sendBroadcast(intent);
                }
            });
            showArrivedNotification(remoteMessage.getNotification().getBody());
        } else if (remoteMessage.getNotification().getTitle().equals("Late")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("MyData");
                    intent.putExtra("Title", "Late");
                    intent.putExtra("Message", remoteMessage.getNotification().getBody());
                    broadcastManager.sendBroadcast(intent);
                }
            });
        } else if (remoteMessage.getNotification().getTitle().equals("Called")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("MyData");
                    intent.putExtra("Title", "Called");
                    intent.putExtra("Message", remoteMessage.getNotification().getBody());
                    broadcastManager.sendBroadcast(intent);
                }
            });
        } else if (remoteMessage.getNotification().getTitle().equals("Destination")) {
            LatLng destination = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);
            Common.desLat = destination.latitude;
            Common.desLong = destination.longitude;
        } else if (remoteMessage.getNotification().getTitle().equals("Arrived Destination")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("MyData");
                    intent.putExtra("Title", "Arrived Destination");
                    intent.putExtra("Message", remoteMessage.getNotification().getBody());
                    broadcastManager.sendBroadcast(intent);
                }
            });
        } else if (remoteMessage.getNotification().getTitle().equals("Mode of Payment")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("MyData");
                    intent.putExtra("Title", "Mode of Payment");
                    intent.putExtra("Message", remoteMessage.getNotification().getBody());
                    broadcastManager.sendBroadcast(intent);
                }
            });
        } else if (remoteMessage.getNotification().getTitle().equals("Paid")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("MyData");
                    intent.putExtra("Title", "Paid");
                    intent.putExtra("Message", remoteMessage.getNotification().getBody());
                    broadcastManager.sendBroadcast(intent);
                }
            });
        } else if (remoteMessage.getNotification().getTitle().equals("Transaction Complete")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("MyData");
                    intent.putExtra("Title", "Transaction Complete");
                    intent.putExtra("Message", remoteMessage.getNotification().getBody());
                    broadcastManager.sendBroadcast(intent);
                }
            });
        } else if (remoteMessage.getNotification().getTitle().equals("UserKey")) {
            Common.userKey = remoteMessage.getNotification().getBody();
        } else {
            Common.driverRequest++;
            Common.riderToken = remoteMessage.getNotification().getTitle();

            if (Common.driverRequest == 1) {
                LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);

                Intent intent = new Intent(getBaseContext(), RiderCall.class);
                intent.putExtra("title", "Rider");
                intent.putExtra("lat", customer_location.latitude);
                intent.putExtra("lng", customer_location.longitude);
                intent.putExtra("customer", remoteMessage.getNotification().getTitle());
                Common.pickupLat = customer_location.latitude;
                Common.pickupLong = customer_location.longitude;

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else if (Common.driverRequest > 1) {
                Intent intent = new Intent(getBaseContext(), RiderCall.class);
                intent.putExtra("title", "Called");
                intent.putExtra("customer", remoteMessage.getNotification().getTitle());

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    private void showArrivedNotification(String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }
}
