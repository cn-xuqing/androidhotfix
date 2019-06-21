package site.xuqing.hotfix.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import site.xuqing.hotfix.config.ConfigManager;

public class HotFixFileUtils {
    private static final String HOTFIX_OLD_CONFIG_FILE_NAME="system_config_old.txt";
    private static final String HOTFIX_TEMP_CONFIG_FILE_NAME="system_config_temp.txt";
    private static final String HOTFIX_CURRENT_CONFIG_FILE_NAME="system_config.txt";
    private static final String HOTFIX_APK_FILE_FIRST_NAME="system_app_";
    private static final String HOTFIX_FIX_FILE_FIRST_NAME="system_part_";
    private static final String HOTFIX_BASE_PATH=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"XQHotFix/";
    private static final String HOTFIX_FIX_PATH=HOTFIX_BASE_PATH+"hotfix/";
    private static final String HOTFIX_APK_PATH=HOTFIX_BASE_PATH+"apk/";
    public static String getHotfixBasePath(){
        File apkPath=new File(HOTFIX_BASE_PATH);
        if (!apkPath.exists()){
            apkPath.mkdirs();
        }
        return HOTFIX_BASE_PATH;
    }
    public static String getHotfixFixPath(){
        File fixPath=new File(HOTFIX_FIX_PATH);
        if (!fixPath.exists()){
            fixPath.mkdirs();
        }
        return HOTFIX_FIX_PATH;
    }
    public static String getHotfixApkPath(){
        File apkPath=new File(HOTFIX_APK_PATH);
        if (!apkPath.exists()){
            apkPath.mkdirs();
        }
        return HOTFIX_APK_PATH;
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

    public static void fixBug(Context context) {
        // 对应目录 /data/data/packageName/mydex/classes2.dex
        File fileDir = context.getDir(HotFixDexUtils.DEX_DIR, Context.MODE_PRIVATE);
        String filePath = fileDir.getAbsolutePath() + File.separator + HotFixFileUtils.getHotfixFixFileName();
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            //下载已修复的dex，保存在 SD卡路径根目录下的 /01Sinya/classes2.dex
            String downDexFilePath = HOTFIX_FIX_PATH  + HotFixFileUtils.getHotfixFixFileName();
            inputStream = new FileInputStream(downDexFilePath);
            fileOutputStream = new FileOutputStream(filePath);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = inputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, len);
            }
            File newFile = new File(filePath);
            Log.i("HotFixManager",filePath);
            if (newFile.exists()) {
                Log.i("HotFixManager","dex 迁移成功");
            }
            //热修复
            HotFixDexUtils.loadFixedDex(context);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("HotFixManager", e.toString());
        }finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e2) {
                Log.i("HotFixManager", e2.toString());
            }
        }
    }
}
