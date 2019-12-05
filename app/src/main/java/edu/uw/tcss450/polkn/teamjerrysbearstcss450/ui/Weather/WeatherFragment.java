package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather;

import android.app.Activity;
import android.bluetooth.BluetoothAssignedNumbers;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.ArrayList;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MainActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MobileNavigationDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ViewProfileFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.GetAsyncTask;

public class WeatherFragment extends Fragment {

    private WeatherObject mWeather;
    private WeatherObject mNextWeather;

    private boolean isUserDumbRightNow = false;

    private int mNextLocation = 1;
    private ArrayList<String[]> mSavedLocations = new ArrayList<>();
    private ArrayList<WeatherObject> mCurrentLocations = new ArrayList<>();

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
        ((HomeActivity) getActivity()).hideAddGroup();
        ((HomeActivity) getActivity()).hideAddUser();
        ((HomeActivity) getActivity()).hideViewProfile();
        ((HomeActivity) getActivity()).hideChatIcon();
        return root;
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handles making the keyboard disappear when you click elsewhere
        EditText zipText = getActivity().findViewById(R.id.editText_weatherFragment_zipSearch);
        zipText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        // Handles setup of search-by-zipcode functionality
        Button zipButton = getActivity().findViewById(R.id.button_weatherFragment_zipSearch);
        zipButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ZipCodeLookupTask().execute();
            }
        });

        // Handles navigation to WeatherForecast fragment
        ConstraintLayout lay = getActivity().findViewById(R.id.constraintLayout_weatherFragment_location1);
        lay.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ForecastWeatherTask().execute();
            }
        });

//        // Handles deleting weather locations///////////////////////////////////////////////////////////////////////
//        Button b = view.findViewById(R.id.button_current_save1);
//        b.setOnClickListener( new View.OnClickListener() {
//            public void onClick(View v ) {
//                // endpoint needs to addlocation with lat/long
//
//            }
//        });
        /*
        Button b = view.findViewById(R.id.button_login_login);
        b.setOnClickListener(butt -> onLoginClicked());
        b = view.findViewById(R.id.button_login_register);
        b.setOnClickListener(butt -> onRegisterClicked());*/
    }


    // make a call to the web service to get current weather
    // build a weather object
    // populate the fields with the weather data
    // also build the weather special data class
    @Override
    public void onStart() {
        super.onStart();

        // Grab our param WeatherObject and display it
        // jk just get it now .. add this asynctask to the sidebar when you click weather fragment tho


        Uri uri_weather = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .appendPath(getString(R.string.ep_weather_params))
                .appendQueryParameter("lat", "47.2446")
                .appendQueryParameter("lon", "-122.4376")
                .build();

        new CurrentWeatherTask().execute(uri_weather.toString());

        // fill in the other saved location views
        new SavedLocationsTask().execute();
        mNextLocation = 1;
//        ConstraintLayout lay = getActivity().findViewById(R.id.constraintLayout_weatherFragment_location1);
//        lay.setOnClickListener( new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new ForecastWeatherTask().execute();
//            }
//        }); done in onViewCreated now
    }

    private class SavedLocationsTask extends AsyncTask<String, Void, ArrayList<String[]>> {
        protected ArrayList<String[]> doInBackground(String... strings) {

            Uri uri_weather = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_weather))
                    .appendPath(getString(R.string.ep_weather_getlocations))
                    .appendQueryParameter("username", ((HomeActivity) getActivity()).getmUsername())
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
                // build the ArrayList of the [lat, long] arrays which hold my saved locations
                mSavedLocations.clear();
                JSONObject bigObject = new JSONObject(resultString);
                JSONArray arrayOfLocations = bigObject.getJSONArray("msg");
                for (int i=0; i < arrayOfLocations.length(); i++) {
                    JSONObject coords = arrayOfLocations.getJSONObject(i);
                    String lat = coords.getString("lat");
                    String lon = coords.getString("long");
                    mSavedLocations.add(new String[] {lat, lon});
                }
                Log.e("saving locations", "returning mSavedLocations from SavedLocationsTask");
                return mSavedLocations;

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ERROR!", e.getMessage());
                Log.e("WEATHER ERROR!", "failed during SavedLocationsTask");
            }
            return null; //oof this is about to break some stuff ^ log it for sure
        }

        protected void onPostExecute(ArrayList<String[]> result) {
            Log.e("mSavedLocations", result.toString());
            Log.e("mSavedLocations", ""+result.size());

            for (int i=0; i<result.size(); i++) {
                if (i < 4) {
                    Log.e("mSavedLocations", "i=" + i);
                    Log.e("mSavedLocations", "i=" + i + result.get(i)[0] + " " + result.get(i)[1]);

                    Log.e("mSavedLocations", result.get(i).toString());
                    new DifferentLocationWeatherTask().execute(result.get(i));
                }
            }
        }


    }



    private class CurrentWeatherTask extends AsyncTask<String, Void, WeatherObject> {
        protected WeatherObject doInBackground(String... strings) {
            Log.e("CurrentWeatherTask", "CurrentWeatherTask");

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
        TextView temp = getActivity().findViewById(R.id.textView_current_temp1);
        TextView coordinates = getActivity().findViewById(R.id.textView_current_location1);
        TextView deets = getActivity().findViewById(R.id.textView_current_descrip1);

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
                    .navigate(R.id.action_nav_weather_to_viewWeatherFragment, args);
        }

//        private void updateIcon(ImageView imageView) {
//
//        }
    }

    private String getZipString() {
        EditText et = getActivity().findViewById(R.id.editText_weatherFragment_zipSearch);
//        Log.e("zipcode", et.getText().toString());
        String zip = et.getText().toString();
        et.setText("");
        return zip;
    }


    private class ZipCodeLookupTask extends AsyncTask<String, Void, String[]> {
        protected String[] doInBackground(String... strings) {
            Log.e("zipcodelookuptask", "starting doinbackground");
            //ONLY CALL THIS ONCE
            final String zipString = getZipString();
            Log.e("ZipCodeLookupWeatherTask", zipString);



            String zipcode = zipString;

            Uri uri_weather = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_weather))
                    .appendPath(getString(R.string.ep_weather_getCordsFromZipCode))
                    .appendQueryParameter("zip", zipcode)
                    .build();

            Log.e("google json connection url", uri_weather.toString());

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
                Log.e("ERROR CONNECTING ZIPLOOKUP", "Unable to connect, Reason: " + e.getMessage());
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            try {
                JSONObject outter = new JSONObject(resultString);
                Log.e("google json", outter.toString());
                JSONArray outterArray= outter.getJSONArray("results");
                JSONObject outterObject = outterArray.getJSONObject(0);
                JSONObject geometryObject = outterObject.getJSONObject("geometry");
                JSONObject locationObject = geometryObject.getJSONObject("location");
                String lat = locationObject.getString("lat");
                String lon = locationObject.getString("lng");

                String[] coords = new String[] {lat, lon};
                Log.e("zipcode", coords[0] + " " + coords[1]);
                return coords;

            } catch(JSONException e) {
                isUserDumbRightNow = true;
                e.printStackTrace();
                Log.e("ERROR!", e.getMessage());
                Log.e("ZIPCODE ERROR!", "null pointer in WeatherFragment.java bad json stuff");
            }
            return null; //oof this is about to break some stuff ^ log it for sure
        }
