package com.binroot.collar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import net.sf.doodleproject.numerics4j.random.ExponentialRandomVariable;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: binroot
 * Date: 5/12/13
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class Notifier {

    final static int DEFAULT_FREQ = 2;

    public static long getNextTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_FILE, Context.MODE_PRIVATE);
        int freq = sharedPreferences.getInt(Constants.SETTINGS_FREQ_KEY, DEFAULT_FREQ);
        return getNextTime(freq);
    }

    private static long getNextTime(int timesPerDay) {
        ExponentialRandomVariable exponentialRandomVariable = new ExponentialRandomVariable(1.0/((double)timesPerDay));
        double x = exponentialRandomVariable.nextRandomVariable();
        long ms = (long)(x * 86400000);
        return ms;
    }

    public static void setUpNotification(Context context, Person person, long delayInMs) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlertBroadcastReceiver.class);
        intent.putExtra(Constants.INTENT_DISPLAYNAME, person.displayName);
        intent.putExtra(Constants.INTENT_ID, person.id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delayInMs, pendingIntent);

        long nextRing = Calendar.getInstance().getTimeInMillis() + delayInMs;
        saveNextRing(context, nextRing);

        int secs = (int)delayInMs/1000;
        int mins = secs/60;
        Toast.makeText(context, "Next call in "+mins+" mins", Toast.LENGTH_SHORT).show();
    }

    private static void saveNextRing(Context context, long nextRing) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_FILE, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(Constants.SETTINGS_NEXT_RING, nextRing).commit();
    }

    public static void showNotification(Context context, Person person) {
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        Intent callIntent = new Intent(context, CallActivity.class);
        callIntent.putExtra(Constants.INTENT_ACTION, 1);
        callIntent.putExtra(Constants.INTENT_DISPLAYNAME, person.displayName);
        callIntent.putExtra(Constants.INTENT_ID, person.id);

        Intent broadcastLaterIntent = new Intent(context, AlertBroadcastReceiver.class);
        broadcastLaterIntent.putExtra(Constants.INTENT_ACTION, 2);

        Intent broadcastIgnoreIntent = new Intent(context, AlertBroadcastReceiver.class);
        broadcastIgnoreIntent.putExtra(Constants.INTENT_ACTION, 3);

        Intent deleteIntent = new Intent(context, AlertBroadcastReceiver.class);
        deleteIntent.putExtra(Constants.INTENT_ACTION, 4);



        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setTicker(context.getString(R.string.noti_ticker))
                .setContentTitle(context.getString(R.string.noti_title))
                .setContentText("with " + person.displayName + "!")
                .setLights(context.getResources().getColor(R.color.notification), 10000, 1)
                .setSound(sound)
                .setSmallIcon(R.drawable.heart_small)
                .setVibrate(new long[]{0, 100,100,100,100,100})
                .setContentIntent(PendingIntent.getActivity(context, 200, callIntent, PendingIntent.FLAG_ONE_SHOT))
                .setDeleteIntent(PendingIntent.getBroadcast(context, 4, deleteIntent, PendingIntent.FLAG_ONE_SHOT))
                .addAction(R.drawable.phone2, context.getString(R.string.noti_action1),
                        PendingIntent.getActivity(context, 100, callIntent, PendingIntent.FLAG_ONE_SHOT))
                .addAction(R.drawable.clock, context.getString(R.string.noti_action2),
                        PendingIntent.getBroadcast(context, 2, broadcastLaterIntent, PendingIntent.FLAG_ONE_SHOT))
                .addAction(R.drawable.cross_small, context.getString(R.string.noti_action3),
                        PendingIntent.getBroadcast(context, 3, broadcastIgnoreIntent, PendingIntent.FLAG_ONE_SHOT));


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);

    }

    public static Intent contactCardIntent(Context context, String id) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(id));
        intent.setData(uri);
        return intent;
    }
}
