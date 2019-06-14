package site.xuqing.hotfix;

import android.app.Application;
import android.content.Context;

import site.xuqing.hotfix.utils.HotFixDexUtils;

public class HotFixApplication extends Application {
    //后执行
    @Override
    public void onCreate() {
        super.onCreate();
        HotFixDexUtils.loadFixedDex(this,"classes");
    }

    //先执行
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
