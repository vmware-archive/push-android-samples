package io.pivotal.android.push.sample.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.pivotal.android.push.geofence.GeofenceRegistrar;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.service.PushService;
import io.pivotal.android.push.util.Logger;

public class GeofenceActivity extends FragmentActivity {

    private static final long LOCATION_ITERATION_PAUSE_TIME = 1000L;
    private static final int NUMBER_OF_LOCATION_ITERATIONS = 10;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm aa", Locale.getDefault());

    private GoogleMap map; // Might be null if Google Play services APK is not available.
    private LatLngBounds latLngBounds = null;
    private UpdateLocationRunnable updateLocationRunnable;
    private LocationManager locationManager;
    private Marker locationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        registerReceiver(geofenceUpdateBroadcastReceiver, new IntentFilter(GeofenceRegistrar.GEOFENCE_UPDATE_BROADCAST));
        registerReceiver(geofenceEnterBroadcastReceiver, new IntentFilter(PushService.GEOFENCE_ENTER_BROADCAST));
        registerReceiver(geofenceExitBroadcastReceiver, new IntentFilter(PushService.GEOFENCE_EXIT_BROADCAST));

        Logger.i("Setup mock location providers");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Logger.i("GPS provider");
        locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, true, false, false, false, false, false, Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

        Logger.i("Network provider");
        locationManager.addTestProvider(LocationManager.NETWORK_PROVIDER, true, false, true, false, false, false, false, Criteria.POWER_MEDIUM, Criteria.ACCURACY_FINE);
        locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(geofenceUpdateBroadcastReceiver);
        unregisterReceiver(geofenceEnterBroadcastReceiver);
        unregisterReceiver(geofenceExitBroadcastReceiver);

        if (updateLocationRunnable != null) {
            updateLocationRunnable.interrupt();
        }

        Logger.i("Cleanup our fields");
        locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
        locationManager = null;
        updateLocationRunnable = null;
        map = null;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);
        map.setOnMapLoadedCallback(onMapLoadedCallback);
        map.setOnMapClickListener(onMapClickListener);
        map.clear();
        if (Pivotal.getGeofencesEnabled(this)) {
            final List<Map<String, String>> geofences = loadGeofences();
            final LatLngBounds.Builder builder = LatLngBounds.builder();
            if (geofences != null && !geofences.isEmpty()) {
                addGeofences(geofences, builder);
                latLngBounds = builder.build();
            }
        } else {
            Toast.makeText(this, "Note that geofences are currently disabled.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addGeofences(List<Map<String, String>> geofences, LatLngBounds.Builder builder) {
        for (final Map<String, String> geofence : geofences) {
            if (geofence != null) {
                final double latitude = Double.parseDouble(geofence.get("lat"));
                final double longitude = Double.parseDouble(geofence.get("long"));
                final float radius = Float.parseFloat(geofence.get("rad"));
                final String name = geofence.get("name");
                final Date expiry = new Date(Long.parseLong(geofence.get("expiry")));
                final LatLng point = new LatLng(latitude, longitude);

                builder.include(point);

                final CircleOptions circleOptions = new CircleOptions()
                        .center(point)
                        .radius(radius)
                        .strokeWidth(1.0f)
                        .fillColor(Color.argb(0x55, 0x00, 0x00, 0xff));
                final MarkerOptions markerOptions = new MarkerOptions()
                        .alpha(0.5f)
                        .position(point)
                        .draggable(false)
                        .title(name)
                        .snippet("Expires " + dateFormat.format(expiry));
                map.addMarker(markerOptions);
                map.addCircle(circleOptions);
            }
        }
    }

    private List<Map<String, String>> loadGeofences() {

        final File externalFilesDir = getExternalFilesDir(null);
        if (externalFilesDir == null) {
            Logger.e(getString(R.string.external_files_dir_error));
            return new ArrayList<>();
        }
        final File dir = new File(externalFilesDir.getAbsolutePath() + File.separator + "pushlib");
        final File file = new File(dir, "geofences.json");
        if (!file.exists() || !file.canRead()) {
            Logger.i(String.format(getString(R.string.read_file_error), "geofences", file.getAbsoluteFile()));
            return null;
        }
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            final Type type = new TypeToken<List<Map<String, String>>>(){}.getType();
            return new Gson().fromJson(fr, type);

        } catch (Exception e) {
            Logger.e(String.format(getString(R.string.error_reading_file), "geofences", file.getAbsoluteFile()));
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // Swallow exception
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    // Swallow exception
                }
            }
        }
    }

    private final BroadcastReceiver geofenceUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Logger.i("Received geofence update.");
                    setUpMap();
                }
            });
        }
    };


    private final BroadcastReceiver geofenceEnterBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(GeofenceActivity.this, getString(R.string.geofence_entered, intent.getStringExtra("message")), Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private final BroadcastReceiver geofenceExitBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(GeofenceActivity.this, getString(R.string.geofence_exited, intent.getStringExtra("message")), Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private final GoogleMap.OnMapLoadedCallback onMapLoadedCallback = new GoogleMap.OnMapLoadedCallback() {
        @Override
        public void onMapLoaded() {
            if (map != null && latLngBounds != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 80));
            }
        }
    };

    private final GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {

        @Override
        public void onMapClick(LatLng point) {

            if (updateLocationRunnable != null && updateLocationRunnable.isAlive() && !updateLocationRunnable.isInterrupted()) {
                updateLocationRunnable.interrupt();
            }
            updateLocationRunnable = new UpdateLocationRunnable(locationManager, point);
            updateLocationRunnable.start();

            if (locationMarker != null) {
                locationMarker.remove();
                locationMarker = null;
            }

            final MarkerOptions marker = new MarkerOptions()
                    .position(point)
                    .draggable(false)
                    .title("My location");
            locationMarker = map.addMarker(marker);

        }
    };

    private class UpdateLocationRunnable extends Thread {

        private final LocationManager locationManager;
        private final LatLng point;
        Location mockGpsLocation;
        Location mockNetworkLocation;

        UpdateLocationRunnable(LocationManager locationManager, LatLng point) {
            this.locationManager = locationManager;
            this.point = point;
        }

        @Override
        public void run() {
            try {
                Logger.i(String.format("Setting Mock Location to: %1$s, %2$s", point.latitude, point.longitude));

                for (int i = 0; !isInterrupted() && i <= NUMBER_OF_LOCATION_ITERATIONS; i++) {
                    if (locationManager != null) {
                        mockGpsLocation = createMockLocation(LocationManager.GPS_PROVIDER, point.latitude, point.longitude);
                        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockGpsLocation);
                        mockNetworkLocation = createMockLocation(LocationManager.NETWORK_PROVIDER, point.latitude, point.longitude);
                        locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, mockNetworkLocation);
                    }
                    Thread.sleep(LOCATION_ITERATION_PAUSE_TIME);
                }
            } catch (InterruptedException e) {
                Logger.i("Interrupted.");
                // Do nothing.  We expect this to happen when location is successfully updated.
            } finally {
                Logger.i("Done moving location.");
            }
        }
    }

    private Location createMockLocation(String locationProvider, double latitude, double longitude) {
        final Location location = new Location(locationProvider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(1.0f);
        location.setTime(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        try {
            final Method locationJellyBeanFixMethod = Location.class.getMethod("makeComplete");
            if (locationJellyBeanFixMethod != null) {
                locationJellyBeanFixMethod.invoke(location);
            }
        } catch (Exception e) {
            // There's no action to take here.  This is a fix for Jelly Bean and no reason to report a failure.
        }

        return location;
    }
}
