package com.firebase.uidemo.todolist;

import com.google.firebase.database.DatabaseReference;

/**
 * Created by Katherine on 4/28/2017.
 */

public class SharedListCreater {
    DatabaseReference myRef;

    public SharedListCreater(DatabaseReference ref, String id1, String uid2) {
        myRef = ref;
    }
}
