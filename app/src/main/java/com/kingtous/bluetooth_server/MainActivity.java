package com.kingtous.bluetooth_server;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    java.util.UUID uuid;

    TextView message_view;
    TextView message_status;

    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    ListenThread currentThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message_view=findViewById(R.id.bluetooth_message);
        message_status=findViewById(R.id.bluetooth_status);

        bluetoothManager=(BluetoothManager) getSystemService(Activity.BLUETOOTH_SERVICE);
        bluetoothAdapter=bluetoothManager.getAdapter();
        if (bluetoothAdapter==null)
        {
            new AlertDialog.Builder(this)
                    .setTitle("错误")
                    .setMessage("设备无蓝牙设备")
                    .setNegativeButton("关闭软件", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }

        uuid=UUID.fromString(getString(R.string.UUID));
        IntentFilter filter=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver,filter);
        startListen();
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    changeStatus("蓝牙未打开","red");
                    log("蓝牙被关闭，请重新打开蓝牙");
                    if (currentThread.isAlive())
                    {
                        currentThread.interrupt();
                    }
                    break;
                case BluetoothAdapter.STATE_ON:
                    startListen();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    break;
                default:
                    log("Unkonwn STATE");
                    break;
            }
        }
    };

    private void startListen()
    {
        currentThread=new ListenThread();
        if (!bluetoothAdapter.isEnabled())
        {
            changeStatus("蓝牙未打开","red");
            new AlertDialog.Builder(this)
                    .setTitle("蓝牙检测")
                    .setMessage("蓝牙未打开.")
                    .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bluetoothAdapter.enable();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
        else
        {
            listen();
        }
    }

    private void listen()
    {
            log("初始化正常，开始监听");
            changeStatus("正在监听","green");
            currentThread.start();
    }

    private void log(String text)
    {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private class ListenThread extends Thread{
        @Override
        public void run() {

            try {
                Looper.prepare();
                BluetoothServerSocket socket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("name",uuid);
                while (true)
                {
                    BluetoothSocket deviceSocket=socket.accept();
                    if (deviceSocket!=null)
                    {
                        BluetoothDevice device=deviceSocket.getRemoteDevice();
                        log("设备连接:"+device.getName());
                        InputStream stream=deviceSocket.getInputStream();
                        byte[] buffer=new byte[1024];
                        int cnt=stream.read(buffer);
                        stream.close();
                        String s=new String(buffer,0,cnt);
                        message_view.append(device.getName()+": "+s+'\n');
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                Looper.loop();
            }
        }
    }

    private void changeStatus(String statusString,String color)
    {
        message_status.setText(getString(R.string.bluetooth_status));
        String str="<font color='"+color+"'><small>"+statusString+"</small></font>";
        message_status.append(Html.fromHtml(str));
    }

}
