package edu.uw.tcss450.polkn.teamjerrysbearstcss450;

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

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.model.Credentials;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {

    private Credentials mCredentials;
    public RegisterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_register_register)
                .setOnClickListener(this::attemptRegister);
    }

    private void attemptRegister(final View theButton) {
        EditText firstNameEdit = getActivity().findViewById(R.id.editText_register_firstName);
        EditText lastNameEdit = getActivity().findViewById(R.id.editText_register_lastName);
        EditText emailEdit = getActivity().findViewById(R.id.editText_register_email);
        EditText nicknameEdit = getActivity().findViewById(R.id.editText_register_nickname);
        EditText password1Edit = getActivity().findViewById(R.id.editText_register_password);
        EditText password2Edit = getActivity().findViewById(R.id.editText_register_retypePassword);

        if (validateFirstAndLastName(firstNameEdit, lastNameEdit)
                && MainActivity.validateEmail(emailEdit)
                && validateNickname(nicknameEdit)
                && validatePasswords(password1Edit, password2Edit)) {


            /* Before changing to show email on Home page.*/
            Credentials credentials = new Credentials.Builder(
                    emailEdit.getText().toString(),
                    password1Edit.getText().toString())
                    .addFirstName(firstNameEdit.getText().toString())
                    .addLastName(lastNameEdit.getText().toString())
                    .addUsername(nicknameEdit.getText().toString())
                    .build();
            //build the web service URL
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_register))
                    .build();
            //build the JSONObject
            JSONObject msg = credentials.asJSONObject();
            mCredentials = credentials;
            //instantiate and execute the AsyncTask.
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleRegisterOnPre)
                    .onPostExecute(this::handleRegisterOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        }
    }

    private boolean validateFirstAndLastName(EditText firstName, EditText lastName) {
        boolean isValid = false;

        if (firstName.getText().length() == 0) {
            firstName.setError("First name must not be empty.");
        } else  if (lastName.getText().length() == 0) {
            lastName.setError("Last name must not be empty.");
        }  else {
            isValid = true;
        }

        return isValid;
    }


    private boolean validateNickname(EditText nickname) {
        boolean isValid = false;

        if (nickname.getText().length() == 0) {
            nickname.setError("Nickname must not be empty.");
        } else  if (nickname.getText().length() < 5) {
            nickname.setError("Nickname must be at least 5 characters.");
        }  else if (nickname.getText().length() > 12) {
            nickname.setError("Nickname must be less than 12 characters.");
        }  {
            isValid = true;
        }

        // TODO: additional validation for nickname in use?

        return isValid;
    }

    private boolean validatePasswords(EditText password1, EditText password2) {
        boolean isValid = false;

        if (MainActivity.validatePassword(password1) && MainActivity.validatePassword(password2)) {
            if (!password1.getText().toString().equals(password2.getText().toString())) {
                password1.setError("Passwords must match.");
            } else {
                isValid = true;
            }
        }

        return isValid;
    }
    /**
     * handle errors in Async task.
     * @param result the provided error message
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }
    /**
     * Handle the setup of the UI before the HTTP call to the webservice.
     */
    private void handleRegisterOnPre() {
        getActivity().findViewById(R.id.layout_register_wait).setVisibility(View.VISIBLE);
    }
    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleRegisterOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_success));
            Log.d("results", resultsJSON.toString());
            if (success) {
            /*    RegisterFragmentDirections
                        .ActionNavFragmentRegisterToHomeActivity homeActivity =
                        RegisterFragmentDirections.actionNavFragmentRegisterToHomeActivity(mCredentials);
               homeActivity.setJwt(
                        resultsJSON.getString(
                                getString(R.string.keys_json_jwt)));

                Navigation.findNavController(getView())
                        .navigate(homeActivity);*/

                Navigation.findNavController(getView()).
                        navigate(R.id.action_nav_fragment_register_to_nav_fragment_verifyEmail);

                return;
            } else {
                String errorMessage = resultsJSON.getJSONObject("error").getString("detail");
                //Registration was unsuccessful. Don’t switch fragments and
                // inform the user
                Log.d("no success branch", "oop");
                if(errorMessage != null) {
                    ((TextView) getView().findViewById(R.id.editText_register_email))
                            .setError(errorMessage);
                } else {
                    ((TextView) getView().findViewById(R.id.editText_register_email))
                            .setError("Register Unsuccessful");
                }
            }
            getActivity().findViewById(R.id.layout_register_wait)
                    .setVisibility(View.GONE);
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            getActivity().findViewById(R.id.layout_register_wait)
                    .setVisibility(View.GONE);
            ((TextView) getView().findViewById(R.id.editText_register_email))
                    .setError("Register Unsuccessful");
        }
    }

}
