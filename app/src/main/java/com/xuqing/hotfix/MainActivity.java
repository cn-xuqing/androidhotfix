package com.xuqing.hotfix;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
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
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})
    public void requestPermission() {

    }
}
