package site.xuqing.hotfix.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import site.xuqing.hotfix.Hotfix;

public class AppInfoHelper {
    private static final String META_DATA_KEY="site.xuqing.hotfix.META_DATA_KEY";
    /**
     * 返回当前程序版本号
     */
    public static String getAppVersionCode() {
        int versioncode = 0;
        try {
            PackageManager pm = Hotfix.getApplicationContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(Hotfix.getApplicationContext().getPackageName(), 0);
            // versionName = pi.versionName;
            versioncode = pi.versionCode;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versioncode + "";
    }

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName() {
        String versionName=null;
        try {
            PackageManager pm = Hotfix.getApplicationContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(Hotfix.getApplicationContext().getPackageName(), 0);
            versionName = pi.versionName;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * 返回当前应用的包名
     */
    public static String getPackageName() {
        try {
            PackageManager packageManager = Hotfix.getApplicationContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    Hotfix.getApplicationContext().getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return null;
    }

    /**
     * 返回当前应用的application中设置的meta-data值，即sign值
     */
    public static String getAppMetaData(){
        String sign=null;
        try {
            ApplicationInfo appInfo = Hotfix.getApplicationContext().getPackageManager()
                    .getApplicationInfo(Hotfix.getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            sign = appInfo.metaData.getString(META_DATA_KEY);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("MetaData", "Exception", e);
        }
        return sign;
    }
}
