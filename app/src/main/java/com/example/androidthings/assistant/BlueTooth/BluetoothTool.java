package com.example.androidthings.assistant.BlueTooth;

import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.androidthings.assistant.AppController;
import com.example.androidthings.assistant.AssistantActivity;
import com.example.androidthings.assistant.BlueTooth.Device.GetMacAddress;
import com.example.androidthings.assistant.NetWork.WifiSetting.WifiMenu;
import com.example.androidthings.assistant.NetWork.WifiSetting.WifiSetting;
import com.example.androidthings.assistant.Tool.ToastUtile;
import com.example.androidthings.assistant.Tool.Utils;
import com.google.android.things.bluetooth.BluetoothClassFactory;
import com.google.android.things.bluetooth.BluetoothConfigManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


import static android.bluetooth.BluetoothClass.Device.Major.AUDIO_VIDEO;
import static android.bluetooth.BluetoothClass.Device.Major.COMPUTER;
import static android.bluetooth.BluetoothClass.Device.Major.HEALTH;
import static android.bluetooth.BluetoothClass.Device.Major.IMAGING;
import static android.bluetooth.BluetoothClass.Device.Major.MISC;
import static android.bluetooth.BluetoothClass.Device.Major.NETWORKING;
import static android.bluetooth.BluetoothClass.Device.Major.PERIPHERAL;
import static android.bluetooth.BluetoothClass.Device.Major.PHONE;
import static android.bluetooth.BluetoothClass.Device.Major.TOY;
import static android.bluetooth.BluetoothClass.Device.Major.UNCATEGORIZED;
import static android.bluetooth.BluetoothClass.Device.Major.WEARABLE;
import static android.content.Context.UI_MODE_SERVICE;
import static com.example.androidthings.assistant.BlueTooth.A2dpSinkHelper.ACTION_CONNECTION_STATE_CHANGED;


/*
http://tw.gitbook.net/android/android_bluetooth.html

https://developer.android.com/guide/topics/connectivity/bluetooth#java

//蓝牙设备的连接
http://fecbob.pixnet.net/blog/post/44970812-%5Bandroid%5D--%E8%97%8D%E7%89%99%E8%A8%AD%E5%82%99%E7%9A%84%E6%9F%A5%E6%89%BE%E5%92%8C%E9%80%A3%E6%8E%A5
//蓝牙设备的连接2
http://pclevinblog.pixnet.net/blog/post/314562352-android%E7%A8%8B%E5%BC%8Fbluetoothchat%E7%AF%84%E4%BE%8B%E7%A8%8B%E5%BC%8F%E5%AD%B8%E8%97%8D%E8%8A%BD%E9%80%A3%E7%B7%9A%E5%8E%9F%E7%90%86
 */
public abstract class BluetoothTool {
    static String TAG = BluetoothTool.class.getName();
    Context context;
    //搜索状态的标示
    private boolean mScanning = true;
    //蓝牙适配器List
    private List<BluetoothDevice> mBlueList;
    //蓝牙的回调地址
    private BluetoothAdapter.LeScanCallback mLesanCall;
    HashMap<String, String> bluetoothDeviceName=new HashMap<>();
    //蓝牙适配器 區域藍芽接口(藍芽廣播)
    static BluetoothAdapter mBluetoothAdapter;
    Handler mHandler;
    final int openBluetoothTime=30;
    Boolean isSearchNow=false;
    int time =openBluetoothTime;

