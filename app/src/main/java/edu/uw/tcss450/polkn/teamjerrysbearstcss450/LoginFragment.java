package edu.uw.tcss450.polkn.teamjerrysbearstcss450;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.model.Credentials;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatMessageNotification;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactNotification;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;
import me.pushy.sdk.Pushy;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    private Credentials mCredentials;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (prefs.contains(getString(R.string.keys_prefs_email)) &&
                prefs.contains(getString(R.string.keys_prefs_password))) {

            final String email = prefs.getString(getString(R.string.keys_prefs_email), "");
            final String password = prefs.getString(getString(R.string.keys_prefs_password), "");
            //Load the two login EditTexts with the credentials found in SharedPrefs
            EditText emailEdit = getActivity().findViewById(R.id.editText_login_email);
            emailEdit.setText(email);
            EditText passwordEdit = getActivity().findViewById(R.id.editText_login_pw);
            passwordEdit.setText(password);

            doLogin(new Credentials.Builder(emailEdit.getText().toString(),
                    passwordEdit.getText().toString())
                    .build());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button b = view.findViewById(R.id.button_login_login);
        b.setOnClickListener(butt -> onLoginClicked());
        b = view.findViewById(R.id.button_login_register);
        b.setOnClickListener(butt -> onRegisterClicked());
    }


    private void onLoginClicked() {
        View v = getView();

        EditText email = v.findViewById(R.id.editText_login_email);
        EditText pw = v.findViewById(R.id.editText_login_pw);
        if (MainActivity.validateEmail(email) && MainActivity.validatePassword(pw)) {
            doLogin(new Credentials.Builder(
                    email.getText().toString(), pw.getText().toString()
            ).build());
        }
    }


    private void onRegisterClicked() {
        Navigation.findNavController(getView()).
                navigate(R.id.action_nav_fragment_login_to_nav_fragment_register);
    }


    private void saveCredentials(final Credentials credentials) {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //Store the credentials in SharedPrefs
        prefs.edit().putString(getString(R.string.keys_prefs_email), credentials.getEmail()).apply();
        prefs.edit().putString(getString(R.string.keys_prefs_password), credentials.getPassword()).apply();
    }

    private void doLogin(Credentials credentials) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_login))
                .build();

        mCredentials = credentials;

        new AttemptLoginTask().execute(uri.toString());
    }

    class AttemptLoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.button_login_login).setEnabled(false);
            getActivity().findViewById(R.id.button_login_register).setEnabled(false);
        }

        @Override
        protected String doInBackground(String... urls) {
            //get pushy token
            String deviceToken = "";

            try {
                // Assign a unique token to this device
                deviceToken = Pushy.register(getActivity().getApplicationContext());

                //subscribe to a topic (this is a Blocking call)
                Pushy.subscribe("all", getActivity().getApplicationContext());
            } catch (Exception exc) {

                cancel(true);
                // Return exc to onCancelled
                return exc.getMessage();
            }

            //feel free to remove later.
            Log.d("LOGIN", "Pushy Token: " + deviceToken);

            //attempt to log in: Send credentials AND pushy token to the web service
            StringBuilder response = new StringBuilder();
            HttpURLConnection urlConnection = null;

            try {
                URL urlObject = new URL(urls[0]);
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");


                urlConnection.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());

                JSONObject message = mCredentials.asJSONObject();
                message.put("token", deviceToken);

                wr.write(message.toString());
                wr.flush();
                wr.close();

                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response.append(s);
                }
                publishProgress();
            } catch (Exception e) {
                response = new StringBuilder("Unable to connect, Reason: "
                        + e.getMessage());
                cancel(true);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return response.toString();
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
            getActivity().findViewById(R.id.button_login_login).setEnabled(false);
            getActivity().findViewById(R.id.button_login_register).setEnabled(false);
            Log.e("LOGIN_ERROR", "Error in Login Async Task: " + s);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                Log.d("JSON result", result);
                JSONObject resultsJSON = new JSONObject(result);
                boolean success = resultsJSON.getBoolean("success");

                if (success) {
                    saveCredentials(mCredentials);

                    //Login was successful. Switch to the SuccessFragment.
                    LoginFragmentDirections.ActionNavFragmentLoginToHomeActivity homeActivity =
                            LoginFragmentDirections
                                    .actionNavFragmentLoginToHomeActivity(mCredentials);
                    homeActivity.setJwt(resultsJSON.getString(
                            getString(R.string.keys_json_jwt)));

                    if (getArguments() != null) {
                        if (getArguments().containsKey("type")) {
                            if (getArguments().getString("type").equals("msg")) {
                                String msg = getArguments().getString("message");
                                String sender = getArguments().getString("sender");
                                int chatId = getArguments().getInt("chatid");

                                ChatMessageNotification chat =
                                        new ChatMessageNotification.Builder(sender, msg).build();
                                homeActivity.setChatMessage(chat);
                                homeActivity.setChatId(chatId);
                                
                            } else if (getArguments().getString("type").equals("connectionReq") ||
                                    getArguments().getString("type").equals("connectionAccepted") ||
                                    getArguments().getString("type").equals("connectionRejected")) {

                                String msg = getArguments().getString("message");
                                String sender = getArguments().getString("sender");

                                ContactNotification contact =
                                        new ContactNotification.Builder(sender, msg).build();
                                homeActivity.setContactMessage(contact);
                            }
                        }
                    }
                    Navigation.findNavController(getView()).navigate(homeActivity);
                    getActivity().finish();
                    return;
                } else {
                    getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.button_login_login).setEnabled(true);
                    getActivity().findViewById(R.id.button_login_register).setEnabled(true);
                    //Saving the token wrong. Don’t switch fragments and inform the user
                    ((TextView) getView().findViewById(R.id.editText_login_email))
                            .setError("Login Unsuccessful");
                }
            } catch (JSONException e) {
                //It appears that the web service didn’t return a JSON formatted String
                //or it didn’t have what we expected in it.
                Log.e("JSON_PARSE_ERROR", result
                        + System.lineSeparator()
                        + e.getMessage());

                ((TextView) getView().findViewById(R.id.editText_login_email))
                        .setError("Login Unsuccessful");
            }
        }
    }
}
