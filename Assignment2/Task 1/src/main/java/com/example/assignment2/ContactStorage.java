package com.example.assignment2;

import android.content.Context;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ContactStorage {
    private static final String FILE_NAME = "contacts_data.ser";

    public static void saveContacts(Context context, List<Contact> contacts) {
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(contacts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Contact> loadContacts(Context context) {
        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<Contact>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>(); // Return empty list if file doesn't exist
        }
    }
}