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
                }
                if (ConfigManager.getInstance().getUpgradeDelay() || ConfigManager.getInstance().isUpgrade()) {
                    if (settingConfig != null && settingConfig.isShowDownLoadUpgradeMessage()) {
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = FixType.apk;
                        mHandler.sendMessage(msg);
                    } else {
                        downloadApk();
                    }
                } else if (currentVersionCode < webVersionCode && !versionName.equals(ConfigManager.getInstance().getUpgradeVersion())) {
                    String apkUrl = HotFixFileUtils.getHotfixApkPath() + HotFixFileUtils.getHotfixApkFileName();
                    if (settingConfig != null && settingConfig.isShowInstallApkMessage()) {
                        Message msg = new Message();
                        msg.what = 0;
                        msg.obj = apkUrl;
                        mHandler.sendMessage(msg);
                    } else {
                        final Activity mActivity = mActivityReference.get();
                        if (mActivity != null) {
                            installApk(mActivity, apkUrl);
                        }
                    }
                } else if (ConfigManager.getInstance().getHotfixDelay() || ConfigManager.getInstance().isHotfix()) {
                    if (settingConfig != null && settingConfig.isShowDownLoadHotfixMessage()) {
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = FixType.fix;
                        mHandler.sendMessage(msg);
                    } else {
                        downloadFix();
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
                HotFixDexUtils.fixBug(applicationContext);
                if (settingConfig != null && settingConfig.isShowFixCompleteMessage()) {
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                }
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
