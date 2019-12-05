package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat;

import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupContactFragment.OnListFragmentInteractionListener;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactFragment;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MyGroupContactRecyclerViewAdapter extends RecyclerView.Adapter<MyGroupContactRecyclerViewAdapter.ViewHolder> {

    private final List<Contact> mValues;
    private final OnListFragmentInteractionListener mListener;


    private final HashMap<Integer, Drawable> mDrawableIds;
    private Contact mMyProfile;
    private View mView;
    private String mJwt;
    private int mCount;
    private Set<String> mUsernamesSelected;

    public MyGroupContactRecyclerViewAdapter(List<Contact> items, String theJwt, HashMap drawableIds,
                                             Contact myProfile, OnListFragmentInteractionListener listener, Set<String> usernames) {
        mValues = items;
        mDrawableIds = drawableIds;
        mListener = listener;
        mMyProfile = myProfile;
        mJwt = theJwt;
        mCount = items.size();
        mUsernamesSelected = usernames;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_groupcontact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        String username = mValues.get(position).getUsername();
        String firstName = mValues.get(position).getFirstName();
        String lastName = mValues.get(position).getLastName();

        Boolean contactVerified = mValues.get(position).getIsContactVerified();
        Log.i("contact verified", Boolean.toString(contactVerified));

        String displayString = username;
        holder.mUsernameView.setText(displayString);
        Integer pos = new Integer(position);
        Drawable drawableIcon = mDrawableIds.get(pos);
        drawableIcon.setBounds(0, 0, 120, 120);
        holder.mUsernameView.setCompoundDrawables(drawableIcon, null, null, null);
        holder.mCheckBox.setOnClickListener(b -> contactSelected(b, username));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    private void contactSelected(View view, String username) {
        CheckBox cb = (CheckBox) view;
        if (cb.isChecked()) {
            mUsernamesSelected.add(username);
            Log.i("usernames selected on select", Arrays.deepToString(mUsernamesSelected.toArray()));
        } else {
            mUsernamesSelected.remove(username);
            Log.i("usernames selected on deselect", Arrays.deepToString(mUsernamesSelected.toArray()));
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mUsernameView;
        public final TextView mNewContactView;
        public final LinearLayout mLinearLayoutContact;
        public final CheckBox mCheckBox;

        public Contact mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsernameView = view.findViewById(R.id.text_groupcontact_displayMessage);
            mNewContactView = view.findViewById(R.id.text_groupcontact_username);
            mCheckBox = view.findViewById(R.id.checkBox_groupcontact_select);
            mLinearLayoutContact = view.findViewById(R.id.linearlayout_groupcontact_contact);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUsernameView.getText() + "'";
        }
    }

}
