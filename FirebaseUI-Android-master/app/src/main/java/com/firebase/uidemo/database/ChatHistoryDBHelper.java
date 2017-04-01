package com.firebase.uidemo.database;

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
                    ChatContract.ChatHistory._ID + "INTEGER PRIMARY KEY" +
                    ChatContract.ChatHistory.COLUMN_NAME_UID + " INT," +
                    ChatContract.ChatHistory.COLUMN_NAME_MESSAGES + "TEXT," +
                    ChatContract.ChatHistory.COLUMN_NAME_TIMESTAMP + " INT)";

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
