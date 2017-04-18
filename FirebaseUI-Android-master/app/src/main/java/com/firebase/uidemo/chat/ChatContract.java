package com.firebase.uidemo.chat;

import android.provider.BaseColumns;

/**
 * Created by CathyChi on 3/31/17.
 */

public final class ChatContract {

    private ChatContract() {}

    public static class ChatNames implements BaseColumns {
        public static final String TABLE_NAME = "chat_names";
        public static final String COLUMN_NAME_NAMES = "names";
        public static final String COLUMN_NAME_UID = "uid";
        public static final String COLUMN_NAME_LASTCHAT = "last_chat_time";
    }

    public static class ChatHistory implements BaseColumns {
        public static final String TABLE_NAME = "chat_history";
        public static final String COLUMN_NAME_UID = "uid";
        public static final String COLUMN_NAME_NAMES = "names";
        public static final String COLUMN_NAME_RNAMES = "recipient_name";
        public static final String COLUMN_NAME_MESSAGES = "messages";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_MESSAGEID = "messageid";
        public static final String COLUMN_NAME_RECIPIENTUID = "recipient_uid";
    }

}
