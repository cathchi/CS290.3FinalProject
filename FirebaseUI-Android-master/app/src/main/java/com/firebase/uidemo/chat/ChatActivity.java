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

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.uidemo.R;
import com.firebase.uidemo.util.RecyclerViewClickListener;
import com.firebase.uidemo.util.SignInResultNotifier;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ChatActivity extends AppCompatActivity
        implements FirebaseAuth.AuthStateListener, RecyclerViewClickListener{

    private static final String TAG = "RecyclerViewDemo";

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private DatabaseReference mChatRef;
    private DatabaseReference mMessageRef;
    private DatabaseReference mReceiverChatRef;
    private StorageReference mStorageRef;
    private StorageReference mUserStorageRef;
    private StorageReference mRecipientStorageRef;
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
    private String mType;

    private FloatingActionButton audioButton;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int FILE_PATH_START = 20;
    private static final String TEXT_MESSAGE = "text";
    private static final String AUDIO_MESSAGE = "audio";
    private static final String AUDIO_EXTENSION = ".3pg";

    private String mFileName;
    private String mLastSegmentFileName;
    private boolean startedRecording = false;
    private boolean startPlaying = false;
    private MediaRecorder mRecorder;
    private MediaPlayer mMediaPlayer;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        audioButton = (FloatingActionButton) findViewById(R.id.audioButton);

        mReceiverUID = getIntent().getStringExtra("UID");
        mReceiverName = getIntent().getStringExtra("NAME");

        mRecipientUIDs.add(mReceiverUID);

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);

        mUID = mAuth.getCurrentUser().getUid();

        mSendButton = (Button) findViewById(R.id.sendButton);
        mMessageEdit = (EditText) findViewById(R.id.messageEdit);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = mRef.child("users");
        mMessageRef = ref.child(mAuth.getCurrentUser().getUid());
        mReceiverChatRef = ref.child(mReceiverUID).child("chats");
        mChatRef = mMessageRef.child("chats");

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
            updateMessage();
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

    /**
     * adds RecyclerView for scrolling through messages
     */
    private void attachRecyclerViewAdapter() {

        mAdapter = new ChatAdapter(this, mChats, mUID, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.messagesList);
        recyclerView.setAdapter(mAdapter);

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

    /**
     * queries Chats from SQLite database upon first load into the activity
     */
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
            Chat chat = new Chat(
                    cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_NAMES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_RNAMES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_MESSAGES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_UID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_RECIPIENTUID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_TIMESTAMP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatHistory.COLUMN_NAME_MESSAGETYPE)));
            int index = mChats.indexOf(chat);
            if (index < 0) {
                if (chat.getType().equals(AUDIO_MESSAGE) && !chat.getUid().equals(mUID)
                        && !fileExists(getExternalCacheDir().getAbsolutePath()
                        + "/" + chat.getMessage().substring(FILE_PATH_START))) {
                    downloadRecording(chat.getMessage().substring(FILE_PATH_START));
                }
                mChats.add(chat);
                Collections.sort(mChats);
                mAdapter.notifyItemInserted(mChats.size() - 1);
            }

        }
        db.close();
        cursor.close();
    }

    /**
     *
     */
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
                    if (chat.getType().equals(AUDIO_MESSAGE) && !chat.getUid().equals(mUID)
                            && !fileExists(getExternalCacheDir().getAbsolutePath() + "/" +
                            chat.getMessage().substring(FILE_PATH_START))) {
                        downloadRecording(chat.getMessage().substring(FILE_PATH_START));
                    }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private boolean checkPermission() {
        int externalStorage = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int audio = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return externalStorage == PackageManager.PERMISSION_GRANTED && audio == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(ChatActivity.this, new
                String[] { WRITE_EXTERNAL_STORAGE, RECORD_AUDIO }, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private boolean fileExists(String s) {
        File file = new File(s);
        if (file == null || !file.exists()) return false;
        return true;
    }

    public void record(View view) {
        if (checkPermission()) {
            if (startedRecording) {
                stopRecording();
                mType = AUDIO_MESSAGE;
                Calendar calendar = Calendar.getInstance();
                mDate = calendar.getTimeInMillis();
                mMessage = "Audio Message File: " + mLastSegmentFileName;
                storeStorage();
            }
            else {
                mFileName = getExternalCacheDir().getAbsolutePath();
                mLastSegmentFileName = System.currentTimeMillis()+ AUDIO_EXTENSION;
                mFileName += "/"
                        + mLastSegmentFileName;
                startRecording();
            }

        }
        else {
            requestPermission();
        }

    }

    private void startRecording() {
        Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
            startedRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "prepare() failed");
        } catch (IllegalStateException e) {
            Log.e(TAG, "start() failed");
        }

    }

    private void stopRecording() {
        try {
            Toast.makeText(this, "Recording Ended", Toast.LENGTH_SHORT).show();
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            startedRecording = false;
        } catch (IllegalStateException e){
            Log.e(TAG, "release() failed");
        } catch (NullPointerException e) {
            Log.e(TAG, "Null mRecorder");
        }
    }

    private void sendMessage() {
        String name = mAuth.getCurrentUser().getDisplayName();
        mChat = new Chat(name, mReceiverName, mMessage, mUID, mReceiverUID, mDate, mType);
        mChatRef.push().setValue(mChat, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference reference) {
                if (error != null) {
                    Log.e(TAG, "Failed to write message", error.toException());
                }
            }
        });
        mReceiverChatRef.push().setValue(mChat, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference reference) {
                if (error != null) {
                    Log.e(TAG, "Failed to write message", error.toException());
                }
            }
        });
    }

    private void storeStorage() {
        Uri file = Uri.fromFile(new File(mFileName));
        mUserStorageRef = mStorageRef.child(mUID).child(file.getLastPathSegment());
        UploadTask uploadTask = mUserStorageRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Failed to upload file");
                Toast.makeText(ChatActivity.this, "Failed to send", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "File successfully uploaded");
                sendMessage();
            }
        });
    }

    @Override
    public void recyclerViewItemClicked(int position) {
        if (startPlaying && mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (NullPointerException e){
                Log.e(TAG, "Media Player is null");
                e.printStackTrace();
            }
        }
        else {
            playRecording(position);
        }
    }

    private void downloadRecording(String s) {
        mRecipientStorageRef = mStorageRef.child(mReceiverUID).child(s);
        File localFile;
        localFile = new File(getExternalCacheDir().getAbsolutePath() + "/" + s);
        mFileName = localFile.getAbsolutePath();
        mRecipientStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "File successfully downloaded");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "File failed to download");
            }
        });
    }

    private void playRecording(int position) {
        mMediaPlayer = new MediaPlayer();
        mFileName = getExternalCacheDir().getAbsolutePath() + "/" +
                //extension + "/" +
                mChats.get(position).getMessage().substring(FILE_PATH_START);
        try {
            mMediaPlayer.setDataSource(mFileName);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        startPlaying = true;

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
                startPlaying = false;
            }
        });
    }

    public void send(View view) {
        mMessage = mMessageEdit.getText().toString();
        if (!mMessage.equals("")) {
            Calendar calendar = Calendar.getInstance();
            mDate = calendar.getTimeInMillis();
            mType = TEXT_MESSAGE;
            sendMessage();
        }
        mMessageEdit.setText("");
    }
}