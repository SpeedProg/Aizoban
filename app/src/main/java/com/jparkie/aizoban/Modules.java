package com.jparkie.aizoban;

import com.jparkie.aizoban.modules.AizobanModule;

import java.util.Arrays;
import java.util.List;

public final class Modules {
    public static final String TAG = Modules.class.getSimpleName();

    private Modules() {
        throw new AssertionError(TAG + ": Cannot be initialized.");
    }

    public static List<Object> getModules(AizobanApplication application) {
        return Arrays.<Object>asList(
                new AizobanModule(application)
        );
    }
}
