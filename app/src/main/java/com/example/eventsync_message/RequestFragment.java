package com.example.eventsync_message;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestFragment extends Fragment {

    private View RequestFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference RequestRef, UserRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String Current_UserID;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public RequestFragment() {
        // Required empty public constructor
    }

    public static RequestFragment newInstance(String param1, String param2) {
        RequestFragment fragment = new RequestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        Current_UserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance("https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Users");
        RequestRef = FirebaseDatabase.getInstance("https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Chat Request");
        ContactsRef = FirebaseDatabase.getInstance("https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Contacts");

        myRequestList = RequestFragmentView.findViewById(R.id.chat_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(RequestRef.child(Current_UserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Contacts model) {

                holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                final String list_user_id = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("request_type");
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String type = snapshot.getValue().toString();
                            if (type.equals("received")) {

                                UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (snapshot.exists()) {


                                            final String requestUserName = snapshot.child("name").getValue().toString();
                                            final String requestUserStatus = snapshot.child("status").getValue().toString();

                                            holder.userName.setText(requestUserName);
                                            holder.userStatus.setText(requestUserStatus);


                                            holder.itemView.setOnClickListener(new View.OnClickListener() {


                                                @Override
                                                public void onClick(View v) {

                                                    CharSequence options[] = new CharSequence[]{

                                                            "Accept",
                                                            "Cancel"

                                                    };
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle(requestUserName + "Chat Request");

                                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            if (which == 0) {

                                                                ContactsRef.child(Current_UserID).child(list_user_id).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if(task.isSuccessful()){

                                                                            ContactsRef.child(list_user_id).child(Current_UserID).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful()){

                                                                                        RequestRef.child(Current_UserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                if(task.isSuccessful()){

                                                                                                    RequestRef.child(Current_UserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                                            if (task.isSuccessful()){

                                                                                                                RequestRef.child(list_user_id).child(Current_UserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                                        if (task.isSuccessful()){
                                                                                                                            Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();
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

                                                                                }
                                                                            });



                                                                        }

                                                                    }
                                                                });

                                                            }

                                                            if (which == 1) {

                                                                RequestRef.child(Current_UserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if(task.isSuccessful()){

                                                                            RequestRef.child(Current_UserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful()){

                                                                                        RequestRef.child(list_user_id).child(Current_UserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), "Contact Deleted ", Toast.LENGTH_SHORT).show();
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

                                                        }
                                                    });

                                                    builder.show();
                                                }

                                            });


                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new RequestViewHolder(view);
            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptRequestButton, declineRequestButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptRequestButton = itemView.findViewById(R.id.request_accept_button);
            declineRequestButton = itemView.findViewById(R.id.request_cancel_button);
        }
    }
}