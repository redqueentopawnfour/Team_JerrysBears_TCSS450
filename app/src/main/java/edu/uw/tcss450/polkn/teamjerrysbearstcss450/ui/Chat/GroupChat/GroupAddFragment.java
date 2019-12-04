package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupAddFragment extends Fragment {


    public GroupAddFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_add, container, false);
    }

}
