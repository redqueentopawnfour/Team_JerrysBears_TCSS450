package edu.uw.tcss450.polkn.teamjerrysbearstcss450;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.model.Credentials;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {


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

            RegisterFragmentDirections.ActionNavFragmentRegisterToHomeActivity homeActivity =
                    RegisterFragmentDirections.actionNavFragmentRegisterToHomeActivity(new Credentials.
                            Builder(emailEdit.getText().toString(), password1Edit.getText().toString()).
                            build());

            homeActivity.setJwt("Later");
            Navigation.findNavController(getView()).navigate(homeActivity);


            /* Before changing to show email on Home page.*/
//            Bundle args = new Bundle();
//            args.putSerializable("Key",
//                    new Credentials.Builder(
//                            emailEdit.getText().toString(),
//                            password1Edit.getText().toString())
//                            .build());
//
//            Navigation.findNavController(theButton)
//                    .navigate(R.id.action_nav_fragment_register_to_homeActivity, args);
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

}
