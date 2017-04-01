package com.firebase.uidemo.database;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.uidemo.R;
import com.firebase.uidemo.util.RecyclerViewClickListener;

import java.util.List;

/**
 * Created by CathyChi on 3/30/17.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private List<String> mNames;
    private Context mContext;
    private RecyclerViewClickListener mRecyclerViewClickListener;

    public ChatListAdapter(Context context, List<String> names, RecyclerViewClickListener recyclerViewClickListener) {
        this.mNames = names;
        this.mContext = context;
        this.mRecyclerViewClickListener = recyclerViewClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View itemView = layoutInflater.inflate(R.layout.chat, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = mNames.get(position);
        holder.textView.setText(name);
        final int p = position;
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerViewClickListener.recyclerViewItemClicked(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public View mView;

        public ViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.chat_name);
            mView = itemView;
        }
    }
}
