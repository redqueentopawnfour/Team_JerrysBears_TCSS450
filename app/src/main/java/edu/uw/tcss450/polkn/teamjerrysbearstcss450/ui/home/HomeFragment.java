package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.home;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivityArgs;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MobileNavigationDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.model.Credentials;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather.WeatherFragment;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather.WeatherObject;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.GetAsyncTask;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private WeatherObject mWeather;

    private List<String> mNames;
    private List<Integer> mChatIds;

//    private HashMap<Integer, String> mContacts; // for the most recent

    private String mEmail;
    private String mJwToken;

    private TextView view1;
    private TextView view2;
    private TextView view3;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mNames = new ArrayList<>();
        mChatIds = new ArrayList<>();



        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        TextView test = view.findViewById(R.id.textView_email);

        HomeActivityArgs args = HomeActivityArgs.fromBundle(getArguments());
        Credentials credentials = args.getCredentials();
        ((TextView) getActivity().findViewById(R.id.text_email)).
                setText(credentials.getEmail());
        mJwToken = args.getJwt();
        mEmail = credentials.getEmail();
        Log.d("JWT", mJwToken);
        Log.d("Email", mEmail);

        ConstraintLayout constraint = getActivity().findViewById(R.id.constraintLayout_home_location1);
        constraint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new ForecastWeatherTask().execute();
            }
        });

        //        TextView test = view.findViewById(R.id.textView_email);

        view1 = view.findViewById(R.id.button_chat1);
        view1.setOnClickListener(b -> chatClicked(mChatIds.get(0)));


        view2 = view.findViewById(R.id.button_chat2);
        view2.setOnClickListener(b -> chatClicked(mChatIds.get(1)));


        view3 = view.findViewById(R.id.button_chat3);
        view3.setOnClickListener(b -> chatClicked(mChatIds.get(2)));

    }

    private void chatClicked(Integer integer) {

    }

    //fills in weather panel with current weather
    @Override
    public void onStart() {
        super.onStart();
        new CurrentWeatherTask().execute();
        Log.e("onStart", "calling onStart");
    }

    private class CurrentWeatherTask extends AsyncTask<String, Void, WeatherObject> {
        protected WeatherObject doInBackground(String... strings) {
            Log.e("CurrentWeatherTask", "CurrentWeatherTask");

            double latdouble = ((HomeActivity) getActivity()).getLatitude();
            double londouble = ((HomeActivity) getActivity()).getLonitude();

            Uri uri_weather = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_weather))
                    .appendPath(getString(R.string.ep_weather_params))
                    .appendQueryParameter("lat", ""+latdouble)
                    .appendQueryParameter("lon", ""+londouble)
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
                String lat = "", lon = "", temp = "", mainDescript = "", location ="", country ="";
                JSONObject weather = new JSONObject(resultString);
                Log.e("weather json", weather.toString());
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
                if (weather.has("sys")) {
                    JSONObject sys = weather.getJSONObject("sys");
                    country = sys.getString("country");
                }

                location = weather.optString(getString(R.string.keys_json_weather_city));

                mWeather = new WeatherObject.Builder(
                        temp, lat, lon).addDescription(mainDescript).addLocation(location + ", " + country).build();
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
        TextView temp = getActivity().findViewById(R.id.textView_home_temp1);
        TextView coordinates = getActivity().findViewById(R.id.textView_home_location1);
        TextView deets = getActivity().findViewById(R.id.textView_home_descrip1);
        ImageView image = getActivity().findViewById(R.id.imageView_home1);
        ConstraintLayout lay = getActivity().findViewById(R.id.constraintLayout_home_location1);

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

        WeatherFragment.setCorrectIcon(image, weather);

        temp.setVisibility(View.VISIBLE);
        coordinates.setVisibility(View.VISIBLE);
        deets.setVisibility(View.VISIBLE);
        image.setVisibility(View.VISIBLE);
        lay.setVisibility(View.VISIBLE);

    }

    private class ForecastWeatherTask extends AsyncTask<String, Void, WeatherObject[]> {
        protected WeatherObject[] doInBackground(String... strings) {

            Uri uri_weather = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_weather))
                    .appendPath(getString(R.string.ep_weather_forecast))
                    .appendQueryParameter("lat", mWeather.getLat())
                    .appendQueryParameter("lon", mWeather.getLon())
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
                String lat = "", lon = "", temp = "", mainDescript = "", location ="", country ="", date ="";
                WeatherObject[] weathers = new WeatherObject[10];

                // Retrieve JSON objects according to api docs
                JSONObject arrayOfForecastsObject = new JSONObject(resultString);
                JSONArray arrayOfForecasts = arrayOfForecastsObject.getJSONArray("data");

                // Set information not dependant on time
                lat = arrayOfForecastsObject.getString(getString(R.string.keys_json_weather_forecast_lat));
                lon = arrayOfForecastsObject.getString(getString(R.string.keys_json_weather_forecast_lon));
                location = arrayOfForecastsObject.getString(getString(R.string.keys_json_weather_forecast_city)); //city
                country = arrayOfForecastsObject.getString("country_code");


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
                            temp, lat, lon).addDescription(mainDescript).addLocation(location + ", " + country).addDate(date).build();
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
                    .navigate(R.id.action_global_viewWeatherFragment, args);
        }

    }


    /**
     * Method to load chat history
     */
    private void loadRecentChatHistory() {
        String getUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_home_getfavorite))
                .build()
                .toString();
        new GetAsyncTask.Builder(getUrl).addHeaderField("email", mEmail)
                .addHeaderField("authorization", mJwToken)
                .onCancelled(error -> Log.e("an error", error))
                .onPostExecute(this::endOfLoadChatTask).build().execute();
//        Log.d("TESTING MESSAGE:", mMessage.toString());
    }

    private void endOfLoadChatTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);
            if (res.has(getString(R.string.keys_json_success))
                    && !res.getBoolean(getString(R.string.keys_json_success))) {
            } else {
//                JSONArray chats = (JSONArray) res.get(getString(R.string.keys_json_messages));
                JSONArray details = (JSONArray) res.get("details");
                for (int i = 0; i < details.length(); i++) {
                    JSONObject temp = (JSONObject) details.get(i);
                    String tempName = temp.getString("chatname");
                    int tempChatId = temp.getInt("chatid");
                    mNames.add(tempName);
                    mChatIds.add(tempChatId);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();

            Log.d("TESTING Names:", mNames.toString());
            Log.d("TESTING Chat ID:", mChatIds.toString());
        }
    }
}