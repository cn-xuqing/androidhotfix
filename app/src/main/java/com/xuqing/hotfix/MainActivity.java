package com.xuqing.hotfix;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.List;

import site.xuqing.hotfix.Hotfix;
import site.xuqing.hotfix.bean.SettingConfig;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Class<?> clz = Class.forName("com.xuqing.hotfix.Test");
                    Test impl = (Test) clz.newInstance();//通过该方法得到IShowToast类
                    impl.run();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.fix).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fixBug("testclasses.dex");
                //fixBug("classes2.dex");
            }
        });

        requestPermission();
        SettingConfig settingConfig=new SettingConfig();
        /**
         * 是否显示下载热更新提示
         */
        settingConfig.setShowDownLoadHotfixMessage(true);
        /**
         * 是否显示下载安装包提示
         */
        settingConfig.setShowDownLoadUpgradeMessage(true);
        /**
         * 是否显示修复完成相关提示
         */
        settingConfig.setShowFixCompleteMessage(true);
        /**
         * 是否显示安装确认提示
         */
        settingConfig.setShowInstallApkMessage(true);
        Hotfix.registerFix(this,settingConfig);
    }

    private void requestPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {

                    }
                }).onDenied(new Action<List<String>>() {

            @Override
            public void onAction(List<String> permissions) {
            }
        })
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Hotfix.unregisterFix();
    }
}
