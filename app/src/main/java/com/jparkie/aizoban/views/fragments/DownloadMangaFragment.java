package com.jparkie.aizoban.views.fragments;

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
import com.jparkie.aizoban.modules.scoped.DownloadMangaFragmentModule;
import com.jparkie.aizoban.presenters.DownloadMangaFragmentPresenter;
import com.jparkie.aizoban.views.DownloadMangaFragmentView;
import com.jparkie.aizoban.views.fragments.base.BaseFragment;
import com.jparkie.aizoban.views.itemdecorations.DividerItemDecoration;
import com.jparkie.aizoban.views.viewholders.DownloadMangaListViewHolder;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DownloadMangaFragment extends BaseFragment implements DownloadMangaFragmentView {
    public static final String TAG = DownloadMangaFragment.class.getSimpleName();

    @Inject
    DownloadMangaFragmentPresenter mDownloadMangaFragmentPresenter;

    @InjectView(R.id.downloadMangaRecyclerView)
    RecyclerView mDownloadMangaRecyclerView;

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
        View downloadMangaView = inflater.inflate(R.layout.fragment_download_manga, container, false);

        ButterKnife.inject(this, downloadMangaView);

        return downloadMangaView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDownloadMangaFragmentPresenter.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        mDownloadMangaFragmentPresenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        mDownloadMangaFragmentPresenter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDownloadMangaFragmentPresenter.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mDownloadMangaFragmentPresenter.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mDownloadMangaFragmentPresenter.onCreateOptionsMenu(menu, inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDownloadMangaFragmentPresenter.onOptionsItemSelected(item);
    }

    // BaseFragment:

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new DownloadMangaFragmentModule(this)
        );
    }

    // DownloadMangaFragmentView:

    @Override
    public void initializeDownloadMangaToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_download_manga);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void initializeDownloadMangaRecyclerView() {
        if (mDownloadMangaRecyclerView != null) {
            mDownloadMangaRecyclerView.setHasFixedSize(false);
            mDownloadMangaRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            mDownloadMangaRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mDownloadMangaRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        }
    }

    @Override
    public void initializeDownloadMangaEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyImageView != null && mEmptyTextView != null && mInstructionsTextView != null) {
            mEmptyImageView.setImageResource(R.drawable.ic_file_download_white_48dp);
            mEmptyImageView.setColorFilter(getResources().getColor(R.color.accentColor), PorterDuff.Mode.MULTIPLY);
            mEmptyTextView.setText(R.string.no_download_manga);
            mInstructionsTextView.setText(R.string.download_manga_instructions);
        }
    }

    @Override
    public void setAdapterForDownloadMangaRecyclerView(RecyclerView.Adapter<DownloadMangaListViewHolder> adapter) {
        if (mDownloadMangaRecyclerView != null && adapter != null) {
            mDownloadMangaRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public Parcelable saveDownloadMangaLayoutManagerInstanceState() {
        if (mDownloadMangaRecyclerView != null && mDownloadMangaRecyclerView.getLayoutManager() != null) {
            RecyclerView.LayoutManager layoutManager = mDownloadMangaRecyclerView.getLayoutManager();

            return layoutManager.onSaveInstanceState();
        }

        return null;
    }

    @Override
    public void restoreDownloadMangaLayoutManagerInstanceState(Parcelable state) {
        if (mDownloadMangaRecyclerView != null && mDownloadMangaRecyclerView.getLayoutManager() != null && state != null) {
            RecyclerView.LayoutManager layoutManager = mDownloadMangaRecyclerView.getLayoutManager();

            layoutManager.onRestoreInstanceState(state);
        }
    }

    @Override
    public int findDownloadMangaLayoutManagerFirstVisibleItemPosition() {
        if (mDownloadMangaRecyclerView != null && mDownloadMangaRecyclerView.getLayoutManager() != null && mDownloadMangaRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager)mDownloadMangaRecyclerView.getLayoutManager();

            return layoutManager.findFirstVisibleItemPosition();
        }

        return RecyclerView.NO_POSITION;
    }

    @Override
    public void showDownloadMangaEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyRelativeLayout.getVisibility() != View.VISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideDownloadMangaEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null&& mEmptyRelativeLayout.getVisibility() != View.INVISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void scrollToTop() {
        if (mDownloadMangaRecyclerView != null) {
            mDownloadMangaRecyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
