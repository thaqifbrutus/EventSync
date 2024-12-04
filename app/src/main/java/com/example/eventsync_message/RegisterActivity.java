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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private Button RegisterButton;

    private EditText userEmail, UserPassword;
    private TextView LoginAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        InitializeFields();

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        LoginAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CreateNewAccount();

            }
        });
    }

    private void CreateNewAccount() {

        String email = userEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter your email BRO!", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your password BRO!", Toast.LENGTH_LONG).show();
        }
        else {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Tunggu jap, we are creating your account");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password).
                    addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){

                                String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                RootRef = FirebaseDatabase.getInstance("https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();


                                RootRef.child("Users").child(currentUserID).setValue("Email");



                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error :" +message, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();

                            }

                        }
                    });


        }
    }

    private void InitializeFields() {

        RegisterButton =  (Button) findViewById(R.id.register_button);
        userEmail =  (EditText) findViewById(R.id.register_email);
        UserPassword =  (EditText) findViewById(R.id.register_password);
        LoginAccountLink =  (TextView) findViewById(R.id.already_have_account_link);

        loadingBar  = new ProgressDialog(this);

    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);

        startActivity(mainIntent);
        finish();
    }
    private void SendUserToLoginActivity(){

        Intent registerIntent = new Intent(RegisterActivity.this, Login_Activity.class);
        startActivity(registerIntent);

    }
}