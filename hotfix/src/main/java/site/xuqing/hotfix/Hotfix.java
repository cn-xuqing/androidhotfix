package site.xuqing.hotfix;

import site.xuqing.hotfix.app.HotFixApplication;
import site.xuqing.hotfix.listener.OnHotfixListener;
import site.xuqing.hotfix.listener.OnUpgradeListener;

public class Hotfix {
    private OnUpgradeListener onUpgradeListener;
    private OnHotfixListener onHotfixListener;
    private static Hotfix hotfix=new Hotfix();
    private Hotfix(){}
    public static Hotfix getInstance(){
        return hotfix;
    }
    public void setOnUpgradeListener(OnUpgradeListener onUpgradeListener){
        this.onUpgradeListener=onUpgradeListener;
        if (HotFixApplication.getData(HotFixApplication.KEY_APK)!=null){
            onUpgradeListener.onUpgrade(HotFixApplication.getData(HotFixApplication.KEY_APK));
        }
    }

    public void setOnHotfixListener(OnHotfixListener onHotfixListener){
        this.onHotfixListener=onHotfixListener;
        if (HotFixApplication.getData(HotFixApplication.KEY_FIX)!=null){
            onHotfixListener.onHotfix(HotFixApplication.getData(HotFixApplication.KEY_FIX));
        }
    }

    public OnUpgradeListener getOnUpgradeListener() {
        return onUpgradeListener;
    }

    public OnHotfixListener getOnHotfixListener() {
        return onHotfixListener;
    }
}
