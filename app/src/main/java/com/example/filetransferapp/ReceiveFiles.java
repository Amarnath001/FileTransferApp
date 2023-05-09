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
    Thread backRun;
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
            Log.v(TAG, "In On create of rec activity");
            // Start a new thread to handle the network connection and file transfer
            backRun = new Thread(() -> {
                try {
                    String address = serverIp.getText().toString();
                    // Connect to the server
                    Socket socket = new Socket(address, SERVER_PORT); // The server's IP address
                    // The server's port number
                    // Create a new input stream to receive data from the server
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                    String fileName = dataInputStream.readUTF();   /*Read the file name
                                                           and size from the data input stream */
                    long fileSize = dataInputStream.readLong();

                    runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            Toast.makeText(ReceiveFiles.this, "Connected to Server!!", Toast.LENGTH_LONG).show();
                            statusTextView.setText("Receiving file...");
                            fileNameTextView.setText(fileName);
                        }
                    });

                    byte[] buffer = new byte[4096]; // Create a buffer to hold the file data
                    int bytesRead;
                    int totalBytesRead = 0;
                    File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);/*Creates a new file object to represent the output file
                                                                                                                                    where the received data will be stored*/
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile); /*Creates a new file output stream
                                                                                  to write the received data to the output file*/

                    while ((bytesRead = dataInputStream.read(buffer, 0, buffer.length)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);  //Writes the data read from the input stream to the output stream.
                        totalBytesRead += bytesRead;
                        if (totalBytesRead >= fileSize) {  /*Checks if the total number of bytes read so far
                                                 is equal to or greater than the expected file size*/
                            break;
                        }
                    }

                    // Close the output stream, input stream, and socket connection
                    fileOutputStream.close();
                    dataInputStream.close();
                    socket.close();

                    runOnUiThread(new Runnable() {  // Update the UI TextView to indicate that the file transfer is complete
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            statusTextView.setText("File received!");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            backRun.start();
        });
    }
}
