package edu.uw.tcss450.polkn.teamjerrysbearstcss450;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.Navigation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import me.pushy.sdk.Pushy;

public class MainActivity extends AppCompatActivity {

    public static String PACKAGE_NAME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pushy.listen(this);
        setContentView(R.layout.activity_main);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("type")) {
                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .setGraph(R.navigation.nav_graph, getIntent().getExtras());
            }
        }

        // Used in "WeatherForecastFragment" to be able to find textview ids easily in a loop
        PACKAGE_NAME = getApplicationContext().getPackageName();

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
