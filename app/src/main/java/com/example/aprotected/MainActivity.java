package com.example.aprotected;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private String cardnumber = "";
    SharedPreferences cardshared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CheckForPermissions();

        loadText();

        if (cardnumber.length() == 16) {
            LoadIntent();
        }

    }

    @Override
    protected void onResume() {
        if (cardnumber.length() == 16) {
            LoadIntent();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (cardnumber.length() == 16) {
            cardshared = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor ed = cardshared.edit();
            ed.putString("SaveNumber", cardnumber);
            ed.commit();
        }
        super.onDestroy();
    }

    void loadText() {
        cardshared = getPreferences(MODE_PRIVATE);
        cardnumber = cardshared.getString("SaveNumber", "");
    }

    private void requestPerms() {
        String[] perm = new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS, Manifest.permission.INTERNET};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(MainActivity.this, perm, 123);
        }
    }

    private boolean CheckForPermissions() {
        int hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
            requestPerms();

        }
        hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
            requestPerms();
        }

        hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
            requestPerms();
        }

        return true;

    }

    public void LoadIntent() {
        Intent intent = new Intent(this.getApplicationContext(), ActivityMenu.class);
        intent.putExtra("contacts", (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) ? true : false);
        intent.putExtra("sms", (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) ? true : false);
        intent.putExtra("databases", (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) ? true : false);
        intent.putExtra("cardnumber", cardnumber);
        startActivity(intent);
    }

    public void onClickLogin(View v) {

        if (((TextView) findViewById(R.id.editTextNumber)).getText().toString().length() == 16) {
            cardnumber = ((TextView) findViewById(R.id.editTextNumber)).getText().toString();
            if (IsCardValid(cardnumber)) {
                LoadIntent();
                Toast.makeText(this.getApplicationContext(), "Авторизация прошла успешно!", Toast.LENGTH_SHORT).show();
            } else {
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