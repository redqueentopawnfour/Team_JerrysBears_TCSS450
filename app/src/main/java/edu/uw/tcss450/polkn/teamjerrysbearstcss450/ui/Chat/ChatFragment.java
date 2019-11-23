package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.spec.PSSParameterSpec;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.PushReceiver;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.model.Credentials;

public class ChatFragment extends Fragment {

    private ChatViewModel chatViewModel;


    private static final String TAG = "CHAT_FRAG";

    private static final String CHAT_ID = "3";

    private TextView mMessageOutputTextView;
    private EditText mMessageInputEditText;

    private String mUserName;
    private String mEmail;
    private String mJwToken;
    private String mSendUrl;
    private int mChatId;


    private PushMessageReceiver mPushMessageReciever;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        chatViewModel =
                ViewModelProviders.of(this).get(ChatViewModel.class);
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        final TextView textView = root.findViewById(R.id.text_chat_message_display);

        ((HomeActivity)getActivity()).hideViewProfile();
        ((HomeActivity)getActivity()).hideAddUser();

        chatViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessageOutputTextView = view.findViewById(R.id.text_chat_message_display);
        mMessageInputEditText = view.findViewById(R.id.edit_chat_message_input);
        view.findViewById(R.id.button_chat_send).setOnClickListener(this::handleSendClick);

        ChatFragmentArgs args = ChatFragmentArgs.fromBundle(getArguments());
        if (args.getMessage() != null) {
            mMessageOutputTextView.append(args.getMessage().getSender());
            mMessageOutputTextView.append(": ");
            mMessageOutputTextView.append(args.getMessage().getMessage());
            mMessageOutputTextView.append(System.lineSeparator());
            mMessageOutputTextView.append(System.lineSeparator());
        }




    }


    @Override
    public void onStart() {
        super.onStart();

        ChatFragmentArgs args = ChatFragmentArgs.fromBundle(getArguments());
        mUserName = args.getUsername();
        Credentials cred = ((HomeActivity)getActivity()).getmCredentials();
        mEmail = cred.getEmail();
        mUserName = cred.getUsername();
        mJwToken = args.getJwt();
        mChatId = args.getChatid();
        Log.i("chat id: ", Integer.toString(mChatId));

        if (mChatId > 0) { // 0 is the default value which means no chat id has been passed
            mMessageOutputTextView.append("CHAT ID: " + mChatId + '\n');
        }


        //We will use this url every time the user hits send. Let's only build it once, ya?
        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_messaging_send))
                .build()
                .toString();
    }


    private void handleSendClick(final View theButton) {
        String msg = mMessageInputEditText.getText().toString();
        mMessageInputEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mMessageInputEditText.setText("");
        Log.d("this should be the chatid", mChatId + "");
        Log.d("email? and then username?", mEmail + mUserName);
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("email", mEmail);
            messageJson.put("username", mUserName);
            messageJson.put("message", msg);
            messageJson.put("chatid", mChatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(error -> Log.e(TAG, error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();
    }

    private void endOfSendMsgTask(final String result) {
        try {
            //This is the result from the web service
            JSONObject res = new JSONObject(result);

            if(res.has("success")  && !res.getBoolean("success")) {
                //The web service got our message. Time to clear out the input EditText
                if(res.has(getString(R.string.keys_json_message))){
                    mMessageInputEditText.setError(res.getString(getString(R.string.keys_json_message)));
                }


                //its up to you to decide if you want to send the message to the output here
                //or wait for the message to come back from the web service.
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * A BroadcastReceiver that listens for messages sent from PushReceiver
     */
    private class PushMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra("SENDER") && intent.hasExtra("MESSAGE")) {

                String sender = intent.getStringExtra("SENDER");
                String messageText = intent.getStringExtra("MESSAGE");

                mMessageOutputTextView.append(sender + ":" + messageText);
                mMessageOutputTextView.append(System.lineSeparator());
                mMessageOutputTextView.append(System.lineSeparator());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPushMessageReciever == null) {
            mPushMessageReciever = new PushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        getActivity().registerReceiver(mPushMessageReciever, iFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReciever != null){
            getActivity().unregisterReceiver(mPushMessageReciever);
        }
    }

}