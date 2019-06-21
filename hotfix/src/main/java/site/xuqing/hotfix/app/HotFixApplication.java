package site.xuqing.hotfix.app;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;

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
    private static final Map<String,String>data=new HashMap<>();
    public static final String KEY_FIX="fix";
    public static final String KEY_APK="apk";
    @Override
    public void onCreate() {
        super.onCreate();
        HotFixDexUtils.loadFixedDex(this);
        init(getApplicationContext(),0);
    }

    //TODO 需要获取系统的package和sign签名以及版本号version
    private void init(final Context context, final int version) {
        Web.loadConfig(WebUrl.CONFIG_URL, "123", "123", new WebListener() {
            @Override
            public void onWebSuccess(String data) {
                System.out.println(data);
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
                }else if(version>100000){//TODO 判断apk的版本值是否小于网络上的版本值，如果小则更新
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
}
