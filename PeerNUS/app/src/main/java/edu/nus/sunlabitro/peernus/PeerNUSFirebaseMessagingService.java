package edu.nus.sunlabitro.peernus;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

public class PeerNUSFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "PeerNUSFMS";
    private final String USER_PREF = getString(R.string.USER_PREF);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            /*
            if (// Check if data needs to be processed by long running job  true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
            */

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            // sendNotification();
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

//    public void sendNotification () {
//        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
//
//        if (!cn.getClassName().equals(ChatActivity.class.getName())) {
//            Intent intent = new Intent(this, ChatActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            Bundle bundle = new Bundle();
//            bundle.putString("receiverName", receiverName);
//            bundle.putString("email", email);
//
//            if (bytes.length > 0) {
//                bundle.putByteArray("profilePic", bytes);
//            } else {
//                bundle.putByteArray("profilePic", null);
//            }
//
//            intent.putExtras(bundle);
//
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                    PendingIntent.FLAG_ONE_SHOT);
//
//
//            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            NotificationCompat.Builder notificationBuilder =
//                    new NotificationCompat.Builder(this, chatroomId)
//                            .setSmallIcon(R.drawable.ic_peernus_logo_round)
//                            .setContentTitle(receiverName)
//                            .setContentText(messageBody)
//                            .setAutoCancel(true)
//                            .setSound(defaultSoundUri)
//                            .setContentIntent(pendingIntent);
//
//            NotificationManager notificationManager =
//                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//            // Since android Oreo notification channel is needed.
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                NotificationChannel channel = new NotificationChannel(chatroomId,
//                        "Channel human readable title",
//                        NotificationManager.IMPORTANCE_DEFAULT);
//                notificationManager.createNotificationChannel(channel);
//            }
//
//            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//        }
//    }

}
