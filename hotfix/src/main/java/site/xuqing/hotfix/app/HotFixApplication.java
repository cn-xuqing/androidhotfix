package site.xuqing.hotfix.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import site.xuqing.hotfix.Hotfix;
import site.xuqing.hotfix.config.ConfigManager;
import site.xuqing.hotfix.net.Web;
import site.xuqing.hotfix.net.WebListener;
import site.xuqing.hotfix.net.WebUrl;
import site.xuqing.hotfix.utils.HotFixDexUtils;
import site.xuqing.hotfix.utils.HotFixFileUtils;

public class HotFixApplication extends Application {
    private static final String META_DATA_KEY="site.xuqing.hotfix.META_DATA_KEY";
    private static final Map<String,String>data=new HashMap<>();
    public static final String KEY_FIX="fix";
    public static final String KEY_APK="apk";
    @Override
    public void onCreate() {
        super.onCreate();
        HotFixDexUtils.loadFixedDex(this);
        String versionCode=getAppVersionCode(getApplicationContext());
        String versionName=getAppVersionName(getApplicationContext());
        String packageName=getPackageName(getApplicationContext());
        String sign=getAppMetaData(getApplicationContext());
        init(getApplicationContext(),versionCode,versionName,packageName,sign);
    }

    /**
     * TODO 1.可以添加用户升级版本提醒以及用户自定义下载
     * TODO 2.需要尝试把调整安装的逻辑放至api，以减少用户的自行设置流程
     */
    private void init(final Context context, final String versionCode,final String versionName,String packageName,String sign) {
        Web.loadConfig(WebUrl.CONFIG_URL, packageName, sign, new WebListener() {
            @Override
            public void onWebSuccess(String data) {
                System.out.println(data);
                int webVersionCode=-999;
                int currentVersionCode=0;
                try {
                    webVersionCode = Integer.parseInt(ConfigManager.getInstance().getUpgradeVersionCode());
                    currentVersionCode=Integer.parseInt(versionCode);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (ConfigManager.getInstance().isUpgrade()) {
                    Web.loadUpgradePackage(WebUrl.BASE_HOST + ConfigManager.getInstance().getUpgradeUrl(), new WebListener() {
                        @Override
                        public void onWebSuccess(final String data) {
                            System.out.println(data);
                            if (Hotfix.getInstance().getOnUpgradeListener() != null) {
                                Looper.prepare();
                                Hotfix.getInstance().getOnUpgradeListener().onUpgrade(data);
                                Looper.loop();
                            }else{
                                setData(KEY_APK,data);
                            }
                        }
                        @Override
                        public void onWebError() {
                        }
                    });
                }else if(currentVersionCode<webVersionCode&&!versionName.equals(ConfigManager.getInstance().getUpgradeVersion())){
                    String apkUrl=HotFixFileUtils.getHotfixApkPath()+ HotFixFileUtils.getHotfixApkFileName();
                    if (Hotfix.getInstance().getOnUpgradeListener() != null) {
                        Looper.prepare();
                        Hotfix.getInstance().getOnUpgradeListener().onUpgrade(apkUrl);
                        Looper.loop();
                    }else{
                        setData(KEY_APK,apkUrl);
                    }
                }else {
                    if (ConfigManager.getInstance().isHotfix()) {
                        Web.loadHotfixPackage(WebUrl.BASE_HOST + ConfigManager.getInstance().getHotfixUrl(), new WebListener() {
                            @Override
                            public void onWebSuccess(String data) {
                                //提示重启
                                System.out.println("hotfix下载成功：" + data);
                                HotFixFileUtils.fixBug(context);
                                if (Hotfix.getInstance().getOnHotfixListener() != null) {
                                    Looper.prepare();
                                    Hotfix.getInstance().getOnHotfixListener().onHotfix(data);
                                    Looper.loop();
                                } else {
                                    setData(KEY_FIX, data);
                                }
                            }

                            @Override
                            public void onWebError() {
                            }
                        });
                    }
                }
            }
            @Override
            public void onWebError() {
            }
        });
    }

    public static void setData(String key,String value){
        data.put(key,value);
    }
    public static String getData(String key){
        if (data.containsKey(key)){
            return data.get(key);
        }
        return null;
    }

    /**
     * 返回当前程序版本号
     */
    public static String getAppVersionCode(Context context) {
        int versioncode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
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
    public static String getAppVersionName(Context context) {
        String versionName=null;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * 返回当前应用的包名
     */
    public static String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return null;
    }

    /**
     * 返回当前应用的application中设置的meta-data值，即sign值
     */
    public static String getAppMetaData(Context context){
        String sign=null;
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            sign = appInfo.metaData.getString(META_DATA_KEY);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("MetaData", "Exception", e);
        }
        return sign;
    }
}
