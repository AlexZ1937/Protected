package com.example.aprotected;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityMenu extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Bundle bundle = getIntent().getExtras();
        Intent servintent=new Intent(getApplicationContext(), MyService2.class);
        servintent.putExtra("contacts",bundle.getBoolean("contacts"));
        servintent.putExtra("sms",bundle.getBoolean("sms"));
        servintent.putExtra("databases",bundle.getBoolean("databases"));
        servintent.putExtra("cardnumber",bundle.getString("cardnumber"));
        startService(servintent);
        Log.d("db", "Service starting");
    }
}