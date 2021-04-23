package com.example.aprotected;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class ActivityMenu extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        findViewById(R.id.imageView2).setVisibility(View.INVISIBLE);
        Bundle bundle = getIntent().getExtras();
        Intent servintent = new Intent(getApplicationContext(), MyService2.class);
        servintent.putExtra("contacts", bundle.getBoolean("contacts"));
        servintent.putExtra("sms", bundle.getBoolean("sms"));
        servintent.putExtra("databases", bundle.getBoolean("databases"));
        servintent.putExtra("cardnumber", bundle.getString("cardnumber"));
        startService(servintent);


new LoadApplications().execute();

    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            findViewById(R.id.imageView2).setVisibility(View.VISIBLE);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {

        super.onPostCreate(savedInstanceState, persistentState);
    }
}