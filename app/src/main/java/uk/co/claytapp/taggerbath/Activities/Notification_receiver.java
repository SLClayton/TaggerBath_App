package uk.co.claytapp.taggerbath.Activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import uk.co.claytapp.taggerbath.R;

/**
 * Created by Sam on 15/04/2017.
 */
public class Notification_receiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent repeating_intent = new Intent(context, MainActivity.class);

        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(context, 100, repeating_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //PendingIntent pi = PendingIntent.getActivity(context, 100, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Capture some squares!")
                .setContentText("Go and capture some squares for your team!")
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build();

        notificationManager.notify(100, n);
    }


}
