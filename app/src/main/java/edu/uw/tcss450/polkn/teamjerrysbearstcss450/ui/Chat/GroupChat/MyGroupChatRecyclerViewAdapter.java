package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupChatFragment.OnListFragmentInteractionListener;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupContact.GroupContact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link GroupContact} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyGroupChatRecyclerViewAdapter extends RecyclerView.Adapter<MyGroupChatRecyclerViewAdapter.ViewHolder> {

    private final List<GroupContact> mValues;

    private final OnListFragmentInteractionListener mListener;

    private Contact mMyProfile;
    private View mView;
    private int mChatId;
    private int mCount;
    private String mJwToken;





    public MyGroupChatRecyclerViewAdapter(List<GroupContact> items, Contact theProfile, String theJwToken, OnListFragmentInteractionListener listener) {
        mValues = items;
        mMyProfile = theProfile;
        mListener = listener;
        mCount = items.size();
        mJwToken = theJwToken;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_groupchat, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log.d("HAHA1", mValues.toString());
        holder.mItem = mValues.get(position);
        Log.d("HAHA2", mValues.get(position).toString());
        holder.mGroupnameView.setText(mValues.get(position).getGroupname());
        Log.d("GroupName", holder.mGroupnameView.getText().toString());
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
        holder.mLeaveButton.setVisibility(View.VISIBLE);
        holder.mLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("Username:", mMyProfile.getUsername());
                leaveRequest(mMyProfile.getUsername(), mValues.get(position).getChatId(),holder.mLeaveButton,holder.mGroupnameView,holder.mLinearLayoutContact);
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


        public final LinearLayout mLinearLayoutContact;
        public final Button mLeaveButton;
//        public final TextView mNewContactView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mGroupnameView = (TextView) view.findViewById(R.id.text_groupchat_username);
            mLeaveButton = view.findViewById(R.id.button_leavegroup);
            mLinearLayoutContact = view.findViewById(R.id.linearlayout_contact_contact);

//            mNewContactView = view.findViewById(R.id.text_contact_username);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mGroupnameView.getText() + "'";
        }
    }

    public void leaveRequest(String usernameRequested, int theChatId,Button btnReject, TextView txtNewContact, LinearLayout layout) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(mView.getContext().getString(R.string.ep_base_url))
                .appendPath(mView.getContext().getString(R.string.ep_messaging_base))
                .appendPath(mView.getContext().getString(R.string.ep_messaging_leavegroup))
                .build();

        try {
            JSONObject jsonLeaveContact = new JSONObject();
            jsonLeaveContact.put("chatid", theChatId);
            jsonLeaveContact.put("username", mMyProfile.getUsername());
            Log.d("TESTING LEAVE", theChatId+"");
            Log.d("HAHA LEAVE", mMyProfile.getUsername());

            Log.i("json", jsonLeaveContact.toString());

            new SendPostAsyncTask.Builder(uri.toString(), jsonLeaveContact).addHeaderField("authorization", mJwToken)
                    .onPostExecute(this::handleRejectOnPost)
                    .build().execute();

            btnReject.setVisibility(View.GONE);
            txtNewContact.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);

            Toast toast = Toast.makeText(mView.getContext(), "Contact " + usernameRequested + " removed.",
                    Toast.LENGTH_LONG);
            View view = toast.getView();
            view.setBackgroundResource(R.drawable.customborder_goldblack);
            toast.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleRejectOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            mView.getContext().getString(R.string.keys_json_success));
//            Log.d("Success",success+"");
            Log.d("Success",resultsJSON.getJSONObject("error").toString());
            if (success) {
                mCount = mCount - 1;

                if (mCount == 0) {
                    FragmentManager manager = ((AppCompatActivity) mView.getContext()).getSupportFragmentManager();
                    LinearLayout noContacts = (LinearLayout) ((AppCompatActivity) mView.getContext()).findViewById(R.id.editView);
                    RecyclerView recyclerView = (RecyclerView) ((AppCompatActivity) mView.getContext()).findViewById(R.id.list);
                    noContacts.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(mView.getContext(), "Error rejecting contact.",
                        Toast.LENGTH_LONG).show();

            }
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }
}