    private BluetoothService mService = null;
    String mConnectedDeviceName="";
    private BluetoothDevice connectingDevice;
    SearchOldBluetoothdevice searchOldBluetoothdevice;
    public BluetoothTool(Context context) {
        this.context = context;

        //首先获取BluetoothManager
        BluetoothManager bluetoothManager = null;
        //設定為藍芽音響設備
        try {
            if(AssistantActivity.USE_VOICEHAT_I2S_DAC) {
                bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                if(bluetoothManager!=null) {
                    BluetoothConfigManager manager = BluetoothConfigManager.getInstance();
                    BluetoothClass deviceClass = BluetoothClassFactory.build(
                            BluetoothClass.Service.AUDIO,
                            BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER);
                    manager.setBluetoothClass(deviceClass);
                }
            }
        }catch (Exception e){
            String[] infos = Utils.getAutoJumpLogInfos();
            String error = Utils.FormatStackTrace(e);
            Log.e(TAG,"BluetoothConfigManager set AUDIO Exception:"+error);
        }
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
            mBluetoothAdapter.getState();
            String mac = getBluetoothMac();
            android.util.Log.d(TAG, "\n====================  Bluetooth mac : " + mac + " getState:"+mBluetoothAdapter.getState() );
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        isSupportBlue();
        // 打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            openBlueTooth();
        }
        //初始化List
        mBlueList = new ArrayList<>();
        //实例化蓝牙回调
        mLesanCall = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                //返回三个对象 分类别是 蓝牙对象 蓝牙信号强度 以及蓝牙的广播包
                if (!mBlueList.contains(bluetoothDevice)) {
                    mBlueList.add(bluetoothDevice);
                }
            }
        };
        mService = new BluetoothService(context, serviceHandler);
        if (mService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mService.start();
            }
        }
    }



    public String getBluetoothName(String bluetoothName){
        if(mBluetoothAdapter==null)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBluetoothAdapter.setName(bluetoothName);
        return mBluetoothAdapter.getName();//获取本地蓝牙名称
    }

    public String getBluetoothMac(){
        if(mBluetoothAdapter==null)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String mac= GetMacAddress.getBluetoothMacAddress();
        return mac;
    }


    public int getBluetoothClass(){
        int type = -1;
        try {
            if(AssistantActivity.USE_VOICEHAT_I2S_DAC) {
                BluetoothConfigManager bluetoothConfigManager = BluetoothConfigManager.getInstance();
                BluetoothClass bluetoothClass = bluetoothConfigManager.getBluetoothClass();
//                Log.e(TAG, "getBluetoothClass bluetoothClass:" + bluetoothClass.toString());
                type = bluetoothClass.getMajorDeviceClass();
//                Log.e(TAG, "getBluetoothClass type:" + type);
            }
            return type;
        }catch (Exception e){
            Log.e(TAG,"getBluetoothClass Exception:"+e);
        }
        return -1;
    }

    public boolean isDiscovering(){
        if(mBluetoothAdapter==null)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isDiscovering();//判断当前是否正在查找设备，是返回true
    }

    //判断是否支持蓝牙
    public boolean isSupportBlue(){
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            android.util.Log.d(TAG,"设备不支持蓝牙");
            return true;
        }else {
            android.util.Log.d(TAG,"设备支持蓝牙");
            return false;
        }
    }

    public void findBuletoothDevice(){
        android.util.Log.d(TAG,"findBuletoothDevice");
        try {
// 设置广播信息过滤
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(ACTION_CONNECTION_STATE_CHANGED);
            intentFilter.addAction(A2dpSinkHelper.ACTION_PLAYING_STATE_CHANGED);
            context.registerReceiver(receiver, intentFilter);
// 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
            mBluetoothAdapter.startDiscovery();
            mScanning = true;
            bluetoothDeviceName = new HashMap<>();
            if(mBluetoothAdapter!=null) {
                BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                mBluetoothLeScanner.startScan(scanCallback);
                //Bluetooth low energy
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    public boolean stopSearchBltDevice() {
        BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.stopScan(scanCallback);
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        //暂停搜索设备
        if(mBluetoothAdapter!=null)
            return mBluetoothAdapter.cancelDiscovery();
        return false;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }
    public abstract void getBluetoothDeviceName(HashMap<String, String> bluetoothDeviceName , BluetoothDevice device);
    public abstract void openBluetoothTime(int time);
    public abstract void startBT( Intent intent);
    public void reSearchOldBluetoothdevice(){};

    public void onDestroy() {
        android.util.Log.e(TAG,"onDestroy()");
        context.unregisterReceiver(receiver);
        if (mBluetoothAdapter != null)
            mBluetoothAdapter.cancelDiscovery();
        if(mService!=null){
            mService.stop();
        }
    }

    public void onStart(){

    }

    final Runnable runnable = new Runnable() {
        public void run() {
            if (time>0){
                time--;
//                Log.d(TAG,"openBluetoothTime:"+time);
                openBluetoothTime(time);
                mHandler.postDelayed(runnable,1000);
            }else {
                time=0;
                openBluetoothTime(time);
                isSearchNow = false;
                if(isDiscovering())
                    stopSearchBltDevice();
            }
        }
    };

    public void openBlueTooth(){
        android.util.Log.d(TAG,"openBlueTooth");
        if(isSearchNow)
            return;
        if(mBluetoothAdapter==null)
            return;
        time=openBluetoothTime;
        if (!mBluetoothAdapter.isEnabled() ) {
            android.util.Log.d(TAG, "打开蓝牙");
            AppController.getInstance().speak(context,"bluetooth on ");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // 设置蓝牙可见性，最多300秒
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, openBluetoothTime);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startBT(intent);
//            context.startActivity(intent);
            Toast.makeText(context,"Turned on", Toast.LENGTH_LONG).show();
            mHandler = new Handler();
            mHandler.post(runnable);
            openBluetoothTime(time);
            isSearchNow = true;
        }else{
            android.util.Log.d(TAG, "蓝牙可被搜尋");
            AppController.getInstance().speak(context,"bluetooth can search ");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            // 设置蓝牙可见性，最多300秒  目前設定請看openBluetoothTime參數
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, openBluetoothTime);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startBT(intent);
//            context.startActivity(intent);
            Toast.makeText(context,"Turned on", Toast.LENGTH_LONG).show();
            mHandler = new Handler();
            mHandler.post(runnable);
            openBluetoothTime(time);
            isSearchNow = true;
            Toast.makeText(context,"Already on", Toast.LENGTH_LONG).show();
        }
    }

    int bluetoothDeviceNameSize=0;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            android.util.Log.d(TAG,"BluetoothDevice onReceive "+action);
            if (BluetoothDevice.ACTION_FOUND.equals(action) && isSearchNow) {
                // 获取查找到的蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addBluetoothDevice(device);
            }//状态改变时
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                android.util.Log.d(TAG,"the Bluetooth :"+device.getName()+" state is changed");
                Message msg = serviceHandler.obtainMessage();
                msg.what=BluetoothService.MESSAGE_STATE_CHANGE;
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING://正在配对
                        mConnectedDeviceName = device.getName();
                        msg.arg1=BluetoothService.STATE_CONNECTING;
                        serviceHandler.sendMessage(msg);
                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        mConnectedDeviceName = device.getName();
                        msg.arg1=BluetoothService.STATE_CONNECTED;
                        serviceHandler.sendMessage(msg);
                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        mConnectedDeviceName = device.getName();
                        msg.arg1=BluetoothService.STATE_NONE;
                        serviceHandler.sendMessage(msg);
                    default:
                        Log.d(TAG,"BluetoothDevice BlueToothTestActivity:"+device.getBondState());
                        Toast.makeText(context,device.getBondState()+" "+device.getName()+"......", Toast.LENGTH_SHORT).show();
                        break;
                }
            } // When discovery is finished, change the Activity title
             else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context,"停止搜尋藍牙......", Toast.LENGTH_LONG).show();
                AppController.getInstance().speak(context,"停止搜尋藍牙設備!");
            }else if (intent.getAction().equals(A2dpSinkHelper.ACTION_PLAYING_STATE_CHANGED)) {
                int oldState = A2dpSinkHelper.getPreviousProfileState(intent);
                int newState = A2dpSinkHelper.getCurrentProfileState(intent);
                BluetoothDevice device = A2dpSinkHelper.getDevice(intent);
                Log.d(TAG, "Bluetooth A2DP sink changing playback state from " + oldState +
                        " to " + newState + " device " + device);
                if (device != null) {
                    if (newState == A2dpSinkHelper.STATE_PLAYING) {
                        android.util.Log.i(TAG, "Playing audio from device " + device.getAddress());
                        Toast.makeText(context,"播放狀態改變......播放", Toast.LENGTH_LONG).show();
                    } else if (newState == A2dpSinkHelper.STATE_NOT_PLAYING) {
                        android.util.Log.i(TAG, "Stopped playing audio from " + device.getAddress());
                        Toast.makeText(context,"播放狀態改變......暫停", Toast.LENGTH_LONG).show();
                    }
                }
            }else  if (intent.getAction().equals(ACTION_CONNECTION_STATE_CHANGED)) {
                int oldState = A2dpSinkHelper.getPreviousProfileState(intent);
                int newState = A2dpSinkHelper.getCurrentProfileState(intent);
                BluetoothDevice device = A2dpSinkHelper.getDevice(intent);
                android.util.Log.d(TAG, "Bluetooth A2DP sink changing connection state from " + oldState +
                        " to " + newState + " device " + device);
                if (device != null) {
                    String deviceName = Objects.toString(device.getName(), "a device");
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        connectingDevice = device;
                        AppController.getInstance().speak(context,"藍牙設備以連結");
                        Toast.makeText(context,"Connected to " + deviceName, Toast.LENGTH_LONG).show();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        AppController.getInstance().speak(context,"藍牙設備以斷開");
                        Toast.makeText(context,"Disconnected from " + deviceName, Toast.LENGTH_LONG).show();
                    }
                }
            }
            else{
            }
        }
    };

    private final Handler serviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            android.util.Log.d(TAG,"BluetoothDevice BlueToothTestActivity"+ " 完成配对:"+mConnectedDeviceName);
                            Toast.makeText(context,"完成配對 "+mConnectedDeviceName+"......", Toast.LENGTH_SHORT).show();
                            AppController.getInstance().speak(context," 設備完成配对");
                            if(searchOldBluetoothdevice!=null)
                                searchOldBluetoothdevice.searchBluetoothdevice();
                            stopSearchBltDevice();
                            String ipp =  "no connect wifi!";
                            String ip =ipp;
                            WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                            WifiInfo wifiInf = wifiMan.getConnectionInfo();
                            int ipAddress = wifiInf.getIpAddress();
                            ip= String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
                            String IP=""+ip;
                            String ssID = wifiInf.getSSID();
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("IP", IP);
                                jsonObject.put("SSID", ssID);
                                bluetoothWrite(jsonObject);
                            }catch (JSONException e){
                                Log.e(TAG,"");
                            }
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            android.util.Log.d( TAG,"BluetoothDevice BlueToothTestActivity"+ "正在配对......");
                            Toast.makeText(context,"正在配对"+mConnectedDeviceName+"......", Toast.LENGTH_SHORT).show();
                            AppController.getInstance().speak(context," 設備連結中 ");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            Toast.makeText(context,"沒有配對藍牙裝置......", Toast.LENGTH_SHORT).show();
                            AppController.getInstance().speak(context,"連結失敗!");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    ToastUtile.showText(context,"Me: "+writeMessage);
                    android.util.Log.d(TAG,"BlueTooth chatroom writeMessage:"+writeMessage);
                    break;
                case BluetoothService.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    String deviceName ="Get Msg";
                    try {
                        deviceName = connectingDevice.getName();
                    }catch (NullPointerException e){

                    }
                    proccessMSG(readMessage);
                    ToastUtile.showText(context, deviceName+ ":  " +readMessage);
                    android.util.Log.d(TAG,"BlueTooth chatroom readMessage:"+readMessage);
                    break;
                case BluetoothService.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
                    if (null != context) {
                        Toast.makeText(context, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothService.MESSAGE_TOAST:
                    if (null != context) {
                        Toast.makeText(context, msg.getData().getString(BluetoothService.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
//            Log.i(TAG, "onScanResult findBuletoothDevice scan succeed device size=  " +result);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BluetoothDevice device = result.getDevice();
//                Log.i(TAG, "findBuletoothDevice scan succeed device ==  " +device);
                addBluetoothDevice(device);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
//            Log.i(TAG, "onBatchScanResults findBuletoothDevice scan succeed device size=  " +results.size());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                for (ScanResult result: results){
                    BluetoothDevice device = result.getDevice();
                    android.util.Log.i(TAG, "findBuletoothDevice scan succeed device ==  " +device);
                    addBluetoothDevice(device);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            android.util.Log.i(TAG,"findBuletoothDevice scan fail");
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    addBluetoothDevice(device);
                }
            };

    private void addBluetoothDevice(BluetoothDevice device){
//        Log.d(TAG,"BluetoothDevice 获取查找到的蓝牙设备 :("+getBlueToothType(device.getBluetoothClass().getMajorDeviceClass())+")"+device.getName()+":"+device.getAddress()+" mScanning:"+mScanning+ " type:0x"+device.getBluetoothClass().getMajorDeviceClass());
        int classint = device.getBluetoothClass().getMajorDeviceClass();
        String type=getBlueToothType(classint);
        //排除未分類(UNCATEGORIZED)或是雜項(MISC) 或是其他的(Other) 藍牙設備
        if(type.equals("UNCATEGORIZED") || type.equals("Other") || type.equals("MISC"))
            return;
        bluetoothDeviceName.put(device.getAddress(),device.getName()+"("+type+")");
        if(bluetoothDeviceName.size()!=bluetoothDeviceNameSize){
            bluetoothDeviceNameSize = bluetoothDeviceName.size();
            getBluetoothDeviceName(bluetoothDeviceName ,device);
        }
    }


    public String getBlueToothType(int classint){
//        Log.d(TAG,"getBlueToothType type:0x"+Integer.toHexString(classint));
        String type="";
        switch (classint){
            case MISC://0x0000
                type="MISC";
                break;
            case COMPUTER://0x0100
                type="COMPUTER";
                break;
            case PHONE://0x0200
                type="PHONE";
                break;
            case NETWORKING://0x0300
                type="NETWORKING";
                break;
            case AUDIO_VIDEO://0x0400
                type="AUDIO_VIDEO";
                break;
            case PERIPHERAL://0x0500
                type="PERIPHERAL";
                break;
            case IMAGING://0x0600
                type="IMAGING";
                break;
            case WEARABLE://0x0700
                type="WEARABLE";
                break;
            case TOY://0x0800
                type="TOY";
                break;
            case HEALTH://0x0900
                type="HEALTH";
                break;
            case UNCATEGORIZED:// 0x1F00
                type="UNCATEGORIZED";
                break;
            case 2://bluetooth_le
                type="BLE";
            default:
                type="Other";
                break;
        }
        return type;
    }

    public interface SearchOldBluetoothdevice{
        void searchBluetoothdevice();
    }

    public void setSearchBluetoothdevice(SearchOldBluetoothdevice searchOldBluetoothdevice){
        this.searchOldBluetoothdevice=searchOldBluetoothdevice;
    }

    public void bluetoothWrite(JSONObject jsonObject){
        if(mService!=null){
            String jsonS = jsonObject.toString();
//            android.util.Log.d(TAG,"bluetooth chatroom write: "+jsonS);
            byte[] writeBuf = (byte[]) jsonS.getBytes();
            mService.write(writeBuf);
        }
    }

    public void proccessMSG(String msg){
        try {
            JSONObject jsonObject = new JSONObject(msg);
            if(jsonObject.has("SSID")){
                String ssid = jsonObject.optString("SSID");
                String passWD = jsonObject.optString("PASSWD");
                String wifiType = jsonObject.optString("WifiTYPE");
                WifiSetting wifiSetting = new WifiSetting();
                wifiSetting.initWifi(context);
                wifiSetting.addNetwork(WifiSetting.createWifiConfiguration(ssid, passWD, wifiSetting.selectWifiCipherType(wifiType)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



}
