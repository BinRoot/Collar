package com.binroot.collar;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.provider.ContactsContract;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: binroot
 * Date: 5/12/13
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyContacts {
    private final String DEBUG = "MyContacts";

    private final String DELIMITER = ",,";

    public static final int TOP_BIAS = 0;
    public static final int NO_BIAS = 1;
    public static final int BOTTOM_BIAS = 2;

    private static MyContacts ourInstance = null;
    private ArrayList<Person> contactsList = null;
    private Context context = null;

    public static MyContacts getInstance(Context context) {
        if(ourInstance == null) {
            ourInstance = new MyContacts(context);
        }
        return ourInstance;
    }

    private MyContacts(Context context) {
        this.context = context;

        updateContacts();
    }

    public Person pop(int bias) {
        if(contactsList.isEmpty()) return new Person("NO CONTACT FOUND", "0");

        Person retPerson = new Person(contactsList.get(0));

        contactsList.remove(0);

        if(bias == MyContacts.NO_BIAS) {
            addContactRandomly(contactsList, retPerson);
        }
        else if(bias == MyContacts.BOTTOM_BIAS) {
            addContactBottomBiased(contactsList, retPerson);
        }
        else if(bias == MyContacts.TOP_BIAS) {
            addContactTopBiased(contactsList, retPerson);
        }

        saveStoredContacts(contactsList);

        return retPerson;
    }




    private void updateContacts() {
        ArrayList<Person> freshContactsList = getFreshContacts();
        ArrayList<Person> mergedList = merge(freshContactsList);

        saveStoredContacts(mergedList);
        contactsList = new ArrayList<Person>(mergedList);
    }

    private ArrayList<Person> getFreshContacts() {
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        ArrayList<Person> freshContactsList = new ArrayList<Person>();
        while(!cursor.isAfterLast()) {
            int hasNumberColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

            try {
                String hasNumber = cursor.getString(hasNumberColumnIndex);

                if(hasNumber.equals("1")) {

                    int idColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                    int displayNameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                    String id = cursor.getString(idColumnIndex);
                    String displayName = cursor.getString(displayNameColumnIndex);

                    freshContactsList.add(new Person(displayName, id));

                }
            }
            catch(CursorIndexOutOfBoundsException exception) {}

            cursor.moveToNext();
        }

        return freshContactsList;
    }

    private ArrayList<Person> merge(ArrayList<Person> freshContactsList) {
        ArrayList<Person> staleContactsList = getStoredContacts();
        ArrayList<Person> mergedList = new ArrayList<Person>();

        // list of brand new users
        ArrayList<Person> newPersons = new ArrayList<Person>();
        for(Person freshPerson : freshContactsList) {
            if(!staleContactsList.contains(freshPerson)) {
                newPersons.add(freshPerson);
            }
        }

        // randomly adds the new users
        for(Person newPerson : newPersons) {
            addContactRandomly(staleContactsList, newPerson);
        }

        // ignores stale people
        for(Person stalePerson : staleContactsList) {
            if(freshContactsList.contains(stalePerson)) {
                mergedList.add(stalePerson);
            }
        }


        return mergedList;
    }

    private void addContactRandomly(ArrayList<Person> list, Person p) {
        int rMin = 0;
        int rMax = list.size();
        int pos = rMin + (int)(Math.random() * ((rMax - rMin) + 1));

        list.add(pos, p);
    }

    private void addContactTopBiased(ArrayList<Person> list, Person p) {
        int start = 0;
        int end = list.size();

        while(end-start > 1) {

            int flip = (int)(Math.random()*9); // [0, 9]
            if(flip <= 6) { // favor top/start
                end = end - (end-start)/2;
            }
            else {
                start = start + (end-start)/2;
            }
        }

        list.add(start, p);
    }

    private void addContactBottomBiased(ArrayList<Person> list, Person p) {
        int start = 0;
        int end = list.size();

        while(end-start > 1) {
            int flip = (int)(Math.random()*9); // [0, 9]
            if(flip <= 3) { // favor bottom/end
                end = end - (end-start)/2;
            }
            else {
                start = start + (end-start)/2;
            }
        }

        list.add(end, p);
    }

    private void saveStoredContacts(ArrayList<Person> personList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.CONTACTS_FILE, Context.MODE_PRIVATE);

        StringBuilder sb = new StringBuilder();
        for(int i=0; i<personList.size(); i++) {
            sb.append(personList.get(i).displayName).append(DELIMITER).append(personList.get(i).id);

            if(i!=personList.size()-1) {
                sb.append(DELIMITER);
            }
        }

        sharedPreferences.edit().putString(Constants.CONTACTS_KEY, sb.toString()).commit();
    }

    private ArrayList<Person> getStoredContacts() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.CONTACTS_FILE, Context.MODE_PRIVATE);
        String rawData = sharedPreferences.getString(Constants.CONTACTS_KEY,"");
        String [] rawDataParts = rawData.split(DELIMITER);

        ArrayList<Person> personList = new ArrayList<Person>();
        if(rawDataParts.length>1) {
            for(int i=0; i<rawDataParts.length; i+=2) {
                String displayName = rawDataParts[i];
                String id = rawDataParts[i+1];
                personList.add(new Person(displayName, id));
            }
        }

        return personList;
    }



}
