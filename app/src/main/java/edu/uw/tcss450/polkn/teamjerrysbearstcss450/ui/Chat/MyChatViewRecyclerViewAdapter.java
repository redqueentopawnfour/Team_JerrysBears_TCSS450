package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatViewFragment.OnListFragmentInteractionListener;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.Message.Message;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Message} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyChatViewRecyclerViewAdapter extends RecyclerView.Adapter<MyChatViewRecyclerViewAdapter.ViewHolder> {

    private final List<Message> mValues;
    private int mChatId;
    private String mUsername;

    private final OnListFragmentInteractionListener mListener;

    public MyChatViewRecyclerViewAdapter(List<Message> items, OnListFragmentInteractionListener listener, String userName) {
        mValues = items;
        mListener = listener;
        mUsername = userName;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_chatview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mUsernameView.setText(mValues.get(position).getUsername());
//        Log.d("My Values", mValues.get(position).getMessage());
        holder.mMessageView.setText(mValues.get(position).getMessage());
        Log.d("my adapter user", mUsername);
        Log.d("my message user", mValues.get(position).getUsername());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 5, 0, 5);
        if(holder.mItem.getUsername().equals(mUsername)) {
            Log.d("my user", mUsername);

            layoutParams.gravity = Gravity.END;
            holder.mCardView.setLayoutParams(layoutParams);
        } else {
            layoutParams.gravity = Gravity.START;
            holder.mCardView.setLayoutParams(layoutParams);

        }
        Log.i("chat id: ", Integer.toString(mChatId));

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
        public final TextView mMessageView;
        public Message mItem;
        public LinearLayout mLinearLayoutChat;
        public final CardView mCardView;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsernameView = (TextView) view.findViewById(R.id.text_chat_username);
            mMessageView = (TextView) view.findViewById(R.id.text_chat_displayMessage);
//            mLinearLayoutChat = view.findViewById(R.id.layout_chat_list);
            mCardView = view.findViewById(R.id.chat_message_cardview);
            Log.d("view type", view.getClass().toString());
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mMessageView.getText() + "'";
        }
    }
}
