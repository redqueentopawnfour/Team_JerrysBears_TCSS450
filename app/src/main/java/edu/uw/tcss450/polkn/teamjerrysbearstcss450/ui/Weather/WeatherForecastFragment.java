package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
        if (getArguments() != null) {
            mWeathers = (WeatherObject[]) getArguments().getSerializable("weather");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather_forecast, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        updateInfo(mWeathers);

//        Uri uri_weather = new Uri.Builder()
//                .scheme("https")
//                .appendPath(getString(R.string.ep_base_url))
//                .appendPath(getString(R.string.ep_weather))
//                .appendPath(getString(R.string.ep_weather_forecast))
//                .build();
//
//        new ForecastWeatherTask().execute(uri_weather.toString());
    }

/*
    private class ForecastWeatherTask extends AsyncTask<String, Void, WeatherObject[]> {
        protected WeatherObject[] doInBackground(String... strings) {

            String resultString = "";
            HttpURLConnection urlConnection = null;

            try {
                URL urlObject = new URL(strings[0]);
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    resultString += s;
                }
            } catch (Exception e) {
                Log.e("ERROR CONNECTING", "Unable to connect, Reason: " + e.getMessage());
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            try {
                String lat = "", lon = "", temp = "", mainDescript = "", location ="", date ="";
                WeatherObject[] weathers = new WeatherObject[10];

                // Retrieve JSON objects according to api docs
                JSONObject arrayOfForecastsObject = new JSONObject(resultString);
                JSONArray arrayOfForecasts = arrayOfForecastsObject.getJSONArray("data");

                // Set information not dependant on time
                lat = arrayOfForecastsObject.getString(getString(R.string.keys_json_weather_forecast_lat));
                lon = arrayOfForecastsObject.getString(getString(R.string.keys_json_weather_forecast_lon));
                location = arrayOfForecastsObject.getString(getString(R.string.keys_json_weather_forecast_city)); //city

                // Build 10 days of forecasts to analyse time based forecasting
                for (int i = 0; i < 10; i++) {
                    // Get the ith forecast
                    JSONObject forecast = arrayOfForecasts.getJSONObject(i);

                    // Grab temperate and date information
                    temp = forecast.getString(getString(R.string.keys_json_weather_forecast_temp));
                    date = forecast.getString(getString(R.string.keys_json_weather_forecast_date));

                    // Description information is one JSON Object deeper
                    JSONObject weatherDetails = forecast.getJSONObject(getString(R.string.keys_json_weather_forecast_details));
                    mainDescript = weatherDetails.getString(getString(R.string.keys_json_weather_forecast_description));

                    // Build and store this WeatherObject
                    WeatherObject weather = new WeatherObject.Builder(
                            temp, lat, lon).addDescription(mainDescript).addLocation(location).addDate(date).build();
                    weathers[i] = weather;
                }


                return weathers;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ERROR!", e.getMessage());
                Log.e("WEATHER ERROR!", "null pointer in WeatherFragment.java bad json stuff");
            }
            return null; //oof this is about to break some stuff ^ log it for sure
        }

        protected void onPostExecute(WeatherObject[] result) {
            updateInfo(result);
        }
    }
*/
    private void updateInfo(WeatherObject[] weathers) {
        // Oh my god I thought I could loop through the TextViews but I can't figure it out...
        // Can't even write a loop in onPostExecute and pass in which TextView level I'll need becuase
        // can't dynamically retrieve id of the TextView.......

        // Wait does this work

        // Hold the similar beginning of each TextView id
        String tempName = getString(R.string.text_forecast_temp_notnumbered);
        String dateName = getString(R.string.text_forecast_date_notnumbered);
        String deetsName = getString(R.string.text_forecast_description_notnumbered);
        int id;

        // Iterate through the TextViews by finding then with that similar beginning + their index number
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
