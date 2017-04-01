package com.firebase.uidemo.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.firebase.uidemo.R;
import com.firebase.uidemo.util.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CathyChi on 3/30/17.
 */

public class ChatListActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private List<String> mNames = new ArrayList<>();
    private List<Integer> mUIDs;
    private SQLiteOpenHelper mDBHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        readDatabase();

    }

    public void initializeView () {
        ChatListAdapter chatListAdapter = new ChatListAdapter(this, mNames, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.chats);
        recyclerView.setAdapter(chatListAdapter);
    }

    private void readDatabase() {
        new AsyncTask<Object, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Object... params) {
                mDBHelper = new ChatNamesDBHelper(getApplicationContext());
                SQLiteDatabase database = ChatListActivity.this.mDBHelper.getReadableDatabase();
                String[] projection = {
                        ChatContract.ChatNames._ID,
                        ChatContract.ChatNames.COLUMN_NAME_NAMES,
                        ChatContract.ChatNames.COLUMN_NAME_UID,
                        ChatContract.ChatNames.COLUMN_NAME_LASTCHAT
                };

                String sortOrder = ChatContract.ChatNames.COLUMN_NAME_LASTCHAT + "DESC";

                Cursor cursor = database.query(
                        ChatContract.ChatNames.TABLE_NAME,
                        projection, null, null, null, null, sortOrder);

                while(cursor.moveToNext())

                {
                    mNames.add(cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatNames.COLUMN_NAME_NAMES)));
                    mUIDs.add(cursor.getInt(cursor.getColumnIndexOrThrow(ChatContract.ChatNames.COLUMN_NAME_UID)));
                }
                cursor.close();
                return mNames;
            }

            @Override
            protected void onPostExecute(List<String> strings) {
                initializeView();
            }
        }.execute(new Object());
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }


    @Override
    public void recyclerViewItemClicked(int position) {

    }
}
