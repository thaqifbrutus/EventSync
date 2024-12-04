package com.example.eventsync_message;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private Toolbar SettingsToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance(" https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();



        InitializeFields();



        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");

                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });




    }





    private void InitializeFields() {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        userStatus = (EditText)  findViewById(R.id.set_profile_status);
        userName = (EditText) findViewById(R.id.set_user_name);

        SettingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please input your username", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, " Please set up your user status", Toast.LENGTH_LONG).show();
        }
        else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_LONG).show();

                    }
                    else {
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_LONG).show();
                    }
                }
            });



        }

    }

    private void RetrieveUserInfo() {

        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if ((snapshot.exists() && snapshot.hasChild("name") && (snapshot.hasChild("image")))){
                    
                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveStatus = snapshot.child("status").getValue().toString();
                    String retrieveProfileImage = snapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);



                }
                else if ((snapshot.exists()) && (snapshot.hasChild("name"))){

                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveStatus = snapshot.child("status").getValue().toString();


                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                    
                }
                else {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please Set Up & Update Your Profile Information", Toast.LENGTH_LONG).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);

        startActivity(mainIntent);
        finish();
    }
}