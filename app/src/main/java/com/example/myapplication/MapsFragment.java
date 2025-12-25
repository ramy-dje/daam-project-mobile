package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapsFragment extends Fragment {
    
    private static final String TAG = "MapsFragment";
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private MyLocationNewOverlay myLocationOverlay;
    private Spinner storeSpinner;
    private GeoPoint userLocation;
    private Polyline routeLine;
    private Map<String, GeoPoint> storeLocations;
    private Map<String, Marker> storeMarkers;
    private ExecutorService executorService;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Configure osmdroid
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        
        mapView = view.findViewById(R.id.map);
        storeSpinner = view.findViewById(R.id.storeSpinner);
        
        executorService = Executors.newSingleThreadExecutor();
        
        initializeStoreLocations();
        setupMap();
        setupStoreSpinner();
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        setupLocationCallback();
        getUserLocation();
        
        return view;
    }
    
    private void initializeStoreLocations() {
        storeLocations = new HashMap<>();
        storeMarkers = new HashMap<>();
        
        // 3 stores in Constantine, Algeria
        storeLocations.put("Store 1 - Centre Ville", new GeoPoint(36.3650, 6.6147));
        storeLocations.put("Store 2 - Zouaghi", new GeoPoint(36.3500, 6.6000));
        storeLocations.put("Store 3 - Ali Mendjeli", new GeoPoint(36.2800, 6.5500));
    }
    
    private void setupStoreSpinner() {
        List<String> storeNames = new ArrayList<>();
        storeNames.add("Select a store");
        storeNames.addAll(storeLocations.keySet());
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            storeNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        storeSpinner.setAdapter(adapter);
        
        storeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedStore = storeNames.get(position);
                    drawRouteToStore(selectedStore);
                } else {
                    clearRoute();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                clearRoute();
            }
        });
    }
    
    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
        
        // Add my location overlay
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
        
        // Add store markers
        addStoreMarkers();
    }
    
    private void addStoreMarkers() {
        for (Map.Entry<String, GeoPoint> entry : storeLocations.entrySet()) {
            Marker marker = new Marker(mapView);
            marker.setPosition(entry.getValue());
            marker.setTitle(entry.getKey());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
            storeMarkers.put(entry.getKey(), marker);
        }
    }
    
    private void drawRouteToStore(String storeName) {
        if (userLocation == null) {
            Toast.makeText(requireContext(), "User location not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        clearRoute();
        
        GeoPoint storeLocation = storeLocations.get(storeName);
        if (storeLocation == null) {
            return;
        }
        
        // Fetch route from OSRM API in background thread
        executorService.execute(() -> {
            try {
                List<GeoPoint> routePoints = getRouteFromOSRM(userLocation, storeLocation);
                
                // Update UI on main thread
                requireActivity().runOnUiThread(() -> {
                    if (routePoints != null && routePoints.size() > 0) {
                        drawRoute(routePoints, storeLocation);
                    } else {
                        // Fallback to straight line if routing fails
                        List<GeoPoint> fallbackPoints = new ArrayList<>();
                        fallbackPoints.add(userLocation);
                        fallbackPoints.add(storeLocation);
                        drawRoute(fallbackPoints, storeLocation);
                        Toast.makeText(requireContext(), "Using direct path", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching route: " + e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error loading route", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private List<GeoPoint> getRouteFromOSRM(GeoPoint start, GeoPoint end) {
        List<GeoPoint> routePoints = new ArrayList<>();
        
        try {
            String urlString = String.format(
                "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                start.getLongitude(), start.getLatitude(),
                end.getLongitude(), end.getLatitude()
            );
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray routes = jsonResponse.getJSONArray("routes");
                
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject geometry = route.getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    
                    // Convert coordinates to GeoPoints
                    for (int i = 0; i < coordinates.length(); i++) {
                        JSONArray coord = coordinates.getJSONArray(i);
                        double lon = coord.getDouble(0);
                        double lat = coord.getDouble(1);
                        routePoints.add(new GeoPoint(lat, lon));
                    }
                }
            }
            conn.disconnect();
            
        } catch (Exception e) {
            Log.e(TAG, "OSRM API error: " + e.getMessage());
        }
        
        return routePoints;
    }
    
    private void drawRoute(List<GeoPoint> points, GeoPoint storeLocation) {
        // Create polyline (route line)
        routeLine = new Polyline();
        routeLine.setColor(Color.BLUE);
        routeLine.setWidth(8f);
        routeLine.setPoints(points);
        
        mapView.getOverlays().add(routeLine);
        mapView.invalidate();
        
        // Zoom to show both points
        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = Double.MIN_VALUE;
        
        for (GeoPoint point : points) {
            minLat = Math.min(minLat, point.getLatitude());
            maxLat = Math.max(maxLat, point.getLatitude());
            minLon = Math.min(minLon, point.getLongitude());
            maxLon = Math.max(maxLon, point.getLongitude());
        }
        
        double centerLat = (minLat + maxLat) / 2;
        double centerLon = (minLon + maxLon) / 2;
        
        mapView.getController().setCenter(new GeoPoint(centerLat, centerLon));
    }
    
    private void clearRoute() {
        if (routeLine != null) {
            mapView.getOverlays().remove(routeLine);
            routeLine = null;
            mapView.invalidate();
        }
    }
    
    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                    updateMapLocation(location.getLatitude(), location.getLongitude());
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };
    }
    
    private void updateMapLocation(double latitude, double longitude) {
        userLocation = new GeoPoint(latitude, longitude);
        GeoPoint startPoint = new GeoPoint(latitude, longitude);
        mapView.getController().setCenter(startPoint);
    }
    
    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        updateMapLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        requestLocationUpdates();
                    }
                });
        } else {
            // Default location Constantine, Algeria
            userLocation = new GeoPoint(36.3650, 6.6147);
            mapView.getController().setCenter(userLocation);
        }
    }
    
    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();
                
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
