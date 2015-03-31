package com.jparkie.aizoban.modules.scoped;

import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.modules.AizobanModule;
import com.jparkie.aizoban.presenters.QueueFragmentPresenter;
import com.jparkie.aizoban.presenters.QueueFragmentPresenterImpl;
import com.jparkie.aizoban.views.QueueFragmentView;
import com.jparkie.aizoban.views.fragments.QueueFragment;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = QueueFragment.class,
        addsTo = AizobanModule.class,
        complete = false
)
public final class QueueFragmentModule {
    public static final String TAG = QueueFragmentModule.class.getSimpleName();

    private QueueFragmentView mQueueFragmentView;

    public QueueFragmentModule(QueueFragmentView queueFragmentView) {
        mQueueFragmentView = queueFragmentView;
    }

    @Provides
    public QueueFragmentView provideQueueFragmentView() {
        return mQueueFragmentView;
    }

    @Provides
    public QueueFragmentPresenter provideQueueFragmentPresenter(QueueFragmentView queueFragmentView, QueryManager queryManager) {
        return new QueueFragmentPresenterImpl(queueFragmentView, queryManager);
    }
}
