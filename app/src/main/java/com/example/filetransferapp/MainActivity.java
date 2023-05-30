package com.example.filetransferapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import static com.example.filetransferapp.NetworkShare.verifyStoragePermissions;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w("before","Logcat save");
        try {
            Process process = Runtime.getRuntime().exec("logcat");
            verifyStoragePermissions(MainActivity.this);
            File file = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"MainActivityLog.txt");
            if(!file.exists()) {
                Toast.makeText(MainActivity.this,"Log File does not exist , Creating new one !!",Toast.LENGTH_LONG).show();
                file.createNewFile();
                process = Runtime.getRuntime().exec( "logcat" +(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"MainActivityLog.txt"));
            }
            else{
                process = Runtime.getRuntime().exec( "logcat" +(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"MainActivityLog.txt"));
                Toast.makeText(MainActivity.this,"Log File already exists writing into it!!",Toast.LENGTH_LONG).show();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
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