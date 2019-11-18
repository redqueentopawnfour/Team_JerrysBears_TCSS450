package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MainActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewProfileFragment extends Fragment {

    private Contact mUser;
    private Context mContext;

    public ViewProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_view_profile, container, false);
        mContext = v.getContext();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get "start chat" icon
        // add on click listener to it
        updateProfile();
        ((HomeActivity)getActivity()).showChatIcon(mUser);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewProfileFragmentArgs args = ViewProfileFragmentArgs.fromBundle(getArguments());
        mUser = args.getContact();
        Log.i("user", mUser.toString());
    }

    private void updateProfile() {
        TextView firstLastView = getActivity().findViewById(R.id.textView_viewProfile_firstLast);
        TextView emailView = getActivity().findViewById(R.id.textView_viewProfile_email);
        TextView usernameView = getActivity().findViewById(R.id.textView_viewProfile_username);
        ImageView userIconView = getActivity().findViewById(R.id.image_viewProfile_usericon);
        ImageView isEmailVerifiedView = getActivity().findViewById(R.id.imageView_viewProfile_isVerified);
        int drawableId = mContext.getResources().getIdentifier(mUser.getUserIcon(), "drawable", mContext.getPackageName());
        Boolean isEmailVerified = mUser.getIsEmailVerified();

        firstLastView.setText(mUser.getFirstName() + " " + mUser.getLastName());
        emailView.setText(mUser.getEmail());
        usernameView.setText(mUser.getUsername());
        userIconView.setImageResource(drawableId);

       /* if (isEmailVerified == true) {
            isEmailVerifiedView.setVisibility(View.VISIBLE);
        }*/
    }
}
