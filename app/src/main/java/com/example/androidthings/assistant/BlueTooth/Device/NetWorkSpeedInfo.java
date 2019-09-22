package com.example.androidthings.assistant.BlueTooth.Device;

import android.content.Context;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.DecimalFormat;

public class NetWorkSpeedInfo {
    static String TAG = NetWorkSpeedInfo.class.getName();
    private static long lastTotalRxBytes = 0;

    private static long lastTimeStamp = 0;
    private int UPDATE = 100;

    public static String getNetSpeed() {
        String speedS="none kb/s";
        try {
            long nowTotalRxBytes = getTotalRxBytes();
            long nowTimeStamp = System.currentTimeMillis();
            long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
            DecimalFormat df = new DecimalFormat("00000");
            lastTimeStamp = nowTimeStamp;
            lastTotalRxBytes = nowTotalRxBytes;
            if (speed == 0) {
                speedS = "00.00kb/s";
            } else {
                speedS = df.format(speed) + "kB/s";
            }
        }catch (Exception e){
            Log.e(TAG,"getNetSpeed Exception:"+e);
            e.printStackTrace();
        }
        return speedS;
    }

    //https://blog.csdn.net/u011068702/article/details/52305065
    //TrafficStats实现流量实时监测
    private static long getTotalRxBytes() {
// return TrafficStats.getUidRxBytes(getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 :(TrafficStats.getTotalRxBytes()/1024);//转为KB
        return TrafficStats.getTotalRxBytes()/1024;//转为KB
    }

    //http://jc7003.pixnet.net/blog/post/342594668-android-%E5%8F%96%E5%BE%97%E7%95%B6%E5%89%8D%E9%80%A3%E7%B7%9A%E7%8B%80%E6%85%8B%E8%88%87%E9%9B%BB%E4%BF%A1%E5%95%86
    private static WifiInfo getWifiInfo(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && getWifiState(wifiManager)== WifiManager.WIFI_STATE_ENABLED) {
            return wifiManager.getConnectionInfo();
        }
        return null;
    }

    private static int getWifiState(WifiManager manager) {
        return manager == null ? WifiManager.WIFI_STATE_UNKNOWN : manager.getWifiState();
    }


    //https://abgne.tw/android/android-code-snippets/android-telephonymanager-network-information.html
    private static String getGPSInfo(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // 手機漫遊狀態
        String roamingStatus = telManager.isNetworkRoaming() ? "漫遊中" : "非漫遊";
        // 電信網路國別
        String country = telManager.getNetworkCountryIso();
        // 電信公司代號
        String operator = telManager.getNetworkOperator();
        // 電信公司名稱
        String operatorName = telManager.getNetworkOperatorName();
        // 行動網路類型
        String[] networkTypeArray = {"UNKNOWN", "GPRS", "EDGE", "UMTS", "CDMA", "EVDO 0", "EVDO A", "1xRTT", "HSDPA", "HSUPA", "HSPA","IDEN","EVDO_B","LTE","eHRPD","HSPAP","GSM","SCDMA","IWLAN","LTE_CA"};
        String networkType="";
        if(networkTypeArray.length>telManager.getNetworkType())
            networkType= networkTypeArray[telManager.getNetworkType()];
        // 行動通訊類型
        String[] phoneTypeArray = {"NONE", "GSM", "CDMA"};
        String phoneType = phoneTypeArray[telManager.getPhoneType()];
        return roamingStatus+","+operatorName+" "+networkType+","+phoneType;
    }

    public static String getSSID(Context context) {
        WifiInfo wifiInfo = getWifiInfo(context);
        if(wifiInfo != null){
            return wifiInfo.getSSID();
        }else if ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE) !=null) {
            return getGPSInfo(context);
        }
        return null;
    }
}
