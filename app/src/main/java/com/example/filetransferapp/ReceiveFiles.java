package com.example.filetransferapp;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
public class ReceiveFiles extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    String[] fileMd5;
    ArrayList<File>files;
    ServerSocket ss;
    Rectransfer rectransfer;
    Process process;
    public static final int SERVER_PORT = 8080;
    int status =0;
    public static String TAG = "Receive Files : ";
    private static final String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(ReceiveFiles.this);
        setContentView(R.layout.activity_recieve_files);
        TextView serverIp = findViewById(R.id.IpServer);
        serverIp.setText("Please enter this IP at server to send file : "+NetworkShare.getIpAddress());
        Button serverConnect = findViewById(R.id.Connect);
        Button integrityCheck = findViewById(R.id.Integrity);
        Log.w("before","Logcat save");
        try {
            boolean st;
            process = Runtime.getRuntime().exec("logcat");
            verifyStoragePermissions(this);
            File file = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"RecFileLog.txt");
            if(!file.exists()) {
                st = file.createNewFile();
                Toast.makeText(ReceiveFiles.this,"Log File does not exist , Creating new one !!"+st,Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(ReceiveFiles.this,"Log File already exists writing into it!!",Toast.LENGTH_LONG).show();
            }
            process = Runtime.getRuntime().exec( "logcat -f " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +"RecFileLog.txt");
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        serverConnect.setOnClickListener(v -> {
            Log.v(TAG, "In On create of rec activity");
            Toast.makeText(ReceiveFiles.this,
                    "WAITING FOR HOST!!!!",
                    Toast.LENGTH_LONG).show();
                ClientRxThread clientRxThread = new ClientRxThread(SERVER_PORT);
                clientRxThread.start();
            });
        integrityCheck.setOnClickListener(v->{
            for(int i=0;i<files.size();i++)
            {
                try {
                    if(fileMd5[i].equals(md5File(files.get(i))))
                    {
                        Toast.makeText(ReceiveFiles.this,
                                        "FILE INTEGRITY OF : "+files.get(i)+" IS VERIFIED !!!!",
                                        Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(ReceiveFiles.this,
                                        "FILE : "+files.get(i)+" IS CORRUPT!!!!",
                                        Toast.LENGTH_LONG).show();
                    }
                    System.out.println(Arrays.toString(fileMd5));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void verifyStoragePermissions(Activity activity) {
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

    private class ClientRxThread extends Thread {
        int dstPort;
        ClientRxThread(int port) {
            //dstAddress = address;
            dstPort = port;
            Log.v(TAG,"Port : " + dstPort);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            Log.v(TAG, "In Clientrxthread run method!!");
            Socket socket;
            try {
                while(true){
                    Log.v(TAG,"Inside while loop of ClientRX thread");
                    ss = new ServerSocket(dstPort);
                    Log.v(TAG,""+ss);
                    socket = ss.accept();
                    Log.v(TAG,""+socket);
                    if(socket.isConnected()){
                        ReceiveFiles.this.runOnUiThread(() -> Toast.makeText(ReceiveFiles.this,
                                "Connected to host!!",
                                Toast.LENGTH_LONG).show());
                        break;
                    }
                }
                rectransfer = new Rectransfer(socket);
                rectransfer.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    private class Rectransfer extends Thread {
        Socket socket;
        public Rectransfer(Socket socket){
            this.socket=socket;
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            receive(socket);
        }
    }
    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void receive(Socket socket){
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            verifyStoragePermissions(this);
            int stat;
            int number = dis.readInt();
            int[] ign =new int[number];
            fileMd5 = new String[number];
            for(int j = 0 ;j<number;j++)
            {
                ign[j]=1;
            }
            files = new ArrayList<>(number);
            long[] FileSize = new long[number];
            System.out.println("Number of Files to be received: " +number);
            for(int i = 0; i< number;i++){
                File file = new File(dis.readUTF());
                files.add(file);
            }
            System.out.println("FILE LIST IN CLIENT SIDE : " +files);
            for(int i=0;i<number;i++)
            {

                FileSize[i]= dis.readLong();

            }
            for(int i=0;i<number;i++)
            {
                int length=dis.readInt();
                byte[] data=new byte[length];
                dis.readFully(data);
                String str=new String(data,"UTF-8");
                fileMd5[i]= str;
                Log.v(TAG,"MD5 vlaue of file from Server Side :"+fileMd5[i]);
                System.out.println("MD5 : "+fileMd5[i]);
            }
            System.out.println("Files MD value from Server side are : "+ Arrays.toString(fileMd5));
            System.out.println("FILE LIST (SIZES) IN CLIENT SIDE : " + Arrays.toString(FileSize));
            int n;
            byte[]buf = new byte[4092];
            for(int i = 0; i < files.size();i++){
                    int finalI = i;
                    System.out.println("Receiving file: " + files.get(i).getName());
                    File in = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), files.get(i).getName());
                    if (in.exists()) {
                        ReceiveFiles.this.runOnUiThread(() -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveFiles.this);
                            // Set the message show for the Alert time
                            builder.setMessage("FILE ALREADY EXISTS!!! (DO YOU WANT TO CONTINUE?) -------- FILE IS : "+in.getName());
                            // Set Alert Title
                            builder.setTitle("Alert !");
                            // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                            builder.setCancelable(false);
                            // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                // When the user click yes button then app will close
                                status =1;
                                dialog.cancel();
                            });
                            // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
                            builder.setNegativeButton("No", (dialog, which) -> {
                                // If user click no then dialog box is canceled.
                                status=-1;
                                dialog.cancel();
                            });

                            // Create the Alert dialog
                            AlertDialog alertDialog = builder.create();
                            // Show the Alert Dialog box
                            alertDialog.show();
                        });
                    } else {
                        ReceiveFiles.this.runOnUiThread(() -> Toast.makeText(ReceiveFiles.this,
                                "FILE DOES NOT EXIST , Creating New File!!",
                                Toast.LENGTH_LONG).show());
                        in.createNewFile();
                    }
                    if(status!=-1){
                    ReceiveFiles.this.runOnUiThread(() -> Toast.makeText(ReceiveFiles.this,
                            "FILE RECEIVING IS : " + files.get(finalI).getName(),
                            Toast.LENGTH_LONG).show());}
                    if(status!=-1) {
                        FileOutputStream fos = new FileOutputStream(in);
                        //read file
                        while (FileSize[i] >= 0 && (n = dis.read(buf, 0, (int) Math.min(buf.length, FileSize[i]))) != -1) {
                            fos.write(buf, 0, n);
                            FileSize[i] -= n;
                            ReceiveFiles.this.runOnUiThread(() -> {
                                TextView fileName = findViewById(R.id.file_name_text_view);
                                fileName.setText("File currently transferring is : "+in.getName());
                            });
                            if (FileSize[i] == 0 || FileSize[i] < 0)
                                break;
                        }
                        if(md5File(in).equals(fileMd5[i]))
                        {
                                stat = 1;
                        }
                        else
                        {
                            stat = -1;
                        }
                        dos.writeInt(stat);
                        dos.flush();
                        fos.close();
                    }
                }
        } catch (EOFException ignore) {
            // TODO Auto-generated catch block

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ReceiveFiles.this.runOnUiThread(() -> Toast.makeText(ReceiveFiles.this,
                    "OPERATION COMPLETED SUCCESSFULLY",
                    Toast.LENGTH_LONG).show());
            try{socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();}catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String md5File(File file) throws IOException {
        String Fname=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+file.getName();
        byte[] data = Files.readAllBytes(Paths.get(Fname));
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        String checksum = new BigInteger(1, hash).toString(16);
        System.out.println("FILE NAME : "+file.getName()+" MD5 : "+checksum);
        return checksum;
    }
}