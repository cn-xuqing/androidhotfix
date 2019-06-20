package site.xuqing.hotfix;

import android.app.Application;
import android.content.Context;

import site.xuqing.hotfix.net.Web;
import site.xuqing.hotfix.net.WebListener;
import site.xuqing.hotfix.net.WebUrl;
import site.xuqing.hotfix.utils.HotFixDexUtils;

public class HotFixApplication extends Application {
    //后执行
    @Override
    public void onCreate() {
        super.onCreate();
        HotFixDexUtils.loadFixedDex(this,"classes");
        Web.loadConfig(WebUrl.CONFIG_URL, "123", "123", new WebListener() {
            @Override
            public void onWebSuccess(String data) {
                System.out.println(data);
            }

            @Override
            public void onWebError() {

            }
        });
    }

    //先执行
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
