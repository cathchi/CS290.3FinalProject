/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firebase.uidemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.firebase.uidemo.auth.AuthUiActivity;
import com.firebase.uidemo.auth.SignedInActivity;
import com.firebase.uidemo.database.ChatListActivity;
import com.firebase.uidemo.storage.ImageActivity;
import com.firebase.uidemo.todolist.ListsActivity;
import com.firebase.uidemo.todolist.ToDoListActivity;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChooserActivity extends AppCompatActivity {
    @BindView(R.id.activities)
    RecyclerView mActivities;

    private static final int RC_SIGN_IN = 100;

    private static final String UNCHANGED_CONFIG_VALUE = "CHANGE-ME";
    private static final String GOOGLE_TOS_URL = "https://www.google.com/policies/terms/";
    private static final String FIREBASE_TOS_URL = "https://firebase.google.com/terms/";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        mActivities.setLayoutManager(new LinearLayoutManager(this));
        mActivities.setAdapter(new ActivityChooserAdapter());
        mActivities.setHasFixedSize(true);
        if (mAuth.getCurrentUser() == null) {
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                            .setTheme(getSelectedTheme())
                            .setLogo(getSelectedLogo())
                            .setProviders(getProviders())
                            .setTosUrl(getSelectedTosUrl())
                            .setIsSmartLockEnabled(true)
                            .setAllowNewEmailAccounts(true)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    private static class ActivityChooserAdapter extends RecyclerView.Adapter<ActivityStarterHolder> {
        private static final Class[] CLASSES = new Class[]{
                ChatListActivity.class,
                AuthUiActivity.class,
                ImageActivity.class,
                ListsActivity.class
        };

        private static final int[] DESCRIPTION_NAMES = new int[]{
                R.string.name_chat,
                R.string.name_auth_ui,
                R.string.name_image,
                R.string.name_todolist
        };

        private static final int[] DESCRIPTION_IDS = new int[]{
                R.string.desc_chat,
                R.string.desc_auth_ui,
                R.string.desc_image,
                R.string.desc_todolist
        };

        @Override
        public ActivityStarterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ActivityStarterHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.activity_chooser_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ActivityStarterHolder holder, int position) {
            holder.bind(CLASSES[position], DESCRIPTION_NAMES[position], DESCRIPTION_IDS[position]);
        }

        @Override
        public int getItemCount() {
            return CLASSES.length;
        }
    }

    private static class ActivityStarterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitle;
        private TextView mDescription;

        private Class mStarterClass;

        public ActivityStarterHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.text1);
            mDescription = (TextView) itemView.findViewById(R.id.text2);
        }

        private void bind(Class aClass, @StringRes int name, @StringRes int description) {
            mStarterClass = aClass;

            mTitle.setText(name);
            mDescription.setText(description);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemView.getContext().startActivity(new Intent(itemView.getContext(), mStarterClass));
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == ResultCodes.OK) {
            startActivity(SignedInActivity.createIntent(this, response));
            //finish();
            return;
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                //showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                //showSnackbar(R.string.no_internet_connection);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                //showSnackbar(R.string.unknown_error);
                return;
            }
        }

        //showSnackbar(R.string.unknown_sign_in_response);
    }

    @MainThread
    @StyleRes
    private int getSelectedTheme() {
        return R.style.PurpleTheme;
    }

    @MainThread
    @DrawableRes
    private int getSelectedLogo() {
        return R.drawable.logo_googleg_color_144dp;
    }

    private List<AuthUI.IdpConfig> getProviders() {
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();
        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
        selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER)
                        .setPermissions(getFacebookPermissions())
                        .build());
        selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                        .setPermissions(getGooglePermissions())
                        .build());
        return selectedProviders;
    }

    @MainThread
    private String getSelectedTosUrl() {
        return FIREBASE_TOS_URL;
    }

    @MainThread
    private List<String> getGooglePermissions() {
        List<String> result = new ArrayList<>();
        return result;
    }

    @MainThread
    private List<String> getFacebookPermissions() {
        List<String> result = new ArrayList<>();
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sign_in:
                goToSignIn();
                return true;
            case R.id.sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToSignIn() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setTheme(getSelectedTheme())
                        .setLogo(getSelectedLogo())
                        .setProviders(getProviders())
                        .setTosUrl(getSelectedTosUrl())
                        .setIsSmartLockEnabled(true)
                        .setAllowNewEmailAccounts(true)
                        .build(),
                RC_SIGN_IN);
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.sign_in).setVisible(mAuth.getCurrentUser() == null);
        menu.findItem(R.id.sign_out).setVisible(mAuth.getCurrentUser() != null);
        return true;
    }
}
