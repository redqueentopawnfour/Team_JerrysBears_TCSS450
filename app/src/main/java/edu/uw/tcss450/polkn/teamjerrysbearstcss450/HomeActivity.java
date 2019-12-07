package edu.uw.tcss450.polkn.teamjerrysbearstcss450;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ColorFilter;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;


import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatMessageNotification;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatViewFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupChatFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactFragment;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactNotification;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ViewProfileFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather.WeatherFragment;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Weather.WeatherObject;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.PushReceiver;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.model.Credentials;

import me.pushy.sdk.Pushy;

public class HomeActivity extends AppCompatActivity {

    private String mJwToken;
    private String mEmail;

    private Credentials mCredentials;
    private AppBarConfiguration mAppBarConfiguration;
    private MenuItem mAddContacts;
    private MenuItem mViewOwnProfile;
    private MenuItem mChat;
    private MenuItem mNavContactList;
    private MenuItem mAddGroup;
    private MenuItem mDisplayGroup;

    private Contact mMyProfile;
    private ChatMessageNotification mChatMessage;
    private ContactNotification mContactNotification;
    private Contact[] mContacts;



    private ColorFilter mDefault;
    private HomePushMessageReceiver mPushMessageReciever;

    public Location mLocation;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // A constant int for the permissions request code. Must be a 16 bit number
    private static final int MY_PERMISSIONS_LOCATIONS = 8414;

    private LocationRequest mLocationRequest;

    //Use a FusedLocationProviderClient to request the location
    private FusedLocationProviderClient mFusedLocationClient;

