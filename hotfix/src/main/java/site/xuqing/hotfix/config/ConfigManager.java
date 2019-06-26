package site.xuqing.hotfix.config;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import site.xuqing.hotfix.bean.ConfigBean;
import site.xuqing.hotfix.bean.DelayBean;
import site.xuqing.hotfix.utils.HotFixFileUtils;

public final class ConfigManager {
    private static ConfigBean configBeanOld = new ConfigBean();
    private static ConfigBean configBeanCurrent = new ConfigBean();
    private static ConfigBean configBeanTemp = new ConfigBean();
    private static DelayBean delayBean=new DelayBean();
    private static ConfigManager configManager = new ConfigManager();

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        initConfig(ConfigVersionType.OLD);
        initConfig(ConfigVersionType.CURRENT);
        initConfig(ConfigVersionType.TEMP);
        return configManager;
    }

    public String getHotfixVersion() {
        return configBeanCurrent.getHotfixVersion();
    }

    public String getUpgradeVersion() {
        return configBeanCurrent.getUpgradeVersion();
    }

    public String getUpgradeVersionCode(){
        return configBeanCurrent.getUpgradeVersionCode();
    }

    public String getHotfixEndName() {
        String hotfixUrl = configBeanCurrent.getHotfixUrl();
        if (hotfixUrl != null) {
            return hotfixUrl.substring(hotfixUrl.lastIndexOf("."));
        }
        return null;
    }

    public String getHotfixUrl() {
        return configBeanCurrent.getHotfixUrl();
    }

    public String getUpgradeUrl() {
        return configBeanCurrent.getUpgradeUrl();
    }

    public boolean getUpgradeDelay(){
        readDelayFile();
        return delayBean.isUpgradeDelay();
    }
    public void setUpgradeDelay(boolean upgradeDelay){
        delayBean.setUpgradeDelay(upgradeDelay);
        writeDelayFile();
    }

    public boolean getHotfixDelay(){
        readDelayFile();
        return delayBean.isHotfixDelay();
    }
    public void setHotfixDelay(boolean hotfixDelay){
        delayBean.setHotfixDelay(hotfixDelay);
        writeDelayFile();
    }

    public boolean isUpgrade() {
        String oldVersion = configBeanOld.getUpgradeVersion();
        String currentVersion = configBeanCurrent.getUpgradeVersion();
        if (currentVersion != null && oldVersion != null) {
            if (!oldVersion.equals(currentVersion)) {
                return true;
            }
        } else if (currentVersion != null) {
            return true;
        }
        return false;
    }

    public boolean isHotfix() {
        if (!isUpgrade()) {
            String oldVersion = configBeanOld.getHotfixVersion();
            String currentVersion = configBeanCurrent.getHotfixVersion();
            if (currentVersion != null && oldVersion != null) {
                if (!oldVersion.equals(currentVersion)) {
                    return true;
                }
            } else if (currentVersion != null) {
                return true;
            }
        }
        return false;
    }

    public boolean compareOldAndCurrentConfig(){
        String oldHotfixVersion=configBeanOld.getHotfixVersion();
        String oldUpgradeVersion=configBeanOld.getUpgradeVersion();
        String currentHotfixVersion=configBeanCurrent.getHotfixVersion();
        String currentUpgradeVersion=configBeanCurrent.getUpgradeVersion();
        if (oldHotfixVersion==null
                ||oldUpgradeVersion==null
                ||!oldHotfixVersion.equals(currentHotfixVersion)
                ||!oldUpgradeVersion.equals(currentUpgradeVersion)){
            return false;
        }
        return true;
    }

    /**
     * 相等为true不想等为false
     * @return
     */
    public boolean compareTempAndCurrentConfig(){
        String tempHotfixVersion=configBeanTemp.getHotfixVersion();
        String tempUpgradeVersion=configBeanTemp.getUpgradeVersion();
        String currentHotfixVersion=configBeanCurrent.getHotfixVersion();
        String currentUpgradeVersion=configBeanCurrent.getUpgradeVersion();
        if (currentHotfixVersion==null
                ||currentUpgradeVersion==null
                ||!currentHotfixVersion.equals(tempHotfixVersion)
                ||!currentUpgradeVersion.equals(tempUpgradeVersion)){
            return false;
        }
        return true;
    }

    private enum ConfigVersionType {
        /**
         * 老版本
         */
        OLD,
        /**
         * 新版本
         */
        CURRENT,
        /**
         * 缓存版本
         */
        TEMP
    }

    private static void initConfig(ConfigVersionType type) {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            File file = null;
            switch (type) {
                case OLD:
                    file = new File(HotFixFileUtils.getHotfixBasePath() + HotFixFileUtils.getHotfixOldConfigFileName());
                    break;
                case CURRENT:
                    file = new File(HotFixFileUtils.getHotfixBasePath() + HotFixFileUtils.getHotfixCurrentConfigFileName());
                    break;
                case TEMP:
                    file = new File(HotFixFileUtils.getHotfixBasePath() + HotFixFileUtils.getHotfixTempConfigFileName());
                    break;
                default:
                    break;
            }
            if (file.exists()) {
                inputStream = new FileInputStream(file);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String jsonStr = stringBuilder.toString();
                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONObject jsonObjectData = jsonObject.getJSONObject("data");
                JSONObject jsonObjectHotfix = jsonObjectData.getJSONObject("hotfix");
                JSONObject jsonObjectUpgrade = jsonObjectData.getJSONObject("upgrade");
                switch (type) {
                    case OLD:
                        configBeanOld.setHotfixVersion(jsonObjectHotfix.getString("version"));
                        configBeanOld.setHotfixUrl(jsonObjectHotfix.getString("fixUrl"));
                        configBeanOld.setUpgradeVersion(jsonObjectUpgrade.getString("version"));
                        configBeanOld.setUpgradeVersionCode(jsonObjectUpgrade.getString("versionCode"));
                        configBeanOld.setUpgradeUrl(jsonObjectUpgrade.getString("apkUrl"));
                        break;
                    case CURRENT:
                        configBeanCurrent.setHotfixVersion(jsonObjectHotfix.getString("version"));
                        configBeanCurrent.setHotfixUrl(jsonObjectHotfix.getString("fixUrl"));
                        configBeanCurrent.setUpgradeVersion(jsonObjectUpgrade.getString("version"));
                        configBeanCurrent.setUpgradeVersionCode(jsonObjectUpgrade.getString("versionCode"));
                        configBeanCurrent.setUpgradeUrl(jsonObjectUpgrade.getString("apkUrl"));
                        break;
                    case TEMP:
                        configBeanTemp.setHotfixVersion(jsonObjectHotfix.getString("version"));
                        configBeanTemp.setHotfixUrl(jsonObjectHotfix.getString("fixUrl"));
                        configBeanTemp.setUpgradeVersion(jsonObjectUpgrade.getString("version"));
                        configBeanTemp.setUpgradeVersionCode(jsonObjectUpgrade.getString("versionCode"));
                        configBeanTemp.setUpgradeUrl(jsonObjectUpgrade.getString("apkUrl"));
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeDelayFile(){
        BufferedWriter bufferedWriter=null;
        OutputStream outputStream=null;
        try {
            File file=new File(HotFixFileUtils.getHotfixBasePath(),HotFixFileUtils.getHotfixDelayFileName());
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("upgradeDelay",delayBean.isUpgradeDelay());
            jsonObject.put("hotfixDelay",delayBean.isHotfixDelay());
            outputStream=new FileOutputStream(file);
            bufferedWriter=new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write(jsonObject.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void readDelayFile(){
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            File file=new File(HotFixFileUtils.getHotfixBasePath()+HotFixFileUtils.getHotfixDelayFileName());
            if (file.exists()){
                inputStream = new FileInputStream(file);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String jsonStr = stringBuilder.toString();
                JSONObject jsonObject = new JSONObject(jsonStr);
                delayBean.setUpgradeDelay(jsonObject.getBoolean("upgradeDelay"));
                delayBean.setHotfixDelay(jsonObject.getBoolean("hotfixDelay"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
