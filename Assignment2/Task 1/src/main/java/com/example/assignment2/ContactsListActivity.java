package com.example.assignment2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.example.assignment2.modules.PubSubBroker;
import com.example.assignment2.modules.Subscriber;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class ContactsListActivity extends AppCompatActivity {

    private ContactAdapter adapter;
    private List<Contact> contactList;

    @SuppressLint("NotifyDataSetChanged")
    private final Subscriber<Contact> contactSubscriber = message -> runOnUiThread(() -> {
        contactList.clear();
        contactList.addAll(ContactStorage.loadContacts(this));
        adapter.notifyDataSetChanged();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PubSubBroker.getInstance().subscribe("CONTACTS_CHANGED", contactSubscriber);

        contactList = ContactStorage.loadContacts(this);
        if (contactList.isEmpty()) {
            populateInitialContacts();
        }


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new ContactAdapter(contactList, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, ContactActivity.class);
            startActivity(intent);
        });
    }

    private void populateInitialContacts() {

        contactList.add(new Contact("User 1" , "066123123431" , R.drawable.avatar_01));
        contactList.add(new Contact("User 2" , "066123123432" , R.drawable.avatar_02));
        contactList.add(new Contact("User 3" , "066123123433", R.drawable.avatar_03));
        contactList.add(new Contact("User 4" , "066123123434" , R.drawable.avatar_04));
        contactList.add(new Contact("User 5" , "066123123435" , R.drawable.avatar_05));
        contactList.add(new Contact("User 6" , "066123123436" , R.drawable.avatar_06));
        contactList.add(new Contact("User 7" , "066123123437", R.drawable.avatar_07));
        contactList.add(new Contact("User 8" , "066123123438", R.drawable.avatar_08));
        contactList.add(new Contact("User 9" , "066123123439", R.drawable.avatar_09));
        contactList.add(new Contact("User 10", "0661231234310", R.drawable.avatar_pokemon));


        ContactStorage.saveContacts(this, contactList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PubSubBroker.getInstance().unsubscribe("CONTACTS_CHANGED", contactSubscriber);
    }
}