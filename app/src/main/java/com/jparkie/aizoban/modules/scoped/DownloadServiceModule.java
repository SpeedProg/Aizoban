package com.jparkie.aizoban.modules.scoped;

import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.downloads.DownloadController;
import com.jparkie.aizoban.data.downloads.DownloadManager;
import com.jparkie.aizoban.data.downloads.DownloadManagerImpl;
import com.jparkie.aizoban.data.downloads.DownloadService;
import com.jparkie.aizoban.data.networks.NetworkService;
import com.jparkie.aizoban.data.preferences.PreferenceManager;
import com.jparkie.aizoban.modules.AizobanModule;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = DownloadService.class,
        addsTo = AizobanModule.class,
        complete = false
)
public final class DownloadServiceModule {
    public static final String TAG = DownloadServiceModule.class.getSimpleName();

    private DownloadController mDownloadController;

    public DownloadServiceModule(DownloadController downloadController) {
        mDownloadController = downloadController;
    }

    @Provides
    public DownloadController provideDownloadController() {
        return mDownloadController;
    }

    @Provides
    public DownloadManager provideDownloadManager(DownloadController downloadController, AizobanRepository aizobanRepository, NetworkService networkService, PreferenceManager preferenceManager, QueryManager queryManager) {
        return new DownloadManagerImpl(downloadController, aizobanRepository, networkService, preferenceManager, queryManager);
    }
}
