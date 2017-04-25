package com.sjsu.caregivergeofencesample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sjsu.caregivergeofencesample.model.GeoFenceDetail;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.angrybyte.numberpicker.view.ActualNumberPicker;

import static com.sjsu.caregivergeofencesample.R.id.map;

public class GeofenceSelectorActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        LocationListener,
        GoogleMap.OnCameraIdleListener,
        PlaceSelectionListener{


    //TODO Change the models and fix the location enabling settings.
    private final String TAG = getClass().getSimpleName();
    private static final int MY_LOCATION_REQUEST_CODE = 1;

    private PlaceAutocompleteFragment autocompleteFragment;
    private GoogleMap googleMap;
    private Circle radiusCircle;

    /**
     * UI Elements for accessing the map
     */
    GoogleApiClient apiClient;
    private Location lastLocation;
    private Geocoder geocoder;

    //Final Details to be submitted
    private GeoFenceDetail detailToSubmit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence_selector);

        //detailToSubmit = new GeoFenceDetail();

        detailToSubmit = getIntent().getParcelableExtra(MainActivity.SELECTED_NAME_KEY);
        Log.d(TAG, "Name received is : "+detailToSubmit.getFirstName()+" "+
                detailToSubmit.getLastName());
        lastLocation = new Location("currLocation");
        lastLocation.setLatitude(0);
        lastLocation.setLongitude(0);
        geocoder = new Geocoder(this, Locale.getDefault());


        /*
        UI elements
        */
        ActualNumberPicker radiusPicker = (ActualNumberPicker) findViewById(R.id.actual_picker);
        detailToSubmit.setRadius(radiusPicker.getValue());
        radiusPicker.setListener((oldValue, newValue) -> {
            Log.d(TAG, "New value is : "+newValue);
            detailToSubmit.setRadius(newValue);
            if(radiusCircle != null)
                radiusCircle.setRadius(newValue);
        });


        autocompleteFragment =  (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete);

        RequestQueue queue = Volley.newRequestQueue(this);

        //Set button and onClickListener
        Button submitBtn = (Button)findViewById(R.id.submitButton);
        submitBtn.setOnClickListener(v -> {
            //Make Volley request
            String url = getResources().getString(R.string.service_post_url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT,
                    String.format("%s/%s", url, detailToSubmit.getId()),
                    detailToSubmit.getJson(),
                    response -> {
                        try {
                            Log.d(TAG, String.format("Saved Detail as follows : %s",
                                    response.toString(2)));
                            //Show Toast
                            Toast.makeText(GeofenceSelectorActivity.this.getApplicationContext(),
                                    String.format("Saved Detail as follows : %%s%s",
                                            response.toString(2)),
                                    Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> Log.e(TAG, "Error on sending request : "+ error.getMessage())){

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json");
                    return headers;
                }
            };

            queue.add(request);

        });


        //TODO plot location on map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
    }

    protected synchronized void buildGoogleApiClient() {
        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        apiClient.connect();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
            if(lastLocation != null){
                Log.d(TAG, "Lat : "+lastLocation.getLatitude()+" Long : "+
                        lastLocation.getLongitude());
            }
        } catch (SecurityException e){
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        apiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onStart() {
        if(apiClient == null)
            buildGoogleApiClient();
        apiClient.connect();
        super.onStart();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorMessage());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection suspended.");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    googleMap.setMyLocationEnabled(true);
                }catch (SecurityException e){
                    Log.e(TAG, e.getMessage());
                }
            }
        } else {
            // Permission was denied. Display an error message.
            Log.e(TAG, "Permission was denied");
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                try {
                    new AlertDialog.Builder(this)
                            .setTitle("Location Permission Needed")
                            .setMessage("This app needs the Location permission, please accept " +
                                    "to use location functionality")
                            .setPositiveButton("OK", (dialogInterface, i) -> {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(GeofenceSelectorActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_LOCATION_REQUEST_CODE );
                            })
                            .create()
                            .show();


                } catch (Exception e){
                    Log.e(TAG, e.getMessage());
                }


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_LOCATION_REQUEST_CODE );
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                //Request Location Permission
                enableMyLocation();
            }
        }

        buildGoogleApiClient();
        positionMyLocationButton();
        this.googleMap.setOnMyLocationButtonClickListener(this);
        this.googleMap.setOnCameraIdleListener(this);
        this.googleMap.setMyLocationEnabled(true);
        //Tell the auto complete text field to listen for location changes
        autocompleteFragment.setOnPlaceSelectedListener(this);

        //Add radius circle
        CameraPosition position= this.googleMap.getCameraPosition();
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center( new LatLng(position.target.latitude,
                position.target.longitude));
        circleOptions.fillColor(0x5500ff00);
        //circleOptions.fillColor(Color.GREEN);
        circleOptions.strokeWidth(1);
        circleOptions.radius(detailToSubmit.getRadius());
        radiusCircle = this.googleMap.addCircle(circleOptions);


    }

    @Override
    public void onLocationChanged(Location location) {
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //Add current Location marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.
                defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        googleMap.addMarker(markerOptions);
    }

    @Override
    public void onCameraIdle() {
        Log.d(TAG, "Camera stopped moving");
        CameraPosition currPosition = googleMap.getCameraPosition();

        //googleMap.clear();

        radiusCircle.setCenter(new LatLng(currPosition.target.latitude,
                currPosition.target.longitude));
        detailToSubmit.setLat(currPosition.target.latitude);
        detailToSubmit.setLng(currPosition.target.longitude);

        //googleMap.addCircle(circleOptions);
        Log.d(TAG, "Circle added with radius" + detailToSubmit.getRadius());

        //Set address in autocomplete text
        try {
            List<Address> geoCodedPlaces = geocoder.getFromLocation(currPosition.target.latitude,
                    currPosition.target.longitude, 1);
            if(!geoCodedPlaces.isEmpty()){
                autocompleteFragment.setText(geoCodedPlaces.get(0).getAddressLine(0));
                Log.d(TAG, "Geolocated position : " + geocoder.getFromLocation(
                        currPosition.target.latitude, currPosition.target.longitude, 1).get(0)
                        .getAddressLine(0));
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onPlaceSelected(Place place) {
        // TODO: Move camera to selected place
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
        Log.i(TAG, "Place: " + place.getName());
    }

    @Override
    public void onError(Status status) {
        // TODO: Handle the error.
        Log.i(TAG, "An error occurred: " + status);
    }

    /**
     * Sets the My Location button at the right bottom of the map.
     */
    public void positionMyLocationButton(){
        //Set position of My Location button
        if (findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) findViewById(Integer.parseInt("1")).getParent())
                    .findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 60, 60);
        }
    }


   /* @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, String.format(" Touch event occurred %d", event.getAction()));
        if(v.getId() == R.id.mapView){
            switch (event.getAction()){
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG,"Updating center marker");
                    CameraPosition currPosition = googleMap.getCameraPosition();
                    googleMap.clear();
                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(new LatLng(currPosition.target.latitude,
                            currPosition.target.longitude));
                    circleOptions.fillColor(0x5500ff00);
                    circleOptions.radius(detailToSubmit.getRadius());

                    googleMap.addCircle(circleOptions);

                    break;
            }
            return true;
        }
        return false;
    }
*/


   /* @Override
    public void onCameraMove() {
        if(googleMap != null){
            CameraPosition position = googleMap.getCameraPosition();
            currLatLng = null;
            currLatLng = new LatLng(position.target.latitude, position.target.longitude);
            //centerMarker.setPosition(currLatLng);
            radiusCircle.setCenter(currLatLng);
            detailToSubmit.setLat(position.target.latitude);
            detailToSubmit.setLng(position.target.longitude);

            googleMap.clear();
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(new LatLng(position.target.latitude,
                    position.target.longitude));
            circleOptions.fillColor(0x5500ff00);
            circleOptions.radius(detailToSubmit.getRadius());

            radiusCircle.setCenter(new LatLng(position.target.latitude,
                            position.target.longitude));

            //googleMap.addCircle(circleOptions);

            //googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            Log.d(TAG, "Curr position changed to : "+position.target.latitude+
                    " and "+position.target.longitude);
        }
    }
    */


}
