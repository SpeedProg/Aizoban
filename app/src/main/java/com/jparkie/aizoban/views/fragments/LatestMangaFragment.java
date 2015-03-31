package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.modules.scoped.LatestMangaFragmentModule;
import com.jparkie.aizoban.presenters.LatestMangaFragmentPresenter;
import com.jparkie.aizoban.views.LatestMangaFragmentView;
import com.jparkie.aizoban.views.fragments.base.BaseFragment;
import com.jparkie.aizoban.views.itemdecorations.DividerItemDecoration;
import com.jparkie.aizoban.views.viewholders.LatestMangaListViewHolder;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LatestMangaFragment extends BaseFragment implements LatestMangaFragmentView {
    public static final String TAG = LatestMangaFragment.class.getSimpleName();

    @Inject
    LatestMangaFragmentPresenter mLatestMangaFragmentPresenter;

    @InjectView(R.id.latestMangaSwipeRefreshLayout)
    SwipeRefreshLayout mLatestMangaSwipeRefreshLayout;
    @InjectView(R.id.latestMangaRecyclerView)
    RecyclerView mLatestMangaRecyclerView;

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

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View latestMangaView = inflater.inflate(R.layout.fragment_latest_manga, container, false);

        ButterKnife.inject(this, latestMangaView);

        return latestMangaView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLatestMangaFragmentPresenter.onViewCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        mLatestMangaFragmentPresenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        mLatestMangaFragmentPresenter.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mLatestMangaFragmentPresenter.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLatestMangaFragmentPresenter = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mLatestMangaFragmentPresenter.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mLatestMangaFragmentPresenter.onCreateOptionsMenu(menu, inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mLatestMangaFragmentPresenter.onOptionsItemSelected(item);
    }

    // BaseFragment:

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new LatestMangaFragmentModule(this)
        );
    }

    // LatestMangaFragmentView:

    @Override
    public void initializeLatestMangaToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_latest_manga);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void initializeLatestMangaSwipeRefreshLayout() {
        if (mLatestMangaSwipeRefreshLayout != null) {
            mLatestMangaSwipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics()));
            mLatestMangaSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accentColor));
            mLatestMangaSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mLatestMangaFragmentPresenter.onSwipeRefreshLayoutRefreshing();
                }
            });
        }
    }

    @Override
    public void initializeLatestMangaRecyclerView() {
        if (mLatestMangaRecyclerView != null) {
            mLatestMangaRecyclerView.setHasFixedSize(false);
            mLatestMangaRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            mLatestMangaRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mLatestMangaRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        }
    }

    @Override
    public void initializeLatestMangaEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyImageView != null && mEmptyTextView != null && mInstructionsTextView != null) {
            mEmptyImageView.setImageResource(R.drawable.ic_new_releases_white_48dp);
            mEmptyImageView.setColorFilter(getResources().getColor(R.color.accentColor), PorterDuff.Mode.MULTIPLY);
            mEmptyTextView.setText(R.string.no_latest_manga);
            mInstructionsTextView.setText(R.string.latest_manga_instructions);
        }
    }

    @Override
    public void setAdapterForLatestMangaRecyclerView(RecyclerView.Adapter<LatestMangaListViewHolder> adapter) {
        if (mLatestMangaRecyclerView != null && adapter != null) {
            mLatestMangaRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public Parcelable saveLatestMangaLayoutManagerInstanceState() {
        if (mLatestMangaRecyclerView != null && mLatestMangaRecyclerView.getLayoutManager() != null) {
            RecyclerView.LayoutManager layoutManager = mLatestMangaRecyclerView.getLayoutManager();

            return layoutManager.onSaveInstanceState();
        }
        
        return null;
    }

    @Override
    public void restoreLatestMangaLayoutManagerInstanceState(Parcelable state) {
        if (mLatestMangaRecyclerView != null && mLatestMangaRecyclerView.getLayoutManager() != null && state != null) {
            RecyclerView.LayoutManager layoutManager = mLatestMangaRecyclerView.getLayoutManager();

            layoutManager.onRestoreInstanceState(state);
        }
    }

    @Override
    public int findLatestMangaLayoutManagerFirstVisibleItemPosition() {
        if (mLatestMangaRecyclerView != null && mLatestMangaRecyclerView.getLayoutManager() != null && mLatestMangaRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager)mLatestMangaRecyclerView.getLayoutManager();

            return layoutManager.findFirstVisibleItemPosition();
        }

        return RecyclerView.NO_POSITION;
    }

    @Override
    public boolean isLatestMangaSwipeRefreshLayoutRefreshing() {
        return mLatestMangaSwipeRefreshLayout != null && mLatestMangaSwipeRefreshLayout.isRefreshing();
    }

    @Override
    public void showLatestMangaSwipeRefreshLayoutRefreshing() {
        if (mLatestMangaSwipeRefreshLayout != null) {
            mLatestMangaSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void hideLatestMangaSwipeRefreshLayoutRefreshing() {
        if (mLatestMangaSwipeRefreshLayout != null) {
            mLatestMangaSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void showLatestMangaEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyRelativeLayout.getVisibility() != View.VISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLatestMangaEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null&& mEmptyRelativeLayout.getVisibility() != View.INVISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void scrollToTop() {
        if (mLatestMangaRecyclerView != null) {
            mLatestMangaRecyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void toastLatestMangaError() {
        Toast.makeText(getActivity(), R.string.toast_latest_manga_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
