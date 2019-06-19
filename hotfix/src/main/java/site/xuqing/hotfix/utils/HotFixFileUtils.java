package site.xuqing.hotfix.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class HotFixFileUtils {
    public static final String HOTFIX_BASE_PATH=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"SystemHotFix/";
    public static final String HOTFIX_FIX_PATH=HOTFIX_BASE_PATH+"hotfix/";
    public static final String HOTFIX_APK_PATH=HOTFIX_BASE_PATH+"apk/";
    static{
        File fixPath=new File(HOTFIX_FIX_PATH);
        if (!fixPath.exists()){
            fixPath.mkdirs();
        }
        File apkPath=new File(HOTFIX_APK_PATH);
        if (!apkPath.exists()){
            apkPath.mkdirs();
        }
    }
    public static void fixBug(Context context,String name) {
        // 对应目录 /data/data/packageName/mydex/classes2.dex
        File fileDir = context.getDir(HotFixDexUtils.DEX_DIR, Context.MODE_PRIVATE);
        String filePath = fileDir.getAbsolutePath() + File.separator + name;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            //下载已修复的dex，保存在 SD卡路径根目录下的 /01Sinya/classes2.dex
            String downDexFilePath = HOTFIX_FIX_PATH + name;
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
            HotFixDexUtils.loadFixedDex(context,name);
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
