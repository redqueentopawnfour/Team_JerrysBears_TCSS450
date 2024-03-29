package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection.contact;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

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
    private final Boolean mIsEmailVerified;
    private final Boolean mIsContactVerified;
    private final Integer mRequestNumber;   // If there is a request number, then the contact is the requester not the receiver
    private final Integer mChatId;          // Default to 0

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected Contact(Parcel in) {
        mUsername = in.readString();
        mUserIcon = in.readString();
        mFirstName = in.readString();
        mLastName = in.readString();
        mEmail = in.readString();
        mIsEmailVerified = in.readBoolean();
        mIsContactVerified = in.readBoolean();
        mRequestNumber = in.readInt();
        mChatId = in.readInt();
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUsername);
        dest.writeString(mUserIcon);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mEmail);
        dest.writeByte((byte) (mIsEmailVerified ? 1 : 0));
        dest.writeByte((byte) (mIsContactVerified ? 1 : 0));
        dest.writeInt(mRequestNumber);
        dest.writeInt(mChatId);
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
        private Boolean mIsEmailVerified = false;
        private Boolean mIsContactVerified = false;
        private Integer mRequestNumber = 0;
        private Integer mChatId = 0;

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

        /**
         * Add an optional isEmailVerified for the full contact.
         * @param val an optional isEmailVerified for the full contact
         * @return the Builder of this Contact
         */
        public Builder addIsEmailVerified(final Boolean val) {
            mIsEmailVerified = val;
            return this;
        }

        /**
         * Add an optional isContactVerified for the full contact.
         * @param val an optional isContactVerified for the full contact
         * @return the Builder of this Contact
         */
        public Builder addIsContactVerified(final Boolean val) {
            mIsContactVerified = val;
            return this;
        }

        /**
         * Add an optional request number for the full contact.
         * @param val an optional request number for the full contact
         * @return the Builder of this Contact
         */
        public Builder addRequestNumber(final Integer val) {
            mRequestNumber = val;
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

    private Contact(final Builder builder) {
        this.mUsername = builder.mUsername;
        this.mUserIcon = builder.mUserIcon;
        this.mFirstName = builder.mFirstName;
        this.mLastName = builder.mLastName;
        this.mEmail = builder.mEmail;
        this.mIsEmailVerified = builder.mIsEmailVerified;
        this.mIsContactVerified = builder.mIsContactVerified;
        this.mRequestNumber = builder.mRequestNumber;
        this.mChatId = builder.mChatId;
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

    public Boolean getIsEmailVerified() {
        return mIsEmailVerified;
    }

    public Boolean getIsContactVerified() {
        return mIsContactVerified;
    }

    public Integer getRequestNumber() {
        return mRequestNumber;
    }

    public Integer getChatId() {
        return mChatId;
    }

   /* public Integer getmRequestNumber() {
        return mRequestNumber;
    }*/
}
