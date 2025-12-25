package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myapplication.models.Client;
import com.example.myapplication.session.SessionManager;

public class StoresActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private LinearLayout navDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stores_activity);
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

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // Hide the action bar title so only the toolbar content (menu icon) shows
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Drawer
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

        // Menu icon opens/closes drawer
        View menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navDrawer)) drawerLayout.closeDrawer(navDrawer);
            else drawerLayout.openDrawer(navDrawer);
        });
        
        // Setup drawer navigation listeners
        setupDrawerNavigation();

        // Wire ViewPager2 + TabLayout in layout
        androidx.viewpager2.widget.ViewPager2 viewPager = findViewById(R.id.view_pager);
        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tab_layout);

        // Adapter provides the two fragments
        StoresPagerAdapter adapter = new StoresPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Set offscreen page limit to keep fragments alive
        viewPager.setOffscreenPageLimit(2);

        // Link TabLayout and ViewPager2
        new com.google.android.material.tabs.TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) tab.setText("Maps"); else tab.setText("Products");
                }).attach();
    }

    private void setupDrawerNavigation() {
        // Home navigation
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            drawerLayout.closeDrawer(navDrawer);
            Intent intent = new Intent(StoresActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        
        // Stores - already here, just close drawer
        findViewById(R.id.nav_stores).setOnClickListener(v -> {
            drawerLayout.closeDrawer(navDrawer);
        });
        
        // Logout
        findViewById(R.id.nav_logout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(navDrawer);
            showLogoutDialog();
        });
    }
    
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear session
                    new SessionManager(this).clear();
                    Intent intent = new Intent(StoresActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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