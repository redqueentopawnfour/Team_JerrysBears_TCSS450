package edu.uw.tcss450.polkn.teamjerrysbearstcss450;


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

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.model.Credentials;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {


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

            LoginFragmentDirections.ActionNavFragmentLoginToHomeActivity homeActivity =
                    LoginFragmentDirections.actionNavFragmentLoginToHomeActivity(new Credentials.
                            Builder(email.getText().toString(), pw.getText().toString()).
                            build());


            homeActivity.setJwt("Later");
            Navigation.findNavController(getView()).navigate(homeActivity);

//            Bundle args = new Bundle();
//            args.putSerializable("Key",
//                    new Credentials.Builder(
//                            email.getText().toString(),
//                            pw.getText().toString())
//                            .build());
//
//            Navigation.findNavController(v)
//                    .navigate(R.id.action_nav_fragment_login_to_homeActivity, args);
        }
    }

    private void onRegisterClicked() {
        Navigation.findNavController(getView()).
                navigate(R.id.action_nav_fragment_login_to_nav_fragment_register);
    }
}
