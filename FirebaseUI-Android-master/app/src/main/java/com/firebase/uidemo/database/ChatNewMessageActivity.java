package com.firebase.uidemo.database;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.uidemo.R;
import com.firebase.uidemo.auth.User;
import com.firebase.uidemo.util.RecyclerViewClickListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CathyChi on 4/15/17.
 */

public class ChatNewMessageActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private DatabaseReference mRef;
    private List<String> mEmails = new ArrayList<>();
    private List<String> mUIDs = new ArrayList<>();
    private String mSearchedEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        mRef = FirebaseDatabase.getInstance().getReference().child("users");

        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mEmails.add(dataSnapshot.getValue(User.class).getEmail());
                mUIDs.add(dataSnapshot.getValue(User.class).getUid());
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

        final EditText editText = (EditText) findViewById(R.id.edit_text);
//        editText.setFocusableInTouchMode(true);
//        editText.requestFocus();
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                mSearchedEmail = editText.getText().toString();
                handleSearch();
                return false;
            }
        });

        //mOptionsAdapter = new ChatListAdapter(this, )
        //RecyclerView
    }

    private void handleSearch() {
        if (mSearchedEmail != null) {
            int index = mEmails.indexOf(mSearchedEmail);
            if (index>-1) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("UID", mUIDs.get(index));
                intent.putExtra("NEW_MESSAGE", false);
                startActivity(intent);
            }
            else {
                Toast.makeText(ChatNewMessageActivity.this,
                        "The email you have entered is not registered in our database.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void recyclerViewItemClicked(int position) {

    }
}
