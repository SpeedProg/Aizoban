package com.jparkie.aizoban.modules.initial;

import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.AizobanRepositoryImpl;
import com.jparkie.aizoban.data.factories.SourceFactory;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public final class RepositoryModule {
    public static final String TAG = RepositoryModule.class.getSimpleName();

    @Provides
    public AizobanRepository provideRepository(SourceFactory sourceFactory) {
        return new AizobanRepositoryImpl(sourceFactory);
    }
}
