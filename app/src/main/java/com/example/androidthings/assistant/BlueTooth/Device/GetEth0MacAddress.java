package com.example.androidthings.assistant.BlueTooth.Device;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class GetEth0MacAddress {
    /**
     *取得mac
     * @return ethMacaddress
     */
    String TAG = GetEth0MacAddress.class.getName();

    public String getMacaddress(Context context) {
        String str_ethMacaddress = "";

        try{
            str_ethMacaddress = loadFileAsString("/sys/class/net/eth0/address");
            Log.d(TAG,"eth0 mac:"+str_ethMacaddress);
        }catch(Exception e){
            e.printStackTrace();
        }

        if(str_ethMacaddress.equals("") || str_ethMacaddress == null) {
            try {
                byte[] b = NetworkInterface.getByName("eth0")
                        .getHardwareAddress();
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < b.length; i++) {
                    if (i != 0) {
                        buffer.append(':');
                    }
                    System.out.println("mac b:"+(b[i]&0xFF));
                    String str = Integer.toHexString(b[i] & 0xFF).toUpperCase();
                    buffer.append(str.length() == 1 ? 0 + str : str);
                    Log.d(TAG,"MacAdd str is " + str.toUpperCase());
                }
                str_ethMacaddress = buffer.toString().toUpperCase();
            }catch(Exception e){
                e.printStackTrace();
            }
            Log.d(TAG,"eth0 mac2:"+str_ethMacaddress);
        }

        if (str_ethMacaddress.equals("")) {
            // //wifi mac
            try {
                WifiManager wifiMan = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInf = wifiMan.getConnectionInfo();
                if (wifiInf.getMacAddress() != null
                        && !wifiInf.getMacAddress().equals("")) {
                    str_ethMacaddress = wifiInf.getMacAddress();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (str_ethMacaddress == null || str_ethMacaddress.equals("")) {
                String macSerial = "";
                String str = "";
                try {
                    Process mProcess = Runtime.getRuntime().exec(
                            "cat /sys/class/net/wlan0/address"); // eth0 wlan0
                    InputStreamReader ir = new InputStreamReader(
                            mProcess.getInputStream());
                    LineNumberReader input = new LineNumberReader(ir);
                    for (; null != str;) {
                        str = input.readLine().toUpperCase();
                        if (str != null) {
                            macSerial = str.trim();
                            break;
                        }
                        Log.d(TAG,"MacAdd str is " + str.toUpperCase());
                    }
                    if (!macSerial.equals("")) {
                        str_ethMacaddress = macSerial;
                    }

                    System.out.println("macSerial:"+macSerial);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG,"wifi mac:"+str_ethMacaddress);
        }

        if (str_ethMacaddress == null || str_ethMacaddress.equals("")) {
            try {
                // 獲得 IP 位址
                InetAddress ip = getLocalInetAddress();
                byte[] b = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
                StringBuffer buffer = new StringBuffer();
                for(int i=0; i<b.length; i++) {
                    if(i!=0) {
                        //buffer.append(':');
                    }
                    String str = Integer.toHexString(b[i] & 0xFF).toUpperCase();
                    Log.d(TAG,"MacAdd str is " + str.toUpperCase());
                    buffer.append(str.length() == 1 ? 0 + str : str);
                }
                str_ethMacaddress = buffer.toString();
            } catch (Exception e) {
                Log.d(TAG, "取得 MAC 失敗 == " + e.toString());
                str_ethMacaddress ="020000000000";
            }
        }

        str_ethMacaddress = str_ethMacaddress.replace(":","").trim();

        Log.d(TAG,"MacAdd is " + str_ethMacaddress.toUpperCase());
        return str_ethMacaddress.toUpperCase();
    }

    private String loadFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    // 獲取移動設備本地 IP
    private static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            // 列舉
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {
                // 是否還有元素
                NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();
                // 得到下一個元素
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();
                // 得到一個 IP 位址的列舉
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && !ip.getHostAddress().contains(":"))
                        break;
                    else
                        ip = null;
                }
                if (ip != null) {
                    break;
                }
            }
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }
}
