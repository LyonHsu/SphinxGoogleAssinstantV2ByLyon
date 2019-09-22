package com.example.androidthings.assistant.BlueTooth.Device;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;

public class DeviceInfoManager {
    private static final String TAG = "DeviceInfoManager";
    private static ActivityManager mActivityManager;
    public synchronized static ActivityManager getActivityManager(Context context) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }
    /**
     * 用於獲取狀態列的高度。
     *
     * @return 返回狀態列高度的畫素值。
     */
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }
    /**
     * 計算已使用記憶體的百分比，並返回。
     *
     * @param context
     *      可傳入應用程式上下文。
     * @return 已使用記憶體的百分比，以字串形式返回。
     */
    public static String getUsedPercentValue(Context context) {
        long totalMemorySize = getTotalMemory();
        long availableSize = getAvailableMemory(context) / 1024;
        int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
        return percent+"%";
    }
    /**
     * 獲取當前可用記憶體，返回資料以位元組為單位。
     *
     * @param context 可傳入應用程式上下文。
     * @return 當前可用記憶體。
     */
    public static long getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        getActivityManager(context).getMemoryInfo(mi);
        return mi.availMem;
    }
    /**
     * 獲取系統總記憶體,返回位元組單位為KB
     * @return 系統總記憶體
     */
    public static long getTotalMemory() {
        long totalMemorySize = 0;
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
//將非數字的字元替換為空
            try {
                totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D", ""));
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalMemorySize;
    }
    /**
     * 獲取頂層activity的包名
     * @param context
     * @return activity的包名
     */
    public static String getTopActivityPackageName(Context context) {
        ActivityManager activityManager = getActivityManager(context);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
        return runningTasks.get(0).topActivity.getPackageName();
    }
    /**
     * 獲取當前程序的CPU使用率
     * @return CPU的使用率
     */
    public static float getCurProcessCpuRate()
    {
        float totalCpuTime1 = getTotalCpuTime();
        float processCpuTime1 = getAppCpuTime();
        try
        {
            Thread.sleep(360);
        }
        catch (Exception e)
        {
        }
        float totalCpuTime2 = getTotalCpuTime();
        float processCpuTime2 = getAppCpuTime();
        float cpuRate = 100 * (processCpuTime2 - processCpuTime1)
                / (totalCpuTime2 - totalCpuTime1);
        return cpuRate;
    }
    /**
     * 獲取總的CPU使用率
     * @return CPU使用率
     */
    public static float getTotalCpuRate() {
        float totalCpuTime1 = getTotalCpuTime();
        float totalUsedCpuTime1 = totalCpuTime1 - sStatus.idletime;
        try {
            Thread.sleep(360);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        float totalCpuTime2 = getTotalCpuTime();
        float totalUsedCpuTime2 = totalCpuTime2 - sStatus.idletime;
        float cpuRate = 100 * (totalUsedCpuTime2 - totalUsedCpuTime1)
                / (totalCpuTime2 - totalCpuTime1);

        return cpuRate;
    }
    /**
     * 獲取系統總CPU使用時間
     * @return 系統CPU總的使用時間
     */
    public static long getTotalCpuTime() {
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
//   long totalCpu = Long.parseLong(cpuInfos[2])
//         Long.parseLong(cpuInfos[3])   Long.parseLong(cpuInfos[4])
//         Long.parseLong(cpuInfos[6])   Long.parseLong(cpuInfos[5])
//         Long.parseLong(cpuInfos[7])   Long.parseLong(cpuInfos[8]);
            sStatus.usertime = Long.parseLong(cpuInfos[2]);
            sStatus.nicetime = Long.parseLong(cpuInfos[3]);
            sStatus.systemtime = Long.parseLong(cpuInfos[4]);
            sStatus.idletime = Long.parseLong(cpuInfos[5]);
            sStatus.iowaittime = Long.parseLong(cpuInfos[6]);
            sStatus.irqtime = Long.parseLong(cpuInfos[7]);
            sStatus.softirqtime = Long.parseLong(cpuInfos[8]);
        }catch (IOException ex)
        {
            Log.e(TAG,"getTotalCpuTime ex:"+ex);
        }
        return sStatus.getTotalTime();
    }
    /**
     * 獲取當前程序的CPU使用時間
     * @return 當前程序的CPU使用時間
     */
    public static long getAppCpuTime()
    {
// 獲取應用佔用的CPU時間
        String[] cpuInfos = null;
        try
        {
            int pid = android.os.Process.myPid();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid  + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13]);
        Long.parseLong(cpuInfos[14]);
        Long.parseLong(cpuInfos[15]);
        Long.parseLong(cpuInfos[16]);
        return appCpuTime;
    }
    static Status sStatus = new Status();
    static class Status {
        public long usertime;
        public long nicetime;
        public long systemtime;
        public long idletime;
        public long iowaittime;
        public long irqtime;
        public long softirqtime;
        public long getTotalTime() {
            return (usertime +  nicetime
                    +systemtime  + idletime +  iowaittime +
                    irqtime +  softirqtime);
        }
    }
}
