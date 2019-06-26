package com.xuqing.hotfix;

import site.xuqing.hotfix.Hotfix;
import site.xuqing.hotfix.app.HotFixApplication;

public class MyApplication extends HotFixApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Hotfix.init(this);
    }
}
