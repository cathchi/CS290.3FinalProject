package com.firebase.uidemo.chat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by CathyChi on 3/31/17.
 */

public class ChatHistoryDBHelper extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "chat.db";

    private static final String SQL_CREATE_CHATHISTORY_TABLE =
            "CREATE TABLE " +
                    ChatContract.ChatHistory.TABLE_NAME + " (" +
                    ChatContract.ChatHistory.COLUMN_NAME_MESSAGEID + " STRING PRIMARY KEY UNIQUE," +
                    ChatContract.ChatHistory.COLUMN_NAME_NAMES + " STRING," +
                    ChatContract.ChatHistory.COLUMN_NAME_RNAMES + " STRING," +
                    ChatContract.ChatHistory.COLUMN_NAME_UID + " STRING," +
                    ChatContract.ChatHistory.COLUMN_NAME_RECIPIENTUID + " STRING," +
                    ChatContract.ChatHistory.COLUMN_NAME_MESSAGES + " TEXT," +
                    ChatContract.ChatHistory.COLUMN_NAME_TIMESTAMP + " LONG)";

    private static final String SQL_DELETE_CHATHISTORY_TABLE =
            "DROP TABLE IF EXISTS " + ChatContract.ChatHistory.TABLE_NAME;

    public ChatHistoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CHATHISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DATABASE_VERSION = newVersion;
        db.execSQL(SQL_DELETE_CHATHISTORY_TABLE);
    }
}
