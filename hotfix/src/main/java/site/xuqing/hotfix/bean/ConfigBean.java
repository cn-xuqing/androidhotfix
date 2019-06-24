package site.xuqing.hotfix.bean;

public class ConfigBean {
    private String hotfixVersion;
    private String hotfixUrl;
    private String upgradeVersion;
    private String upgradeVersionCode;
    private String upgradeUrl;

    public String getHotfixVersion() {
        return hotfixVersion;
    }

    public void setHotfixVersion(String hotfixVersion) {
        this.hotfixVersion = hotfixVersion;
    }

    public String getHotfixUrl() {
        return hotfixUrl;
    }

    public void setHotfixUrl(String hotfixUrl) {
        this.hotfixUrl = hotfixUrl;
    }

    public String getUpgradeVersion() {
        return upgradeVersion;
    }

    public void setUpgradeVersion(String upgradeVersion) {
        this.upgradeVersion = upgradeVersion;
    }

    public String getUpgradeVersionCode() {
        return upgradeVersionCode;
    }

    public void setUpgradeVersionCode(String upgradeVersionCode) {
        this.upgradeVersionCode = upgradeVersionCode;
    }

    public String getUpgradeUrl() {
        return upgradeUrl;
    }

    public void setUpgradeUrl(String upgradeUrl) {
        this.upgradeUrl = upgradeUrl;
    }
}
