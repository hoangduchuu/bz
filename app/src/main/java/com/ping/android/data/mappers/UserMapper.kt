package com.ping.android.data.mappers;

import com.ping.android.model.User
import javax.inject.Inject

/**
 * Created by Huu Hoang on 20/12/2018
 */
class UserMapper @Inject constructor() {
    fun getUserDisplay(user : User):String{
        if (user.nickName != null){
          return user.nickName
        }

        if (user.firstName !=null){
            return user.firstName
        }
        if (user.lastName !=null){
            return user.lastName
        }
        return ""
    }
}