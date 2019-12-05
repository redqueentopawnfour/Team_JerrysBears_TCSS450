package edu.uw.tcss450.polkn.teamjerrysbearstcss450;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;


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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.function.Consumer;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatMessageNotification;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.ChatViewFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupChatFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactFragment;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactFragmentDirections;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.ContactNotification;
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
    private MenuItem mNavContactList;
    private MenuItem mAddGroup;
    private Contact mMyProfile;
    private ChatMessageNotification mChatMessage;
    private ContactNotification mContactNotification;
    private Contact[] mContacts;



    private ColorFilter mDefault;
    private HomePushMessageReceiver mPushMessageReciever;


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
                mChat.setVisible(false);                        // NP 11/23/2019- Set icons to invisible from here when navigating to Home - all other icon handling done in individual fragments
                mViewOwnProfile.setVisible(false);              // but since HomeFragment loads before the menu inflater, HomeFragment must be handled from HomeActivity
                mAddContacts.setVisible(false);
                mAddGroup.setVisible(false);
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
                navController.navigate(R.id.nav_groupChat); // can remove if adding Global Action
//                ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon().setColorFilter(mDefault);
//
//                MobileNavigationDirections.ActionGlobalNavChat directions;
//                if (mChatMessage != null) {
//
//                    Log.d("Message", mChatMessage.getMessage());
//
//                    directions = ChatViewFragmentDirections.actionGlobalNavChat()
//                            .setEmail(mEmail)
//                            .setJwt(mJwToken);
////                            .setMessage(mChatMessage);
//                } else {
//                    directions = ChatViewFragmentDirections.actionGlobalNavChat()
//                            .setEmail(mEmail)
//                            .setJwt(mJwToken);
//                }
//
//                Navigation.findNavController(this, R.id.nav_host_fragment)
//                        .navigate(directions);
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
                    NavController navController =
                            Navigation.findNavController(this, R.id.nav_host_fragment);
                    navController.navigate(R.id.nav_groupContacts, getIntent().getExtras());
                } else {
                    if (resultsJSON.has(getString(R.string.keys_json_message))) {
                        JSONArray data = resultsJSON.getJSONArray(
                                getString(R.string.keys_json_message));
                        int verifiedCount = 0;
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonContact = data.getJSONObject(i);
                            if (jsonContact.getBoolean(getString(R.string.keys_json_contacts_isEmailVerified))) {
                                verifiedCount++;
                            }
                        }
                        mContacts = new Contact[verifiedCount];
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonContact = data.getJSONObject(i);
                            if(jsonContact.getBoolean(getString(R.string.keys_json_contacts_isEmailVerified))) {
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
                        }

                        GroupChatFragmentDirections.ActionNavGroupChatToGroupContactFragment directions
                                = GroupChatFragmentDirections.actionNavGroupChatToGroupContactFragment(mMyProfile);
                        directions.setJwt(mJwToken);
                        directions.setContacts(mContacts);
//                        directions.
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
                        ;

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
}
