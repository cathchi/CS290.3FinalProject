package com.firebase.uidemo.database;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.firebase.uidemo.R;
import com.firebase.uidemo.util.RecyclerViewClickListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CathyChi on 3/30/17.
 */

public class ChatListActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private List<String> mDisplayNames = new ArrayList<>();
    private List<String> mNames = new ArrayList<>();
    private List<String> mUIDs = new ArrayList<>();
    private List<String> mMessages = new ArrayList<>();
    private List<String> mMessageIDs = new ArrayList<>();
    private ChatListAdapter chatListAdapter;
    private SQLiteOpenHelper mDBHelper;
    private DatabaseReference mChatRef;
    private DatabaseReference mRef;

    private static final String TAG = "ERROR";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatlist);
        chatListAdapter = new ChatListAdapter(this, mDisplayNames, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.chats);
        recyclerView.setAdapter(chatListAdapter);
        getDatabaseData();
    }


    private void writeDatabase() {
        new AsyncTask<List<String>, Void, Integer>() {
            @Override
            protected Integer doInBackground(List<String>... params) {
                if (mDBHelper == null) {
                    mDBHelper = new ChatHistoryDBHelper(getApplicationContext());
                }
                SQLiteDatabase database = mDBHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                for (int i = 0; i < mNames.size(); i++) {
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_NAMES, mNames.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_UID, mUIDs.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_MESSAGES, mMessages.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_MESSAGEID, mMessageIDs.get(i));
                }
                int count = database.update(ChatContract.ChatHistory.TABLE_NAME, contentValues, null, null);
                return count;
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void getDatabaseData () {
        mRef = FirebaseDatabase.getInstance().getReference();
        mChatRef = mRef.child("users").child(getIntent().getStringExtra("uid")).child("chats");
        mChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d("HI", ds.getKey());
                    mMessageIDs.add(ds.getKey());
                    if (!mDisplayNames.contains(ds.getValue(Chat.class).getName())) {
                        mDisplayNames.add(ds.getValue(Chat.class).getName());
                    }
                    mNames.add(ds.getValue(Chat.class).getName());
                    chatListAdapter.notifyItemInserted(0);
                    mUIDs.add(ds.getValue(Chat.class).getUid());
                    mMessages.add(ds.getValue(Chat.class).getMessage());
                }
                if (!mNames.isEmpty()) {
                    writeDatabase();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }


    @Override
    public void recyclerViewItemClicked(int position) {
        String name = mDisplayNames.get(position);
        String id = mUIDs.get(mNames.indexOf(name));
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("UID", id);
        startActivity(intent);

    }
}
