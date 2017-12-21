package com.ping.android.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.activity.BaseActivity;
import com.ping.android.activity.CoreActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuanluong on 12/15/17.
 */

public class BaseFragment extends Fragment {
    protected Map<DatabaseReference, Object> databaseReferences = new HashMap<>();

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (DatabaseReference reference : databaseReferences.keySet()) {
            Object listener = databaseReferences.get(reference);
            if (listener instanceof ChildEventListener) {
                reference.removeEventListener((ChildEventListener) listener);
            } else if (listener instanceof ValueEventListener) {
                reference.removeEventListener((ValueEventListener) listener);
            }
        }
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
}
