package com.example.assignment2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment2.modules.Message;
import com.example.assignment2.modules.PubSubBroker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Button;
import java.util.List;

public class ContactActivity extends AppCompatActivity {
    private EditText editName, editNumber;
    private String currentContactId = null;
    private List<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        editName = findViewById(R.id.edit_name);
        editNumber = findViewById(R.id.edit_number);
        Button btnSave = findViewById(R.id.btn_save);
        FloatingActionButton fabDial = findViewById(R.id.fab_dial);

        contacts = ContactStorage.loadContacts(this);

        if (getIntent().hasExtra("CONTACT_ID")) {
            currentContactId = getIntent().getStringExtra("CONTACT_ID");
            for (Contact c : contacts) {
                if (c.getId().equals(currentContactId)) {
                    editName.setText(c.getName());
                    editNumber.setText(c.getNumber());
                    break;
                }
            }
        }

        btnSave.setOnClickListener(v -> saveContact());

        fabDial.setOnClickListener(v -> {
            String num = editNumber.getText().toString();
            if (!num.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
                startActivity(intent);
            }
        });
    }

    private void saveContact() {
        String name = editName.getText().toString();
        String number = editNumber.getText().toString();

        if (currentContactId != null) {
            for (Contact c : contacts) {
                if (c.getId().equals(currentContactId)) {
                    c.setName(name);
                    c.setNumber(number);
                    break;
                }
            }
        } else {
            contacts.add(new Contact(name, number, R.drawable.avatar_01));
        }

        ContactStorage.saveContacts(this, contacts);
        PubSubBroker.getInstance().publish(new Message<>("CONTACTS_CHANGED", "ContactActivity", null));
        finish();
    }
}