package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Chat;

import java.io.Serializable;

public class ChatMessageNotification implements Serializable {

    private final String mMessage;
    private final String mSender;


    public static class Builder {
        private final String message;
        private final String sender;

        public Builder(String sender, String message) {
            this.message = message;
            this.sender = sender;
        }

        public ChatMessageNotification build() {
            return new ChatMessageNotification(this);
        }
    }

    private ChatMessageNotification(final Builder builder) {
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
