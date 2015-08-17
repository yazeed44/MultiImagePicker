package net.yazeed44.imagepicker.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yazeed44 on 8/15/15.
 */

@Module
class AppModule {

    private final MyApplication mApp;

    public AppModule(final MyApplication app) {
        mApp = app;
    }

    @Provides
    @Singleton
    public Context provideApplicationContext() {
        return mApp;
    }


}
