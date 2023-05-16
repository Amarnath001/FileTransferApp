package com.example.filetransferapp;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.Arrays;
//import static com.example.filetransferapp.NetworkShare.verifyStoragePermissions;
public class ReceiveFiles extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    //private TextView statusTextView;
    //private TextView fileNameTextView;
    public int SERVER_PORT = 8080;
    int status =0;
    Thread backRun;
    public static String TAG = "Receive Files : ";
    private static final String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //public Socket socket;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(ReceiveFiles.this);
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
            verifyStoragePermissions(this);
//read the number of files from the client
            int number = dis.readInt();
            int ign[]=new int[number];
            for(int j = 0 ;j<number;j++)
            {
                ign[j]=1;
            }
            ArrayList<File>files = new ArrayList<File>(number);
            long FileSize[] = new long[number];
            System.out.println("Number of Files to be received: " +number);
            //read file names, add files to arraylist
            for(int i = 0; i< number;i++){
                File file = new File(dis.readUTF());
                files.add(file);
            }
            System.out.println("FILE LIST IN CLIENT SIDE : " +files);
            for(int i=0;i<number;i++)
            {

                FileSize[i]= dis.readLong();

            }
            System.out.println("FILE LIST (SIZES) IN CLIENT SIDE : " + Arrays.toString(FileSize));
            int n = 0;
            byte[]buf = new byte[4092];
            //outer loop, executes one for each file
            for(int i = 0; i < files.size();i++){
                    int finalI = i;
                    System.out.println("Receiving file: " + files.get(i).getName());
                    //create a new fileoutputstream for each new file
                    //String filename = dis.readUTF();
                    // System.out.println("UTF File name is : "+dis.readUTF());
                    //System.out.println("FILE SIZES : "+fileSize);
                    File in = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), files.get(i).getName());
                    //in.createNewFile();
                    if (in.exists()) {
                        ReceiveFiles.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveFiles.this);
                                // Set the message show for the Alert time
                                builder.setMessage("FILE ALREADY EXISTS!!! (DO YOU WANT TO CONTINUE?) -------- FILE IS : "+in.getName());
                                // Set Alert Title
                                builder.setTitle("Alert !");
                                // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                                builder.setCancelable(false);
                                // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
                                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                                    // When the user click yes button then app will close
                                    status =1;
                                    dialog.cancel();
                                });
                                // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
                                builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                                    // If user click no then dialog box is canceled.
                                    status=-1;
                                    dialog.cancel();
                                });

                                // Create the Alert dialog
                                AlertDialog alertDialog = builder.create();
                                // Show the Alert Dialog box
                                alertDialog.show();
                            }
                        });
                    } else {
                        ReceiveFiles.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ReceiveFiles.this,
                                        "FILE DOES NOT EXIST , Creating New File!!",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        in.createNewFile();
                    }
                    if(status!=-1){
                    ReceiveFiles.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ReceiveFiles.this,
                                "FILE RECEIVING IS : " + files.get(finalI).getName(),
                                Toast.LENGTH_LONG).show();
                    }
                    });}
                    if(status!=-1) {
                        FileOutputStream fos = new FileOutputStream(in);
                        //read file
                        while (FileSize[i] >= 0 && (n = dis.read(buf, 0, (int) Math.min(buf.length, FileSize[i]))) != -1) {
                            fos.write(buf, 0, n);
                            FileSize[i] -= n;
                            if (FileSize[i] == 0 || FileSize[i] < 0)
                                break;
                        }
                    fos.close();
                    }
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
    public int checkFile(String filename)
    {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+filename);
        if(file.exists())
            return 1;
        else
            return -1;
    }
}