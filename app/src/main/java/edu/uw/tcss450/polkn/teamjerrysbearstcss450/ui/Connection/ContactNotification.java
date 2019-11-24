package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;

import java.io.Serializable;

public class ContactNotification implements Serializable {

    private final String mMessage;
    private final String mSender;


    public static class Builder {
        private final String message;
        private final String sender;

        public Builder(String sender, String message) {
            this.message = message;
            this.sender = sender;
        }

        public ContactNotification build() {
            return new ContactNotification(this);
        }
    }

    private ContactNotification(final Builder builder) {
        mMessage = builder.message;
        mSender = builder.sender;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getSender() {
        return mSender;
    }
}
