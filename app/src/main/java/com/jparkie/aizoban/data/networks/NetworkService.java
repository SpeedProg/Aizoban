package com.jparkie.aizoban.data.networks;

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Response;

import rx.Observable;

public interface NetworkService {
    public Observable<Response> getResponse(String url, CacheControl cacheControl, Headers headers);

    public Observable<String> mapResponseToString(Response response);
}
