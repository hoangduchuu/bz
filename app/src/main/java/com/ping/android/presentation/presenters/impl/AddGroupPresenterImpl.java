package com.ping.android.presentation.presenters.impl;

import com.ping.android.domain.usecase.group.UploadGroupProfileImageUseCase;
import com.ping.android.presentation.presenters.AddGroupPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/8/18.
 */

public class AddGroupPresenterImpl implements AddGroupPresenter {
    @Inject
    UploadGroupProfileImageUseCase uploadGroupProfileImageUseCase;

    @Inject
    public AddGroupPresenterImpl() {}

    @Override
    public void createGroup(String profileImage) {

    }
}
