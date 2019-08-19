package com.example.androidthings.assistant.Tool;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.UI_MODE_SERVICE;

public class Utils {

    static String TAG = Utils.class.getName();

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
//            throw new RuntimeException(TAG+" Could not get package name: " + e);
            return "Could not get VersionName";
        }
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
//            throw new RuntimeException("Could not get package name: " + e);
            return -1;
        }
    }

    public static int dpToPx(Context context, int dp){
        if(context == null) {
            return MainConstant.NO_DATA;
        }

        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }


    public static int boolToInt(boolean b) {
        return b ? 1 : 0;
    }



    public static int stringToInt(String text){
        try {
            Matcher matcher = Pattern.compile("\\d+").matcher(text);
            String number = "";
            while (matcher.find()) {
                number += matcher.group();
            }

            return Integer.parseInt(number);
        }catch (Exception e){
            return -1;
        }
    }

    public static String FormatStackTrace(Throwable throwable) {
        if(throwable==null) return "";
        String rtn = throwable.getStackTrace().toString();
        try {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            throwable.printStackTrace(printWriter);
            printWriter.flush();
            writer.flush();
            rtn = writer.toString();
            printWriter.close();
            writer.close();
        } catch (IOException e) {
            System.out.println(TAG + ": an error FormatStackTrace..." + Utils.FormatStackTrace(e));
        } catch (Exception ex) {
            System.out.println(TAG + ": an error FormatStackTrace..." + Utils.FormatStackTrace(ex));
        }
        return rtn;
    }

    public static String inputStreamToString(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, bytes.length);
            String json = new String(bytes);
            return json;
        } catch (IOException e) {
            return null;
        }
    }

    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public static boolean intToBool(int i) {
        return i == 1;
    }

    public static int getDisplayWidth(Context context) {
        if (context == null) {
            return MainConstant.NO_DATA;
        }

        return context.getResources().getDisplayMetrics().widthPixels;
    }
    public static int getDisplayHight(Context context) {
        if (context == null) {
            return MainConstant.NO_DATA;
        }

        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getFragmentWidth(Context context){
        return (getDisplayWidth(context) - dpToPx(context,72)) >> 1;
    }

    public static boolean checkTVDevice(Context mContext){
        boolean isTVDevice = true;
        UiModeManager uiModeManager = (UiModeManager) mContext.getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            android.util.Log.d(TAG, "checkTVDevice Running on a TV Device");
            isTVDevice = true;
        } else {
            android.util.Log.d(TAG, "checkTVDevice Running on a non-TV Device");
            isTVDevice = false;
        }
        return isTVDevice;
    }






    //判断Activity是否Destroy
    public static boolean isDestroy(Activity activity) {
        if (activity == null || activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
            return true;
        } else {
            return false;
        }
    }


    static String T =  "true";
    static String F =  "false";
    public static String boolenToString(boolean b){
        if(b){
            return T;
        }else{
            return F;
        }
    }

    public static boolean StringToBoolen(String s){
        if(T.equals(s)){
            return true;
        }else{
            return false;
        }
    }
}
