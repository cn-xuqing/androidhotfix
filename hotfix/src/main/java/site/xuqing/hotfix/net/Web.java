package site.xuqing.hotfix.net;

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

/**
 * @hide
 */
public class Web {
    public static void loadConfig(String configUrl, String appPackage,String sign, WebListener webListener) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("appPackage",appPackage);
        builder.add("sign",sign);
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
    }

    public enum HotFixType {
        config, apk, fix
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
                                file = new File(HotFixFileUtils.getHotfixApkPath(), "system_app_" + ConfigManager.getUpgradeVersion() + ".apk");
                                break;
                            case fix:
                                file = new File(HotFixFileUtils.getHotfixFixPath(), "system_part_" + ConfigManager.getHotfixVersion() + ConfigManager.getHotfixEndName());
                                break;
                            case config:
                                file=new File(HotFixFileUtils.getHotfixBasePath(), "system_config.json");
                                break;
                            default:
                                break;
                        }
                        if (!file.exists()){
                            file.mkdir();
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

    @Deprecated
    private static okhttp3.Callback getConfigCallback(final WebListener webListener) {
        return new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                System.out.println("请求失败...");
                webListener.onWebError();
            }

            @Override
            public void onResponse(Call call, Response response) {
                //输出流
                FileOutputStream fos = null;
                try {
                    String content = response.body().toString();
                    System.out.println("请求成功:" + content);
                    File file = new File(HotFixFileUtils.getHotfixBasePath(), "system_config.json");
                    fos = new FileOutputStream(file);
                    fos.write(content.getBytes(), 0, content.getBytes().length);
                    webListener.onWebSuccess(file.getAbsolutePath());
                    fos.flush();
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("解析失败...");
                    webListener.onWebError();
                } finally {
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
