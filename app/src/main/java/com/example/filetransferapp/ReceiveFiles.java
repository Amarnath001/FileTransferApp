package com.example.filetransferapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
//import java.io.DataOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

import static com.example.filetransferapp.NetworkShare.verifyStoragePermissions;

public class ReceiveFiles extends AppCompatActivity {
    //private TextView statusTextView;
    //private TextView fileNameTextView;
    public int SERVER_PORT = 8080;
    Thread backRun;
    public static String TAG = "Receive Files : ";
    /*private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    */
    //public Socket socket;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
        setContentView(R.layout.activity_recieve_files);
        EditText serverIp = findViewById(R.id.IpServer);
        Button serverConnect = findViewById(R.id.Connect);
        serverConnect.setOnClickListener(v -> {
            Log.v(TAG, "In On create of rec activity");
            // Start a new thread to handle the network connection and file transfer
            //backRun.start();
                ClientRxThread clientRxThread =
                        new ClientRxThread(
                                serverIp.getText().toString(),
                                SERVER_PORT);

                clientRxThread.start();
            });
    }
    private class ClientRxThread extends Thread {
        String dstAddress;
        int dstPort;

        ClientRxThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
            Log.v(TAG, "Add : " + dstAddress + "Port : " + dstPort);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            Log.v(TAG, "In Clientrxthread run method!!");
            Socket socket;
            try {
                socket = new Socket(dstAddress, dstPort);
                if (socket.isConnected()) {
                    ReceiveFiles.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(ReceiveFiles.this,
                                    "Connected to host!!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }
                Log.v(TAG, "Socket : " + socket);
                receive(socket);
                //assert false;
                //long fileSize = dataInputStream.readLong();
                //DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                //String filename = dataInputStream.readUTF();
                //InputStream is = socket.getInputStream();
                //File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "TestSend.pdf");
                /*Log.v(TAG,"File named"+file.getName());
                int total;
                byte[] bytes = new byte[4096];
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                while(true) {
                    int bytesRead = is.read(bytes, 0, bytes.length);
                    if(bytesRead<0)break;
                    bos.write(bytes, 0, bytesRead);
                    ReceiveFiles.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setContentView(R.layout.activity_recieve_files);
                            TextView status = findViewById(R.id.status_text_view);
                            status.setText("The File is being transfered.......");
                        }});
                }
                bos.close();
                socket.close();
                ReceiveFiles.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ReceiveFiles.this,
                                "Finished",
                                Toast.LENGTH_LONG).show();
                    }});
                TextView status = findViewById(R.id.status_text_view);
                status.setText("The File is Transfered Successfully!!!");
            } catch (IOException e) {

                e.printStackTrace();

                final String eMsg = "Something wrong: " + e.getMessage();
                ReceiveFiles.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ReceiveFiles.this,
                                eMsg,
                                Toast.LENGTH_LONG).show();
                    }});

            }*/
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    public void receive(Socket socket){
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//read the number of files from the client
            int number = dis.readInt();
            ArrayList<File>files = new ArrayList<File>(number);
            System.out.println("Number of Files to be received: " +number);
            //read file names, add files to arraylist
            for(int i = 0; i< number;i++){
                File file = new File(dis.readUTF());
                files.add(file);
            }
            int n = 0;
            byte[]buf = new byte[4092];
            //outer loop, executes one for each file
            for(int i = 0; i < files.size();i++){
                int finalI = i;
                ReceiveFiles.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ReceiveFiles.this,
                                "FILE RECEIVING IS : "+files.get(finalI).getName(),
                                Toast.LENGTH_LONG).show();
                    }});
                //System.out.println("Receiving file: " + files.get(i).getName());
                //create a new fileoutputstream for each new file
                String filename = dis.readUTF();
                long fileSize = dis.readLong();
                File in = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),filename);
                FileOutputStream fos = new FileOutputStream(filename);
                //read file
                while (fileSize > 0 && (n = dis.read(buf, 0, (int)Math.min(buf.length, fileSize))) != -1)
                {
                    fos.write(buf,0,n);
                    fileSize -= n;
                }
                fos.close();
            }

        } catch (EOFException ignore) {
            // TODO Auto-generated catch block

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ReceiveFiles.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(ReceiveFiles.this,
                            "FILE RECEIVED!!!!",
                            Toast.LENGTH_LONG).show();
                }});
        }
    }
}
