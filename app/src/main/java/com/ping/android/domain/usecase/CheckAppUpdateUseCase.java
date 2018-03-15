package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.ping.android.activity.BuildConfig;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/15/18.
 */

public class CheckAppUpdateUseCase extends UseCase<CheckAppUpdateUseCase.Output, Void> {
    private FirebaseRemoteConfig remoteConfig;

    @Inject
    public CheckAppUpdateUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(!BuildConfig.DEBUG).build());
        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("adr_store_current_version", "0.0.0");
        defaults.put("adr_force_update_enabled", 0);
        defaults.put("adr_app_id", "com.ping.android");
        remoteConfig.setDefaults(defaults);
    }

    @NotNull
    @Override
    public Observable<Output> buildUseCaseObservable(Void aVoid) {
        return fetchConfigSettings();
    }

    private Observable<Output> fetchConfigSettings() {
        return Observable.create(emitter -> {
            final Task<Void> fetch = remoteConfig.fetch(0);
            fetch.addOnCompleteListener(task -> {
                remoteConfig.activateFetched();
                String storeCurrentVersion = remoteConfig.getString("adr_store_current_version");
                int forceUpdateEnabled = (int) remoteConfig.getLong("adr_force_update_enabled");
                String appId = remoteConfig.getString("adr_app_id");

                Output output = new Output();
                output.appId = appId;
                output.currentVersion = storeCurrentVersion;
                if (forceUpdateEnabled <= 0 || !versionCompare(storeCurrentVersion, BuildConfig.VERSION_NAME)) {
                    output.needUpdate = false;
                } else {
                    output.needUpdate = true;
                }
                emitter.onNext(output);
            });
        });
    }

    private boolean versionCompare(String storeVersion, String installedVersion) {
        String[] ver1s = storeVersion.split("\\.");
        String[] ver2s = installedVersion.split("\\.");

        return Integer.parseInt(ver1s[0]) > Integer.parseInt(ver2s[0])
                || Integer.parseInt(ver1s[1]) > Integer.parseInt(ver2s[1]);
    }

    public static class Output {
        public String appId;
        public String currentVersion;
        public boolean needUpdate;
    }
}
