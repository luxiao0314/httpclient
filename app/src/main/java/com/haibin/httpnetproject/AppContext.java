package com.haibin.httpnetproject;

import android.app.Application;

/**
 * Created by haibin on 2016/9/24.
 */

public class AppContext extends Application {

//    public static RefWatcher refWatcher;
//
//    public static RefWatcher getRefWatcher() {
//        return refWatcher;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
//        refWatcher = LeakCanary.install(this);
    }
}
