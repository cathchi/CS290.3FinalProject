package com.firebase.uidemo.database;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.uidemo.R;
import com.firebase.uidemo.util.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CathyChi on 3/30/17.
 */

public class ChatListActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private List<String> mNames;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mNames = new ArrayList<>();
        ChatAdapter chatAdapter = new ChatAdapter(this, mNames, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.chats);
        recyclerView.setAdapter(chatAdapter);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }


    @Override
    public void recyclerViewItemClicked(int position) {

    }
}
