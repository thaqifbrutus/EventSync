package com.example.eventsync_message;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login_Activity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private Button LoginButton, phoneLoginButton;
    private EditText userEmail, UserPassword;
    private TextView SignUpAccountLink, ForgetPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Ensure the system bars don't overlap the layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        InitializeFields();

        // Set onClick listeners
        SignUpAccountLink.setOnClickListener(v -> SendUserToRegisterActivity());

        LoginButton.setOnClickListener(v -> AllowUserToLogin());

        phoneLoginButton.setOnClickListener(v -> {
            Intent phoneLoginIntent = new Intent(Login_Activity.this, PhoneLoginActivity.class);
            startActivity(phoneLoginIntent);
        });
    }

    private void AllowUserToLogin() {
        String email = userEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email!", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password!", Toast.LENGTH_LONG).show();
        } else {
            loadingBar.setTitle("Logging in");
            loadingBar.setMessage("We'll verify your credentials.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            SendUserToMainActivity();
                            Toast.makeText(Login_Activity.this, "Login Successful!", Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(Login_Activity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                    });
        }
    }

    private void InitializeFields() {
        // Initialize all UI elements
        LoginButton = findViewById(R.id.login_button);
        phoneLoginButton = findViewById(R.id.phone_login_button);
        userEmail = findViewById(R.id.login_email);
        UserPassword = findViewById(R.id.login_password);
        SignUpAccountLink = findViewById(R.id.create_account_link);
        ForgetPasswordLink = findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(Login_Activity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(Login_Activity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
