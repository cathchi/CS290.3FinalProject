package com.firebase.uidemo.database;

import android.support.v7.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

/**
 * Created by CathyChi on 4/4/17.
 */

public class ChatAdapter extends FirebaseRecyclerAdapter {
    /**
     * @param modelClass      Firebase will marshall the data at a location into
     *                        an instance of a class that you provide
     * @param modelLayout     This is the layout used to represent a single item in the list.
     *                        You will be responsible for populating an instance of the corresponding
     *                        view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     *
     */
    public ChatAdapter(Class modelClass, int modelLayout, Class viewHolderClass) {
        super(modelClass, modelLayout, viewHolderClass, null);
    }

    @Override
    protected void populateViewHolder(RecyclerView.ViewHolder viewHolder, Object model, int position) {

    }
}
