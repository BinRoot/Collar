package com.binroot.collar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: binroot
 * Date: 5/12/13
 * Time: 11:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class CallActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String id = getIntent().getExtras().getString("id");

        Notifier.cancelNotification(this);
        Person p = MyContacts.getInstance(this).pop(MyContacts.NO_BIAS);
        Notifier.setUpNotification(this, p, Notifier.getNextTime(this));

        Intent i = Notifier.contactCardIntent(this, id);
        startActivity(i);

        this.finish();
    }
}