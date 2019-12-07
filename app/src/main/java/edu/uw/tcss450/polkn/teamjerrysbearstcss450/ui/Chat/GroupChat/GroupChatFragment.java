package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupContact.GroupContact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.GetAsyncTask;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.PushReceiver;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class GroupChatFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private List<GroupContact> myGroupContacts;

    private List<Contact> mContacts;

    private RecyclerView recyclerView;

    private String mGroupname;
    private PushMessageReceiver mPushMessageReciever;




    private String mJwToken;
    private String mSendUrl;
    private int mChatId;
    private String mUsername;
    private String mEmail;
    private Contact mProfile;



    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupChatFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static GroupChatFragment newInstance(int columnCount) {
        GroupChatFragment fragment = new GroupChatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GroupChatFragmentArgs args = GroupChatFragmentArgs.fromBundle(getArguments());
        myGroupContacts = new ArrayList<>();
        mContacts = new ArrayList<>();
        Contact[] temp = args.getContact();
        if (args.getContact() != null){
            for (int i = 0; i<temp.length; i++) {
                mContacts.add(temp[i]);
            }
        }

        Log.d("Contact List in Group", mContacts.size() +"");

        if (args.getGroupContact() != null) {
            myGroupContacts = new ArrayList<>(Arrays.asList(args.getGroupContact()));
        }




        mProfile = args.getProfile();
        Log.d("my Profile in Group", mProfile.getUsername().toString());
        mJwToken = args.getJwt();
        mUsername = mProfile.getUsername();
        mEmail = mProfile.getEmail();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groupchat_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        Context context = recyclerView.getContext();
        if (recyclerView instanceof RecyclerView) {
            if (myGroupContacts != null) {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                recyclerView.setAdapter(new MyGroupChatRecyclerViewAdapter(myGroupContacts,mProfile,mJwToken,this::displayGroupContact));
            } else {
                recyclerView.setVisibility(View.GONE);
            }
//            recyclerView.setAdapter(new MyGroupChatRecyclerViewAdapter(DummyContent.ITEMS, mListener));
        }


        ((HomeActivity) getActivity()).hideAddUser();
        ((HomeActivity) getActivity()).hideViewProfile();
        ((HomeActivity) getActivity()).hideChatIcon();
        if (mContacts.size() == 0) {
            ((HomeActivity) getActivity()).showAddGroup();
        }
        else {
            ((HomeActivity) getActivity()).hideAddGroup();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPushMessageReciever == null) {
            mPushMessageReciever = new PushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        getActivity().registerReceiver(mPushMessageReciever,iFilter);
        loadGroupHistory();
        ((HomeActivity) getActivity()).hideAddUser();
        ((HomeActivity) getActivity()).hideViewProfile();
        ((HomeActivity) getActivity()).hideChatIcon();
        if (mContacts.size() == 0) {
            ((HomeActivity) getActivity()).showAddGroup();
        }
        else {
            ((HomeActivity) getActivity()).hideAddGroup();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReciever != null) {
            getActivity().unregisterReceiver(mPushMessageReciever);
        }
        myGroupContacts = new ArrayList<>();
    }

    private void displayGroupContact(final GroupContact theGroupContact) {

        GroupChatFragmentDirections.ActionNavGroupChatToNavChat directions = GroupChatFragmentDirections.actionNavGroupChatToNavChat();
        directions.setChatid(theGroupContact.getChatId());
        directions.setJwt(mJwToken);
        directions.setChatname(theGroupContact.getGroupname());
        directions.setUsername(mUsername);
        Navigation.findNavController(getView())
                .navigate(directions);
    }

    /**
     * Method to load chat history
     */
    private void loadGroupHistory() {
        String getUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_groupchat_getGroup))
                .build()
                .toString();
        new GetAsyncTask.Builder(getUrl).addHeaderField("username", mUsername)
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
                JSONArray messages = (JSONArray) res.get(getString(R.string.key_groupchat_chat));

                for(int i = 0; i < messages.length(); i++) {
                    JSONObject group =  (JSONObject) messages.get(i);
                    GroupContact tempGroup = new GroupContact(group.getString("name"),group.getInt("chatid"));



                    Log.d(" loaded message", tempGroup.toString());
                    myGroupContacts.add(tempGroup);

                }
                final RecyclerView.Adapter adapter = recyclerView.getAdapter();
                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        recyclerView.scrollToPosition(mMessage.size()-1); // updating all message from chat history
        Log.d("TESTING GROUP:", myGroupContacts.toString());
    }





    /**
     * A BroadcastReceiver that listens for messages sent from PushReceiver
     */
    private class PushMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("SENDER") && intent.hasExtra("MESSAGE") && intent.hasExtra("TYPE")) {
                String type = intent.getStringExtra("TYPE");
                String sender = intent.getStringExtra("SENDER");
                String messageText = intent.getStringExtra("MESSAGE");
//                final RecyclerView.Adapter adapter = recyclerView.getAdapter();
//                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

//                ((HomeActivity) getActivity()).reloadContactList();   // ????

            }
        }
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
        // TODO: Update argument type and name
        void onListFragmentInteraction(GroupContact item);
    }
}
