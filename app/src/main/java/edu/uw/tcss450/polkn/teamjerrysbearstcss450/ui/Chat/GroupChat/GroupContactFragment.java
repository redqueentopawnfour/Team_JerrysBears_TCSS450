package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MobileNavigationDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactFragmentArgs;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.MyContactRecyclerViewAdapter;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ViewProfileFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class GroupContactFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<Contact> mContacts;
    private HashMap iconDrawables;
    private Contact mProfile;
    private String mJwt;
    private RecyclerView recyclerView;
    private View viewNoContacts;
    private Button mCreateChat;
    private EditText mGroupNameInput;
    private Set<String> mUserNamesSelected;
    private int mChatId; //only used when navigating away to pass between chained async tasks
    private String mChatName;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupContactFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static GroupContactFragment newInstance(int columnCount) {
        GroupContactFragment fragment = new GroupContactFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        GroupContactFragmentArgs args = GroupContactFragmentArgs.fromBundle(getArguments());

        if (args.getContacts() != null) {
            mContacts = new ArrayList<>(Arrays.asList(args.getContacts()));
        }
        mProfile = args.getProfile();
        mJwt = args.getJwt();
        iconDrawables = new HashMap<Integer, Drawable>();
        mUserNamesSelected = new HashSet<String>();
    }
    private void displayContact(final Contact theContact) {
        final Bundle args = new Bundle();

        MobileNavigationDirections.ActionGlobalViewProfileFragment directions
                = ViewProfileFragmentDirections.actionGlobalViewProfileFragment(theContact);

        Navigation.findNavController(getView())
                .navigate(directions);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groupcontact_list, container, false);
        recyclerView = view.findViewById(R.id.list_groupcontacts_contacts);
        Context context = recyclerView.getContext();
        mCreateChat = view.findViewById(R.id.button_createGroup_createButton);
        mGroupNameInput = view.findViewById(R.id.editText_createGroup);
        mCreateChat.setOnClickListener(b -> createClicked());
        viewNoContacts = view.findViewById(R.id.layout_groupcontacts_noconnections);
        Log.d("the contacts on load groupcontact", Arrays.deepToString(mContacts.toArray()));
        if (recyclerView != null) {
            if (mContacts != null) {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                populateIconDrawables(context);
                recyclerView.setAdapter(new MyGroupContactRecyclerViewAdapter(mContacts, mJwt,
                        iconDrawables, mProfile, this::displayContact, mUserNamesSelected));
            } else {
                viewNoContacts.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }

        }
        ((HomeActivity) getActivity()).hideAddUser();
        ((HomeActivity) getActivity()).hideViewProfile();
        ((HomeActivity) getActivity()).hideChatIcon();
        ((HomeActivity) getActivity()).hideAddGroup();
        return view;
    }

    private void createClicked() {
        if (mGroupNameInput.getText().length() == 0) {
            mGroupNameInput.setError("Please enter a name for your group!");
            return;
        }

        String mChatName = mGroupNameInput.getText().toString();
        mGroupNameInput.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mGroupNameInput.setText("");
        JSONObject createChatJSON = new JSONObject();
        try {
            createChatJSON.put("name", mChatName);
            createChatJSON.put("email", ((HomeActivity)getActivity()).getmEmail());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_messaging_creategroup))
                .build()
                .toString();
        new SendPostAsyncTask.Builder(url, createChatJSON).onPostExecute(this::endOfCreateChatTask).addHeaderField("authorization", mJwt).build().execute();
    }

    private void endOfCreateChatTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);
            if(res.has(getString(R.string.keys_json_success))) {
                if(res.getBoolean(getString(R.string.keys_json_success))) {
                    mChatId = res.getInt(getString(R.string.keys_chat_id));
                    String url = new Uri.Builder()
                            .scheme("https")
                            .appendPath(getString(R.string.ep_base_url))
                            .appendPath(getString(R.string.ep_messaging_base))
                            .appendPath(getString(R.string.ep_messaging_addgroupmembers))
                            .build()
                            .toString();
                    JSONObject toSend = new JSONObject();
                    JSONArray usernamesSelected = new JSONArray(mUserNamesSelected.toArray());
                    toSend.put("usernames", usernamesSelected);
                    toSend.put("chatid", mChatId);
                    new SendPostAsyncTask.Builder(url, toSend).
                            onPostExecute(this::endOfAddMembersTask).
                            addHeaderField("authorization", mJwt).build().execute();
                } else {
                    Log.d("error on chat build", res.get("error").toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void endOfAddMembersTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);
            if (res.has("success")) {
                if(res.getBoolean("success")) {
                    Log.d("members added successfully", mUserNamesSelected.toString());
                    GroupContactFragmentDirections.ActionNavGroupContactsToNavChat directions
                            = GroupContactFragmentDirections.actionNavGroupContactsToNavChat();
                    directions.setJwt(mJwt);
                    directions.setChatid(mChatId);
                    directions.setChatname(mChatName);
                    NavController nc = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    nc.navigate(directions);
                } else {
                    Log.d("didn't add members successfully", "or dummy dumb dumb error");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateIconDrawables(Context context) {
        for (int i = 0; i < mContacts.size(); i++) {
            String userIcon = mContacts.get(i).getUserIcon();
            Log.i("usericon", userIcon);
            int drawableId = context.getResources().getIdentifier(userIcon, "drawable", context.getPackageName());
            Drawable drawableIcon = ResourcesCompat.getDrawable(getResources(), drawableId, null);
            iconDrawables.put(i, drawableIcon);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        void onListFragmentInteraction(Contact item);
    }
}
