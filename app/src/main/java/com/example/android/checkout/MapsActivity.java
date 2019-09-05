/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.example.android.checkout;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ChildEventListener {

    private static final String FIREBASE_URL = "https://[REPLACE_WITH_YOUR__URL].firebaseio.com/";
    private static final String FIREBASE_ROOT_NODE = "checkouts";

    private GoogleMap mMap;
    private Firebase mFirebase;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds.Builder mBounds = new LatLngBounds.Builder();

    private Location myCurrentLocation;

    private EditText etLocationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        etLocationName = (EditText) findViewById(R.id.etLocationName);

        // Set up Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up the API client for Places API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();

        // Set up Firebase
        Firebase.setAndroidContext(this);
        mFirebase = new Firebase(FIREBASE_URL);

        mFirebase.child(FIREBASE_ROOT_NODE).addChildEventListener(this);
    }

    public void checkOut(View view) {
        mFirebase.child(FIREBASE_ROOT_NODE)
                .child("" + new Date().getTime() + "")
                .setValue(new MyLatLng(myCurrentLocation.getLatitude() + "", myCurrentLocation.getLongitude() + "", etLocationName.getText().toString()+""));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                myCurrentLocation = location;

                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                addPointToViewPort(ll);
                // we only want to grab the location once, to allow the user to pan and zoom freely.
                mMap.setOnMyLocationChangeListener(null);
            }
        });

        // Pad the map controls to make room for the button - note that the button may not have
        // been laid out yet.
        final Button button = (Button) findViewById(R.id.checkout_button);
        button.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mMap.setPadding(0, button.getHeight(), 0, 0);
                    }
                }
        );
    }

    private void addPointToViewPort(LatLng newPoint) {
        mBounds.include(newPoint);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds.build(),
                findViewById(R.id.checkout_button).getHeight()));
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        MyLatLng myLatLng = dataSnapshot.getValue(MyLatLng.class);

        mMap.addMarker(
                new MarkerOptions()
                        .position(
                                new LatLng(Double.parseDouble(myLatLng.getLat()), Double.parseDouble(myLatLng.getLng())))
                        .title(myLatLng.getLocationName())
        );
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        MyLatLng myLatLng = dataSnapshot.getValue(MyLatLng.class);

        mMap.addMarker(
                new MarkerOptions()
                        .position(
                                new LatLng(Double.parseDouble(myLatLng.getLat()), Double.parseDouble(myLatLng.getLng()))));
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
