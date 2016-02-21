package com.example.derek.whereabouts;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;
    protected static final String TAG = "whereabouts-app";
    protected LocationRequest mLocationRequest;
    static protected HashMap<String, Marker> markerList = new HashMap<String, Marker>();
    static public HashMap<String, Integer> drawableMap = new HashMap<String, Integer>();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (drawableMap.isEmpty()) {
            initializeDrawableMap();
        }
        setContentView(R.layout.activity_maps);

        buildGoogleApiClient();
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if ( ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 12);
        }
        mMap.setMyLocationEnabled(true);

        LatLng yourLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yourLatLng, 16.0f));

        class JSONAsyncTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                // Get chat history
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://ec2-54-165-233-14.compute-1.amazonaws.com:3000/rooms/" + getIntent().getStringExtra("ROOM_NAME"));
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setChunkedStreamingMode(0);

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    StringBuilder result = new StringBuilder();
                    while((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    JSONObject jsonObj = new JSONObject(result.toString());

                    JSONArray jsonArray = (JSONArray)jsonObj.get("locationHistory");
                    if (jsonArray != null) {
                        int len = jsonArray.length();
                        for (int i=0;i<len;i++){
                            JSONObject jsonLocation = (JSONObject) jsonArray.get(i);
                            String username = jsonLocation.get("username").toString();
                            LatLng location = new LatLng(Double.parseDouble(jsonLocation.get("latitude").toString()),
                                    Double.parseDouble(jsonLocation.get("longitude").toString()));
                            if (markerList.get(username) == null && !username.equals(getIntent().getStringExtra("USERNAME"))) {
                                MarkerOptions newMarkerOps = new MarkerOptions().position(location).title(username)
                                        .icon(BitmapDescriptorFactory.fromResource(drawableMap.get(username.charAt(0))));
                                markerList.put(username, mMap.addMarker(newMarkerOps));
                            }
                            else {
                                Marker marker = markerList.get(username);
                                MapsActivity.animateMarker(marker, location, false);
                            }
                        }
                    }

                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        JSONAsyncTask task = new JSONAsyncTask();
        task.execute();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if ( ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 12);
        }
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mCurrentLocation != null) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "no location detected", Toast.LENGTH_LONG).show();
        }
        startLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        LatLng yourLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        JSONObject data = new JSONObject();
        try {
            Intent intent = getIntent();
            data.put("username",intent.getStringExtra("USERNAME"));
            data.put("room", intent.getStringExtra("ROOM_NAME"));
            data.put("latitude", mCurrentLocation.getLatitude());
            data.put("longitude", mCurrentLocation.getLongitude());
            ChatRoomActivityFragment.mSocket.emit("update", data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void startLocationUpdates() {
        if ( ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 12);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public static void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    public static void initializeDrawableMap() {
        drawableMap.put("a", R.drawable.a);
        drawableMap.put("b", R.drawable.b);
        drawableMap.put("c", R.drawable.c);
        drawableMap.put("d", R.drawable.d);
        drawableMap.put("e", R.drawable.e);
        drawableMap.put("f", R.drawable.f);
        drawableMap.put("g", R.drawable.g);
        drawableMap.put("h", R.drawable.h);
        drawableMap.put("i", R.drawable.i);
        drawableMap.put("j", R.drawable.j);
        drawableMap.put("k", R.drawable.k);
        drawableMap.put("l", R.drawable.l);
        drawableMap.put("m", R.drawable.m);
        drawableMap.put("n", R.drawable.n);
        drawableMap.put("o", R.drawable.o);
        drawableMap.put("p", R.drawable.p);
        drawableMap.put("q", R.drawable.q);
        drawableMap.put("r", R.drawable.r);
        drawableMap.put("s", R.drawable.s);
        drawableMap.put("t", R.drawable.t);
        drawableMap.put("u", R.drawable.u);
        drawableMap.put("v", R.drawable.v);
        drawableMap.put("w", R.drawable.w);
        drawableMap.put("x", R.drawable.x);
        drawableMap.put("y", R.drawable.y);
        drawableMap.put("z", R.drawable.z);
    }

    //returns distance in miles between two coordinates
    public double distance(double lat1, double lon1, double lat2, double lon2) {
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLong = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.sin(deltaLong / 2) *
                Math.sin(deltaLong / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return 3959 * c;
    }
}
