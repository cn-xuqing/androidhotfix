package site.xuqing.hotfix.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import site.xuqing.hotfix.ConfigManager;
import site.xuqing.hotfix.utils.HotFixFileUtils;

public class Web {
    public static void loadConfig(){}
    @Deprecated
    public static void getHotfixVersion(){}
    @Deprecated
    public static void getUpgradeVersion(){}
    public static void loadHotfixPackage(String hotfixUrl){
    }
    public static void loadUpgradePackage(){}

    private static void request(String url,FormBody body,okhttp3.Callback callback){
        OkHttpClient okHttpClient = new OkHttpClient();
        if (body==null) {
            FormBody.Builder builder = new FormBody.Builder();
            body = builder.build();
        }
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public enum HotFixType{
        apk,fix
    }

    private static okhttp3.Callback getDownloadCallback(final HotFixType type){
        return new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                System.out.println("下载失败...");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //输入流
                InputStream is = null;
                //输出流
                FileOutputStream fos = null;
                try {
                    //获取输入流
                    is = response.body().byteStream();
                    //获取文件大小
                    long total = response.body().contentLength();
                    if(is != null){
                        // 设置路径,apk
                        File file=null;
                        switch (type){
                            case apk:
                                file = new File(HotFixFileUtils.HOTFIX_APK_PATH, "system_app_"+ConfigManager.getUpgradeVersion()+".apk");
                                break;
                            case fix:
                                file = new File(HotFixFileUtils.HOTFIX_FIX_PATH, "system_part_"+ConfigManager.getHotfixVersion()+ConfigManager.getHotfixEndName());
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

                    }
                    fos.flush();
                    // 下载完成
                    if(fos != null){
                        fos.close();
                    }
                    System.out.println("下载成功...");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("下载失败...");
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        };
    }
}
