package com.xiwang.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    TextView textView;

    String macAddress = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.text);


        initialize();


        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                macAddress = "";
                textView.setText("");
                scanLeDevice(true);
            }
        });

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnect();
                connect(macAddress);
               // LogUtils.i("开始连接");
               // mBluetoothGatt = currentdevice.connectGatt(MainActivity.this, true, mGattCallback);

            }
        });

        findViewById(R.id.disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnect();
            }
        });

    }



    private BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    protected BluetoothGattCharacteristic mNotifyCharacteristic;

  //  BluetoothDevice currentdevice;

    /**
     * Bluetooth 初始化
     *
     * @return 返回是否初始化成功
     */
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                LogUtils.e("无法初始化BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogUtils.e("无法获取BluetoothAdapter.");
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        return true;
    }

    /**
     * 扫描设备
     *
     * @param enable
     */
    public void scanLeDevice(boolean enable) {
        if (enable) {
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    LogUtils.i("结束搜索");
//                }
//            }, 10000);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
            LogUtils.i("开始搜索");
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            LogUtils.i("结束搜索");
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
        };
    };

    /**
     * 扫描的回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogUtils.d(device.getAddress() + "<==>" + device.getName());
                    //08:7C:BE:9F:0A:66
                    //08:7C:BE:9F:0D:62
                    //08:7C:BE:9F:0D:61 莫
                    if (device.getAddress().equals("08:7C:BE:9F:0A:66")) {
                        scanLeDevice(false);
                        textView.setText(device.getAddress() + "<==>" + device.getName());
                        macAddress = device.getAddress();
                        //  currentdevice = device;

                    }
                }
            });

        }
    };



    /**
     * 连接蓝牙
     *
     * @param address
     * @return
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            LogUtils.w("蓝牙未初始化或未指定地址.");
            return false;
        }

       // scanLeDevice(false);

        if (mBluetoothGatt != null) {
            mWriteCharacteristic = null;
            mNotifyCharacteristic = null;
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            LogUtils.w("设备未找到,不能连接.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        LogUtils.d("试图创建新的连接.");
        return false;
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtils.w("BluetoothAdapter或mBluetoothGatt未初始化");
            return;
        }
        mBluetoothGatt.disconnect();
       // mBluetoothGatt.close();

    }

    /**
     * 连接蓝牙 回调
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogUtils.e("onConnectionStateChange : "+status+"  newState : "+newState);

            //  if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {

                    LogUtils.i("连接到GATT服务.");
                    LogUtils.i("试图启动服务发现:" + mBluetoothGatt.discoverServices());

                } else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mNotifyCharacteristic = null;
                    mWriteCharacteristic = null;
                    LogUtils.i("从GATT服务上断开.");

                   // mBluetoothGatt.close();

//                    if (mBluetoothGatt != null) {
//                        mBluetoothGatt.disconnect();
//                        mBluetoothGatt.close();
//                        mBluetoothGatt = null;
//                    }
                }
//            } else {
//                LogUtils.e("这是什么情况..." + status + "《==》" + newState);
//            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                BluetoothGattService gattService_v2 = mBluetoothGatt.getService(V2_SERVICE_UUID);
                if (gattService_v2 != null) {

                    if (null == mWriteCharacteristic) {
                        mWriteCharacteristic = gattService_v2.getCharacteristic(V2_WRITE_CHARACTER);
                        LogUtils.i("BLE_V2---->" + mWriteCharacteristic);
                    }


                    if (null == mNotifyCharacteristic) {
                        mNotifyCharacteristic = gattService_v2.getCharacteristic(V2_NOTIFY_CHARACTER);
                        setCharacteristicNotification(mNotifyCharacteristic,true);
                        LogUtils.d("通道开启成功！");
                    }

                    if ((null != mWriteCharacteristic) && (null != mNotifyCharacteristic)) {
                    } else {
                        LogUtils.e("获取特征失败...");
                    }

                } else {
                    LogUtils.e("获取特征失败...");
                }





            } else {
                LogUtils.w("onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            LogUtils.i("蓝牙回复-->" + bytes2hex(data));

        }
    };













    public final static UUID V2_SERVICE_UUID = UUID.fromString("0000fee9-0000-1000-8000-00805f9b34fb");
    public final static UUID V2_WRITE_CHARACTER = UUID.fromString("d44bc439-abfd-45a2-b575-925416129600");
    public final static UUID V2_NOTIFY_CHARACTER = UUID.fromString("d44bc439-abfd-45a2-b575-925416129601");
    public final static UUID CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /**
     * 启用和禁用通知
     *
     * @param characteristic
     * @param enabled
     */
    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtils.w("蓝牙适配器未初始化.");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }


    public static String bytes2hex(byte[] bytes) {
        //final String HEX = "0123456789ABCDEF";
        final String HEX = "0123456789abcdef";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            if (i != bytes.length - 1) {
                // 取出这个字节的高4位，然后与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
                sb.append("0x");
                sb.append(HEX.charAt((bytes[i] >> 4) & 0x0f));
                // 取出这个字节的低位，与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
                sb.append(HEX.charAt(bytes[i] & 0x0f));
                sb.append(",");
            } else {
                // 取出这个字节的高4位，然后与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
                sb.append("0x");
                sb.append(HEX.charAt((bytes[i] >> 4) & 0x0f));
                // 取出这个字节的低位，与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
                sb.append(HEX.charAt(bytes[i] & 0x0f));
            }
        }
        return sb.toString();
    }
}
