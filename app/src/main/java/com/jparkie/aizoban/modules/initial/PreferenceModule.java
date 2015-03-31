package com.jparkie.aizoban.modules.initial;

import android.app.Application;

import com.jparkie.aizoban.data.preferences.PreferenceManager;
import com.jparkie.aizoban.data.preferences.PreferenceManagerImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public final class PreferenceModule {
    public static final String TAG = PreferenceModule.class.getSimpleName();

    @Provides
    @Singleton
    public PreferenceManager providesPreferenceManager(Application application) {
        return new PreferenceManagerImpl(application);
    }
}