    // Will use this call back to decide what to do when a location change is detected
    private LocationCallback mLocationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_contactList, R.id.nav_groupChat,R.id.nav_weather)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.setGraph(R.navigation.mobile_navigation, getIntent().getExtras());

        HomeActivityArgs args = HomeActivityArgs.fromBundle(getIntent().getExtras());
        mCredentials = args.getCredentials();
        mJwToken = args.getJwt();
        mEmail = args.getCredentials().getEmail();
        Log.d("JWT", mJwToken);

        populateCurrentProfile();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (args.getChatMessage() != null) {
                            MobileNavigationDirections.ActionGlobalNavChat directions =
                                    ChatViewFragmentDirections.actionGlobalNavChat().setJwt(mJwToken).setEmail(mEmail);
//                            directions.setMessage(args.getChatMessage());
                            int chatId = args.getChatId();
                            if (chatId != 0) {
                                directions.setChatid(chatId);
                            }
                            navController.navigate(directions);
                        } else if (args.getContactMessage() != null) {
                            loadContacts(HomeActivity.this::handleContactsOnPostExecute);
                        }
                    }
                },
                500);

        navigationView.setNavigationItemSelectedListener(this::onNavigationSelected);
        mDefault = toolbar.getNavigationIcon().getColorFilter();

        Menu menuNav = navigationView.getMenu();
        mNavContactList = menuNav.findItem(R.id.nav_contactList);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                            , Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_LOCATIONS);
        } else {
            //The user has already allowed the use of Locations. Get the current location.
            requestLocation();
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    Log.d("LOCATION UPDATE!", location.toString());
                }
            };
        };

        createLocationRequest();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // locations-related task you need to do.
                    requestLocation();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("PERMISSION DENIED", "Nothing to see or do here.");

                    //Shut down the app. In production release, you would let the user
                    //know why the app is shutting down...maybe ask for permission again?
                    finishAndRemoveTask();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Create and configure a Location Request used when retrieving location updates
     */
    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("REQUEST LOCATION", "User did NOT allow permission to request location!");
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                mLocation = location;
                                Log.d("LOCATION", location.toString());
                            }
                        }
                    });
        }
    }



    /**
     * Requests location updates from the FusedLocationApi.
     */
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mPushMessageReciever == null) {
            mPushMessageReciever = new HomePushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        registerReceiver(mPushMessageReciever, iFilter);
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReciever != null) {
            unregisterReceiver(mPushMessageReciever);
        }
        stopLocationUpdates();
    }

    private boolean onNavigationSelected(final MenuItem menuItem) {
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);

        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                mChat.setVisible(false);                        // NP 11/23/2019- Set icons to invisible from here when navigating to Home - all other icon handling done in individual fragments
                mViewOwnProfile.setVisible(false);              // but since HomeFragment loads before the menu inflater, HomeFragment must be handled from HomeActivity
                mAddContacts.setVisible(false);
                mAddGroup.setVisible(false);
                mDisplayGroup.setVisible(false);

                navController.navigate(R.id.nav_home, getIntent().getExtras());
                break;
            case R.id.nav_contactList:
                Uri uri_contacts = new Uri.Builder()
                        .scheme("https")
                        .appendPath(getString(R.string.ep_base_url))
                        .appendPath(getString(R.string.ep_contacts))
                        .build();

                String email = mCredentials.getEmail();
                String json = "{\"email\":\"" + email + "\"}";

                try {
                    JSONObject jsonEmail = new JSONObject(json);
                    new SendPostAsyncTask.Builder(uri_contacts.toString(), jsonEmail)
                            .onPostExecute(this::handleContactsOnPostExecute)
                            .build().execute();

                } catch (Throwable tx) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }
                break;
            case R.id.nav_weather:


                navController.navigate(R.id.nav_weather);
                break;
            case R.id.nav_chat:
                ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon().setColorFilter(mDefault);

                MobileNavigationDirections.ActionGlobalNavChat directions;
                if (mChatMessage != null) {

                    Log.d("Message", mChatMessage.getMessage());

                    directions = ChatViewFragmentDirections.actionGlobalNavChat()
                            .setEmail(mEmail)
                            .setJwt(mJwToken);
//                            .setMessage(mChatMessage);
                } else {
                    directions = ChatViewFragmentDirections.actionGlobalNavChat()
                            .setEmail(mEmail)
                            .setJwt(mJwToken);
                }

                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .navigate(directions);
                break;

            case R.id.nav_groupChat:

                ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon().setColorFilter(mDefault);

                MobileNavigationDirections.ActionGlobalNavGroupChat direction;

                direction = GroupChatFragmentDirections.actionGlobalNavGroupChat(mMyProfile)
                        .setJwt(mJwToken)
                        .setContact(mContacts);
                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .navigate(direction);
                break;

        }

        //Close the drawer
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        mAddContacts = menu.findItem(R.id.action_addContact);
        mViewOwnProfile = menu.findItem(R.id.action_viewOwnProfile);
        mChat = menu.findItem(R.id.action_chat);
        mAddGroup = menu.findItem(R.id.action_addGroup);
        mDisplayGroup = menu.findItem(R.id.action_display);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_addContact) {
            NavController navController =
                    Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.navigate(R.id.nav_addContactFragment, getIntent().getExtras());
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == 16908332) {    // Reloads contact list if the current fragment is add contacts or view profile
            NavController navController =
                    Navigation.findNavController(this, R.id.nav_host_fragment);
            NavDestination nd = navController.getCurrentDestination();
            if (nd.getId() == R.id.nav_addContactFragment) {
                loadContacts(this::handleContactsOnPostExecute);
            } else if (nd.getId() == R.id.nav_viewProfileFragment) {
                loadContacts(this::handleContactsOnPostExecute);
            }
            return super.onOptionsItemSelected(item);
        } else if (id == R.id.action_viewOwnProfile) {
            MobileNavigationDirections.ActionGlobalViewProfileFragment directions
                    = ViewProfileFragmentDirections.actionGlobalViewProfileFragment(mMyProfile);

            directions.setIsOwnProfile(true);

            Navigation.findNavController(this, R.id.nav_host_fragment)
                    .navigate(directions);
            return true;
        } else if (id == R.id.action_chat) {
            MobileNavigationDirections.ActionGlobalNavChat directions
                    = ChatViewFragmentDirections.actionGlobalNavChat();

            Navigation.findNavController(this, R.id.nav_host_fragment)
                    .navigate(directions);
            return true;
        } else if (id == R.id.action_addGroup) {
            loadContacts(this::handleAddGroupOnPostExecute);

        }
