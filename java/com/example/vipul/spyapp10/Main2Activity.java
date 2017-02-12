package com.example.vipul.spyapp10;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;


public class Main2Activity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {
    Location mLastLocation;
    public static String myLocation;
    public static Double lat, lng;
    GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks((ConnectionCallbacks) this)
                    .addOnConnectionFailedListener((OnConnectionFailedListener) this).addApi(LocationServices.API).build();
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            myLocation=mLastLocation.getLatitude() + "|" + mLastLocation.getLongitude();
            Intent i = new Intent(Main2Activity.this, MyService2.class);
            i.putExtra("myValue", mLastLocation.getLatitude() + "|" + mLastLocation.getLongitude());
            startService(i);
            finish();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
