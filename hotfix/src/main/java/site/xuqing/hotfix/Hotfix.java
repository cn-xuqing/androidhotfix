package site.xuqing.hotfix;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

import site.xuqing.hotfix.bean.SettingConfig;
import site.xuqing.hotfix.config.ConfigManager;
import site.xuqing.hotfix.net.Web;
import site.xuqing.hotfix.net.WebListener;
import site.xuqing.hotfix.net.WebUrl;
import site.xuqing.hotfix.utils.AppInfoHelper;
import site.xuqing.hotfix.utils.HotFixDexUtils;
import site.xuqing.hotfix.utils.HotFixFileUtils;

/**
 * @author xuqing
 */
public final class Hotfix {
    private static Context applicationContext;
    private static WeakReference<Activity> mActivityReference;
    private static SettingConfig settingConfig;
    private static Handler mHandler;

    public static void init(Context context) {
        if (context != null) {
            Hotfix.applicationContext = context.getApplicationContext();
            HotFixDexUtils.loadFixedDex(context);
        }
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }

    public static void unregisterFix() {
        if (mActivityReference != null) {
            mActivityReference.clear();
            mActivityReference = null;
        }
    }

    public static void registerFix(Activity activity, SettingConfig settingConfig) {
        if (settingConfig != null) {
            Hotfix.settingConfig = settingConfig;
        }
        registerFix(activity);
    }

    public static void registerFix(Activity activity) {
        mActivityReference = new WeakReference<>(activity);
        if (mHandler == null) {
            final Activity mActivity = mActivityReference.get();
            if (mActivity != null) {
                mHandler = new MyHandler(mActivity);
            }
        }
        final String versionCode = AppInfoHelper.getAppVersionCode();
        final String versionName = AppInfoHelper.getAppVersionName();
        final String packageName = AppInfoHelper.getPackageName();
        final String sign = AppInfoHelper.getAppMetaData(AppInfoHelper.META_DATA_SIGN_KEY);
        Web.loadConfig(WebUrl.CONFIG_URL, packageName, sign, new WebListener() {
            @Override
            public void onWebSuccess(String data) {
                System.out.println(data);
                int webVersionCode = -999;
                int currentVersionCode = 0;
                try {
                    webVersionCode = Integer.parseInt(ConfigManager.getInstance().getUpgradeVersionCode());
                    currentVersionCode = Integer.parseInt(versionCode);
                } catch (Exception e) {
                    e.printStackTrace();
                    //如果数据解析报错，证明保存的config文件出错，则不再进行下一步
                    //此处会产生bug，当没有apk信息时会报错，导致hotfix包无法下载
                    //return;
                }
                //如果apk下载被延迟，或者是可以升级，则跳转对话框下载或者直接下载
                if (ConfigManager.getInstance().getUpgradeDelay() || ConfigManager.getInstance().isUpgrade()) {
                    downloadApkShowDialog();
                //如果版本号大于当前版本号，且版本名不想等
                } else if (currentVersionCode < webVersionCode && !versionName.equals(ConfigManager.getInstance().getUpgradeVersion())) {
                    String apkUrl = HotFixFileUtils.getHotfixApkPath() + HotFixFileUtils.getHotfixApkFileName();
                    File apkFile=new File(apkUrl);
                    System.out.println("本地apk文件大小："+apkFile.length());
                    //首先判断apk是否已被下载，不存在先去下载，如果文件存在，但是大小和网络大小不符，则去下载
                    if (!apkFile.exists()
                            ||(int)apkFile.length()==0
                            ||(ConfigManager.getInstance().getUpgradeLength()!=0&&apkFile.length()!=ConfigManager.getInstance().getUpgradeLength())){
                        downloadApkShowDialog();
                    }else {
                        //如果apk已被下载，则提示安装或者直接安装
                        installApkShowDialog(apkUrl);
                    }
                    //如果热更新下载被延迟，或者可以进行热更新，则提示下载热更新文件或者直接下载热更新文件，校验文件的大小
                } else if (ConfigManager.getInstance().getHotfixDelay() || ConfigManager.getInstance().isHotfix()) {
                    String fixUrl=HotFixFileUtils.getHotfixFixPath()+HotFixFileUtils.getHotfixFixFileName();
                    File fixFile=new File(fixUrl);
                    System.out.println("本地fix文件大小："+fixFile.length());
                    if (!fixFile.exists()
                            ||(int)fixFile.length()==0
                            ||(ConfigManager.getInstance().getHotfixLength()!=0&&fixFile.length()!=ConfigManager.getInstance().getHotfixLength())) {
                        downloadHotfixShowDialog();
                    }else{
                        installFix(fixUrl);
                    }
                }
            }

            @Override
            public void onWebError() {
            }
        });
    }

    static class MyHandler extends Handler {
        WeakReference<Activity> mActivityReference;

