package com.firebase.uidemo.database;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private List<ArrayList<String>> mRecipientUIDs = new ArrayList<>();
    private List<Long> mTimeStamps = new ArrayList<>();
    private ChatListAdapter chatListAdapter;
    private SQLiteOpenHelper mDBHelper;
    private DatabaseReference mChatRef;
    private DatabaseReference mRef;

    private static final String TAG = "ERROR";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatlist);


        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.new_message_toolbar);
        //View view = getSupportActionBar().getCustomView();

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
//                    Log.d("PRINTING EVERYTHING", mUIDs.get(i));
//                    Log.d("PRINTING EVERYTHING", mNames.get(i));
//                    Log.d("PRINTING EVERYTHING", mMessages.get(i));
//                    Log.d("PRINTING EVERYTHING", mMessageIDs.get(i));
//                    JSONObject RUIDs = new JSONObject();
//                    try {
//                        RUIDs.put("RECIPIENT UIDs", new JSONArray(mRecipientUIDs.get(i)));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    String newRUIDs = RUIDs.toString();
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_MESSAGEID, mMessageIDs.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_NAMES, mNames.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_RNAMES, mRNames. get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_UID, mUIDs.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_MESSAGES, mMessages.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_TIMESTAMP, mTimeStamps.get(i));
                    contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_RECIPIENTUID, mRecipientUID.get(i));
                    //contentValues.put(ChatContract.ChatHistory.COLUMN_NAME_RECIPIENTUID, newRUIDs);
                    try {
                        id += database.insertOrThrow(ChatContract.ChatHistory.TABLE_NAME, null, contentValues);
                    } catch (SQLException e){
                        e.printStackTrace();
                    }
                }
                mDBHelper.close();
//                int count = database.update(ChatContract.ChatHistory.TABLE_NAME, contentValues, null, null);
                Log.d("COUNTING ID", id+"");
                return id;
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void getDatabaseData () {
        mRef = FirebaseDatabase.getInstance().getReference();
        mChatRef = mRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chats");
        mChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mMessageIDs.add(ds.getKey());
                    if (!mDisplayNames.contains(ds.getValue(Chat.class).getRName())
                            && !ds.getValue(Chat.class).getName()
                            .equals(ds.getValue(Chat.class).getRName())) {
                        mDisplayNames.add(ds.getValue(Chat.class).getRName());
                    }
                    chatListAdapter.notifyItemInserted(mDisplayNames.size()-1);
                    mNames.add(ds.getValue(Chat.class).getName());
                    mUIDs.add(ds.getValue(Chat.class).getUid());
                    mMessages.add(ds.getValue(Chat.class).getMessage());
                    mRecipientUID.add(ds.getValue(Chat.class).getRUID());
                    mRNames.add(ds.getValue(Chat.class).getRName());
                    //mRecipientUIDs.add(ds.getValue(Chat.class).getRUIDs());
                    mTimeStamps.add(ds.getValue(Chat.class).getTimeStamp());

//                    Log.d("MESSAGE ID", ds.getKey());
//                    Log.d("NAMES", ds.getValue(Chat.class).getName());
//                    Log.d("UID", ds.getValue(Chat.class).getUid());
//                    Log.d("MESSAGES", ds.getValue(Chat.class).getMessage());
//                    Log.d("SIZE", mDisplayNames.size() +"");
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
        String id = mRecipientUID.get(mRNames.indexOf(name));
        //String id = "jmuFR6aaVaYj8enOr1bO9cmCxoZ2";
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("UID", id);
        intent.putExtra("NEW_MESSAGE", false);
        startActivity(intent);

    }
}
