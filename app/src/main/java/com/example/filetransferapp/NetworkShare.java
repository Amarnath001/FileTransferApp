package com.example.filetransferapp;

import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.TextView;

public class NetworkShare extends AppCompatActivity {
    String ip;
    TextView Textview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_share);
        Textview=findViewById(R.id.textView);
        WifiManager manager=(WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        ip= Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());
        Textview.setText("Ip Address: "+ ip);
    }
}