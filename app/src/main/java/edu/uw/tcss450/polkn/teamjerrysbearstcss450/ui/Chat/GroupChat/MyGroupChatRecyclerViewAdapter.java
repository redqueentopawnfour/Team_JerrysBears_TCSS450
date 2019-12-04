package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupChatFragment.OnListFragmentInteractionListener;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupContact.GroupContact;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link GroupContact} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyGroupChatRecyclerViewAdapter extends RecyclerView.Adapter<MyGroupChatRecyclerViewAdapter.ViewHolder> {

    private final List<GroupContact> mValues;

    private final OnListFragmentInteractionListener mListener;


    private int mChatId;





    public MyGroupChatRecyclerViewAdapter(List<GroupContact> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_groupchat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mGroupnameView.setText(mValues.get(position).getGroupname());

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
        public final TextView mGroupnameView;
        public GroupContact mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mGroupnameView = (TextView) view.findViewById(R.id.text_groupchat_username);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mGroupnameView.getText() + "'";
        }
    }
}