//        } else if (id  == R.id.action_display) {
//            MobileNavigationDirections.
//        }

        return super.onOptionsItemSelected(item);
    }

    private void loadContacts(Consumer<String> onPost) {
        Uri uri_contacts = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_contacts))
                .build();

        String email = mCredentials.getEmail();
        String json = "{\"email\":\"" + email + "\"}";

        try {
            JSONObject jsonEmail = new JSONObject(json);

            Log.d("Email string", jsonEmail.toString());
            Log.d("Email", jsonEmail.getString("email"));

            new SendPostAsyncTask.Builder(uri_contacts.toString(), jsonEmail)
                    .onPostExecute(onPost)
                    .build().execute();

        } catch (Throwable tx) {
            Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



    private void handleAddGroupOnPostExecute(final String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);

            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_success));

            if (success) {
                String message = resultsJSON.getString(getString(R.string.keys_json_message));

                if (message.equals("no contacts found")) {
//                    int drawableId = getResources().getIdentifier(mMyProfile.getUserIcon(), "drawable", getPackageName());
//                    mViewOwnProfile.setIcon(drawableId);
                    Log.d("no contacts found", resultsJSON.toString());
                    MobileNavigationDirections.ActionGlobalNavGroupChat directions
                            = GroupChatFragmentDirections.actionGlobalNavGroupChat(mMyProfile);
                } else {
                    if (resultsJSON.has(getString(R.string.keys_json_message))) {
                        JSONArray data = resultsJSON.getJSONArray(
                                getString(R.string.keys_json_message));
                        int verifiedCount = 0;
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonContact = data.getJSONObject(i);
                            if (jsonContact.getBoolean(getString(R.string.keys_json_contacts_isContactVerified))) {
                                verifiedCount++;
                            }
                        }
                        mContacts = new Contact[verifiedCount];
                        int j = 0;
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonContact = data.getJSONObject(i);
                            if(jsonContact.getBoolean(getString(R.string.keys_json_contacts_isContactVerified))) {
                                mContacts[j++] = new Contact.Builder(
                                        jsonContact.getString(
                                                getString(R.string.keys_json_contact_username)),
                                        jsonContact.getString(
                                                getString(R.string.keys_json_contact_usericon)))
                                        .addFirstName(jsonContact.getString(
                                                getString(R.string.keys_json_contact_firstname)))
                                        .addLastName(jsonContact.getString(
                                                getString(R.string.keys_json_contact_lastname)))
                                        .addEmail(jsonContact.getString(
                                                getString(R.string.keys_json_contact_email)))
                                        .addIsEmailVerified(jsonContact.getBoolean(
                                                getString(R.string.keys_json_contacts_isEmailVerified)))
                                        .addRequestNumber(jsonContact.getInt(
                                                getString(R.string.keys_json_contacts_requestNumber)))
                                        .addIsContactVerified(jsonContact.getBoolean(
                                                getString(R.string.keys_json_contacts_isContactVerified)))
                                        .addChatId(jsonContact.getInt(
                                                getString(R.string.keys_json_contacts_chatId)))
                                        .build();
                            }
                        }

                        GroupChatFragmentDirections.ActionNavGroupChatToGroupContactFragment directions
                                = GroupChatFragmentDirections.actionNavGroupChatToGroupContactFragment(mMyProfile);
                        directions.setJwt(mJwToken);
                        directions.setContacts(mContacts);

                        NavController nc = Navigation.findNavController(this, R.id.nav_host_fragment);
                        nc.navigate(directions);
                    } else {
                        Log.e("ERROR!", "No response");
                    }
                }
            } else {
                // failure
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }

    private void handleContactsOnPostExecute(final String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);

            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_success));

            if (success) {
                String message = resultsJSON.getString(getString(R.string.keys_json_message));

                if (message.equals("no contacts found")) {
                    int drawableId = getResources().getIdentifier(mMyProfile.getUserIcon(), "drawable", getPackageName());
                    mViewOwnProfile.setIcon(drawableId);

                    MobileNavigationDirections.ActionGlobalNavContactList directions
                            = ContactFragmentDirections.actionGlobalNavContactList(mMyProfile);

                    Navigation.findNavController(this, R.id.nav_host_fragment)
                            .navigate(directions);
                } else {
                    if (resultsJSON.has(getString(R.string.keys_json_message))) {
                        JSONArray data = resultsJSON.getJSONArray(
                                getString(R.string.keys_json_message));
                        mContacts = new Contact[data.length()];

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonContact = data.getJSONObject(i);

                            mContacts[i] = new Contact.Builder(
                                    jsonContact.getString(
                                            getString(R.string.keys_json_contact_username)),
                                    jsonContact.getString(
                                            getString(R.string.keys_json_contact_usericon)))
                                    .addFirstName(jsonContact.getString(
                                            getString(R.string.keys_json_contact_firstname)))
                                    .addLastName(jsonContact.getString(
                                            getString(R.string.keys_json_contact_lastname)))
                                    .addEmail(jsonContact.getString(
                                            getString(R.string.keys_json_contact_email)))
                                    .addIsEmailVerified(jsonContact.getBoolean(
                                            getString(R.string.keys_json_contacts_isEmailVerified)))
                                    .addRequestNumber(jsonContact.getInt(
                                            getString(R.string.keys_json_contacts_requestNumber)))
                                    .addIsContactVerified(jsonContact.getBoolean(
                                            getString(R.string.keys_json_contacts_isContactVerified)))
                                    .addChatId(jsonContact.getInt(
                                            getString(R.string.keys_json_contacts_chatId)))
                                    .build();
                        }

                        int drawableId = getResources().getIdentifier(mMyProfile.getUserIcon(), "drawable", getPackageName());
                        mViewOwnProfile.setIcon(drawableId);

                       /* MobileNavigationDirections.ActionGlobalNavContactList directions
                                = ContactFragmentDirections.actionGlobalNavContactList(contacts, mMyProfile).setJwt(mJwToken);
                            */

                        MobileNavigationDirections.ActionGlobalNavContactList directions
                                = ContactFragmentDirections.actionGlobalNavContactList(mMyProfile).setJwt(mJwToken);

                        directions.setContact(mContacts);

                        Navigation.findNavController(this, R.id.nav_host_fragment)
                                .navigate(directions);
                    } else {
                        Log.e("ERROR!", "No response");
                    }
                }
            } else {
                // failure
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }

    private void handleMyProfileOnPostExecute(final String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);

            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_success));

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_message))) {
                    JSONObject jsonContact = resultsJSON.getJSONObject(
                            getString(R.string.keys_json_message));

                    mMyProfile = new Contact.Builder(
                            jsonContact.getString(
                                    getString(R.string.keys_json_contact_username)),
                            jsonContact.getString(
                                    getString(R.string.keys_json_contact_usericon)))
                            .addFirstName(jsonContact.getString(
                                    getString(R.string.keys_json_contact_firstname)))
                            .addLastName(jsonContact.getString(
                                    getString(R.string.keys_json_contact_lastname)))
                            .addEmail(jsonContact.getString(
                                    getString(R.string.keys_json_contact_email)))
                            .addIsEmailVerified(jsonContact.getBoolean(
                                    getString(R.string.keys_json_contacts_isEmailVerified)))
                            .build();
                } else {
                    Log.e("ERROR!", "No response");
                }

            } else {
                // failure
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }

    private void logout() {

        new DeleteTokenAsyncTask().execute();
//        SharedPreferences prefs =
//                getSharedPreferences(
//                        getString(R.string.keys_shared_prefs),
//                        Context.MODE_PRIVATE);
//        //remove the saved credentials from StoredPrefs
//        prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
//        prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();

        //close the app
        /*finishAndRemoveTask();*/


        //or close this activity and bring back the Login
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);

        //End this Activity and remove it from the Activity back stack.
        finish();
    }

    public void populateCurrentProfile() {
        Uri uri_contacts = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_contacts))
                .appendPath(getString(R.string.ep_profile))
                .build();

        String email = mCredentials.getEmail();
        String json = "{\"email\":\"" + email + "\"}";

        try {
            JSONObject jsonEmail = new JSONObject(json);

            new SendPostAsyncTask.Builder(uri_contacts.toString(), jsonEmail)
                    .onPostExecute(this::handleMyProfileOnPostExecute)
                    .build().execute();

        } catch (Throwable tx) {
            Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
        }
    }

