package com.jparkie.aizoban.views.fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.data.downloads.DownloadService;
import com.jparkie.aizoban.modules.scoped.QueueFragmentModule;
import com.jparkie.aizoban.presenters.QueueFragmentPresenter;
import com.jparkie.aizoban.views.QueueFragmentView;
import com.jparkie.aizoban.views.fragments.base.BaseFragment;
import com.jparkie.aizoban.views.itemdecorations.DividerItemDecoration;
import com.jparkie.aizoban.views.viewholders.QueueListViewHolder;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class QueueFragment extends BaseFragment implements QueueFragmentView {
    public static final String TAG = QueueFragment.class.getSimpleName();

    @Inject
    QueueFragmentPresenter mQueueFragmentPresenter;

    @InjectView(R.id.queueRecyclerView)
    RecyclerView mQueueRecyclerView;

    @InjectView(R.id.emptyRelativeLayout)
    RelativeLayout mEmptyRelativeLayout;
    @InjectView(R.id.emptyImageView)
    ImageView mEmptyImageView;
    @InjectView(R.id.emptyTextView)
    TextView mEmptyTextView;
    @InjectView(R.id.instructionsTextView)
    TextView mInstructionsTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View queueFragmentView = inflater.inflate(R.layout.fragment_queue, container, false);

        ButterKnife.inject(this, queueFragmentView);

        return queueFragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mQueueFragmentPresenter.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        mQueueFragmentPresenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        mQueueFragmentPresenter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mQueueFragmentPresenter.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mQueueFragmentPresenter.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mQueueFragmentPresenter.onCreateOptionsMenu(menu, inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mQueueFragmentPresenter.onOptionsItemSelected(item);
    }

    // BaseFragment:

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new QueueFragmentModule(this)
        );
    }

    // QueueFragmentView:

    @Override
    public void initializeQueueToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_queue);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void initializeQueueRecyclerView() {
        if (mQueueRecyclerView != null) {
            mQueueRecyclerView.setHasFixedSize(false);
            mQueueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            mQueueRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mQueueRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        }
    }

    @Override
    public void initializeQueueEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyImageView != null && mEmptyTextView != null && mInstructionsTextView != null) {
            mEmptyImageView.setImageResource(R.drawable.ic_file_download_white_48dp);
            mEmptyImageView.setColorFilter(getResources().getColor(R.color.accentColor), PorterDuff.Mode.MULTIPLY);
            mEmptyTextView.setText(R.string.no_queued_downloads);
            mInstructionsTextView.setText(R.string.queue_downloads_instructions);
        }
    }

    @Override
    public void setAdapterForQueueRecyclerView(RecyclerView.Adapter<QueueListViewHolder> adapter) {
        if (mQueueRecyclerView != null && adapter != null) {
            mQueueRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public Parcelable saveQueueChapterLayoutManagerInstanceState() {
        if (mQueueRecyclerView != null && mQueueRecyclerView.getLayoutManager() != null) {
            RecyclerView.LayoutManager layoutManager = mQueueRecyclerView.getLayoutManager();

            return layoutManager.onSaveInstanceState();
        }

        return null;
    }

    @Override
    public void restoreQueueChapterLayoutManagerInstanceState(Parcelable state) {
        if (mQueueRecyclerView != null && mQueueRecyclerView.getLayoutManager() != null && state != null) {
            RecyclerView.LayoutManager layoutManager = mQueueRecyclerView.getLayoutManager();

            layoutManager.onRestoreInstanceState(state);
        }
    }

    @Override
    public int findQueueLayoutManagerFirstVisibleItemPosition() {
        if (mQueueRecyclerView != null && mQueueRecyclerView.getLayoutManager() != null && mQueueRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager)mQueueRecyclerView.getLayoutManager();

            return layoutManager.findFirstVisibleItemPosition();
        }

        return RecyclerView.NO_POSITION;
    }

    @Override
    public void showQueueEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyRelativeLayout.getVisibility() != View.VISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideQueueEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null&& mEmptyRelativeLayout.getVisibility() != View.INVISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void scrollToTop() {
        if (mQueueRecyclerView != null) {
            mQueueRecyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public boolean isDownloadServiceRunning() {
        boolean isServiceRunning = false;

        ActivityManager activityManager = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceInfo.service.getClassName().equals(DownloadService.class.getName())) {
                isServiceRunning = true;
                break;
            }
        }

        return isServiceRunning;
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
