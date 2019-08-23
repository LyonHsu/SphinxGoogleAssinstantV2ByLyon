package com.example.androidthings.assistant.NetWork;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.androidthings.assistant.NetWork.WifiSetting.WifiMenu;
import com.example.androidthings.assistant.R;

/*
adb shell am start -n  com.example.androidthings.assistant/.AssistantActivity
adb shell am start -n  com.android.iotlauncher/.DefaultIoTLauncher
 */

public class NetWork extends RelativeLayout {
    String TAG = NetWork.class.getSimpleName();
    Context context ;
    View view;
    ImageView netWorkIcon;
    TextView SSIDTxt,IPTxt,networkTxt;


    public void onResume(){
        getLocalIpAddress(context);
    }

    public NetWork(Context context) {
        super(context);
        this.context=context;
        init();
    }

    public NetWork(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        init();
    }

    public NetWork(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        init();
    }



    private void init(){
        view=inflate(context, R.layout.network_layout,null);
        setClickable(true);
        setFocusable(true);
        setBackground(context.getResources().getDrawable(R.drawable.btn_blue));
        netWorkIcon = (ImageView)view.findViewById(R.id.netWorkIcon);
        SSIDTxt = (TextView)view.findViewById(R.id.SSIDTxt);
        IPTxt = (TextView)view.findViewById(R.id.IPTxt);
        networkTxt = (TextView)view.findViewById(R.id.networkTxt);
        networkTxt.setText("Network:"+"    ver:"+getAppVersionName(context)+" "+getAppVersion(context));
        addView(view);
        getLocalIpAddress(context);
//        setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                superOnClick();
//            }
//        });
    }

    public void superOnClick(){
        Log.e(TAG,"NetWork onClick!");
        Intent intent = new Intent(context, WifiMenu.class);
        context.startActivity(intent);
        Log.e(TAG,"ACTION_SETTINGS");
    }

    public String getLocalIpAddress(Context context) {

        String ipp =  "no connect wifi!";
        String ip =ipp;
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        ip= String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));

        if(SSIDTxt!=null)
            SSIDTxt.setText(wifiInf.getSSID());
        if(IPTxt!=null)
            IPTxt.setText(ip);
        if(ip.equals(ipp)){
            netWorkIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_gr));
        }else{
            netWorkIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_ge));
        }

        Log.e(TAG, "20190610 ***** IP="+ ip);
        String theSpeech="已經連結到"+wifiInf.getSSID()+",Ip="+ip.replace(".","點");
        Log.e(TAG, "20190610***** theSpeechIP="+ theSpeech);
        return theSpeech;
    }

    private static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
//            throw new RuntimeException(TAG+" Could not get package name: " + e);
            return "Could not get VersionName";
        }
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
//            throw new RuntimeException("Could not get package name: " + e);
            return -1;
        }
    }

}
