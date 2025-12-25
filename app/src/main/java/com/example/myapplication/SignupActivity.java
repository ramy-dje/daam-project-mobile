package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.apis.AuthApi;
import com.example.myapplication.apis.RetrofitClient;
import com.example.myapplication.models.Client;
import com.example.myapplication.models.SignupRequest;
import com.example.myapplication.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set status bar background color (light color so icons are visible)
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));

            // Make status bar icons dark (black)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }
        }

        EditText firstnameInput = findViewById(R.id.firstname);
        EditText lastnameInput = findViewById(R.id.lastname);
        EditText emailInput = findViewById(R.id.email);
        EditText passwordInput = findViewById(R.id.password);
        Button signupButton = findViewById(R.id.button);
        TextView loginText = findViewById(R.id.loginButton);

        // SIGNUP BUTTON → API CALL
        signupButton.setOnClickListener(v -> {
            String firstName = firstnameInput.getText().toString().trim();
            String lastName = lastnameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Basic validation
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            signupUser(firstName, lastName, email, password);
        });

        // LOGIN TEXT → Navigate to LoginActivity
        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void signupUser(String firstName, String lastName, String email, String password) {
        AuthApi api = RetrofitClient.getInstance().create(AuthApi.class);
        SignupRequest request = new SignupRequest(firstName, lastName, email, password);

        api.signup(request).enqueue(new Callback<Client>() {
            @Override
            public void onResponse(Call<Client> call, Response<Client> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ✅ SIGNUP SUCCESS - Save client and navigate to MainActivity
                    Toast.makeText(SignupActivity.this,
                            "Welcome " + response.body().getFirstName() + "!",
                            Toast.LENGTH_SHORT).show();

                    // Save client into session
                    new SessionManager(SignupActivity.this).saveClient(response.body());

                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    // ❌ SIGNUP FAILED - Email might already exist or validation error
                    Toast.makeText(SignupActivity.this,
                            "Signup failed. Email might already exist.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Client> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(SignupActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
