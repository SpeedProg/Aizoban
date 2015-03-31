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
import com.jparkie.aizoban.modules.scoped.FavouriteMangaFragmentModule;
import com.jparkie.aizoban.presenters.FavouriteMangaFragmentPresenter;
import com.jparkie.aizoban.views.FavouriteMangaFragmentView;
import com.jparkie.aizoban.views.fragments.base.BaseFragment;
import com.jparkie.aizoban.views.itemdecorations.DividerItemDecoration;
import com.jparkie.aizoban.views.viewholders.FavouriteMangaListViewHolder;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FavouriteMangaFragment extends BaseFragment implements FavouriteMangaFragmentView{
    public static final String TAG = FavouriteMangaFragment.class.getSimpleName();

    @Inject
    FavouriteMangaFragmentPresenter mFavouriteMangaFragmentPresenter;

    @InjectView(R.id.favouriteMangaRecyclerView)
    RecyclerView mFavouriteMangaRecyclerView;

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
        View favouriteMangaView = inflater.inflate(R.layout.fragment_favourite_manga, container, false);

        ButterKnife.inject(this, favouriteMangaView);

        return favouriteMangaView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFavouriteMangaFragmentPresenter.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mFavouriteMangaFragmentPresenter.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mFavouriteMangaFragmentPresenter.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mFavouriteMangaFragmentPresenter.onCreateOptionsMenu(menu, inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mFavouriteMangaFragmentPresenter.onOptionsItemSelected(item);
    }

    // BaseFragment;

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new FavouriteMangaFragmentModule(this)
        );
    }

    // FavouriteMangaFragmentView:

    @Override
    public void initializeFavouriteMangaToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_favourite_manga);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void initializeFavouriteMangaRecyclerView() {
        if (mFavouriteMangaRecyclerView != null) {
            mFavouriteMangaRecyclerView.setHasFixedSize(false);
            mFavouriteMangaRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            mFavouriteMangaRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mFavouriteMangaRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        }
    }

    @Override
    public void initializeFavouriteMangaEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyImageView != null && mEmptyTextView != null && mInstructionsTextView != null) {
            mEmptyImageView.setImageResource(R.drawable.ic_favourite_white_48dp);
            mEmptyImageView.setColorFilter(getResources().getColor(R.color.accentColor), PorterDuff.Mode.MULTIPLY);
            mEmptyTextView.setText(R.string.no_favourite_manga);
            mInstructionsTextView.setText(R.string.favourite_manga_instructions);
        }
    }

    @Override
    public void setAdapterForFavouriteMangaRecyclerView(RecyclerView.Adapter<FavouriteMangaListViewHolder> adapter) {
        if (mFavouriteMangaRecyclerView != null && adapter != null) {
            mFavouriteMangaRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public Parcelable saveFavouriteMangaLayoutManagerInstanceState() {
        if (mFavouriteMangaRecyclerView != null && mFavouriteMangaRecyclerView.getLayoutManager() != null) {
            RecyclerView.LayoutManager layoutManager = mFavouriteMangaRecyclerView.getLayoutManager();

            return layoutManager.onSaveInstanceState();
        }

        return null;
    }

    @Override
    public void restoreFavouriteMangaLayoutManagerInstanceState(Parcelable state) {
        if (mFavouriteMangaRecyclerView != null && mFavouriteMangaRecyclerView.getLayoutManager() != null && state != null) {
            RecyclerView.LayoutManager layoutManager = mFavouriteMangaRecyclerView.getLayoutManager();

            layoutManager.onRestoreInstanceState(state);
        }
    }

    @Override
    public int findFavouriteMangaLayoutManagerFirstVisibleItemPosition() {
        if (mFavouriteMangaRecyclerView != null && mFavouriteMangaRecyclerView.getLayoutManager() != null && mFavouriteMangaRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager)mFavouriteMangaRecyclerView.getLayoutManager();

            return layoutManager.findFirstVisibleItemPosition();
        }

        return RecyclerView.NO_POSITION;
    }

    @Override
    public void showFavouriteMangaEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyRelativeLayout.getVisibility() != View.VISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideFavouriteMangaEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null&& mEmptyRelativeLayout.getVisibility() != View.INVISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void scrollToTop() {
        if (mFavouriteMangaRecyclerView != null) {
            mFavouriteMangaRecyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
