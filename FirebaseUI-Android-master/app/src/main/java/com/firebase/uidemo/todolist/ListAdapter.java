package com.firebase.uidemo.todolist;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.firebase.uidemo.R;

/**
 * Created by JasmineLu on 4/29/17.
 */

public class ListAdapter extends ArrayAdapter<ListItem> {
    private Context context;
    private int listslayout;

    public ListAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId) {
        super(context, resource, textViewResourceId);
        this.context = context;
        this.listslayout = resource;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            view= inflater.inflate(listslayout, parent, false);
        }
        TextView ListTitle = (TextView) view.findViewById(R.id.text1);
        TextView ListUsers = (TextView) view.findViewById(R.id.text2);

        final ListItem list = getItem(position);

        ListTitle.setText(list.getListTitle());
        if(list.isShared())
            ListUsers.setText("Shared by: " + list.getUserString());
        return view;
    }
}
