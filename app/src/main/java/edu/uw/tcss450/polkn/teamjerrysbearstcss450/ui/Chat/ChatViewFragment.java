package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupContact.GroupContact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.Message.Message;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.home.HomeViewModel;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.home.MyHomeViewModelFactory;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.GetAsyncTask;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.PushReceiver;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

/**
 * The chat fragment, used for both group and single chats.
 *
 */
public class ChatViewFragment extends Fragment {


    HomeViewModel homeViewModel;
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    private List<Message> mMessage;
    private List<String> mNames;

    private static final String TAG = "CHAT_FRAG";

    private static final String CHAT_ID = "1";

    private EditText mMessageInputEditText;

    private PushMessageReceiver mPushMessageReciever;
    private RecyclerView recyclerView;

    private String mEmail;
    private String mJwToken;
    private String mSendUrl;
    private int mChatId;
    private String mUsername;

    private String mChatname;
    private View mDisplayButton;
    private View mFavorite;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatViewFragment() {
    }

    @SuppressWarnings("unused")
    public static ChatViewFragment newInstance(int columnCount) {
        ChatViewFragment fragment = new ChatViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = ViewModelProviders.of(getActivity(),
                new MyHomeViewModelFactory(mEmail,  mJwToken)).get(HomeViewModel.class);
        ChatViewFragmentArgs args = ChatViewFragmentArgs.fromBundle(getArguments());
        mUsername = args.getUsername();

        mChatname = args.getChatname();

        ((HomeActivity) getActivity())
                .setActionBarTitle(mChatname);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        mMessage = new ArrayList<Message>();


//        mMessage = new ArrayList(Arrays.asList(args.getMessage()));
        Log.d("My Length:", mMessage.size()+"");

        ((HomeActivity) getActivity()).hideAddUser();
        ((HomeActivity) getActivity()).hideViewProfile();
        ((HomeActivity) getActivity()).hideChatIcon();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatview_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        Context context = recyclerView.getContext();

        recyclerView.setAdapter(new MyChatViewRecyclerViewAdapter(mMessage, this::displayMessage, mUsername));
        ((HomeActivity)getActivity()).hideViewProfile();
        ((HomeActivity)getActivity()).hideAddUser();
        ((HomeActivity)getActivity()).hideAddGroup();
        ((HomeActivity)getActivity()).showDisplayMember();
        ((HomeActivity) getActivity()).showFavorite();
        mDisplayButton = getActivity().findViewById(R.id.action_display);
        mDisplayButton.setOnClickListener(b -> displayClicked(mNames));
        mFavorite = getActivity().findViewById(R.id.action_addFavorite);

        mFavorite.setOnClickListener(b -> favoriteClicked());
        return view;
    }

    private void displayClicked(final List<String> theNames) {
        Bundle args = new Bundle();
        args.putSerializable("Intent key for creds", //set a key at here to open the door. "Intetnt key for creds"
                new ArrayList<String>(theNames));
        Navigation.findNavController(getView()).navigate(R.id.action_nav_chat_to_contactDisplay,args); //args



    }

    private void favoriteClicked() {
        Map<Integer, GroupContact> currentFavoriteChats = homeViewModel.getFavoriteChats().getValue();
        if(currentFavoriteChats != null && currentFavoriteChats.keySet().size() > 3) {
            Toast toast = Toast.makeText(getActivity().getBaseContext(), getString(R.string.toast_chat_favoritesfull),
                    Toast.LENGTH_LONG);
            View view = toast.getView();
            view.setBackgroundResource(R.drawable.customborder_greypurple);
            toast.show();
            return;
        }
        try {
            JSONObject req = new JSONObject();
            req.put("chatid", mChatId);
            req.put("email", mEmail);
            String url = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_messaging_base))
                    .appendPath(getString(R.string.ep_messaging_addfavorite))
                    .build()
                    .toString();
            new SendPostAsyncTask.Builder(url, req).
                    addHeaderField("authorization", mJwToken).
                    onPostExecute(this::handleFavoritesOnPost).build().execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayMessage(final Message theMessage) {
        final Bundle args = new Bundle();
    }

