# AndroidHotfix
### 项目网站：[hotfix.51bugs.com](http://hotfix.51bugs.com)
## 一、项目介绍
本项目旨在为了方便Android开发者快速的接入热更新已及apk升级功能。
- 支持apk下载提示安装升级。
- 支持热更新快速修复bug。
## 二、快速使用
### 1.APP配置
- 项目配置，在project下的build.gradle文件中添加：
```groovy
repositories {
   maven { url 'https://jitpack.io' }
}
```
- 项目依赖：
```groovy
dependencies {
   implementation 'com.github.cn-xuqing:androidhotfix:2.2.1'
}
```
- APP的AndroidManifest.xml中配置：
```xml
<application
    <meta-data
        android:name="site.xuqing.hotfix.META_DATA_KEY"
        android:value="填写你APP的sign值" />
</application>
```
- [sign值获取](http://hotfix.51bugs.com)
- 加入相关权限
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```
- 使用
首先初始化Hotfix，有两种方式：

（1）你APP的Application继承HotFixApplication.
```java
public class MyApplication extends HotFixApplication {}
```
（2）在你的APP的Application的onCreate中添加：
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Hotfix.init(this);
    }
}
```
然后注册Hotfix，在你的activity中的onCreate中注册和取消注册:
```java
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Hotfix.registerFix(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Hotfix.unregisterFix();
    }
}
```
## 三、高级设置
- 对apk下载提示与选择的设置。
- 对热更新包的下载提示与选择的设置。
- 对apk更新时用户安装的提示与询问框设置。
- 对热更新完成时候提示给用户的设置。
```java
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        SettingConfig settingConfig=new SettingConfig();
        /**
         * 是否显示下载热更新提示
         */
        settingConfig.setShowDownLoadHotfixMessage(true);
        /**
         * 是否显示下载安装包提示
         */
        settingConfig.setShowDownLoadUpgradeMessage(true);
        /**
         * 是否显示修复完成相关提示
         */
        settingConfig.setShowFixCompleteMessage(true);
        /**
         * 是否显示安装确认提示
         */
        settingConfig.setShowInstallApkMessage(true);
        Hotfix.registerFix(this,settingConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Hotfix.unregisterFix(); 
    }
}
```
## 四、版权
```
Copyright 2019 XuQing

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
```
