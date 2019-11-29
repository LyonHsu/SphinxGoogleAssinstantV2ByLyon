package com.example.androidthings.assistant.Tool;


import com.example.androidthings.assistant.AppController;

import org.json.JSONException;
import org.json.JSONObject;

public class Log {
    public final static String TAG = "Lyon";
    static boolean isDebug = true;
    public static void d(String tag, String msg) {
        if(isDebug) {
            android.util.Log.d(TAG + "/" + tag, msg);
//            AppController.getInstance().setLogInfo("d", TAG + "/" + tag, msg);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Type","Logd");
                jsonObject.put("TAG",TAG);
                jsonObject.put("MSG",msg);
                if(AppController.getInstance().getBluetoothTool()!=null)
                    AppController.getInstance().getBluetoothTool().bluetoothWrite(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (NullPointerException e){
                e.printStackTrace();
            }

        }
    }
    public static void i(String tag, String msg) {
        if(isDebug) {
            android.util.Log.i(TAG + "/" + tag, msg);
//            AppController.getInstance().setLogInfo("i", TAG + "/" + tag, msg);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Type","Logi");
                jsonObject.put("TAG",TAG);
                jsonObject.put("MSG",msg);
                if(AppController.getInstance().getBluetoothTool()!=null)
                AppController.getInstance().getBluetoothTool().bluetoothWrite(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }
    public static void e(String tag, String msg) {
        if(isDebug) {
            android.util.Log.e(TAG + "/" + tag, msg);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Type","Loge");
                jsonObject.put("TAG",TAG);
                jsonObject.put("MSG",msg);
                if(AppController.getInstance().getBluetoothTool()!=null)
                AppController.getInstance().getBluetoothTool().bluetoothWrite(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }

    public static void e(String tag, String msg,Exception e) {
        if(isDebug) {
            android.util.Log.e(TAG + "/" + tag, msg+" e:"+e);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Type","Loge");
                jsonObject.put("TAG",TAG);
                jsonObject.put("MSG",msg+" e:"+e);
                if(AppController.getInstance().getBluetoothTool()!=null)
                AppController.getInstance().getBluetoothTool().bluetoothWrite(jsonObject);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }catch (NullPointerException e1){
                e1.printStackTrace();
            }
        }
    }

    public static void w(String tag, String msg) {
        if(isDebug) {
            android.util.Log.e(TAG + "/" + tag, msg);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Type","Logw");
                jsonObject.put("TAG",TAG);
                jsonObject.put("MSG",msg);
                if(AppController.getInstance().getBluetoothTool()!=null)
                AppController.getInstance().getBluetoothTool().bluetoothWrite(jsonObject);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }

    public static void v(String tag, String msg) {
        if(isDebug) {
            android.util.Log.v(TAG + "/" + tag, msg);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Type","Logv");
                jsonObject.put("TAG",TAG);
                jsonObject.put("MSG",msg);
                if(AppController.getInstance().getBluetoothTool()!=null)
                AppController.getInstance().getBluetoothTool().bluetoothWrite(jsonObject);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }
}
