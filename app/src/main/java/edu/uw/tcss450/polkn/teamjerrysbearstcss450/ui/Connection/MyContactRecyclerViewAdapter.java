package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactFragment.OnListFragmentInteractionListener;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;

import java.util.HashMap;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Contact} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyContactRecyclerViewAdapter extends RecyclerView.Adapter<MyContactRecyclerViewAdapter.ViewHolder> {

    private final List<Contact> mValues;
    private static Context mContext;
    private final HashMap<Integer, Drawable> mDrawableIds;
    private final OnListFragmentInteractionListener mListener;

    /*public MyContactRecyclerViewAdapter(List<Contact> items, HashMap drawableIds) {*/
    public MyContactRecyclerViewAdapter(List<Contact> items, HashMap drawableIds, OnListFragmentInteractionListener listener) {
        mValues = items;
        mDrawableIds = drawableIds;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        String username = mValues.get(position).getUsername();
        String firstName = mValues.get(position).getFirstName();
        String lastName = mValues.get(position).getLastName();

        String displayString = "";
        if (username.length() > 0 && firstName.length() > 0 && lastName.length() > 0) {
            displayString = username + " (" + firstName + " " + lastName + ")";
        } else if (username.length() > 0) {
            displayString = username;
        }

        holder.mUsernameView.setText(displayString);
        Integer pos = new Integer(position);
        Drawable drawableIcon = mDrawableIds.get(pos);
        drawableIcon.setBounds(0, 0, 60, 60);
        holder.mUsernameView.setCompoundDrawables(drawableIcon, null, null, null);

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

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mUsernameView;

        public Contact mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsernameView = view.findViewById(R.id.username);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUsernameView.getText() + "'";
        }
    }
}
