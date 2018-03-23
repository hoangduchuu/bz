package com.ping.android.utils;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.view.View;

import com.ping.android.presentation.view.fragment.BaseFragment;

import java.util.List;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

/**
 * Helper class for navigating between fragments
 */
public class Navigator {
    private int containerId;
    //private int subContainerId;
    private FragmentManager mFragmentManager;
    private NavigationListener mNavigationListener;
    private String rootTag;

    /**
     * Initialize the NavigatorImpl with a FragmentManager, which will be used at the
     * fragment transactions.
     *
     * @param fragmentManager
     */
    public void init(FragmentManager fragmentManager, int containerId) {
        mFragmentManager = fragmentManager;
        mFragmentManager.addOnBackStackChangedListener(() -> {
            if (mNavigationListener != null) {
                mNavigationListener.onBackStackChanged();
            }
        });
        this.containerId = containerId;
    }

    /**
     * Displays the next fragment
     *
     * @param fragment
     */
    protected void open(BaseFragment fragment) {
        if (mFragmentManager != null) {
            //@formatter:off
            mFragmentManager.beginTransaction()
                    .add(containerId, fragment)
                    .addToBackStack(fragment.getClass().getName())
                    .commitAllowingStateLoss();
            //@formatter:on
        }
    }

    public void moveToFragment(BaseFragment fragment, int enterTransition, int exitTransition) {
        mFragmentManager.beginTransaction()
                .setCustomAnimations(enterTransition, 0, 0, exitTransition)
                .setTransition(enterTransition)
                .replace(containerId, fragment)
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }

    /**
     * pops every fragment and starts the given fragment as a new one.
     *
     * @param fragment
     */
    public void openAsRoot(BaseFragment fragment) {
        popEveryFragment();
        open(fragment);
        rootTag = fragment.getClass().getName();
    }


    /**
     * Pops all the queued fragments
     */
    private void popEveryFragment() {
        // Clear all back stack.
        int backStackCount = mFragmentManager.getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            // Get the back stack fragment id.
            int backStackId = mFragmentManager.getBackStackEntryAt(i).getId();
            mFragmentManager.popBackStack(backStackId, POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void popToRoot() {
        int backStackCount = mFragmentManager.getBackStackEntryCount();
        for (int i = 1; i < backStackCount; i++) {
            // Get the back stack fragment id.
            int backStackId = mFragmentManager.getBackStackEntryAt(i).getId();
            mFragmentManager.popBackStack(backStackId, POP_BACK_STACK_INCLUSIVE);
        }
    }

    public Fragment getFragmentByTag(String tag) {
        return mFragmentManager.findFragmentByTag(tag);
    }

    public void moveToFragment(BaseFragment fragment) {
        if (mFragmentManager != null) {
            String tag = fragment.getClass().getName();
            //boolean fragmentPopped = mFragmentManager.popBackStackImmediate(tag, POP_BACK_STACK_INCLUSIVE);
            mFragmentManager.beginTransaction()
                    .add(containerId, fragment, tag)
                    .addToBackStack(tag)
                    .commit();
        }
    }

    public void moveToFragment(BaseFragment fragment, List<Pair<View, String>> lstPair) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.replace(containerId, fragment);
            for (Pair<View, String> pair : lstPair) {
                transaction.addSharedElement(pair.first, pair.second);
            }
            transaction.addToBackStack(fragment.getClass().getName())
                    .commit();
    }

    /**
     * Navigates back by popping teh back stack. If there is no more items left we finish the current activity.
     *
     * @param baseActivity
     */
    public void navigateBack(Activity baseActivity) {
        if (!isRootFragmentVisible()) {
            mFragmentManager.popBackStackImmediate();
        } else if (baseActivity != null) {
            baseActivity.finish();
        }
    }

    public boolean canNavigateBack() {
        return !isRootFragmentVisible();
    }

    /**
     * @return true if the current fragment displayed is a root fragment
     */
    public boolean isRootFragmentVisible() {
        return mFragmentManager.getBackStackEntryCount() <= 1;
    }

    public BaseFragment getCurrentFragment() {
        Fragment fragment = mFragmentManager.findFragmentById(containerId);
        if (fragment != null) {
            if (!(fragment instanceof BaseFragment)) {
                throw new ClassCastException("Fragment must be an instance of BaseFragment");
            }
            return (BaseFragment) fragment;
        }
        return null;
    }

    public void showRoot() {
        int backStackCount = mFragmentManager.getBackStackEntryCount();
        if (backStackCount > 0) {
            int backStackId = mFragmentManager.getBackStackEntryAt(1).getId();
            mFragmentManager.popBackStack(backStackId, POP_BACK_STACK_INCLUSIVE);
        }
    }

    public NavigationListener getNavigationListener() {
        return mNavigationListener;
    }

    public void setNavigationListener(NavigationListener navigationListener) {
        mNavigationListener = navigationListener;
    }

    /**
     * Listener interface for navigation events.
     */
    interface NavigationListener {

        /**
         * Callback on backstack changed.
         */
        void onBackStackChanged();
    }
}
