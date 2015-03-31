package com.jparkie.aizoban.modules.initial;

import com.jparkie.aizoban.data.networks.NetworkService;
import com.jparkie.aizoban.data.networks.NetworkServiceImpl;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public final class NetworkModule {
    public static final String TAG = NetworkModule.class.getSimpleName();

    public static final CacheControl NULL_CACHE_CONTROL = new CacheControl.Builder().noCache().build();
    public static final Headers NULL_HEADERS = new Headers.Builder().build();

    @Provides
    public NetworkService provideNetworkService(OkHttpClient client) {
        return new NetworkServiceImpl(client);
    }
}
