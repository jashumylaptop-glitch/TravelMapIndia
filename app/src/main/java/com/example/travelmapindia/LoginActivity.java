package com.example.travelmapindia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editPassword;
    private Button btnLogin, btnGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        if (prefs.contains("userId")) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    User user = AppDatabase.getInstance(this).userDao().login(email, password);
                    runOnUiThread(() -> {
                        if (user != null) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("userId", user.getId());
                            editor.putString("userName", user.getUsername());
                            editor.putString("userEmail", user.getEmail());
                            // Store the profile image URI if it exists in DB
                            if (user.getProfileImageUri() != null) {
                                editor.putString("profileImageUri", user.getProfileImageUri());
                            }
                            editor.apply();

                            Toast.makeText(this, "Welcome " + user.getUsername(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e("LoginActivity", "Login error", e);
                    runOnUiThread(() -> Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}