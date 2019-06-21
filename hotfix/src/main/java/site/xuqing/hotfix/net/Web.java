package site.xuqing.hotfix.net;

import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import site.xuqing.hotfix.config.ConfigManager;
import site.xuqing.hotfix.utils.HotFixFileUtils;

/**
 * @hide
 */
public class Web {
    public static void loadConfig(String configUrl, String appPackage, String sign, WebListener webListener) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("appPackage", appPackage);
        builder.add("sign", sign);
        getRequest(configUrl, builder.build(), getDownloadCallback(HotFixType.config, webListener));
    }

    public static void loadHotfixPackage(String hotfixUrl, WebListener webListener) {
        getRequest(hotfixUrl, null, getDownloadCallback(HotFixType.fix, webListener));
    }

    public static void loadUpgradePackage(String apkUrl, WebListener webListener) {
        getRequest(apkUrl, null, getDownloadCallback(HotFixType.apk, webListener));
    }

    private static void getRequest(String url, FormBody body, okhttp3.Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        if (body == null) {
            FormBody.Builder builder = new FormBody.Builder();
            body = builder.build();
        }
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
        System.out.println("URL:"+url);
    }

    private enum HotFixType {
        /**
         * 配置文件
         */
        config,
        /**
         * apk包
         */
        apk,
        /**
         * fix文件
         */
        fix
    }

    private static okhttp3.Callback getDownloadCallback(final HotFixType type, final WebListener webListener) {
        return new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                System.out.println("下载失败...");
                webListener.onWebError();
            }

            @Override
            public void onResponse(Call call, Response response) {
                //输入流
                InputStream is = null;
                //输出流
                FileOutputStream fos = null;
                try {
                    //获取输入流
                    is = response.body().byteStream();
                    //获取文件大小
                    long total = response.body().contentLength();
                    if (is != null) {
                        // 设置路径,apk
                        File file = null;
                        switch (type) {
                            case apk:
                                file = new File(HotFixFileUtils.getHotfixApkPath(), HotFixFileUtils.getHotfixApkFileName());
                                break;
                            case fix:
                                file = new File(HotFixFileUtils.getHotfixFixPath(), HotFixFileUtils.getHotfixFixFileName());
                                break;
                            case config:
                                file = new File(HotFixFileUtils.getHotfixBasePath(), HotFixFileUtils.getHotfixTempConfigFileName());
                                break;
                            default:
                                break;
                        }
                        fos = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int ch = -1;
                        int process = 0;
                        while ((ch = is.read(buf)) != -1) {
                            fos.write(buf, 0, ch);
                            process += ch;
                            //这里可以添加更新进度
                        }
                        if (type==HotFixType.config) {
                            //这里做文件拷贝
                            if (!ConfigManager.getInstance().compareOldAndCurrentConfig()) {
                                File fileCurrent = new File(HotFixFileUtils.getHotfixBasePath() + HotFixFileUtils.getHotfixCurrentConfigFileName());
                                File fileOld = new File(HotFixFileUtils.getHotfixBasePath(), HotFixFileUtils.getHotfixOldConfigFileName());
                                copyRenameFile(fileCurrent, fileOld);
                            }
                            if (!ConfigManager.getInstance().compareTempAndCurrentConfig()) {
                                File fileTemp = new File(HotFixFileUtils.getHotfixBasePath() + HotFixFileUtils.getHotfixTempConfigFileName());
                                File fileCurrent = new File(HotFixFileUtils.getHotfixBasePath(), HotFixFileUtils.getHotfixCurrentConfigFileName());
                                copyRenameFile(fileTemp, fileCurrent);
                            }
                        }
                        webListener.onWebSuccess(file.getAbsolutePath());
                    }
                    fos.flush();
                    if (fos != null) {
                        fos.close();
                    }
                    System.out.println("下载成功...");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("下载失败...");
                    webListener.onWebError();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * 拷贝并重命名文件
     * @param fileIn 拷贝和重命名的文件
     * @param fileOut 目标文件
     */
    private static void copyRenameFile(File fileIn,File fileOut){
        FileInputStream inCopy=null;
        FileOutputStream outCopy=null;
        try{
            if (fileIn.exists()){
                inCopy=new FileInputStream(fileIn);
                outCopy=new FileOutputStream(fileOut);
                byte[] buf = new byte[1024];
                int ch = -1;
                while ((ch = inCopy.read(buf)) != -1) {
                    outCopy.write(buf, 0, ch);
                }
            }
            if (outCopy != null) {
                outCopy.close();
            }
            if (inCopy!=null){
                inCopy.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if (inCopy!=null){
                    inCopy.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                if (outCopy!=null){
                    outCopy.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
