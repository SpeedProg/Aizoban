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
import com.jparkie.aizoban.modules.scoped.RecentChapterFragmentModule;
import com.jparkie.aizoban.presenters.RecentChapterFragmentPresenter;
import com.jparkie.aizoban.views.RecentChapterFragmentView;
import com.jparkie.aizoban.views.fragments.base.BaseFragment;
import com.jparkie.aizoban.views.itemdecorations.DividerItemDecoration;
import com.jparkie.aizoban.views.viewholders.RecentChapterListViewHolder;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RecentChapterFragment extends BaseFragment implements RecentChapterFragmentView {
    public static final String TAG = RecentChapterFragment.class.getSimpleName();

    @Inject
    RecentChapterFragmentPresenter mRecentChapterFragmentPresenter;

    @InjectView(R.id.recentChapterRecyclerView)
    RecyclerView mRecentChapterRecyclerView;

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
        View recentChapterView = inflater.inflate(R.layout.fragment_recent_chapter, container, false);

        ButterKnife.inject(this, recentChapterView);

        return recentChapterView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecentChapterFragmentPresenter.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRecentChapterFragmentPresenter.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mRecentChapterFragmentPresenter.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mRecentChapterFragmentPresenter.onCreateOptionsMenu(menu, inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mRecentChapterFragmentPresenter.onOptionsItemSelected(item);
    }

    // BaseFragment:

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new RecentChapterFragmentModule(this)
        );
    }

    // RecentChapterFragmentView:

    @Override
    public void initializeRecentChapterToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_recent_chapter);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void initializeRecentChapterRecyclerView() {
        if (mRecentChapterRecyclerView != null) {
            mRecentChapterRecyclerView.setHasFixedSize(false);
            mRecentChapterRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            mRecentChapterRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecentChapterRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        }
    }

    @Override
    public void initializeRecentChapterEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyImageView != null && mEmptyTextView != null && mInstructionsTextView != null) {
            mEmptyImageView.setImageResource(R.drawable.ic_history_white_48dp);
            mEmptyImageView.setColorFilter(getResources().getColor(R.color.accentColor), PorterDuff.Mode.MULTIPLY);
            mEmptyTextView.setText(R.string.no_recent_chapter);
            mInstructionsTextView.setText(R.string.recent_chapter_instructions);
        }
    }

    @Override
    public void setAdapterForRecentChapterRecyclerView(RecyclerView.Adapter<RecentChapterListViewHolder> adapter) {
        if (mRecentChapterRecyclerView != null && adapter != null) {
            mRecentChapterRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public Parcelable saveRecentChapterLayoutManagerInstanceState() {
        if (mRecentChapterRecyclerView != null && mRecentChapterRecyclerView.getLayoutManager() != null) {
            RecyclerView.LayoutManager layoutManager = mRecentChapterRecyclerView.getLayoutManager();

            return layoutManager.onSaveInstanceState();
        }

        return null;
    }

    @Override
    public void restoreRecentChapterLayoutManagerInstanceState(Parcelable state) {
        if (mRecentChapterRecyclerView != null && mRecentChapterRecyclerView.getLayoutManager() != null && state != null) {
            RecyclerView.LayoutManager layoutManager = mRecentChapterRecyclerView.getLayoutManager();

            layoutManager.onRestoreInstanceState(state);
        }
    }

    @Override
    public int findRecentChapterLayoutManagerFirstVisibleItemPosition() {
        if (mRecentChapterRecyclerView != null && mRecentChapterRecyclerView.getLayoutManager() != null && mRecentChapterRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager)mRecentChapterRecyclerView.getLayoutManager();

            return layoutManager.findFirstVisibleItemPosition();
        }

        return RecyclerView.NO_POSITION;
    }

    @Override
    public void showRecentChapterEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyRelativeLayout.getVisibility() != View.VISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideRecentChapterEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null&& mEmptyRelativeLayout.getVisibility() != View.INVISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void scrollToTop() {
        if (mRecentChapterRecyclerView != null) {
            mRecentChapterRecyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
