package com.firebase.uidemo.todolist;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

/**
 * Created by JasmineLu on 4/15/17.
 */

public class ListItemAdapter extends ArrayAdapter {
    public ListItemAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

}