/*  THIS IS FOR A DIFFERENT WEB SERVICE, BUT IT SUCKS SO USE THE ABOVE CODE ^^^
            // Now we have the endpoint result from our zipcode-to-coordinates endpoint

            try {
                String lat = "", lon ="";
                String[] coords = new String[2];

                JSONObject obj = new JSONObject(resultString);
//                Log.e("printing json", obj.toString());

                lat = obj.getString("lat");
                lon = obj.getString("lng"); // not lon for this one! lol @ string resources

                coords[0] = lat;
                coords[1] = lon;

                // Set up a new Weather View panel with this information populating the view
                //displayAnotherLocation(lat, lon);

                return coords;
            } catch (JSONException e) {
                // tell user there was bad input
                isUserDumbRightNow = true;

                e.printStackTrace();
                Log.e("ERROR!", e.getMessage());
                Log.e("WEATHER ERROR!", "null pointer in WeatherFragment.java bad json stuff");
            }
            return null; //oof this is about to break some stuff ^ log it for sure
        }*/

        protected void onPostExecute(String[] result) {
            Log.e("user dumb", ""+isUserDumbRightNow);
            if (!isUserDumbRightNow) {
                new DifferentLocationWeatherTask().execute(result);
                // add this location to our saved weather locations
                // call to webservice adding this location goes here
                new SaveThisLocationTask().execute(result);
            }
            else {
                // maybe they'll learn, let's give them another chance :)
                isUserDumbRightNow = false;
            }
        }

    }

    private class SaveThisLocationTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... strings) {
            Log.e("Saving Locations Now", "thislat " + strings[0] + "long " + strings[1]);
            Uri uri_weather = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_weather))
                    .appendPath(getString(R.string.ep_weather_addlocation))
                    .appendQueryParameter("username", ((HomeActivity) getActivity()).getmUsername())
                    .appendQueryParameter("lat", strings[0])
                    .appendQueryParameter("long", strings[1])
                    .build();

            String resultString = "";
            HttpURLConnection urlConnection = null;

            try { //honestly don't even need this part.. not even pretending to check this but keeping it in case of debug
                URL urlObject = new URL(uri_weather.toString());
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    resultString += s;
                }
                Log.e("saved location", resultString);
            } catch (Exception e) {
                Log.e("ERROR CONNECTING", "Unable to connect, Reason: " + e.getMessage());
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            Log.e("LEAVING SAVETHISLOCATIONTASK", "here");
            return null;
        }
    }


        private class DifferentLocationWeatherTask extends AsyncTask<String, Void, WeatherObject> {
        protected WeatherObject doInBackground(String[] strings) {

            Log.e("differentlocationweathertask", "lat of this one: " + strings[0]);
            Log.e("differentlocationweathertask", "lon of this one: " + strings[1]);

            Uri uri_weather = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_weather))
                    .appendPath(getString(R.string.ep_weather_params))
                    .appendQueryParameter("lat", strings[0])
                    .appendQueryParameter("lon", strings[1])
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
                Log.e("differentlocationweathertask", "result of api" + resultString);
