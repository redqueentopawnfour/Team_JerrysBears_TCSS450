package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MobileNavigationDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.PushReceiver;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ContactFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<Contact> mContacts;
    private HashMap iconDrawables;
    private Contact mProfile;
    private PushMessageReceiver mPushMessageReciever;
    private String mJwt;
    private RecyclerView recyclerView;
    private View viewNoContacts;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactFragment() {
    }

    @SuppressWarnings("unused")
    public static ContactFragment newInstance(int columnCount) {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContactFragmentArgs args = ContactFragmentArgs.fromBundle(getArguments());

        if (args.getContact() != null) {
            mContacts = new ArrayList<>(Arrays.asList(args.getContact()));
        }
        mProfile = args.getProfile();
        mJwt = args.getJwt();
        iconDrawables = new HashMap<Integer, Drawable>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        Context context = recyclerView.getContext();
        viewNoContacts = view.findViewById(R.id.editView);

        if (recyclerView instanceof RecyclerView) {
            if (mContacts != null) {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                populateIconDrawables(context);
                recyclerView.setAdapter(new MyContactRecyclerViewAdapter(mContacts, mJwt, iconDrawables, mProfile, this::displayContact));
            } else {
                viewNoContacts.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }

        }

        ((HomeActivity) getActivity()).showAddUser();
        ((HomeActivity) getActivity()).showViewProfile();
        ((HomeActivity) getActivity()).hideChatIcon();

        return view;
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
        if (mPushMessageReciever != null) {
            getActivity().unregisterReceiver(mPushMessageReciever);
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

    private void displayContact(final Contact theContact) {
        final Bundle args = new Bundle();

        MobileNavigationDirections.ActionGlobalViewProfileFragment directions
                = ViewProfileFragmentDirections.actionGlobalViewProfileFragment(theContact);

        Navigation.findNavController(getView())
                .navigate(directions);
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

                ((HomeActivity) getActivity()).reloadContactList();
            }
        }
    }

    public void showNoContacts() {
        viewNoContacts.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
}
