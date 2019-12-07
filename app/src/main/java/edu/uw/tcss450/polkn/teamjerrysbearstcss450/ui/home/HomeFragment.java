package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.home;

import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
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
import java.util.Map;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivity;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.HomeActivityArgs;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.MobileNavigationDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.model.Credentials;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatViewFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.Message.Message;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather.WeatherFragment;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupContact.GroupContact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather.WeatherObject;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.GetAsyncTask;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private WeatherObject mWeather;

    private List<String> mNames;
    private int[] mChatIds;

    private String mEmail;
    private String mJwToken;

    private TextView view1;
    private TextView view2;
    private TextView view3;
    private List<TextView> mChatViews;
    private List<View> mLayoutFavorites;
    private List<View> mRemoveFavorites;
    private View mView;

    private TextView mFavoritesTitle;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeActivityArgs args = HomeActivityArgs.fromBundle(getArguments());
        Credentials credentials = args.getCredentials();
//        ((TextView) getActivity().findViewById(R.id.text_email)).
//                setText(credentials.getEmail());
        mJwToken = args.getJwt();
        mEmail = credentials.getEmail();
        Log.d("JWT", mJwToken);
        Log.d("Email", mEmail);
        homeViewModel = ViewModelProviders.of(getActivity(),
                new MyHomeViewModelFactory(mEmail,  mJwToken)).get(HomeViewModel.class);
        Log.d("homeViewModel from home", homeViewModel.toString());
