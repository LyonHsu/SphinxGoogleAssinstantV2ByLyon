package com.example.androidthings.assistant.BlueTooth.Device;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class GetMacAddress {
    static String TAG = GetMacAddress.class.getName();

    public static String queryMyDevice(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //检查Wifi状态
        if (!wm.isWifiEnabled()) {
            Log.e(TAG,"mac wifi status isWifiEnabled:"+wm.isWifiEnabled());
            return "null";
        }

        WifiInfo wi = wm.getConnectionInfo();
        //获取32位整型IP地址
        int ipAdd = wi.getIpAddress();
        //把整型地址转换成“*.*.*.*”地址
        String ip = intToIp(ipAdd);
        String mac = wi.getMacAddress();

        //结合下述第二种获取方式
        if(TextUtils.equals(mac, "02:00:00:00:00:00")){
            String temp = getMacAddress();
            if(!TextUtils.isEmpty(temp)){
                mac = temp;
            }
        }

        return mac;

    }

    private static String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    private static String getMacAddress(){
        String macAddress = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iF = interfaces.nextElement();

                byte[] addr = iF.getHardwareAddress();
                if (addr == null || addr.length == 0) {
                    continue;
                }

                StringBuilder buf = new StringBuilder();
                for (byte b : addr) {
                    buf.append(String.format("%02X:", b));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                String mac = buf.toString();
                Log.d(TAG, "mac interfaceName="+iF.getName()+", mac="+mac);

//                if(TextUtils.equals(iF.getName(), "wlan0")){
//                    return mac;
//                }
                if(TextUtils.equals(iF.getName(), "eth0")){
                    return mac;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            Log.e(TAG,"SocketException:"+e);
            return macAddress;
        }

        return macAddress;
    }

    public static String getBlueToothMac(Context context) {
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        String addressStr = mBluetoothAdapter.getAddress();
        return addressStr;
    }

    public static String getBluetoothMacAddress() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String bluetoothMacAddress = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            try {
                Field mServiceField = bluetoothAdapter.getClass().getDeclaredField("mService");
                mServiceField.setAccessible(true);

                Object btManagerService = mServiceField.get(bluetoothAdapter);

                if (btManagerService != null) {
                    bluetoothMacAddress = (String) btManagerService.getClass().getMethod("getAddress").invoke(btManagerService);
                }
            } catch (NoSuchFieldException e) {

            } catch (NoSuchMethodException e) {

            } catch (IllegalAccessException e) {

            } catch (InvocationTargetException e) {

            }
        } else {
            bluetoothMacAddress = bluetoothAdapter.getAddress();
        }
        return bluetoothMacAddress;
    }
}
