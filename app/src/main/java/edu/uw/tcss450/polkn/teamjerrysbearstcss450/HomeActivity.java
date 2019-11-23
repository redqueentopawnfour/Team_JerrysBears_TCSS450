package edu.uw.tcss450.polkn.teamjerrysbearstcss450;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatFragmentArgs;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatMessageNotification;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ViewProfileFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
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
    private Contact mMyProfile;
    private ChatMessageNotification mChatMessage;

    private ColorFilter mDefault;
    private HomePushMessageReceiver mPushMessageReciever;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Invitation", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_contactList, R.id.nav_chat,
                R.id.nav_weather)
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

        if (args.getChatMessage() != null) {
            MobileNavigationDirections.ActionGlobalNavChat directions =
                    ChatFragmentDirections.actionGlobalNavChat().setJwt(mJwToken).setEmail(mEmail);
            directions.setMessage(args.getChatMessage());
            navController.navigate(directions);
        }

        navigationView.setNavigationItemSelectedListener(this::onNavigationSelected);
        mDefault = toolbar.getNavigationIcon().getColorFilter();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPushMessageReciever == null) {
            mPushMessageReciever = new HomePushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        registerReceiver(mPushMessageReciever, iFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReciever != null) {
            unregisterReceiver(mPushMessageReciever);
        }
    }

    private boolean onNavigationSelected(final MenuItem menuItem) {
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);

        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                mAddContacts.setVisible(false);
                mViewOwnProfile.setVisible(false);

                navController.navigate(R.id.nav_home, getIntent().getExtras());
                break;
            case R.id.nav_contactList:
                mAddContacts.setVisible(true);
                mViewOwnProfile.setVisible(true);

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
                mAddContacts.setVisible(false);
                mViewOwnProfile.setVisible(false);
                navController.navigate(R.id.nav_weather);
                break;
            case R.id.nav_chat:
                ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon().setColorFilter(mDefault);

                MobileNavigationDirections.ActionGlobalNavChat directions;
                if (mChatMessage != null) {

                    Log.d("Message", mChatMessage.getMessage());

                    directions = ChatFragmentDirections.actionGlobalNavChat()
                            .setEmail(mEmail)
                            .setJwt(mJwToken)
                            .setMessage(mChatMessage);
                } else {
                    directions = ChatFragmentDirections.actionGlobalNavChat()
                            .setEmail(mEmail)
                            .setJwt(mJwToken);
                }

                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .navigate(directions);

                mAddContacts.setVisible(false);
                mViewOwnProfile.setVisible(false);
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_addContact) {
            mAddContacts.setVisible(false);
            mViewOwnProfile.setVisible(false);
            mChat.setVisible(false);

            NavController navController =
                    Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.navigate(R.id.nav_addContactFragment, getIntent().getExtras());
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == 16908332) {
            NavController navController =
                    Navigation.findNavController(this, R.id.nav_host_fragment);
            NavDestination nd = navController.getCurrentDestination();
            if (nd.getId() == R.id.nav_addContactFragment) {
                loadContacts();
            } else if (nd.getId() == R.id.nav_viewProfileFragment) {
                loadContacts();
            }
            return super.onOptionsItemSelected(item);
        } else if (id == R.id.action_viewOwnProfile) {
            mAddContacts.setVisible(false);
            mViewOwnProfile.setVisible(false);
            mChat.setVisible(false);

            MobileNavigationDirections.ActionGlobalViewProfileFragment directions
                    = ViewProfileFragmentDirections.actionGlobalViewProfileFragment(mMyProfile);

            Navigation.findNavController(this, R.id.nav_host_fragment)
                    .navigate(directions);
            return true;
        } else if (id == R.id.action_chat) {
            mAddContacts.setVisible(false);
            mViewOwnProfile.setVisible(false);
            mChat.setVisible(false);

            MobileNavigationDirections.ActionGlobalNavChat directions
                    = ChatFragmentDirections.actionGlobalNavChat();

            Navigation.findNavController(this, R.id.nav_host_fragment)
                    .navigate(directions);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadContacts() {
        mAddContacts.setVisible(true);
        mViewOwnProfile.setVisible(true);
        mChat.setVisible(false);

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
                    .onPostExecute(this::handleContactsOnPostExecute)
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

    private void handleContactsOnPostExecute(final String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);

            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_success));

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_message))) {
                    JSONArray data = resultsJSON.getJSONArray(
                            getString(R.string.keys_json_message));
                    Contact[] contacts = new Contact[data.length()];

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jsonContact = data.getJSONObject(i);

                        contacts[i] = new Contact.Builder(
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

                    MobileNavigationDirections.ActionGlobalNavContactList directions
                            = ContactFragmentDirections.actionGlobalNavContactList(contacts, mMyProfile).setJwt(mJwToken);

                    Navigation.findNavController(this, R.id.nav_host_fragment)
                            .navigate(directions);
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

                 /*   String contactEmail = jsonContact.getString(
                            getString(R.string.keys_json_contact_email));
                    String userEmail = mMyProfile.getEmail();
                    Boolean isContactVerified = true;
*/
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

    public void showChatIcon(Contact theContact) {
        mAddContacts.setVisible(false);
        mViewOwnProfile.setVisible(false);

        if (theContact.getUsername() == mMyProfile.getUsername()) {
            mChat.setVisible(false);
        } else {
           // mChat.setVisible(true);
            mChat.setVisible(false);    // NP 11/28/2019 set false for rn until figure out how to pass it a chatId through the global action from homeActivity
        }
    }

    public void hideViewProfile() {
        mViewOwnProfile.setVisible(false);
    }

    public void hideAddUser() {
        mAddContacts.setVisible(false);
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
            if (nd.getId() != R.id.nav_chat) {

                if (intent.hasExtra("SENDER") && intent.hasExtra("MESSAGE")) {

                    String sender = intent.getStringExtra("SENDER");
                    String messageText = intent.getStringExtra("MESSAGE");

                    //change the hamburger icon to red alerting the user of the notification
                    ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon()
                            .setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);


                    Log.d("HOME", sender + ": " + messageText);
                    mChatMessage = new ChatMessageNotification.Builder(sender, messageText).build();
                }
            }
        }
    }

  /*  public void setChatId(int chatId) {
        mChatId = chatId;
    }*/
    public String getmEmail() {
        return mEmail;
    }
    public Credentials getmCredentials() {
        return mCredentials;
    }
}
