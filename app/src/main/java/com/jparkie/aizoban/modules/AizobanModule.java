package com.jparkie.aizoban.modules;

import android.app.Application;

import com.jparkie.aizoban.AizobanApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        includes = {
                DataModule.class
        },
        injects = {
                AizobanApplication.class
        }
)
public final class AizobanModule {
    public static final String TAG = AizobanModule.class.getSimpleName();

    private final AizobanApplication mApplication;

    public AizobanModule(AizobanApplication application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return mApplication;
    }
}
