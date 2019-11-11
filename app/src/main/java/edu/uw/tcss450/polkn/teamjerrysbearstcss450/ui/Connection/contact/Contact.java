package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Class to encapsulate a Contact. Building an Object requires an email and username.
 *
 * @author
 * @version
 */
public class Contact implements Serializable, Parcelable {

    private final String mUsername;
    private final String mUserIcon;
    private final String mFirstName;
    private final String mLastName;
    private final String mEmail;

    protected Contact(Parcel in) {
        mUsername = in.readString();
        mUserIcon = in.readString();
        mFirstName = in.readString();
        mLastName = in.readString();
        mEmail = in.readString();
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUsername);
        dest.writeString(mUserIcon);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mEmail);
    }

    /**
     * Helper class for building a Contact.
     *
     * @author Nadia Polk
     */
    public static class Builder {
        private final String mUsername;
        private final String mUserIcon;
        private String mFirstName = "";
        private String mLastName = "";
        private String mEmail = "";

        /**
         * Constructs a new Builder.
         *
         * @param username the username of the contact
         * @param email the email of the contact
         */
        public Builder(String username, String email) {
            this.mUsername = username;
            this.mUserIcon = email;
        }

        public Contact build() {
            return new Contact(this);
        }

        /**
         * Add an optional first name for the full contact.
         * @param val an optional first name for the full contact
         * @return the Builder of this Contact
         */
        public Builder addFirstName(final String val) {
            mFirstName = val;
            return this;
        }

        /**
         * Add an optional last name for the full contact.
         * @param val an optional last name for the full contact
         * @return the Builder of this Contact
         */
        public Builder addLastName(final String val) {
            mLastName = val;
            return this;
        }

        /**
         * Add an optional email for the full contact.
         * @param val an optional email for the full contact
         * @return the Builder of this Contact
         */
        public Builder addEmail(final String val) {
            mEmail = val;
            return this;
        }
    }

    private Contact(final Builder builder) {
        this.mUsername = builder.mUsername;
        this.mUserIcon = builder.mUserIcon;
        this.mFirstName = builder.mFirstName;
        this.mLastName = builder.mLastName;
        this.mEmail = builder.mEmail;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public String getUserIcon() {
        return mUserIcon;
    }
}
