package com.firebase.uidemo.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Katherine on 4/14/2017.
 * Displays all the user's to-do lists.
 */

public class ListsActivity extends AppCompatActivity {
    private List<ListItem> listitems = new ArrayList<ListItem>();
    //private List<String> listids = new ArrayList<>();
    private String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listoptions);

        fillListView();
        addListsfromFB();

        setTitle("Your To-Do Lists");
    }

    // gets all the to do lists from Firebase
    // listname is set to the list retreived
    public void addListsfromFB() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUid = currentUser.getUid();
        final DatabaseReference ref= database.getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> td = (HashMap<String,Object>)dataSnapshot.child("users").child(mUid).child("todolists").getValue();
                if(td != null) {
                    Set<String> ids = td.keySet();
                    for(String id : ids) {
                        DataSnapshot list = dataSnapshot.child("lists").child(id);
                        String title = list.child("title").getValue().toString();
                        ArrayList<String> Users = new ArrayList<String>();
                        for(DataSnapshot user: list.child("users").getChildren()){
                            String userID = user.getValue().toString();
                            String name = dataSnapshot.child("users").child(userID).child("name").getValue().toString();
                            Users.add(name);
                        }
                        ListItem myList= new ListItem(title, Users, id);
                        listitems.add(myList);
                        /*
                        listitems.add
                        listids.add(id);

                        if(list.child("users").child("1").getValue() != null)
                            listnames.add("(SHARED) " + list.child("title").getValue().toString());
                        else
                            listnames.add(list.child("title").getValue().toString());*/
                    }
                }
                else {
                    //listids = new ArrayList<>();
                    listitems = new ArrayList<ListItem>();
                }
                fillListView();
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
        final ListAdapter adapter = new ListAdapter(this, R.layout.list_item, R.id.listnametext);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
        //listView.setOnItemClickListener(this);
        for(ListItem listitem: listitems){

            adapter.add(listitem);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startListActivity(adapter.getItem(position).getMyID(),
                        adapter.getItem(position).getListTitle(),
                        adapter.getItem(position).getUserString());
            }
        });
    }

    /*public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        startListActivity(listids.get(position), listnames.get(position));
    }*/

    // starts new activity
    private void startListActivity(String id, String name, String users) {
        Intent i = new Intent(ListsActivity.this, ToDoListActivity.class);
        i.putExtra("childid", id);
        i.putExtra("childname", name);
        i.putExtra("users", users);
        startActivity(i);
    }

    // handles when user clicks "make new list" button to create a new to-do list
    // uses AlertDialog and EditText to allow user to enter a name for the to-do list
    // when user clicks "ok" the ToDoListActivity starts to display the new list.
    public void onClickNewList(View v) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        alertDialogBuilder.setTitle("New List");
        alertDialogBuilder.setMessage("Name this list: ");

        final EditText et = new EditText(this.getApplicationContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        et.setLayoutParams(lp);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(et);
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = et.getText().toString();
                Log.d("Got text", text);
                if(!text.equals("")) {
                    NewListCreater create = new NewListCreater(text);
                    String newid = create.addToFirebase();
                    startListActivity(newid, text, "");
                    //listids.add(newid);
                    listitems.add(create.getListObject());
                    alertDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //addListsfromFB();
        fillListView();
    }
}
