package com.animalloo;

import android.app.Application;

import com.animalloo.util.RepositoryProvider;

public class AnimalLooApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RepositoryProvider.init(this);
    }

    @Override
    public void onTerminate() {
        RepositoryProvider.shutdownIfInitialized();
        super.onTerminate();
    }
}
