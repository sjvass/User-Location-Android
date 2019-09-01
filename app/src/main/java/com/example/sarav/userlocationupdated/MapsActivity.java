package com.example.sarav.userlocationupdated;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static android.widget.Toast.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    //map object
    private GoogleMap mMap;
    //location server objects
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    //most recent location
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //instantiate fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //create location callback
        //handles result of location server call
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //prints last location to console
//                Log.i("Last Location1", locationResult.toString());

                //if a last location is found
                if(locationResult.getLastLocation() != null) {
                    //saves last location from map to lastLocation and updates map
                    lastLocation = locationResult.getLastLocation();
                    updateMap();
                }

            }
        };

        //set up locationRequest
        locationRequest = new LocationRequest();
        //set interval to 1 sec
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        //set locationRequest to highest accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //if location permission not granted, request permission
        if(!checkPermission()) {
            requestPermission();
        }
        //requests location updates
        requestLocationUpdates();

    }


    //sets up a default map view when map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //sets mMap to a standard map
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //Zoom in on the world with US at center
        LatLng USCenter = new LatLng(39.8283, -98.5795);
        //zooms camera to show North America
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(USCenter, 3));

        //If permission not granted
        if(!checkPermission()) {
            //request permission
            requestPermission();
        }
    }

    //checks if user has given app permission to access their location
    //true if granted, false if not granted
    private boolean checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        else {
            return true;
        }
    }

    //requests permission if not already granted
    private void requestPermission() {
        //if permission not granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //requests permission
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        //if permission already granted, logs it to console
        else {
            Log.i("Permission", "Granted");
        }
    }

    //overridden method handles results of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //if permission granted and result not empty
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //calls getLastLocation method
            getLastLocation();
        }
        else {
            //displays that user has denied permission
            Toast permissionDenied = new Toast(getApplicationContext());
            permissionDenied.makeText(getApplicationContext(), "You have denied this app permission to access your location." +
                "Please grant location permission to continue.", LENGTH_LONG).setGravity(Gravity.CENTER, 0, 0);
            permissionDenied.show();
        }
    }

    //requests updates from google play service API when location changes
    //suppress missing permission warning b/c checks permission before calling method
    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {
        //looper handles prioritizing requests
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }


    //requests location updates when location changes
    //suppress missing permission warning b/c checks permission before calling method
    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        //checks if location changes
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //if the task is successful and not null
                if(task.isSuccessful() && task.getResult() != null) {
                    //set last location to result and logs it
                    lastLocation = task.getResult();
//                    Log.i("Last Location", lastLocation.toString());
                    updateMap();
                }
                else {
                    Log.i("Last Location", "No last location");
                }
            }
        });
    }


    //updates map marker/zoom when location changes
    public void updateMap() {
        //clears all previous markers
        mMap.clear();
        //creates LatLng object for lastLocation
        LatLng curLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        //adds marker at curLocation
        mMap.addMarker(new MarkerOptions().position(curLocation).title("Marker at current location").icon(BitmapDescriptorFactory.defaultMarker()));
        //zooms in to street level at curLocation
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 15));
    }

}
