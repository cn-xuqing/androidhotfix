package com.xuqing.hotfix;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.List;
import java.util.Map;

import site.xuqing.hotfix.Hotfix;
import site.xuqing.hotfix.listener.OnHotfixListener;
import site.xuqing.hotfix.listener.OnUpgradeListener;

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
        Hotfix.getInstance().setOnUpgradeListener(new OnUpgradeListener() {
            @Override
            public void onUpgrade(String apkPath) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>"+apkPath);
                Toast.makeText(MainActivity.this,"APP更新："+apkPath,Toast.LENGTH_SHORT).show();
                /**
                 * 此处添加升级提醒和跳转用户升级界面
                 * 由于7.0以上系统的文件打开需要在AndroidManifest.xml文件中配置Provider，故留给用户APP自行配置暴露
                 */
            }
        });
        Hotfix.getInstance().setOnHotfixListener(new OnHotfixListener() {
            @Override
            public void onHotfix(String hotfixPath) {
                Toast.makeText(MainActivity.this,"热修复："+hotfixPath,Toast.LENGTH_SHORT).show();
            }
        });
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
}