/*    public void showChatIcon(Contact theContact) {
        if (theContact.getUsername() == mMyProfile.getUsername()) {
            mChat.setVisible(false);
        } else if (theContact.getChatId() > 0) {
            // mChat.setVisible(true);
            mChat.setVisible(true);    // NP 11/28/2019 set false for rn until figure out how to pass it a chatId through the global action from homeActivity
        }
    }*/

    public void hideChatIcon() {
        if (mChat != null) {
            mChat.setVisible(false);
        }
    }

    public void hideViewProfile() {
        if (mViewOwnProfile != null) {
            mViewOwnProfile.setVisible(false);
        }
    }

    public void showViewProfile() {
        if (mViewOwnProfile != null) {
            mViewOwnProfile.setVisible(true);
        }
    }

    public void hideAddUser() {
        if (mAddContacts != null) {
            mAddContacts.setVisible(false);
        }
    }

    public void showAddUser() {
        if (mAddContacts != null) {
            mAddContacts.setVisible(true);
        }
    }

    public void showAddGroup() {
        if(mAddGroup != null){
            mAddGroup.setVisible(true);
        }
    }


    public void hideAddGroup() {
        if(mAddGroup != null){
            mAddGroup.setVisible(false);
        }
    }


    public void showDisplayMember() {
        if(mDisplayGroup != null) {
            mDisplayGroup.setVisible(true);
        }
    }

    public void hideDisplayMember() {
        if(mDisplayGroup != null ) {
            mDisplayGroup.setVisible(false);
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }



    // Deleting the Pushy device token must be done asynchronously. Good thing
    // we have something that allows us to do that.
    class DeleteTokenAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            //since we are already doing stuff in the background, go ahead
            //and remove the credentials from shared prefs here.
            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);

            prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
            prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();

            //unregister the device from the Pushy servers
            Pushy.unregister(HomeActivity.this);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //close the app
            finishAndRemoveTask();

