package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactFragment.OnListFragmentInteractionListener;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Contact} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class ContactSearchResultsRecyclerViewAdapter extends RecyclerView.Adapter<ContactSearchResultsRecyclerViewAdapter.ViewHolder> {

    private final List<Contact> mValues;
    private final HashMap<Integer, Drawable> mDrawableIds;
    private View mView;
    private String mEmailSender;

    public ContactSearchResultsRecyclerViewAdapter(List<Contact> items, HashMap drawableIds, String email) {
        mValues = items;
        mDrawableIds = drawableIds;
        mEmailSender = email;
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
        holder.mSendRequestButton.setVisibility(View.VISIBLE);

        holder.mUsernameView.setText(username);
        Integer pos = new Integer(position);
        Drawable drawableIcon = mDrawableIds.get(pos);
        drawableIcon.setBounds(0, 0, 120, 120);
        holder.mUsernameView.setCompoundDrawables(drawableIcon, null, null, null);

        Log.i("username requested", mValues.get(position).getUsername());

        holder.mSendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest(mValues.get(position).getUsername());
            }
        });
    }

    private void sendRequest(String usernameRequested) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(mView.getContext().getString(R.string.ep_base_url))
                .appendPath(mView.getContext().getString(R.string.ep_contacts))
                .appendPath(mView.getContext().getString(R.string.ep_add))
                .build();

        try {
            JSONObject jsonAddContact = new JSONObject();
            jsonAddContact.put("email_sender", mEmailSender);
            jsonAddContact.put("username_requested", usernameRequested);

            Log.i("json", jsonAddContact.toString());

            new SendPostAsyncTask.Builder(uri.toString(), jsonAddContact)
                    .onPostExecute(this::handleAddContactOnPost)
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
    private void handleAddContactOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            mView.getContext().getString(R.string.keys_json_success));

            if (success) {
                Toast toast = Toast.makeText(mView.getContext(), "Connection request sent",
                        Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.customborder_greypurple);
                toast.show();
            } else {
                Toast toast = Toast.makeText(mView.getContext(), "Unable to send request",
                        Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.customborder_greypurple);
                toast.show();
            }

        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mUsernameView;
        public final ImageButton mSendRequestButton;

        public Contact mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsernameView = view.findViewById(R.id.text_contact_displayMessage);
            mSendRequestButton = view.findViewById(R.id.imageButton_contactResult_sendRequest);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUsernameView.getText() + "'";
        }
    }
}
