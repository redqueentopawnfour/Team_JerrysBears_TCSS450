package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactFragmentArgs;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.MyContactRecyclerViewAdapter;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ViewProfileFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;

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
    List<String> mUserNamesSelected;
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

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }


        ContactFragmentArgs args = ContactFragmentArgs.fromBundle(getArguments());

        if (args.getContact() != null) {
            mContacts = new ArrayList<>(Arrays.asList(args.getContact()));
        }
        mProfile = args.getProfile();
        mJwt = args.getJwt();
        iconDrawables = new HashMap<Integer, Drawable>();
        mUserNamesSelected = new ArrayList<String>();
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
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        Context context = recyclerView.getContext();
        viewNoContacts = view.findViewById(R.id.editView);

        if (recyclerView instanceof RecyclerView) {
            if (mContacts != null) {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                populateIconDrawables(context);
                recyclerView.setAdapter(new MyContactRecyclerViewAdapter(mContacts, mJwt,
                        iconDrawables, mProfile, this::displayContact));
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
