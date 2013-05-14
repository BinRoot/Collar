package com.example.NotificationTest;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: binroot
 * Date: 5/12/13
 * Time: 2:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlertBroadcastReceiver extends BroadcastReceiver {

    private final String DEBUG = "AlertBroadcast";

    public void onReceive(Context context, Intent intent) {

        Log.d(DEBUG, "onReceive called with "+intent.getExtras().get(Constants.INTENT_ACTION));

        if(intent.getExtras().get(Constants.INTENT_ACTION) == null) {
            String displayName = intent.getExtras().getString(Constants.INTENT_DISPLAYNAME);
            String id = intent.getExtras().getString(Constants.INTENT_ID);

            Notifier.showNotification(context, new Person(displayName, id));
        }
        else {
            if(intent.getExtras().getInt(Constants.INTENT_ACTION) == 1) { // unused.
                doAction1(context, intent);
            }
            else if(intent.getExtras().getInt(Constants.INTENT_ACTION) == 2) {
                doAction2(context);
            }
            else if(intent.getExtras().getInt(Constants.INTENT_ACTION) == 3) {
                doAction3(context);
            }
            else if(intent.getExtras().getInt(Constants.INTENT_ACTION) == 4) {
                doAction4(context);
            }
        }
    }

    // user wants to make a class. no bias. (deprecated)
    private void doAction1(Context context, Intent intent) {
        Notifier.cancelNotification(context);

        Person p = MyContacts.getInstance(context).pop(MyContacts.NO_BIAS);
        Notifier.setUpNotification(context, p, Notifier.getNextTime(context));

        Intent i = Notifier.contactCardIntent(context, intent.getExtras().getString(Constants.INTENT_ID));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    // user wants to talk later. top bias.
    private void doAction2(Context context) {
        Notifier.cancelNotification(context);

        Person p = MyContacts.getInstance(context).pop(MyContacts.TOP_BIAS);
        Notifier.setUpNotification(context, p, Notifier.getNextTime(context));
    }

    // user wants to ignore. bottom bias.
    private void doAction3(Context context) {
        Notifier.cancelNotification(context);

        Person p = MyContacts.getInstance(context).pop(MyContacts.BOTTOM_BIAS);
        Notifier.setUpNotification(context, p, Notifier.getNextTime(context));
    }

    // user deletes notification. no bias.
    private void doAction4(Context context) {
        Person p = MyContacts.getInstance(context).pop(MyContacts.NO_BIAS);
        Notifier.setUpNotification(context, p, Notifier.getNextTime(context));
    }
}
