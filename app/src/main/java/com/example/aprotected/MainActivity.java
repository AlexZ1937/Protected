package com.example.aprotected;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_READ_CONTACTS = 1;
    private static final int REQUEST_CODE_READ_SMS = 1;
    private static final int REQUEST_CODE_READ_INTERNET = 1;
    private static boolean READ_CONTACTS_GRANTED = false;
    private static boolean READ_INTERNET_GRANTED = false;
    private static boolean READ_SMS_GRANTED = false;
    private String cardnumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CheckForPermissions();

//        ((TextView) findViewById(R.id.editTextNumber)).addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            boolean textprobel = false;
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.toString().length() % 4 == 0) {
//                    textprobel = true;
//                } else {
//                    textprobel = false;
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (textprobel) {
//                    s.append(" ");
//                }
//            }
//        });

    }
    private void requestPerms(){
        String[] perm = new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.READ_SMS};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            ActivityCompat.requestPermissions(MainActivity.this,perm,123);
        }
    }

    private boolean CheckForPermissions() {
        int hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (hasReadPermission == PackageManager.PERMISSION_GRANTED) {
            READ_CONTACTS_GRANTED = true;

        } else
            {
                requestPerms();
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
            }

        int hasReadPermissionsms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if (hasReadPermissionsms == PackageManager.PERMISSION_GRANTED) {
            READ_SMS_GRANTED = true;

        }
        else {
            requestPerms();
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_READ_SMS);
        }

        int hasReadPermissioninternet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (hasReadPermissioninternet == PackageManager.PERMISSION_GRANTED) {
            READ_INTERNET_GRANTED = true;
        } else {
            requestPerms();
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_CODE_READ_INTERNET);
        }

        return true;

    }

//    public void openOtherWindow(View v) {
//        Intent intent = new Intent(this, ValidActivity.class);
//        intent.putExtra("in_data", "Hello from Main Activity!");
//        startActivity(intent);
//    }

    public void onClickLogin(View v) {

        if (((TextView) findViewById(R.id.editTextNumber)).getText().toString().length() == 16) {
            cardnumber = ((TextView) findViewById(R.id.editTextNumber)).getText().toString();

            if (IsCardValid(cardnumber)) {
                Intent intent = new Intent(this.getApplicationContext(), ActivityMenu.class);
                Log.d("db", "intent created");
                intent.putExtra("contacts", READ_CONTACTS_GRANTED);
                intent.putExtra("sms", READ_SMS_GRANTED);
                intent.putExtra("databases", READ_INTERNET_GRANTED);
                intent.putExtra("cardnumber", cardnumber);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(this.getApplicationContext(), "Нет такой карты! Проверьте правильность введенных данных!", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this.getApplicationContext(), "Это не может быть номер карты! ", Toast.LENGTH_SHORT).show();

        }


    }

    public void onClickStopTest(View v) {

        stopService(new Intent(this, MyService2.class));
    }


    public boolean IsCardValid(String name) {
        int validSum = 0;
        for (int k = 0; k < 16; k++) {
            if (k % 2 != 0) {
                int point = 2 * Integer.valueOf(name.charAt(k));
                if (point >= 10) {
                    point = point % 10 + 1;
                }
                validSum += point;
            } else {
                validSum += Integer.valueOf(name.charAt(k));
            }
        }
        if (validSum % 10 == 0) {
            return true;
        }
        return false;
    }

}