package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MainActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class WeatherForecastFragment extends Fragment {

    private  WeatherObject[] mWeathers;
    // to be used later when we make a recylcerview //private OnFragmentInteractionListener mListener;

    public WeatherForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("issue here in weather", "ABOUT TO LOG MWEATHERS FROM WEATHERFORECAST");
        if (getArguments() != null) {
            mWeathers = (WeatherObject[]) getArguments().getSerializable("weather");
            Log.e("here", mWeathers.toString());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((HomeActivity) getActivity()).hideAddGroup();
        ((HomeActivity) getActivity()).hideAddUser();
        ((HomeActivity) getActivity()).hideViewProfile();
        ((HomeActivity) getActivity()).hideChatIcon();

        ((HomeActivity) getActivity()).setActionBarTitle("10 Day Forecast");

        return inflater.inflate(R.layout.fragment_weather_forecast, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        updateInfo(mWeathers);
    }

    private void updateInfo(WeatherObject[] weathers) {
        // This strategy for iterating the text views was taken from Stack Overflow
        // https://stackoverflow.com/questions/17218229/looping-through-textview-and-setting-the-text
        // 2 Year old strategy so there's probably something better but it works like a charm :)

        // Hold the similar beginnings of each TextView id
        String tempName = getString(R.string.text_forecast_temp_notnumbered);
        String dateName = getString(R.string.text_forecast_date_notnumbered);
        String deetsName = getString(R.string.text_forecast_description_notnumbered);
        String imageName = getString(R.string.image_forecast_notnumbered);
        int id;

        // Iterate through the TextViews by finding them with their similar id beginnings + their index number
        // Set the temperature/ description/ date for each of the WeatherObjects we're writing to the screen
        for (int i = 0; i <= 9 ; i++) {
            String tempNameWithI = tempName + (i+1);
            id = getResources().getIdentifier(tempNameWithI, "id", MainActivity.PACKAGE_NAME);
            TextView temp = getActivity().findViewById(id);
            temp.setText(weathers[i].getTemp() + getString(R.string.weather_degrees_symbol));

            String dateNameWithI = dateName + (i+1);
            id = getResources().getIdentifier(dateNameWithI, "id", MainActivity.PACKAGE_NAME);
            TextView date = getActivity().findViewById(id);
            date.setText(weathers[i].getDate());

            String deetsNameWithI = deetsName + (i+1);
            id = getResources().getIdentifier(deetsNameWithI, "id", MainActivity.PACKAGE_NAME);
            TextView deets = getActivity().findViewById(id);
            deets.setText(weathers[i].getDesciption());

            String imageWithI = imageName + (i+1);
            id = getResources().getIdentifier(imageWithI, "id", MainActivity.PACKAGE_NAME);
            ImageView image = getActivity().findViewById(id);
            WeatherFragment.setCorrectIcon(image, weathers[i]);
        }

        TextView location = getActivity().findViewById(R.id.textView_forecast_location);

        // If the city field is not empty, fill with city info instead of coordinates
        // Else fill this TextView with coordinates
        if (!weathers[0].getLocation().equals("") && !weathers[0].getLocation().isEmpty()) {
            location.setText(weathers[0].getLocation());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Lat: ");
            sb.append(weathers[0].getLat());
            sb.append((" Long: "));
            sb.append(weathers[0].getLon());
            location.setText(sb.toString());
        }

    }
}
