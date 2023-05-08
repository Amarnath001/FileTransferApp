package com.example.filetransferapp;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.BufferUnderflowException;
import java.util.Enumeration;

public class NetworkShare extends AppCompatActivity {
    int reqcode =1;
    String Wifiip;
    String NetIp;
    static final int SocketServerPORT = 8080;
    ServerSocket serverSocket;
    FileTxThread fileTxThread;
    serverSocketThread ServerSocketThread;
    //ServerSocketThread serverSocketThread;
    private static final String TAG = "NetworkShare";

    public void SendFile(View view) {
        ServerSocketThread = new serverSocketThread();
        Log.v(TAG,"In sendfile button");
        ServerSocketThread.start();

    }

    //private int PERMISSION_REQUEST_CODE;
    public class serverSocketThread extends Thread {

        public void run() {
            Socket socket = null;
            try{
                Log.v(TAG,"IN socket thread run!!!");
                serverSocket = new ServerSocket(SocketServerPORT);
                NetworkShare.this.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        setContentView(R.layout.activity_network_share);
                        TextView infoport = findViewById(R.id.infoport);
                        infoport.setText("Waiting at : "+serverSocket.getLocalPort());
                    }
                });
                while(true){
                    Log.v(TAG,"In socket thread while loop");
                    socket = serverSocket.accept();
                    fileTxThread = new FileTxThread(socket);
                    fileTxThread.start();
                }
            }catch (IOException e)
            {
                e.printStackTrace();
            }finally {
                Log.v(TAG,"In socket thread finally section");
                if(socket!=null)
                {
                    try {
                        socket.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public class FileTxThread extends Thread{
        Uri uri;
        Socket socket;
        FileTxThread(Socket socket)
        {
            Log.v(TAG,"Got socket info: "+socket);
            this.socket=socket;
        }

        public void setUri(Uri uri) {
            Log.v(TAG,"URI is : "+uri.getPath());
            this.uri = uri;
        }

        @Override
        public void run() {
            Log.v(TAG,"In FileTXthread run!!!");
            File file = new File(uri.getPath());
            Log.v(TAG,"File is : "+file);
            byte[] bytes = new byte[(int)file.length()];
            BufferedInputStream bis;
            try {
                bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(bytes,0,bytes.length);
                OutputStream os = socket.getOutputStream();
                os.write(bytes,0,bytes.length);
                os.flush();
                socket.close();

                final String sentMsg = "File Sent to : " + socket.getInetAddress();
                NetworkShare.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NetworkShare.this,sentMsg,Toast.LENGTH_LONG).show();
                    }
                });
            }catch (IOException e)
            {
                e.printStackTrace();
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_share);
        TextView IpAddress = findViewById(R.id.ipName);
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        //noinspection deprecation
        Wifiip = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());
        if (!Wifiip.equals("0.0.0.0"))
            IpAddress.setText("Ip Address: " + Wifiip);
        else {
            NetIp = getIpAddress();
            IpAddress.setText("Ip Address: " + NetIp);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static String getIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public void getFile(View v)
    {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //noinspection deprecation
        startActivityForResult(intent, reqcode);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int reqcode, int resultcode, Intent data)
    {
        setContentView(R.layout.activity_network_share);
        TextView FileName= findViewById(R.id.fileName);
        TextView fileSize = findViewById(R.id.infoport);
        TextView IpAddress = findViewById(R.id.ipName);
        long totalFileSize=0;
        super.onActivityResult(reqcode, resultcode, data);
        if(reqcode==reqcode && resultcode == Activity.RESULT_OK)
        {
            if(data == null)
                return;
            if(null!= data.getClipData())
            {
                String fileNames = "";
                for(int i=0; i<data.getClipData().getItemCount();i++)
                {
                   Uri uri = data.getClipData().getItemAt(i).getUri();
                   fileTxThread.setUri(uri);
                   try{
                    AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri , "r");
                    totalFileSize+=fileDescriptor.getLength();
                   }
                   catch(Exception e)
                   {
                       Log.d(TAG,"ERROR IN ONACTIVITY RESULT");
                   }
                    fileNames += uri.getPath() + " ";
                }
                Log.d(TAG,"The File Names Are : "+fileNames);
                Log.d(TAG,"The size of files are : "+totalFileSize);
                FileName.setText(fileNames);
                fileSize.setText(totalFileSize+"");
            }
            else
            {
                Uri uri = data.getData();
                try{
                    AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri , "r");
                    totalFileSize=fileDescriptor.getLength();}
                catch(Exception e)
                {
                    Log.d(TAG,"ERROR IN ONACTIVITY RESULT");
                }
                fileSize.setText(totalFileSize+"");
                FileName.setText(uri.getPath());
            }
        }
        NetIp = getIpAddress();
        IpAddress.setText("Ip Address: " + NetIp);
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
