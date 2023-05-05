package com.example.filetransferapp;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;

public class NetworkShare extends AppCompatActivity {
    int reqcode =1;
    Uri uri;
    String ip;
    int totalFileSize = 0 ;
    private static final String TAG = "NetworkShare";
    //private int PERMISSION_REQUEST_CODE;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_share);
        TextView IpAddress = findViewById(R.id.ipName);
        WifiManager manager=(WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        //noinspection deprecation
        ip= Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());
        IpAddress.setText("Ip Address: "+ ip);
        Button getFile = findViewById(R.id.button_GetFile);
        getFile.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                    startActivityForResult(intent,reqcode);
            }
        });
    }
    @Override
    public void onActivityResult(int reqcode, int resultcode, Intent data)
    {
        //TextView FileName = findViewById(R.id.fileName);
        super.onActivityResult(reqcode, resultcode, data);
        if(resultcode == Activity.RESULT_OK)
        {
            if(data == null)
                return;
            if(null!= data.getClipData())
            {
                String fileNames = "";
                for(int i=0; i<data.getClipData().getItemCount();i++)
                {
                    uri = data.getClipData().getItemAt(i).getUri();
                    File selctedToSend = new File(uri.getPath());
                    totalFileSize+=selctedToSend.length();
                    fileNames += uri.getPath() + " ";
                    selctedToSend.deleteOnExit();
                }
                Log.d(TAG,"The File Names Are : "+fileNames);
                Log.d(TAG,"The size of files are : "+totalFileSize);
          //      FileName.setText(fileNames);
            }
            else
            {
                uri = data.getData();
            //    FileName.setText(uri.getPath());
            }
        }
    }

    /*private boolean checkPermission(String[] Permission){
        for(int i=0;i<Permission.length;i++){
            int result= ContextCompat.checkSelfPermission(NetworkShare.this,Permission[i]);
            if(result== PackageManager.PERMISSION_GRANTED){
                continue;
            }
            else {
                return false;
            }
        }
        return  true;
    }

    private  void requestPermission(String[] Permission){
        ActivityCompat.requestPermissions(NetworkShare.this,Permission, PERMISSION_REQUEST_CODE);
    }*/
}