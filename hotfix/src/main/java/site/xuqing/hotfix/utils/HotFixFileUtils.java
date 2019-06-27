package site.xuqing.hotfix.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import site.xuqing.hotfix.config.ConfigManager;

public final class HotFixFileUtils {
    private static final String HOTFIX_OLD_CONFIG_FILE_NAME="system_config_old.txt";
    private static final String HOTFIX_TEMP_CONFIG_FILE_NAME="system_config_temp.txt";
    private static final String HOTFIX_CURRENT_CONFIG_FILE_NAME="system_config.txt";
    private static final String HOTFIX_APK_FILE_FIRST_NAME="system_app_";
    private static final String HOTFIX_FIX_FILE_FIRST_NAME="system_part_";
    private static final String HOTFIX_DELAY_FILE_NAME="system_delay_flag.txt";
    private static final String HOTFIX_LENGTH_FILE_NAME="system_length_flag.txt";
    private static final String HOTFIX_BASE_PATH=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"XQHotFix/";
    public static String getHotfixBasePath(){
        String path=HOTFIX_BASE_PATH+AppInfoHelper.getPackageName()+"/";
        File apkPath=new File(path);
        if (!apkPath.exists()){
            apkPath.mkdirs();
        }
        return path;
    }
    public static String getHotfixFixPath(){
        String path=getHotfixBasePath()+"hotfix/";
        File fixPath=new File(path);
        if (!fixPath.exists()){
            fixPath.mkdirs();
        }
        return path;
    }
    public static String getHotfixApkPath(){
        String path=getHotfixBasePath()+"apk/";
        File apkPath=new File(path);
        if (!apkPath.exists()){
            apkPath.mkdirs();
        }
        return path;
    }

    public static String getHotfixApkFileName(){
        return HOTFIX_APK_FILE_FIRST_NAME + ConfigManager.getInstance().getUpgradeVersion() + ".apk";
    }

    public static String getHotfixFixFileName(){
        return HOTFIX_FIX_FILE_FIRST_NAME + ConfigManager.getInstance().getHotfixVersion() + ConfigManager.getInstance().getHotfixEndName();
    }

    public static String getHotfixCurrentConfigFileName(){
        return HOTFIX_CURRENT_CONFIG_FILE_NAME;
    }

    public static String getHotfixOldConfigFileName(){
        return HOTFIX_OLD_CONFIG_FILE_NAME;
    }

    public static String getHotfixTempConfigFileName(){
        return HOTFIX_TEMP_CONFIG_FILE_NAME;
    }

    public static String getHotfixDelayFileName(){
        return HOTFIX_DELAY_FILE_NAME;
    }

    public static String getHotfixLengthFileName(){
        return HOTFIX_LENGTH_FILE_NAME;
    }
}
