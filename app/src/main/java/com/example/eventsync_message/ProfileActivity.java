package com.example.eventsync_message;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID, Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendRequestButton, DeclineMessageButton;

    private DatabaseReference usersRef, ChatRequestRef, ContactsRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase reference
        usersRef = FirebaseDatabase.getInstance("https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance("https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Chat Request");
        ContactsRef = FirebaseDatabase.getInstance("https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Contacts");


        // Retrieve user ID from intent
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        Toast.makeText(this, "User ID: " + receiverUserID, Toast.LENGTH_SHORT).show();

        // Initialize UI elements
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendRequestButton = findViewById(R.id.send_message_request_button);
        DeclineMessageButton = findViewById(R.id.decline_message_request_button);
        Current_State = "new";

        // Retrieve user info from Firebase
        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        usersRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    String profileStatus = snapshot.child("status").getValue(String.class);

                    // Update UI
                    userProfileName.setText(userName);
                    userProfileStatus.setText(profileStatus);

                    ManageChatRequest();

                } else {
                    Toast.makeText(ProfileActivity.this, "User not found.", Toast.LENGTH_SHORT).show();

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ManageChatRequest() {

        ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChild(receiverUserID)){

                    String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){
                        Current_State = "request_sent";
                        sendRequestButton.setText("Cancel Chat Request");
                    }

                    else if(request_type.equals("received")){
                        Current_State = "request_received";
                        sendRequestButton.setText("Accept Chat Request");

                        DeclineMessageButton.setVisibility(View.VISIBLE);
                        DeclineMessageButton.setEnabled(true);

                        DeclineMessageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CancelChatRequest();

                            }
                        });


                    }
                }
                else{
                    ContactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.hasChild(receiverUserID)){

                                Current_State = "friends";
                                sendRequestButton.setText("Remove this Contact");


                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!senderUserID.equals(receiverUserID)){

            sendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    sendRequestButton.setEnabled(false);

                    if (Current_State.equals("new")){

                        SendChatRequest();
                    }

                    if (Current_State.equals("request_sent")){

                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received")){

                        AcceptChatRequest();
                    }

                    if(Current_State.equals("friends")){
                        RemoveContact();
                    }

                }
            });

        }
        else{


            sendRequestButton.setVisibility(View.INVISIBLE);

        }
    }

    private void RemoveContact() {

        ContactsRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    ContactsRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                sendRequestButton.setEnabled(true);
                                Current_State = "new";
                                sendRequestButton.setText("Send Message");
                                Toast.makeText(ProfileActivity.this, "Chat Request Cancelled", Toast.LENGTH_SHORT).show();

                                DeclineMessageButton.setVisibility(View.INVISIBLE);
                                DeclineMessageButton.setEnabled(false);
                            }

                        }
                    });
                }

            }
        }); ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    ChatRequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                sendRequestButton.setEnabled(true);
                                Current_State = "new";
                                sendRequestButton.setText("Send Message");
                                Toast.makeText(ProfileActivity.this, "Chat Request Cancelled", Toast.LENGTH_SHORT).show();

                                DeclineMessageButton.setVisibility(View.INVISIBLE);
                                DeclineMessageButton.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });



    }

    private void AcceptChatRequest() {

        ContactsRef.child(senderUserID).child(receiverUserID).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    ContactsRef.child(receiverUserID).child(senderUserID).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {



                                            if(task.isSuccessful()){
                                                ChatRequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        sendRequestButton.setEnabled(true);
                                                        Current_State = "friends";
                                                        sendRequestButton.setText("Remove this Contact");

                                                        DeclineMessageButton.setVisibility(View.INVISIBLE);
                                                        DeclineMessageButton.setEnabled(false);



                                                    }
                                                });



                                    }
                                }
                                });
                            }

                        }
                    });


                }

            }
        });




    }

    private void CancelChatRequest() {

        ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    ChatRequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                sendRequestButton.setEnabled(true);
                                Current_State = "new";
                                sendRequestButton.setText("Send Message");
                                Toast.makeText(ProfileActivity.this, "Chat Request Cancelled", Toast.LENGTH_SHORT).show();

                                DeclineMessageButton.setVisibility(View.INVISIBLE);
                                DeclineMessageButton.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });
    }

    private void SendChatRequest() {

        ChatRequestRef.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    ChatRequestRef.child(receiverUserID).child(senderUserID).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                sendRequestButton.setEnabled(true);
                                Current_State = "request_sent";
                                sendRequestButton.setText("Cancel Chat Request");
                                Toast.makeText(ProfileActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }

            }
        });
    }
}
