package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.home;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupContact.GroupContact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.GetAsyncTask;
import edu.uw.tcss450.polkn.teamjerrysbearstcss450.utils.SendPostAsyncTask;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<Map<Integer, GroupContact>> mFavoriteChats;
    private String mEmail;
    private String mJwt;
    private Map<Integer, GroupContact> mGroupContact;
    public HomeViewModel(String email, String jwt) {
        mGroupContact = new HashMap<>();
        mText = new MutableLiveData<>();
        mEmail = email;
        mJwt = jwt;
        mText.setValue("Welcome to Beary Good Connection!");
        mFavoriteChats = new MutableLiveData<>();
        loadFavorites();
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<Map<Integer, GroupContact>> getFavoriteChats() { return mFavoriteChats; }

    public void loadFavorites(){
        String url = new Uri.Builder()
                .scheme("https")
                .appendPath("beary-good-connection.herokuapp.com")
                .appendPath("messaging")
                .appendPath("getfavorites")
                .build()
                .toString();

        new GetAsyncTask.Builder(url).addHeaderField("email", mEmail).
                addHeaderField("authorization", mJwt).onPostExecute(this::handleLoadFavoritesOnPost)
                .build().execute();

    }

    private void handleLoadFavoritesOnPost(final String result) {
        JSONObject res = null;
        try {
            res = new JSONObject(result);
            if(res.has("success") && res.getBoolean("success")) {
                JSONArray details = res.getJSONArray("details");
                for (int i = 0; i < details.length(); i++) {
                    JSONObject temp = details.getJSONObject(i);
                    String url = new Uri.Builder()
                            .scheme("https")
                            .appendPath("beary-good-connection.herokuapp.com")
                            .appendPath("messaging")
                            .appendPath("getchatmembers")
                            .build()
                            .toString();
                    int chatId = temp.getInt("chatid");
                    mGroupContact.put(chatId, new GroupContact(temp.getString("name"), chatId));
                    Log.d("am i getting to load favorites on post", mGroupContact.toString());
                    new GetAsyncTask.Builder(url).addHeaderField("authorization", mJwt).addHeaderField("chatid", Integer.toString(chatId))
                            .onPostExecute(this::handleLoadMembersOnPost).build().execute();
                }
            }
            else {
                Log.d("webservice error on load favorites", res.get("error").toString());

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void handleLoadMembersOnPost(final String result) {
        Log.d("am i getting to load members on post", result);
        int chatid = 0;
        List<Contact> users = new ArrayList<Contact>();
        JSONObject res = null;
        try {
            res = new JSONObject(result);
            if(res.has("success") && res.getBoolean("success")) {
                JSONArray details = res.getJSONArray("usernames");
                chatid = res.getInt("chatid");
                for (int i = 0; i < details.length(); i++) {
                    String name = details.getJSONObject(i).getString("username");
                    users.add(new Contact.Builder(name, "").build());
                    Log.d("am i getting to load members on post", mGroupContact.toString());
                }

            } else {
                Log.d("webservice error on load members", res.get("error").toString());

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mGroupContact.containsKey(chatid)) {
            GroupContact.Builder gcBuilder = new GroupContact.Builder(mGroupContact.get(chatid).getGroupname(), chatid);
            for (Contact user: users) {
                gcBuilder.addMemeber(user);
            }
            GroupContact gc = gcBuilder.build();
            Log.d("should be a group", gc.getContact().get(0).getUsername());
            mGroupContact.put(chatid, gc);
        }
        mFavoriteChats.postValue(mGroupContact);
    }

    public void removeFavorite(int chatid) {
        mGroupContact = mFavoriteChats.getValue();
        if (mGroupContact != null)
            mGroupContact.remove(chatid);
        mFavoriteChats.postValue(mGroupContact);
    }
}