package site.xuqing.hotfix;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;

import site.xuqing.hotfix.bean.SettingBean;
import site.xuqing.hotfix.config.ConfigManager;
import site.xuqing.hotfix.net.Web;
import site.xuqing.hotfix.net.WebListener;
import site.xuqing.hotfix.net.WebUrl;
import site.xuqing.hotfix.utils.AppInfoHelper;
import site.xuqing.hotfix.utils.HotFixDexUtils;
import site.xuqing.hotfix.utils.HotFixFileUtils;

public class Hotfix {
    private static Context applicationContext;
    private static Context mContext;
    public static void init(Context context){
        if (context!=null) {
            Hotfix.applicationContext=context.getApplicationContext();
            HotFixDexUtils.loadFixedDex(context);
        }
    }
    public static Context getApplicationContext(){
        return applicationContext;
    }
    public static void unregisterFix(){
        mContext=null;
    }
    public static void registerFix(Context context, SettingBean settingBean){
        //TODO 设置一些值
        registerFix(context);
    }
    /**
     * TODO 1.可以添加用户升级版本提醒以及用户自定义下载
     * TODO 2.需要尝试把调整安装的逻辑放至api，以减少用户的自行设置流程(完成)
     * TODO 3.文件夹需要分包存储，不然多应用使用会混乱(完成)
     * TODO 4.当apk、jar、dex下载不存在时，应当做判断
     */
    public static void registerFix(Context context) {
        mContext=context;
        final String versionCode=AppInfoHelper.getAppVersionCode();
        final String versionName=AppInfoHelper.getAppVersionName();
        final String packageName=AppInfoHelper.getPackageName();
        final String sign=AppInfoHelper.getAppMetaData();
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
                            Message msg=new Message();
                            msg.what=0;
                            msg.obj=data;
                            handler.sendMessage(msg);
                        }
                        @Override
                        public void onWebError() {
                        }
                    });
                }else if(currentVersionCode<webVersionCode&&!versionName.equals(ConfigManager.getInstance().getUpgradeVersion())){
                    String apkUrl= HotFixFileUtils.getHotfixApkPath()+ HotFixFileUtils.getHotfixApkFileName();
                    Message msg=new Message();
                    msg.what=0;
                    msg.obj=apkUrl;
                    handler.sendMessage(msg);
                }else {
                    if (ConfigManager.getInstance().isHotfix()) {
                        Web.loadHotfixPackage(WebUrl.BASE_HOST + ConfigManager.getInstance().getHotfixUrl(), new WebListener() {
                            @Override
                            public void onWebSuccess(String data) {
                                //提示重启
                                System.out.println("hotfix下载成功：" + data);
                                HotFixDexUtils.fixBug(applicationContext);
                                Message msg=new Message();
                                msg.what=1;
                                msg.obj=data;
                                handler.sendMessage(msg);
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

    static Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String path=(String)msg.obj;
            switch (msg.what){
                case 0:
                    System.out.println(">>>>>>>>>>>>>>>>>0");
                    showDialog(path);
                break;
                case 1:
                    System.out.println(">>>>>>>>>>>>>>>>>1");
                    showToast();
                break;
                default:
                    break;
            }
        }
    };

    private static void showToast(){
        Toast.makeText(mContext,"热修复的包下载完成，下次重启应用时生效...",Toast.LENGTH_SHORT).show();
    }

    private static void showDialog(final String path){
        new AlertDialog.Builder(mContext)
                .setTitle("更新提示")
                .setMessage("发现新版本："+ConfigManager.getInstance().getUpgradeVersion()+"。当前APP版本为："+AppInfoHelper.getAppVersionName()+"。是否立即更新？")
                .setNeutralButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        installApk(mContext,path);
                    }
                })
                .create()
                .show();
    }

    /**
     * 安装APK文件
     */
    private static void installApk(Context context,String path) {
        File apkFile = new File(path);
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            //兼容8.0
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                boolean hasInstallPermission = context.getPackageManager().canRequestPackageInstalls();
                if (!hasInstallPermission) {
                    //请求安装未知应用来源的权限
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 6666);
                }
            }
        } else {
            // 通过Intent安装APK文件
            intent.setDataAndType(Uri.parse("file://" + apkFile.toString()),
                    "application/vnd.android.package-archive");
        }
        if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
            context.startActivity(intent);
        }
    }
}
