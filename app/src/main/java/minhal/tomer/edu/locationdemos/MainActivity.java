package minhal.tomer.edu.locationdemos;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    private static final int REQUEST_LOCATION = 10;
    private EditText etSearch;
    //Used later for location Requests.
    private GoogleApiClient mApiClient;
    private TextView tvLocation;
    private GoogleMap mMap;
    private BroadcastReceiver mLocationAddressResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        etSearch = (EditText) findViewById(R.id.etSearch);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoMaps();
            }
        });

        mLocationAddressResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LatLng latLng = intent.getParcelableExtra(Constants.EXTRA_LOCATION);
                String address = intent.getStringExtra(Constants.EXTRA_LOCATION_ADDRESS);

                if (mMap != null) {
                    mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                }
            }
        };

        requestLocation();
        if (savedInstanceState == null) {
            SupportMapFragment mapFragment = new SupportMapFragment();

            mapFragment.getMapAsync(this);
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.mapContainer, mapFragment).commit();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter(Constants.ACTION_LOCATION_RESULT);
        manager.registerReceiver(mLocationAddressResultReceiver, filter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.unregisterReceiver(mLocationAddressResultReceiver);
    }

    private void gotoMaps() {
        Uri uri = Uri.parse("geo:47.6,-122.3?z=11");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        LatLng markerPosition = new LatLng(31.252371, 34.8025847);

        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        map.addMarker(new MarkerOptions().position(markerPosition)
                .title("Minhal").
                        snippet("Android Course").
                        icon(BitmapDescriptorFactory.
                                fromResource(R.mipmap.ic_launcher))
        );

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 12));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    //Method that requires Permission
    public void requestLocation() {
        //if no permission -> Request it and return away from the method:
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }


        //we have the permission, Start Initing the API Client
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this);
        mApiClient = builder.build();

    }

    public void onConnected(@Nullable Bundle bundle) {
        //Connected to the api... May start using the api
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
       /* Location lastLocation = LocationServices.FusedLocationApi.
                getLastLocation(mApiClient);

        if (lastLocation != null) {
            tvLocation.setText(String.format("%s, %s",
                    lastLocation.getLatitude(),
                    lastLocation.getLongitude()));
        }

        */

        addGeofence();

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(1000);

        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient,
                locationRequest,
                this);


    }

    public void onConnectionSuspended(int i) {
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        LatLng latLng = new LatLng(latitude, longitude);
        //Update the map
       /* if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(latLng).title("Lehavim").snippet("WayPoint"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        }*/
        tvLocation.setText(String.format("%s, %s",
                longitude, latitude));

        //We have the location:
        //We want to Reverse geoCode the LatLng to an address:

        Intent geoIntent = new Intent(this, MyGeoCodingService.class);
        //Send paramteres with the intent
        geoIntent.putExtra(Constants.EXTRA_LOCATION, latLng);
        //start the service with the intent.
        startService(geoIntent);
    }

    private void addGeofence() {
        Geofence.Builder builder = new Geofence.Builder();
        builder.setCircularRegion(31.3690897, 34.8044, 100);
        builder.setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
                //Geofence.GEOFENCE_TRANSITION_DWELL /*Dwell*/
                );
        //Dwell:
        //builder.setLoiteringDelay(1000);
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE);

        builder.setRequestId("RequestLocation");

        Geofence geofence = builder.build();

        GeofencingRequest geofencingRequest =
                new GeofencingRequest.Builder().addGeofence(geofence)
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).
                        build();

        Intent geoFenceIntent = new Intent(this, MyGeoFenceService.class);
        PendingIntent pendingIntent =
                PendingIntent.getService(this,
                        Constants.REQUEST_GEOFENCE, geoFenceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.GeofencingApi.addGeofences(mApiClient, geofencingRequest, pendingIntent)
        .setResultCallback(this);
    }

    public void search(View view) {
        final String address = etSearch.getText().toString();

        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Geocoder geocoder = new Geocoder(MainActivity.this);
                try {
                    List<Address> locations = geocoder.getFromLocationName(address, 1);
                    if (locations != null && locations.size() > 0) {
                        Address addr = locations.get(0);
                        double longitude = addr.getLongitude();
                        double latitude = addr.getLatitude();

                        LatLng latLng = new LatLng(latitude, longitude);
                        //Update the UI!!!
                        return latLng;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                LatLng latLng = (LatLng) o;
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                }
            }
        };
        asyncTask.execute();
    }

    @Override
    public void onResult(@NonNull Status status) {

    }
}
