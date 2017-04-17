package com.firebase.uidemo.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.firebase.uidemo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Katherine on 4/14/2017.
 * Displays all the user's to-do lists.
 */

public class ListsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private String [] listnames = new String[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listoptions);

        fillListView();
        addListsfromFB();

    }

    // gets all the to do lists from Firebase
    // listname is set to the list retreived
    public void addListsfromFB() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String mUid = currentUser.getUid();
        final DatabaseReference ref= database.getReference()
                .child("users").child(mUid).child("todolists");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> td = (HashMap<String,Object>) dataSnapshot.getValue();
                if(td != null) {
                    Set<String> ids = td.keySet();
                    listnames = ids.toArray(new String[0]);
                }
                else {
                    listnames = new String [0];
                }
                fillListView();
                Log.d("names number", listnames.length+"");
                Log.d("Read success", "items added");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Read failed", "failed");
            }
        });
    }

    // adds the contents of listname into the display
    public void fillListView() {
        final ListView listView = (ListView) findViewById(R.id.listView);

        // Create a new Adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, listnames);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        startListActivity(listnames[position]);
    }

    // starts new activity
    private void startListActivity(String id) {
        Intent i = new Intent(ListsActivity.this, ToDoListActivity.class);
        i.putExtra("childid", id);
        startActivity(i);
    }

    // handles when user clicks "make new list" button to create a new to-do list
    // uses AlertDialog and EditText to allow user to enter a name for the to-do list
    // when user clicks "ok" the ToDoListActivity starts to display the new list.
    public void onClickNewList(View v) {
        Log.d("Inside onClickNewList", "yes");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        alertDialogBuilder.setTitle("New List");
        alertDialogBuilder.setMessage("Name this list: ");

        final EditText et = new EditText(this.getApplicationContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        et.setLayoutParams(lp);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(et);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String text = et.getText().toString();
                Log.d("Got text", text);
                NewListCreater create = new NewListCreater(text);
                String newid = create.addToFirebase();
                startListActivity(newid);
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }
}
