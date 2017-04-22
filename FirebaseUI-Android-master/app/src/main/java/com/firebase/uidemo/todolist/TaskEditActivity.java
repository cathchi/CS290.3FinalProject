package com.firebase.uidemo.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.uidemo.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by JasmineLu on 4/15/17.
 */

public class TaskEditActivity extends Activity {
    public static final int LOCATION_REQUEST = 0;
    private DatabaseReference myRef;
    private TextView addressText;
    private String mLat, mLong, mPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);

        Bundle b = getIntent().getExtras();
        final String toDoListID = b.getString("toDoListID");
        String taskID = b.getString("taskID");

        // Connect to the Firebase database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String mUid = currentUser.getUid();
        myRef= database.getReference()
                .child("users").child(mUid).child("todolists").child(toDoListID).child(taskID);

        final EditText taskEdit = (EditText) findViewById(R.id.taskNameEdit);
        final EditText notesEdit = (EditText) findViewById(R.id.notesEdit);
        final EditText assignEdit = (EditText) findViewById(R.id.assignEdit);

        addressText = (TextView) findViewById(R.id.locationEdit);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    taskEdit.setText(dataSnapshot.child("task").getValue(String.class));
                    if (dataSnapshot.child("notes").getValue() != null)
                        notesEdit.setText(dataSnapshot.child("notes").getValue(String.class));
                    if (dataSnapshot.child("assign").getValue() != null)
                        assignEdit.setText(dataSnapshot.child("assign").getValue(String.class));
                    if (dataSnapshot.child("location").child("place").getValue() != null) {
                        addressText.setText(dataSnapshot.child("location").child("place").getValue(String.class));
                        mLat = dataSnapshot.child("location").child("lat").getValue(String.class);
                        mLong = dataSnapshot.child("location").child("long").getValue(String.class);
                        mPlace = dataSnapshot.child("location").child("place").getValue(String.class);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Read failed", "failed");
            }
        });

        final Button done = (Button) findViewById(R.id.doneButton);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child("task").setValue(taskEdit.getText().toString());
                myRef.child("notes").setValue(notesEdit.getText().toString());
                myRef.child("assign").setValue(assignEdit.getText().toString());
                finish();
            }
        });

        addressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TaskEditActivity.this, PlaceActivity.class);
                i.putExtra("lat", mLat);
                i.putExtra("long", mLong);
                i.putExtra("place", mPlace);
                startActivityForResult(i,LOCATION_REQUEST);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("ResultA", Integer.toString(resultCode));
        if(resultCode == Activity.RESULT_OK){
            mPlace = data.getStringExtra("place");
            mLong = data.getStringExtra("long");
            mLat = data.getStringExtra("lat");
            myRef.child("location").child("place").setValue(mPlace);
            myRef.child("location").child("long").setValue(mLong);
            myRef.child("location").child("lat").setValue(data.getStringExtra(mLat));
            myRef.child("location").child("address").setValue(data.getStringExtra("address"));
            addressText.setText(mPlace);
            Log.d("TaskEditActivity", "address set");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
