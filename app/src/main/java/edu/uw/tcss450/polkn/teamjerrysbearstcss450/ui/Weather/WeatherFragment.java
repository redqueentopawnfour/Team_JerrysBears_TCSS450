package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MobileNavigationDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ViewProfileFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.GetAsyncTask;

public class WeatherFragment extends Fragment {

    private static int counter = 0;

    private WeatherObject mWeather;

    private WeatherViewModel weatherViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        weatherViewModel =
                ViewModelProviders.of(this).get(WeatherViewModel.class);
        View root = inflater.inflate(R.layout.fragment_weather, container, false);
        //final TextView textView = root.findViewById(R.id.somethingelse);
//        weatherViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }


    // make a call to the web service to get current weather
    // build a weather object
    // populate the fields with the weather data
    // also build the weather special data class
    @Override
    public void onStart() {
        super.onStart();
        counter++;
        TextView tv1 = getActivity().findViewById(R.id.text_weather_temp);
        TextView tv2 = getActivity().findViewById(R.id.text_weather_details);
        TextView tv3 = getActivity().findViewById(R.id.text_weather_details);
        tv1.setText("LOADING");
        tv2.setText("LOADING");
        tv3.setText("LOADING");

        Uri uri_weather = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                //.appendPath(getString(R.string.ep_weather_params))
                .build();

        new CurrentWeatherTask().execute(uri_weather.toString());

        ConstraintLayout lay = getActivity().findViewById(R.id.linearLayout_weatherFragment_location1);
        lay.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ForecastWeatherTask().execute();
//
//                final Bundle args = new Bundle();
//                args.putSerializable("weather", mWeather);
//                Navigation.findNavController(getView())
//                        .navigate(R.id.action_nav_weather_to_viewWeatherFragment, args);
            }
        });
    }



    private class CurrentWeatherTask extends AsyncTask<String, Void, WeatherObject> {
        protected WeatherObject doInBackground(String... strings) {


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
                String lat = "", lon = "", temp = "", mainDescript = "", location ="";
                JSONObject weather = new JSONObject(resultString);
                if (weather.has(getString(R.string.keys_json_weather_coord))) {
                    JSONObject coords = weather.getJSONObject(getString(R.string.keys_json_weather_coord));
                    lat = coords.getString(getString(R.string.keys_json_weather_lat));
                    lon = coords.getString(getString(R.string.keys_json_weather_lon));
                }

                if (weather.has(getString(R.string.keys_json_weather_main))) {
                    JSONObject main = weather.getJSONObject(getString(R.string.keys_json_weather_main));
                    temp = main.getString(getString(R.string.keys_json_weather_temp));
                }

                if (weather.has(getString(R.string.keys_json_weather_details))) {
                    JSONArray detailsArray = weather.getJSONArray(getString(R.string.keys_json_weather_details));
                    JSONObject detailsObject = (JSONObject) detailsArray.get(0); // lol will this casting even work?
                    mainDescript = detailsObject.getString(getString(R.string.keys_json_weather_main));
                }

                location = weather.optString(getString(R.string.keys_json_weather_city));

                mWeather = new WeatherObject.Builder(
                        temp, lat, lon).addDescription(mainDescript).addLocation(location).build();

                //updateInfo(mWeather);
                return mWeather;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ERROR!", e.getMessage());
                Log.e("WEATHER ERROR!", "null pointer in WeatherFragment.java bad json stuff");
            }
            return null; //oof this is about to break some stuff ^ log it for sure
        }

        protected void onPostExecute(WeatherObject result) {
            updateInfo(result);
        }
    }

    private void updateInfo(WeatherObject weather) {
        TextView temp = getActivity().findViewById(R.id.text_weather_temp);
        TextView coordinates = getActivity().findViewById(R.id.text_weather_coords);
        TextView deets = getActivity().findViewById(R.id.text_weather_details);

        // if the city field is not empty, fill with city info instead of coordinates
        // else fill this text view with coordinates
        if (!weather.getLocation().equals("") && !weather.getLocation().isEmpty()) {
            coordinates.setText(weather.getLocation());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Lat: ");
            sb.append(weather.getLat());
            sb.append((" Long: "));
            sb.append(weather.getLon());
            coordinates.setText(sb.toString());
        }

        // Set the temperature and weather description without other logic
        temp.setText(weather.getTemp() + "°");
        deets.setText(weather.getDesciption());
    }

    private class ForecastWeatherTask extends AsyncTask<String, Void, WeatherObject[]> {
        protected WeatherObject[] doInBackground(String... strings) {

            Uri uri_weather = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_weather))
                    .appendPath(getString(R.string.ep_weather_forecast))
                    .build();

            String resultString = "";
            HttpURLConnection urlConnection = null;

            try {
                URL urlObject = new URL(uri_weather.toString());
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
            //updateInfo(result);
            final Bundle args = new Bundle();
            args.putSerializable("weather", result);
            Navigation.findNavController(getView())
                    .navigate(R.id.action_nav_weather_to_viewWeatherFragment, args);
        }
    }




            // IF THE CITY FIELD IS NOT EMPTY, PUT CITY INSTEAD OF LAT/LONG IN THE PREVIEW BOX
            // BUILD THE MORE DETAILS BOX
        // LEARN TO PASS lat/long TO IT, ASK STERLING?
    // this thing needs to redo this whole call but for the forecast

    // INTEGRATE LAB 6 LOL, CURRENT LOCATION EASY WHO KNOWS ABOUT THE MAPS PART THO

    // port this to home screen somehow?


    // At some point (the end) I need to add authorization like pushy token or jwt or w/e

/*
    private void displayContact(final Contact theContact) {
        final Bundle args = new Bundle();

        MobileNavigationDirections.ActionGlobalViewProfileFragment directions
                = ViewProfileFragmentDirections.actionGlobalViewProfileFragment(theContact);

        Navigation.findNavController(getView())
                .navigate(directions);
    }
 */

/*
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Contact item);
    }
 */

/*
// Setting an onclick listener which passes the Contact to the method which navigates via a contact w/ contact param
 holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
 */





}