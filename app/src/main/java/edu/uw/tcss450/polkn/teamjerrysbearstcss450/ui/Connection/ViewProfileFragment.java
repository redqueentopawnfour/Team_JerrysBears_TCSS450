package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MainActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.GetAsyncTask;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewProfileFragment extends Fragment {

    private Contact mUser;
    private Context mContext;
    private View mChatIcon;

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
        mChatIcon = (getActivity()).findViewById(R.id.action_chat);
        mChatIcon.setOnClickListener(this::onChatClicked);

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
        Log.d("viewing profile", mUser.getEmail().toString());
        if (isEmailVerified == true) {
            isEmailVerifiedView.setVisibility(View.VISIBLE);
        }


    }


    private void handleOnPre(){
        mChatIcon.setEnabled(false);
    }
    private void handleCancel(String cancel){
        mChatIcon.setEnabled(true);
    }


    private void handleOnPost(final String result) {
        ViewProfileFragmentDirections.ActionViewProfileFragmentToNavChat directions = ViewProfileFragmentDirections.actionViewProfileFragmentToNavChat();
        Navigation.findNavController(getView()).navigate(directions);
        ((MenuItem) mChatIcon).setVisible(false);
    }
    private void onChatClicked(final View chatButton) {
        String uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_messaging_getall))
                .build()
                .toString();
        String myEmail = ((HomeActivity) getActivity()).getmMyEmail();
        String theirEmail = mUser.getEmail();
        String[] emails = {myEmail, mUser.getEmail()};
        Arrays.sort(emails);
        int chatId = Objects.hash(emails);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(getString(R.string.keys_chat_id), chatId);
            jsonObject.put(getString(R.string.keys_chat_theiremail), theirEmail);
            jsonObject.put(getString(R.string.keys_chat_myemail), myEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(uri, jsonObject)
                .onPreExecute(this::handleOnPre)
                .onPostExecute(this::handleOnPost).onCancelled(this::handleCancel).build().execute();
    }
}
