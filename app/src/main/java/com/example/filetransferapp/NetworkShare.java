package com.example.filetransferapp;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

public class NetworkShare extends AppCompatActivity {
    int reqcode =1;
    String Wifiip;
    String NetIp;
    String Md5file;
    String dstAddress;
    static final int SocketServerPORT = 8080;
    //ServerSocket serverSocket;
    public static Uri urt;
    FileTxThread op;
    //serverSocketThread ServerSocketThread;
    ipTransfer IpTransferThread;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public ArrayList<File> FileList = new ArrayList<>();
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
       // ServerSocketThread = new serverSocketThread();
        Log.v(TAG,"In sendfile button");
        EditText sendIp = findViewById(R.id.IpSend);
        IpTransferThread = new ipTransfer();
        dstAddress = sendIp.getText().toString();
        long totalSendSize=0;
        for(int i=0;i<FileList.size();i++)
        {
            totalSendSize = FileList.get(i).length();
        }
        if(totalSendSize>200000)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(NetworkShare.this);
            // Set the message show for the Alert time
            builder.setMessage("FILE SIZE LIMIT EXCEEDED, DO YOU WANT TO CONTINUE?");
            // Set Alert Title
            builder.setTitle("Alert !");
            // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
            builder.setCancelable(false);
            // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
            builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                // When the user click yes button then app will close
                if(!IpTransferThread.isAlive())
                    IpTransferThread.start();
                else
                    Toast.makeText(NetworkShare.this,"The IP share thread is already running!!",Toast.LENGTH_LONG).show();
            });
            // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
            builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                // If user click no then dialog box is canceled.
                dialog.cancel();
            });
            // Create the Alert dialog
            AlertDialog alertDialog = builder.create();
            // Show the Alert Dialog box
            alertDialog.show();
        }
        else {
            if (!IpTransferThread.isAlive())
                IpTransferThread.start();
            else
                Toast.makeText(NetworkShare.this, "The IP share thread is already running!!", Toast.LENGTH_LONG).show();
            //ServerSocketThread.start();}
        }
    }
    public class ipTransfer extends Thread {
        @Override
        public void run() {
            Socket socket = null;
            try {
                socket = new Socket(dstAddress,SocketServerPORT);

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Log.v(TAG, "In IPtransfer thread run!!!");
                //serverSocket = new ServerSocket(SocketServerPORT);
                NetworkShare.this.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        setContentView(R.layout.activity_network_share);
                        TextView infoport = findViewById(R.id.infoport);
                        TextView ipAdd = findViewById(R.id.ipName);
                        infoport.setText("Waiting at : " + SocketServerPORT);
                        ipAdd.setText(getIpAddress() + "");
                        System.out.println(dstAddress);
                    }
                });
                if(dstAddress==null)
                {
                    NetworkShare.this.runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            Toast.makeText(NetworkShare.this,"PLEASE ENTER IP ADDRESS OF CLIENT TO PROCCEED!!",Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else{
                Log.v(TAG,urt+"");
                    assert socket != null;
                    if(socket.isConnected()){
                        NetworkShare.this.runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                Toast.makeText(NetworkShare.this,"Connected to host!!",Toast.LENGTH_LONG).show();
                            }
                        });
                        op = new FileTxThread(socket,FileList);
                        op.start();}
                    else{
                        NetworkShare.this.runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                Toast.makeText(NetworkShare.this,"Socket is not connecting to host!!, Socket is null!!!",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            } finally {
                Log.v(TAG,"In IpTransfer thread finally section");
                assert socket != null;
                if(socket.isClosed()){
                NetworkShare.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recreate();
                        }
                    });
                }
            }
        }
    }
    //private int PERMISSION_REQUEST_CODE;
    public class FileTxThread extends Thread {
        Uri uri;
        Socket socket;
        ArrayList<File> ftlist = new ArrayList<>();
        FileTxThread(Socket socket,Uri uri)
        {
            Log.v(TAG,"Got socket info: "+socket);
            this.socket=socket;
            this.uri=uri;
        }
        FileTxThread(Socket socket,ArrayList<File> ft)
        {
            this.socket=socket;
            ftlist.addAll(ft);
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            Log.v(TAG,"In FileTXthread run!!!");
            send(ftlist,socket);
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
        TextView MD5 = findViewById(R.id.md5);
        long totalFileSize=0;
        super.onActivityResult(reqcode, resultcode, data);
        if(reqcode==reqcode && resultcode == Activity.RESULT_OK)
        {
            String fileNames = "";
            if(data == null)
                return;
            if(null!= data.getClipData())
            {
                FileList.clear();
                for(int i=0; i<data.getClipData().getItemCount();i++) {
                    //Intent is = data.getClipData().getItemAt(i).getIntent();
                    //fileNames = data.getData().getPath();
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    fileNames += getFileName(uri) + ",  ";
                    try {
                        uri = getFilePathFromUri(uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri, "r");
                        totalFileSize += fileDescriptor.getLength();
                    }
                    //System.out.println("FILENAMES ARE : "+fileNames);
                    //fileTxThread.setUri(uri);
                    catch (Exception e) {
                        Log.d(TAG, "ERROR IN ON ACTIVITY RESULT");
                    }
                    urt = uri;
                    //uri = getFilePathFromUri(uri);
                    File file = new File(urt.getPath());
                    FileList.add(file);
                    try {
                        Md5file += md5File(file)+" , ";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.v(TAG, "" + FileList);
                    if (file.exists()) {
                        Toast.makeText(NetworkShare.this, "The file is found : " + file.getName(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(NetworkShare.this, "FILE NOT FOUND!!! ", Toast.LENGTH_LONG).show();
                    }
                }
                System.out.println(FileList);
                Log.d(TAG, "The File Names Are : " + fileNames);
                Log.d(TAG, "The size of files are : " + setSize(totalFileSize));
                FileName.setText("The File Selected Are : " + fileNames);
                fileSize.setText(setSize(totalFileSize) + "");
                IpAddress.setText(getIpAddress());
                MD5.setText("MD5 Values are : "+Md5file);
            }
            else
            {
                FileList.clear();
                Uri uri = data.getData();
                //String temp = data.getData().getPath();
                String temp = getFileName(uri);
                try {
                    uri=getFilePathFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try{
                    AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri , "r");
                    totalFileSize=fileDescriptor.getLength();}
                catch(Exception e)
                {
                    Log.d(TAG,"ERROR IN ONACTIVITY RESULT");
                }
                urt = uri;
                //fileSize.setText(setSize(totalFileSize)+"");
                FileName.setText(temp+"");
                File file = new File(uri.getPath());
                FileList.add(file);
                Log.v(TAG,""+FileList);
                try {
                    Md5file = md5File(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(file.exists())
                {
                    Toast.makeText(NetworkShare.this,"The file is found : "+file.getName(),Toast.LENGTH_LONG).show();
                }
                else
                    {
                        Toast.makeText(NetworkShare.this,"FILE NOT FOUND!!! ",Toast.LENGTH_LONG).show();
                    }
                NetIp = getIpAddress();
                fileSize.setText(setSize(totalFileSize)+"");
                IpAddress.setText("Ip Address: " + NetIp);
                MD5.setText("MD5 Values are : "+Md5file);
            }
        }
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String md5File(File file) throws IOException {
        String Fname=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+file.getName();
        byte[] data = Files.readAllBytes(Paths.get(Fname));
        byte[] hash = new byte[0];
        try {
            hash = MessageDigest.getInstance("MD5").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        String checksum = new BigInteger(1, hash).toString(16);
        System.out.println("FILE NAME : "+file.getName()+" MD5 : "+checksum);
        return checksum;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void send(ArrayList<File> files, Socket socket){
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            //System.out.println(files.size());
            //write the number of files to the server
            dos.writeInt(files.size());
            Log.d(TAG,"File Size (dos.writeLong) : "+ files.size());
            dos.flush();
            //write file names
            for(int i = 0 ; i < files.size();i++){
                dos.writeUTF(files.get(i).getName());
                dos.flush();
            }
            //write file sizes
            for(int i=0;i< files.size();i++)
            {
                dos.writeLong(files.get(i).length());
                System.out.println("File SIZE is : "+files.get(i).length());
                dos.flush();
            }
            for(int i=0;i< files.size();i++)
            {
                String secCheck = md5File(files.get(i));
                byte[] data=secCheck.getBytes("UTF-8");
                dos.writeInt(data.length);
                dos.write(data);
                dos.flush();
            }
            //buffer for file writing, to declare inside or outside loop?
            int n;
            byte[]buf = new byte[4092];
            //outer loop, executes one for each file
            for(int i =0; i < files.size(); i++){
                System.out.println(files.get(i).length());
                System.out.println(files.get(i).getName());
                //create new fileinputstream for each file
                FileInputStream fis = new FileInputStream(files.get(i));
                //write file to dos
                while((n =fis.read(buf)) != -1){
                    dos.write(buf,0,n);
                    dos.flush();

                }
                int status;
                status = dis.readInt();
                if(status == 1)
                {
                    int finalI = i;
                    NetworkShare.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NetworkShare.this,
                                    "FILE WAS TRANSFERRED SUCCESSFULLY: "+files.get(finalI).getName(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                if(status == -1)
                {
                    int finalI1 = i;
                    NetworkShare.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NetworkShare.this,
                                    "FILE WAS CORRUPTED DURING TRANSFER PLEASE TRY AGAIN LATER: "+files.get(finalI1).getName(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                //should i close the dataoutputstream here and make a new one each time?
            }
            //or is this good?
            dos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            try{
                socket.shutdownInput();
                socket.shutdownOutput();
            socket.close();}
            catch (IOException ignore){}
        }
    }
}
