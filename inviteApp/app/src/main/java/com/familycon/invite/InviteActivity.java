package com.familycon.invite;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.widget.LinearLayout.VERTICAL;

public class InviteActivity extends AppCompatActivity {

    RecyclerView                    mRecyclerView;
    private List<ContactModel>      mContactModelList = new ArrayList<>();
    ContactAdapter                  mContactAdapter;

    String                          mSearchKey = "";
    boolean                         mPermGranted = false;

    private static String[]         PERMISSION_CONTACT = {Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS};
    private static final int        REQUEST_CONTACT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        mRecyclerView = findViewById(R.id.rv_contacts);

        EditText editSearch = (EditText)findViewById(R.id.search_string);
        editSearch.setOnEditorActionListener(new EditText.OnEditorActionListener(){

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mSearchKey = ((EditText)v).getText().toString();

                getContactInfo();
                return false;
            }
        });

        setDataToAdapter();
        requestContactsPermissions();

    }

    private void setDataToAdapter(){
        mContactAdapter = new ContactAdapter(this, mContactModelList);
        initRecyclerView();
    }

    private void initRecyclerView(){
        Log.e("+++++++++", "+++++++++++++++");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, VERTICAL);
        mRecyclerView.addItemDecoration(itemDecor);
        mRecyclerView.setAdapter(mContactAdapter);
    }

    private void getContactInfo(){
        mContactAdapter.removeAll();
        Log.e("+++++++++", "============");
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PHONE_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String PHONE_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI,null,null,null,DISPLAY_NAME);

        if (cursor.getCount() > 0){
            while (cursor.moveToNext()){
                String CONTACT_ID = cursor.getString(cursor.getColumnIndex(ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                mSearchKey.trim();
                if (mSearchKey.length() > 0) {
                    if (!name.toLowerCase().contains(mSearchKey.toLowerCase()))
                        continue;
                }

                int hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(HAS_PHONE_NUMBER));
                ContactModel contactModel = new ContactModel();
                if (hasPhoneNumber > 0){
                    contactModel.setName(name);

                    Cursor phoneCursor = contentResolver.query(PHONE_URI, new String[]{NUMBER},PHONE_ID+" = ?",new String[]{CONTACT_ID},null);
                    List<String> contactList = new ArrayList<>();
                    phoneCursor.moveToFirst();
                    while (!phoneCursor.isAfterLast()){
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER)).replace(" ","");
                        contactList.add(phoneNumber);
                        phoneCursor.moveToNext();
                    }
                    contactModel.setNumber(contactList);
                    mContactModelList.add(contactModel);
                    phoneCursor.close();
                }
            }
            mContactAdapter.notifyDataSetChanged();
        }
    }

    public void requestContactsPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){

                Snackbar.make(mRecyclerView, "permission Contact", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(InviteActivity.this,PERMISSION_CONTACT,REQUEST_CONTACT);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(InviteActivity.this,PERMISSION_CONTACT,REQUEST_CONTACT);
            }
        } else {
            getContactInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults){
            if (result == PackageManager.PERMISSION_GRANTED){
                getContactInfo();
            }
        }
    }

    public void setInviteSMS(String strPhoneNumber) {
        String message = this.getString(R.string.invite_msg) + "&hl=en";

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        final InviteActivity that = this;
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(that, "Invitation SMS sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(that, "Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(that, "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(that, "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(that, "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(that, "Invitation SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(that, "Invitation SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(strPhoneNumber, null, message, sentPI, deliveredPI);

    }
}