//                Log.e("weather here", "here0");

                String lat = "", lon = "", temp = "", mainDescript = "", location ="", country ="";
                JSONObject weather = new JSONObject(resultString);
                Log.e("weather json", weather.toString());
                if (weather.has(getString(R.string.keys_json_weather_coord))) {
                    JSONObject coords = weather.getJSONObject(getString(R.string.keys_json_weather_coord));
                    lat = coords.getString(getString(R.string.keys_json_weather_lat));
                    lon = coords.getString(getString(R.string.keys_json_weather_lon));
                }
//                Log.e("weather here", "here1");

                if (weather.has(getString(R.string.keys_json_weather_main))) {
                    JSONObject main = weather.getJSONObject(getString(R.string.keys_json_weather_main));
                    temp = main.getString(getString(R.string.keys_json_weather_temp));
                }
//                Log.e("weather here", "here2");

                if (weather.has(getString(R.string.keys_json_weather_details))) {
                    JSONArray detailsArray = weather.getJSONArray(getString(R.string.keys_json_weather_details));
                    JSONObject detailsObject = (JSONObject) detailsArray.get(0); // lol will this casting even work?
                    mainDescript = detailsObject.getString(getString(R.string.keys_json_weather_main));
                }

                if (weather.has("sys")) {
                    JSONObject sys = weather.getJSONObject("sys");
                    country = sys.getString("country");
                }
//                Log.e("weather here", "here3");


                location = weather.optString(getString(R.string.keys_json_weather_city));
//                Log.e("weather here", "here4");

                Log.e("nextweather", "temp "+temp);
                Log.e("nextweather", "lat" + lat);
                Log.e("nextweather", "lon" + lon);
                Log.e("nextweather", "maindescript" +  mainDescript);
                Log.e("nextweather", "location" + location);


                mNextWeather = new WeatherObject.Builder(
                        temp, lat, lon).addDescription(mainDescript).addLocation(location + ", " + country).build();
//                Log.e("weather here", "here4");

                //updateInfo(mWeather);
                return mNextWeather;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ERROR!", e.getMessage());
                Log.e("WEATHER ERROR!", "null pointer in WeatherFragment.java bad json stuff");
            }
            return null; //oof this is about to break some stuff ^ log it for sure
        }

        protected void onPostExecute(WeatherObject result) {
//            Log.e("weather here", "here999updateInfo");
            displayAnotherLocation(result);
        }
    }

    private void displayAnotherLocation(WeatherObject theWeather) {
        mNextLocation++;

        if (mNextLocation > 5)
            return;

        String tempName, locationName, descriptionName, backgroundName, idName;
        tempName = getString(R.string.text_current_temp_notnumbered) + mNextLocation;
        descriptionName = getString(R.string.text_current_description_notnumbered) + mNextLocation;
        locationName = getString(R.string.text_current_location_notnumbered) + mNextLocation;
        backgroundName = getString(R.string.constraint_current_background_notnumbered) + mNextLocation;
        idName = getString(R.string.image_current_notnumbered) + mNextLocation;

        int tempId = getResources().getIdentifier(tempName, "id", MainActivity.PACKAGE_NAME);
        int descriptionId = getResources().getIdentifier(descriptionName, "id", MainActivity.PACKAGE_NAME);
        int locationId = getResources().getIdentifier(locationName, "id", MainActivity.PACKAGE_NAME);
        int backgroundId = getResources().getIdentifier(backgroundName, "id", MainActivity.PACKAGE_NAME);
        int imageId = getResources().getIdentifier(idName, "id", MainActivity.PACKAGE_NAME);

        TextView temp = getActivity().findViewById(tempId);
        TextView desciption = getActivity().findViewById(descriptionId);
        TextView location = getActivity().findViewById(locationId);
        ConstraintLayout background = getActivity().findViewById(backgroundId);
        ImageView image = getActivity().findViewById(imageId);

        temp.setText(theWeather.getTemp());
        desciption.setText(theWeather.getDesciption());
        location.setText(theWeather.getLocation());

        temp.setVisibility(View.VISIBLE);
        desciption.setVisibility((View.VISIBLE));
        location.setVisibility(View.VISIBLE);
        background.setVisibility(View.VISIBLE);
        image.setVisibility(View.VISIBLE);

        /*
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
         */
    }


/*
 @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button b = view.findViewById(R.id.button_login_login);
        b.setOnClickListener(butt -> onLoginClicked());
        b = view.findViewById(R.id.button_login_register);
        b.setOnClickListener(butt -> onRegisterClicked());
    }


    private void onLoginClicked() {
        View v = getView();

        EditText email = v.findViewById(R.id.editText_login_email);
        EditText pw = v.findViewById(R.id.editText_login_pw);
        if (MainActivity.validateEmail(email) && MainActivity.validatePassword(pw)) {
            doLogin(new Credentials.Builder(
                    email.getText().toString(), pw.getText().toString()
            ).build());
        }
    }

 */




}