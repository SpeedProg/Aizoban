package com.jparkie.aizoban.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.utils.PreferenceUtils;
import com.jparkie.aizoban.views.adapters.CatalogueAdapter;

import rx.Observable;
import rx.Subscriber;

public final class PreferenceManagerImpl implements PreferenceManager {
    public static final String TAG = PreferenceManagerImpl.class.getSimpleName();

    private Context mContext;

    public PreferenceManagerImpl(Context context) {
        mContext = context;

        initializePreferences();
    }

    private void initializePreferences() {
        android.preference.PreferenceManager.setDefaultValues(mContext, R.xml.preferences, false);

        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPreferences.getString(mContext.getString(R.string.preference_download_storage_key), null) == null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(mContext.getString(R.string.preference_download_storage_key), mContext.getFilesDir().getAbsolutePath());
            editor.commit();
        }
    }

    @Override
    public Observable<Integer> getCatalogueRecyclerViewType() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    subscriber.onNext(sharedPreferences.getInt(PreferenceUtils.CATALOGUE_VIEW_TYPE_KEY, CatalogueAdapter.VIEW_TYPE_GRID_ITEM));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> setCatalogueRecyclerViewType(final int viewType) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(PreferenceUtils.CATALOGUE_VIEW_TYPE_KEY, viewType);
                    subscriber.onNext(editor.commit());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> getStartupScreen() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    subscriber.onNext(Integer.valueOf(sharedPreferences.getString(mContext.getString(R.string.preference_startup_key), mContext.getString(R.string.preference_startup_default_value))));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> setStartupScreen(final int startupScreen) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(mContext.getString(R.string.preference_startup_key), String.valueOf(startupScreen));
                    subscriber.onNext(editor.commit());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<String> getSource() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    subscriber.onNext(sharedPreferences.getString(mContext.getString(R.string.preference_source_key), mContext.getString(R.string.preference_source_default_value)));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> setSource(final String sourceName) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(mContext.getString(R.string.preference_source_key), sourceName);
                    subscriber.onNext(editor.commit());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> getIsSortChapterAscending() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    subscriber.onNext(sharedPreferences.getBoolean(PreferenceUtils.SORT_CHAPTERS_ASCENDING_KEY, false));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> setIsSortChapterAscending(final boolean isAscending) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(PreferenceUtils.SORT_CHAPTERS_ASCENDING_KEY, isAscending);
                    subscriber.onNext(editor.commit());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> getIsLazyLoading() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    subscriber.onNext(sharedPreferences.getBoolean(mContext.getString(R.string.preference_lazy_loading_key), false));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> setIsLazyLoading(final boolean isLazyLoading) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(mContext.getString(R.string.preference_lazy_loading_key), isLazyLoading);
                    subscriber.onNext(editor.commit());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> getIsRightToLeftDirection() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    subscriber.onNext(sharedPreferences.getBoolean(mContext.getString(R.string.preference_direction_key), false));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> setIsRightToLeftDirection(final boolean isRightToLeftDirection) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(mContext.getString(R.string.preference_direction_key), isRightToLeftDirection);
                    subscriber.onNext(editor.commit());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> getIsLockOrientation() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    subscriber.onNext(sharedPreferences.getBoolean(mContext.getString(R.string.preference_orientation_key), false));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> setIsLockOrientation(final boolean isLockOrientation) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(mContext.getString(R.string.preference_orientation_key), isLockOrientation);
                    subscriber.onNext(editor.commit());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> getIsLockZoom() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    subscriber.onNext(sharedPreferences.getBoolean(mContext.getString(R.string.preference_zoom_key), false));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> setIsLockZoom(final boolean isLockZoom) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(mContext.getString(R.string.preference_zoom_key), isLockZoom);
                    subscriber.onNext(editor.commit());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> getIsWiFiOnly() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    subscriber.onNext(sharedPreferences.getBoolean(mContext.getString(R.string.preference_download_wifi_key), true));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> setIsWiFiOnly(final boolean isWiFiOnly) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(mContext.getString(R.string.preference_download_wifi_key), isWiFiOnly);
                    subscriber.onNext(editor.commit());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> getIsExternalStorage() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    String preferenceDirectory = sharedPreferences.getString(mContext.getString(R.string.preference_download_storage_key), null);
                    String internalDirectory = mContext.getFilesDir().getAbsolutePath();

                    subscriber.onNext(!preferenceDirectory.equals(internalDirectory));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<String> getDownloadDirectory() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    subscriber.onNext(sharedPreferences.getString(mContext.getString(R.string.preference_download_storage_key), mContext.getFilesDir().getAbsolutePath()));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> setDownloadDirectory(final String downloadDirectory) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(mContext.getString(R.string.preference_download_storage_key), downloadDirectory);
                    subscriber.onNext(editor.commit());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
