package com.ping.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.scopes.HasComponent;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.activity.BaseActivity;
import com.ping.android.activity.CoreActivity;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by tuanluong on 12/15/17.
 */

public class BaseFragment extends Fragment {
    protected Map<DatabaseReference, Object> databaseReferences = new HashMap<>();
    // Disposable for UI events
    private CompositeDisposable disposables;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposables = new CompositeDisposable();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getPresenter() != null) {
            getPresenter().resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getPresenter() != null) {
            getPresenter().destroy();
        }
        disposables.dispose();
        for (DatabaseReference reference : databaseReferences.keySet()) {
            Object listener = databaseReferences.get(reference);
            if (listener instanceof ChildEventListener) {
                reference.removeEventListener((ChildEventListener) listener);
            } else if (listener instanceof ValueEventListener) {
                reference.removeEventListener((ValueEventListener) listener);
            }
        }
    }

    protected void registerEvent(Disposable disposable) {
        disposables.add(disposable);
    }

    public void showLoading() {
        Activity activity = getActivity();
        if (activity instanceof CoreActivity) {
            ((CoreActivity) activity).showLoading();
        }
    }

    public void hideLoading() {
        Activity activity = getActivity();
        if (activity instanceof CoreActivity) {
            ((CoreActivity) activity).hideLoading();
        }
    }

    protected BasePresenter getPresenter() {
        return null;
    }

    /**
     * Gets component for dependency injection by its mode
     *
     * @param componentType
     * @param <C>
     * @return
     */
    protected <C> C getComponent(Class<C> componentType) {
        return componentType.cast(((HasComponent<C>) getActivity()).getComponent());
    }
}
