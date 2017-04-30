package com.firebase.uidemo.chat;

import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.firebase.uidemo.R;
import com.firebase.uidemo.util.RecyclerViewClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private List<String> mRNames = new ArrayList<>();
    private List<String> mUIDs = new ArrayList<>();
    private List<String> mRecipientUID = new ArrayList<>();
    private List<String> mMessages = new ArrayList<>();
    private List<String> mMessageIDs = new ArrayList<>();
    private List<Chat> mChats = new ArrayList<>();
    private List<String> mTypes = new ArrayList<>();
    private List<Long> mTimeStamps = new ArrayList<>();
    private ChatListAdapter chatListAdapter;
    private SQLiteOpenHelper mDBHelper;
    private DatabaseReference mChatRef;
    private DatabaseReference mRef;
    private FirebaseUser mUser;


    private static final String TAG = "ERROR";

    private static final String UID = "UID";
    private static final String NAME = "NAME";
    private static final String DATABASE_REF_USERS = "users";
    private static final String DATABSE_REF_CHATS = "chats";

    /**
     * Initializes view
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatlist);


        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.new_message_toolbar);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.new_message_button);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ChatNewMessageActivity.class);
                startActivity(i);
            }
        });

        chatListAdapter = new ChatListAdapter(this, mDisplayNames, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.chats);
        recyclerView.setAdapter(chatListAdapter);
        getDatabaseData();
    }

    /**
     * Stores messages from Firebase Database into SQLite database in an AsyncTask
     */
    private void writeDatabase() {
        new AsyncTask<List<String>, Void, Long>() {
            @Override
            protected Long doInBackground(List<String>... params) {
                if (mDBHelper == null) {
                    mDBHelper = new ChatHistoryDBHelper(getApplicationContext());
                }
                SQLiteDatabase database = mDBHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                long id = 0;
                for (int i = 0; i < mNames.size(); i++) {
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_MESSAGEID, mMessageIDs.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_NAMES, mNames.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_RNAMES, mRNames. get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_UID, mUIDs.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_MESSAGES, mMessages.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_TIMESTAMP, mTimeStamps.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_RECIPIENTUID, mRecipientUID.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_MESSAGETYPE, mTypes.get(i));
                    try {
                        id += database.insertOrThrow(ChatContract.ChatHistory.TABLE_NAME, null, contentValues);
                    } catch (SQLException e){
                        e.printStackTrace();
                    }
                }
                mDBHelper.close();
                return id;
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    /**
     * Fetches all users who the user has had chats with from Firebase Database to store into SQLite database
     * Showing those user names without duplicates
     */
    private void getDatabaseData () {
        mRef = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        final String displayName = mUser.getDisplayName();
        mChatRef = mRef.child(DATABASE_REF_USERS).child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).child(DATABSE_REF_CHATS);
        mChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mMessageIDs.add(ds.getKey());
                    if ((mDisplayNames.indexOf(ds.getValue(Chat.class).getRName()) < 0) &&
                    (mDisplayNames.indexOf(ds.getValue(Chat.class).getName()) < 0)) {
                        if (!displayName.equals(ds.getValue(Chat.class).getRName())) {
                            mDisplayNames.add(ds.getValue(Chat.class).getRName());
                        } else {
                            mDisplayNames.add(ds.getValue(Chat.class).getName());
                        }
                        mChats.add(ds.getValue(Chat.class));
                    }
                    chatListAdapter.notifyItemInserted(mDisplayNames.size()-1);
                    mNames.add(ds.getValue(Chat.class).getName());
                    mUIDs.add(ds.getValue(Chat.class).getUid());
                    mMessages.add(ds.getValue(Chat.class).getMessage());
                    mRecipientUID.add(ds.getValue(Chat.class).getRUID());
                    mRNames.add(ds.getValue(Chat.class).getRName());
                    mTypes.add(ds.getValue(Chat.class).getType());
                    mTimeStamps.add(ds.getValue(Chat.class).getTimeStamp());
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

    /**
     * Changes to ChatActivity where the user can chat with selected user
     * @param position is the index of the name the user clicks on the chat with
     */
    @Override
    public void recyclerViewItemClicked(int position) {
        String id;
        String name;
        if (mUser.getUid().equals(mChats.get(position).getUid())) {
            id = mChats.get(position).getRUID();
            name = mChats.get(position).getRName();
        } else {
            id = mChats.get(position).getUid();
            name = mChats.get(position).getName();
        }
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(UID, id);
        intent.putExtra(NAME, name);
        startActivity(intent);

    }
}
