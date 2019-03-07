package com.kingtous.bluetooth_server;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Looper;
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

    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message_view=findViewById(R.id.bluetooth_message);

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

        listen();
    }


    private void listen()
    {
            new Thread(new Runnable() {
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
            }).start();
    }

    private void log(String text)
    {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

}
