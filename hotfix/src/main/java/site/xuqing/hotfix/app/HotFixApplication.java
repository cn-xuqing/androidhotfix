package site.xuqing.hotfix.app;

import android.app.Application;

import site.xuqing.hotfix.Hotfix;

public class HotFixApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Hotfix.init(this);
    }
}
