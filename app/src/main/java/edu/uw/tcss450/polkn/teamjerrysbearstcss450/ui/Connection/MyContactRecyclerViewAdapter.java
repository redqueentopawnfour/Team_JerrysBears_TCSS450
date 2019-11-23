package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;

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

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MobileNavigationDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatFragmentDirections;
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
    private final HashMap<Integer, Drawable> mDrawableIds;
    private final OnListFragmentInteractionListener mListener;
    private Contact mMyProfile;
    private View mView;
    private String mUsername_requested;
    private String mJwt;
    private int mChatId;

    /*public MyContactRecyclerViewAdapter(List<Contact> items, HashMap drawableIds) {*/
    public MyContactRecyclerViewAdapter(List<Contact> items, String theJwt, HashMap drawableIds, Contact myProfile, OnListFragmentInteractionListener listener) {
        mValues = items;
        mDrawableIds = drawableIds;
        mListener = listener;
        mMyProfile = myProfile;
        mUsername_requested = "";
        mJwt = theJwt;
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
//        mChatId = mValues.get(position).getChatId();
        Log.i("chat id: ", Integer.toString(mChatId));


        mUsername_requested = username;
        /*.setVisibility(View.GONE);*/

        int requestNumber = mValues.get(position).getRequestNumber();
        Log.i("Requestnum", Integer.toString(requestNumber));

        Boolean contactVerified = mValues.get(position).getIsContactVerified();
        Log.i("contact verified", Boolean.toString(contactVerified));

        String displayString = username;

   /*     if (username.length() > 0 && firstName.length() > 0 && lastName.length() > 0) {
            displayString = username + " (" + firstName + " " + lastName + ")";
        } else if (username.length() > 0) {
            displayString = username;
        }*/

        if (requestNumber > 0 && !contactVerified) {
            holder.mPendingContactView.setVisibility(View.VISIBLE);
        } else if (requestNumber == 0 && !contactVerified) {
            /* holder.mLinearLayoutContact.setBackgroundResource(R.drawable.customborder_gold);    use this for notification*/
            holder.mNewContactView.setVisibility(View.VISIBLE);
            holder.mAcceptButton.setVisibility(View.VISIBLE);
            holder.mRejectButton.setVisibility(View.VISIBLE);
        } else if (contactVerified) {
            holder.mChat.setVisibility(View.VISIBLE);
            holder.mRejectButton.setVisibility(View.VISIBLE);
        }

        holder.mUsernameView.setText(displayString);
        Integer pos = new Integer(position);
        Drawable drawableIcon = mDrawableIds.get(pos);
        drawableIcon.setBounds(0, 0, 120, 120);
        holder.mUsernameView.setCompoundDrawables(drawableIcon, null, null, null);

        holder.mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest();
            }
        });
        holder.mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectRequest();
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
            mUsernameView = view.findViewById(R.id.text_contact_displayString);
            mNewContactView = view.findViewById(R.id.text_contact_newContact);
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

    public void acceptRequest() {
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
            jsonAddContact.put("username_requested", mUsername_requested);

            Log.i("json", jsonAddContact.toString());

            new SendPostAsyncTask.Builder(uri.toString(), jsonAddContact)
                    .onPostExecute(this::handleAcceptOnPost)
                    .build().execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void rejectRequest() {
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
            jsonAddContact.put("username_requested", mUsername_requested);

            Log.i("json", jsonAddContact.toString());

            new SendPostAsyncTask.Builder(uri.toString(), jsonAddContact)
                    .onPostExecute(this::handleRejectOnPost)
                    .build().execute();

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

                mView.findViewById(R.id.imageButton_contact_accept).setVisibility(View.GONE);
                /*mView.findViewById(R.id.imageButton_contact_reject).setVisibility(View.GONE);*/
                mView.findViewById(R.id.text_contact_newContact).setVisibility(View.GONE);
                mView.findViewById(R.id.imageButton_contact_chat).setVisibility(View.VISIBLE);
                Toast toast = Toast.makeText(mView.getContext(), "Contact " + mUsername_requested + " accepted.",
                        Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.customborder_goldblack);
                /*TextView text = (TextView) view.findViewById(android.R.id.message);*/
                toast.show();
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
                = ChatFragmentDirections.actionGlobalNavChat().setChatid(chatId).setJwt(mJwt);

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
                mView.findViewById(R.id.imageButton_contact_accept).setVisibility(View.GONE);
                mView.findViewById(R.id.imageButton_contact_reject).setVisibility(View.GONE);
                mView.findViewById(R.id.text_contact_newContact).setVisibility(View.GONE);
                mView.findViewById(R.id.linearlayout_contact_contact).setVisibility(View.GONE);
                Toast toast = Toast.makeText(mView.getContext(), "Contact " + mUsername_requested + " removed.",
                        Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.customborder_goldblack);
                /*TextView text = (TextView) view.findViewById(android.R.id.message);*/
                toast.show();
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
