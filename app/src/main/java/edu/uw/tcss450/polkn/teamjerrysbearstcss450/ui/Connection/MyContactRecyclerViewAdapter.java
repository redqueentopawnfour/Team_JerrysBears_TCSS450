package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MobileNavigationDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatViewFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.Message.Message;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactFragment.OnListFragmentInteractionListener;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

import java.util.HashMap;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Contact} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyContactRecyclerViewAdapter extends RecyclerView.Adapter<MyContactRecyclerViewAdapter.ViewHolder> {

    private final List<Contact> mValues;
//    private final List<Message> mMessage;


    private final HashMap<Integer, Drawable> mDrawableIds;
    private final OnListFragmentInteractionListener mListener;
    private Contact mMyProfile;
    private View mView;
    private String mJwt;
    private int mChatId;
    private int mCount;

    public MyContactRecyclerViewAdapter(List<Contact> items, String theJwt, HashMap drawableIds, Contact myProfile, OnListFragmentInteractionListener listener) {
        mValues = items;
        mDrawableIds = drawableIds;
        mListener = listener;
        mMyProfile = myProfile;
        mJwt = theJwt;
        mCount = items.size();


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contact, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        String username = mValues.get(position).getUsername();
        String firstName = mValues.get(position).getFirstName();
        String lastName = mValues.get(position).getLastName();
        Log.i("chat id: ", Integer.toString(mChatId));

        int requestNumber = mValues.get(position).getRequestNumber();
        Log.i("Requestnum", Integer.toString(requestNumber));

        Boolean contactVerified = mValues.get(position).getIsContactVerified();
        Log.i("contact verified", Boolean.toString(contactVerified));

        String displayString = username;

        if (requestNumber > 0 && !contactVerified) {
            holder.mPendingContactView.setVisibility(View.VISIBLE);
            holder.mAcceptButton.setVisibility(View.INVISIBLE);
        } else if (requestNumber == 0 && !contactVerified) {
            holder.mLinearLayoutContact.setBackgroundResource(R.drawable.customborder_gold);   // use this for notification
            holder.mNewContactView.setVisibility(View.VISIBLE);
            holder.mAcceptButton.setVisibility(View.VISIBLE);
        } else if (contactVerified) {
            holder.mChat.setVisibility(View.VISIBLE);
        }

        holder.mRejectButton.setVisibility(View.VISIBLE);
        holder.mUsernameView.setText(displayString);
        Integer pos = new Integer(position);
        Drawable drawableIcon = mDrawableIds.get(pos);
        drawableIcon.setBounds(0, 0, 120, 120);
        holder.mUsernameView.setCompoundDrawables(drawableIcon, null, null, null);

        Log.i("username requested", mValues.get(position).getUsername());

        holder.mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest(mValues.get(position).getUsername(), holder.mAcceptButton, holder.mChat, holder.mNewContactView, holder.mLinearLayoutContact);
            }
        });
        holder.mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectRequest(mValues.get(position).getUsername(), holder.mAcceptButton, holder.mChat, holder.mNewContactView, holder.mLinearLayoutContact);
            }
        });
        holder.mChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChat(mValues.get(position).getChatId());
            }
        });
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
        public final TextView mNewContactView;
        public final TextView mPendingContactView;
        public final ImageButton mAcceptButton;
        public final ImageButton mRejectButton;
        public final ImageButton mChat;
        public final LinearLayout mLinearLayoutContact;

        public Contact mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsernameView = view.findViewById(R.id.text_contact_displayMessage);
            mNewContactView = view.findViewById(R.id.text_contact_username);
            mPendingContactView = view.findViewById(R.id.text_contact_pendingContact);
            mAcceptButton = view.findViewById(R.id.imageButton_contact_accept);
            mRejectButton = view.findViewById(R.id.imageButton_contact_reject);
            mChat = view.findViewById(R.id.imageButton_contact_chat);
            mLinearLayoutContact = view.findViewById(R.id.linearlayout_contact_contact);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUsernameView.getText() + "'";
        }
    }

    public void acceptRequest(String usernameRequested, ImageButton btnAccept, ImageButton btnChat, TextView txtNewContact, LinearLayout layout) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(mView.getContext().getString(R.string.ep_base_url))
                .appendPath(mView.getContext().getString(R.string.ep_contacts))
                .appendPath(mView.getContext().getString(R.string.ep_accept))
                .build();

        try {
            JSONObject jsonAddContact = new JSONObject();
            jsonAddContact.put("email_sender", mMyProfile.getEmail());
            jsonAddContact.put("username_requested", usernameRequested);

            Log.i("json", jsonAddContact.toString());

            new SendPostAsyncTask.Builder(uri.toString(), jsonAddContact)
                    .onPostExecute(this::handleAcceptOnPost)
                    .build().execute();

            btnAccept.setVisibility(View.GONE);
            txtNewContact.setVisibility(View.GONE);
            btnChat.setVisibility(View.VISIBLE);
            layout.setBackgroundResource(R.drawable.customborder);
            Toast toast = Toast.makeText(mView.getContext(), "Contact " + usernameRequested + " accepted.",
                    Toast.LENGTH_LONG);
            View view = toast.getView();
            view.setBackgroundResource(R.drawable.customborder_goldblack);
            toast.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void rejectRequest(String usernameRequested, ImageButton btnAccept, ImageButton btnReject, TextView txtNewContact, LinearLayout layout) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(mView.getContext().getString(R.string.ep_base_url))
                .appendPath(mView.getContext().getString(R.string.ep_contacts))
                .appendPath(mView.getContext().getString(R.string.ep_reject))
                .build();

        try {
            JSONObject jsonAddContact = new JSONObject();
            jsonAddContact.put("email_sender", mMyProfile.getEmail());
            jsonAddContact.put("username_requested", usernameRequested);

            Log.i("json", jsonAddContact.toString());

            new SendPostAsyncTask.Builder(uri.toString(), jsonAddContact)
                    .onPostExecute(this::handleRejectOnPost)
                    .build().execute();

            btnAccept.setVisibility(View.GONE);
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

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     *
     * @param result the JSON formatted String response from the web service
     */
    private void handleAcceptOnPost(String result) {

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            mView.getContext().getString(R.string.keys_json_success));
            if (success) {
                //mChatId = mValues.get(position).getChatId();
                if (resultsJSON.has(mView.getContext().getResources().getString(R.string.keys_json_message))) {
                    Log.i("contacts", "contacts");
                }
            } else {
                Toast.makeText(mView.getContext(), "Error accepting contact.",
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

    private void openChat(int chatId) {
        MobileNavigationDirections.ActionGlobalNavChat directions
                = ChatViewFragmentDirections.actionGlobalNavChat().setChatid(chatId).setJwt(mJwt).setUsername(mMyProfile.getUsername());

        Navigation.findNavController(mView).navigate(directions);
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     *
     * @param result the JSON formatted String response from the web service
     */
    private void handleRejectOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            mView.getContext().getString(R.string.keys_json_success));

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
