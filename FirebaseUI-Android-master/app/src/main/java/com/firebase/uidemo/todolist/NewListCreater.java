package com.firebase.uidemo.todolist;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Katherine on 4/14/2017.
 * Helps create a new list by adding the list to firebase
 */

public class NewListCreater {

    private String title;

    public NewListCreater(String listname) {
        title = listname;
    }

    public NewListCreater() {
        this(null);
    }

    // stores the list as a child
    public String addToFirebase() {
        String id = createId();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String mUid = currentUser.getUid();
        DatabaseReference ref = database.getReference()
                .child("users").child(mUid).child("todolists").child(id);
        return id;
    }

    // generates an ID for the list for Firebase
    private String createId() {
        String s = title.replaceAll("\\s+","");
        return s;
    }
}