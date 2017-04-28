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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

/**
 * Created by Katherine on 4/20/2017.
 */

public class PlaceActivity extends FragmentActivity implements OnConnectionFailedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "PlaceActivity";
    private static final int FINE_LOCATION_PERMISSION = 1600;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;

    private Place mPlace;
    private String mPlaceName, mChoice;
    private Marker mPlaceMarker;
    private Button chooserButton;
    private LatLng mLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        setUpChooserButton();
        setUpGoogleMapAndPlaces();
    }

    private boolean placeExistingLocation() {
        Intent i = getIntent();
        String latitude = i.getStringExtra("lat");
        String longitude = i.getStringExtra("long");
        String place = i.getStringExtra("place");
        if (latitude != null && longitude != null && !latitude.equals("") && !longitude.equals("")) {
            LatLng latlong = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            mPlaceMarker = mGoogleMap.addMarker(new MarkerOptions().position(latlong).title(place));
            mPlaceName = place;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latlong));
            mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(13));
            fillChooserButton(mPlaceName);
            return true;
        }
        return false;
    }

    private void setUpChooserButton() {
        chooserButton = (Button) findViewById(R.id.placeChooserButton);
        if (mPlaceName == null) {
            chooserButton.setText("Choose No Location");
        } else {
            fillChooserButton(mPlaceName);
        }

        chooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, mChoice);
                Intent returnIntent = new Intent();
                if(mChoice.equals("Current Position")) {
                    Log.i(TAG, "current position chosen");
                    returnIntent.putExtra("place", "Custom Place");
                    Log.d(TAG, " " + mLoc.latitude + " " + mLoc.longitude);
                    returnIntent.putExtra("lat", Double.toString(mLoc.latitude));
                    returnIntent.putExtra("long", Double.toString(mLoc.longitude));
                    setResult(RESULT_OK, returnIntent);
                }
                else if (mPlace != null) {
                    returnIntent.putExtra("place", (String) mPlace.getName());
                    LatLng ll = mPlace.getLatLng();
                    returnIntent.putExtra("lat", Double.toString(ll.latitude));
                    returnIntent.putExtra("long", Double.toString(ll.longitude));
                    setResult(RESULT_OK, returnIntent);
                }
                else {
                    setResult(RESULT_CANCELED, returnIntent);
                }
                finish();
            }
        });
    }

    private void fillChooserButton(String loc) {
        String text = String.format("Set %s as your task location", loc);
        chooserButton.setText(text);
        Log.d(TAG, loc);
        mChoice = loc;

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
                if (mPlaceMarker != null) {
                    mPlaceMarker.remove();
                }
                mPlace = place;
                mPlaceName = place.getName().toString();
                fillChooserButton(mPlaceName);
                mPlaceMarker = mGoogleMap.addMarker(new MarkerOptions().position(placeLoc).title(mPlaceName));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(placeLoc));
                mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                float[] results = new float[1];
                if(mLoc != null) {
                    Location.distanceBetween(mLoc.latitude, mLoc.longitude, placeLoc.latitude, placeLoc.longitude, results);
                    float dist = results[0] / (float)1609.34;
                    String distToast = String.format("%s is %.2f miles from your current location.", mPlaceName, dist);
                    Toast.makeText(getApplicationContext(), distToast, Toast.LENGTH_LONG).show();
                }

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
        mGoogleMap.setOnMarkerClickListener(this);
        placeExistingLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        getLocation();

    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PlaceActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        handleLocation(location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }
                return;
            }
        }
    }

    private void handleLocation(Location location) {
        if(location != null) {
            double curLat = location.getLatitude();
            double curLong = location.getLongitude();
            mLoc = new LatLng(curLat, curLong);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(mLoc);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mGoogleMap.addMarker(markerOptions);
            if (mPlaceMarker == null) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mLoc));
                mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(13));
            }
            Log.d(TAG, "Circle added");
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.i(TAG, "marker clicked" + marker.getTitle());
        marker.showInfoWindow();
        fillChooserButton(marker.getTitle());
        return true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

}
