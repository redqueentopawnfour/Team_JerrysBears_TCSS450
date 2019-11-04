package edu.uw.tcss450.polkn.teamjerrysbearstcss450;


import android.net.Uri;
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

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.model.Credentials;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    private Credentials mCredentials;
    public LoginFragment() {
        // Required empty public constructor
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
    /*

     */
    private void onLoginClicked() {
        View v = getView();

        EditText email =  v.findViewById(R.id.editText_login_email);
        EditText pw = v.findViewById(R.id.editText_login_pw);
        if(MainActivity.validateEmail(email) && MainActivity.validatePassword(pw)) {
            Credentials credentials = new Credentials.Builder(
                    email.getText().toString(),
                    pw.getText().toString())
                    .build();
            //build the web service URL
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_login))
                    .build();
            //build the JSONObject
            JSONObject msg = credentials.asJSONObject();
            mCredentials = credentials;
            //instantiate and execute the AsyncTask.
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleLoginOnPre)
                    .onPostExecute(this::handleLoginOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        }
    }
    /**
     * handle errors in Async task.
     * @param result the provided error message
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }
    private void onRegisterClicked() {
        Navigation.findNavController(getView()).
                navigate(R.id.action_nav_fragment_login_to_nav_fragment_register);
    }

    /**
     * Handle the setup of the UI before the HTTP call to the webservice.
     */
    private void handleLoginOnPre() {
        getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.VISIBLE);
    }
    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleLoginOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_success));
            Log.d("results", resultsJSON.toString());

            String message =
                    resultsJSON.getString(
                            getString(R.string.keys_json_message));

            if (success) {
                LoginFragmentDirections.ActionNavFragmentLoginToHomeActivity
                        homeActivity =
                        LoginFragmentDirections
                                .actionNavFragmentLoginToHomeActivity(mCredentials);
                homeActivity.setJwt(
                        resultsJSON.getString(
                                getString(R.string.keys_json_jwt)));
                Navigation.findNavController(getView())
                        .navigate(homeActivity);
                return;
            } else {
                //Login was unsuccessful. Don’t switch fragments and
                // inform the user
                Log.d("no success branch", "oop");

                String errorMessage = "Login unsuccessful.";
                if (message != null) {
                    errorMessage= message;
                }

                ((TextView) getView().findViewById(R.id.editText_login_email))
                        .setError(errorMessage);
            }
            getActivity().findViewById(R.id.layout_login_wait)
                    .setVisibility(View.GONE);
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            getActivity().findViewById(R.id.layout_login_wait)
                    .setVisibility(View.GONE);
            ((TextView) getView().findViewById(R.id.editText_login_email))
                    .setError("Login Unsuccessful");
        }
    }
}
