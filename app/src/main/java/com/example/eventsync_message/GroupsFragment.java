package com.example.eventsync_message;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupsFragment extends Fragment {

    private View groupFragmentView;
    private ListView listview;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups = new ArrayList<>();

    private DatabaseReference GroupRef;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GroupsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupsFragment newInstance(String param1, String param2) {
        GroupsFragment fragment = new GroupsFragment();
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
        groupFragmentView =  inflater.inflate(R.layout.fragment_groups, container, false);

        InitializedFields();

        GroupRef = FirebaseDatabase.getInstance("https://eventsync-firebase-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Groups");

        RetrieveAndDisplayGroups();

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String currentGroupName = parent.getItemAtPosition(position).toString();
                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                startActivity(groupChatIntent);

            }
        });

        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1, list_of_groups);
        listview.setAdapter(arrayAdapter);

        return groupFragmentView;
    }


    private void InitializedFields() {

        // Initialize ListView
        listview = groupFragmentView.findViewById(R.id.list_view);

        // Initialize the ArrayAdapter only if it's not initialized yet
        if (arrayAdapter == null) {
            arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, list_of_groups);
            listview.setAdapter(arrayAdapter);
        }

        // Ensure that the adapter is properly updated if needed
        arrayAdapter.notifyDataSetChanged();
    }

    private void RetrieveAndDisplayGroups() {
        Log.d("RetrieveGroups", "Fetching data from 'Groups' node...");
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("RetrieveGroups", "Number of Groups: " + snapshot.getChildrenCount());

                if (snapshot.exists()) {
                    Set<String> set = new HashSet<>();
                    for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                        set.add(groupSnapshot.getKey());  // Adds group names to set
                    }

                    // Clear previous data and add new groups
                    list_of_groups.clear();
                    list_of_groups.addAll(set);

                    Log.d("GroupsList", "Groups: " + list_of_groups);

                    // Notify the adapter on the main thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                arrayAdapter.notifyDataSetChanged();  // Refresh the ListView
                            }
                        });
                    }
                } else {
                    Log.d("RetrieveGroups", "No groups found in the database.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RetrieveGroups", "Failed to read groups: " + error.getMessage());
            }
        });
    }
}