package com.example.filetransferapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recieve_files);
        EditText serverIp = findViewById(R.id.IpServer);
        Button serverConnect = findViewById(R.id.Connect);
        serverConnect.setOnClickListener(v -> {
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
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                String fileName = dataInputStream.readUTF();
                long fileSize = dataInputStream.readLong();
                File file = new File(
                        Environment.DIRECTORY_DOWNLOADS,
                        fileName);

                byte[] bytes = new byte[4096];
                InputStream is = socket.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                while(true) {
                    int bytesRead = is.read(bytes, 0, bytes.length);
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
                status.setText("The File is Tranfered Successfully!!!");
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

            } finally {
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}