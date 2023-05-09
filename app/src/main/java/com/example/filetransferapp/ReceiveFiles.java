package com.example.filetransferapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
//import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReceiveFiles extends AppCompatActivity {
    private TextView statusTextView;
    private TextView fileNameTextView;
    public int SERVER_PORT = 8080;
    //  Thread backRun;
    public static String TAG = "Receive Files : ";
    //public Socket socket;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recieve_files);
        EditText serverIp = findViewById(R.id.IpServer);
        Button serverConnect = findViewById(R.id.Connect);
        serverConnect.setOnClickListener(v -> {
            Log.v(TAG,"In On create of rec activity");
            // Start a new thread to handle the network connection and file transfer
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
            Log.v(TAG,"Add : "+dstAddress+"Port : "+dstPort);
        }
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            Log.v(TAG,"In Clientrxthread run method!!");
            Socket socket;
            try {
                socket = new Socket(dstAddress,dstPort);
                if(socket.isConnected())
                {
                    ReceiveFiles.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(ReceiveFiles.this,
                                    "Connected to host!!",
                                    Toast.LENGTH_LONG).show();
                        }});

                }
                Log.v(TAG,"Socket : "+socket);
                //assert false;
                //long fileSize = dataInputStream.readLong();
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                String filename = dataInputStream.readUTF();
                File file = new File(
                        Environment.DIRECTORY_DOWNLOADS,
                        filename);
                Log.v(TAG,"File named"+file.getName());
                byte[] bytes = new byte[4096];
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                while(true) {
                    int bytesRead = dataInputStream.read(bytes, 0, bytes.length);
                    if(bytesRead<0)break;
                    bos.write(bytes, 0, bytesRead);
                    setContentView(R.layout.activity_recieve_files);
                    TextView status = findViewById(R.id.status_text_view);
                    status.setText("The File is being transfered.......");
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

            }
        }
    }
}
