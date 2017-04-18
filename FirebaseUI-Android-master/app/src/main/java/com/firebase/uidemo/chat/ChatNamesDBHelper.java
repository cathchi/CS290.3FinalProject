package com.firebase.uidemo.chat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by CathyChi on 3/31/17.
 */

public class ChatNamesDBHelper extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "chat.db";

    private static final String SQL_CREATE_CHATNAMES_TABLE =
            "CREATE TABLE " +
                    ChatContract.ChatNames.TABLE_NAME + " (" +
                    ChatContract.ChatNames._ID + "INTEGER PRIMARY KEY" +
                    ChatContract.ChatNames.COLUMN_NAME_NAMES + " TEXT," +
                    ChatContract.ChatNames.COLUMN_NAME_UID + " STRING" +
                    ChatContract.ChatNames.COLUMN_NAME_LASTCHAT + "LONG)";

    private static final String SQL_DELETE_CHATNAMES_TABLE =
            "DROP TABLE IF EXISTS " + ChatContract.ChatNames.TABLE_NAME;

    public ChatNamesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CHATNAMES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DATABASE_VERSION = newVersion;
        db.execSQL(SQL_DELETE_CHATNAMES_TABLE);

    }
}
