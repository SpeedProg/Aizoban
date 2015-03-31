package com.jparkie.aizoban.modules.initial;

import android.app.Application;

import com.jparkie.aizoban.data.databases.ApplicationSQLiteOpenHelper;
import com.jparkie.aizoban.data.databases.LibrarySQLiteOpenHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public final class DatabaseModule {
    public static final String TAG = DatabaseModule.class.getSimpleName();

    @Provides
    @Singleton
    public ApplicationSQLiteOpenHelper provideApplicationSQLiteOpenHelper(Application application) {
        return new ApplicationSQLiteOpenHelper(application);
    }

    @Provides
    @Singleton
    public LibrarySQLiteOpenHelper provideLibrarySQLiteOpenHelper(Application application) {
        return new LibrarySQLiteOpenHelper(application);
    }
}
