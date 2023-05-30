package com.example.filetransferapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendViaNetwork= findViewById(R.id.NetorkSend);
        Button recFiles =findViewById(R.id.RecFiles);

        sendViaNetwork.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this,NetworkShare.class);
            startActivity(intent);
        });
        recFiles.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this,ReceiveFiles.class);
            startActivity(intent);
        });

    }
}