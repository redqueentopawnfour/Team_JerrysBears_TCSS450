package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.GroupChat.GroupContact;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact.Contact;


public class GroupContact implements Serializable, Parcelable{
    private final String mGroupname;

    private final List<Contact> mContacts;

    private final Integer mChatId;


    protected GroupContact(Parcel in) {
        mGroupname = in.readString();
        mContacts = new ArrayList<Contact>();
        in.readList(mContacts, Contact.class.getClassLoader());

        mChatId = in.readInt();

    }
    public GroupContact(String groupname, Integer chatId) {
        mGroupname = groupname;
        mContacts = new ArrayList<Contact>();
        mChatId  =   chatId;

    }


    public GroupContact(String groupname, List<Contact> contacts, Integer chatId) {
        mGroupname = groupname;
        mContacts = contacts;
        mChatId  =   chatId;

    }


    public static final Creator<GroupContact> CREATOR = new Creator<GroupContact>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public GroupContact createFromParcel(Parcel in) {
            return new GroupContact(in);
        }

        @Override
        public GroupContact[] newArray(int size) {
            return new GroupContact[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mGroupname);
        dest.writeList(mContacts);
        dest.writeInt(mChatId);

//        if (mChatId == null) {
//            dest.writeByte((byte) 0);
//        } else {
//            dest.writeByte((byte) 1);
//            dest.writeInt(mChatId);
//        }
    }


    /**
     * Helper class for building a Message.
     *
     *
     */
    public static class Builder {
        private String mGroupname;
        private List<Contact> mContacts = new ArrayList<>();
        private Integer mChatId = 0;


        /**
         * Constructs a new Builder.
         *
         * @param groupname the groupname of the group contact
         */
        public Builder(String groupname, Integer chatId) {
            this.mGroupname = groupname;
            this.mChatId = chatId;
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public GroupContact build() {
            return new GroupContact(this);
        }



        /**
         * Add an optional last name for the full contact.
         * @param val an optional last name for the full contact
         * @return the Builder of this Contact
         */
        public Builder setGroupname(final String val) {
            mGroupname = val;
            return this;
        }

        public Builder addMemeber (final Contact val) {
            mContacts.add(val);
            return this;
        }


        /**
         * Add an optional chatid for the full contact.
         * @param val an optional chatid for the full contact
         * @return the Builder of this Contact
         */
        public Builder addChatId(final Integer val) {
            mChatId = val;
            return this;
        }
    }

    private GroupContact(final Builder builder) {
        this.mGroupname = builder.mGroupname;
        this.mContacts  = builder.mContacts;
        this.mChatId = builder.mChatId;


    }

    public String getGroupname() { return mGroupname; }

    public List<Contact> getContact() { return mContacts;}

    public Integer getChatId() { return mChatId; }


    @NonNull
    @Override
    public String toString() {
        return mGroupname;
    }
}
