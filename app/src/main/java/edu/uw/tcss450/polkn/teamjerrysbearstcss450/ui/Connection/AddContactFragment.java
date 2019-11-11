package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivityArgs;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.model.Credentials;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

public class AddContactFragment extends Fragment {

    String mEmail_sender;
    String mUsername_requested;

    public AddContactFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_contact, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HomeActivityArgs args = HomeActivityArgs.fromBundle(getArguments());
        Credentials credentials = args.getCredentials();
        mEmail_sender = credentials.getEmail();

        view.findViewById(R.id.button_addContact_sendRequest)
                .setOnClickListener(this::sendRequest);
    }

    private void sendRequest(final View theButton) {
        EditText contactUsernameView = getActivity().findViewById(R.id.editText_addContact_enterUsername);
        String contactUsername = contactUsernameView.getText().toString();
        mUsername_requested = contactUsername;

        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_contacts))
                .appendPath(getString(R.string.ep_add))
                .build();

        try {
            JSONObject jsonAddContact = new JSONObject();
            jsonAddContact.put("email_sender", mEmail_sender);
            jsonAddContact.put("username_requested", contactUsername);

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
     * @param result the JSON formatted String response from the web service
     */
    private void handleAddContactOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_success));

            if (success) {
                Toast.makeText(getActivity(), "Contact " + mUsername_requested + " successfully added.",
                        Toast.LENGTH_LONG).show();
                EditText contactUsernameView = getActivity().findViewById(R.id.editText_addContact_enterUsername);
                contactUsernameView.setText("");
            } else {
                ((TextView) getView().findViewById(R.id.editText_addContact_enterUsername))
                        .setError("Unable to add user.");

            }
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR",  result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }
}
