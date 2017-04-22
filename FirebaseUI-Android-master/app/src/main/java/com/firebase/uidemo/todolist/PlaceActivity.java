package com.firebase.uidemo.todolist;

import com.firebase.uidemo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by Katherine on 4/20/2017.
 */

public class PlaceActivity extends FragmentActivity implements OnConnectionFailedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "PlaceActivity";
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    private Place curPlace;
    private String curPlaceName;
    private Marker curDestMarker;
    private Button chooserButton;
    private boolean existing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        setUpChooserButton();
        setUpGoogleMapAndPlaces();
    }

    private boolean placeExistingLocation() {
        Intent i = getIntent();
        String mLat = i.getStringExtra("lat");
        String mLong = i.getStringExtra("long");
        String mPlace = i.getStringExtra("place");
        if(!mLat.equals("") && !mLong.equals("")) {
            LatLng mLL = new LatLng(Double.parseDouble(mLat),Double.parseDouble(mLong));
            curDestMarker = mGoogleMap.addMarker(new MarkerOptions().position(mLL).title(mPlace));
            curPlaceName = mPlace;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mLL));
            mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(13));
            fillChooserButton();
            return true;
        }
        return false;
    }

    private void setUpChooserButton() {
        chooserButton = (Button) findViewById(R.id.placeChooserButton);
        if(curPlaceName == null) {
            chooserButton.setText("Choose No Location");
        }
        else {
            fillChooserButton();
        }

        chooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                if(curPlace != null) {
                    String destAddress = (String)curPlace.getAddress();
                    returnIntent.putExtra("address", destAddress);
                    returnIntent.putExtra("place",(String)curPlace.getName());
                    LatLng ll = curPlace.getLatLng();
                    returnIntent.putExtra("lat",Double.toString(ll.latitude));
                    returnIntent.putExtra("long",Double.toString(ll.longitude));
                    setResult(RESULT_OK,returnIntent);
                }
                else {
                    setResult(RESULT_CANCELED, returnIntent);
                }
                finish();
            }
        });
    }

    private void fillChooserButton() {
        String text = String.format("Set %s as your task location", curPlaceName);
        chooserButton.setText(text);
    }

    private void setUpGoogleMapAndPlaces() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng placeLoc = place.getLatLng();
                if(curDestMarker != null) {
                    curDestMarker.remove();
                }
                curPlace = place;
                curPlaceName = place.getName().toString();
                fillChooserButton();
                curDestMarker = mGoogleMap.addMarker(new MarkerOptions().position(placeLoc).title(curPlaceName));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(placeLoc));
                mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                // TODO: Get info about the selected place.
                Log.i("", "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("", "An error occurred: " + status);
            }
        });

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "connection failed");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        existing = placeExistingLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(PlaceActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i(TAG, "no permission");
            // TODO: handle case where user doesn't grant permission
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            Log.i(TAG, "location null");
        } else {
            handleLocation(location);
        }
    }

    private void handleLocation(Location location) {
        double curLat = location.getLatitude();
        double curLong = location.getLongitude();
        LatLng curLoc = new LatLng(curLat, curLong);
        mGoogleMap.addCircle(new CircleOptions().center(curLoc).radius(100).fillColor(Color.BLUE));
        if(!existing) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(curLoc));
            mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(13));
        }
        Log.d(TAG, "Circle added");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }


    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

}