//            //or close this activity and bring back the Login
//            Intent i = new Intent(this, MainActivity.class);
//
//            startActivity(i);
////            //Ends this Activity and removes it from the Activity back stack.
//            finish();
        }
    }

    /**
     * A BroadcastReceiver that listens for messages sent from PushReceiver
     */
    private class HomePushMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            NavController nc =
                    Navigation.findNavController(HomeActivity.this, R.id.nav_host_fragment);
            NavDestination nd = nc.getCurrentDestination();

            Log.i("PUSHY type", intent.getStringExtra("TYPE"));
            Log.i("PUSHY message", intent.getStringExtra("MESSAGE"));
            Log.i("PUSHY sender", intent.getStringExtra("SENDER"));

            if (nd.getId() != R.id.nav_chat) {
                if (intent.hasExtra("SENDER") && intent.hasExtra("MESSAGE") && intent.hasExtra("TYPE")) {
                    String type = intent.getStringExtra("TYPE");
                    String sender = intent.getStringExtra("SENDER");
                    String messageText = intent.getStringExtra("MESSAGE");

                    if (type.equals("msg")) {
                        Toast toast = Toast.makeText(getBaseContext(), sender + ": " + messageText,
                                Toast.LENGTH_LONG);
                        View view = toast.getView();
                        view.setBackgroundResource(R.drawable.customborder_greypurple);
                        toast.show();

                       /* //change the hamburger icon to red alerting the user of the notification
                        ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon()
                                .setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);*/

                        mChatMessage = new ChatMessageNotification.Builder(sender, messageText).build();

                    } else if (type.equals("connectionReq")) {
                        Toast toast = Toast.makeText(getBaseContext(), messageText + " from " + sender,
                                Toast.LENGTH_LONG);
                        View view = toast.getView();
                        view.setBackgroundResource(R.drawable.customborder_greypurple);
                        toast.show();
                    } else if (type.equals("connectionAccepted")) {
                        Toast toast = Toast.makeText(getBaseContext(), sender + " has accepted your connection.",
                                Toast.LENGTH_LONG);
                        View view = toast.getView();
                        view.setBackgroundResource(R.drawable.customborder_greypurple);
                        toast.show();
                    } else if (type.equals("connectionRejected")) {
                        Toast toast = Toast.makeText(getBaseContext(), sender + " has rejected your connection.",
                                Toast.LENGTH_LONG);
                        View view = toast.getView();
                        view.setBackgroundResource(R.drawable.customborder_greypurple);
                        toast.show();

                        mContactNotification = new ContactNotification.Builder(sender, messageText).build();
                    }
                }
            }
        }
    }

    public void reloadContactList() {
        onNavigationSelected(mNavContactList);
    }

    @Override
    public void onBackPressed() {
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);
        NavDestination nd = navController.getCurrentDestination();
        if (nd.getId() == R.id.nav_viewProfileFragment || nd.getId() == R.id.nav_addContactFragment) {
            reloadContactList();                                                    // NP 11/23/2019 -ONLY show View Profile and Add Contact icons in Contacts so that this Back press works as expected
        }                                                                           // , otherwise contact list doesn't load properly on back pressed

        super.onBackPressed();  // optional depending on your needs
    }

    public String getmEmail() {
        return mEmail;
    }

    public String getmUsername() { return mMyProfile.getUsername(); }

//    public Contact[] getContacts() { return mContacts;}


    public double getLatitude() {
        return mLocation.getLatitude();
    }

    public double getLonitude() {
        return mLocation.getLongitude();
    }
}
