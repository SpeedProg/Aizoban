package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.modules.scoped.NavigationFragmentModule;
import com.jparkie.aizoban.presenters.NavigationFragmentPresenter;
import com.jparkie.aizoban.views.NavigationFragmentView;
import com.jparkie.aizoban.views.fragments.base.BaseFragment;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

public class NavigationFragment extends BaseFragment implements NavigationFragmentView {
    public static final String TAG = NavigationFragment.class.getSimpleName();

    public static final String POSITION_ARGUMENT_KEY = TAG + ":" + "PositionArgumentKey";

    @Inject
    NavigationFragmentPresenter mNavigationFragmentPresenter;

    @InjectView(R.id.navigationListView)
    ListView mNavigationListView;
    @InjectView(R.id.navigationThumbnailImageView)
    ImageView mThumbnailImageView;
    @InjectView(R.id.navigationSourceTextView)
    TextView mSourceTextView;

    public static NavigationFragment newInstance(int initialPosition) {
        NavigationFragment newInstance = new NavigationFragment();

        Bundle arguments = new Bundle();
        arguments.putInt(POSITION_ARGUMENT_KEY, initialPosition);
        newInstance.setArguments(arguments);

        return newInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View navigationView = inflater.inflate(R.layout.fragment_navigation, container, false);

        ButterKnife.inject(this, navigationView);

        return navigationView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNavigationFragmentPresenter.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        mNavigationFragmentPresenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        mNavigationFragmentPresenter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mNavigationFragmentPresenter.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mNavigationFragmentPresenter.onSaveInstanceState(outState);
    }

    @OnItemClick(R.id.navigationListView)
    public void onNavigationItemClick(int position) {
        mNavigationFragmentPresenter.onNavigationItemClick(position);
    }

    // BaseFragment:

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new NavigationFragmentModule(this)
        );
    }

    // NavigationFragmentView:

    @Override
    public void setAdapterForListView(BaseAdapter adapter) {
        if (mNavigationListView != null) {
            mNavigationListView.setAdapter(adapter);
        }
    }

    @Override
    public void setSourceTextView(String source) {
        if (mSourceTextView != null) {
            mSourceTextView.setText(source.toUpperCase());
        }
    }

    @Override
    public void setThumbnailImageView(String imageUrl) {
        if (mThumbnailImageView != null) {
            mThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mThumbnailImageView.setColorFilter(getResources().getColor(R.color.accentColor), PorterDuff.Mode.MULTIPLY);

            Glide.with(getActivity())
                    .load(imageUrl)
                    .animate(android.R.anim.fade_in)
                    .into(mThumbnailImageView);
        }
    }

    @Override
    public void highlightPosition(int position) {
        if (mNavigationListView != null) {
            mNavigationListView.setItemChecked(position, true);
        }
    }

    @Override
    public Bundle getParameters() {
        return getArguments();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
