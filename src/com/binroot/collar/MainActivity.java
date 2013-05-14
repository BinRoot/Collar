package com.binroot.collar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends Activity
{
    DisplayCountDownTimer displayCountDownTimer = null;
    final String DEBUG = "MainActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.freq_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(24);

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.SETTINGS_FILE, MODE_PRIVATE);
                Log.d(DEBUG, "freq set to "+newVal);
                sharedPreferences.edit().putInt(Constants.SETTINGS_FREQ_KEY, newVal).commit();
            }
        });

        setUpFreq();
    }

    @Override
    public void onResume() {
        super.onResume();

        setUpCountdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_refresh) {
            setUpCountdown();
            Toast.makeText(this, "Refreshing countdown", Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId() == R.id.menu_help) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.suit)
                    .setTitle("Help")
                    .setItems(new CharSequence[]{
                            "\n"+getString(R.string.help_about)+"\n",
                            "\n"+getString(R.string.help_countdown)+"\n",
                            "\n"+getString(R.string.help_picker)+"\n",
                            "\n"+getString(R.string.help_clock)+"\n",
                            "\n"+getString(R.string.help_go)+"\n",
                            "\n"+getString(R.string.help_later)+"\n",
                            "\n"+getString(R.string.help_nope)+"\n",
                            "\n"+getString(R.string.help_deleting)+"\n"
                    }, null)
                    .setNegativeButton("Close", null)
                    .show();
        }

        return true;
    }

    private void setUpFreq() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SETTINGS_FILE, MODE_PRIVATE);
        int freq = sharedPreferences.getInt(Constants.SETTINGS_FREQ_KEY, 2);

        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.freq_picker);
        numberPicker.setValue(freq);
    }

    private void setUpCountdown() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SETTINGS_FILE, MODE_PRIVATE);
        long nextRing = sharedPreferences.getLong(Constants.SETTINGS_NEXT_RING, -1);

        Log.d(DEBUG, "nextRing: "+nextRing);

        if(nextRing != -1) {
            long timeDiff = nextRing-Calendar.getInstance().getTimeInMillis();

            if(displayCountDownTimer != null) {
                displayCountDownTimer.cancel();
            }
            displayCountDownTimer = new DisplayCountDownTimer(timeDiff, 1000);
            displayCountDownTimer.start();
        }
        else {
            Notifier.setUpNotification(MainActivity.this, MyContacts.getInstance(MainActivity.this).pop(MyContacts.NO_BIAS), 0);
            setUpCountdown();
        }
    }


    public void countdownClicked(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Call now?")
                .setMessage("Skip the countdown")
                .setIcon(R.drawable.suit)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(DEBUG, "yes clicked");
                        Notifier.setUpNotification(MainActivity.this, MyContacts.getInstance(MainActivity.this).pop(MyContacts.NO_BIAS), 0);
                        setUpCountdown();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public class DisplayCountDownTimer extends CountDownTimer {
        TextView countdown = (TextView)findViewById(R.id.countdown);

        public DisplayCountDownTimer(long time, long interval) {
            super(time, interval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long minutes = millisUntilFinished/(1000*60);
            long secs = millisUntilFinished/1000 - minutes*60;
            countdown.setText(minutes + " minutes and " + secs + " seconds");
        }

        @Override
        public void onFinish() {
            countdown.setText("Ring ring!");
        }
    }

}