        MyHandler(Activity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final Activity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case 0:
                        System.out.println(">>>>>>>>>>>>>>>>>0");
                        showInstallApkDialog((String) msg.obj);
                        break;
                    case 1:
                        System.out.println(">>>>>>>>>>>>>>>>>1");
                        showToast();
                        break;
                    case 2:
                        System.out.println(">>>>>>>>>>>>>>>>>2");
                        showMessageDialog((FixType) msg.obj);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static void downloadHotfixShowDialog(){
        if (settingConfig != null && settingConfig.isShowDownLoadHotfixMessage()) {
            Message msg = new Message();
            msg.what = 2;
            msg.obj = FixType.fix;
            mHandler.sendMessage(msg);
        } else {
            downloadFix();
        }
    }

    private static void downloadApkShowDialog(){
        if (settingConfig != null && settingConfig.isShowDownLoadUpgradeMessage()) {
            Message msg = new Message();
            msg.what = 2;
            msg.obj = FixType.apk;
            mHandler.sendMessage(msg);
        } else {
            downloadApk();
        }
    }

    private static void installApkShowDialog(String data){
        if (settingConfig != null && settingConfig.isShowInstallApkMessage()) {
            Message msg = new Message();
            msg.what = 0;
            msg.obj = data;
            mHandler.sendMessage(msg);
        } else {
            final Activity mActivity = mActivityReference.get();
            if (mActivity != null) {
                installApk(mActivity, data);
            }
        }
    }

    private static void showToast() {
        final Activity mActivity = mActivityReference.get();
        if (mActivity != null) {
            Toast.makeText(mActivity, "热修复的包下载完成，下次重启应用时生效...", Toast.LENGTH_SHORT).show();
        }
    }

    private static void showInstallApkDialog(final String path) {
        final Activity mActivity = mActivityReference.get();
        if (mActivity != null) {
            new AlertDialog.Builder(mActivity)
                    .setTitle("安装提示")
                    .setMessage("发现新版本：" + ConfigManager.getInstance().getUpgradeVersion() + "。当前APP版本为：" + AppInfoHelper.getAppVersionName() + "。是否立即更新？")
                    .setNeutralButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            installApk(mActivity, path);
                        }
                    })
                    .create()
                    .show();
        }
    }

    /**
     * 安装APK文件
     */
    private static void installApk(Context context, String path) {
        File apkFile = new File(path);
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(context, AppInfoHelper.getAppMetaData(AppInfoHelper.META_DATA_PROVIDER_KEY)+"."+BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            //兼容8.0，26
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    private static void installFix(String data){
        HotFixDexUtils.fixBug(applicationContext);
        if (settingConfig != null && settingConfig.isShowFixCompleteMessage()) {
            Message msg = new Message();
            msg.what = 0;
            msg.obj = data;
            mHandler.sendMessage(msg);
        }
    }

    private static void showMessageDialog(final FixType type) {
        final Activity mActivity = mActivityReference.get();
        if (mActivity != null) {
            String message = "";
            switch (type) {
                case fix:
                    message = "发现新的热更新：" + ConfigManager.getInstance().getHotfixVersion() + "。是否立即下载？";
                    break;
                case apk:
                    message = "发现新版本：" + ConfigManager.getInstance().getUpgradeVersion() + "。当前APP版本为：" + AppInfoHelper.getAppVersionName() + "。是否立即下载？";
                    break;
                default:
                    break;
            }
            new AlertDialog.Builder(mActivity)
                    .setTitle("更新提示")
                    .setMessage(message)
                    .setCancelable(false)
                    .setNeutralButton("马上下载", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            switch (type) {
                                case fix:
                                    downloadFix();
                                    ConfigManager.getInstance().setHotfixDelay(false);
                                    break;
                                case apk:
                                    downloadApk();
                                    ConfigManager.getInstance().setUpgradeDelay(false);
                                    break;
                                default:
                                    break;
                            }
                        }
                    })
                    .setNegativeButton("稍后提醒", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            switch (type) {
                                case fix:
                                    ConfigManager.getInstance().setHotfixDelay(true);
                                    break;
                                case apk:
                                    ConfigManager.getInstance().setUpgradeDelay(true);
                                    break;
                                default:
                                    break;
                            }
                        }
                    })
                    .create()
                    .show();
        }
    }

    private static void downloadFix() {
        Web.loadHotfixPackage(WebUrl.BASE_HOST + ConfigManager.getInstance().getHotfixUrl(), new WebListener() {
            @Override
            public void onWebSuccess(String data) {
                System.out.println("hotfix下载成功：" + data);
                installFix(data);
            }

            @Override
            public void onWebError() {
            }
        });
    }

    private static void downloadApk() {
        Web.loadUpgradePackage(WebUrl.BASE_HOST + ConfigManager.getInstance().getUpgradeUrl(), new WebListener() {
            @Override
            public void onWebSuccess(final String data) {
                System.out.println(data);
                installApkShowDialog(data);
            }

            @Override
            public void onWebError() {
            }
        });
    }

    private enum FixType {
        /**
         * 更新apk
         */
        apk,
        /**
         * 热更新
         */
        fix
    }
}
