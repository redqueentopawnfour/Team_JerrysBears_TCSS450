package edu.uw.tcss450.polkn.teamjerrysbearstcss450;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    protected static boolean validatePassword(EditText password) {
        boolean isValid = false;

        if (password.getText().length() == 0) {
            password.setError("Field must not be empty.");
        } else if (password.getText().length() < 5) {
            password.setError("Password must be longer than 5 characters");
        } else {
            isValid = true;
        }

        return isValid;
    }

    protected static boolean validateEmail(EditText email) {
        boolean isValid = false;

        if (email.getText().length() == 0) {
            email.setError("Email must not be empty.");
        } else if (email.getText().toString().chars().filter(ch -> ch == '@').count() != 1) {
            email.setError("Email address must be valid.");
        } else {
            isValid = true;
        }

        return isValid;
    }
}
