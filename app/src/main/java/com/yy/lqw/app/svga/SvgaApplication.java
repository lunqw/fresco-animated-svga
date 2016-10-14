package com.yy.lqw.app.svga;

import android.app.Application;

import com.facebook.common.logging.FLog;
import com.yy.lqw.fresco.svga.FrescoWrapper;

/**
 * Created by lunqingwen on 2016/10/13.
 */

public class SvgaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FrescoWrapper.initialize(this);
        FLog.setMinimumLoggingLevel(FLog.VERBOSE);
    }
}
