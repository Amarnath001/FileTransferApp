package com.example.filetransferapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class BluetoothTransfer extends AppCompatActivity {
    private static final String TAG = "BluetoothTransfer";
    BluetoothAdapter mBluetoothAdapter;
    //On receive defined for our mBTStateChangeRec (BR REC) ---> Used to get state changes of BT Adapter

    private final BroadcastReceiver mBTStateChangeRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state)
                {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"Bluetooth state change : ON ");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"Bluetooth state change : Turning ON");
                        break;
                }
            }
        }
    };
    //Comment to check if GIT is working
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button sendFile = findViewById(R.id.button_SendFile);
        setContentView(R.layout.activity_bluetooth_transfer);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Button Clicked!");
                int status = checkBT();
                if(status == 1 )
                    Toast.makeText(BluetoothTransfer.this,"Bluetooth is already Turned on!!",Toast.LENGTH_LONG).show();
            }
        });
    }
    public int checkBT()
    {
        if(mBluetoothAdapter==null)
        {Toast.makeText(this,"Bluetooth is not present in this device!!!!",Toast.LENGTH_LONG).show();
            Log.d(TAG,"Bluetooth Is not present in device!");
            return -1;
        }
        else if(mBluetoothAdapter.isEnabled())
            return 1;
        else {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBT);
            IntentFilter stateBT = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBTStateChangeRec,stateBT);
            return 0;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"ON DESTROY CALLED!");
        super.onDestroy();
        unregisterReceiver(mBTStateChangeRec);
    }
}