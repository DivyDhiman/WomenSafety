package com.example.abhay0648.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Address_location_get extends Runtime_permission implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,View.OnClickListener {
    private GoogleMap mMap;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    protected String mLastUpdateTime,address_loc;
    private boolean check_map_ready, get_location_onlocation_change, check_location;
    private LocationManager manager;
    private double latitude, longitude;
    private Context context;
    private LatLng latLng;
    private Marker marker;
    private Button btnShowLocationDialog,btnPlaySound;
    private MyCallbacks myCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        intentServiceTrigger = new Intent(Address_location_get.this, SoundPlayService.class);
        initialise();

        check_map_ready = false;
        get_location_onlocation_change = true;
        check_location = false;

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLastUpdateTime = "";
        updateValuesFromBundle(savedInstanceState);

        myCallbacks = new MyCallbacks() {
            @Override
            public void getPermissionValue(String permissionData) {
                if(permissionData.equals("Allow")){
                    check_permissions(context,myCallbacks, Manifest.permission.ACCESS_FINE_LOCATION,"Allow_Fine",28);
                }else if(permissionData.equals("Allow_Fine")){
                    call_location();
                }
            }
        };
    }

    private void initialise() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnShowLocationDialog = (Button) findViewById(R.id.btnshowdialog);
        btnPlaySound = (Button) findViewById(R.id.btnplaysound);

        btnShowLocationDialog.setOnClickListener(this);
        btnPlaySound.setOnClickListener(this);
        context = Address_location_get.this;

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private void call_location() {
        get_location_onlocation_change = true;
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        check_map_ready = true;
        mMap.setMyLocationEnabled(true);
    }

    private void alert_dialog(String title, String des, String pos, String neg, final int type ) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(des);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(pos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (type == 0){
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
                else
                {
                    String phone = "100";
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                    startActivity(intent);

                }

            }
        });

        alertDialog.setNegativeButton(neg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        if (get_location_onlocation_change) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            latLng = new LatLng(latitude, longitude);

            drawPin(latLng);
            address_loc = getCompleteAddressString(latitude, longitude);
           // Toast.makeText(context,address_loc,Toast.LENGTH_SHORT).show();
            alert_dialog("Panic Situation","You are at "+address_loc+" wanna make a call to police ?","Call","Cancel",1);
            get_location_onlocation_change = false;
        }
    }

    public void drawPin(LatLng latlng) {

        this.latLng = latlng;

        mMap.getUiSettings().setCompassEnabled(false);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).bearing(90).tilt(30).zoom(17).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        if (marker != null) {
            marker.remove();
        }

        marker = mMap.addMarker(new MarkerOptions()
                .position(latLng));
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            } else {
                strAdd = "Error";
            }
        } catch (Exception e) {
            strAdd = "Error";
        }
        return strAdd;
    }


int countServ = 0;
    Intent intentServiceTrigger;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnshowdialog:
                if (!manager.isProviderEnabled((LocationManager.GPS_PROVIDER))) {
                    alert_dialog("Enable GPS","Enable your GPS","Settings","Cancel",0);
                }else {
                    if(Current_Build_version() >= Build.VERSION_CODES.LOLLIPOP){
                        check_permissions(context,myCallbacks, Manifest.permission.ACCESS_COARSE_LOCATION,"Allow",24);
                    }else {
                        call_location();
                    }
                }
                break;

            case R.id.btnplaysound:
                if(countServ  == 0) {

                    startService(intentServiceTrigger);
                    ++countServ;
                }
                else
                {
                    stopService(intentServiceTrigger);
                    countServ = 0;

                }



                break;
        }
    }


    //Update location from google fused api
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
        }
    }

    //synchronized google fused location api
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    //create location request
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }


    @Override
    public void onConnected(Bundle bundle) {

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            check_location = true;
        }
        startLocationUpdates();

    }

    //check connection suspended
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    //Location update
    protected void startLocationUpdates() {
        if (check_location) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    //location update close when activity closed
    protected void stopLocationUpdates() {
        if (check_location) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    //on resume activity
    @Override
    public void onResume() {
        super.onResume();

        if (check_map_ready) {
            if (mGoogleApiClient.isConnected()) {
                startLocationUpdates();
            }
        }
    }

    //when activity goes on pause
    @Override
    protected void onPause() {

        if (check_map_ready) {
            if (mGoogleApiClient.isConnected()) {
                stopLocationUpdates();
            }
        }
        super.onPause();
    }


    //when activity stops
    @Override
    protected void onStop() {
        if (check_map_ready) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

 }