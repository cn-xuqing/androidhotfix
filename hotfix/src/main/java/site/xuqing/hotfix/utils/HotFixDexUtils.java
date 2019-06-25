package site.xuqing.hotfix.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class HotFixDexUtils {
    /**
     * app应用程序根目录下的mydex文件夹，下载好的dex修复文件 会被通过io流 拷贝到这里
     */
    public static final String DEX_DIR = "mydex";
    /**
     * app应用程序路径下 自定义的文件夹。因为类加载器只能读取应用安装的路径下的文件
     */
    public static final String LOCAL_DEX_DIR = "opt_dex";
    private static HashSet<File> loadedDex = new HashSet<>();

    static {
        loadedDex.clear();
    }

    //dex命名必须使用传入的服务器获取的特定名字为前缀，该前缀生成必须为：classes_upgrade版本号_hotfix版本号
    //整个名字为：classes_upgrade版本号_hotfix版本号.dex
    public static void loadFixedDex(Context context) {
        if (context == null) {
            return;
        }
        //遍历所有要修复的dex
        File fileDir = context.getDir(DEX_DIR, Context.MODE_PRIVATE);
        //拿到这个文件夹目录中的所有文件
        File[] listFiles = fileDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().startsWith(HotFixFileUtils.getHotfixFixFileName()) && file.getName().endsWith(".dex")) {
                loadedDex.add(file);//存入集合
            }
        }
        Log.i("HotFixManager", "loadedDexList.size: " + loadedDex.size());
        //新的已修复的dex，与之前手机系统中的dex进行合并
        doDexInject(context, fileDir);
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
            String downDexFilePath = HotFixFileUtils.getHotfixFixPath()  + HotFixFileUtils.getHotfixFixFileName();
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

    private static void doDexInject(Context context, File fileDir) {
        String dirPath = fileDir.getAbsolutePath() + File.separator + LOCAL_DEX_DIR;
        Log.i("HotFixManager", "dirPath: " + dirPath);
        File copyFileDir = new File(dirPath);
        if (!copyFileDir.exists()) {
            copyFileDir.mkdirs();
        }
        try {
            PathClassLoader pathClassLoader = (PathClassLoader) context.getApplicationContext().getClassLoader();
            for (File dex : loadedDex) {
                Log.i("HotFixManager", "dex: " + dex.getAbsolutePath());
                DexClassLoader classLoader = new DexClassLoader(//
                        dex.getAbsolutePath(),// dexPath
                        copyFileDir.getAbsolutePath(),// optimizedDirectory
                        null,// libraryPath
                        pathClassLoader);// ClassLoader parent
                Object dexObj = getPathList(classLoader);
                Object pathObj = getPathList(pathClassLoader);
                Object dexElementList = getDexElements(dexObj);
                Object pathDexElementList = getDexElements(pathObj);
                Object dexElement = combineArray(dexElementList, pathDexElementList);
                Object pathList = getPathList(pathClassLoader);
                setField(pathList, pathList.getClass(), "dexElements", dexElement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object getPathList(Object baseDexClassLoader) throws Exception {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getDexElements(Object obj) throws Exception {
        return getField(obj, obj.getClass(), "dexElements");
    }

    private static Object getField(Object obj, Class<?> clazz, String fieldName) throws Exception {
        Field localField = clazz.getDeclaredField(fieldName);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    private static void setField(Object obj, Class<?> clazz, String fieldName, Object value) throws Exception {
        Field localFiled = clazz.getDeclaredField(fieldName);
        localFiled.setAccessible(true);
        localFiled.set(obj, value);
    }

    /**
     * 合并两个数组
     *
     * @param arrayLhs
     * @param arrayRhs
     * @return
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; k++) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }
}