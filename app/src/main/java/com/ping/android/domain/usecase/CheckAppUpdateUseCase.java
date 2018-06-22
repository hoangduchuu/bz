package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.ping.android.BuildConfig;
import com.ping.android.utils.CommonMethod;

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
                output.needUpdate = forceUpdateEnabled > 0 && !CommonMethod.checkVersionValid(BuildConfig.VERSION_NAME, storeCurrentVersion);
                emitter.onNext(output);
            });
        });
    }

    public static class Output {
        public String appId;
        public String currentVersion;
        public boolean needUpdate;
    }
}
