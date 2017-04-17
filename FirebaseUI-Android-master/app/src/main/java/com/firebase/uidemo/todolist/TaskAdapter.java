package com.firebase.uidemo.todolist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.firebase.uidemo.R;

import java.util.ArrayList;

/**
 * Created by Katherine on 4/17/2017.
 */

public class TaskAdapter extends ArrayAdapter<Task> {
    private Context context;
    private int listitemlayout;

    public TaskAdapter(@NonNull Context context, int listitemlayout, int textid) {
        super(context, listitemlayout, textid);
        this.context = context;
        this.listitemlayout = listitemlayout;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            view= inflater.inflate(listitemlayout, parent, false);
        }
        TextView taskTitle = (TextView) view.findViewById(R.id.text1);
        TextView taskNotes = (TextView) view.findViewById(R.id.text2);

        final Task task = getItem(position);

        taskTitle.setText(task.getName());
        taskNotes.setText(task.getNotes());

        return view;
    }

}
