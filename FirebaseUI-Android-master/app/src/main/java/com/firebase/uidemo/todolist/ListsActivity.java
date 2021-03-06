package com.firebase.uidemo.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.uidemo.R;
import com.firebase.uidemo.auth.SignedInActivity;
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
    private static final String TAG = "ListsActivity";
    private static final String DATABASE_REF_USERS = "users";
    private static final String DATABASE_REF_TODOLIST = "todolists";
    private static final String DATABASE_REF_LIST = "lists";
    private static final String DATABASE_REF_TITLE = "title";
    private static final String DATABASE_REF_NAME = "name";
    private static final String INTENT_REF_ID = "childid";
    private static final String INTENT_REF_NAME = "childname";

    private List<ListItem> mListItems = new ArrayList<>();
    private String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listoptions);

        fillListView();
        addListsfromFB();

        setTitle("Your To-Do Lists");
    }

    /**
     * gets all the to do lists from Firebase
     * listname is set to the list retreived
     * */
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
                            Object nameobj = dataSnapshot.child("users").child(userID).child("name").getValue();
                            if(nameobj != null) {
                                String name = nameobj.toString();
                                Users.add(name);
                            }
                        }
                        ListItem myList= new ListItem(title, Users, id);
                        mListItems.add(myList);
                    }
                }
                else {
                    mListItems = new ArrayList<>();
                }
                fillListView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "failed");
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
        for(ListItem listitem: mListItems){

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

    // starts new activity
    private void startListActivity(String id, String name, String users) {
        Intent i = new Intent(ListsActivity.this, ToDoListActivity.class);
        i.putExtra(INTENT_REF_ID, id);
        i.putExtra(INTENT_REF_NAME, name);
        i.putExtra(DATABASE_REF_USERS, users);
        startActivity(i);
    }

    /**
     * handles when user clicks "make new list" button to create a new to-do list
     * uses AlertDialog and EditText to allow user to enter a name for the to-do list
     * when user clicks "ok" the ToDoListActivity starts to display the new list.
     */
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
                Log.d(TAG, text);
                if(!text.equals("")) {
                    NewListCreater create = new NewListCreater(text);
                    String newid = create.addToFirebase();
                    startListActivity(newid, text, "");
                    mListItems.add(create.getListObject());
                    alertDialog.dismiss();
                }
            }
        });
    }
    // menu item to go to shared list between two users
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.return_home:
                goHome();
                return true;
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(this);
        goHome();
    }

    public void goHome(){
        Intent intent = new Intent(getApplicationContext(), SignedInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        fillListView();
    }
}