//        homeViewModel =
//                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFavoritesTitle = view.findViewById(R.id.textView_home_favorites);
        mView = view;
        mChatIds = new int[3];
        mChatViews = new ArrayList<TextView>();
        mLayoutFavorites = new ArrayList<View>();
        mRemoveFavorites = new ArrayList<>();
        View remove1 = view.findViewById(R.id.button_delete1);
        view1 = view.findViewById(R.id.button_chat1);
        View layout1 = view.findViewById(R.id.layout1);
        mChatViews.add(view1);
        mLayoutFavorites.add(layout1);
        view2 = view.findViewById(R.id.button_chat2);
        View layout2 = view.findViewById(R.id.layout2);
        mChatViews.add(view2);
        View remove2 = view.findViewById(R.id.button_delete2);
        mLayoutFavorites.add(layout2);
        view3 = view.findViewById(R.id.button_chat3);
        View layout3 = view.findViewById(R.id.layout3);
        View remove3 = view.findViewById(R.id.button_delete3);
        mLayoutFavorites.add(layout3);
        mChatViews.add(view3);
        Log.d("chatviews?", mChatViews.toString());
        mRemoveFavorites.add(remove1);
        mRemoveFavorites.add(remove2);
        mRemoveFavorites.add(remove3);

        final TextView textView = view.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        homeViewModel.getFavoriteChats().observe(this, new Observer<Map<Integer, GroupContact>>() {
            @Override
            public void onChanged(@Nullable Map<Integer, GroupContact> favorites) {
                Object[] keyArray = favorites.keySet().toArray();
                Log.d("favorites observe triggered", favorites.toString());
                int i;
                for (i = 0; i < keyArray.length && i < mChatViews.size(); i++) {
                    mFavoritesTitle.setVisibility(View.VISIBLE);
                    GroupContact current = favorites.get(keyArray[i]);
                    //if the chat is a person to person chat, set the text to be the name of the chatmember
                    if (current.getGroupname().equals("primary")) {
                        mLayoutFavorites.get(i).setVisibility(View.VISIBLE);
                        mChatViews.get(i).setText("Chat with: " + current.getContact().get(0).getUsername());
                    } else {//otherwise set the text to be the groupname
                        Log.d("should be a group chat", current.getGroupname());
                        StringBuilder sb = new StringBuilder("Group chat: ");
                        sb.append(current.getGroupname());
                        mChatViews.get(i).setText(sb.toString());
                        mLayoutFavorites.get(i).setVisibility(View.VISIBLE);
                    }
                    mChatIds[i] = current.getChatId();
                }
                for (;i < mChatViews.size(); i++) {
                    if (i == 0)
                        mFavoritesTitle.setVisibility(View.GONE);
                    Log.d("should hide", i+"");
                    mLayoutFavorites.get(i).setVisibility(View.GONE);
                }
            }
        });
        ConstraintLayout constraint = getActivity().findViewById(R.id.constraintLayout_home_location1);
        constraint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new ForecastWeatherTask().execute();
            }
        });

        //        TextView test = view.findViewById(R.id.textView_email);

        view1 = view.findViewById(R.id.button_chat1);
        view1.setOnClickListener(b -> chatClicked(mChatIds[0], homeViewModel.getFavoriteChats().getValue().get(mChatIds[0]).getGroupname()));


        view2 = view.findViewById(R.id.button_chat2);
        view2.setOnClickListener(b -> chatClicked(mChatIds[1], homeViewModel.getFavoriteChats().getValue().get(mChatIds[1]).getGroupname()));


        view3 = view.findViewById(R.id.button_chat3);
        view3.setOnClickListener(b -> chatClicked(mChatIds[2], homeViewModel.getFavoriteChats().getValue().get(mChatIds[2]).getGroupname()));
        remove1.setOnClickListener((b -> removeFavoriteClicked(mChatIds[0])));
        remove2.setOnClickListener((b -> removeFavoriteClicked(mChatIds[1])));
        remove3.setOnClickListener((b -> removeFavoriteClicked(mChatIds[2])));

    }
    private void removeFavoriteClicked(int chatid) {
        Log.d("remove a favorite", chatid+"");
        Map<Integer, GroupContact> currentFavoriteChats = homeViewModel.getFavoriteChats().getValue();
        if(currentFavoriteChats != null && currentFavoriteChats.keySet().size() == 0) {
            Toast toast = Toast.makeText(getActivity().getBaseContext(), "No favorites",
                    Toast.LENGTH_LONG);
            View view = toast.getView();
            view.setBackgroundResource(R.drawable.customborder_greypurple);
            toast.show();
            return;
        }
        try {
            Log.d("remove a favorite", chatid+"");
            JSONObject req = new JSONObject();
            req.put("chatid", chatid);
            req.put("email", mEmail);
            String url = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_messaging_base))
                    .appendPath(getString(R.string.ep_messaging_removefavorite))
                    .build()
                    .toString();
            new SendPostAsyncTask.Builder(url, req).
                    addHeaderField("authorization", mJwToken).
                    onPostExecute(this::handleRemoveFavoritesOnPost).build().execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayMessage(final Message theMessage) {
        final Bundle args = new Bundle();
    }

    private void handleRemoveFavoritesOnPost(String result) {
        try {
            //This is the result from the web service
            JSONObject res = new JSONObject(result);
            Log.d("removed?", res.toString());
            if(res.has(getString(R.string.keys_json_success))  && res.getBoolean(getString(R.string.keys_json_success))) {
                Toast toast = Toast.makeText(getActivity().getBaseContext(), R.string.toast_chat_favoriteremoved,
                        Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.customborder_greypurple);
                toast.show();
                homeViewModel.removeFavorite(res.getInt("chatid"));
            } else if (res.has(getString(R.string.keys_json_success))) {
                Log.d("bummer man", res.getJSONObject("error").toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void chatClicked(int chatid, String chatName) {
        MobileNavigationDirections.ActionGlobalNavChat directions
                = ChatViewFragmentDirections.actionGlobalNavChat().setChatid(chatid)
                .setJwt(mJwToken).setUsername(((HomeActivity)getActivity()).getmUsername()).setChatname(chatName);

        Navigation.findNavController(mView).navigate(directions);
    }

    //fills in weather panel with current weather
    @Override
    public void onStart() {
        super.onStart();
        new CurrentWeatherTask().execute();
        homeViewModel.loadFavorites();
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
}