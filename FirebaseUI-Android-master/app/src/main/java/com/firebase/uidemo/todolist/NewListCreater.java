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

    private String title, mUid;
    private FirebaseDatabase database;
    private DatabaseReference listRef, userRef, otherRef;
    private List<String> users = new ArrayList<>();
    public NewListCreater(String listname) {
        title = listname;
    }

    // stores the list as a child
    public String addToFirebase() {
        database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUid = currentUser.getUid();
        userRef = database.getReference()
                .child("users").child(mUid).child("todolists");
        listRef = database.getReference().child("lists").push();
        userRef.child(listRef.getKey()).child("title").setValue(title);
        listRef.child("title").setValue(title);
        users.add(mUid);

        listRef.child("users").setValue(users);
        Log.d("New List Creater", listRef.getKey());
        return listRef.getKey();
    }

    public String addSharedToFirebase(String otherUId) {
        users.add(otherUId);
        addToFirebase();
        otherRef = database.getReference()
                .child("users").child(otherUId).child("todolists");
        otherRef.child(listRef.getKey()).child("title").setValue(title);

        otherRef.child(listRef.getKey()).child("shared").setValue(mUid);
        userRef.child(listRef.getKey()).child("shared").setValue(otherUId);

        return listRef.getKey();
    }

    public ListItem getListObject(){
        final ArrayList<String> userNames = new ArrayList<String>();
        for(String user: users){
            DatabaseReference myuser = database.getReference().child("users").child(user).child("name");
            myuser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userNames.add(dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("Read failed", "failed");
                }
            });
        }
        ListItem myList = new ListItem(title, userNames, listRef.getKey());
        return myList;
    }

}