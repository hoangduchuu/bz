package com.ping.android.domain.repository;

import com.ping.android.model.User;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/23/18.
 */

public interface SearchRepository {
    Observable<List<User>> searchUsers(String text);
}
