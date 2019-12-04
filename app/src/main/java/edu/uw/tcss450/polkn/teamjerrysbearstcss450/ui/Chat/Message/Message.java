package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat.Message;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.Serializable;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Message implements Serializable, Parcelable {

    private final String mUsername;

    private final String mMessage;

//    private final String mSender;
//
//    private final Integer mChatId;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected Message(Parcel in) {
        mUsername = in.readString();
        mMessage = in.readString();
//        mChatId = in.readInt();

    }


    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public Message(String message, String username) {
        mUsername = username;
        mMessage = message;

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUsername);
        dest.writeString(mMessage);
//        dest.writeInt(mChatId);

//        dest.writeString(mSender);

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
        private final String mUsername;
        private String mMessage = "";
        private Integer mChatId = 0;


        /**
         * Constructs a new Builder.
         *
         * @param username the username of the contact
         */
        public Builder(String username, Integer chatId) {
            this.mUsername = username;
            this.mChatId = chatId;
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public Message build() {
            return new Message(this);
        }



        /**
         * Add an optional last name for the full contact.
         * @param val an optional last name for the full contact
         * @return the Builder of this Contact
         */
        public Builder addMessage(final String val) {
            mMessage = val;
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

    private Message(final Builder builder) {
        this.mUsername = builder.mUsername;
        this.mMessage  = builder.mMessage;
//        this.mChatId = builder.mChatId;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getMessage() {return mMessage;}

//    public Integer getChatId() {
//        return mChatId;
//    }


    @NonNull
    @Override
    public String toString() {
        return mUsername + ": " + mMessage;
    }
}
