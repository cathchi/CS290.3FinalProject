package compsci290.edu.duke.todolist;

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
 * Created by Katherine on 4/13/2017.
 */

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String [] listnames = {"Project 1", "Project 2"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fillListView();
        addListsfromFB();

    }

    public void addListsfromFB() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> td = (HashMap<String,Object>) dataSnapshot.getValue();
                Set<String> ids = td.keySet();
                listnames = ids.toArray(new String[0]);
                fillListView();
                System.out.println("names number: " + listnames.length);
                Log.d("Read success", "items added");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Read failed", "failed");
            }
        });
    }

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

    private void startListActivity(String id) {
        Intent i = new Intent(MainActivity.this, ListActivity.class);
        i.putExtra("childid", id);
        startActivity(i);
    }

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
