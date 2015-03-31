package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.modules.scoped.CatalogueFragmentModule;
import com.jparkie.aizoban.presenters.CatalogueFragmentPresenter;
import com.jparkie.aizoban.views.CatalogueFragmentView;
import com.jparkie.aizoban.views.fragments.base.BaseFragment;
import com.jparkie.aizoban.views.viewholders.base.BaseCatalogueViewHolder;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CatalogueFragment extends BaseFragment implements CatalogueFragmentView {
    public static final String TAG = CatalogueFragment.class.getSimpleName();

    @Inject
    CatalogueFragmentPresenter mCatalogueFragmentPresenter;

    @InjectView(R.id.catalogueRecyclerView)
    RecyclerView mCatalogueRecyclerView;
    RecyclerView.ItemDecoration mCatalogueItemDecoration;

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
        View catalogueView = inflater.inflate(R.layout.fragment_catalogue, container, false);

        ButterKnife.inject(this, catalogueView);

        return catalogueView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCatalogueFragmentPresenter.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCatalogueFragmentPresenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        mCatalogueFragmentPresenter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCatalogueFragmentPresenter.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mCatalogueFragmentPresenter.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mCatalogueFragmentPresenter.onCreateOptionsMenu(menu, inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mCatalogueFragmentPresenter.onOptionsItemSelected(item);
    }

    // BaseFragment:

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new CatalogueFragmentModule(this)
        );
    }

    // CatalogueFragmentView:

    @Override
    public void initializeCatalogueToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_catalogue);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void initializeCatalogueRecyclerView() {
        if (mCatalogueRecyclerView != null) {
            mCatalogueRecyclerView.setHasFixedSize(false);
            mCatalogueRecyclerView.setItemAnimator(new DefaultItemAnimator());

            final int itemWidth = (int)getActivity().getResources().getDimension(R.dimen.grid_item_width);
            mCatalogueRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mCatalogueRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
                        final int measuredWidth = mCatalogueRecyclerView.getMeasuredWidth();
                        final int spanCount = (int) Math.floor(measuredWidth / itemWidth);

                        final GridLayoutManager gridLayoutManager = (GridLayoutManager) mCatalogueRecyclerView.getLayoutManager();
                        if (gridLayoutManager != null) {
                            gridLayoutManager.setSpanCount(spanCount);
                            gridLayoutManager.requestLayout();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void initializeCatalogueEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyImageView != null && mEmptyTextView != null && mInstructionsTextView != null) {
            mEmptyImageView.setImageResource(R.drawable.ic_photo_library_white_48dp);
            mEmptyImageView.setColorFilter(getResources().getColor(R.color.accentColor), PorterDuff.Mode.MULTIPLY);
            mEmptyTextView.setText(R.string.no_catalogue);
            mInstructionsTextView.setText(R.string.catalogue_instructions);
        }
    }

    @Override
    public void invalidateCatalogueRecyclerView() {
        if (mCatalogueRecyclerView != null) {
            mCatalogueRecyclerView.invalidate();
        }
    }

    @Override
    public void setAdapterForCatalogueRecyclerView(RecyclerView.Adapter<BaseCatalogueViewHolder> adapter) {
        if (mCatalogueRecyclerView != null && adapter != null) {
            mCatalogueRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void setLayoutManagerForCatalogueRecyclerView(RecyclerView.LayoutManager layoutManager) {
        if (mCatalogueRecyclerView != null && layoutManager != null) {
            mCatalogueRecyclerView.setLayoutManager(layoutManager);
        }
    }

    @Override
    public void setItemDecorationForCatalogueRecyclerView(RecyclerView.ItemDecoration itemDecoration) {
        if (mCatalogueRecyclerView != null && itemDecoration != null) {
            if (mCatalogueItemDecoration != null) {
                mCatalogueRecyclerView.removeItemDecoration(mCatalogueItemDecoration);
            }

            mCatalogueRecyclerView.addItemDecoration(itemDecoration);
            mCatalogueItemDecoration = itemDecoration;
        }
    }

    @Override
    public int getApproximateGridLayoutManagerSpanCount() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        final WindowManager windowManager = getActivity().getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        final float displayWidth = displayMetrics.widthPixels;
        final float itemWidth = getActivity().getResources().getDimensionPixelSize(R.dimen.grid_item_width);

        return (int)Math.floor(displayWidth / itemWidth);
    }

    @Override
    public Parcelable saveCatalogueLayoutManagerInstanceState() {
        if (mCatalogueRecyclerView != null && mCatalogueRecyclerView.getLayoutManager() != null) {
            RecyclerView.LayoutManager layoutManager = mCatalogueRecyclerView.getLayoutManager();

            return layoutManager.onSaveInstanceState();
        }

        return null;
    }

    @Override
    public void restoreCatalogueLayoutManagerInstanceState(Parcelable state) {
        if (mCatalogueRecyclerView != null && mCatalogueRecyclerView.getLayoutManager() != null && state != null) {
            RecyclerView.LayoutManager layoutManager = mCatalogueRecyclerView.getLayoutManager();

            layoutManager.onRestoreInstanceState(state);
        }
    }

    @Override
    public int findCatalogueLayoutManagerFirstVisibleItemPosition() {
        if (mCatalogueRecyclerView != null && mCatalogueRecyclerView.getLayoutManager() != null && mCatalogueRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager)mCatalogueRecyclerView.getLayoutManager();

            return layoutManager.findFirstVisibleItemPosition();
        }

        return RecyclerView.NO_POSITION;
    }

    @Override
    public void showCatalogueEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null && mEmptyRelativeLayout.getVisibility() != View.VISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideCatalogueEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null&& mEmptyRelativeLayout.getVisibility() != View.INVISIBLE) {
            mEmptyRelativeLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void scrollToTop() {
        if (mCatalogueRecyclerView != null) {
            mCatalogueRecyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
