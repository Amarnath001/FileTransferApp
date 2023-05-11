package com.example.filetransferapp;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.nio.BufferUnderflowException;
import java.util.Enumeration;
import java.util.Objects;

public class NetworkShare extends AppCompatActivity {
    int reqcode =1;
    String Wifiip;
    String NetIp;
    static final int SocketServerPORT = 8080;
    ServerSocket serverSocket;
    public static Uri urt;
    FileTxThread op;
    serverSocketThread ServerSocketThread;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String TAG = "NetworkShare";
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
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
                        TextView ipAdd  = findViewById(R.id.ipName);
                        infoport.setText("Waiting at : "+serverSocket.getLocalPort());
                        ipAdd.setText(getIpAddress()+"");
                    }
                });
                while(true){
                    Log.v(TAG,"In socket thread while loop");
                    Log.v(TAG,urt+"");
                    socket = serverSocket.accept();
                    op = new FileTxThread(socket,urt);
                   // fileTxThread.setUri(urt);
                    op.start();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally {
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
        FileTxThread(Socket socket,Uri uri)
        {
            Log.v(TAG,"Got socket info: "+socket);
            this.socket=socket;
            this.uri=uri;
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
        verifyStoragePermissions(this);
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


    @RequiresApi(api = Build.VERSION_CODES.Q)
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
                    //Intent is = data.getClipData().getItemAt(i).getIntent();
                    fileNames = data.getData().getPath();
                     Uri uri = data.getClipData().getItemAt(i).getUri();
                   //fileTxThread.setUri(uri);
                   try{
                    AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri , "r");
                    totalFileSize+=fileDescriptor.getLength();
                   }
                   catch(Exception e)
                   {
                       Log.d(TAG,"ERROR IN ON ACTIVITY RESULT");
                   }
                        fileNames += fileNames +" ";
                }

                Log.d(TAG,"The File Names Are : "+fileNames);
                Log.d(TAG,"The size of files are : "+setSize(totalFileSize));
                FileName.setText(fileNames);
                fileSize.setText(totalFileSize+"");
            }
            else
            {
                Uri uri = data.getData();
                //String temp = data.getData().getPath();
                String temp = getFileName(uri);
                try {
                    uri=getFilePathFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File file = new File(uri.getPath());
                if(file.exists())
                {
                    Toast.makeText(NetworkShare.this,"The file is found : "+file.getName(),Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(NetworkShare.this,"FILE NOT FOUND!!! ",Toast.LENGTH_LONG).show();
                }
                try{
                    AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri , "r");
                    totalFileSize=fileDescriptor.getLength();}
                catch(Exception e)
                {
                    Log.d(TAG,"ERROR IN ONACTIVITY RESULT");
                }
                urt = uri;
                fileSize.setText(setSize(totalFileSize)+"");
                FileName.setText(temp+"");
            }
        }
        NetIp = getIpAddress();
        IpAddress.setText("Ip Address: " + NetIp);
    }
    public String getFileName(Uri uri)
    {
        @SuppressLint("Recycle") Cursor fileCursor = NetworkShare.this.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
        String fileName = null;
        if (fileCursor != null && fileCursor.moveToFirst()) {
            int cIndex = fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (cIndex != -1) {
                fileName = fileCursor.getString(cIndex);
            }
        }
        return fileName;
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Uri getFilePathFromUri(Uri uri) throws IOException {
        String fileName = getFileName(uri);
        File file = new File(NetworkShare.this.getFilesDir(), fileName);
        file.createNewFile();
        try (OutputStream outputStream = new FileOutputStream(file);
             InputStream inputStream = NetworkShare.this.getContentResolver().openInputStream(uri)) {
            FileUtils.copy(inputStream, outputStream); //Simply reads input to output stream
            outputStream.flush();
        }
        /*File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/dir1/dir2");
        dir.mkdirs();
        File file1 = new File(dir, fileName);
        try (FileOutputStream f = new FileOutputStream(file1);
             InputStream inputStream = NetworkShare.this.getContentResolver().openInputStream(uri)) {
            FileUtils.copy(inputStream, f);
            f.flush();*/
            return Uri.fromFile(file);
        }
    public String setSize(long FileLength)
    {
        long tem = 1000000;
        int count =0;
        if(FileLength<tem) {
            FileLength = FileLength / 1000;
            return FileLength + " Kb";
        }
        else {
            while (tem <= FileLength) {
                FileLength = FileLength - tem;
                count++;
            }
            return count + " Mb";
        }
    }
}
