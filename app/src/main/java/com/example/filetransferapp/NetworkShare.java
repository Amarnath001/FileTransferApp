package com.example.filetransferapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.channels.Channel;
import java.util.ArrayList;

public class NetworkShare extends AppCompatActivity {
    String ip;
    TextView Textview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Textview=findViewById(R.id.textView);
        WifiManager manager=(WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        ip= Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());
        Textview.setText("Ip Address: "+ ip);

    }
}

