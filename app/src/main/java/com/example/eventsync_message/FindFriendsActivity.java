package com.example.eventsync_message;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventsync_message.databinding.ActivityFindFriendsBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private ActivityFindFriendsBinding binding;
    private Toolbar mToolbar;
    private RecyclerView findFriendsRecyclerList;
    private DatabaseReference usersRef; // Reference to the Firebase Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityFindFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar
        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Find Friends");
        }

        // Initialize Firebase Database reference
        usersRef = FirebaseDatabase
                .getInstance("https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference()
                .child("Users");

        // Setup RecyclerView
        findFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        // Floating Action Button (FAB) Action
        binding.fab.setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show()
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Setup FirebaseRecyclerOptions
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(usersRef, Contacts.class)
                        .build();

        // Initialize FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        // Inflate the user display layout
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.users_display_layout, parent, false);
                        return new FindFriendsViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull Contacts model) {
                        // Bind the Contacts data to the ViewHolder
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());

                        // Set click listener
                        holder.itemView.setOnClickListener(v -> {
                            int currentPosition = holder.getAdapterPosition(); // Get current position
                            if (currentPosition != RecyclerView.NO_POSITION) { // Check for valid position
                                String visitUserId = getRef(currentPosition).getKey(); // Get user ID

                                // Start the ProfileActivity
                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visitUserId);
                                startActivity(profileIntent);
                            }
                        });
                    }
                };

        // Attach the adapter to the RecyclerView
        findFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    // ViewHolder class for RecyclerView items
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
