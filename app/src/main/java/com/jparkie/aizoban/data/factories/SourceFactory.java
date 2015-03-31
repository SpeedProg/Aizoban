package com.jparkie.aizoban.data.factories;

import com.jparkie.aizoban.data.sources.Source;

public interface SourceFactory {
    public Source constructSourceFromPreferences();

    public Source constructSourceFromName(String sourceName);

    public Source constructSourceFromUrl(String url);
}
