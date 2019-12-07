package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivityArgs;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.model.Credentials;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather.WeatherFragment;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

public class AddContactFragment extends Fragment {

    private String mEmail_sender;
    private Contact[] mContacts;
    private List<Contact> mContactResultsList;
    private HashMap iconDrawables;
    private View viewNoContacts;
    public AddContactFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iconDrawables = new HashMap<Integer, Drawable>();
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

        View innerView = view.findViewById(R.id.addContacts_inner);
        Button searchButton = innerView.findViewById(R.id.button_addContact_searchContacts);
        searchButton.setOnClickListener(this::searchContacts);

        view.setOnClickListener(this::searchContacts);
        ((HomeActivity) getActivity()).hideAddUser();
        ((HomeActivity) getActivity()).hideViewProfile();
/*
        EditText contactSearch = getActivity().findViewById(R.id.editText_addContact_enterSearchTerm);
        contactSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });*/
    }
/*
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
*/
    private void searchContacts(final View theButton) {
        EditText contactSearchBoxView = getActivity().findViewById(R.id.editText_addContact_enterSearchTerm);
        String searchTerm = contactSearchBoxView.getText().toString();

        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_contacts))
                .appendPath(getString(R.string.ep_search))
                .build();

        try {
            JSONObject jsonSearch = new JSONObject();
            jsonSearch.put("searchTerm", searchTerm);
            jsonSearch.put("email_sender", mEmail_sender);

            Log.i("json", jsonSearch.toString());

            new SendPostAsyncTask.Builder(uri.toString(), jsonSearch)
                    .onPostExecute(this::handleSearchOnPostExecute)
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
    private void handleSearchOnPostExecute(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_success));

            RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.addContacts_recyclerView_contactResults);
            viewNoContacts = getView().findViewById(R.id.linear_contactsResults_noResults);
            Context context = getView().getContext();

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_message))) {
                    JSONArray data = resultsJSON.getJSONArray(
                            getString(R.string.keys_json_message));
                    mContacts  = new Contact[data.length()];

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jsonContact = data.getJSONObject(i);

                        mContacts[i] = new Contact.Builder(
                                jsonContact.getString(
                                        getString(R.string.keys_json_contact_username)),
                                jsonContact.getString(
                                        getString(R.string.keys_json_contact_usericon)))
                                .addFirstName(jsonContact.getString(
                                        getString(R.string.keys_json_contact_firstname)))
                                .addLastName(jsonContact.getString(
                                        getString(R.string.keys_json_contact_lastname)))
                                .addEmail(jsonContact.getString(
                                        getString(R.string.keys_json_contact_email)))
                                .build();
                    }

                    mContactResultsList = new ArrayList<>(Arrays.asList(mContacts));

                    if (recyclerView instanceof RecyclerView) {
                        if (mContacts != null) {
                            recyclerView.setLayoutManager(new GridLayoutManager(context, 1));
                            populateIconDrawables(context);
                            recyclerView.setAdapter(new ContactSearchResultsRecyclerViewAdapter(mContactResultsList, iconDrawables, mEmail_sender));
                            recyclerView.setVisibility(View.VISIBLE);
                            viewNoContacts.setVisibility(View.GONE);
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            viewNoContacts.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    Log.e("ERROR!", "No response");
                }
            } else {recyclerView.setVisibility(View.GONE);
                viewNoContacts.setVisibility(View.VISIBLE);

            }
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    private void populateIconDrawables(Context context) {
        for (int i = 0; i < mContactResultsList.size(); i++) {
            String userIcon = mContactResultsList.get(i).getUserIcon();
            Log.i("usericon", userIcon);
            int drawableId = context.getResources().getIdentifier(userIcon, "drawable", context.getPackageName());
            Drawable drawableIcon = ResourcesCompat.getDrawable(getResources(), drawableId, null);

            iconDrawables.put(i, drawableIcon);
        }
    }
}
