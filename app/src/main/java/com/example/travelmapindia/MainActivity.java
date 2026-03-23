package com.example.travelmapindia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private LinearLayout profileDropdown;
    private LinearLayout btnProfile;
    private LinearLayout btnLogout;
    private TextView drawerUserName, drawerUserEmail;
    private TextView headerUserName, headerUserEmail;
    private ShapeableImageView headerProfileImage;
    private ImageView profileArrow;
    private boolean isProfileExpanded = false;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    saveProfileImage(uri);
                    loadProfileImage(uri.toString());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);

        // Custom Drawer Views
        profileDropdown = findViewById(R.id.profile_dropdown);
        btnProfile = findViewById(R.id.btn_drawer_profile);
        btnLogout = findViewById(R.id.btn_drawer_logout);
        drawerUserName = findViewById(R.id.drawer_user_name);
        drawerUserEmail = findViewById(R.id.drawer_user_email);
        profileArrow = findViewById(R.id.profile_arrow);

        // Header Views
        headerProfileImage = findViewById(R.id.header_profile_image);
        headerUserName = findViewById(R.id.header_user_name);
        headerUserEmail = findViewById(R.id.header_user_email);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navView, navController);
        }

        setupDrawerActions();
        loadUserData();
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String name = prefs.getString("userName", "User");
        String email = prefs.getString("userEmail", "Not available");
        String imageUri = prefs.getString("profileImageUri", null);

        headerUserName.setText(name);
        headerUserEmail.setText(email);
        
        if (imageUri != null) {
            loadProfileImage(imageUri);
        }
    }

    private void setupDrawerActions() {
        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        
        btnProfile.setOnClickListener(v -> {
            isProfileExpanded = !isProfileExpanded;
            if (isProfileExpanded) {
                String name = prefs.getString("userName", "User");
                String email = prefs.getString("userEmail", "Not available");
                drawerUserName.setText("Name: " + name);
                drawerUserEmail.setText("Email: " + email);
                profileDropdown.setVisibility(View.VISIBLE);
                profileArrow.setImageResource(android.R.drawable.arrow_up_float);
            } else {
                profileDropdown.setVisibility(View.GONE);
                profileArrow.setImageResource(android.R.drawable.arrow_down_float);
            }
        });

        headerProfileImage.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
        
        // Other dummy actions for drawer items
        findViewById(R.id.btn_drawer_settings).setOnClickListener(v -> drawerLayout.closeDrawer(Gravity.RIGHT));
        findViewById(R.id.btn_drawer_about).setOnClickListener(v -> drawerLayout.closeDrawer(Gravity.RIGHT));
    }

    private void saveProfileImage(Uri uri) {
        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        
        if (userId != -1) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("profileImageUri", uri.toString());
            editor.apply();

            // Persist to Database
            new Thread(() -> {
                UserDao userDao = AppDatabase.getInstance(this).userDao();
                User user = userDao.getUserById(userId);
                if (user != null) {
                    user.setProfileImageUri(uri.toString());
                    userDao.update(user);
                }
            }).start();
        }
        
        // Take persistable URI permission if needed
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfileImage(String uriString) {
        Glide.with(this)
                .load(Uri.parse(uriString))
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .into(headerProfileImage);
    }

    private void showLogoutDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (d, which) -> {
                    SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Style buttons
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.primary));
        }
        if (negativeButton != null) {
            negativeButton.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }
    }

    public void openMenu() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(Gravity.RIGHT);
        }
    }
}