    private void handleFavoritesOnPost(String result) {
        try {
            //This is the result from the web service
            JSONObject res = new JSONObject(result);

            if(res.has(getString(R.string.keys_json_success))  && res.getBoolean(getString(R.string.keys_json_success))) {
                    Toast toast = Toast.makeText(getActivity().getBaseContext(), R.string.toast_chat_favorited,
                            Toast.LENGTH_LONG);
                    View view = toast.getView();
                    view.setBackgroundResource(R.drawable.customborder_greypurple);
                    toast.show();
                homeViewModel.loadFavorites();
            } else if (res.has(getString(R.string.keys_json_success))) {
                Log.d("bummer man", res.getJSONObject("error").toString());
                Toast toast = Toast.makeText(getActivity().getBaseContext(), getString(R.string.toast_chat_alreadyfavorite),
                        Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.customborder_greypurple);
                toast.show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        mMessageOutputTextView = view.findViewById(R.id.text_chat_displayMessage);
        mMessageInputEditText = view.findViewById(R.id.edit_chat_message_input);
        view.findViewById(R.id.button_chat_send).setOnClickListener(this::handleSendClick);

//        ChatViewFragmentArgs args = ChatViewFragmentArgs.fromBundle(getArguments());
//        if (args.getMessage() != null) {
//            mMessageOutputTextView.append(args.getMessage().getSender());
//            mMessageOutputTextView.append(": ");
//            mMessageOutputTextView.append(args.getMessage().getMessage());
//            mMessageOutputTextView.append(System.lineSeparator());
//            mMessageOutputTextView.append(System.lineSeparator());
//        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mNames = new ArrayList<>();
        ChatViewFragmentArgs args = ChatViewFragmentArgs.fromBundle(getArguments());
        mEmail = ((HomeActivity)getActivity()).getmEmail();

        mJwToken = args.getJwt();
        mChatId = args.getChatid();
        Log.i("chat id: ", Integer.toString(mChatId));
        Log.i("username: ", mUsername);

//        if (mChatId > 0) { // 0 is the default value which means no chat id has been passed
//            mMessageOutputTextView.append("CHAT ID: " + mChatId + '\n');
//        }

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


        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("email", mEmail);
            messageJson.put("username", "Can we not pass this?");
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

            if(res.has(getString(R.string.keys_json_success))  && !res.getBoolean(getString(R.string.keys_json_success))) {
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
            int fromChatId = intent.getIntExtra("CHATID", 0);
            Log.d("from chat id", fromChatId + "");
            Log.d("get  chat id", mChatId+"");
            mMessage.add(new Message(messageText,sender));
            final RecyclerView.Adapter adapter = recyclerView.getAdapter();
            getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
        recyclerView.scrollToPosition(mMessage.size()-1);

    }
}

    /**
     * Method to load chat history
     */
    private void loadChatHistory() {
        String getUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_messaging_getall))
                .build()
                .toString();
        Log.d("chatid at load", mChatId +"");
        new GetAsyncTask.Builder(getUrl).addHeaderField("chatid", Integer.toString(mChatId))
                .addHeaderField("authorization", mJwToken)
                .onCancelled(error -> Log.e("an error", error))
                .onPostExecute(this::endOfLoadChatTask).build().execute();
//        Log.d("TESTING MESSAGE:", mMessage.toString());
    }

    private void endOfLoadChatTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);
            if (res.has(getString(R.string.keys_json_success))
                    && !res.getBoolean(getString(R.string.keys_json_success))) {
            } else {
                JSONArray messages = (JSONArray) res.get(getString(R.string.keys_json_messages));
                JSONArray names = (JSONArray) res.get("usernames");
                for(int i = 0; i < messages.length(); i++) {
                    JSONObject message =  (JSONObject) messages.get(i);
                    Message tempmessage = new Message(message.getString("message"), message.getString("username"));
                    Log.d(" loaded message", tempmessage.toString());
                    mMessage.add(tempmessage);
                }
                for (int i = 0; i < names.length();i++) {
                    JSONObject name = (JSONObject) names.get(i);
                    String tempName = name.getString("username");
                    mNames.add(tempName);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String nameChat = "";
            if (mNames.size() > 2) {
//                nameChat = "Group Chat";
                ChatViewFragmentArgs args = ChatViewFragmentArgs.fromBundle(getArguments());
                nameChat = args.getChatname();
            }
            else {
                for (int i = 0; i < mNames.size(); i++) {
                    if (!mNames.get(i).equals(mUsername)) {
                        nameChat = mNames.get(i);
                    }
                }

            }
        recyclerView.scrollToPosition(mMessage.size()-1); // updating all message from chat history
        ((HomeActivity) getActivity())
                .setActionBarTitle(nameChat);
        Log.d("TESTING MESSAGE:", mMessage.toString());
        Log.d("TESTING NAMES:", mNames.toString());

    }



    @Override
    public void onResume() {
        super.onResume();
        if (mPushMessageReciever == null) {
            mPushMessageReciever = new ChatViewFragment.PushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        getActivity().registerReceiver(mPushMessageReciever, iFilter);
        Log.i("Resume", "Resume happened");
        loadChatHistory();


//        final RecyclerView.Adapter adapter = recyclerView.getAdapter();
//        getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());


    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReciever != null){
            getActivity().unregisterReceiver(mPushMessageReciever);
        }
        Log.i("Is this happening", "happened");
        ((HomeActivity)getActivity()).hideDisplayMember();
        ((HomeActivity) getActivity()).hideFavorite();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Message item);
    }
}
