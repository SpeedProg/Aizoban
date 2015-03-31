package com.jparkie.aizoban.modules.initial;

import com.jparkie.aizoban.data.databases.ApplicationSQLiteOpenHelper;
import com.jparkie.aizoban.data.databases.LibrarySQLiteOpenHelper;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.databases.QueryManagerImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public final class QueryModule {
    public static final String TAG = QueryModule.class.getSimpleName();

    @Provides
    @Singleton
    public QueryManager provideQueryManager(ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper, LibrarySQLiteOpenHelper librarySQLiteOpenHelper) {
        return new QueryManagerImpl(applicationSQLiteOpenHelper, librarySQLiteOpenHelper);
    }
}
