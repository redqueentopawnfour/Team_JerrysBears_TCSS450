package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.contactDisplay;


import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.Message.Message;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class contactDisplay extends Fragment {

    private List<String> mContacts;

    private TextView mMemeberOutput;


    public contactDisplay() {
        // Required empty public constructor
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_display, container, false);



        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

//        TextView test = view.findViewById(R.id.textView_email);

        Bundle args = getArguments();


        if (args != null) {
            mContacts = (List) args.getSerializable("Intent key for creds"); //wrong key
            mMemeberOutput = (TextView) view.findViewById(R.id.text_groupchat_username);
            for (int i = 0; i < mContacts.size(); i++) {
                mMemeberOutput.setText(mContacts.get(i)+"\n");
            }

        }
    }



}
