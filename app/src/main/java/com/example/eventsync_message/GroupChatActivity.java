package com.example.eventsync_message;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;

import com.example.eventsync_message.databinding.ActivityGroupChatBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class GroupChatActivity extends AppCompatActivity {

    private ActivityGroupChatBinding binding;

    private Toolbar mtoolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameReference, GroupMessageKeyRef;
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve group name and display it in a Toast message
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance("https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Users");
        GroupNameReference = FirebaseDatabase.getInstance("https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Groups");

        // Use binding to inflate layout and set as the content view
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        // Initialize UI elements
        InitializedFields();

        GetUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        // Setup the toolbar as the action bar
        setSupportActionBar(binding.toolbar);

        // Floating action button action
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        DatabaseReference groupMessagesRef = GroupNameReference.child(currentGroupName);

        groupMessagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()){

                    DisplayMessages(snapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()){

                    DisplayMessages(snapshot);
                }


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(GroupChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void DisplayMessages(DataSnapshot snapshot) {

        Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()) {


            // Use safe checks and retrieve values as strings
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            // Check if any value is null and skip if so
            if (chatDate != null && chatMessage != null && chatName != null && chatTime != null) {
                displayTextMessage.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "       " + chatDate + "\n\n\n");
            }

        }
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    //Save Message to Database
    private void SaveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = GroupNameReference.child(currentGroupName).push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
        } else {
            // Get the current date and time
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            // Create a reference to the specific group and message key
            GroupMessageKeyRef = GroupNameReference.child(currentGroupName).child(messageKey);

            // Create a map with message details to update the database
            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);

            GroupMessageKeyRef.updateChildren(messageInfoMap);

        }
    }

    //Get User Information
    private void GetUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    currentUserName = snapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }



    private void InitializedFields() {
        // Set up toolbar and set the group name as title
        mtoolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentGroupName);

        // Initialize input fields and buttons
        SendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        displayTextMessage = findViewById(R.id.group_chat_text_display);
        mScrollView = findViewById(R.id.my_scroll_view);

        // Initialize the displayTextMessage TextView with an empty string
        displayTextMessage.setText("");
    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }


}
