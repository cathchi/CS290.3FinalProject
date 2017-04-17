package com.firebase.uidemo.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.uidemo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Created by Katherine on 4/14/2017.
 */

public class ToDoListActivity extends AppCompatActivity {
    private ArrayList<String> taskIDs = new ArrayList<String>();
    String childname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todolist);

        Bundle b = getIntent().getExtras();
        childname = b.getString("childid");

        // Get ListView object from xml
        final ListView listView = (ListView) findViewById(R.id.listView);

        // Create a new Adapter
        //final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        //        android.R.layout.simple_list_item_checked, android.R.id.text1);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
               android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // Connect to the Firebase database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String mUid = currentUser.getUid();
        final DatabaseReference myRef= database.getReference()
                .child("users").child(mUid).child("todolists").child(childname);

        // Get a reference to the todoItems child items it the database
        //final DatabaseReference myRef = database.getReference("todoItems");

        // Assign a listener to detect changes to the child items
        // of the database reference.
        myRef.addChildEventListener(new ChildEventListener(){

            // This function is called once for each child that exists
            // when the listener is added. Then it is called
            // each time a new child is added.

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String value = dataSnapshot.child("task").getValue(String.class);
                taskIDs.add(dataSnapshot.getKey());
                adapter.add(value);
            }

            // This function is called each time a child item is removed.
            public void onChildRemoved(DataSnapshot dataSnapshot){
                String value = dataSnapshot.child("task").getValue(String.class);
                adapter.remove(value);
            }

            // The following functions are also required in ChildEventListener implementations.
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName){
                Log.d("onchildchanged","prevchild: " + previousChildName);
            }
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName){}

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG:", "Failed to read value.", error.toException());
            }
        });

        // Add items via the Button and EditText at the bottom of the window.
        final EditText text = (EditText) findViewById(R.id.todoText);
        final Button button = (Button) findViewById(R.id.addButton);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Create a new child with a auto-generated ID.
                DatabaseReference childRef = myRef.push();


                // Set the child's data to the value passed in from the text box.
                childRef.child("task").setValue(text.getText().toString());
                childRef.child("notes").setValue("");
                childRef.child("assign").setValue("");

            }
        });

        //get item data on click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                final LayoutInflater inflater = ToDoListActivity.this.getLayoutInflater();
                AlertDialog.Builder adb = new AlertDialog.Builder(
                        ToDoListActivity.this);
                View dView = inflater.inflate(R.layout.task_details_dialog, null);
                adb.setView(dView);
                final TextView titleSection = (TextView)  dView.findViewById(R.id.taskTitle);
                titleSection.setText("Task: "+ listView.getItemAtPosition(position).toString());
                final TextView notesSection = (TextView) dView.findViewById(R.id.notes);
                notesSection.setText("Notes:");
                final TextView assignSection = (TextView) dView.findViewById(R.id.assign);
                assignSection.setText("Assigned to: ");
                final int posIndex = position;
                adb.setPositiveButton("Edit", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        /*AlertDialog.Builder editor = new AlertDialog.Builder(ToDoListActivity.this);
                        View editorView = inflater.inflate(R.layout.task_editor_dialog, null);
                        editor.setView(editorView);
                        EditText titleEdit = (EditText) editorView.findViewById(R.id.titleEdit);
                        editor.show();*/
                        Intent i = new Intent(ToDoListActivity.this, TaskEditActivity.class);
                        i.putExtra("taskID", taskIDs.get(posIndex));
                        i.putExtra("toDoListID", childname);
                        startActivity(i);
                    }
                });//change to Edit
                adb.setNeutralButton("DELETE", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Query myQuery = myRef.child(taskIDs.get(posIndex));

                        /*myRef.child(taskIDs.get(posIndex)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.d("onDataChange DELETE", "dataSnapshot:" + dataSnapshot.toString());
                                /*for (DataSnapshot testSnapshot: dataSnapshot.getChildren()) {
                                    testSnapshot.getRef().removeValue();
                                }
                                //dataSnapshot.getRef().removeValue();
                                dataSnapshot.getRef().child("notes").setValue(null);
                                dataSnapshot.getRef().child("assign").setValue(null);
                                dataSnapshot.getRef().setValue(null);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });*/
                        Log.d("DELETE", "removeID: " + taskIDs.get(posIndex));
                        myRef.child(taskIDs.get(posIndex)).removeValue();
                    }
                });
                adb.setNegativeButton("OK", null);
                adb.show();

                Query myQuery = myRef.child(taskIDs.get(position));

                myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            DataSnapshot notesRef = dataSnapshot.child("notes");
                            if(notesRef.getValue() != null)
                                notesSection.setText("Notes: " + notesRef.getValue().toString());
                            else
                                notesSection.setText("Notes: ");
                            if(dataSnapshot.child("assign").getValue() != null)
                                assignSection.setText("Assigned to: " + dataSnapshot.child("assign").getValue().toString());
                            else
                                assignSection.setText("Assigned to: ");

                            //firstChild.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                })
                ;}
        })
        ;}
}