package com.example.eventsync_message;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton, VerifyButton;
    private EditText phoneNumberInput, verificationCodeInput;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private ProgressDialog loadingBar;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_phone_login);

        // Initialize views
        sendVerificationCodeButton = findViewById(R.id.send_verification_code_button);
        VerifyButton = findViewById(R.id.verify_button);
        phoneNumberInput = findViewById(R.id.phone_number_input);
        verificationCodeInput = findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);

        // Initialize callbacks before setting click listeners
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // Auto-detection of SMS message with code
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                phoneNumberInput.setVisibility(View.VISIBLE);
                VerifyButton.setVisibility(View.INVISIBLE);
                verificationCodeInput.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d("PhoneLogin", "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent", Toast.LENGTH_LONG).show();
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                phoneNumberInput.setVisibility(View.INVISIBLE);
                VerifyButton.setVisibility(View.VISIBLE);
                verificationCodeInput.setVisibility(View.VISIBLE);
            }
        };

        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberInput.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Phone Number required", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    // Format the phone number to include the country code
                    String formattedPhoneNumber = phoneNumberInput.getText().toString().trim();
                    if (!formattedPhoneNumber.startsWith("+")) {
                        formattedPhoneNumber = "+60" + formattedPhoneNumber.replaceFirst("^0", "");
                    }

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(formattedPhoneNumber)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(PhoneLoginActivity.this)
                                    .setCallbacks(callbacks)
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verificationCode = verificationCodeInput.getText().toString();
                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter verification code first", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("Please wait");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(PhoneLoginActivity.this, "Verification successful", Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                } else {
                    Toast.makeText(PhoneLoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
