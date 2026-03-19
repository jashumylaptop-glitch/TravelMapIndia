package com.example.travelmapindia;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editUsername, editEmail, editPassword;
    private Button btnRegister, btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editUsername = findViewById(R.id.editRegUsername);
        editEmail = findViewById(R.id.editRegEmail);
        editPassword = findViewById(R.id.editRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnRegister.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                UserDao userDao = AppDatabase.getInstance(this).userDao();
                User existingUser = userDao.getUserByEmail(email);
                
                if (existingUser != null) {
                    runOnUiThread(() -> Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show());
                } else {
                    User newUser = new User(username, email, password);
                    userDao.insert(newUser);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Registration successful. You can now login.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }).start();
        });

        btnBackToLogin.setOnClickListener(v -> finish());
    }
}