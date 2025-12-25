package com.example.myapplication; // Replace with your actual package name

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myapplication.R;
import com.example.myapplication.StoresActivity;
import com.example.myapplication.models.Client;
import com.example.myapplication.session.SessionManager;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private LinearLayout navDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE); // white background

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.WHITE); // white background

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    getWindow().getInsetsController().setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        }

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        navDrawer = findViewById(R.id.nav_drawer);

        // Populate drawer header with client info from SessionManager
        TextView drawerName = findViewById(R.id.drawer_name);
        TextView drawerEmail = findViewById(R.id.drawer_email);
        Client client = new SessionManager(this).getClient();
        if (client != null) {
            if (client.getFirstName() != null && client.getLastName() != null) {
                drawerName.setText(client.getFirstName() + " " + client.getLastName());
            }
            if (client.getEmail() != null) {
                drawerEmail.setText(client.getEmail());
            }
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Request location permissions
        requestLocationPermissions();
        
        // Check if location is enabled
        checkLocationEnabled();

        // Setup menu icon click listener
        findViewById(R.id.menu_icon).setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navDrawer)) {
                drawerLayout.closeDrawer(navDrawer);
            } else {
                drawerLayout.openDrawer(navDrawer);
            }
        });

        // Setup navigation item click listeners
        setupNavigationListeners();
    }

    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 
                100);
        }
    }
    
    private void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        
        if (!isGpsEnabled && !isNetworkEnabled) {
            showLocationSettingsDialog();
        }
    }
    
    private void showLocationSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable Location")
                .setMessage("Location services are disabled. Would you like to enable them?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Open location settings
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .setCancelable(false)
                .show();
    }

    private void setupNavigationListeners() {
        // Home click listener
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            // Already on home, just close drawer
            drawerLayout.closeDrawer(navDrawer);
            showToast("Home clicked");
        });

        // Stores click listener - Navigate to Stores activity
        findViewById(R.id.nav_stores).setOnClickListener(v -> {
            drawerLayout.closeDrawer(navDrawer);
            navigateToStores();
        });

        // Profile click listener
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            drawerLayout.closeDrawer(navDrawer);
            showToast("Profile clicked");
            // Add your profile navigation logic here
        });

        // Settings click listener
        findViewById(R.id.nav_settings).setOnClickListener(v -> {
            drawerLayout.closeDrawer(navDrawer);
            showToast("Settings clicked");
            // Add your settings navigation logic here
        });

        // Logout click listener
        findViewById(R.id.nav_logout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(navDrawer);
            showLogoutDialog();
        });
    }

    private void navigateToStores() {
        // Create an intent to navigate to StoresActivity
        // Make sure you have StoresActivity created
        try {
            Intent intent = new Intent(MainActivity.this, StoresActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showToast("Stores feature coming soon!");
            // For now, just show a toast if StoresActivity doesn't exist
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear session
                    new SessionManager(this).clear();
                    // Navigate to login screen
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // CORRECTED showToast method - removed incorrect parameter
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navDrawer)) {
            drawerLayout.closeDrawer(navDrawer);
        } else {
            super.onBackPressed();
        }
    }
}