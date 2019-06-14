package site.xuqing.hotfix;

import android.content.Context;

import site.xuqing.hotfix.utils.HotFixFileUtils;

public class HotFixManager {
    public static void init(Context context){
        HotFixFileUtils.fixBug(context,"classes2.dex");
    }
}
