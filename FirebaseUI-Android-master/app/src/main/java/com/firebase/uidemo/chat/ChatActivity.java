/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firebase.uidemo.chat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.uidemo.R;
import com.firebase.uidemo.util.SignInResultNotifier;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    private static final String TAG = "RecyclerViewDemo";

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private DatabaseReference mChatRef;
    private DatabaseReference mMessageRef;
    private DatabaseReference mReceiverChatRef;
    private Button mSendButton;
    private EditText mMessageEdit;

    private RecyclerView mMessages;
    private LinearLayoutManager mManager;
    private ChatAdapter mAdapter;

    private Long mDate;
    private SQLiteOpenHelper mDBHelper;
    private String mMessage;
    private String mUID;
    private Chat mChat;
    private List<Chat> mChats = new ArrayList<>();
    private ArrayList<String> mRecipientUIDs = new ArrayList<>();
    private String mReceiverUID;
    private String mReceiverName;

    private boolean firstDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        firstDownload = false;
        //Log.d("MESSAGE", mChats.get(0).getMessage());
        mReceiverUID = getIntent().getStringExtra("UID");
        mReceiverName = getIntent().getStringExtra("NAME");

        mRecipientUIDs.add(mReceiverUID);

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);

        mUID = mAuth.getCurrentUser().getUid();

        mSendButton = (Button) findViewById(R.id.sendButton);
        mMessageEdit = (EditText) findViewById(R.id.messageEdit);

        mRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = mRef.child("users");
        mMessageRef = ref.child(mAuth.getCurrentUser().getUid());
        mReceiverChatRef = ref.child(mReceiverUID).child("chats");
        mChatRef = mMessageRef.child("chats");

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mAuth.getCurrentUser().getDisplayName();

                mMessage = mMessageEdit.getText().toString();

                Calendar calendar = Calendar.getInstance();
                mDate = calendar.getTimeInMillis();
                Log.d("ChatActivity", "Pushing chat with RName=" + mReceiverName);
                mChat = new Chat(name, mReceiverName, mMessage, mUID, mReceiverUID, mDate);
                mChatRef.push().setValue(mChat, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference reference) {
                        if (error != null) {
                            Log.e(TAG, "Failed to write message", error.toException());
                        }
                    }
                });
                mMessageEdit.setText("");

                mReceiverChatRef.push().setValue(mChat, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference reference) {
                        if (error != null) {
                            Log.e(TAG, "Failed to write message", error.toException());
                        }
                    }
                });


                //storeToDatabase();
            }
        });

        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(false);

        mMessages = (RecyclerView) findViewById(R.id.messagesList);
        mMessages.setHasFixedSize(false);
        mMessages.setLayoutManager(mManager);

    }

    @Override
    public void onStart() {
        super.onStart();

        // Default Database rules do not allow unauthenticated reads, so we need to
        // sign in before attaching the RecyclerView adapter otherwise the Adapter will
        // not be able to read any data from the Database.
        if (isSignedIn()) {
            attachRecyclerViewAdapter();
            readFromDatabase();
            if (firstDownload) {
                updateMessage();
            }
        } else {
            signInAnonymously();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAuth != null) {
            mAuth.removeAuthStateListener(this);
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        updateUI();
    }

    private void attachRecyclerViewAdapter() {

        /*Query lastFifty = mChatRef.limitToLast(50);
        mAdapter = new FirebaseRecyclerAdapter<Chat, ChatHolder>(
                Chat.class, R.layout.message, ChatHolder.class, lastFifty) {
            @Override
            public void populateViewHolder(ChatHolder holder, Chat chat, int position) {
                holder.setName(chat.getName());
                holder.setText(chat.getMessage());

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null && chat.getUid().equals(currentUser.getUid())) {
                    holder.setIsSender(true);
                } else {
                    holder.setIsSender(false);
                }
            }

            @Override
            protected void onDataChanged() {
                // If there are no chat messages, show a view that invites the user to add a message.
                mEmptyListMessage.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        };

        // Scroll to bottom on new messages
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mManager.smoothScrollToPosition(mMessages, null, mAdapter.getItemCount());
            }
        });*/

        mAdapter = new ChatAdapter(this, mChats, mUID);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.messagesList);
        recyclerView.setAdapter(mAdapter);

        //mMessages.setAdapter(mAdapter);
    }

    private void signInAnonymously() {
        Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show();
        mAuth.signInAnonymously()
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult result) {
                        attachRecyclerViewAdapter();
                    }
                })
                .addOnCompleteListener(new SignInResultNotifier(this));
    }

    private boolean isSignedIn() {
        return mAuth.getCurrentUser() != null;
    }

    private void updateUI() {
        // Sending only allowed when signed in
        mSendButton.setEnabled(isSignedIn());
        mMessageEdit.setEnabled(isSignedIn());
    }


    private void readFromDatabase() {
        ChatActivity.this.mDBHelper = new ChatHistoryDBHelper(getApplicationContext());

        SQLiteDatabase db = ChatActivity.this.mDBHelper.getReadableDatabase();

        String[] projection = {
                "*"
        };

        String selection = "(" + ChatContract.ChatHistory.COLUMN_NAME_UID + " = ?"
                + " AND " + ChatContract.ChatHistory.COLUMN_NAME_RECIPIENTUID + " = ?) OR ("
                + ChatContract.ChatHistory.COLUMN_NAME_UID + " = ? AND " +
                ChatContract.ChatHistory.COLUMN_NAME_RECIPIENTUID + " = ?)";
        String[] selectionArgs = {mUID, mReceiverUID, mReceiverUID, mUID};

        String sortOrder = ChatContract.ChatHistory.COLUMN_NAME_TIMESTAMP + " DESC";

        Cursor cursor = db.query(
                ChatContract.ChatHistory.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        while (cursor.moveToNext()) {
            Chat e = new Chat(
                    cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_NAMES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_RNAMES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_MESSAGES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_UID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_RECIPIENTUID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_TIMESTAMP))
            );
            int index = mChats.indexOf(e);
            if (index < 0) {
                mChats.add(e);
                Collections.sort(mChats);
                mAdapter.notifyItemInserted(mChats.size() - 1);
            }
            Log.d("DATABASESQLITE", "ADDED");

        }
        Log.d("EXECUTED", "DONE");
        db.close();
        cursor.close();
        firstDownload = true;
    }

    private void updateMessage() {
        mChatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                int index = mChats.indexOf(chat);
                if ((chat.getName().equals(mAuth.getCurrentUser().getDisplayName()) ||
                        chat.getRName().equals(mAuth.getCurrentUser().getDisplayName())) &&
                        (chat.getName().equals(mReceiverName) ||
                                chat.getRName().equals(mReceiverName)) && index < 0) {
                    // add part of code where every single UID of the people is checked
                    mChats.add(chat); // this is not sorted potentially
                    Collections.sort(mChats);
                    mAdapter.notifyItemInserted(mChats.size() - 1);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}