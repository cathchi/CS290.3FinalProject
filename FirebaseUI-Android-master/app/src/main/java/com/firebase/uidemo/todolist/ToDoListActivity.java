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
import com.firebase.uidemo.todolist.OCR.OcrCaptureActivity;
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
    private HashMap<String, Task> tasks = new HashMap<String, Task>();
    private String childname;
    private TaskAdapter adapter;
    private ChildEventListener mChildListener;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todolist);

        Bundle b = getIntent().getExtras();
        childname = b.getString("childid");

        setTitle(childname);
        // Get ListView object from xml
        final ListView listView = (ListView) findViewById(R.id.listView);

        // Create a new Adapter
        adapter = new TaskAdapter(this, R.layout.todolist_item, R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // Connect to the Firebase database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String mUid = currentUser.getUid();
        myRef= database.getReference()
                .child("users").child(mUid).child("todolists").child(childname);

        // Assign a listener to detect changes to the child items
        // of the database reference.
        mChildListener = new ChildEventListener(){

            // This function is called once for each child that exists
            // when the listener is added. Then it is called
            // each time a new child is added.

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String value = dataSnapshot.child("task").getValue(String.class);
                String notes = dataSnapshot.child("notes").getValue(String.class);
                String location = dataSnapshot.child("location").child("place").getValue(String.class);
                Task newtask = new Task(value, notes, dataSnapshot.getKey(),location);
                tasks.put(dataSnapshot.getKey(), newtask);
                adapter.add(newtask);
            }

            // This function is called each time a child item is removed.
            public void onChildRemoved(DataSnapshot dataSnapshot){
                String taskid = dataSnapshot.getKey().toString();
                adapter.remove(tasks.get(taskid));
                tasks.remove(taskid);

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
        };

        myRef.addChildEventListener(mChildListener);

        // Add items via the Button and EditText at the bottom of the window.
        final EditText text = (EditText) findViewById(R.id.todoText);
        final Button button = (Button) findViewById(R.id.addButton);
        final Button ocr_button = (Button) findViewById(R.id.OCRButton);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Create a new child with a auto-generated ID.
                DatabaseReference childRef = myRef.push();


                // Set the child's data to the value passed in from the text box.
                childRef.child("task").setValue(text.getText().toString());
                Log.d("TASKVALUE", text.getText().toString());
                childRef.child("notes").setValue("");
                childRef.child("assign").setValue("");
                childRef.child("location").child("place").setValue("");
                text.setText("");
            }
        });

        ocr_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ToDoListActivity.this, OcrCaptureActivity.class);
                i.putExtra("toDoListID", childname);
                startActivity(i);
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
                titleSection.setText("Task: "+ ((Task)listView.getItemAtPosition(position)).getName());
                final TextView notesSection = (TextView) dView.findViewById(R.id.notes);
                notesSection.setText("Notes:");
                final TextView assignSection = (TextView) dView.findViewById(R.id.assign);
                assignSection.setText("Assigned to: ");
                final TextView locationSection = (TextView) dView.findViewById(R.id.location);
                locationSection.setText("Location: ");
                final int posIndex = position;
                adb.setPositiveButton("Edit", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(ToDoListActivity.this, TaskEditActivity.class);
                        String tid = adapter.getItem(posIndex).getTaskid();
                        i.putExtra("taskID", tid);
                        i.putExtra("toDoListID", childname);
                        startActivity(i);
                    }
                });//change to Edit`q
                adb.setNeutralButton("DELETE", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String tid = adapter.getItem(posIndex).getTaskid();
                        adapter.remove(tasks.get(tid));
                        myRef.child(tid).removeValue();
                        Log.d("DELETE", "removeID: " + tid);
                    }
                });
                adb.setNegativeButton("OK", null);
                adb.show();

                Query myQuery = myRef.child(adapter.getItem(posIndex).getTaskid());

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
                            if(dataSnapshot.child("location").getValue() != null)
                                locationSection.setText("Location: " + dataSnapshot.child("location").child("place").getValue().toString());
                            else
                                locationSection.setText("Location: ");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                })
                ;}
        })
        ;}

    @Override
    public void onRestart() {
        super.onRestart();
        updateList();
    }

    public void updateList() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener (){

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot taskid : dataSnapshot.getChildren()) {
                    Log.d("OLDTASK", taskid.getKey().toString());
                    Task oldTask = tasks.get(taskid.getKey());
                    int pos = adapter.getPosition(oldTask);
                    if(oldTask != null) {
                        String taskname = taskid.child("task").getValue(String.class);
                        String tasknotes = taskid.child("notes").getValue(String.class);
                        //String address = taskid.child("location").child("place").getValue(String.class);
                        boolean needsUpdate = oldTask.checkUpdates(taskname, tasknotes);
                        if(needsUpdate) {
                            oldTask.setTaskTitle(taskid.child("task").getValue(String.class));
                            oldTask.setNotes(taskid.child("notes").getValue(String.class));
                            //oldTask.setLocation(taskid.child("location").child("place").getValue(String.class));
                            adapter.remove(oldTask);
                            adapter.insert(oldTask, pos);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        Log.d("UPDATELIST", "list count = " + adapter.getCount());
        for (int i = 0; i < adapter.getCount(); i++) {
            Task t = adapter.getItem(i);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myRef.removeEventListener(mChildListener);
    }

}