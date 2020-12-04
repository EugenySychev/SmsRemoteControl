package com.sychev.smsremotecontrol.view;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sychev.smsremotecontrol.R;
import com.sychev.smsremotecontrol.data.SettingsStore;

public class ContactSelectionActivity extends AppCompatActivity implements ContactListAdapter.ItemClickListener, SwipeCallback.SwipeActionCallback {

    private static final int CONTACT_REQUEST_CODE = 20;
    private static final String TAG = "CONT_SELECTOR";
    private com.sychev.smsremotecontrol.view.ContactListAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_selection);


        recyclerView = findViewById(R.id.contactListRecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactListAdapter(this, SettingsStore.getInstance().getPhoneNumbers());
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeCallback(adapter, this));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        ActionBar toolbar = getSupportActionBar();
        toolbar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_nav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return super.onOptionsItemSelected(item);
        }
        if (id == R.id.action_add_source) {
            Intent phonebookIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            phonebookIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(phonebookIntent, CONTACT_REQUEST_CODE);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CONTACT_REQUEST_CODE) {
            String phoneNo = null;
            String name = null;

            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            Log.d(TAG, "Uri is " + uri.toString());
            if (cursor.moveToFirst()) {
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                phoneNo = cursor.getString(phoneIndex);
                SettingsStore.getInstance().addPhoneNumber(phoneNo);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void processSwipe(int position) {
        SettingsStore.getInstance().deletePhoneNumber(adapter.getItem(position));
    }
}