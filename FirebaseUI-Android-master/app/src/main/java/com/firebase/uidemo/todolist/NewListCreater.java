package com.firebase.uidemo.todolist;

import android.util.Log;

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
 * Created by Katherine on 4/14/2017.
 * Helps create a new list by adding the list to firebase
 */

public class NewListCreater {
    private static final String TAG = "NewListCreater";
    private static final String DATABASE_REF_USERS = "users";
    private static final String DATABASE_REF_TODOLIST = "todolists";
    private static final String DATABASE_REF_LIST = "lists";
    private static final String DATABASE_REF_TITLE = "title";
    private static final String DATABASE_REF_NAME = "name";
    private static final String DATABASE_REF_SHARED = "shared";


    private String mTitle, mUID;
    private FirebaseDatabase database;
    private DatabaseReference mListRef, mUserRef;
    private List<String> users = new ArrayList<>();
    public NewListCreater(String listname) {
        mTitle = listname;
    }

    // stores the list as a child
    public String addToFirebase() {
        database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUID = currentUser.getUid();
        mUserRef = database.getReference()
                .child(DATABASE_REF_USERS).child(mUID).child(DATABASE_REF_TODOLIST);
        mListRef = database.getReference().child(DATABASE_REF_LIST ).push();
        mUserRef.child(mListRef.getKey()).child(DATABASE_REF_TITLE).setValue(mTitle);
        mListRef.child("title").setValue(mTitle);
        users.add(mUID);

        mListRef.child(DATABASE_REF_USERS).setValue(users);
        Log.d(TAG, mListRef.getKey());
        return mListRef.getKey();
    }

    public String addSharedToFirebase(String otherUId) {
        users.add(otherUId);
        addToFirebase();
        DatabaseReference otherRef = database.getReference()
                .child(DATABASE_REF_USERS).child(otherUId).child(DATABASE_REF_TODOLIST);
        otherRef.child(mListRef.getKey()).child(DATABASE_REF_TITLE).setValue(mTitle);

        otherRef.child(mListRef.getKey()).child(DATABASE_REF_SHARED).setValue(mUID);
        mUserRef.child(mListRef.getKey()).child(DATABASE_REF_SHARED).setValue(otherUId);

        return mListRef.getKey();
    }

    public ListItem getListObject(){
        final ArrayList<String> userNames = new ArrayList<>();
        for(String user: users){
            DatabaseReference myuser = database.getReference().child(DATABASE_REF_USERS).child(user).child(DATABASE_REF_NAME);
            myuser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userNames.add(dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "failed");
                }
            });
        }
        return new ListItem(mTitle, userNames, mListRef.getKey());
    }

}