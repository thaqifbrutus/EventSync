package com.example.eventsync_message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TaskAccessorAdapter extends FragmentPagerAdapter {

    public TaskAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ChatFragment();
            case 1:
                return new ContactsFragment();
            case 2:
                return new GroupsFragment();
            case 3 :
                return new RequestFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;  // Number of tabs
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Contacts";
            case 2:
                return "Groups";
            case 3 :
                return "Requests";
            default:
                return null;
        }
    }
}
