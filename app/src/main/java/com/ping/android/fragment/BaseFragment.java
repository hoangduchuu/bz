package com.ping.android.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.ping.android.activity.BaseActivity;
import com.ping.android.activity.CoreActivity;

/**
 * Created by tuanluong on 12/15/17.
 */

public class BaseFragment extends Fragment {
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
