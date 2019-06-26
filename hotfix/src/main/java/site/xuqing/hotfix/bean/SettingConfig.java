package site.xuqing.hotfix.bean;

public final class SettingConfig {
    /**
     * 是否显示下载安装包提示
     */
    private boolean isShowDownLoadUpgradeMessage;
    /**
     * 是否显示下载热更新提示
     */
    private boolean isShowDownLoadHotfixMessage;
    /**
     * 是否显示安装确认提示
     */
    private boolean isShowInstallApkMessage;
    /**
     * 是否显示修复完成相关提示
     */
    private boolean isShowFixCompleteMessage;

    public boolean isShowDownLoadUpgradeMessage() {
        return isShowDownLoadUpgradeMessage;
    }

    public void setShowDownLoadUpgradeMessage(boolean showDownLoadUpgradeMessage) {
        isShowDownLoadUpgradeMessage = showDownLoadUpgradeMessage;
    }

    public boolean isShowDownLoadHotfixMessage() {
        return isShowDownLoadHotfixMessage;
    }

    public void setShowDownLoadHotfixMessage(boolean showDownLoadHotfixMessage) {
        isShowDownLoadHotfixMessage = showDownLoadHotfixMessage;
    }

    public boolean isShowInstallApkMessage() {
        return isShowInstallApkMessage;
    }

    public void setShowInstallApkMessage(boolean showInstallApkMessage) {
        isShowInstallApkMessage = showInstallApkMessage;
    }

    public boolean isShowFixCompleteMessage() {
        return isShowFixCompleteMessage;
    }

    public void setShowFixCompleteMessage(boolean showFixCompleteMessage) {
        isShowFixCompleteMessage = showFixCompleteMessage;
    }
}
